package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class SpriteSequence(
    val frames: List<File>,
    val frameDurationMs: Long,
    val width: Int,
    val height: Int
)

object SpriteFrameCache {
    private const val FPS = 12
    private const val FRAME_DURATION_MS = 1000L / FPS
    private const val CACHE_VERSION = "rgba-v1"

    private val worker = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val lock = Any()

    private val pending =
        mutableMapOf<
            String,
            MutableList<(Result<SpriteSequence>) -> Unit>
        >()

    private val bitmapCache =
        object : LruCache<String, Bitmap>(
            96 * 1024 * 1024
        ) {
            override fun sizeOf(
                key: String,
                value: Bitmap
            ): Int =
                value.allocationByteCount
        }

    fun memoryBytes(): Int =
        bitmapCache.size()

    fun bitmapFor(
        file: File
    ): Bitmap? {
        val key = file.absolutePath

        bitmapCache.get(key)
            ?.let { return it }

        val decoded =
            BitmapFactory.decodeFile(key)
                ?: return null

        bitmapCache.put(key, decoded)
        return decoded
    }

    fun prewarmGecko(
        context: Context,
        resolution: SpriteResolution
    ) {
        val profile = MascotAnimationProfiles.gecko

        val assets =
            buildList {
                profile.appearanceAsset?.let(::add)
                profile.disappearanceAsset?.let(::add)
                addAll(profile.idleAssets)
                addAll(profile.cuteAssets)
            }.distinct()

        MediaTrace.event(
            source = "SpriteFrameCache",
            event = "PREWARM_REQUEST",
            detail =
                "assets=" + assets.size +
                    " resolution=" +
                    resolution.label
        )

        assets.forEach { asset ->
            prepare(
                context = context,
                assetPath = asset,
                keyColor = profile.keyColor,
                resolution = resolution
            ) { result ->
                result.exceptionOrNull()?.let { error ->
                    MediaTrace.event(
                        source = "SpriteFrameCache",
                        event = "PREWARM_ERROR",
                        assetPath = asset,
                        detail =
                            "error=" +
                                error.javaClass.simpleName
                    )
                }
            }
        }
    }

    fun prepare(
        context: Context,
        assetPath: String,
        keyColor: ChromaKeyColor,
        resolution: SpriteResolution,
        callback: (Result<SpriteSequence>) -> Unit
    ) {
        val appContext = context.applicationContext
        val key = cacheKey(
            assetPath,
            keyColor,
            resolution
        )
        val dir =
            File(
                appContext.cacheDir,
                "sprites/$key"
            )

        readSequence(dir)?.let { sequence ->
            mainHandler.post {
                callback(
                    Result.success(sequence)
                )
            }
            return
        }

        var shouldBuild = false

        synchronized(lock) {
            val callbacks = pending[key]

            if (callbacks != null) {
                callbacks.add(callback)
            } else {
                pending[key] =
                    mutableListOf(callback)
                shouldBuild = true
            }
        }

        if (!shouldBuild) return

        worker.execute {
            val result =
                runCatching {
                    buildSequence(
                        context = appContext,
                        assetPath = assetPath,
                        keyColor = keyColor,
                        resolution = resolution,
                        dir = dir
                    )
                }

            val callbacks =
                synchronized(lock) {
                    pending.remove(key).orEmpty()
                }

            mainHandler.post {
                callbacks.forEach {
                    it(result)
                }
            }
        }
    }

    private fun buildSequence(
        context: Context,
        assetPath: String,
        keyColor: ChromaKeyColor,
        resolution: SpriteResolution,
        dir: File
    ): SpriteSequence {
        dir.mkdirs()
        dir.listFiles()?.forEach { it.delete() }

        MediaTrace.event(
            source = "SpriteFrameCache",
            event = "SPRITE_BUILD_START",
            assetPath = assetPath,
            detail =
                "resolution=" +
                    resolution.label +
                    " fps=" +
                    FPS
        )

        val retriever =
            MediaMetadataRetriever()

        try {
            context.assets.openFd(assetPath).use { afd ->
                retriever.setDataSource(
                    afd.fileDescriptor,
                    afd.startOffset,
                    afd.length
                )
            }

            val durationMs =
                retriever.extractMetadata(
                    MediaMetadataRetriever
                        .METADATA_KEY_DURATION
                )
                    ?.toLongOrNull()
                    ?.coerceAtLeast(1L)
                    ?: 1L

            val sourceWidth =
                retriever.extractMetadata(
                    MediaMetadataRetriever
                        .METADATA_KEY_VIDEO_WIDTH
                )
                    ?.toIntOrNull()
                    ?.coerceAtLeast(1)
                    ?: 854

            val sourceHeight =
                retriever.extractMetadata(
                    MediaMetadataRetriever
                        .METADATA_KEY_VIDEO_HEIGHT
                )
                    ?.toIntOrNull()
                    ?.coerceAtLeast(1)
                    ?: 480

            val targetHeight =
                min(
                    resolution.heightPx,
                    sourceHeight
                ).coerceAtLeast(1)

            val targetWidth =
                (
                    sourceWidth.toDouble() *
                        targetHeight.toDouble() /
                        sourceHeight.toDouble()
                    )
                    .roundToInt()
                    .coerceAtLeast(1)

            val requestedFrames =
                max(
                    1,
                    (
                        durationMs *
                            FPS /
                            1000L
                        ).toInt()
                )

            val extension =
                if (
                    Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.R
                ) {
                    "webp"
                } else {
                    "png"
                }

            val frames =
                mutableListOf<File>()

            repeat(requestedFrames) { index ->
                val timeUs =
                    index.toLong() *
                        1_000_000L /
                        FPS

                val source =
                    frameAt(
                        retriever,
                        timeUs,
                        targetWidth,
                        targetHeight
                    )
                        ?: return@repeat

                val transparent =
                    applyChromaKey(
                        source,
                        keyColor
                    )

                if (transparent !== source) {
                    source.recycle()
                }

                val frameFile =
                    File(
                        dir,
                        "frame-" +
                            index
                                .toString()
                                .padStart(
                                    5,
                                    '0'
                                ) +
                            "." +
                            extension
                    )

                FileOutputStream(frameFile)
                    .use { stream ->
                        val format =
                            if (
                                Build.VERSION.SDK_INT >=
                                    Build.VERSION_CODES.R
                            ) {
                                Bitmap.CompressFormat
                                    .WEBP_LOSSLESS
                            } else {
                                Bitmap.CompressFormat.PNG
                            }

                        check(
                            transparent.compress(
                                format,
                                100,
                                stream
                            )
                        )
                    }

                transparent.recycle()
                frames.add(frameFile)

                if (frames.size % 60 == 0) {
                    MediaTrace.event(
                        source = "SpriteFrameCache",
                        event = "SPRITE_BUILD_PROGRESS",
                        assetPath = assetPath,
                        detail =
                            "frames=" +
                                frames.size +
                                "/" +
                                requestedFrames
                    )
                }
            }

            check(frames.isNotEmpty()) {
                "No sprite frame decoded for $assetPath"
            }

            File(dir, "manifest.txt")
                .writeText(
                    frames.size.toString() +
                        "|" +
                        FRAME_DURATION_MS +
                        "|" +
                        targetWidth +
                        "|" +
                        targetHeight +
                        "|" +
                        extension
                )

            MediaTrace.event(
                source = "SpriteFrameCache",
                event = "SPRITE_BUILD_DONE",
                assetPath = assetPath,
                detail =
                    "frames=" +
                        frames.size +
                        " resolution=" +
                        targetWidth +
                        "x" +
                        targetHeight +
                        " fps=" +
                        FPS
            )

            return SpriteSequence(
                frames = frames.toList(),
                frameDurationMs =
                    FRAME_DURATION_MS,
                width = targetWidth,
                height = targetHeight
            )
        } finally {
            retriever.release()
        }
    }

    private fun frameAt(
        retriever: MediaMetadataRetriever,
        timeUs: Long,
        width: Int,
        height: Int
    ): Bitmap? {
        if (
            Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O_MR1
        ) {
            retriever.getScaledFrameAtTime(
                timeUs,
                MediaMetadataRetriever
                    .OPTION_CLOSEST,
                width,
                height
            )?.let { return it }
        }

        val source =
            retriever.getFrameAtTime(
                timeUs,
                MediaMetadataRetriever
                    .OPTION_CLOSEST
            )
                ?: return null

        if (
            source.width == width &&
            source.height == height
        ) {
            return source
        }

        return Bitmap.createScaledBitmap(
            source,
            width,
            height,
            true
        ).also {
            if (it !== source) {
                source.recycle()
            }
        }
    }

    private fun applyChromaKey(
        source: Bitmap,
        keyColor: ChromaKeyColor
    ): Bitmap {
        val width = source.width
        val height = source.height
        val pixels =
            IntArray(width * height)

        source.getPixels(
            pixels,
            0,
            width,
            0,
            0,
            width,
            height
        )

        val threshold =
            AssetMediaCatalog.KEY_THRESHOLD
        val softness =
            AssetMediaCatalog.KEY_SOFTNESS
        val despill =
            AssetMediaCatalog.KEY_DESPILL
        val greenStrength =
            if (
                keyColor ==
                    ChromaKeyColor.GREEN
            ) {
                1f
            } else {
                0f
            }

        for (index in pixels.indices) {
            val color = pixels[index]

            val r =
                Color.red(color) / 255f
            var g =
                Color.green(color) / 255f
            var b =
                Color.blue(color) / 255f

            val blueMax = max(r, g)
            val greenMax = max(r, b)
            val blueDominance =
                b - blueMax
            val greenDominance =
                g - greenMax

            val dominance =
                blueDominance *
                    (1f - greenStrength) +
                    greenDominance *
                    greenStrength

            val keyChannel =
                b *
                    (1f - greenStrength) +
                    g *
                    greenStrength

            val chromaKey =
                smoothstep(
                    threshold,
                    threshold + softness,
                    dominance
                )

            val brightness =
                smoothstep(
                    .18f,
                    .42f,
                    keyChannel
                )

            val key =
                (
                    chromaKey *
                        brightness
                    ).coerceIn(
                    0f,
                    1f
                )

            val neutralBlue =
                blueMax + .04f
            val neutralGreen =
                greenMax + .04f

            b =
                mix(
                    b,
                    min(b, neutralBlue),
                    key *
                        despill *
                        (1f - greenStrength)
                )

            g =
                mix(
                    g,
                    min(g, neutralGreen),
                    key *
                        despill *
                        greenStrength
                )

            val alpha =
                (
                    Color.alpha(color) /
                        255f *
                        (1f - key)
                    ).coerceIn(
                    0f,
                    1f
                )

            pixels[index] =
                Color.argb(
                    (
                        alpha * 255f
                        ).roundToInt()
                        .coerceIn(
                            0,
                            255
                        ),
                    (
                        r * 255f
                        ).roundToInt()
                        .coerceIn(
                            0,
                            255
                        ),
                    (
                        g * 255f
                        ).roundToInt()
                        .coerceIn(
                            0,
                            255
                        ),
                    (
                        b * 255f
                        ).roundToInt()
                        .coerceIn(
                            0,
                            255
                        )
                )
        }

        return Bitmap.createBitmap(
            pixels,
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
    }

    private fun smoothstep(
        edge0: Float,
        edge1: Float,
        value: Float
    ): Float {
        val t =
            (
                (value - edge0) /
                    (edge1 - edge0)
                ).coerceIn(
                0f,
                1f
            )

        return t *
            t *
            (3f - 2f * t)
    }

    private fun mix(
        a: Float,
        b: Float,
        amount: Float
    ): Float =
        a +
            (b - a) *
            amount.coerceIn(
                0f,
                1f
            )

    private fun readSequence(
        dir: File
    ): SpriteSequence? {
        val manifest =
            File(dir, "manifest.txt")

        if (!manifest.isFile) return null

        val parts =
            manifest.readText()
                .trim()
                .split("|")

        if (parts.size != 5) return null

        val count =
            parts[0].toIntOrNull()
                ?: return null
        val frameDuration =
            parts[1].toLongOrNull()
                ?: return null
        val width =
            parts[2].toIntOrNull()
                ?: return null
        val height =
            parts[3].toIntOrNull()
                ?: return null
        val extension =
            parts[4]

        val frames =
            (0 until count).map { index ->
                File(
                    dir,
                    "frame-" +
                        index
                            .toString()
                            .padStart(
                                5,
                                '0'
                            ) +
                        "." +
                        extension
                )
            }

        if (frames.any { !it.isFile }) {
            return null
        }

        return SpriteSequence(
            frames = frames,
            frameDurationMs =
                frameDuration,
            width = width,
            height = height
        )
    }

    private fun cacheKey(
        assetPath: String,
        keyColor: ChromaKeyColor,
        resolution: SpriteResolution
    ): String {
        val raw =
            CACHE_VERSION +
                "|" +
                assetPath +
                "|" +
                keyColor.name +
                "|" +
                resolution.heightPx +
                "|" +
                AssetMediaCatalog.KEY_THRESHOLD +
                "|" +
                AssetMediaCatalog.KEY_SOFTNESS +
                "|" +
                AssetMediaCatalog.KEY_DESPILL

        return Integer.toHexString(
            raw.hashCode()
        ) +
            "-" +
            resolution.heightPx
    }
}

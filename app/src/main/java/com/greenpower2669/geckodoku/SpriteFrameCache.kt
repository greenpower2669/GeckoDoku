package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
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

data class StableFrameResult(
    val bitmap: Bitmap,
    val cacheHit: Boolean,
    val cacheLayer: String,
    val elapsedMs: Long,
    val width: Int,
    val height: Int
)

object SpriteFrameCache {
    private const val FPS = 12
    private const val FRAME_DURATION_MS = 1000L / FPS
    private const val CACHE_VERSION = "rgba-v1"
    private const val HOT_PIN_BUDGET_PER_ROLE_BYTES =
        16 * 1024 * 1024
    private const val BACKGROUND_IDLE_DELAY_MS =
        1_500L
    private const val QUICK_START_FRAMES = 4
    private const val BUILDING_MANIFEST =
        "building.txt"

    private val worker = Executors.newSingleThreadExecutor()
    private val pinWorker = Executors.newSingleThreadExecutor()
    private val stableFrameWorker =
        Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val lock = Any()
    private val hotPinLock = Any()
    private val stableFrameLock = Any()

    private var hotPinnedResolutionHeight = -1

    private val hotPinnedBitmaps =
        linkedMapOf<
            SpriteWarmRole,
            List<Bitmap>
        >()

    private val pending =
        mutableMapOf<
            String,
            MutableList<(Result<SpriteSequence>) -> Unit>
        >()

    private val pendingQuickStart =
        mutableMapOf<
            String,
            MutableList<(SpriteSequence) -> Unit>
        >()

    private val stableFrameMemory =
        mutableMapOf<
            String,
            Bitmap
        >()

    private val stableFramePending =
        mutableMapOf<
            String,
            MutableList<
                (Result<StableFrameResult>) -> Unit
            >
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

    fun requestStableFrame(
        context: Context,
        assetPath: String,
        keyColor: ChromaKeyColor,
        resolution: SpriteResolution,
        callback:
            (Result<StableFrameResult>) -> Unit
    ) {
        val appContext =
            context.applicationContext
        val key =
            cacheKey(
                assetPath,
                keyColor,
                resolution
            )

        val memoryHit =
            synchronized(stableFrameLock) {
                stableFrameMemory[key]
            }

        if (memoryHit != null) {
            mainHandler.post {
                callback(
                    Result.success(
                        StableFrameResult(
                            bitmap = memoryHit,
                            cacheHit = true,
                            cacheLayer = "MEMORY",
                            elapsedMs = 0L,
                            width = memoryHit.width,
                            height = memoryHit.height
                        )
                    )
                )
            }
            return
        }

        var shouldBuild = false

        synchronized(stableFrameLock) {
            val callbacks =
                stableFramePending[key]

            if (callbacks != null) {
                callbacks.add(callback)
            } else {
                stableFramePending[key] =
                    mutableListOf(callback)
                shouldBuild = true
            }
        }

        if (!shouldBuild) return

        stableFrameWorker.execute {
            val startedAt =
                SystemClock.elapsedRealtime()

            val result =
                runCatching {
                    loadOrExtractStableFrame(
                        context = appContext,
                        assetPath = assetPath,
                        keyColor = keyColor,
                        resolution = resolution,
                        key = key,
                        startedAtMs = startedAt
                    )
                }

            val callbacks =
                synchronized(stableFrameLock) {
                    stableFramePending
                        .remove(key)
                        .orEmpty()
                }

            mainHandler.post {
                callbacks.forEach {
                    it(result)
                }
            }
        }
    }

    fun prewarmLivingCore(
        context: Context,
        resolution: SpriteResolution
    ) {
        val appContext =
            context.applicationContext
        val plan =
            SpriteWarmupPolicy.plan()

        resetHotPinsFor(
            resolution
        )

        MediaTrace.event(
            source = "SpriteFrameCache",
            event = "PREWARM_REQUEST",
            detail =
                "hotPhysicalBanks=" +
                    plan.hot.size +
                    " logicalSlots=3" +
                    " yellow=shared-gecko" +
                    " backgroundIdle=" +
                    plan.backgroundIdle.size +
                    " resolution=" +
                    resolution.label
        )

        var remainingHot =
            plan.hot.size

        fun hotFinished() {
            remainingHot -= 1

            if (remainingHot <= 0) {
                prewarmBackgroundIdle(
                    context = appContext,
                    resolution = resolution,
                    assets =
                        plan.backgroundIdle,
                    index = 0
                )
            }
        }

        plan.hot.forEach { asset ->
            prepare(
                context = appContext,
                assetPath = asset.assetPath,
                keyColor = asset.keyColor,
                resolution = resolution
            ) { result ->
                result.fold(
                    onSuccess = { sequence ->
                        pinHotBankAsync(
                            role = asset.role,
                            sequence = sequence,
                            resolution =
                                resolution
                        )
                    },
                    onFailure = { error ->
                        MediaTrace.event(
                            source =
                                "SpriteFrameCache",
                            event =
                                "PREWARM_ERROR",
                            assetPath =
                                asset.assetPath,
                            detail =
                                "role=" +
                                    asset.role +
                                    " error=" +
                                    error.javaClass
                                        .simpleName
                        )
                    }
                )

                hotFinished()
            }
        }
    }

    fun prewarmGecko(
        context: Context,
        resolution: SpriteResolution
    ) {
        // Compatibility entry point. The old eager seven-asset
        // warmup is intentionally replaced by the living-core plan.
        prewarmLivingCore(
            context = context,
            resolution = resolution
        )
    }

    private fun prewarmBackgroundIdle(
        context: Context,
        resolution: SpriteResolution,
        assets: List<SpriteWarmAsset>,
        index: Int
    ) {
        if (index >= assets.size) {
            MediaTrace.event(
                source = "SpriteFrameCache",
                event = "PREWARM_IDLE_DONE",
                detail =
                    "assets=" +
                        assets.size +
                        " resolution=" +
                        resolution.label
            )
            return
        }

        val currentResolution =
            RichMediaSettings
                .spriteResolutionFor(
                    context
                )

        if (currentResolution != resolution) {
            MediaTrace.event(
                source = "SpriteFrameCache",
                event =
                    "PREWARM_ABORT_RESOLUTION_CHANGED",
                detail =
                    "expected=" +
                        resolution.label +
                        " actual=" +
                        currentResolution.label
            )
            return
        }

        val asset =
            assets[index]

        mainHandler.postDelayed(
            {
                prepare(
                    context = context,
                    assetPath =
                        asset.assetPath,
                    keyColor =
                        asset.keyColor,
                    resolution =
                        resolution
                ) { result ->
                    result.exceptionOrNull()
                        ?.let { error ->
                            MediaTrace.event(
                                source =
                                    "SpriteFrameCache",
                                event =
                                    "PREWARM_ERROR",
                                assetPath =
                                    asset.assetPath,
                                detail =
                                    "role=" +
                                        asset.role +
                                        " error=" +
                                        error.javaClass
                                            .simpleName
                            )
                        }

                    prewarmBackgroundIdle(
                        context = context,
                        resolution =
                            resolution,
                        assets = assets,
                        index = index + 1
                    )
                }
            },
            BACKGROUND_IDLE_DELAY_MS
        )
    }

    private fun resetHotPinsFor(
        resolution: SpriteResolution
    ) {
        synchronized(hotPinLock) {
            if (
                hotPinnedResolutionHeight ==
                    resolution.heightPx
            ) {
                return
            }

            hotPinnedBitmaps.clear()
            hotPinnedResolutionHeight =
                resolution.heightPx

            MediaTrace.event(
                source = "SpriteFrameCache",
                event = "HOT_BANK_RESET",
                detail =
                    "resolution=" +
                        resolution.label
            )
        }
    }

    private fun pinHotBankAsync(
        role: SpriteWarmRole,
        sequence: SpriteSequence,
        resolution: SpriteResolution
    ) {
        pinWorker.execute {
            val stillCurrent =
                synchronized(hotPinLock) {
                    hotPinnedResolutionHeight ==
                        resolution.heightPx
                }

            if (!stillCurrent) {
                return@execute
            }

            var bytes = 0
            val pinned =
                mutableListOf<Bitmap>()

            for (file in sequence.frames) {
                val bitmap =
                    bitmapFor(file)
                        ?: continue

                val nextBytes =
                    bytes +
                        bitmap.allocationByteCount

                if (
                    nextBytes >
                        HOT_PIN_BUDGET_PER_ROLE_BYTES
                ) {
                    break
                }

                pinned.add(bitmap)
                bytes = nextBytes
            }

            val accepted =
                synchronized(hotPinLock) {
                    if (
                        hotPinnedResolutionHeight ==
                            resolution.heightPx
                    ) {
                        hotPinnedBitmaps[role] =
                            pinned.toList()
                        true
                    } else {
                        false
                    }
                }

            if (!accepted) {
                return@execute
            }

            MediaTrace.event(
                source = "SpriteFrameCache",
                event = "HOT_BANK_PINNED",
                detail =
                    "role=" +
                        role +
                        " frames=" +
                        pinned.size +
                        "/" +
                        sequence.frames.size +
                        " resolution=" +
                        resolution.label +
                        " memMb=" +
                        String.format(
                            java.util.Locale.US,
                            "%.2f",
                            bytes /
                                (1024f * 1024f)
                        ) +
                        if (
                            role ==
                                SpriteWarmRole
                                    .GECKO_SHARED
                        ) {
                            " yellow=shared"
                        } else {
                            ""
                        }
            )
        }
    }

    fun prepare(
        context: Context,
        assetPath: String,
        keyColor: ChromaKeyColor,
        resolution: SpriteResolution,
        onQuickReady:
            ((SpriteSequence) -> Unit)? = null,
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

        var quickAlreadyDelivered = false

        if (onQuickReady != null) {
            readBuildingSequence(dir)
                ?.let { partial ->
                    quickAlreadyDelivered = true

                    mainHandler.post {
                        onQuickReady(partial)
                    }
                }
        }

        var shouldBuild = false

        synchronized(lock) {
            if (
                !quickAlreadyDelivered &&
                onQuickReady != null
            ) {
                pendingQuickStart
                    .getOrPut(key) {
                        mutableListOf()
                    }
                    .add(onQuickReady)
            }

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
                        dir = dir,
                        onQuickReady = { partial ->
                            val quickCallbacks =
                                synchronized(lock) {
                                    pendingQuickStart
                                        .remove(key)
                                        .orEmpty()
                                }

                            if (
                                quickCallbacks
                                    .isNotEmpty()
                            ) {
                                mainHandler.post {
                                    quickCallbacks
                                        .forEach {
                                            it(partial)
                                        }
                                }
                            }
                        }
                    )
                }

            val callbacks =
                synchronized(lock) {
                    pendingQuickStart.remove(key)
                    pending.remove(key).orEmpty()
                }

            mainHandler.post {
                callbacks.forEach {
                    it(result)
                }
            }
        }
    }

    private fun loadOrExtractStableFrame(
        context: Context,
        assetPath: String,
        keyColor: ChromaKeyColor,
        resolution: SpriteResolution,
        key: String,
        startedAtMs: Long
    ): StableFrameResult {
        val extension =
            if (
                Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.R
            ) {
                "webp"
            } else {
                "png"
            }

        val stableDir =
            File(
                context.cacheDir,
                "stable-frames/$key"
            )
        stableDir.mkdirs()

        val stableFile =
            File(
                stableDir,
                "frame-00000.$extension"
            )

        if (stableFile.isFile) {
            val diskBitmap =
                BitmapFactory.decodeFile(
                    stableFile.absolutePath
                )

            if (diskBitmap != null) {
                synchronized(stableFrameLock) {
                    stableFrameMemory[key] =
                        diskBitmap
                }

                return StableFrameResult(
                    bitmap = diskBitmap,
                    cacheHit = true,
                    cacheLayer = "DISK",
                    elapsedMs =
                        SystemClock
                            .elapsedRealtime() -
                            startedAtMs,
                    width = diskBitmap.width,
                    height = diskBitmap.height
                )
            }

            stableFile.delete()
        }

        val spriteDir =
            File(
                context.cacheDir,
                "sprites/$key"
            )
        val spriteFrame =
            File(
                spriteDir,
                "frame-00000.$extension"
            )

        if (spriteFrame.isFile) {
            val spriteBitmap =
                bitmapFor(spriteFrame)

            if (spriteBitmap != null) {
                synchronized(stableFrameLock) {
                    stableFrameMemory[key] =
                        spriteBitmap
                }

                return StableFrameResult(
                    bitmap = spriteBitmap,
                    cacheHit = true,
                    cacheLayer = "SPRITE",
                    elapsedMs =
                        SystemClock
                            .elapsedRealtime() -
                            startedAtMs,
                    width = spriteBitmap.width,
                    height = spriteBitmap.height
                )
            }
        }

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

            val source =
                frameAt(
                    retriever = retriever,
                    timeUs = 0L,
                    width = targetWidth,
                    height = targetHeight
                )
                    ?: error(
                        "No stable frame decoded for " +
                            assetPath
                    )

            val transparent =
                applyChromaKey(
                    source,
                    keyColor
                )

            if (transparent !== source) {
                source.recycle()
            }

            FileOutputStream(stableFile)
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

            synchronized(stableFrameLock) {
                stableFrameMemory[key] =
                    transparent
            }

            return StableFrameResult(
                bitmap = transparent,
                cacheHit = false,
                cacheLayer = "EXTRACTED",
                elapsedMs =
                    SystemClock
                        .elapsedRealtime() -
                        startedAtMs,
                width = transparent.width,
                height = transparent.height
            )
        } finally {
            retriever.release()
        }
    }

    private fun buildSequence(
        context: Context,
        assetPath: String,
        keyColor: ChromaKeyColor,
        resolution: SpriteResolution,
        dir: File,
        onQuickReady:
            (SpriteSequence) -> Unit
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

            val buildingManifest =
                File(
                    dir,
                    BUILDING_MANIFEST
                )

            buildingManifest.writeText(
                requestedFrames.toString() +
                    "|" +
                    FRAME_DURATION_MS +
                    "|" +
                    targetWidth +
                    "|" +
                    targetHeight +
                    "|" +
                    extension
            )

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

                val quickThreshold =
                    min(
                        QUICK_START_FRAMES,
                        requestedFrames
                    )

                if (
                    frames.size ==
                        quickThreshold
                ) {
                    val partial =
                        SpriteSequence(
                            frames =
                                frames.toList(),
                            frameDurationMs =
                                FRAME_DURATION_MS,
                            width = targetWidth,
                            height = targetHeight
                        )

                    MediaTrace.event(
                        source =
                            "SpriteFrameCache",
                        event =
                            "SPRITE_QUICK_READY",
                        assetPath =
                            assetPath,
                        detail =
                            "frames=" +
                                partial.frames.size +
                                "/" +
                                requestedFrames +
                                " resolution=" +
                                resolution.label
                    )

                    onQuickReady(partial)
                }

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

            buildingManifest.delete()

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

            if (
                !File(
                    dir,
                    "manifest.txt"
                ).isFile
            ) {
                File(
                    dir,
                    BUILDING_MANIFEST
                ).delete()
            }
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

    private fun readBuildingSequence(
        dir: File
    ): SpriteSequence? {
        val manifest =
            File(
                dir,
                BUILDING_MANIFEST
            )

        if (!manifest.isFile) return null

        val parts =
            manifest.readText()
                .trim()
                .split("|")

        if (parts.size != 5) return null

        val expectedCount =
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

        val minimum =
            min(
                QUICK_START_FRAMES,
                expectedCount
            )

        val frames =
            mutableListOf<File>()

        for (
            index in
                0 until expectedCount
        ) {
            val frame =
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

            if (!frame.isFile) {
                break
            }

            frames.add(frame)
        }

        if (frames.size < minimum) {
            return null
        }

        return SpriteSequence(
            frames = frames.toList(),
            frameDurationMs =
                frameDuration,
            width = width,
            height = height
        )
    }

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

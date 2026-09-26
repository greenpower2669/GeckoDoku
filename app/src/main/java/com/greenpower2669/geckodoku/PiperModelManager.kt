package com.greenpower2669.geckodoku

import android.content.Context
import android.os.Debug
import android.os.SystemClock
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import java.io.File
import java.io.FileOutputStream

data class PiperGeneratedVoice(
    val samples: FloatArray,
    val sampleRate: Int,
    val generationMs: Long,
    val loadMs: Long,
    val modelLoadedNow: Boolean,
    val modelSizeBytes: Long,
    val approximatePssDeltaKb: Int
)

class PiperModelManager(
    context: Context
) {
    private val appContext =
        context.applicationContext

    private data class LoadedPiper(
        val tts: OfflineTts,
        val loadMs: Long,
        val baselinePssKb: Int
    )

    private val slot =
        PiperSingleModelSlot<LoadedPiper>(
            create = {
                loadModel(it)
            },
            release = {
                it.tts.release()
            }
        )

    @Synchronized
    fun generate(
        variant: VoiceBenchmarkVariant,
        text: String
    ): PiperGeneratedVoice {
        val spec =
            VoiceBenchmarkCatalog
                .piper(variant)

        val required =
            slot.requireWithStatus(
                variant
            )

        val loaded =
            required.value

        val start =
            SystemClock
                .elapsedRealtimeNanos()

        val audio =
            loaded.tts.generate(
                text = text,
                sid = 0,
                speed = 1.0f
            )

        val generationMs =
            (
                SystemClock
                    .elapsedRealtimeNanos() -
                    start
                ) / 1_000_000L

        val pssNow =
            Debug.getPss()

        return PiperGeneratedVoice(
            samples = audio.samples,
            sampleRate = audio.sampleRate,
            generationMs =
                generationMs,
            loadMs =
                if (
                    required.loadedNow
                ) {
                    loaded.loadMs
                } else {
                    0L
                },
            modelLoadedNow =
                required.loadedNow,
            modelSizeBytes =
                spec.modelSizeBytes,
            approximatePssDeltaKb =
                pssNow -
                    loaded.baselinePssKb
        )
    }

    @Synchronized
    fun release() {
        slot.releaseAll()
    }

    private fun loadModel(
        variant: VoiceBenchmarkVariant
    ): LoadedPiper {
        val spec =
            VoiceBenchmarkCatalog
                .piper(variant)

        ensureAssetExists(
            spec.modelAssetPath
        )

        ensureAssetExists(
            spec.tokensAssetPath
        )

        val baselinePss =
            Debug.getPss()

        val start =
            SystemClock
                .elapsedRealtimeNanos()

        val copiedDataDir =
            prepareEspeakData(
                spec
            )

        val vits =
            OfflineTtsVitsModelConfig(
                model =
                    spec.modelAssetPath,
                tokens =
                    spec.tokensAssetPath,
                dataDir =
                    copiedDataDir
                        .absolutePath
            )

        val config =
            OfflineTtsConfig(
                model =
                    OfflineTtsModelConfig(
                        vits = vits,
                        numThreads = 2,
                        debug = false,
                        provider = "cpu"
                    ),
                maxNumSentences = 1,
                silenceScale = 0.2f
            )

        val tts =
            OfflineTts(
                assetManager =
                    appContext.assets,
                config = config
            )

        val loadMs =
            (
                SystemClock
                    .elapsedRealtimeNanos() -
                    start
                ) / 1_000_000L

        return LoadedPiper(
            tts = tts,
            loadMs = loadMs,
            baselinePssKb =
                baselinePss
        )
    }

    private fun ensureAssetExists(
        path: String
    ) {
        appContext.assets
            .open(path)
            .use {
                // Opening is enough to
                // validate packaging.
            }
    }

    private fun prepareEspeakData(
        spec: PiperModelSpec
    ): File {
        val root =
            File(
                appContext.filesDir,
                "voice_benchmark/" +
                    spec.variant.name
                        .lowercase()
            )

        val destination =
            File(
                root,
                spec.dataDirName
            )

        val marker =
            File(
                destination,
                ".geckodoku-ready"
            )

        if (!marker.exists()) {
            if (destination.exists()) {
                destination
                    .deleteRecursively()
            }

            destination.mkdirs()

            copyAssetTree(
                assetPath =
                    spec.dataAssetPath,
                destination =
                    destination
            )

            marker.writeText(
                "sherpa-onnx-v1.13.8"
            )
        }

        return destination
    }

    private fun copyAssetTree(
        assetPath: String,
        destination: File
    ) {
        val children =
            appContext.assets
                .list(assetPath)
                .orEmpty()

        if (children.isEmpty()) {
            destination.parentFile
                ?.mkdirs()

            appContext.assets
                .open(assetPath)
                .use {
                    input ->

                    FileOutputStream(
                        destination
                    ).use {
                        output ->

                        input.copyTo(
                            output
                        )
                    }
                }

            return
        }

        destination.mkdirs()

        children.forEach {
            name ->

            copyAssetTree(
                assetPath =
                    "$assetPath/$name",
                destination =
                    File(
                        destination,
                        name
                    )
            )
        }
    }
}

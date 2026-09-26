package com.greenpower2669.geckodoku

import android.content.Context
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VoiceBenchmarkLab(
    context: Context
) {
    private val executor:
        ExecutorService =
        Executors
            .newSingleThreadExecutor()

    private val piperManager =
        PiperModelManager(context)

    private val piperPlayer =
        VoicePcmPlayer()

    private val androidEngine =
        AndroidTtsBenchmarkEngine(
            context
        )

    private val lowEngine =
        PiperVoiceBenchmarkEngine(
            variant =
                VoiceBenchmarkVariant
                    .PIPER_LOW,
            manager =
                piperManager,
            executor =
                executor,
            player =
                piperPlayer
        )

    private val mediumEngine =
        PiperVoiceBenchmarkEngine(
            variant =
                VoiceBenchmarkVariant
                    .PIPER_MEDIUM,
            manager =
                piperManager,
            executor =
                executor,
            player =
                piperPlayer
        )

    fun run(
        variant: VoiceBenchmarkVariant,
        text: String =
            VoiceBenchmarkCatalog
                .TEST_PHRASE,
        callback:
            (VoiceBenchmarkResult) -> Unit
    ) {
        engine(variant)
            .synthesizeAndPlay(
                text,
                callback
            )
    }

    fun stopExperiment() {
        androidEngine.stop()
        piperPlayer.stop()

        executor.execute {
            piperManager.release()
        }
    }

    fun release() {
        androidEngine.release()
        piperPlayer.release()

        executor.execute {
            piperManager.release()
        }

        executor.shutdown()
    }

    private fun engine(
        variant: VoiceBenchmarkVariant
    ): VoiceBenchmarkEngine =
        when (variant) {
            VoiceBenchmarkVariant
                .ANDROID_TTS ->
                androidEngine

            VoiceBenchmarkVariant
                .PIPER_LOW ->
                lowEngine

            VoiceBenchmarkVariant
                .PIPER_MEDIUM ->
                mediumEngine
        }
}

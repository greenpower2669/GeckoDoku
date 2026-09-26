package com.greenpower2669.geckodoku

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService

class PiperVoiceBenchmarkEngine(
    private val variant:
        VoiceBenchmarkVariant,
    private val manager:
        PiperModelManager,
    private val executor:
        ExecutorService,
    private val player:
        VoicePcmPlayer
) : VoiceBenchmarkEngine {
    private val mainHandler =
        Handler(Looper.getMainLooper())

    override fun synthesizeAndPlay(
        text: String,
        callback:
            (VoiceBenchmarkResult) -> Unit
    ) {
        executor.execute {
            try {
                val generated =
                    manager.generate(
                        variant = variant,
                        text = text
                    )

                player.play(
                    generated.samples,
                    generated.sampleRate
                )

                val durationMs =
                    if (
                        generated.sampleRate >
                        0
                    ) {
                        generated.samples
                            .size *
                            1000L /
                            generated.sampleRate
                    } else {
                        0L
                    }

                mainHandler.post {
                    callback(
                        VoiceBenchmarkResult(
                            variant = variant,
                            metrics =
                                VoiceBenchmarkMetrics(
                                    generationMs =
                                        generated
                                            .generationMs,
                                    loadMs =
                                        generated
                                            .loadMs,
                                    modelLoadedNow =
                                        generated
                                            .modelLoadedNow,
                                    modelSizeBytes =
                                        generated
                                            .modelSizeBytes,
                                    approximatePssDeltaKb =
                                        generated
                                            .approximatePssDeltaKb,
                                    sampleRate =
                                        generated
                                            .sampleRate,
                                    audioDurationMs =
                                        durationMs
                                )
                        )
                    )
                }
            } catch (
                throwable: Throwable
            ) {
                mainHandler.post {
                    callback(
                        VoiceBenchmarkResult(
                            variant = variant,
                            error =
                                throwable.message
                                    ?: throwable
                                        .javaClass
                                        .simpleName
                        )
                    )
                }
            }
        }
    }

    override fun stop() {
        player.stop()
    }
}

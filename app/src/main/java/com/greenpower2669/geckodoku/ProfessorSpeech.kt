package com.greenpower2669.geckodoku

import android.content.Context

class ProfessorSpeech(
    context: Context
) {
    private val piper =
        PierrePiperSpeechEngine(
            context
        )

    private val androidFallback =
        AndroidProfessorSpeech(
            context
        )

    @Volatile
    private var generation = 0

    var enabled: Boolean = true
        set(value) {
            field = value

            if (!value) {
                stop()
            }
        }

    fun speak(
        text: String,
        onCompletion:
            (() -> Unit)? = null
    ): Boolean {
        if (
            !enabled ||
            text.isBlank()
        ) {
            return false
        }

        val token =
            synchronized(this) {
                generation += 1
                generation
            }

        androidFallback.stop()

        val accepted =
            piper.speak(
                text = text,
                onCompletion = {
                    if (
                        enabled &&
                        token ==
                            generation
                    ) {
                        onCompletion
                            ?.invoke()
                    }
                },
                onFailure = failure@{
                    if (
                        !enabled ||
                        token !=
                            generation
                    ) {
                        return@failure
                    }

                    androidFallback.speak(
                        text = text,
                        onCompletion = {
                            if (
                                enabled &&
                                token ==
                                    generation
                            ) {
                                onCompletion
                                    ?.invoke()
                            }
                        }
                    )
                }
            )

        if (!accepted) {
            return androidFallback
                .speak(
                    text = text,
                    onCompletion =
                        onCompletion
                )
        }

        return true
    }

    fun stop() {
        synchronized(this) {
            generation += 1
        }

        piper.stop()
        androidFallback.stop()
    }

    fun release() {
        stop()
        piper.release()
        androidFallback.release()
    }
}

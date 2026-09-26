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

    @Volatile
    private var speaking = false

    var onSpeakingChanged:
        ((Boolean) -> Unit)? = null

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

        piper.stop()
        androidFallback.stop()
        setSpeaking(false)

        val accepted =
            piper.speak(
                text = text,
                onStarted = {
                    if (
                        enabled &&
                        token ==
                            generation
                    ) {
                        setSpeaking(true)
                    }
                },
                onCompletion = {
                    complete(
                        token,
                        onCompletion
                    )
                },
                onFailure = failure@{
                    if (
                        !enabled ||
                        token !=
                            generation
                    ) {
                        return@failure
                    }

                    setSpeaking(false)

                    startAndroidFallback(
                        token = token,
                        text = text,
                        onCompletion =
                            onCompletion
                    )
                }
            )

        if (!accepted) {
            return startAndroidFallback(
                token = token,
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
        setSpeaking(false)
    }

    fun release() {
        stop()
        piper.release()
        androidFallback.release()
        onSpeakingChanged = null
    }

    private fun startAndroidFallback(
        token: Int,
        text: String,
        onCompletion:
            (() -> Unit)?
    ): Boolean {
        val accepted =
            androidFallback.speak(
                text = text,
                onStarted = {
                    if (
                        enabled &&
                        token ==
                            generation
                    ) {
                        setSpeaking(true)
                    }
                },
                onCompletion = {
                    complete(
                        token,
                        onCompletion
                    )
                }
            )

        if (!accepted) {
            setSpeaking(false)
        }

        return accepted
    }

    private fun complete(
        token: Int,
        onCompletion:
            (() -> Unit)?
    ) {
        if (
            !enabled ||
            token != generation
        ) {
            return
        }

        setSpeaking(false)
        onCompletion?.invoke()
    }

    @Synchronized
    private fun setSpeaking(
        value: Boolean
    ) {
        if (speaking == value) {
            return
        }

        speaking = value
        onSpeakingChanged
            ?.invoke(value)
    }
}

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

    private val requestPolicy =
        ProfessorSpeechRequestPolicy()

    @Volatile
    private var generation = 0

    @Volatile
    private var speaking = false

    @Volatile
    private var activeOrigin:
        SpeechOrigin? = null

    var onSpeakingChanged:
        ((Boolean) -> Unit)? = null

    val isBusy: Boolean
        get() =
            activeOrigin != null

    var enabled: Boolean = true
        set(value) {
            field = value

            if (!value) {
                stop(
                    reason =
                        SpeechStopReason
                            .FX_DISABLED,
                    caller =
                        "ProfessorSpeech.enabled"
                )
            }
        }

    fun speak(
        text: String,
        origin: SpeechOrigin,
        onCompletion:
            (() -> Unit)? = null
    ): Boolean {
        if (
            !enabled ||
            text.isBlank()
        ) {
            return false
        }

        val currentOrigin =
            activeOrigin

        val decision =
            requestPolicy.decide(
                activeOrigin =
                    currentOrigin,
                incomingOrigin =
                    origin
            )

        MediaTrace.event(
            source = "ProfessorSpeech",
            event = "SPEAK_REQUEST",
            detail =
                "origin=" +
                    origin +
                    " activeOrigin=" +
                    currentOrigin +
                    " decision=" +
                    decision
        )

        if (
            decision ==
            SpeechRequestDecision
                .REJECT_BUSY
        ) {
            MediaTrace.event(
                source =
                    "ProfessorSpeech",
                event =
                    "SPEAK_REJECT_BUSY",
                detail =
                    "origin=" +
                        origin +
                        " activeOrigin=" +
                        currentOrigin
            )
            return false
        }

        if (
            decision ==
            SpeechRequestDecision
                .REPLACE
        ) {
            stopTransport(
                reason =
                    if (
                        origin ==
                        SpeechOrigin
                            .END_GAME
                    ) {
                        SpeechStopReason
                            .END_GAME
                    } else {
                        SpeechStopReason
                            .NEW_PROF_REQUEST
                    },
                caller =
                    "speak(" +
                        origin +
                        ")"
            )
        }

        val token =
            synchronized(this) {
                generation += 1
                activeOrigin =
                    origin
                generation
            }

        val accepted =
            piper.speak(
                text = text,
                onStarted = {
                    if (
                        enabled &&
                        token ==
                            generation &&
                        activeOrigin ==
                            origin
                    ) {
                        MediaTrace.event(
                            source =
                                "ProfessorSpeech",
                            event =
                                "SPEAK_STARTED",
                            detail =
                                "origin=" +
                                    origin
                        )
                        setSpeaking(true)
                    }
                },
                onCompletion = {
                    complete(
                        token = token,
                        origin = origin,
                        onCompletion =
                            onCompletion
                    )
                },
                onFailure = failure@{
                    if (
                        !enabled ||
                        token !=
                            generation ||
                        activeOrigin !=
                            origin
                    ) {
                        return@failure
                    }

                    setSpeaking(false)

                    startAndroidFallback(
                        token = token,
                        origin = origin,
                        text = text,
                        onCompletion =
                            onCompletion
                    )
                }
            )

        if (!accepted) {
            return startAndroidFallback(
                token = token,
                origin = origin,
                text = text,
                onCompletion =
                    onCompletion
            )
        }

        return true
    }

    fun stop(
        reason: SpeechStopReason,
        caller: String
    ) {
        stopTransport(
            reason = reason,
            caller = caller
        )
    }

    fun release() {
        stopTransport(
            reason =
                SpeechStopReason.RELEASE,
            caller =
                "ProfessorSpeech.release"
        )

        piper.release()
        androidFallback.release()
        onSpeakingChanged = null
    }

    private fun stopTransport(
        reason: SpeechStopReason,
        caller: String
    ) {
        val origin =
            activeOrigin

        synchronized(this) {
            generation += 1
            activeOrigin = null
        }

        if (
            origin != null ||
            speaking
        ) {
            MediaTrace.event(
                source =
                    "ProfessorSpeech",
                event = "SPEAK_STOP",
                detail =
                    "origin=" +
                        origin +
                        " reason=" +
                        reason +
                        " caller=" +
                        caller
            )
        }

        piper.stop()
        androidFallback.stop()
        setSpeaking(false)
    }

    private fun startAndroidFallback(
        token: Int,
        origin: SpeechOrigin,
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
                            generation &&
                        activeOrigin ==
                            origin
                    ) {
                        MediaTrace.event(
                            source =
                                "ProfessorSpeech",
                            event =
                                "SPEAK_STARTED",
                            detail =
                                "origin=" +
                                    origin +
                                    " engine=android"
                        )
                        setSpeaking(true)
                    }
                },
                onCompletion = {
                    complete(
                        token = token,
                        origin = origin,
                        onCompletion =
                            onCompletion
                    )
                }
            )

        if (!accepted) {
            synchronized(this) {
                if (
                    token ==
                    generation &&
                    activeOrigin ==
                        origin
                ) {
                    activeOrigin = null
                }
            }
            setSpeaking(false)
        }

        return accepted
    }

    private fun complete(
        token: Int,
        origin: SpeechOrigin,
        onCompletion:
            (() -> Unit)?
    ) {
        if (
            !enabled ||
            token != generation ||
            activeOrigin != origin
        ) {
            return
        }

        synchronized(this) {
            if (
                token ==
                generation &&
                activeOrigin ==
                    origin
            ) {
                activeOrigin = null
            }
        }

        MediaTrace.event(
            source = "ProfessorSpeech",
            event = "SPEAK_COMPLETED",
            detail =
                "origin=" +
                    origin
        )

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

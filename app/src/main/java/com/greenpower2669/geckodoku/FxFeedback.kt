package com.greenpower2669.geckodoku

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

interface FxFeedback {
    var enabled: Boolean
    fun cross()
    fun blocked()
    fun gecko()
    fun error()
    fun hint()
    fun marker()
    fun complete()
    fun celebrationBurst(
        level: Int,
        burstIndex: Int,
        isLast: Boolean
    )
    fun stopCelebration()
    fun release()
}

class ToneFxFeedback : FxFeedback {
    private val tone =
        ToneGenerator(
            AudioManager.STREAM_MUSIC,
            72
        )

    private val celebrationHandler =
        Handler(
            Looper.getMainLooper()
        )

    override var enabled: Boolean = true
        set(value) {
            field = value

            if (!value) {
                stopCelebration()
            }
        }

    private fun play(
        type: Int,
        durationMs: Int
    ) {
        if (enabled) {
            tone.startTone(
                type,
                durationMs
            )
        }
    }

    override fun cross() =
        play(
            ToneGenerator.TONE_PROP_BEEP,
            45
        )

    override fun blocked() =
        play(
            ToneGenerator.TONE_PROP_NACK,
            55
        )

    override fun gecko() =
        play(
            ToneGenerator.TONE_PROP_ACK,
            90
        )

    override fun error() =
        play(
            ToneGenerator.TONE_SUP_ERROR,
            150
        )

    override fun hint() =
        play(
            ToneGenerator.TONE_CDMA_PIP,
            55
        )

    override fun marker() =
        play(
            ToneGenerator.TONE_PROP_PROMPT,
            60
        )

    override fun complete() =
        play(
            ToneGenerator.TONE_PROP_ACK,
            130
        )

    override fun celebrationBurst(
        level: Int,
        burstIndex: Int,
        isLast: Boolean
    ) {
        if (!enabled) {
            return
        }

        val launchType =
            if (
                (burstIndex + level) % 2 == 0
            ) {
                ToneGenerator.TONE_CDMA_PIP
            } else {
                ToneGenerator.TONE_PROP_BEEP
            }

        play(
            launchType,
            38 +
                level.coerceAtMost(7) * 3
        )

        celebrationHandler.postDelayed(
            {
                if (!enabled) {
                    return@postDelayed
                }

                val explosionType =
                    if (
                        (burstIndex + level) % 3 == 0
                    ) {
                        ToneGenerator.TONE_PROP_ACK
                    } else {
                        ToneGenerator.TONE_PROP_PROMPT
                    }

                play(
                    explosionType,
                    70 +
                        level.coerceAtMost(7) * 6
                )
            },
            58L
        )

        if (isLast && level >= 4) {
            celebrationHandler.postDelayed(
                {
                    if (enabled) {
                        play(
                            ToneGenerator.TONE_PROP_ACK,
                            150 +
                                level.coerceAtMost(7) * 10
                        )
                    }
                },
                126L
            )
        }
    }

    override fun stopCelebration() {
        celebrationHandler
            .removeCallbacksAndMessages(
                null
            )
    }

    override fun release() {
        stopCelebration()
        tone.release()
    }
}

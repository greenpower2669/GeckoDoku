package com.greenpower2669.geckodoku

import android.media.AudioManager
import android.media.ToneGenerator

interface FxFeedback {
    var enabled: Boolean
    fun cross()
    fun blocked()
    fun gecko()
    fun error()
    fun hint()
    fun marker()
    fun complete()
    fun release()
}

class ToneFxFeedback : FxFeedback {
    override var enabled: Boolean = true
    private val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 72)

    private fun play(type: Int, durationMs: Int) {
        if (enabled) tone.startTone(type, durationMs)
    }

    override fun cross() = play(ToneGenerator.TONE_PROP_BEEP, 45)
    override fun blocked() = play(ToneGenerator.TONE_PROP_NACK, 55)
    override fun gecko() = play(ToneGenerator.TONE_PROP_ACK, 90)
    override fun error() = play(ToneGenerator.TONE_SUP_ERROR, 150)
    override fun hint() = play(ToneGenerator.TONE_CDMA_PIP, 55)
    override fun marker() = play(ToneGenerator.TONE_PROP_PROMPT, 60)
    override fun complete() = play(ToneGenerator.TONE_PROP_ACK, 150)
    override fun release() = tone.release()
}

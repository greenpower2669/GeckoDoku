package com.greenpower2669.geckodoku

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper

class VoicePcmPlayer {
    private val handler =
        Handler(Looper.getMainLooper())

    private var track:
        AudioTrack? = null

    private var releaseRunnable:
        Runnable? = null

    @Synchronized
    fun play(
        samples: FloatArray,
        sampleRate: Int,
        onStarted: (() -> Unit)? = null,
        onCompletion: (() -> Unit)? = null
    ) {
        stop()

        if (
            samples.isEmpty() ||
            sampleRate <= 0
        ) {
            onCompletion?.invoke()
            return
        }

        val bytes =
            (
                samples.size *
                    Float.SIZE_BYTES
                ).coerceAtLeast(
                    4096
                )

        val next =
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioCapturePolicy
                        .speechAttributes()
                )
                .setAudioFormat(
                    AudioFormat
                        .Builder()
                        .setEncoding(
                            AudioFormat
                                .ENCODING_PCM_FLOAT
                        )
                        .setSampleRate(
                            sampleRate
                        )
                        .setChannelMask(
                            AudioFormat
                                .CHANNEL_OUT_MONO
                        )
                        .build()
                )
                .setTransferMode(
                    AudioTrack.MODE_STATIC
                )
                .setBufferSizeInBytes(
                    bytes
                )
                .build()

        val written =
            next.write(
                samples,
                0,
                samples.size,
                AudioTrack.WRITE_BLOCKING
            )

        if (written < 0) {
            next.release()
            error(
                "AudioTrack write failed: " +
                    written
            )
        }

        track = next

        AudioCapturePolicy.log(
            source = "PIERRE",
            detail =
                "usage=MEDIA content=SPEECH capture=ALLOW_ALL sampleRate=" +
                    sampleRate
        )

        next.play()
        onStarted?.invoke()

        val durationMs =
            (
                samples.size *
                    1000L /
                    sampleRate
                ).coerceAtLeast(
                    100L
                )

        val cleanup =
            Runnable {
                synchronized(this) {
                    if (track === next) {
                        releaseTrack(next)
                        track = null
                        releaseRunnable = null
                        onCompletion?.invoke()
                    }
                }
            }

        releaseRunnable = cleanup

        handler.postDelayed(
            cleanup,
            durationMs + 350L
        )
    }

    @Synchronized
    fun stop() {
        if (track != null) {
            AudioCapturePolicy.log(
                source = "PIERRE",
                detail = "STOP"
            )
        }
        releaseRunnable?.let {
            handler.removeCallbacks(it)
        }

        releaseRunnable = null

        track?.let {
            releaseTrack(it)
        }

        track = null
    }

    fun release() {
        stop()

        AudioCapturePolicy.log(
            source = "PIERRE",
            detail = "RELEASE"
        )
    }

    private fun releaseTrack(
        value: AudioTrack
    ) {
        try {
            value.stop()
        } catch (_: Exception) {
            // Already stopped.
        }

        value.release()
    }
}

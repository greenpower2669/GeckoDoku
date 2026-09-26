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
        sampleRate: Int
    ) {
        stop()

        if (
            samples.isEmpty() ||
            sampleRate <= 0
        ) {
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
                    AudioAttributes
                        .Builder()
                        .setUsage(
                            AudioAttributes
                                .USAGE_ASSISTANCE_ACCESSIBILITY
                        )
                        .setContentType(
                            AudioAttributes
                                .CONTENT_TYPE_SPEECH
                        )
                        .build()
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
        next.play()

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
                        releaseTrack(
                            next
                        )
                        track = null
                        releaseRunnable =
                            null
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

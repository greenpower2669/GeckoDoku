package com.greenpower2669.geckodoku

import android.media.AudioFormat
import android.media.AudioTrack

/**
 * Lecteur PCM de Pierre.
 *
 * L'AudioTrack est conservé entre les phrases afin d'éviter de réamorcer
 * toute la chaîne Android à chaque lecture. Le mode STREAM permet aussi
 * d'attendre la progression réelle du curseur audio au lieu d'estimer la
 * fin avec un timer basé sur la durée théorique.
 */
class VoicePcmPlayer {
    @Volatile
    private var track:
        AudioTrack? = null

    @Volatile
    private var sampleRate =
        0

    @Volatile
    private var generation =
        0

    @Volatile
    private var released =
        false

    /**
     * Lecture bloquante destinée au worker TTS.
     *
     * La fin n'est signalée qu'une fois que playbackHeadPosition confirme
     * que toutes les frames écrites ont réellement été consommées.
     * Pour l'amorce, [muted] garde exactement le même chemin audio mais à
     * volume nul.
     */
    fun playBlocking(
        samples: FloatArray,
        sampleRate: Int,
        muted: Boolean = false,
        onStarted: (() -> Unit)? = null
    ): Boolean {
        if (
            released ||
            samples.isEmpty() ||
            sampleRate <= 0
        ) {
            return false
        }

        val session =
            synchronized(this) {
                if (released) {
                    return false
                }

                generation += 1

                val prepared =
                    ensureTrack(
                        requestedSampleRate =
                            sampleRate
                    )

                resetForNextPlayback(
                    prepared
                )

                prepared.setVolume(
                    if (muted) {
                        0f
                    } else {
                        1f
                    }
                )

                generation
            }

        val activeTrack =
            track
                ?: return false

        val startHead =
            unsignedHead(
                activeTrack
            )

        var writtenFrames =
            0L

        var started =
            false

        fun maybeNotifyStarted() {
            if (
                started ||
                !isSessionActive(
                    session
                )
            ) {
                return
            }

            val consumed =
                consumedSince(
                    start =
                        startHead,
                    current =
                        unsignedHead(
                            activeTrack
                        )
                )

            if (consumed > 0L) {
                started = true
                onStarted?.invoke()
            }
        }

        try {
            activeTrack.play()

            var offset = 0

            while (
                offset <
                    samples.size &&
                isSessionActive(
                    session
                )
            ) {
                val remaining =
                    samples.size -
                        offset

                val chunk =
                    minOf(
                        remaining,
                        WRITE_CHUNK_FRAMES
                    )

                val written =
                    activeTrack.write(
                        samples,
                        offset,
                        chunk,
                        AudioTrack
                            .WRITE_BLOCKING
                    )

                if (written < 0) {
                    error(
                        "AudioTrack write failed: " +
                            written
                    )
                }

                if (written == 0) {
                    Thread.yield()
                    continue
                }

                offset += written
                writtenFrames +=
                    written.toLong()

                maybeNotifyStarted()
            }

            if (
                !isSessionActive(
                    session
                )
            ) {
                return false
            }

            while (
                isSessionActive(
                    session
                )
            ) {
                maybeNotifyStarted()

                val consumed =
                    consumedSince(
                        start =
                            startHead,
                        current =
                            unsignedHead(
                                activeTrack
                            )
                    )

                if (
                    consumed >=
                        writtenFrames
                ) {
                    break
                }

                // Ce délai ne décide pas de la fin : il évite seulement une
                // boucle CPU agressive pendant l'attente du curseur matériel.
                Thread.sleep(
                    PLAYBACK_POLL_MS
                )
            }

            if (
                !isSessionActive(
                    session
                )
            ) {
                return false
            }

            maybeNotifyStarted()

            try {
                activeTrack.pause()
            } catch (_: Exception) {
                // La piste peut déjà être en pause sur certains pilotes.
            }

            activeTrack.setVolume(
                1f
            )

            AudioCapturePolicy.log(
                source = "PIERRE",
                detail =
                    "PLAYBACK_DRAINED frames=" +
                        writtenFrames +
                        " sampleRate=" +
                        sampleRate +
                        " muted=" +
                        muted +
                        " persistentTrack=true"
            )

            return true
        } catch (error: Throwable) {
            if (
                isSessionActive(
                    session
                )
            ) {
                AudioCapturePolicy.log(
                    source = "PIERRE",
                    detail =
                        "PLAYBACK_ERROR type=" +
                            error
                                .javaClass
                                .simpleName
                )
            }

            return false
        } finally {
            if (
                track ===
                    activeTrack
            ) {
                try {
                    activeTrack.setVolume(
                        1f
                    )
                } catch (_: Exception) {
                    // Released concurrently.
                }
            }
        }
    }

    @Synchronized
    fun stop() {
        generation += 1

        track?.let {
            AudioCapturePolicy.log(
                source = "PIERRE",
                detail =
                    "STOP persistentTrack=true"
            )

            try {
                it.pause()
            } catch (_: Exception) {
                // Already stopped / not started.
            }

            try {
                it.flush()
            } catch (_: Exception) {
                // Driver may already have reset its queue.
            }

            try {
                it.setVolume(
                    1f
                )
            } catch (_: Exception) {
                // Ignore teardown races.
            }
        }
    }

    @Synchronized
    fun release() {
        if (released) {
            return
        }

        released = true
        generation += 1

        val current =
            track

        track = null
        sampleRate = 0

        current?.let {
            try {
                it.pause()
            } catch (_: Exception) {
                // Already stopped.
            }

            try {
                it.flush()
            } catch (_: Exception) {
                // Nothing to flush.
            }

            it.release()
        }

        AudioCapturePolicy.log(
            source = "PIERRE",
            detail =
                "RELEASE persistentTrack=true"
        )
    }

    @Synchronized
    private fun ensureTrack(
        requestedSampleRate: Int
    ): AudioTrack {
        val existing =
            track

        if (
            existing != null &&
            sampleRate ==
                requestedSampleRate &&
            existing.state ==
                AudioTrack
                    .STATE_INITIALIZED
        ) {
            AudioCapturePolicy.log(
                source = "PIERRE",
                detail =
                    "TRACK_REUSE mode=STREAM sampleRate=" +
                        requestedSampleRate
            )

            return existing
        }

        existing?.release()

        val minBuffer =
            AudioTrack
                .getMinBufferSize(
                    requestedSampleRate,
                    AudioFormat
                        .CHANNEL_OUT_MONO,
                    AudioFormat
                        .ENCODING_PCM_FLOAT
                )

        if (minBuffer <= 0) {
            error(
                "AudioTrack minimum buffer unavailable: " +
                    minBuffer
            )
        }

        val bufferBytes =
            maxOf(
                minBuffer,
                STREAM_BUFFER_BYTES
            )

        val created =
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
                            requestedSampleRate
                        )
                        .setChannelMask(
                            AudioFormat
                                .CHANNEL_OUT_MONO
                        )
                        .build()
                )
                .setTransferMode(
                    AudioTrack.MODE_STREAM
                )
                .setBufferSizeInBytes(
                    bufferBytes
                )
                .build()

        if (
            created.state !=
            AudioTrack.STATE_INITIALIZED
        ) {
            created.release()
            error(
                "AudioTrack init failed"
            )
        }

        track = created
        sampleRate =
            requestedSampleRate

        AudioCapturePolicy.log(
            source = "PIERRE",
            detail =
                "TRACK_CREATE mode=STREAM persistent=true sampleRate=" +
                    requestedSampleRate +
                    " bufferBytes=" +
                    bufferBytes
        )

        return created
    }

    private fun resetForNextPlayback(
        value: AudioTrack
    ) {
        try {
            if (
                value.playState ==
                    AudioTrack
                        .PLAYSTATE_PLAYING
            ) {
                value.pause()
            }
        } catch (_: Exception) {
            // Continue with a best-effort flush.
        }

        try {
            value.flush()
        } catch (_: Exception) {
            // Some drivers report an empty queue as an error.
        }
    }

    private fun isSessionActive(
        session: Int
    ): Boolean =
        !released &&
            session ==
                generation &&
            track != null

    private fun unsignedHead(
        value: AudioTrack
    ): Long =
        value
            .playbackHeadPosition
            .toLong() and
            UINT32_MASK

    private fun consumedSince(
        start: Long,
        current: Long
    ): Long =
        (
            current -
                start
            ) and
            UINT32_MASK

    private companion object {
        const val WRITE_CHUNK_FRAMES =
            2_048

        const val STREAM_BUFFER_BYTES =
            16_384

        const val PLAYBACK_POLL_MS =
            4L

        const val UINT32_MASK =
            0xffff_ffffL
    }
}

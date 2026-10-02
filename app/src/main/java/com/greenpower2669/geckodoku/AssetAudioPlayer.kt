package com.greenpower2669.geckodoku

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class AssetAudioPlayer(
    private val context: Context
) {
    private val handler =
        Handler(Looper.getMainLooper())
    private var musicPlayer:
        MediaPlayer? = null
    private var voicePlayer:
        MediaPlayer? = null
    private var voiceCompletion:
        (() -> Unit)? = null
    private var voiceGeneration = 0

    var enabled: Boolean = true
        set(value) {
            field = value
            if (!value) stopAll()
        }

    fun playMusic(
        assetPath: String,
        onCompletion: (() -> Unit)? = null
    ): Boolean {
        if (!enabled) return false
        stopMusic()
        return try {
            val player =
                newPlayer(
                    assetPath =
                        assetPath,
                    speech = false
                )

            musicPlayer = player
            player.setOnPreparedListener {
                if (
                    enabled &&
                    musicPlayer === it
                ) {
                    AudioCapturePolicy.log(
                        source = "MUSIC",
                        detail =
                            "START asset=" +
                                assetPath +
                                " usage=MEDIA content=MUSIC capture=ALLOW_ALL"
                    )

                    it.start()
                }
            }
            player.setOnCompletionListener {
                val invoke =
                    musicPlayer === it

                if (invoke) {
                    musicPlayer = null
                }

                it.release()

                if (invoke) {
                    onCompletion?.invoke()
                }
            }
            player.setOnErrorListener {
                    failed, _, _ ->

                val invoke =
                    musicPlayer === failed

                if (invoke) {
                    musicPlayer = null
                }

                failed.release()

                if (invoke) {
                    onCompletion?.invoke()
                }

                true
            }
            player.prepareAsync()
            true
        } catch (_: Exception) {
            stopMusic()
            false
        }
    }

    fun playVoiceSegment(
        assetPath: String,
        startMs: Int,
        endMs: Int,
        onStarted: (() -> Unit)? = null,
        onError: (() -> Unit)? = null,
        onCompletion: (() -> Unit)? = null
    ): Boolean {
        if (
            !enabled ||
            endMs <= startMs
        ) return false

        stopVoice()
        return try {
            val generation =
                ++voiceGeneration
            voiceCompletion = onCompletion
            val player =
                newPlayer(
                    assetPath =
                        assetPath,
                    speech = true
                )

            voicePlayer = player

            player.setOnPreparedListener {
                if (
                    voicePlayer === it &&
                    generation ==
                    voiceGeneration
                ) {
                    it.seekTo(
                        startMs.toLong(),
                        MediaPlayer.SEEK_CLOSEST
                    )
                }
            }
            player.setOnSeekCompleteListener {
                if (
                    voicePlayer !== it ||
                    generation !=
                    voiceGeneration ||
                    !enabled
                ) {
                    return@setOnSeekCompleteListener
                }
                AudioCapturePolicy.log(
                    source =
                        "LEGACY_VOICE_SEGMENT",
                    detail =
                        "START asset=" +
                            assetPath +
                            " usage=MEDIA content=SPEECH capture=ALLOW_ALL"
                )

                it.start()
                onStarted?.invoke()
                handler.postDelayed(
                    {
                        if (
                            voicePlayer === it &&
                            generation ==
                            voiceGeneration
                        ) {
                            finishVoice(true)
                        }
                    },
                    (
                        endMs -
                            startMs +
                            80
                        ).toLong()
                )
            }
            player.setOnCompletionListener {
                if (
                    voicePlayer === it &&
                    generation ==
                    voiceGeneration
                ) {
                    finishVoice(true)
                }
            }
            player.setOnErrorListener {
                    failed, _, _ ->
                if (voicePlayer === failed) {
                    finishVoice(false)
                    onError?.invoke()
                } else {
                    failed.release()
                }
                true
            }
            player.prepareAsync()
            true
        } catch (_: Exception) {
            stopVoice()
            onError?.invoke()
            false
        }
    }

    fun stopMusic() {
        val current = musicPlayer

        if (current != null) {
            AudioCapturePolicy.log(
                source = "MUSIC",
                detail = "STOP"
            )
        }
        musicPlayer = null
        if (current != null) {
            try {
                current.setOnCompletionListener(null)
                current.setOnErrorListener(null)
                current.stop()
            } catch (_: Exception) {}
            current.release()
        }
    }

    fun stopVoice() {
        if (voicePlayer != null) {
            AudioCapturePolicy.log(
                source =
                    "LEGACY_VOICE_SEGMENT",
                detail = "STOP"
            )
        }

        finishVoice(false)
    }

    fun stopAll() {
        stopMusic()
        stopVoice()
    }

    fun release() {
        stopAll()
        handler.removeCallbacksAndMessages(null)

        AudioCapturePolicy.log(
            source = "ASSET_AUDIO",
            detail = "RELEASE"
        )
    }

    private fun finishVoice(
        invokeCompletion: Boolean
    ) {
        handler.removeCallbacksAndMessages(null)
        val completion = voiceCompletion
        voiceCompletion = null
        voiceGeneration += 1
        val current = voicePlayer
        voicePlayer = null
        if (current != null) {
            try {
                current.setOnCompletionListener(null)
                current.setOnSeekCompleteListener(null)
                current.setOnErrorListener(null)
                current.stop()
            } catch (_: Exception) {}
            current.release()
        }
        if (invokeCompletion) {
            completion?.invoke()
        }
    }

    private fun newPlayer(
        assetPath: String,
        speech: Boolean
    ): MediaPlayer {
        val player =
            MediaPlayer()

        player.setAudioAttributes(
            if (speech) {
                AudioCapturePolicy
                    .speechAttributes()
            } else {
                AudioCapturePolicy
                    .musicAttributes()
            }
        )

        val descriptor =
            context.assets.openFd(
                assetPath
            )

        descriptor.use {
            player.setDataSource(
                it.fileDescriptor,
                it.startOffset,
                it.length
            )
        }

        return player
    }
}

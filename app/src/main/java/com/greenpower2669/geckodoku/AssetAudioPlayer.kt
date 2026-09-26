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
        assetPath: String
    ): Boolean {
        if (!enabled) return false
        stopMusic()
        return try {
            val player = newPlayer(assetPath)
            musicPlayer = player
            player.setOnPreparedListener {
                if (
                    enabled &&
                    musicPlayer === it
                ) it.start()
            }
            player.setOnCompletionListener {
                if (musicPlayer === it) {
                    musicPlayer = null
                }
                it.release()
            }
            player.setOnErrorListener {
                    failed, _, _ ->
                if (musicPlayer === failed) {
                    musicPlayer = null
                }
                failed.release()
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
            val player = newPlayer(assetPath)
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
                it.start()
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
                } else {
                    failed.release()
                }
                true
            }
            player.prepareAsync()
            true
        } catch (_: Exception) {
            stopVoice()
            false
        }
    }

    fun stopMusic() {
        val current = musicPlayer
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
        finishVoice(false)
    }

    fun stopAll() {
        stopMusic()
        stopVoice()
    }

    fun release() {
        stopAll()
        handler.removeCallbacksAndMessages(null)
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
        assetPath: String
    ): MediaPlayer {
        val player = MediaPlayer()
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

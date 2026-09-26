package com.greenpower2669.geckodoku

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.io.File
import java.util.Locale
import java.util.UUID

class AndroidTtsBenchmarkEngine(
    context: Context
) : VoiceBenchmarkEngine {
    private val appContext =
        context.applicationContext

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private val voicePolicy =
        ProfessorVoicePolicy()

    private var tts:
        TextToSpeech? = null

    @Volatile
    private var ready = false

    private var player:
        MediaPlayer? = null

    private data class Pending(
        val id: String,
        val startedNs: Long,
        val beforePssKb: Long,
        val callback:
            (VoiceBenchmarkResult) -> Unit,
        val file: File
    )

    @Volatile
    private var pending:
        Pending? = null

    init {
        tts =
            TextToSpeech(
                appContext
            ) {
                status ->

                if (
                    status ==
                    TextToSpeech.SUCCESS
                ) {
                    configureReferenceVoice()
                }
            }

        tts?.setOnUtteranceProgressListener(
            object :
                UtteranceProgressListener() {
                override fun onStart(
                    utteranceId: String?
                ) {
                    // Generation started.
                }

                override fun onDone(
                    utteranceId: String?
                ) {
                    complete(
                        utteranceId,
                        null
                    )
                }

                @Deprecated(
                    "Deprecated in Java"
                )
                override fun onError(
                    utteranceId: String?
                ) {
                    complete(
                        utteranceId,
                        "Android TTS error"
                    )
                }

                override fun onError(
                    utteranceId: String?,
                    errorCode: Int
                ) {
                    complete(
                        utteranceId,
                        "Android TTS error " +
                            errorCode
                    )
                }
            }
        )
    }

    override fun synthesizeAndPlay(
        text: String,
        callback:
            (VoiceBenchmarkResult) -> Unit
    ) {
        val engine =
            tts

        if (
            !ready ||
            engine == null
        ) {
            callback(
                VoiceBenchmarkResult(
                    variant =
                        VoiceBenchmarkVariant
                            .ANDROID_TTS,
                    error =
                        "Android TTS est encore en initialisation."
                )
            )
            return
        }

        stopCurrentRequest()

        val file =
            File(
                appContext.cacheDir,
                "voice-ab-android.wav"
            )

        if (file.exists()) {
            file.delete()
        }

        val id =
            "voice-ab-" +
                UUID.randomUUID()
                    .toString()

        val request =
            Pending(
                id = id,
                startedNs =
                    SystemClock
                        .elapsedRealtimeNanos(),
                beforePssKb =
                    Debug.getPss(),
                callback = callback,
                file = file
            )

        pending = request

        val result =
            engine.synthesizeToFile(
                text,
                Bundle(),
                file,
                id
            )

        if (
            result !=
            TextToSpeech.SUCCESS
        ) {
            pending = null

            callback(
                VoiceBenchmarkResult(
                    variant =
                        VoiceBenchmarkVariant
                            .ANDROID_TTS,
                    error =
                        "Android TTS n'a pas accepté la synthèse."
                )
            )
        }
    }

    override fun stop() {
        stopCurrentRequest()
        stopPlayer()
        tts?.stop()
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
        ready = false
    }

    private fun configureReferenceVoice() {
        val engine =
            tts ?: return

        val result =
            engine.setLanguage(
                Locale.FRENCH
            )

        ready =
            result !=
                TextToSpeech
                    .LANG_MISSING_DATA &&
                result !=
                TextToSpeech
                    .LANG_NOT_SUPPORTED

        val frenchVoices =
            engine.voices
                .orEmpty()
                .filter {
                    it.locale.language
                        .equals(
                            "fr",
                            ignoreCase = true
                        )
                }

        val masculine =
            frenchVoices
                .filter {
                    voicePolicy
                        .looksMasculine(
                            buildString {
                                append(it.name)
                                append(' ')
                                append(
                                    it.features
                                        .joinToString(
                                            " "
                                        )
                                )
                            }
                        )
                }
                .sortedBy {
                    it.isNetworkConnectionRequired
                }
                .firstOrNull()

        val fallback =
            frenchVoices
                .sortedBy {
                    it.isNetworkConnectionRequired
                }
                .firstOrNull()

        (
            masculine
                ?: fallback
            )?.let {
            engine.setVoice(it)
        }

        engine.setSpeechRate(
            voicePolicy.speechRate
        )

        engine.setPitch(
            if (masculine != null) {
                voicePolicy
                    .masculineVoicePitch
            } else {
                voicePolicy
                    .fallbackPitch
            }
        )
    }

    private fun complete(
        utteranceId: String?,
        error: String?
    ) {
        val request =
            pending

        if (
            request == null ||
            request.id !=
                utteranceId
        ) {
            return
        }

        pending = null

        val generationMs =
            (
                SystemClock
                    .elapsedRealtimeNanos() -
                    request.startedNs
                ) / 1_000_000L

        val memoryDelta =
            Debug.getPss() -
                request.beforePssKb

        mainHandler.post {
            if (error == null) {
                playGeneratedFile(
                    request.file
                )

                request.callback(
                    VoiceBenchmarkResult(
                        variant =
                            VoiceBenchmarkVariant
                                .ANDROID_TTS,
                        metrics =
                            VoiceBenchmarkMetrics(
                                generationMs =
                                    generationMs,
                                approximatePssDeltaKb =
                                    memoryDelta
                            )
                    )
                )
            } else {
                request.callback(
                    VoiceBenchmarkResult(
                        variant =
                            VoiceBenchmarkVariant
                                .ANDROID_TTS,
                        error = error
                    )
                )
            }
        }
    }

    private fun playGeneratedFile(
        file: File
    ) {
        stopPlayer()

        if (!file.exists()) {
            return
        }

        try {
            val next =
                MediaPlayer()

            player = next

            next.setDataSource(
                file.absolutePath
            )

            next.setOnPreparedListener {
                it.start()
            }

            next.setOnCompletionListener {
                if (player === it) {
                    player = null
                }
                it.release()
            }

            next.setOnErrorListener {
                    failed,
                    _,
                    _ ->

                if (player === failed) {
                    player = null
                }

                failed.release()
                true
            }

            next.prepareAsync()
        } catch (_: Exception) {
            stopPlayer()
        }
    }

    private fun stopCurrentRequest() {
        pending = null
    }

    private fun stopPlayer() {
        val current =
            player

        player = null

        if (current != null) {
            try {
                current.stop()
            } catch (_: Exception) {
                // Already stopped.
            }

            current.release()
        }
    }
}

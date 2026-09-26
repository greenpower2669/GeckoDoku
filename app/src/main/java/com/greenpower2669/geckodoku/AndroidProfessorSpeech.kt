package com.greenpower2669.geckodoku

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

class AndroidProfessorSpeech(
    context: Context
) {
    private val handler =
        Handler(Looper.getMainLooper())

    private val voicePolicy =
        ProfessorVoicePolicy()

    private var tts:
        TextToSpeech? = null

    @Volatile
    private var ready = false

    private var pendingText:
        String? = null

    private var pendingStarted:
        (() -> Unit)? = null

    private var pendingCompletion:
        (() -> Unit)? = null

    private var currentUtterance:
        String? = null

    private var currentStarted:
        (() -> Unit)? = null

    private var currentCompletion:
        (() -> Unit)? = null

    init {
        tts =
            TextToSpeech(
                context.applicationContext
            ) {
                status ->

                if (
                    status ==
                    TextToSpeech.SUCCESS
                ) {
                    configure()
                }
            }

        tts?.setOnUtteranceProgressListener(
            object :
                UtteranceProgressListener() {
                override fun onStart(
                    utteranceId: String?
                ) {
                    started(
                        utteranceId
                    )
                }

                override fun onDone(
                    utteranceId: String?
                ) {
                    complete(
                        utteranceId
                    )
                }

                @Deprecated(
                    "Deprecated in Java"
                )
                override fun onError(
                    utteranceId: String?
                ) {
                    complete(
                        utteranceId
                    )
                }

                override fun onError(
                    utteranceId: String?,
                    errorCode: Int
                ) {
                    complete(
                        utteranceId
                    )
                }
            }
        )
    }

    fun speak(
        text: String,
        onStarted:
            (() -> Unit)? = null,
        onCompletion:
            (() -> Unit)? = null
    ): Boolean {
        if (text.isBlank()) {
            return false
        }

        if (!ready) {
            pendingText = text
            pendingStarted =
                onStarted
            pendingCompletion =
                onCompletion
            return true
        }

        val engine =
            tts ?: return false

        stopCurrent(
            clearPending = false
        )

        val id =
            "prof_android_" +
                UUID.randomUUID()
                    .toString()

        currentUtterance = id
        currentStarted =
            onStarted
        currentCompletion =
            onCompletion

        val result =
            engine.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                id
            )

        if (
            result !=
            TextToSpeech.SUCCESS
        ) {
            currentUtterance = null
            currentStarted = null
            currentCompletion = null
            return false
        }

        return true
    }

    fun stop() {
        pendingText = null
        pendingStarted = null
        pendingCompletion = null

        stopCurrent(
            clearPending = true
        )
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
        ready = false
    }

    private fun configure() {
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

        (masculine ?: fallback)
            ?.let {
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

        val text =
            pendingText

        val startedCallback =
            pendingStarted

        val completion =
            pendingCompletion

        pendingText = null
        pendingStarted = null
        pendingCompletion = null

        if (
            ready &&
            !text.isNullOrBlank()
        ) {
            speak(
                text = text,
                onStarted =
                    startedCallback,
                onCompletion =
                    completion
            )
        }
    }

    private fun started(
        utteranceId: String?
    ) {
        if (
            utteranceId == null ||
            utteranceId !=
                currentUtterance
        ) {
            return
        }

        handler.post {
            if (
                utteranceId ==
                currentUtterance
            ) {
                currentStarted
                    ?.invoke()
            }
        }
    }

    private fun complete(
        utteranceId: String?
    ) {
        if (
            utteranceId == null ||
            utteranceId !=
                currentUtterance
        ) {
            return
        }

        currentUtterance = null
        currentStarted = null

        val completion =
            currentCompletion

        currentCompletion = null

        handler.post {
            completion?.invoke()
        }
    }

    private fun stopCurrent(
        clearPending: Boolean
    ) {
        if (clearPending) {
            pendingText = null
            pendingStarted = null
            pendingCompletion = null
        }

        currentUtterance = null
        currentStarted = null
        currentCompletion = null
        tts?.stop()
    }
}

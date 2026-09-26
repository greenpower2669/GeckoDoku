package com.greenpower2669.geckodoku

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class ProfessorSpeech(
    context: Context
) {
    private var tts:
        TextToSpeech? = null
    private var ready = false
    private var pendingText:
        String? = null

    private val voicePolicy =
        ProfessorVoicePolicy()

    var enabled: Boolean = true
        set(value) {
            field = value
            if (!value) stop()
        }

    init {
        tts =
            TextToSpeech(
                context.applicationContext
            ) { status ->
                val engine =
                    tts ?: return@TextToSpeech
                if (
                    status ==
                    TextToSpeech.SUCCESS
                ) {
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

                    val masculineVoice =
                        frenchVoices
                            .filter {
                                voicePolicy
                                    .looksMasculine(
                                        buildString {
                                            append(it.name)
                                            append(' ')
                                            append(
                                                it.features
                                                    .joinToString(" ")
                                            )
                                        }
                                    )
                            }
                            .sortedBy {
                                it.isNetworkConnectionRequired
                            }
                            .firstOrNull()

                    val fallbackVoice =
                        frenchVoices
                            .sortedBy {
                                it.isNetworkConnectionRequired
                            }
                            .firstOrNull()

                    val selectedVoice =
                        masculineVoice
                            ?: fallbackVoice

                    if (selectedVoice != null) {
                        engine.setVoice(
                            selectedVoice
                        )
                    }

                    engine.setSpeechRate(
                        voicePolicy.speechRate
                    )

                    engine.setPitch(
                        if (masculineVoice != null) {
                            voicePolicy
                                .masculineVoicePitch
                        } else {
                            voicePolicy
                                .fallbackPitch
                        }
                    )

                    val pending = pendingText
                    pendingText = null
                    if (
                        ready &&
                        enabled &&
                        !pending.isNullOrBlank()
                    ) {
                        speak(pending)
                    }
                }
            }
    }

    fun speak(text: String) {
        if (
            !enabled ||
            text.isBlank()
        ) return
        if (!ready) {
            pendingText = text
            return
        }
        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "prof_gecko"
        )
    }

    fun stop() {
        pendingText = null
        tts?.stop()
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}

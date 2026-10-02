package com.greenpower2669.geckodoku

enum class ProfessorVideoMode {
    NONE,
    ACTION,
    SPEECH
}

enum class ProfessorSpeechVideoCommand {
    START_SPEECH_FROM_ZERO,
    KEEP_PLAYING,
    STOP_SPEECH,
    RESTART_SPEECH_FROM_ZERO
}

class ProfessorSpeechVideoPolicy {
    fun onSpeechStarted(
        currentMode: ProfessorVideoMode
    ): ProfessorSpeechVideoCommand =
        if (currentMode == ProfessorVideoMode.SPEECH) {
            ProfessorSpeechVideoCommand.KEEP_PLAYING
        } else {
            ProfessorSpeechVideoCommand.START_SPEECH_FROM_ZERO
        }

    fun onSpeechEnded(
        currentMode: ProfessorVideoMode
    ): ProfessorSpeechVideoCommand =
        if (currentMode == ProfessorVideoMode.SPEECH) {
            ProfessorSpeechVideoCommand.STOP_SPEECH
        } else {
            ProfessorSpeechVideoCommand.KEEP_PLAYING
        }

    fun onSpeechClipCompleted(
        speechActive: Boolean
    ): ProfessorSpeechVideoCommand =
        if (speechActive) {
            ProfessorSpeechVideoCommand.RESTART_SPEECH_FROM_ZERO
        } else {
            ProfessorSpeechVideoCommand.STOP_SPEECH
        }
}

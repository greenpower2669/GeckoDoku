package com.greenpower2669.geckodoku

enum class ProfessorSpeechLaunchDecision {
    START_VOICE_AND_REVEAL,
    START_VOICE_WITH_PNG
}

class ProfessorSpeechLaunchPolicy {
    val videoFailureMayStopVoice: Boolean = false
    val visualPrepareTimeoutMs: Long = 900L

    fun onVisualReady() =
        ProfessorSpeechLaunchDecision.START_VOICE_AND_REVEAL

    fun onVisualFailed() =
        ProfessorSpeechLaunchDecision.START_VOICE_WITH_PNG

    fun onVisualTimeout() =
        ProfessorSpeechLaunchDecision.START_VOICE_WITH_PNG
}

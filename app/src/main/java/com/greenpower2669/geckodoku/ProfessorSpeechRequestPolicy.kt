package com.greenpower2669.geckodoku

enum class SpeechOrigin {
    PROF_BUTTON,
    AMBIENT,
    STATS,
    ENCOURAGEMENT,
    END_GAME
}

enum class SpeechRequestDecision {
    START,
    REPLACE,
    REJECT_BUSY
}

enum class SpeechStopReason {
    NEW_PROF_REQUEST,
    END_GAME,
    FX_DISABLED,
    PUZZLE_RESET,
    LIFECYCLE_PAUSE,
    LIFECYCLE_DESTROY,
    RELEASE
}

class ProfessorSpeechRequestPolicy {
    fun decide(
        activeOrigin: SpeechOrigin?,
        incomingOrigin: SpeechOrigin
    ): SpeechRequestDecision {
        if (activeOrigin == null) {
            return SpeechRequestDecision.START
        }

        return when (incomingOrigin) {
            SpeechOrigin.PROF_BUTTON,
            SpeechOrigin.END_GAME ->
                SpeechRequestDecision.REPLACE

            SpeechOrigin.AMBIENT,
            SpeechOrigin.STATS,
            SpeechOrigin.ENCOURAGEMENT ->
                SpeechRequestDecision.REJECT_BUSY
        }
    }
}

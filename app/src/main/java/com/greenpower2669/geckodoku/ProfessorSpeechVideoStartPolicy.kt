package com.greenpower2669.geckodoku

class ProfessorSpeechVideoStartPolicy {
    fun canStart(
        animationsEnabled: Boolean,
        playVideoInButton: Boolean,
        viewReady: Boolean,
        speechActive: Boolean,
        previousAttemptFailed: Boolean
    ): Boolean =
        animationsEnabled &&
            playVideoInButton &&
            viewReady &&
            speechActive
}

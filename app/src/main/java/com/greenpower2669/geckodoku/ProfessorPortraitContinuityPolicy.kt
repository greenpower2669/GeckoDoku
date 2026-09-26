package com.greenpower2669.geckodoku

class ProfessorPortraitContinuityPolicy {
    var portraitVisible: Boolean = true
        private set

    var videoVisible: Boolean = false
        private set

    fun onPrepareStarted() {
        portraitVisible = true
        videoVisible = false
    }

    fun onFirstFrameHeld() {
        portraitVisible = true
        videoVisible = false
    }

    fun onRevealSucceeded() {
        portraitVisible = false
        videoVisible = true
    }

    fun onRevealFailed() {
        portraitVisible = true
        videoVisible = false
    }

    fun onTimeout() {
        portraitVisible = true
        videoVisible = false
    }

    fun onVideoError() {
        portraitVisible = true
        videoVisible = false
    }
}

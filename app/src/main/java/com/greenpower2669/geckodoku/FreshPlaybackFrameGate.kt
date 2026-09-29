package com.greenpower2669.geckodoku

class FreshPlaybackFrameGate {
    private var nextGeneration = 0L
    private var currentGeneration = 0L
    private var playerStarted = false
    private var renderingStarted = false
    private var frameAccepted = false

    fun beginPlayback(): Long {
        nextGeneration += 1L
        currentGeneration = nextGeneration
        playerStarted = false
        renderingStarted = false
        frameAccepted = false
        return currentGeneration
    }

    fun onPlayerStarted(
        generation: Long
    ) {
        if (generation == currentGeneration) {
            playerStarted = true
        }
    }

    fun onRenderingStart(
        generation: Long
    ) {
        if (generation == currentGeneration) {
            // Android can report MEDIA_INFO_VIDEO_RENDERING_START
            // while MediaPlayer.start() is still returning.
            // Latch the signal for this generation even if
            // onPlayerStarted() has not run yet.
            renderingStarted = true
        }
    }

    fun onFrameRendered(
        generation: Long
    ): Boolean {
        if (
            generation != currentGeneration ||
            !playerStarted ||
            !renderingStarted ||
            frameAccepted
        ) {
            return false
        }

        frameAccepted = true
        return true
    }

    fun cancel(
        generation: Long
    ) {
        if (generation == currentGeneration) {
            playerStarted = false
            renderingStarted = false
            frameAccepted = true
        }
    }
}

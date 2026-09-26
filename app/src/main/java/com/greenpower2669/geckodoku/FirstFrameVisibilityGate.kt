package com.greenpower2669.geckodoku

class FirstFrameVisibilityGate {
    private enum class State {
        IDLE,
        ARMED,
        VISIBLE,
        ABORTED
    }

    private var state =
        State.IDLE

    val isVisible: Boolean
        get() =
            state == State.VISIBLE

    val isArmed: Boolean
        get() =
            state == State.ARMED

    fun arm() {
        state = State.ARMED
    }

    fun onPrepared() {
        // Prepared does not mean that a decoded frame reached OpenGL.
    }

    fun onStarted() {
        // MediaPlayer.start() does not mean that a frame is visible.
    }

    fun onFirstFrameRendered() {
        if (state == State.ARMED) {
            state = State.VISIBLE
        }
    }

    fun abort() {
        if (state == State.ARMED) {
            state = State.ABORTED
        }
    }

    fun reset() {
        state = State.IDLE
    }
}

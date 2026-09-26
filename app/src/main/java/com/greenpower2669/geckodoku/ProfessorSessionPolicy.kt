package com.greenpower2669.geckodoku

enum class PlayerInteraction {
    SINGLE_TAP,
    DOUBLE_TAP,
    CROSS,
    GECKO_ADD,
    GECKO_REMOVE,
    LONG_PRESS
}

class ProfessorSessionPolicy {
    fun shouldStopSpeech(
        interaction: PlayerInteraction
    ): Boolean = false
}

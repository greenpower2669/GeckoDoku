package com.greenpower2669.geckodoku

enum class SettingsEntry {
    GAME_MODE,
    SOUND,
    ANIMATIONS,
    MEDIA_LOG
}

class SettingsMenuPolicy {
    val entries =
        listOf(
            SettingsEntry.GAME_MODE,
            SettingsEntry.SOUND,
            SettingsEntry.ANIMATIONS,
            SettingsEntry.MEDIA_LOG
        )

    val affectsBoardLayout: Boolean = false
}

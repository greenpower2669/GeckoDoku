package com.greenpower2669.geckodoku

enum class SettingsEntry {
    SOUND,
    ANIMATIONS,
    MEDIA_LOG
}

class SettingsMenuPolicy {
    val entries =
        listOf(
            SettingsEntry.SOUND,
            SettingsEntry.ANIMATIONS,
            SettingsEntry.MEDIA_LOG
        )

    val affectsBoardLayout: Boolean = false
}

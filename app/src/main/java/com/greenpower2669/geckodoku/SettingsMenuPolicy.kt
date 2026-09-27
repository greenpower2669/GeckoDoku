package com.greenpower2669.geckodoku

enum class SettingsEntry {
    GAME_MODE,
    DIFFICULTY,
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

    fun entriesFor(
        mode: GameMode
    ): List<SettingsEntry> =
        if (
            mode ==
                GameMode.SUDOKU
        ) {
            listOf(
                SettingsEntry.GAME_MODE,
                SettingsEntry.DIFFICULTY,
                SettingsEntry.SOUND,
                SettingsEntry.ANIMATIONS,
                SettingsEntry.MEDIA_LOG
            )
        } else {
            entries
        }

    val affectsBoardLayout:
        Boolean = false
}

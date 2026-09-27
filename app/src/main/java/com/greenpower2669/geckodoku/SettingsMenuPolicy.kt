package com.greenpower2669.geckodoku

enum class SettingsEntry {
    GAME_MODE,
    DIFFICULTY,
    SAVE_GRID,
    JOURNAL,
    PLAYER_NAME,
    HALL_OF_FAME,
    CLEAR_HISTORY,
    EXPORT_DATA,
    IMPORT_DATA,
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
            mode !=
                GameMode.GECKODOKU
        ) {
            listOf(
                SettingsEntry.GAME_MODE,
                SettingsEntry.DIFFICULTY,
                SettingsEntry.PLAYER_NAME,
                SettingsEntry.HALL_OF_FAME,
                SettingsEntry.CLEAR_HISTORY,
                SettingsEntry.EXPORT_DATA,
                SettingsEntry.IMPORT_DATA,
                SettingsEntry.SOUND,
                SettingsEntry.ANIMATIONS,
                SettingsEntry.MEDIA_LOG
            )
        } else {
            listOf(
                SettingsEntry.GAME_MODE,
                SettingsEntry.SAVE_GRID,
                SettingsEntry.JOURNAL,
                SettingsEntry.PLAYER_NAME,
                SettingsEntry.HALL_OF_FAME,
                SettingsEntry.CLEAR_HISTORY,
                SettingsEntry.EXPORT_DATA,
                SettingsEntry.IMPORT_DATA,
                SettingsEntry.SOUND,
                SettingsEntry.ANIMATIONS,
                SettingsEntry.MEDIA_LOG
            )
        }

    val affectsBoardLayout:
        Boolean = false
}

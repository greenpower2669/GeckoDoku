package com.greenpower2669.geckodoku

enum class SettingsEntry {
    GAME_MODE,
    DIFFICULTY,
    STATS,
    RECENTER,
    NEXT_UNRESOLVED,
    SAVE_GRID,
    JOURNAL,
    PLAYER_NAME,
    HALL_OF_FAME,
    CLEAR_HISTORY,
    EXPORT_DATA,
    IMPORT_DATA,
    EXPORT_SPRITE_BANKS,
    SOUND,
    ANIMATIONS,
    SPRITE_RESOLUTION,
    GOMOKU_ANIMATION_LIMIT,
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
        when (mode) {
            GameMode.GECKODOKU ->
                listOf(
                    SettingsEntry.GAME_MODE,
                    SettingsEntry.SAVE_GRID,
                    SettingsEntry.JOURNAL,
                    SettingsEntry.STATS,
                    SettingsEntry.PLAYER_NAME,
                    SettingsEntry.HALL_OF_FAME,
                    SettingsEntry.CLEAR_HISTORY,
                    SettingsEntry.EXPORT_DATA,
                    SettingsEntry.IMPORT_DATA,
                    SettingsEntry.SOUND,
                    SettingsEntry.ANIMATIONS,
                    SettingsEntry.MEDIA_LOG
                )

            GameMode.BEES_GECKOS ->
                listOf(
                    SettingsEntry.GAME_MODE,
                    SettingsEntry.DIFFICULTY,
                    SettingsEntry.RECENTER,
                    SettingsEntry.NEXT_UNRESOLVED,
                    SettingsEntry.STATS,
                    SettingsEntry.PLAYER_NAME,
                    SettingsEntry.HALL_OF_FAME,
                    SettingsEntry.CLEAR_HISTORY,
                    SettingsEntry.EXPORT_DATA,
                    SettingsEntry.IMPORT_DATA,
                    SettingsEntry.SOUND,
                    SettingsEntry.ANIMATIONS,
                    SettingsEntry.MEDIA_LOG
                )

            GameMode.SUDOKU ->
                listOf(
                    SettingsEntry.GAME_MODE,
                    SettingsEntry.DIFFICULTY,
                    SettingsEntry.STATS,
                    SettingsEntry.PLAYER_NAME,
                    SettingsEntry.HALL_OF_FAME,
                    SettingsEntry.CLEAR_HISTORY,
                    SettingsEntry.EXPORT_DATA,
                    SettingsEntry.IMPORT_DATA,
                    SettingsEntry.SOUND,
                    SettingsEntry.ANIMATIONS,
                    SettingsEntry.MEDIA_LOG
                )

            GameMode.GOMOKU ->
                listOf(
                    SettingsEntry.GAME_MODE,
                    SettingsEntry.DIFFICULTY,
                    SettingsEntry.STATS,
                    SettingsEntry.PLAYER_NAME,
                    SettingsEntry.HALL_OF_FAME,
                    SettingsEntry.CLEAR_HISTORY,
                    SettingsEntry.EXPORT_DATA,
                    SettingsEntry.IMPORT_DATA,
                    SettingsEntry.SOUND,
                    SettingsEntry.ANIMATIONS,
                    SettingsEntry.GOMOKU_ANIMATION_LIMIT,
                    SettingsEntry.MEDIA_LOG
                )
        }

    val affectsBoardLayout:
        Boolean = false
}

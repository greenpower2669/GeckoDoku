package com.greenpower2669.geckodoku

import android.content.Context

enum class GameMode {
    GECKODOKU,
    SUDOKU,
    GOMOKU,
    BEES_GECKOS
}

enum class GomokuMatchMode {
    VS_PROFESSOR,
    HUMAN_VS_HUMAN
}

enum class SudokuVisualStyle {
    CLASSIC_NUMBERS,
    GECKO_NB,
    GECKO_COLORED
}

object SudokuVisualStylePolicy {
    fun styleForSlot(
        slot: Int
    ): SudokuVisualStyle =
        SudokuVisualStyle.entries[
            slot.coerceIn(
                0,
                SudokuVisualStyle.entries.lastIndex
            )
        ]

    fun slotForStyle(
        style: SudokuVisualStyle
    ): Int =
        SudokuVisualStyle.entries
            .indexOf(style)
            .coerceAtLeast(0)
}

class GameModePreferences(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            "geckodoku_game_mode_v1",
            Context.MODE_PRIVATE
        )

    var gameMode: GameMode
        get() =
            enumValueOrDefault(
                prefs.getString(
                    KEY_GAME_MODE,
                    null
                ),
                GameMode.GECKODOKU
            )
        set(value) {
            prefs.edit()
                .putString(
                    KEY_GAME_MODE,
                    value.name
                )
                .apply()
        }

    var classicSize: Int
        get() =
            prefs.getInt(
                KEY_CLASSIC_SIZE,
                5
            )
                .coerceIn(
                    5,
                    12
                )
        set(value) {
            prefs.edit()
                .putInt(
                    KEY_CLASSIC_SIZE,
                    value.coerceIn(
                        5,
                        12
                    )
                )
                .apply()
        }

    var selectedDifficulty:
        GameDifficulty
        get() =
            enumValueOrDefault(
                prefs.getString(
                    KEY_SELECTED_DIFFICULTY,
                    null
                ),
                GameDifficulty.EASY
            )
        set(value) {
            prefs.edit()
                .putString(
                    KEY_SELECTED_DIFFICULTY,
                    value.name
                )
                .apply()
        }

    var gomokuMatchMode:
        GomokuMatchMode
        get() =
            enumValueOrDefault(
                prefs.getString(
                    KEY_GOMOKU_MATCH_MODE,
                    null
                ),
                GomokuMatchMode
                    .VS_PROFESSOR
            )
        set(value) {
            prefs.edit()
                .putString(
                    KEY_GOMOKU_MATCH_MODE,
                    value.name
                )
                .apply()
        }

    var sudokuVisualStyle:
        SudokuVisualStyle
        get() =
            enumValueOrDefault(
                prefs.getString(
                    KEY_SUDOKU_STYLE,
                    null
                ),
                SudokuVisualStyle
                    .CLASSIC_NUMBERS
            )
        set(value) {
            prefs.edit()
                .putString(
                    KEY_SUDOKU_STYLE,
                    value.name
                )
                .apply()
        }

    private inline fun <reified T : Enum<T>>
        enumValueOrDefault(
            raw: String?,
            fallback: T
        ): T =
        try {
            if (raw == null) {
                fallback
            } else {
                enumValueOf<T>(raw)
            }
        } catch (_: IllegalArgumentException) {
            fallback
        }

    companion object {
        private const val KEY_GAME_MODE =
            "game_mode"
        private const val KEY_SUDOKU_STYLE =
            "sudoku_visual_style"
        private const val KEY_GOMOKU_MATCH_MODE =
            "gomoku_match_mode"
        private const val KEY_CLASSIC_SIZE =
            "classic_size"
        private const val KEY_SELECTED_DIFFICULTY =
            "selected_difficulty"
    }
}

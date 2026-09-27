package com.greenpower2669.geckodoku

import android.content.Context

enum class GameMode {
    GECKODOKU,
    SUDOKU
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
    }
}

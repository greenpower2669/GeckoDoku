package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuSettingsPolicyTest {
    @Test
    fun difficultyMovesIntoSettingsOnlyForSudoku() {
        val policy =
            SettingsMenuPolicy()

        assertTrue(
            SettingsEntry.DIFFICULTY in
                policy.entriesFor(
                    GameMode.SUDOKU
                )
        )

        assertFalse(
            SettingsEntry.DIFFICULTY in
                policy.entriesFor(
                    GameMode.GECKODOKU
                )
        )

        assertTrue(
            SettingsEntry.DIFFICULTY in
                policy.entriesFor(
                    GameMode.GOMOKU
                )
        )

        assertTrue(
            SettingsEntry.SAVE_GRID in
                policy.entriesFor(
                    GameMode.GECKODOKU
                )
        )
        assertTrue(
            SettingsEntry.JOURNAL in
                policy.entriesFor(
                    GameMode.GECKODOKU
                )
        )
        assertFalse(
            SettingsEntry.SAVE_GRID in
                policy.entriesFor(
                    GameMode.SUDOKU
                )
        )
    }

    @Test
    fun pseudoAndProfilesStayAvailableInEveryMode() {
        val policy =
            SettingsMenuPolicy()

        GameMode.entries
            .forEach {
                mode ->

                val entries =
                    policy.entriesFor(
                        mode
                    )

                assertTrue(
                    SettingsEntry.PLAYER_NAME in
                        entries
                )

                assertTrue(
                    SettingsEntry.PLAYER_PROFILE in
                        entries
                )
            }
    }

}

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
    }
}

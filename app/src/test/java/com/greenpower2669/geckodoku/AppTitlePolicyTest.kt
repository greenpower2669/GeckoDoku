package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppTitlePolicyTest {
    @Test
    fun appTitleNeverContainsModeName() {
        for (mode in GameMode.entries) {
            val title =
                AppTitlePolicy.titleFor(
                    mode
                )

            assertEquals(
                "GeckoDoku 🦎",
                title
            )
            assertFalse(
                title.contains(
                    "Sudoku"
                )
            )
            assertFalse(
                title.contains(
                    "Gomoku"
                )
            )
        }
    }
}

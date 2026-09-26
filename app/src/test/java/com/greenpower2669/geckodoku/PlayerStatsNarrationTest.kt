package com.greenpower2669.geckodoku

import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStatsNarrationTest {
    @Test
    fun narrationContainsCoreStats() {
        val text =
            PlayerStatsNarration.build(
                LocalPlayerStats(
                    gamesStarted = 12,
                    gamesCompleted = 7,
                    assistedCompleted = 2,
                    mistakes = 4,
                    totalSeconds = 700
                )
            )

        assertTrue(text.contains("12"))
        assertTrue(text.contains("7"))
        assertTrue(text.contains("58"))
        assertTrue(text.contains("4"))
    }
}

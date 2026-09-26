package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RichMediaSchedulerDefaultFrequencyTest {
    @Test
    fun defaultsUseFortyFivePercentAndFortyFiveSecondCooldown() {
        val scheduler = RichMediaScheduler()

        assertTrue(
            scheduler.shouldPlayLongAction(
                nowMs = 1L,
                randomValue = 44,
                eligible = true,
                busy = false
            )
        )

        assertFalse(
            scheduler.shouldPlayLongAction(
                nowMs = 44_999L,
                randomValue = 0,
                eligible = true,
                busy = false
            )
        )

        assertTrue(
            scheduler.shouldPlayLongAction(
                nowMs = 45_001L,
                randomValue = 44,
                eligible = true,
                busy = false
            )
        )
    }
}

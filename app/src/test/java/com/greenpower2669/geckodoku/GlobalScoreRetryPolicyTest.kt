package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class GlobalScoreRetryPolicyTest {
    @Test
    fun progressiveBackoffMatchesContract() {
        assertEquals(5_000L, GlobalScoreRetryPolicy.delayMs(1, null))
        assertEquals(15_000L, GlobalScoreRetryPolicy.delayMs(2, null))
        assertEquals(30_000L, GlobalScoreRetryPolicy.delayMs(3, null))
        assertEquals(60_000L, GlobalScoreRetryPolicy.delayMs(4, null))
        assertEquals(300_000L, GlobalScoreRetryPolicy.delayMs(5, null))
        assertEquals(300_000L, GlobalScoreRetryPolicy.delayMs(99, null))
    }

    @Test
    fun retryAfterOverridesNormalBackoff() {
        assertEquals(
            60_000L,
            GlobalScoreRetryPolicy.delayMs(
                attemptNumber = 1,
                retryAfterSeconds = 60L
            )
        )
    }

    @Test
    fun negativeRetryAfterCannotScheduleInPast() {
        assertEquals(
            0L,
            GlobalScoreRetryPolicy.delayMs(
                attemptNumber = 3,
                retryAfterSeconds = -1L
            )
        )
    }
}

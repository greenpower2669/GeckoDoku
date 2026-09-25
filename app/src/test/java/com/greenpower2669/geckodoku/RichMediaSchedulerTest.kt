package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RichMediaSchedulerTest {
    @Test
    fun ineligibleOrBusyEventNeverStarts() {
        val scheduler = RichMediaScheduler(cooldownMs = 180_000L, chancePercent = 12)
        assertFalse(scheduler.shouldPlayLongAction(1_000L, 0, eligible = false, busy = false))
        assertFalse(scheduler.shouldPlayLongAction(1_000L, 0, eligible = true, busy = true))
    }

    @Test
    fun eligibleEventUsesConfiguredProbability() {
        val scheduler = RichMediaScheduler(cooldownMs = 180_000L, chancePercent = 12)
        assertTrue(scheduler.shouldPlayLongAction(1_000L, 11, eligible = true, busy = false))
    }

    @Test
    fun acceptedLongActionStartsSharedCooldown() {
        val scheduler = RichMediaScheduler(cooldownMs = 180_000L, chancePercent = 12)
        assertTrue(scheduler.shouldPlayLongAction(1_000L, 0, eligible = true, busy = false))
        assertFalse(scheduler.shouldPlayLongAction(180_999L, 0, eligible = true, busy = false))
        assertTrue(scheduler.shouldPlayLongAction(181_000L, 0, eligible = true, busy = false))
    }
}

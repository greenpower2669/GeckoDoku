package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfessorIdleAnimationPolicyTest {
    @Test
    fun idleDelayIsAlwaysBetweenTwoAndThreeSeconds() {
        val p = ProfessorIdleAnimationPolicy()
        assertEquals(2_000L, p.delayMs(0))
        assertTrue(p.delayMs(Int.MAX_VALUE) in 2_000L..3_000L)
    }

    @Test
    fun policyProvidesSeveralAnimationActions() {
        val p = ProfessorIdleAnimationPolicy()
        val values = (0..20).map { p.actionFor(it) }.toSet()
        assertTrue(values.size >= 3)
    }
}

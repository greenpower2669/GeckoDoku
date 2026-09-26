package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfessorAnimationPolicyTest {
    @Test
    fun eachEligibleProfessorInterventionAnimatesWithoutRandomGate() {
        val policy = ProfessorAnimationPolicy()

        assertTrue(
            policy.shouldAnimate(
                animationsEnabled = true,
                mediaBusy = false,
                hypothesisPending = false,
                celebrationVisible = false
            )
        )
    }

    @Test
    fun priorityConditionsStillBlockProfessorAnimation() {
        val policy = ProfessorAnimationPolicy()

        assertFalse(policy.shouldAnimate(false, false, false, false))
        assertFalse(policy.shouldAnimate(true, true, false, false))
        assertFalse(policy.shouldAnimate(true, false, true, false))
        assertFalse(policy.shouldAnimate(true, false, false, true))
    }
}

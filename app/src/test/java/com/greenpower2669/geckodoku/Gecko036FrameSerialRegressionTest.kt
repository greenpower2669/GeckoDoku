package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gecko036FrameSerialRegressionTest {
    @Test
    fun staleFrameIsConsumedLogicallyButOnlyNewerSerialMayValidate() {
        val gate =
            FreshFrameSerialGate()

        gate.arm(
            generation = 7L,
            currentProducedSerial = 12L
        )

        assertFalse(
            gate.accept(
                generation = 7L,
                consumedSerial = 12L
            )
        )

        assertTrue(
            gate.accept(
                generation = 7L,
                consumedSerial = 13L
            )
        )

        assertFalse(
            gate.accept(
                generation = 7L,
                consumedSerial = 14L
            )
        )
    }

    @Test
    fun oldGenerationNeverValidatesCurrentPlayback() {
        val gate =
            FreshFrameSerialGate()

        gate.arm(
            generation = 9L,
            currentProducedSerial = 20L
        )

        assertFalse(
            gate.accept(
                generation = 8L,
                consumedSerial = 21L
            )
        )

        assertTrue(
            gate.accept(
                generation = 9L,
                consumedSerial = 21L
            )
        )
    }

    @Test
    fun introPathDoesNotArmFreshFrameGate() {
        val policy =
            FirstFrameGateActivationPolicy()

        assertFalse(
            policy.shouldArm(
                revealOnFirstFrame = false
            )
        )

        assertTrue(
            policy.shouldArm(
                revealOnFirstFrame = true
            )
        )
    }
}

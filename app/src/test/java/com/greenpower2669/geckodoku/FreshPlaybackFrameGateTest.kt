package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreshPlaybackFrameGateTest {
    @Test
    fun renderingStartCanArriveBeforePlayerStarted() {
        val gate =
            FreshPlaybackFrameGate()

        val generation =
            gate.beginPlayback()

        gate.onRenderingStart(
            generation
        )

        assertFalse(
            gate.onFrameRendered(
                generation
            )
        )

        gate.onPlayerStarted(
            generation
        )

        assertTrue(
            gate.onFrameRendered(
                generation
            )
        )

        assertFalse(
            gate.onFrameRendered(
                generation
            )
        )
    }

    @Test
    fun staleRenderingStartCannotUnlockNewGeneration() {
        val gate =
            FreshPlaybackFrameGate()

        val oldGeneration =
            gate.beginPlayback()

        gate.onPlayerStarted(
            oldGeneration
        )

        val generation =
            gate.beginPlayback()

        gate.onRenderingStart(
            oldGeneration
        )
        gate.onPlayerStarted(
            generation
        )

        assertFalse(
            gate.onFrameRendered(
                generation
            )
        )

        gate.onRenderingStart(
            generation
        )

        assertTrue(
            gate.onFrameRendered(
                generation
            )
        )
    }

    @Test
    fun cancelKeepsPlaybackClosed() {
        val gate =
            FreshPlaybackFrameGate()

        val generation =
            gate.beginPlayback()

        gate.onRenderingStart(
            generation
        )
        gate.onPlayerStarted(
            generation
        )
        gate.cancel(
            generation
        )

        assertFalse(
            gate.onFrameRendered(
                generation
            )
        )
    }
}

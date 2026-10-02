package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gecko034PresentationTest {
    @Test
    fun videoStaysHiddenUntilFirstRenderedFrame() {
        val gate = FirstFrameVisibilityGate()

        gate.arm()

        assertFalse(gate.isVisible)
        gate.onPrepared()
        assertFalse(gate.isVisible)
        gate.onStarted()
        assertFalse(gate.isVisible)

        gate.onFirstFrameRendered()

        assertTrue(gate.isVisible)
    }

    @Test
    fun abortedVideoNeverBecomesVisible() {
        val gate = FirstFrameVisibilityGate()

        gate.arm()
        gate.onPrepared()
        gate.onStarted()
        gate.abort()

        gate.onFirstFrameRendered()

        assertFalse(gate.isVisible)
    }

    @Test
    fun firstFrameGatesAreIndependentPerSession() {
        val first = FirstFrameVisibilityGate()
        val second = FirstFrameVisibilityGate()

        first.arm()
        second.arm()

        first.onFirstFrameRendered()

        assertTrue(first.isVisible)
        assertFalse(second.isVisible)
    }

    @Test
    fun boardGeometryRemainsFrozenInsideSameWindow() {
        val policy = BoardGeometryPolicy()

        val initial =
            BoardGeometry(
                left = 8,
                top = 220,
                width = 1064,
                height = 1064
            )

        val disturbed =
            BoardGeometry(
                left = 8,
                top = 270,
                width = 1064,
                height = 1014
            )

        assertEquals(
            initial,
            policy.resolve(
                windowWidth = 1080,
                windowHeight = 2400,
                proposed = initial
            )
        )

        assertEquals(
            initial,
            policy.resolve(
                windowWidth = 1080,
                windowHeight = 2400,
                proposed = disturbed
            )
        )
    }

    @Test
    fun boardGeometryMayReanchorOnlyWhenWindowChanges() {
        val policy = BoardGeometryPolicy()

        val portrait =
            BoardGeometry(
                left = 8,
                top = 220,
                width = 1064,
                height = 1064
            )

        val landscape =
            BoardGeometry(
                left = 8,
                top = 120,
                width = 1900,
                height = 720
            )

        policy.resolve(
            windowWidth = 1080,
            windowHeight = 2400,
            proposed = portrait
        )

        assertEquals(
            landscape,
            policy.resolve(
                windowWidth = 1920,
                windowHeight = 1080,
                proposed = landscape
            )
        )
    }
}

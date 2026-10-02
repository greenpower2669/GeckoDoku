package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GomokuViewportPolicyTest {
    @Test
    fun defaultIsTwelveAndPinchZoomsAroundFocus() {
        val policy =
            GomokuViewportPolicy(
                boardSize = 19
            )

        val initial =
            policy.initial()

        assertEquals(
            12f,
            initial.visibleSpan,
            .001f
        )

        val zoomedIn =
            policy.zoom(
                viewport = initial,
                scaleFactor = 2f,
                focusXFraction = .5f,
                focusYFraction = .5f
            )

        assertEquals(
            6f,
            zoomedIn.visibleSpan,
            .001f
        )

        val zoomedOut =
            policy.zoom(
                viewport = zoomedIn,
                scaleFactor = .3f,
                focusXFraction = .5f,
                focusYFraction = .5f
            )

        assertTrue(
            zoomedOut.visibleSpan >
                zoomedIn.visibleSpan
        )
        assertTrue(
            zoomedOut.visibleSpan <=
                19f
        )
    }

    @Test
    fun panRemainsInsideLogicalBoard() {
        val policy =
            GomokuViewportPolicy(
                boardSize = 19
            )

        val moved =
            policy.pan(
                viewport =
                    policy.initial(),
                deltaCols = 100f,
                deltaRows = 100f
            )

        assertTrue(
            moved.originCol >= 0f
        )
        assertTrue(
            moved.originRow >= 0f
        )
        assertTrue(
            moved.originCol +
                moved.visibleSpan <=
                19.001f
        )
        assertTrue(
            moved.originRow +
                moved.visibleSpan <=
                19.001f
        )
    }
}

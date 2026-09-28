package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BeeGeckoLogicalMarkerTest {
    @Test
    fun crossStateCyclesYellowGreenRedThenNone() {
        assertEquals(
            BeeGeckoCrossState.CONFIRMED,
            BeeGeckoCrossState
                .HYPOTHESIS
                .next()
        )

        assertEquals(
            BeeGeckoCrossState.IMPOSSIBLE,
            BeeGeckoCrossState
                .CONFIRMED
                .next()
        )

        assertEquals(
            null,
            BeeGeckoCrossState
                .IMPOSSIBLE
                .next()
        )
    }

    @Test
    fun logicalPieceMarkersCanCoexistAndToggle() {
        val initial =
            BeeGeckoLogicalMarks()

        val gecko =
            initial.toggleGecko()

        assertTrue(
            gecko.geckoCandidate
        )
        assertFalse(
            gecko.beeCandidate
        )

        val both =
            gecko.toggleBee()

        assertTrue(
            both.geckoCandidate
        )
        assertTrue(
            both.beeCandidate
        )

        val beeOnly =
            both.toggleGecko()

        assertFalse(
            beeOnly.geckoCandidate
        )
        assertTrue(
            beeOnly.beeCandidate
        )
    }

    @Test
    fun threeHexAxesCanBeMarkedIndependently() {
        var marker =
            BeeGeckoLogicalMarks()

        HexAxis.entries
            .forEach {
                axis ->
                marker =
                    marker.toggleAxis(
                        axis
                    )
            }

        assertEquals(
            HexAxis.entries
                .toSet(),
            marker.excludedAxes
        )

        marker =
            marker.toggleAxis(
                HexAxis.Q
            )

        assertFalse(
            HexAxis.Q in
                marker.excludedAxes
        )
    }

    @Test
    fun axisMarkerKeepsChosenColor() {
        val marker =
            BeeGeckoLogicalMarks()
                .setAxis(
                    HexAxis.Q,
                    AxisGuideColor.GREEN
                )
                .setAxis(
                    HexAxis.R,
                    AxisGuideColor.YELLOW
                )

        assertEquals(
            AxisGuideColor.GREEN,
            marker.colorFor(
                HexAxis.Q
            )
        )

        assertEquals(
            AxisGuideColor.YELLOW,
            marker.colorFor(
                HexAxis.R
            )
        )

        assertEquals(
            AxisGuideColor.RED,
            marker.colorFor(
                HexAxis.S
            )
        )
    }

    @Test
    fun fogPhaseStaysNormalized() {
        val phase =
            BeeGeckoFogPolicy
                .phase(
                    nowMs = 9_999L,
                    seed = 17
                )

        assertTrue(
            phase >= 0f &&
                phase < 1f
        )
    }
}

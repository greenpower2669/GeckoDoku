package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class GomokuGesturePolicyTest {
    private val policy =
        GomokuGesturePolicy(
            dragThresholdPx = 12f
        )

    @Test
    fun cleanTapPlays() {
        assertEquals(
            GomokuGestureAction.PLAY,
            policy.actionFor(
                pointerCount = 1,
                distancePx = 4f,
                scaleInProgress = false
            )
        )
    }

    @Test
    fun dragNeverPlays() {
        assertEquals(
            GomokuGestureAction.PAN,
            policy.actionFor(
                pointerCount = 1,
                distancePx = 40f,
                scaleInProgress = false
            )
        )
    }

    @Test
    fun pinchAlwaysZoomsAndNeverPlays() {
        assertEquals(
            GomokuGestureAction.ZOOM,
            policy.actionFor(
                pointerCount = 2,
                distancePx = 0f,
                scaleInProgress = true
            )
        )
    }
}

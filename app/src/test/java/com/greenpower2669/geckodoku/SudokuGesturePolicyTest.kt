package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class SudokuGesturePolicyTest {
    @Test
    fun doubleTapOpensPersonalMarkersAndLongPressKeepsInputPalette() {
        val policy =
            SudokuGesturePolicy()

        assertEquals(
            SudokuGestureAction
                .OPEN_PERSONAL_MARKERS,
            policy.actionFor(
                SudokuGesture.DOUBLE_TAP
            )
        )

        assertEquals(
            SudokuGestureAction
                .OPEN_INPUT_PALETTE,
            policy.actionFor(
                SudokuGesture.LONG_PRESS
            )
        )
    }
}

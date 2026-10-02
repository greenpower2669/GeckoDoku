package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class SudokuGesturePolicyTest {
    @Test
    fun singleAndDoubleTapOpenPersistentInputPalette() {
        val policy =
            SudokuGesturePolicy()

        assertEquals(
            SudokuGestureAction
                .OPEN_INPUT_PALETTE,
            policy.actionFor(
                SudokuGesture
                    .SINGLE_TAP
            )
        )

        assertEquals(
            SudokuGestureAction
                .OPEN_INPUT_PALETTE,
            policy.actionFor(
                SudokuGesture
                    .DOUBLE_TAP
            )
        )
    }

    @Test
    fun longPressKeepsPersonalMarkersAccessible() {
        val policy =
            SudokuGesturePolicy()

        assertEquals(
            SudokuGestureAction
                .OPEN_PERSONAL_MARKERS,
            policy.actionFor(
                SudokuGesture
                    .LONG_PRESS
            )
        )
    }
}

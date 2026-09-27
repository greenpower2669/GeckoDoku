package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class SudokuFullWidthBoardPolicyTest {
    @Test
    fun gridUsesFullUsefulWidthWithThreePixelsPerSide() {
        val policy =
            SudokuFullWidthBoardPolicy(
                horizontalMarginPx = 3
            )

        val geometry =
            policy.geometry(
                usefulWidthPx = 720,
                topPx = 240
            )

        assertEquals(3, geometry.left)
        assertEquals(240, geometry.top)
        assertEquals(714, geometry.width)
        assertEquals(714, geometry.height)
        assertEquals(
            3,
            720 -
                geometry.left -
                geometry.width
        )
    }

    @Test
    fun innerGridMarginIsZeroSoOuterThreePixelsRemainTheRealMargin() {
        assertEquals(
            0,
            SudokuFullWidthBoardPolicy
                .INNER_GRID_MARGIN_PX
        )
    }
}

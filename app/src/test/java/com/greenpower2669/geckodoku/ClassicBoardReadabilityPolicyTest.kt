package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class ClassicBoardReadabilityPolicyTest {
    @Test
    fun gridUsesAlmostAllWidthWhenHeightAllows() {
        val geometry =
            ClassicBoardReadabilityPolicy(
                horizontalMarginPx = 3,
                topMarginPx = 3
            ).geometry(
                viewWidthPx = 720,
                viewHeightPx = 1000,
                gutterPx = 120
            )

        assertEquals(3, geometry.left)
        assertEquals(3, geometry.top)
        assertEquals(714, geometry.side)
    }

    @Test
    fun gridFallsBackToAvailableHeightWithoutOverflow() {
        val geometry =
            ClassicBoardReadabilityPolicy(
                horizontalMarginPx = 3,
                topMarginPx = 3
            ).geometry(
                viewWidthPx = 720,
                viewHeightPx = 600,
                gutterPx = 100
            )

        assertEquals(497, geometry.side)
        assertEquals(111, geometry.left)
    }
}

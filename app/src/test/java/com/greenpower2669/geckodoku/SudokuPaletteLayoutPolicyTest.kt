package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuPaletteLayoutPolicyTest {
    @Test
    fun leftGridSelectsValuesAndRightGridSelectsCandidates() {
        val policy =
            SudokuPaletteLayoutPolicy()

        assertEquals(
            SudokuPaletteAction.Value(1),
            policy.actionAt(
                x = 20f,
                y = 45f,
                width = 300,
                height = 220
            )
        )

        assertEquals(
            SudokuPaletteAction.Candidate(1),
            policy.actionAt(
                x = 170f,
                y = 45f,
                width = 300,
                height = 220
            )
        )
    }

    @Test
    fun footerOffersGeckoMarkerOnLeftAndEraseOnRight() {
        val policy =
            SudokuPaletteLayoutPolicy()

        assertEquals(
            SudokuPaletteAction
                .GeckoMarker,
            policy.actionAt(
                x = 40f,
                y = 210f,
                width = 300,
                height = 220
            )
        )

        assertEquals(
            SudokuPaletteAction
                .Erase,
            policy.actionAt(
                x = 260f,
                y = 210f,
                width = 300,
                height = 220
            )
        )
    }

    @Test
    fun popupPlacementAlwaysStaysInsideScreenAndCanFlipAroundAnchor() {
        val policy =
            SudokuPopupPlacementPolicy()

        val nearRight =
            policy.place(
                screenWidth = 720,
                screenHeight = 1500,
                anchor = PixelBox(
                    left = 620,
                    top = 600,
                    right = 690,
                    bottom = 670
                ),
                popupWidth = 560,
                popupHeight = 440,
                margin = 16
            )

        assertTrue(nearRight.x >= 16)
        assertTrue(nearRight.y >= 16)
        assertTrue(
            nearRight.x + 560 <=
                720 - 16
        )
        assertTrue(
            nearRight.y + 440 <=
                1500 - 16
        )
        assertTrue(nearRight.x < 620)
    }
}

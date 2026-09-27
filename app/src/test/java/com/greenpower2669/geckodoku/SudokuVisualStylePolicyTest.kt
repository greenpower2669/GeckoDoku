package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SudokuVisualStylePolicyTest {
    @Test
    fun threeSlotsMapToThreeDistinctStyles() {
        val left =
            SudokuVisualStylePolicy.styleForSlot(0)
        val middle =
            SudokuVisualStylePolicy.styleForSlot(1)
        val right =
            SudokuVisualStylePolicy.styleForSlot(2)

        assertEquals(
            SudokuVisualStyle.CLASSIC_NUMBERS,
            left
        )
        assertEquals(
            SudokuVisualStyle.GECKO_NB,
            middle
        )
        assertEquals(
            SudokuVisualStyle.GECKO_COLORED,
            right
        )
        assertNotEquals(left, middle)
        assertNotEquals(middle, right)
        assertNotEquals(left, right)
    }

    @Test
    fun slotSelectionIsClampedSafely() {
        assertEquals(
            SudokuVisualStyle.CLASSIC_NUMBERS,
            SudokuVisualStylePolicy.styleForSlot(-10)
        )
        assertEquals(
            SudokuVisualStyle.GECKO_COLORED,
            SudokuVisualStylePolicy.styleForSlot(99)
        )
    }

    @Test
    fun gameModeAndVisualStyleRemainSeparateConcepts() {
        assertEquals(3, GameMode.entries.size)
        assertEquals(3, SudokuVisualStyle.entries.size)
    }
}

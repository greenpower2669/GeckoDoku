package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuCandidateLayoutTest {
    @Test
    fun digitsOccupyStableThreeByThreeSlotsInsideCell() {
        val seen =
            linkedSetOf<Pair<Int, Int>>()

        for (digit in 1..9) {
            val slot =
                SudokuCandidateLayout.slot(
                    digit
                )

            assertTrue(slot.left >= 0f)
            assertTrue(slot.top >= 0f)
            assertTrue(slot.right <= 1f)
            assertTrue(slot.bottom <= 1f)
            assertTrue(slot.right > slot.left)
            assertTrue(slot.bottom > slot.top)

            seen +=
                slot.row to slot.col
        }

        assertEquals(9, seen.size)
        assertEquals(
            0 to 0,
            SudokuCandidateLayout
                .slot(1)
                .let { it.row to it.col }
        )
        assertEquals(
            2 to 2,
            SudokuCandidateLayout
                .slot(9)
                .let { it.row to it.col }
        )
    }
}

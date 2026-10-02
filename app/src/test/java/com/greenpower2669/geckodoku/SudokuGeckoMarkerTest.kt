package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuGeckoMarkerTest {
    private fun puzzle(): SudokuPuzzle {
        val solution =
            intArrayOf(
                5,3,4,6,7,8,9,1,2,
                6,7,2,1,9,5,3,4,8,
                1,9,8,3,4,2,5,6,7,
                8,5,9,7,6,1,4,2,3,
                4,2,6,8,5,3,7,9,1,
                7,1,3,9,2,4,8,5,6,
                9,6,1,5,3,7,2,8,4,
                2,8,7,4,1,9,6,3,5,
                3,4,5,2,8,6,1,7,9
            )

        val givens =
            solution.copyOf()

        givens[80] = 0

        return SudokuPuzzle(
            solution = solution,
            givens = givens,
            difficulty =
                GameDifficulty.EASY,
            seed = 99L
        )
    }

    @Test
    fun playerMarkerIsPureAnnotationAndRoundTripsUndoRedo() {
        val engine =
            SudokuGameEngine(
                puzzle()
            )

        val cell =
            Cell(8, 8)

        assertEquals(
            SudokuActionFeedback
                .MARKER_TOGGLED,
            engine.toggleGeckoMarker(
                cell
            )
        )

        assertTrue(
            engine.snapshot()
                .hasGeckoMarker(cell)
        )

        assertEquals(
            0,
            engine.snapshot()
                .valueAt(cell)
        )

        engine.undo()

        assertFalse(
            engine.snapshot()
                .hasGeckoMarker(cell)
        )

        engine.redo()

        assertTrue(
            engine.snapshot()
                .hasGeckoMarker(cell)
        )
    }

    @Test
    fun placingRealValueClearsMarker() {
        val engine =
            SudokuGameEngine(
                puzzle()
            )

        val cell =
            Cell(8, 8)

        engine.toggleGeckoMarker(
            cell
        )

        engine.enterDigit(
            cell = cell,
            digit = 9
        )

        assertFalse(
            engine.snapshot()
                .hasGeckoMarker(cell)
        )
    }

    @Test
    fun givenCellCannotReceiveMarker() {
        val engine =
            SudokuGameEngine(
                puzzle()
            )

        assertEquals(
            SudokuActionFeedback
                .GIVEN_LOCKED,
            engine.toggleGeckoMarker(
                Cell(0, 0)
            )
        )
    }
}

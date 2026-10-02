package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SudokuPersonalMarkerTest {
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
            difficulty = GameDifficulty.EASY,
            seed = 3902L
        )
    }

    @Test
    fun personalMarkerIsVisualUndoableAndClearedByValue() {
        val engine =
            SudokuGameEngine(
                puzzle()
            )
        val cell = Cell(8, 8)

        assertEquals(
            SudokuActionFeedback
                .PERSONAL_MARKER_SET,
            engine.setCustomMarker(
                cell,
                CustomMarker.STAR
            )
        )

        assertEquals(
            CustomMarker.STAR,
            engine.snapshot()
                .customMarkerAt(cell)
        )

        engine.undo()

        assertNull(
            engine.snapshot()
                .customMarkerAt(cell)
        )

        engine.redo()

        assertEquals(
            CustomMarker.STAR,
            engine.snapshot()
                .customMarkerAt(cell)
        )

        engine.enterDigit(
            cell = cell,
            digit = 9
        )

        assertNull(
            engine.snapshot()
                .customMarkerAt(cell)
        )
    }
}

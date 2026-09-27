package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class SudokuCellTapPolicyTest {
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
            seed = 3901L
        )
    }

    @Test
    fun emptyPlayableCellTogglesGeckoMarker() {
        val engine =
            SudokuGameEngine(
                puzzle()
            )

        val action =
            SudokuCellTapPolicy()
                .actionFor(
                    engine.snapshot(),
                    Cell(8, 8)
                )

        assertEquals(
            SudokuCellTapAction
                .TOGGLE_GECKO_MARKER,
            action
        )
    }

    @Test
    fun givenCellOnlySelectsAndNeverTogglesMarker() {
        val engine =
            SudokuGameEngine(
                puzzle()
            )

        assertEquals(
            SudokuCellTapAction
                .SELECT_ONLY,
            SudokuCellTapPolicy()
                .actionFor(
                    engine.snapshot(),
                    Cell(0, 0)
                )
        )
    }

    @Test
    fun filledPlayableCellOnlySelects() {
        val engine =
            SudokuGameEngine(
                puzzle()
            )

        engine.enterDigit(
            cell = Cell(8, 8),
            digit = 9
        )

        assertEquals(
            SudokuCellTapAction
                .SELECT_ONLY,
            SudokuCellTapPolicy()
                .actionFor(
                    engine.snapshot(),
                    Cell(8, 8)
                )
        )
    }
}

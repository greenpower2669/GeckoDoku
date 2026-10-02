package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuReasoningTraceTest {
    @Test
    fun nakedSingleCarriesOrderedRealReasoning() {
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

        val puzzle =
            SudokuPuzzle(
                solution = solution,
                givens = givens,
                difficulty = GameDifficulty.EASY,
                seed = 3903L
            )

        val hint =
            SudokuHintEngine.nextHint(
                puzzle,
                SudokuGameEngine(puzzle)
                    .snapshot()
            )

        assertNotNull(hint)

        val trace =
            requireNotNull(
                hint?.reasoning
            )

        assertEquals(
            hint.cell,
            trace.targetCell
        )
        assertEquals(
            hint.digit,
            trace.targetDigit
        )
        assertTrue(
            trace.steps.isNotEmpty()
        )
        assertTrue(
            trace.steps.any {
                it.eliminations
                    .isNotEmpty()
            }
        )
        assertEquals(
            hint.cell,
            trace.steps.last()
                .finalCell
        )
        assertFalse(
            trace.detailedExplanation
                .startsWith(
                    "Prof Gecko"
                )
        )
    }
}

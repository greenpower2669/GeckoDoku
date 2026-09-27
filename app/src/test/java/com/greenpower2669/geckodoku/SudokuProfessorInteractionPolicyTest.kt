package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuProfessorInteractionPolicyTest {
    private fun puzzle():
        SudokuPuzzle {
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
            seed = 77L
        )
    }

    @Test
    fun firstTapExplainsAndSecondTapAppliesSameStillValidHint() {
        val puzzle = puzzle()
        val engine =
            SudokuGameEngine(puzzle)

        val policy =
            SudokuProfessorInteractionPolicy()

        val first =
            policy.onTap(
                puzzle,
                engine.snapshot()
            )

        assertTrue(
            first is
                SudokuProfessorDecision
                    .Explain
        )
        assertEquals(
            0,
            engine.snapshot()
                .valueAt(
                    Cell(8, 8)
                )
        )

        val second =
            policy.onTap(
                puzzle,
                engine.snapshot()
            )

        assertTrue(
            second is
                SudokuProfessorDecision
                    .Apply
        )

        val apply =
            second as
                SudokuProfessorDecision
                    .Apply

        assertEquals(
            Cell(8, 8),
            apply.hint.cell
        )
        assertEquals(
            9,
            apply.hint.digit
        )
    }

    @Test
    fun noteMutationMakesPendingStaleSoNextTapExplainsAgain() {
        val puzzle = puzzle()
        val engine =
            SudokuGameEngine(puzzle)

        val policy =
            SudokuProfessorInteractionPolicy()

        policy.onTap(
            puzzle,
            engine.snapshot()
        )

        engine.enterDigit(
            cell = Cell(8, 8),
            digit = 1,
            notesMode = true
        )

        val afterMutation =
            policy.onTap(
                puzzle,
                engine.snapshot()
            )

        assertTrue(
            afterMutation is
                SudokuProfessorDecision
                    .Explain
        )
    }

    @Test
    fun longPressRecalculatesAndReturnsDirectSafeApply() {
        val puzzle = puzzle()
        val engine =
            SudokuGameEngine(puzzle)

        val policy =
            SudokuProfessorInteractionPolicy()

        val decision =
            policy.onLongPress(
                puzzle,
                engine.snapshot()
            )

        assertTrue(
            decision is
                SudokuProfessorDecision
                    .Apply
        )

        val apply =
            decision as
                SudokuProfessorDecision
                    .Apply

        assertEquals(
            SudokuTechnique
                .NAKED_SINGLE,
            apply.hint.technique
        )
        assertEquals(
            9,
            apply.hint.digit
        )
    }
}

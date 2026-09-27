package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SudokuProfessorMoveOriginTest {
    @Test
    fun professorMoveIsUndoableRedoableAndKeepsItsOrigin() {
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

        val engine =
            SudokuGameEngine(
                SudokuPuzzle(
                    solution =
                        solution,
                    givens =
                        givens,
                    difficulty =
                        GameDifficulty.EASY,
                    seed = 88L
                )
            )

        assertEquals(
            SudokuActionFeedback
                .COMPLETED,
            engine.enterDigit(
                cell = Cell(8, 8),
                digit = 9,
                notesMode = false,
                origin =
                    SudokuMoveOrigin
                        .PROFESSOR
            )
        )

        assertEquals(
            SudokuMoveOrigin
                .PROFESSOR,
            engine.snapshot()
                .lastMoveOrigin
        )

        engine.undo()

        assertEquals(
            0,
            engine.snapshot()
                .valueAt(
                    Cell(8, 8)
                )
        )
        assertNull(
            engine.snapshot()
                .lastMoveOrigin
        )

        engine.redo()

        assertEquals(
            9,
            engine.snapshot()
                .valueAt(
                    Cell(8, 8)
                )
        )
        assertEquals(
            SudokuMoveOrigin
                .PROFESSOR,
            engine.snapshot()
                .lastMoveOrigin
        )
    }
}

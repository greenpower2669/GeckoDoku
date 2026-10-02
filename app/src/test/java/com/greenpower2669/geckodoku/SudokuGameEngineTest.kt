package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuGameEngineTest {
    private fun puzzle(): SudokuPuzzle {
        val solution = intArrayOf(
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
        val givens = IntArray(81)
        givens[0] = 5
        givens[10] = 7
        return SudokuPuzzle(
            solution = solution,
            givens = givens,
            difficulty = GameDifficulty.EASY,
            seed = 42L
        )
    }

    @Test
    fun givenCellsAreLockedAndWrongValuesDoNotMutateTheGrid() {
        val engine = SudokuGameEngine(puzzle())

        assertEquals(
            SudokuActionFeedback.GIVEN_LOCKED,
            engine.enterDigit(Cell(0, 0), 5)
        )

        assertEquals(
            SudokuActionFeedback.WRONG_VALUE,
            engine.enterDigit(Cell(0, 1), 9)
        )

        val snapshot = engine.snapshot()
        assertEquals(0, snapshot.valueAt(Cell(0, 1)))
        assertEquals(1, snapshot.mistakes)
    }

    @Test
    fun notesToggleAndUndoRedoRoundTrip() {
        val engine = SudokuGameEngine(puzzle())

        assertEquals(
            SudokuActionFeedback.NOTE_TOGGLED,
            engine.enterDigit(
                cell = Cell(0, 1),
                digit = 3,
                notesMode = true
            )
        )
        assertTrue(
            3 in engine.snapshot().notesAt(Cell(0, 1))
        )

        assertEquals(
            SudokuActionFeedback.UNDONE,
            engine.undo()
        )
        assertFalse(
            3 in engine.snapshot().notesAt(Cell(0, 1))
        )

        assertEquals(
            SudokuActionFeedback.REDONE,
            engine.redo()
        )
        assertTrue(
            3 in engine.snapshot().notesAt(Cell(0, 1))
        )
    }

    @Test
    fun correctValueIsPlacedAndCanBeUndone() {
        val engine = SudokuGameEngine(puzzle())

        assertEquals(
            SudokuActionFeedback.VALUE_SET,
            engine.enterDigit(Cell(0, 1), 3)
        )
        assertEquals(
            3,
            engine.snapshot().valueAt(Cell(0, 1))
        )

        engine.undo()
        assertEquals(
            0,
            engine.snapshot().valueAt(Cell(0, 1))
        )
    }

    @Test
    fun candidatesStayIndependentFromHypothesisBranches() {
        val engine =
            SudokuGameEngine(
                puzzle()
            )

        val cell =
            Cell(
                0,
                1
            )

        engine.enterDigit(
            cell =
                cell,
            digit =
                3,
            notesMode =
                true
        )

        engine.cycleHypothesis(
            cell =
                cell,
            digit =
                4
        )

        assertTrue(
            3 in
                engine
                    .snapshot()
                    .notesAt(
                        cell
                    )
        )

        val node =
            engine
                .snapshot()
                .hypothesisAt(
                    cell
                )

        assertEquals(
            4,
            node
                ?.cell
                ?.digit
        )

        assertEquals(
            HypothesisColor.YELLOW,
            node
                ?.color
        )

        engine.cycleHypothesis(
            cell =
                cell,
            digit =
                4
        )

        assertEquals(
            HypothesisBranchState
                .CONTRADICTION,
            engine
                .snapshot()
                .hypothesisAt(
                    cell
                )
                ?.state
        )

        engine.cycleHypothesis(
            cell =
                cell,
            digit =
                4
        )

        assertEquals(
            null,
            engine
                .snapshot()
                .hypothesisAt(
                    cell
                )
        )

        assertTrue(
            3 in
                engine
                    .snapshot()
                    .notesAt(
                        cell
                    )
        )
    }

    @Test
    fun sudokuHypothesesKeepParentChildColorsAndCanRewind() {
        val engine =
            SudokuGameEngine(
                puzzle()
            )

        val firstCell =
            Cell(
                0,
                1
            )

        val secondCell =
            Cell(
                0,
                2
            )

        engine.cycleHypothesis(
            firstCell,
            4
        )

        val first =
            engine
                .snapshot()
                .hypothesisAt(
                    firstCell
                )!!

        engine.cycleHypothesis(
            secondCell,
            6
        )

        val second =
            engine
                .snapshot()
                .hypothesisAt(
                    secondCell
                )!!

        assertEquals(
            first.id,
            second.parentId
        )

        assertEquals(
            HypothesisColor.YELLOW,
            first.color
        )

        assertEquals(
            HypothesisColor.GREEN,
            second.color
        )

        assertTrue(
            engine.rewindHypothesis(
                first.id
            )
        )

        assertEquals(
            first.id,
            engine
                .snapshot()
                .hypothesisTrace
                .activeId
        )

        assertEquals(
            null,
            engine
                .snapshot()
                .hypothesisAt(
                    secondCell
                )
        )
    }
}

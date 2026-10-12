package com.greenpower2669.geckodoku

import org.junit.Assert.*
import org.junit.Test

class SudokuVisualReasoningTest {
    @Test fun projectionRequiresTwoNotesInTheSameRow() {
        val notes = mapOf(
            Cell(0, 0) to setOf(1),
            Cell(1, 0) to setOf(1),
            Cell(0, 4) to setOf(1),
            Cell(0, 8) to setOf(1)
        )
        val lines = SudokuVisualReasoning.projectionSegments(notes)
        assertEquals(2, lines.size)
        assertEquals(Cell(0, 0), lines[0].first.cell)
        assertEquals(Cell(0, 4), lines[0].second.cell)
        assertEquals(Cell(0, 8), lines[1].second.cell)
        assertEquals(1, lines[1].first.digit)
    }

    @Test fun pairRequiresSharedUnitAndTwoCells() {
        val notes = mapOf(
            Cell(0, 0) to setOf(1, 3),
            Cell(0, 5) to setOf(1, 3),
            Cell(4, 8) to setOf(1, 3)
        )
        val result = SudokuVisualReasoning.nakedGroups(notes, 2)
        assertTrue(result.any { it.cells == setOf(Cell(0, 0), Cell(0, 5)) })
        assertFalse(result.any { it.cells == setOf(Cell(0, 5), Cell(4, 8)) })
    }

    @Test fun tripletsCanUseThreePairsInOneBlock() {
        val notes = mapOf(
            Cell(0, 0) to setOf(1, 2),
            Cell(1, 1) to setOf(1, 3),
            Cell(2, 2) to setOf(2, 3)
        )
        assertTrue(
            SudokuVisualReasoning.nakedGroups(notes, 3).any {
                it.cells == notes.keys && it.digits == setOf(1, 2, 3)
            }
        )
    }

    @Test fun xWingNeedsTwoExclusiveColumnsOnEachRow() {
        val notes = mutableMapOf(
            Cell(0, 2) to setOf(1), Cell(0, 7) to setOf(1),
            Cell(5, 2) to setOf(1), Cell(5, 7) to setOf(1)
        )
        assertTrue(SudokuVisualReasoning.isGenuineXWing(notes, 1, 0 to 5, 2 to 7))
        notes[Cell(0, 4)] = setOf(1)
        // Pour invalider aussi le X-Wing par colonnes, une troisième
        // ligne doit contenir le même candidat dans la colonne 2.
        notes[Cell(1, 2)] = setOf(1)
        assertFalse(SudokuVisualReasoning.isGenuineXWing(notes, 1, 0 to 5, 2 to 7))
    }
}

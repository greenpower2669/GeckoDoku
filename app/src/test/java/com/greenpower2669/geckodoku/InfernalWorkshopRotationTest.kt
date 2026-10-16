package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InfernalWorkshopRotationTest {
    @Test
    fun visitsEveryGridSizeOnceBeforeRepeating() {
        var next = InfernalWorkshopPolicy.MIN_SIZE
        val visited = mutableListOf<Int>()
        repeat(16) {
            visited.add(next)
            next = InfernalWorkshopPolicy.nextSize(next)
        }
        assertEquals((5..12).toList() + (5..12).toList(), visited)
        assertEquals(5, next)
    }

    @Test
    fun allBoardsHaveOneDistinctColourPerRegion() {
        val colors = (0 until 12).map { GeckoBoardPalette.colorFor(it) }
        assertEquals(12, colors.toSet().size)
        for (size in 5..12) {
            assertEquals(size, colors.take(size).toSet().size)
        }
    }

    @Test
    fun xWingIsNeverRequiredAtAnySizeInThisExperiment() {
        for (size in 5..12) {
            assertTrue(InfernalWorkshopPolicy.qualifies(size, 2, 0))
            assertTrue(InfernalWorkshopPolicy.qualifies(size, 2, 1))
        }
    }

    @Test
    fun aNestedProofNeverBecomesFourByCountingSeparateGuesses() {
        for (size in 5..12) {
            val xWings = 0
            assertFalse(InfernalWorkshopPolicy.qualifies(size, 1, xWings))
            assertTrue(InfernalWorkshopPolicy.qualifies(size, 2, xWings))
            assertTrue(InfernalWorkshopPolicy.qualifies(size, 3, xWings))
            assertTrue(InfernalWorkshopPolicy.qualifies(size, 4, xWings))
            assertFalse(InfernalWorkshopPolicy.qualifies(size, 5, xWings))
        }
    }

    @Test
    fun manualPromotionKeepsOriginalSizeAndProofRequirements() {
        val solution = intArrayOf(0, 2, 4, 1, 3)
        val smallBoard = Puzzle(
            "small",
            5,
            IntArray(25) { it / 5 },
            solution,
            solution.indices.map { Cell(it, solution[it]) }.toSet()
        )
        val raw = WorkshopEntry(
            "dummy",
            smallBoard,
            xWingCount = 0,
            nestedDepth = 4,
            hypothesisCount = 1,
            createdAt = 1L
        )
        assertFalse(raw.eligibleForInfernal)
        assertTrue(raw.copy(status = WorkshopStatus.VALIDATED)
            .eligibleForInfernal)
        assertFalse(raw.copy(status = WorkshopStatus.VALIDATED,
            nestedDepth = 1).eligibleForInfernal)
        val large = raw.copy(
            puzzle = smallBoard.copy(
                size = 8,
                regions = IntArray(64) { it / 8 },
                solutionCols = IntArray(8) { it }
            ),
            status = WorkshopStatus.VALIDATED
        )
        assertTrue(large.eligibleForInfernal)
    }
}

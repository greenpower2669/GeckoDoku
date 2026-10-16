package com.greenpower2669.geckodoku

import java.util.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkshopForwardPathTest {
    private val solution = intArrayOf(0, 2, 4, 1, 3)
    private fun board(): Puzzle = Puzzle(
        id = "forward-five",
        size = 5,
        regions = IntArray(25) { it / 5 },
        solutionCols = solution,
        givens = emptySet()
    )

    @Test
    fun startsEmptyAndAddsExactlyOneGeckoAtATime() {
        val solutionCells = board().solutionCells()
        val states = WorkshopForwardPath.stages(
            solutionCells, Random(2718L)
        )
        assertEquals(6, states.size)
        assertTrue(states.first().isEmpty())
        assertEquals(solutionCells.toSet(), states.last())
        for (index in 1 until states.size) {
            val before = states[index - 1]
            val after = states[index]
            assertEquals(index, after.size)
            assertTrue(after.containsAll(before))
            assertEquals(1, (after - before).size)
            assertTrue(solutionCells.containsAll(after))
        }
    }

    @Test
    fun clueOrderIsReproducibleAndDistinctAcrossSeeds() {
        val cells = board().solutionCells()
        val first = WorkshopForwardPath.stages(cells, Random(1234L))
        val identical = WorkshopForwardPath.stages(cells, Random(1234L))
        assertEquals(first, identical)
        val variations = (1235L..1245L).map {
            WorkshopForwardPath.stages(cells, Random(it))
        }
        assertTrue(variations.any { it != first })
    }

    @Test
    fun firstUniqueBoundaryIsReachedByAddingNotByRemoving() {
        val puzzle = board()
        val states = WorkshopForwardPath.stages(
            puzzle.solutionCells(), Random(7L)
        )
        assertFalse(PuzzleGenerator.hasExactlyOneSolution(
            puzzle.copy(givens = emptySet())
        ))
        val boundary = states.indexOfFirst { givens ->
            PuzzleGenerator.hasExactlyOneSolution(
                puzzle.copy(givens = givens)
            )
        }
        assertTrue("Full valid solution must be unique", boundary in 1..5)
        for (index in 0 until boundary) {
            assertFalse(PuzzleGenerator.hasExactlyOneSolution(
                puzzle.copy(givens = states[index])
            ))
        }
        for (index in boundary until states.size) {
            assertTrue(PuzzleGenerator.hasExactlyOneSolution(
                puzzle.copy(givens = states[index])
            ))
        }
    }

    @Test
    fun smallBoardsStillAllowH2WithoutXWingButLargeRequireIt() {
        for (size in 5..12) {
            assertEquals(size >= 8, InfernalWorkshopPolicy.requiresXWing(size))
            assertEquals(
                size <= 7,
                InfernalWorkshopPolicy.qualifies(size, 2, 0)
            )
            assertTrue(InfernalWorkshopPolicy.qualifies(size, 2, 1))
            assertFalse(InfernalWorkshopPolicy.qualifies(size, 1, 1))
        }
    }
}

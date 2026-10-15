package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InfernalDepthPolicyTest {
    @Test
    fun twoIndependentGuessesNeverQualifyAsInfernal() {
        assertFalse(InfernalDepthPolicy.accepts(true, 0))
        assertFalse(InfernalDepthPolicy.accepts(true, 1))
        assertFalse(InfernalDepthPolicy.accepts(true, 2))
        assertTrue(InfernalDepthPolicy.accepts(true, 3))
        assertTrue(InfernalDepthPolicy.accepts(true, 4))
        assertFalse(InfernalDepthPolicy.accepts(false, 4))
        assertEquals(3, InfernalDepthPolicy.SEARCH_DEPTH)
    }

    @Test
    fun completedBoardNeedsNoHypothesis() {
        val solution = intArrayOf(0, 2, 4, 1, 3)
        val puzzle = Puzzle(
            id = "proof-complete",
            size = 5,
            regions = IntArray(25) { it / 5 },
            solutionCols = solution,
            givens = solution.indices.map {
                Cell(it, solution[it])
            }.toSet()
        )
        val result = HypothesisSolver.analyze(
            puzzle,
            maxDepth = InfernalDepthPolicy.SEARCH_DEPTH,
            maxHypothesisSteps = 5
        )
        assertTrue(result.solved)
        assertEquals(0, result.hypothesisCount)
        assertEquals(0, result.maxHypothesisDepth)
    }

    @Test
    fun unprovedBoardDoesNotQualify() {
        val puzzle = Puzzle(
            id = "proof-unresolved",
            size = 5,
            regions = IntArray(25) { it / 5 },
            solutionCols = intArrayOf(0, 2, 4, 1, 3)
        )
        val result = HypothesisSolver.analyze(
            puzzle,
            maxDepth = 0,
            maxHypothesisSteps = 0
        )
        assertFalse(result.solved)
        assertFalse(InfernalDepthPolicy.accepts(
            result.solved, result.maxHypothesisDepth
        ))
    }
}

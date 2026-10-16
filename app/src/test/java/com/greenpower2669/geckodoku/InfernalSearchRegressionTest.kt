package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression tests for bounded search and sound triple-depth classification. */
class InfernalSearchRegressionTest {
    private fun puzzle(): Puzzle {
        val solution = intArrayOf(0, 2, 4, 1, 3)
        return Puzzle(
            id = "infernal-search-contract",
            size = 5,
            regions = IntArray(25) { it / 5 },
            solutionCols = solution,
            givens = emptySet()
        )
    }

    @Test
    fun immediateCancellationStopsExactGeneratorWithoutFallback() {
        var checks = 0
        val grid = PuzzleGenerator.generateExact(
            size = 5,
            requested = GameDifficulty.INFERNAL,
            seed = 101L,
            shouldCancel = {
                checks++
                true
            }
        )
        assertNull(grid)
        assertTrue(checks > 0)
    }

    @Test
    fun cancellationPreventsAFalseSolvedClassification() {
        val result = HypothesisSolver.analyze(
            puzzle = puzzle(),
            maxDepth = 3,
            maxHypothesisSteps = 5,
            shouldCancel = { true }
        )
        assertFalse(result.solved)
        assertFalse(
            InfernalDepthPolicy.accepts(
                result.solved, result.maxHypothesisDepth
            )
        )
    }

    @Test
    fun cancelledNestedProofNeverBecomesAForcedPlacement() {
        val step = HypothesisSolver.nextHypothesisStep(
            puzzle = puzzle(),
            confirmed = emptySet(),
            excluded = emptySet(),
            maxDepth = 3,
            shouldCancel = { true }
        )
        assertNull(step)
    }

    @Test
    fun completedGridStillNeedsNoHypothesisOrBacktracking() {
        val complete = puzzle().copy(
            givens = puzzle().solutionCells().toSet()
        )
        val solved = HypothesisSolver.analyze(
            puzzle = complete,
            maxDepth = 3,
            maxHypothesisSteps = 5
        )
        assertTrue(solved.solved)
        assertTrue(solved.maxHypothesisDepth == 0)
        assertFalse(InfernalDepthPolicy.accepts(
            solved.solved, solved.maxHypothesisDepth
        ))
    }
}

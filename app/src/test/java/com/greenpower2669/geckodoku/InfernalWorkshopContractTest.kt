package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InfernalWorkshopContractTest {
    private val solution = intArrayOf(0, 2, 4, 1, 3)
    private fun puzzle() = Puzzle(
        id = "workshop-test",
        size = 5,
        regions = IntArray(25) { it / 5 },
        solutionCols = solution,
        givens = solution.indices.map { Cell(it, solution[it]) }.toSet()
    )

    @Test
    fun uniqueFullBoardIsVerifiedWithoutHypothesisSolver() {
        assertTrue(PuzzleGenerator.hasExactlyOneSolution(puzzle()))
        assertFalse(PuzzleGenerator.hasExactlyOneSolution(
            puzzle().copy(solutionCols = intArrayOf(0, 0, 4, 1, 3))
        ))
        assertFalse(PuzzleGenerator.hasExactlyOneSolution(
            puzzle().copy(givens = setOf(Cell(0, 1)))
        ))
    }

    @Test
    fun candidateCannotBePromotedByGeneratorAutomatically() {
        val candidate = WorkshopEntry(
            id = "not-a-real-fingerprint",
            puzzle = puzzle(),
            xWingCount = 1,
            nestedDepth = 2,
            hypothesisCount = 1,
            createdAt = 100L
        )
        assertFalse(candidate.eligibleForInfernal)
        assertFalse(candidate.copy(status = WorkshopStatus.TESTING)
            .eligibleForInfernal)
        assertFalse(candidate.copy(status = WorkshopStatus.REJECTED)
            .eligibleForInfernal)
        assertTrue(candidate.copy(status = WorkshopStatus.VALIDATED)
            .eligibleForInfernal)
    }

    @Test
    fun promotionKeepsProofDepthRatherThanClaimingThree() {
        val checked = WorkshopEntry(
            id = "again-not-a-fingerprint",
            puzzle = puzzle(),
            xWingCount = 1,
            nestedDepth = 2,
            hypothesisCount = 2,
            createdAt = 100L,
            status = WorkshopStatus.VALIDATED
        )
        assertTrue(checked.eligibleForInfernal)
        assertTrue(checked.nestedDepth == 2)
        // All sizes are now allowed without X-Wing for this experiment.
        assertTrue(checked.copy(xWingCount = 0).eligibleForInfernal)
        // Keep structural invariants: 12 rows and 144 region cells.
        val largeBoard = checked.puzzle.copy(
            size = 12,
            regions = IntArray(144) { it / 12 },
            solutionCols = IntArray(12) { it },
            givens = emptySet()
        )
        assertTrue(checked.copy(
            xWingCount = 0,
            puzzle = largeBoard
        ).eligibleForInfernal)
    }

    @Test
    fun noXWingSolverStillRecognizesFullyCompletedBoard() {
        val result = HypothesisSolver.analyze(
            puzzle = puzzle(),
            maxDepth = 2,
            maxHypothesisSteps = 5,
            rules = SolverRules.NO_X_WING
        )
        assertTrue(result.solved)
        assertTrue(result.maxHypothesisDepth == 0)
    }
}

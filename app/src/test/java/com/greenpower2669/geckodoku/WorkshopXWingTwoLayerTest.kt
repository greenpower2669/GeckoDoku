package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkshopXWingTwoLayerTest {
    @Test fun xwingIsAProspectNotAValidatedInfernalProof() {
        val step = SolveStep(
            technique = SolveTechnique.GECKO_X_WING,
            sourceCells = setOf(Cell(0, 0), Cell(0, 2), Cell(2, 0), Cell(2, 2)),
            eliminated = setOf(Cell(3, 0)),
            axis = "lignes 1 et 3"
        )
        assertTrue(WorkshopXWingTwoLayer.isProspect(step))
        val candidate = WorkshopEntry(
            id = "not-promoted",
            puzzle = Puzzle(
                id = "test", size = 5, regions = IntArray(25) { it / 5 },
                solutionCols = intArrayOf(0, 2, 4, 1, 3)
            ),
            xWingCount = 1, nestedDepth = 2,
            hypothesisCount = 1, createdAt = 1L
        )
        assertFalse(candidate.eligibleForInfernal)
        assertFalse(WorkshopXWingTwoLayer.isProspect(step.copy(eliminated = emptySet())))
        assertFalse(WorkshopXWingTwoLayer.isProspect(
            step.copy(technique = SolveTechnique.REGION_LOCKED)
        ))
    }

    @Test fun uniqueFiveByFiveCanBeObservedBeforeDeepProof() {
        // Complete legal solution. The first two given Gecko have a
        // unique completion, and leave an observable column X-Wing.
        val cols = intArrayOf(0, 2, 4, 1, 3)
        val puzzle = Puzzle(
            id = "two-layer-observation",
            size = 5,
            regions = IntArray(25) { it / 5 },
            solutionCols = cols,
            givens = setOf(Cell(0, 0), Cell(1, 2))
        )
        assertTrue(PuzzleGenerator.hasExactlyOneSolution(puzzle))
        val detection = HumanSolver.firstObservableGeckoXWing(puzzle)
        assertTrue("A real eliminable X-Wing must be found",
            WorkshopXWingTwoLayer.isProspect(detection))
        assertTrue(detection!!.eliminated.isNotEmpty())
    }

    @Test fun interruptedProofKeepsProspectPending() {
        assertEquals(
            WorkshopXWingStatus.PENDING,
            WorkshopXWingTwoLayer.afterProof(
                WorkshopXWingStatus.PENDING, interrupted = true, accepted = false
            )
        )
        assertEquals(
            WorkshopXWingStatus.PENDING,
            WorkshopXWingTwoLayer.afterProof(
                WorkshopXWingStatus.PENDING, interrupted = true, accepted = true
            )
        )
    }

    @Test fun completeProofCanClassifyButNeverValidateAutomatically() {
        assertEquals(
            WorkshopXWingStatus.EXPLORED,
            WorkshopXWingTwoLayer.afterProof(
                WorkshopXWingStatus.PENDING, interrupted = false, accepted = false
            )
        )
        assertEquals(
            WorkshopXWingStatus.PROMISING,
            WorkshopXWingTwoLayer.afterProof(
                WorkshopXWingStatus.PENDING, interrupted = false, accepted = true
            )
        )
    }
}

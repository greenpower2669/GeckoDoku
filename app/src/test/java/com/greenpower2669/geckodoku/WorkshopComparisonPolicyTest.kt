package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkshopComparisonPolicyTest {
    private val xWing = SolveStep(
        technique = SolveTechnique.GECKO_X_WING,
        eliminated = setOf(Cell(2, 0)),
        sourceCells = setOf(Cell(0, 0), Cell(0, 2), Cell(3, 0), Cell(3, 2))
    )
    private val h1 = SolveStep(
        technique = SolveTechnique.HYPOTHESIS_TEST,
        hypothesisDepth = 1
    )
    private val h2 = SolveStep(
        technique = SolveTechnique.DOUBLE_HYPOTHESIS,
        hypothesisDepth = 2
    )

    @Test fun h1XWingNeedsExactlyOneHypothesisAndRealXWingProof() {
        val accepted = HypothesisAnalysis(
            solved = true,
            steps = listOf(xWing, h1),
            hypothesisCount = 1,
            maxHypothesisDepth = 1
        )
        assertTrue(WorkshopComparisonPolicy.qualifies(
            WorkshopComparisonLane.H1_XWING, accepted
        ))
        assertFalse(WorkshopComparisonPolicy.qualifies(
            WorkshopComparisonLane.H1_XWING,
            accepted.copy(steps = listOf(h1))
        ))
        assertFalse(WorkshopComparisonPolicy.qualifies(
            WorkshopComparisonLane.H1_XWING,
            accepted.copy(hypothesisCount = 2)
        ))
        assertFalse(WorkshopComparisonPolicy.qualifies(
            WorkshopComparisonLane.H1_XWING,
            accepted.copy(solved = false)
        ))
        assertFalse(WorkshopComparisonPolicy.qualifies(
            WorkshopComparisonLane.H1_XWING,
            accepted.copy(exhaustedBudget = true)
        ))
    }

    @Test fun h2WithoutXWingMustBeGenuinelyNestedAndNeedTwoLevels() {
        val solved = HypothesisAnalysis(
            solved = true,
            steps = listOf(h2),
            hypothesisCount = 1,
            maxHypothesisDepth = 2
        )
        val shallow = HypothesisAnalysis(
            solved = false, steps = emptyList(),
            hypothesisCount = 0, maxHypothesisDepth = 0
        )
        assertTrue(WorkshopComparisonPolicy.qualifies(
            WorkshopComparisonLane.H2_NO_XWING, solved, shallow
        ))
        assertFalse(WorkshopComparisonPolicy.qualifies(
            WorkshopComparisonLane.H2_NO_XWING,
            solved.copy(steps = listOf(h2, xWing)), shallow
        ))
        assertFalse(WorkshopComparisonPolicy.qualifies(
            WorkshopComparisonLane.H2_NO_XWING,
            solved, shallow.copy(solved = true)
        ))
        assertFalse(WorkshopComparisonPolicy.qualifies(
            WorkshopComparisonLane.H2_NO_XWING,
            solved, shallow.copy(exhaustedBudget = true)
        ))
        assertFalse(WorkshopComparisonPolicy.qualifies(
            WorkshopComparisonLane.H2_NO_XWING,
            solved, null
        ))
        assertFalse(WorkshopComparisonPolicy.qualifies(
            WorkshopComparisonLane.H2_NO_XWING,
            solved.copy(maxHypothesisDepth = 1), shallow
        ))
    }

    @Test fun noExperimentIsEverInfernalValidation() {
        val puzzle = Puzzle(
            id = "exp", size = 5, regions = IntArray(25) { it / 5 },
            solutionCols = intArrayOf(0, 2, 4, 1, 3)
        )
        val low = WorkshopEntry(
            id = "not-a-fingerprint", puzzle = puzzle,
            xWingCount = 1, nestedDepth = 1, hypothesisCount = 1,
            createdAt = 0, status = WorkshopStatus.VALIDATED
        )
        assertFalse(low.eligibleForInfernal)
    }
}

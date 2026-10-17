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
        assertFalse(WorkshopXWingTwoLayer.isPromotedAutomatically(step))
        assertFalse(WorkshopXWingTwoLayer.isProspect(step.copy(eliminated = emptySet())))
        assertFalse(WorkshopXWingTwoLayer.isProspect(
            step.copy(technique = SolveTechnique.REGION_LOCKED)
        ))
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

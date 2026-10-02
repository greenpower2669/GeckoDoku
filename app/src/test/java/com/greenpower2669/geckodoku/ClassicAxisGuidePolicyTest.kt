package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassicAxisGuidePolicyTest {
    @Test
    fun rowLockedStepProducesHorizontalGuide() {
        val step =
            SolveStep(
                technique =
                    SolveTechnique
                        .REGION_LOCKED,
                sourceCells =
                    setOf(
                        Cell(2, 1),
                        Cell(2, 4)
                    ),
                eliminated =
                    setOf(
                        Cell(2, 6)
                    ),
                axis =
                    "ligne 3"
            )

        assertEquals(
            setOf(
                ClassicAxisGuide(
                    ClassicAxisGuideKind
                        .HORIZONTAL,
                    2
                )
            ),
            ClassicProfessorAxisGuidePolicy
                .forStep(step)
        )
    }

    @Test
    fun xWingShowsReservedColumnsWhenEliminatingDownColumns() {
        val step =
            SolveStep(
                technique =
                    SolveTechnique
                        .GECKO_X_WING,
                sourceCells =
                    setOf(
                        Cell(1, 2),
                        Cell(1, 5),
                        Cell(4, 2),
                        Cell(4, 5)
                    ),
                eliminated =
                    setOf(
                        Cell(0, 2),
                        Cell(3, 5)
                    )
            )

        val guides =
            ClassicProfessorAxisGuidePolicy
                .forStep(step)

        assertEquals(
            2,
            guides.size
        )

        assertTrue(
            ClassicAxisGuide(
                ClassicAxisGuideKind
                    .VERTICAL,
                2
            ) in guides
        )

        assertTrue(
            ClassicAxisGuide(
                ClassicAxisGuideKind
                    .VERTICAL,
                5
            ) in guides
        )
    }
}

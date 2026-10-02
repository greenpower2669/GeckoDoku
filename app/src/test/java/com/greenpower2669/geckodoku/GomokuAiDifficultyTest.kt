package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GomokuAiDifficultyTest {
    @Test
    fun harderDifficultyLooksFurtherAndIsMoreReliable() {
        val discovery =
            GomokuDifficultyPolicy
                .profile(
                    GameDifficulty
                        .DISCOVERY
                )

        val infernal =
            GomokuDifficultyPolicy
                .profile(
                    GameDifficulty
                        .INFERNAL
                )

        assertTrue(
            infernal.searchDepth >
                discovery.searchDepth
        )
        assertTrue(
            infernal.beamWidth >=
                discovery.beamWidth
        )
        assertTrue(
            infernal.trapAware
        )
        assertTrue(
            infernal
                .tacticalReliabilityPercent >
                discovery
                    .tacticalReliabilityPercent
        )
        assertEquals(
            1,
            infernal.choiceWindow
        )
        assertTrue(
            discovery.choiceWindow >
                infernal.choiceWindow
        )
    }

    @Test
    fun infernalTakesImmediateWinBeforeLongPlan() {
        val state =
            GomokuSnapshotBuilder(
                size = 15
            )
                .put(
                    GomokuPlayer.PROFESSOR,
                    Cell(7, 3),
                    Cell(7, 4),
                    Cell(7, 5),
                    Cell(7, 6)
                )
                .put(
                    GomokuPlayer.PLAYER,
                    Cell(2, 2),
                    Cell(3, 3)
                )
                .build(
                    currentPlayer =
                        GomokuPlayer.PROFESSOR
                )

        val decision =
            GomokuAi.chooseMove(
                state,
                GameDifficulty.INFERNAL
            )

        assertEquals(
            Cell(7, 2),
            decision?.cell
        )
        assertEquals(
            GomokuReasonKind.WIN,
            decision
                ?.reasoning
                ?.kind
        )
        assertTrue(
            decision?.reason
                ?.contains(
                    "victoire",
                    ignoreCase = true
                ) ==
                true
        )
    }

    @Test
    fun discoveryCanMissImmediateThreatWhileInfernalBlocksIt() {
        val state =
            GomokuSnapshotBuilder(
                size = 15
            )
                .put(
                    GomokuPlayer.PLAYER,
                    Cell(7, 3),
                    Cell(7, 4),
                    Cell(7, 5),
                    Cell(7, 6)
                )
                .build(
                    currentPlayer =
                        GomokuPlayer.PROFESSOR
                )

        val discovery =
            GomokuAi.chooseMove(
                state,
                GameDifficulty.DISCOVERY
            )

        val infernal =
            GomokuAi.chooseMove(
                state,
                GameDifficulty.INFERNAL
            )

        val forcedBlocks =
            setOf(
                Cell(7, 2),
                Cell(7, 7)
            )

        assertFalse(
            discovery?.cell in
                forcedBlocks
        )

        assertTrue(
            infernal?.cell in
                forcedBlocks
        )

        assertEquals(
            GomokuReasonKind.DEFENSE,
            infernal
                ?.reasoning
                ?.kind
        )
    }

    @Test
    fun requestedReasoningNamesConcreteBoardFeatures() {
        val state =
            GomokuSnapshotBuilder(
                size = 15
            )
                .put(
                    GomokuPlayer.PLAYER,
                    Cell(7, 3),
                    Cell(7, 4),
                    Cell(7, 5),
                    Cell(7, 6)
                )
                .build(
                    currentPlayer =
                        GomokuPlayer.PROFESSOR
                )

        val decision =
            requireNotNull(
                GomokuAi.chooseMove(
                    state,
                    GameDifficulty.EXPERT
                )
            )

        assertTrue(
            decision.reason
                .contains(
                    "ligne",
                    ignoreCase = true
                )
        )

        assertTrue(
            decision.reason
                .contains(
                    "colonne",
                    ignoreCase = true
                )
        )

        assertTrue(
            decision.reasoning
                .lineCells
                .isNotEmpty()
        )

        assertTrue(
            decision.reasoning
                .steps
                .isNotEmpty()
        )
    }
}

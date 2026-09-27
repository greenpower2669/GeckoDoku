package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GomokuMatchPolicyTest {
    @Test
    fun humanVsHumanNeverGivesProfessorAutomaticTurn() {
        val engine =
            GomokuGameEngine(
                size = 15
            )

        engine.play(
            Cell(7, 7)
        )

        val snapshot =
            engine.snapshot()

        assertEquals(
            GomokuPlayer.PROFESSOR,
            snapshot.currentPlayer
        )

        assertFalse(
            GomokuMatchPolicy
                .professorControlsCurrentTurn(
                    GomokuMatchMode
                        .HUMAN_VS_HUMAN,
                    snapshot
                )
        )

        assertTrue(
            GomokuMatchPolicy
                .professorCanAdvise(
                    GomokuMatchMode
                        .HUMAN_VS_HUMAN,
                    snapshot,
                    thinking = false
                )
        )
    }

    @Test
    fun versusProfessorOwnsYellowTurnAndLongPressCanPlayGreen() {
        val start =
            GomokuGameEngine(
                size = 15
            ).snapshot()

        assertTrue(
            GomokuMatchPolicy
                .longPressAppliesHumanMove(
                    GomokuMatchMode
                        .VS_PROFESSOR,
                    start
                )
        )

        val engine =
            GomokuGameEngine(
                size = 15
            )

        engine.play(
            Cell(7, 7)
        )

        assertTrue(
            GomokuMatchPolicy
                .professorControlsCurrentTurn(
                    GomokuMatchMode
                        .VS_PROFESSOR,
                    engine.snapshot()
                )
        )
    }

    @Test
    fun aiCanAnalyzeCurrentGreenPlayerPerspective() {
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
                .put(
                    GomokuPlayer.PROFESSOR,
                    Cell(2, 2)
                )
                .build(
                    currentPlayer =
                        GomokuPlayer.PLAYER
                )

        val decision =
            GomokuAi.chooseMoveFor(
                snapshot = state,
                difficulty =
                    GameDifficulty.EASY,
                player =
                    GomokuPlayer.PLAYER
            )

        assertNotNull(
            decision
        )
        assertEquals(
            Cell(7, 2),
            decision?.cell
        )
    }
}

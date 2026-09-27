package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GomokuAiDifficultyTest {
    @Test
    fun harderDifficultyLooksFurtherAndUsesTraps() {
        val easy =
            GomokuDifficultyPolicy
                .profile(
                    GameDifficulty.EASY
                )

        val infernal =
            GomokuDifficultyPolicy
                .profile(
                    GameDifficulty.INFERNAL
                )

        assertTrue(
            infernal.searchDepth >
                easy.searchDepth
        )
        assertTrue(
            infernal.beamWidth >=
                easy.beamWidth
        )
        assertTrue(
            infernal.trapAware
        )
    }

    @Test
    fun professorTakesImmediateWinBeforeLongPlan() {
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
        assertTrue(
            decision?.reason
                ?.contains(
                    "gagner",
                    ignoreCase = true
                ) == true
        )
    }
}

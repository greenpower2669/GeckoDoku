package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GomokuEngineTest {
    @Test
    fun detectsHorizontalFiveAndLocksGame() {
        val engine =
            GomokuGameEngine(
                size = 15
            )

        for (col in 0..3) {
            assertEquals(
                GomokuMoveResult.PLACED,
                engine.play(
                    Cell(7, col)
                )
            )
            assertEquals(
                GomokuMoveResult.PLACED,
                engine.play(
                    Cell(0, col)
                )
            )
        }

        assertEquals(
            GomokuMoveResult.WIN,
            engine.play(
                Cell(7, 4)
            )
        )

        val snapshot =
            engine.snapshot()

        assertEquals(
            GomokuPlayer.PLAYER,
            snapshot.winner
        )
        assertTrue(
            snapshot.winningLine
                .size >= 5
        )

        assertEquals(
            GomokuMoveResult.GAME_OVER,
            engine.play(
                Cell(10, 10)
            )
        )
    }

    @Test
    fun occupiedPositionDoesNotChangeTurn() {
        val engine =
            GomokuGameEngine(
                size = 15
            )

        engine.play(
            Cell(4, 4)
        )

        val before =
            engine.snapshot()
                .currentPlayer

        assertEquals(
            GomokuMoveResult.OCCUPIED,
            engine.play(
                Cell(4, 4)
            )
        )

        assertEquals(
            before,
            engine.snapshot()
                .currentPlayer
        )
    }
}

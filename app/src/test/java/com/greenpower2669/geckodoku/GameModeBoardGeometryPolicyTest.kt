package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class GameModeBoardGeometryPolicyTest {
    @Test
    fun eachGameModeFreezesItsOwnGeometry() {
        val policy =
            GameModeBoardGeometryPolicy()

        val gecko =
            BoardGeometry(
                left = 10,
                top = 100,
                width = 700,
                height = 700
            )

        val sudoku =
            BoardGeometry(
                left = 10,
                top = 100,
                width = 700,
                height = 430
            )

        assertEquals(
            gecko,
            policy.resolve(
                mode = GameMode.GECKODOKU,
                windowWidth = 720,
                windowHeight = 1500,
                proposed = gecko
            )
        )

        assertEquals(
            sudoku,
            policy.resolve(
                mode = GameMode.SUDOKU,
                windowWidth = 720,
                windowHeight = 1500,
                proposed = sudoku
            )
        )

        assertNotEquals(
            policy.resolve(
                mode = GameMode.GECKODOKU,
                windowWidth = 720,
                windowHeight = 1500,
                proposed = sudoku
            ),
            policy.resolve(
                mode = GameMode.SUDOKU,
                windowWidth = 720,
                windowHeight = 1500,
                proposed = gecko
            )
        )
    }
}

package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuGeneratorTest {
    @Test
    fun generatorCreatesAValidUniqueNineByNinePuzzle() {
        val puzzle =
            SudokuGenerator.generate(
                difficulty = GameDifficulty.THINKING,
                seed = 20260927L
            )

        assertEquals(81, puzzle.solution.size)
        assertEquals(81, puzzle.givens.size)
        assertTrue(
            SudokuSolver.isCompleteValidGrid(
                puzzle.solution
            )
        )

        for (i in 0 until 81) {
            if (puzzle.givens[i] != 0) {
                assertEquals(
                    puzzle.solution[i],
                    puzzle.givens[i]
                )
            }
        }

        assertEquals(
            1,
            SudokuSolver.countSolutions(
                puzzle.givens,
                limit = 2
            )
        )
    }
}

package com.greenpower2669.geckodoku

import kotlin.random.Random

object SudokuGenerator {
    fun generate(
        difficulty: GameDifficulty,
        seed: Long =
            Random.nextLong()
    ): SudokuPuzzle {
        val random =
            Random(seed)

        val solution =
            shuffledSolution(
                random
            )

        val givens =
            solution.copyOf()

        val target =
            targetGivens(
                difficulty
            )

        val positions =
            (0 until
                SudokuPuzzle.CELL_COUNT)
                .shuffled(random)

        for (index in positions) {
            if (
                givens.count {
                    it != 0
                } <= target
            ) {
                break
            }

            val old =
                givens[index]

            givens[index] = 0

            if (
                SudokuSolver
                    .countSolutions(
                        givens,
                        limit = 2
                    ) != 1
            ) {
                givens[index] =
                    old
            }
        }

        return SudokuPuzzle(
            solution = solution,
            givens = givens,
            difficulty = difficulty,
            seed = seed
        )
    }

    fun targetGivens(
        difficulty: GameDifficulty
    ): Int =
        when (difficulty) {
            GameDifficulty.DISCOVERY ->
                46

            GameDifficulty.EASY ->
                40

            GameDifficulty.THINKING ->
                35

            GameDifficulty.HARD ->
                31

            GameDifficulty.EXPERT ->
                28

            GameDifficulty.DEMENTIAL ->
                26

            GameDifficulty
                .MISSION_IMPOSSIBLE ->
                24

            GameDifficulty.INFERNAL ->
                22
        }

    private fun shuffledSolution(
        random: Random
    ): IntArray {
        val digits =
            (1..9).shuffled(
                random
            )

        val rows =
            shuffledGroups(
                random
            )

        val cols =
            shuffledGroups(
                random
            )

        return IntArray(
            SudokuPuzzle.CELL_COUNT
        ) {
            index ->

            val row =
                rows[index / 9]

            val col =
                cols[index % 9]

            val baseDigit =
                (
                    row * 3 +
                        row / 3 +
                        col
                    ) % 9

            digits[baseDigit]
        }
    }

    private fun shuffledGroups(
        random: Random
    ): List<Int> =
        (0..2)
            .shuffled(random)
            .flatMap {
                group ->

                (0..2)
                    .shuffled(random)
                    .map {
                        within ->

                        group * 3 +
                            within
                    }
            }
}

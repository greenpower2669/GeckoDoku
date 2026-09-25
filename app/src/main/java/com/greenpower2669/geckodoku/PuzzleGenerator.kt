package com.greenpower2669.geckodoku

import java.util.Random
import kotlin.math.abs

object PuzzleGenerator {
    private data class Candidate(
        val puzzle: Puzzle,
        val distance: Int
    )

    fun generate(
        size: Int,
        requested: GameDifficulty,
        seed: Long = System.nanoTime()
    ): Puzzle {
        require(size in 5..12)

        val random = Random(seed)
        var best: Candidate? = null

        val attempts = when {
            size <= 7 -> 140
            size <= 9 -> 90
            else -> 55
        }

        repeat(attempts) { attempt ->
            val solution =
                randomSolution(size, random) ?: return@repeat

            val regions =
                growRegions(size, solution, random)

            val base = Puzzle(
                id = "g-" +
                    size + "-" +
                    requested.name.lowercase() + "-" +
                    seed + "-" +
                    attempt,
                size = size,
                regions = regions,
                solutionCols = solution,
                difficulty = requested,
                seed = seed
            )

            val tuned =
                tuneGivens(base, requested, random)

            val report =
                DifficultyIndexer.analyze(tuned)

            if (!report.logicallySolvable) {
                return@repeat
            }

            if (countSolutions(tuned, 2) != 1) {
                return@repeat
            }

            val ratedPuzzle = attachTrace(
                tuned.copy(
                    difficulty = report.ratedDifficulty
                )
            )

            if (report.ratedDifficulty == requested &&
                meetsProfile(report, requested)
            ) {
                return ratedPuzzle
            }

            val distance =
                difficultyDistance(report, requested)

            if (best == null ||
                distance < best!!.distance
            ) {
                best = Candidate(
                    ratedPuzzle,
                    distance
                )
            }
        }

        return best?.puzzle
            ?: attachTrace(
                safeFallback(
                    size,
                    requested,
                    seed
                )
            )
    }

    private fun attachTrace(
        puzzle: Puzzle
    ): Puzzle {
        val steps =
            when (puzzle.difficulty) {
                GameDifficulty.MISSION_IMPOSSIBLE ->
                    HypothesisSolver.analyze(
                        puzzle,
                        maxDepth = 1,
                        maxHypothesisSteps = 1
                    ).steps

                GameDifficulty.INFERNAL ->
                    HypothesisSolver.analyze(
                        puzzle,
                        maxDepth = 2,
                        maxHypothesisSteps = 2
                    ).steps

                else ->
                    HumanSolver.analyze(
                        puzzle = puzzle,
                        givens = puzzle.givens,
                        rules = SolverRules.FULL
                    ).steps
            }

        return puzzle.copy(
            solverTrace = steps
        )
    }

    private fun tuneGivens(
        base: Puzzle,
        requested: GameDifficulty,
        random: Random
    ): Puzzle {
        var best = base.copy(
            givens = base.solutionCells().toSet()
        )

        var bestDistance = Int.MAX_VALUE

        repeat(8) {
            val chosen =
                base.solutionCells().toMutableSet()

            val order =
                base.solutionCells().shuffled(random)

            for (cell in order) {
                if (chosen.size <=
                    requested.minimumGivens(base.size)
                ) {
                    break
                }

                chosen.remove(cell)

                val probe =
                    base.copy(givens = chosen.toSet())

                if (countSolutions(probe, 2) != 1) {
                    chosen.add(cell)
                    continue
                }

                val report =
                    DifficultyIndexer.analyze(probe)

                if (!report.logicallySolvable) {
                    chosen.add(cell)
                    continue
                }

                val distance =
                    difficultyDistance(
                        report,
                        requested
                    )

                if (distance < bestDistance) {
                    bestDistance = distance
                    best = probe
                }

                if (report.ratedDifficulty ==
                    requested &&
                    meetsProfile(
                        report,
                        requested
                    )
                ) {
                    return probe
                }

                if (report.ratedDifficulty.ordinal >
                    requested.ordinal + 1
                ) {
                    chosen.add(cell)
                }
            }
        }

        return best
    }

    private fun meetsProfile(
        report: DifficultyReport,
        requested: GameDifficulty
    ): Boolean {
        val f = report.features

        return when (requested) {
            GameDifficulty.DISCOVERY ->
                report.ratedDifficulty ==
                    GameDifficulty.DISCOVERY

            GameDifficulty.EASY ->
                report.ratedDifficulty ==
                    GameDifficulty.EASY &&
                    f.regionLogicCount >= 1

            GameDifficulty.THINKING ->
                report.ratedDifficulty ==
                    GameDifficulty.THINKING &&
                    f.regionLogicCount in 2..3

            GameDifficulty.HARD ->
                report.ratedDifficulty ==
                    GameDifficulty.HARD &&
                    f.regionLogicCount >= 4

            GameDifficulty.EXPERT ->
                report.ratedDifficulty ==
                    GameDifficulty.EXPERT &&
                    f.xWingRequired

            GameDifficulty.DEMENTIAL ->
                report.ratedDifficulty ==
                    GameDifficulty.DEMENTIAL &&
                    f.xWingRequired &&
                    f.projectionRequired

            GameDifficulty.MISSION_IMPOSSIBLE ->
                report.ratedDifficulty ==
                    GameDifficulty.MISSION_IMPOSSIBLE &&
                    f.hypothesisCount == 1 &&
                    f.hypothesisDepth <= 1

            GameDifficulty.INFERNAL ->
                report.ratedDifficulty ==
                    GameDifficulty.INFERNAL &&
                    (
                        f.hypothesisCount >= 2 ||
                        f.hypothesisDepth >= 2
                    )
        }
    }

    private fun difficultyDistance(
        report: DifficultyReport,
        requested: GameDifficulty
    ): Int {
        var d =
            abs(
                report.ratedDifficulty.ordinal -
                    requested.ordinal
            ) * 100

        val f = report.features

        d += when (requested) {
            GameDifficulty.DISCOVERY ->
                f.regionLogicCount * 8 +
                    f.xWingCount * 20

            GameDifficulty.EASY ->
                abs(f.regionLogicCount - 1) * 8

            GameDifficulty.THINKING ->
                when {
                    f.regionLogicCount < 2 ->
                        (2 - f.regionLogicCount) * 8

                    f.regionLogicCount > 3 ->
                        (f.regionLogicCount - 3) * 8

                    else -> 0
                }

            GameDifficulty.HARD ->
                if (f.regionLogicCount >= 4) {
                    0
                } else {
                    (4 - f.regionLogicCount) * 8
                }

            GameDifficulty.EXPERT ->
                if (f.xWingRequired) 0 else 35

            GameDifficulty.DEMENTIAL ->
                (if (f.xWingRequired) 0 else 35) +
                    (if (f.projectionRequired) 0 else 35)

            GameDifficulty.MISSION_IMPOSSIBLE ->
                if (
                    f.hypothesisCount == 1 &&
                    f.hypothesisDepth <= 1
                ) {
                    0
                } else {
                    45 +
                        kotlin.math.abs(
                            f.hypothesisCount - 1
                        ) * 10
                }

            GameDifficulty.INFERNAL ->
                if (
                    f.hypothesisCount >= 2 ||
                    f.hypothesisDepth >= 2
                ) {
                    0
                } else {
                    55
                }
        }

        return d
    }

    private fun randomSolution(
        size: Int,
        random: Random
    ): IntArray? {
        val current = IntArray(size) { -1 }
        val used = BooleanArray(size)

        fun build(row: Int): Boolean {
            if (row == size) {
                return true
            }

            val cols =
                (0 until size).shuffled(random)

            for (col in cols) {
                if (used[col]) continue

                if (row > 0 &&
                    abs(
                        current[row - 1] -
                            col
                    ) <= 1
                ) {
                    continue
                }

                current[row] = col
                used[col] = true

                if (build(row + 1)) {
                    return true
                }

                used[col] = false
                current[row] = -1
            }

            return false
        }

        return if (build(0)) {
            current.copyOf()
        } else {
            null
        }
    }

    private fun growRegions(
        size: Int,
        solution: IntArray,
        random: Random
    ): IntArray {
        val regions =
            IntArray(size * size) { -1 }

        val unassigned =
            mutableSetOf<Int>()

        for (i in regions.indices) {
            unassigned.add(i)
        }

        for (r in 0 until size) {
            val idx =
                r * size + solution[r]

            regions[idx] = r
            unassigned.remove(idx)
        }

        while (unassigned.isNotEmpty()) {
            val frontier =
                mutableListOf<
                    Pair<Int, List<Int>>
                >()

            for (idx in unassigned) {
                val r = idx / size
                val c = idx % size

                val neighborRegions =
                    linkedSetOf<Int>()

                val neighbors = arrayOf(
                    r - 1 to c,
                    r + 1 to c,
                    r to c - 1,
                    r to c + 1
                )

                for ((rr, cc) in neighbors) {
                    if (rr !in 0 until size ||
                        cc !in 0 until size
                    ) {
                        continue
                    }

                    val region =
                        regions[
                            rr * size + cc
                        ]

                    if (region >= 0) {
                        neighborRegions.add(region)
                    }
                }

                if (neighborRegions.isNotEmpty()) {
                    frontier.add(
                        idx to
                            neighborRegions.toList()
                    )
                }
            }

            if (frontier.isEmpty()) {
                break
            }

            val pair =
                frontier[
                    random.nextInt(
                        frontier.size
                    )
                ]

            val idx = pair.first
            val choices = pair.second

            regions[idx] =
                choices[
                    random.nextInt(
                        choices.size
                    )
                ]

            unassigned.remove(idx)
        }

        return regions
    }

    private fun countSolutions(
        puzzle: Puzzle,
        limit: Int
    ): Int {
        val size = puzzle.size
        val usedCols = BooleanArray(size)
        val usedRegions = BooleanArray(size)

        val rowsFixed =
            IntArray(size) { -1 }

        for (given in puzzle.givens) {
            rowsFixed[given.row] =
                given.col
        }

        var count = 0

        val chosen =
            IntArray(size) { -1 }

        fun search(row: Int) {
            if (count >= limit) return

            if (row == size) {
                count++
                return
            }

            val fixed =
                rowsFixed[row]

            val cols: IntRange =
                if (fixed >= 0) {
                    fixed..fixed
                } else {
                    0 until size
                }

            for (col in cols) {
                if (usedCols[col]) continue

                if (row > 0 &&
                    chosen[row - 1] >= 0 &&
                    abs(
                        chosen[row - 1] -
                            col
                    ) <= 1
                ) {
                    continue
                }

                val region =
                    puzzle.regions[
                        row * size + col
                    ]

                if (usedRegions[region]) {
                    continue
                }

                usedCols[col] = true
                usedRegions[region] = true
                chosen[row] = col

                search(row + 1)

                chosen[row] = -1
                usedRegions[region] = false
                usedCols[col] = false

                if (count >= limit) {
                    return
                }
            }
        }

        search(0)
        return count
    }

    private fun safeFallback(
        size: Int,
        requested: GameDifficulty,
        seed: Long
    ): Puzzle {
        val random =
            Random(seed xor 0x5EEDL)

        repeat(200) { attempt ->
            val solution =
                randomSolution(
                    size,
                    random
                ) ?: return@repeat

            val regions =
                growRegions(
                    size,
                    solution,
                    random
                )

            val allGivens =
                solution
                    .mapIndexed { row, col ->
                        Cell(row, col)
                    }
                    .toSet()

            val puzzle = Puzzle(
                id = "safe-" +
                    size + "-" +
                    seed + "-" +
                    attempt,
                size = size,
                regions = regions,
                solutionCols = solution,
                givens = allGivens,
                difficulty =
                    GameDifficulty.DISCOVERY,
                seed = seed
            )

            if (countSolutions(puzzle, 2) == 1) {
                return puzzle
            }
        }

        throw IllegalStateException(
            "Impossible de générer une grille GeckoDoku sûre."
        )
    }
}

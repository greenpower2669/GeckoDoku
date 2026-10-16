package com.greenpower2669.geckodoku

import java.util.Random
import kotlin.math.abs

data class WorkshopCandidateProof(
    val puzzle: Puzzle,
    val xWingCount: Int,
    val hypothesisDepth: Int,
    val hypothesisCount: Int
)

object PuzzleGenerator {

    /**
     * Search for independently unique Classic workshop candidates.
     * Retain only demonstrated X-Wing usage and a dependent depth-two
     * hypothesis. Candidate status is deliberately not "Infernal".
     */
    fun generateWorkshopCandidate(
        size: Int,
        seed: Long,
        shouldCancel: () -> Boolean = { false }
    ): WorkshopCandidateProof? {
        require(size in 5..12)
        val random = Random(seed)
        repeat(12) { attempt ->
            if (shouldCancel()) return null
            val solution = randomSolution(size, random) ?: return@repeat
            val base = Puzzle(
                id = "workshop-$size-$seed-$attempt",
                size = size,
                regions = growRegions(size, solution, random),
                solutionCols = solution,
                difficulty = GameDifficulty.INFERNAL,
                seed = seed
            )
            val chosen = base.solutionCells().toMutableSet()
            for (cell in base.solutionCells().shuffled(random)) {
                if (shouldCancel()) return null
                chosen.remove(cell)
                val candidate = base.copy(givens = chosen.toSet())
                if (!hasExactlyOneSolution(candidate, shouldCancel)) {
                    chosen.add(cell)
                    continue
                }
                val withWing = HypothesisSolver.analyze(
                    candidate, 2, size, shouldCancel
                )
                val wingCount = withWing.steps.count {
                    it.technique == SolveTechnique.GECKO_X_WING
                }
                if (!withWing.solved || withWing.exhaustedBudget ||
                    withWing.maxHypothesisDepth < 2 || wingCount < 1
                ) continue

                val shallow = HypothesisSolver.analyze(
                    candidate, 1, size, shouldCancel
                )
                if (shallow.solved || shallow.exhaustedBudget) continue

                val noWing = HypothesisSolver.analyze(
                    candidate, 2, size, shouldCancel,
                    rules = SolverRules.NO_X_WING
                )
                if (noWing.solved || noWing.exhaustedBudget) continue

                return WorkshopCandidateProof(
                    candidate.copy(solverTrace = withWing.steps),
                    wingCount, withWing.maxHypothesisDepth,
                    withWing.hypothesisCount
                )
            }
        }
        return null
    }

    /** Exact solution count, independent of the pedagogical proof. */
    fun hasExactlyOneSolution(
        puzzle: Puzzle,
        shouldCancel: () -> Boolean = { false }
    ): Boolean {
        if (!validSolution(puzzle)) return false
        return countSolutions(puzzle, 2, shouldCancel) == 1 &&
            !shouldCancel()
    }

    private fun validSolution(puzzle: Puzzle): Boolean {
        val n = puzzle.size
        if (puzzle.regions.any { it !in 0 until n } ||
            puzzle.solutionCols.any { it !in 0 until n } ||
            puzzle.solutionCols.toSet().size != n
        ) return false
        val solution = puzzle.solutionCells()
        if (solution.map { puzzle.regionAt(it) }.toSet().size != n) {
            return false
        }
        if (solution.zipWithNext().any { (a, b) ->
                kotlin.math.abs(a.col - b.col) <= 1
            }
        ) return false
        return puzzle.givens.all { it in solution }
    }

    private data class Candidate(
        val puzzle: Puzzle,
        val distance: Int
    )

    fun generate(
        size: Int,
        requested: GameDifficulty,
        seed: Long = System.nanoTime(),
        shouldCancel: () -> Boolean = { false }
    ): Puzzle {
        require(size in 5..12)

        val random = Random(seed)
        var best: Candidate? = null

        val attempts = if (requested == GameDifficulty.INFERNAL) {
            when {
                size <= 7 -> 32
                size <= 9 -> 24
                else -> 16
            }
        } else {
            when {
                size <= 7 -> 140
                size <= 9 -> 90
                else -> 55
            }
        }

        repeat(attempts) { attempt ->
            if (shouldCancel()) return@repeat
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
                tuneGivens(base, requested, random, shouldCancel)

            if (shouldCancel()) return@repeat
            val report =
                DifficultyIndexer.analyze(tuned, shouldCancel = shouldCancel)

            if (!report.logicallySolvable) {
                return@repeat
            }

            if (countSolutions(tuned, 2, shouldCancel) != 1) {
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

    fun generateExact(
        size: Int,
        requested: GameDifficulty,
        seed: Long = System.nanoTime(),
        shouldCancel: () -> Boolean = {
            false
        },
        onBatchCompleted:
            ((Int) -> Unit)? = null
    ): Puzzle? {
        require(size in 5..12)

        var batch = 0
        // A missing rare puzzle must not turn into an infinite search.
        val deadline = System.nanoTime() + 60_000_000_000L
        val maxBatches = if (requested == GameDifficulty.INFERNAL) 8 else 12

        while (!shouldCancel() && batch < maxBatches &&
            System.nanoTime() < deadline
        ) {
            val candidate =
                generate(
                    size = size,
                    requested = requested,
                    seed =
                        seed +
                            batch.toLong() *
                                104_729L,
                    shouldCancel = {
                        shouldCancel() || System.nanoTime() >= deadline
                    }
                )

            if (shouldCancel()) {
                return null
            }

            val report =
                DifficultyIndexer
                    .analyze(candidate, shouldCancel = {
                        shouldCancel() || System.nanoTime() >= deadline
                    })

            if (
                report.logicallySolvable &&
                report.ratedDifficulty ==
                    requested &&
                meetsProfile(
                    report,
                    requested
                ) &&
                countSolutions(
                    candidate,
                    2,
                    { shouldCancel() || System.nanoTime() >= deadline }
                ) ==
                    1
            ) {
                return candidate
            }

            batch += 1
            onBatchCompleted
                ?.invoke(batch)
        }

        return null
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
                        maxDepth = InfernalDepthPolicy.SEARCH_DEPTH,
                        maxHypothesisSteps = puzzle.size
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
        random: Random,
        shouldCancel: () -> Boolean
    ): Puzzle {
        var best = base.copy(
            givens = base.solutionCells().toSet()
        )

        var bestDistance = Int.MAX_VALUE

        repeat(if (requested == GameDifficulty.INFERNAL) 6 else 8) {
            if (shouldCancel()) return best
            val chosen =
                base.solutionCells().toMutableSet()

            val order =
                base.solutionCells().shuffled(random)

            for (cell in order) {
                if (shouldCancel()) return best
                if (chosen.size <=
                    requested.minimumGivens(base.size)
                ) {
                    break
                }

                chosen.remove(cell)

                val probe =
                    base.copy(givens = chosen.toSet())

                if (countSolutions(probe, 2, shouldCancel) != 1) {
                    chosen.add(cell)
                    continue
                }

                val report =
                    DifficultyIndexer.analyze(probe, shouldCancel = shouldCancel)

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
                    InfernalDepthPolicy.accepts(
                        report.logicallySolvable,
                        f.hypothesisDepth
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
                if (InfernalDepthPolicy.accepts(
                        report.logicallySolvable,
                        f.hypothesisDepth
                    )
                ) {
                    0
                } else {
                    55 * maxOf(
                        1,
                        InfernalDepthPolicy.MINIMUM_DEPTH -
                            f.hypothesisDepth
                    )
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
        limit: Int,
        shouldCancel: () -> Boolean = { false }
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
            if (shouldCancel() || Thread.currentThread().isInterrupted) {
                count = limit
                return
            }
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

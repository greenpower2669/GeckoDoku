package com.greenpower2669.geckodoku

enum class SolveTechnique(val label: String, val weight: Int) {
    GIVEN("Gecko donné", 0),
    ROW_SINGLE("Ligne forcée", 6),
    COLUMN_SINGLE("Colonne forcée", 6),
    REGION_SINGLE("Zone forcée", 8),
    LOCKED_CANDIDATE("Interaction ligne/zone", 15),
    X_WING("X-Wing", 28)
}

data class SolveStep(
    val technique: SolveTechnique,
    val cell: Cell? = null,
    val eliminated: Set<Cell> = emptySet()
)

data class SolveAnalysis(
    val solved: Boolean,
    val steps: List<SolveStep>,
    val maxTechnique: SolveTechnique,
    val remaining: Int
) {
    val xWingCount: Int get() = steps.count { it.technique == SolveTechnique.X_WING }
}

object HumanSolver {
    fun analyze(
        puzzle: Puzzle,
        givens: Set<Cell> = puzzle.givens,
        allowed: SolveTechnique = SolveTechnique.X_WING
    ): SolveAnalysis {
        val placed = linkedSetOf<Cell>().apply { addAll(givens) }
        val eliminated = linkedSetOf<Cell>()
        val steps = mutableListOf<SolveStep>()

        repeat(puzzle.size * puzzle.size * 8) {
            if (placed.size == puzzle.size) {
                return SolveAnalysis(true, steps, maxTechnique(steps), 0)
            }

            val candidates = candidates(puzzle, placed, eliminated)
            val single = findSingle(puzzle, placed, candidates)
            if (single != null) {
                placed.add(single.second)
                steps.add(SolveStep(single.first, single.second))
                return@repeat
            }

            if (allowed.ordinal >= SolveTechnique.LOCKED_CANDIDATE.ordinal) {
                val fresh = findLockedElimination(puzzle, placed, candidates) - eliminated
                if (fresh.isNotEmpty()) {
                    eliminated.addAll(fresh)
                    steps.add(SolveStep(SolveTechnique.LOCKED_CANDIDATE, eliminated = fresh))
                    return@repeat
                }
            }

            if (allowed.ordinal >= SolveTechnique.X_WING.ordinal) {
                val fresh = findXWingElimination(puzzle, placed, candidates) - eliminated
                if (fresh.isNotEmpty()) {
                    eliminated.addAll(fresh)
                    steps.add(SolveStep(SolveTechnique.X_WING, eliminated = fresh))
                    return@repeat
                }
            }

            return SolveAnalysis(false, steps, maxTechnique(steps), puzzle.size - placed.size)
        }

        return SolveAnalysis(placed.size == puzzle.size, steps, maxTechnique(steps), puzzle.size - placed.size)
    }

    fun selectGivens(puzzle: Puzzle, difficulty: GameDifficulty, orderSeed: Long): Set<Cell> {
        val allowed = difficulty.allowedTechnique()
        val minimum = difficulty.minimumGivens(puzzle.size)
        val chosen = puzzle.solutionCells().toMutableSet()
        val order = puzzle.solutionCells().shuffled(java.util.Random(orderSeed))

        for (cell in order) {
            if (chosen.size <= minimum) break
            chosen.remove(cell)
            val probe = analyze(puzzle, chosen, allowed)
            if (!probe.solved || probe.maxTechnique.ordinal > allowed.ordinal) chosen.add(cell)
        }
        return chosen
    }

    private fun candidates(puzzle: Puzzle, placed: Set<Cell>, eliminated: Set<Cell>): Set<Cell> {
        val result = linkedSetOf<Cell>()
        for (r in 0 until puzzle.size) {
            for (c in 0 until puzzle.size) {
                val cell = Cell(r, c)
                if (cell in placed || cell in eliminated) continue
                if (placed.any { conflicts(puzzle, it, cell) }) continue
                result.add(cell)
            }
        }
        return result
    }

    private fun conflicts(puzzle: Puzzle, a: Cell, b: Cell): Boolean {
        if (a == b) return false
        if (a.row == b.row || a.col == b.col) return true
        if (puzzle.regionAt(a) == puzzle.regionAt(b)) return true
        return kotlin.math.abs(a.row - b.row) <= 1 && kotlin.math.abs(a.col - b.col) <= 1
    }

    private fun findSingle(
        puzzle: Puzzle,
        placed: Set<Cell>,
        candidates: Set<Cell>
    ): Pair<SolveTechnique, Cell>? {
        for (r in 0 until puzzle.size) {
            if (placed.any { it.row == r }) continue
            val list = candidates.filter { it.row == r }
            if (list.size == 1) return SolveTechnique.ROW_SINGLE to list.first()
        }
        for (c in 0 until puzzle.size) {
            if (placed.any { it.col == c }) continue
            val list = candidates.filter { it.col == c }
            if (list.size == 1) return SolveTechnique.COLUMN_SINGLE to list.first()
        }
        for (region in 0 until puzzle.size) {
            if (placed.any { puzzle.regionAt(it) == region }) continue
            val list = candidates.filter { puzzle.regionAt(it) == region }
            if (list.size == 1) return SolveTechnique.REGION_SINGLE to list.first()
        }
        return null
    }

    private fun findLockedElimination(
        puzzle: Puzzle,
        placed: Set<Cell>,
        candidates: Set<Cell>
    ): Set<Cell> {
        for (region in 0 until puzzle.size) {
            if (placed.any { puzzle.regionAt(it) == region }) continue
            val rc = candidates.filter { puzzle.regionAt(it) == region }
            if (rc.size >= 2) {
                val rows = rc.map { it.row }.toSet()
                if (rows.size == 1) {
                    val row = rows.first()
                    val out = candidates.filter { it.row == row && puzzle.regionAt(it) != region }.toSet()
                    if (out.isNotEmpty()) return out
                }
                val cols = rc.map { it.col }.toSet()
                if (cols.size == 1) {
                    val col = cols.first()
                    val out = candidates.filter { it.col == col && puzzle.regionAt(it) != region }.toSet()
                    if (out.isNotEmpty()) return out
                }
            }
        }

        for (r in 0 until puzzle.size) {
            if (placed.any { it.row == r }) continue
            val rowCandidates = candidates.filter { it.row == r }
            val regions = rowCandidates.map { puzzle.regionAt(it) }.toSet()
            if (rowCandidates.size >= 2 && regions.size == 1) {
                val region = regions.first()
                val out = candidates.filter { puzzle.regionAt(it) == region && it.row != r }.toSet()
                if (out.isNotEmpty()) return out
            }
        }

        for (c in 0 until puzzle.size) {
            if (placed.any { it.col == c }) continue
            val colCandidates = candidates.filter { it.col == c }
            val regions = colCandidates.map { puzzle.regionAt(it) }.toSet()
            if (colCandidates.size >= 2 && regions.size == 1) {
                val region = regions.first()
                val out = candidates.filter { puzzle.regionAt(it) == region && it.col != c }.toSet()
                if (out.isNotEmpty()) return out
            }
        }
        return emptySet()
    }

    private fun findXWingElimination(
        puzzle: Puzzle,
        placed: Set<Cell>,
        candidates: Set<Cell>
    ): Set<Cell> {
        val rowPairs = mutableMapOf<Set<Int>, MutableList<Int>>()
        for (r in 0 until puzzle.size) {
            if (placed.any { it.row == r }) continue
            val cols = candidates.filter { it.row == r }.map { it.col }.toSet()
            if (cols.size == 2) rowPairs.getOrPut(cols) { mutableListOf() }.add(r)
        }
        for ((cols, rows) in rowPairs) {
            if (rows.size == 2) {
                val out = candidates.filter { it.row !in rows && it.col in cols }.toSet()
                if (out.isNotEmpty()) return out
            }
        }

        val colPairs = mutableMapOf<Set<Int>, MutableList<Int>>()
        for (c in 0 until puzzle.size) {
            if (placed.any { it.col == c }) continue
            val rows = candidates.filter { it.col == c }.map { it.row }.toSet()
            if (rows.size == 2) colPairs.getOrPut(rows) { mutableListOf() }.add(c)
        }
        for ((rows, cols) in colPairs) {
            if (cols.size == 2) {
                val out = candidates.filter { it.col !in cols && it.row in rows }.toSet()
                if (out.isNotEmpty()) return out
            }
        }
        return emptySet()
    }

    private fun maxTechnique(steps: List<SolveStep>): SolveTechnique =
        steps.maxByOrNull { it.technique.ordinal }?.technique ?: SolveTechnique.GIVEN
}

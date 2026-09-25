package com.greenpower2669.geckodoku

enum class SolveTechnique(val label: String, val weight: Int) {
    GIVEN("Gecko donné", 0),
    ROW_SINGLE("Ligne forcée", 4),
    COLUMN_SINGLE("Colonne forcée", 4),
    REGION_SINGLE("Zone forcée", 6),
    REGION_LOCKED("Zone verrouillée ligne/colonne", 12),
    REGION_TOUCH_PROJECTION("Projection de zone", 18),
    GECKO_X_WING("Gecko X-Wing", 30),
    HYPOTHESIS_TEST("Hypothèse par contradiction", 45),
    DOUBLE_HYPOTHESIS("Double hypothèse", 60)
}

data class SolverRules(
    val regionLocked: Boolean = true,
    val regionTouchProjection: Boolean = true,
    val xWing: Boolean = true
) {
    companion object {
        val SINGLES = SolverRules(false, false, false)
        val REGION = SolverRules(true, true, false)
        val NO_PROJECTION = SolverRules(true, false, true)
        val FULL = SolverRules(true, true, true)
    }
}

data class SolveStep(
    val technique: SolveTechnique,
    val cell: Cell? = null,
    val eliminated: Set<Cell> = emptySet(),
    val sourceCells: Set<Cell> = emptySet(),
    val sourceRegions: Set<Int> = emptySet(),
    val axis: String? = null,
    val beforeConfirmed: Set<Cell> = emptySet(),
    val beforeExcluded: Set<Cell> = emptySet(),
    val hypothesisRejected: Cell? = null,
    val hypothesisDepth: Int = 0
) {
    val actionCells: Set<Cell>
        get() = cell?.let { setOf(it) } ?: eliminated
}

data class SolveAnalysis(
    val solved: Boolean,
    val steps: List<SolveStep>,
    val maxTechnique: SolveTechnique,
    val remaining: Int
) {
    val xWingCount: Int get() = steps.count { it.technique == SolveTechnique.GECKO_X_WING }

    val regionLogicCount: Int get() = steps.count {
        it.technique == SolveTechnique.REGION_SINGLE ||
            it.technique == SolveTechnique.REGION_LOCKED ||
            it.technique == SolveTechnique.REGION_TOUCH_PROJECTION
    }

    val projectionCount: Int get() =
        steps.count { it.technique == SolveTechnique.REGION_TOUCH_PROJECTION }
}

object HumanSolver {
    fun analyze(
        puzzle: Puzzle,
        givens: Set<Cell> = puzzle.givens,
        rules: SolverRules = SolverRules.FULL
    ): SolveAnalysis {
        val placed = linkedSetOf<Cell>().apply { addAll(givens) }
        val eliminated = linkedSetOf<Cell>()
        val steps = mutableListOf<SolveStep>()

        repeat(puzzle.size * puzzle.size * 20) {
            if (placed.size == puzzle.size) {
                return SolveAnalysis(true, steps, maxTechnique(steps), 0)
            }

            val raw = nextStep(puzzle, placed, eliminated, rules)
                ?: return SolveAnalysis(
                    false,
                    steps,
                    maxTechnique(steps),
                    puzzle.size - placed.size
                )

            val step = raw.copy(
                beforeConfirmed = placed.toSet(),
                beforeExcluded = eliminated.toSet()
            )

            applyStep(step, placed, eliminated)
            steps.add(step)
        }

        return SolveAnalysis(
            placed.size == puzzle.size,
            steps,
            maxTechnique(steps),
            puzzle.size - placed.size
        )
    }

    fun nextStep(
        puzzle: Puzzle,
        confirmed: Set<Cell>,
        excluded: Set<Cell>,
        rules: SolverRules = SolverRules.FULL
    ): SolveStep? {
        if (confirmed.size >= puzzle.size) return null

        val candidates = candidates(puzzle, confirmed, excluded)
        if (candidates.isEmpty()) return null

        findSingle(puzzle, confirmed, candidates)?.let { return it }

        if (rules.regionLocked) {
            findRegionLockedElimination(puzzle, confirmed, candidates)?.let { return it }
        }

        if (rules.regionTouchProjection) {
            findRegionTouchProjection(puzzle, confirmed, candidates)?.let { return it }
        }

        if (rules.xWing) {
            findGeckoXWingElimination(puzzle, confirmed, candidates)?.let { return it }
        }

        return null
    }

    private fun applyStep(
        step: SolveStep,
        placed: MutableSet<Cell>,
        eliminated: MutableSet<Cell>
    ) {
        step.cell?.let {
            placed.add(it)
            eliminated.remove(it)
        }
        eliminated.addAll(step.eliminated)
    }

    private fun candidates(
        puzzle: Puzzle,
        placed: Set<Cell>,
        eliminated: Set<Cell>
    ): Set<Cell> {
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
        return touches(a, b)
    }

    private fun touches(a: Cell, b: Cell): Boolean =
        kotlin.math.abs(a.row - b.row) <= 1 &&
            kotlin.math.abs(a.col - b.col) <= 1

    private fun findSingle(
        puzzle: Puzzle,
        placed: Set<Cell>,
        candidates: Set<Cell>
    ): SolveStep? {
        for (r in 0 until puzzle.size) {
            if (placed.any { it.row == r }) continue
            val list = candidates.filter { it.row == r }

            if (list.size == 1) {
                return SolveStep(
                    technique = SolveTechnique.ROW_SINGLE,
                    cell = list.first(),
                    sourceCells = (0 until puzzle.size).map { Cell(r, it) }.toSet(),
                    axis = "ligne " + (r + 1)
                )
            }
        }

        for (c in 0 until puzzle.size) {
            if (placed.any { it.col == c }) continue
            val list = candidates.filter { it.col == c }

            if (list.size == 1) {
                return SolveStep(
                    technique = SolveTechnique.COLUMN_SINGLE,
                    cell = list.first(),
                    sourceCells = (0 until puzzle.size).map { Cell(it, c) }.toSet(),
                    axis = "colonne " + (c + 1)
                )
            }
        }

        for (region in 0 until puzzle.size) {
            if (placed.any { puzzle.regionAt(it) == region }) continue
            val list = candidates.filter { puzzle.regionAt(it) == region }

            if (list.size == 1) {
                return SolveStep(
                    technique = SolveTechnique.REGION_SINGLE,
                    cell = list.first(),
                    sourceCells = allCellsOfRegion(puzzle, region),
                    sourceRegions = setOf(region)
                )
            }
        }

        return null
    }

    private fun findRegionLockedElimination(
        puzzle: Puzzle,
        placed: Set<Cell>,
        candidates: Set<Cell>
    ): SolveStep? {
        for (region in 0 until puzzle.size) {
            if (placed.any { puzzle.regionAt(it) == region }) continue
            val rc = candidates.filter { puzzle.regionAt(it) == region }
            if (rc.size < 2) continue

            val rows = rc.map { it.row }.toSet()
            if (rows.size == 1) {
                val row = rows.first()
                val out = candidates
                    .filter { it.row == row && puzzle.regionAt(it) != region }
                    .toSet()

                if (out.isNotEmpty()) {
                    return SolveStep(
                        technique = SolveTechnique.REGION_LOCKED,
                        eliminated = out,
                        sourceCells = rc.toSet(),
                        sourceRegions = setOf(region),
                        axis = "ligne " + (row + 1)
                    )
                }
            }

            val cols = rc.map { it.col }.toSet()
            if (cols.size == 1) {
                val col = cols.first()
                val out = candidates
                    .filter { it.col == col && puzzle.regionAt(it) != region }
                    .toSet()

                if (out.isNotEmpty()) {
                    return SolveStep(
                        technique = SolveTechnique.REGION_LOCKED,
                        eliminated = out,
                        sourceCells = rc.toSet(),
                        sourceRegions = setOf(region),
                        axis = "colonne " + (col + 1)
                    )
                }
            }
        }

        for (r in 0 until puzzle.size) {
            if (placed.any { it.row == r }) continue
            val rowCandidates = candidates.filter { it.row == r }
            val regions = rowCandidates.map { puzzle.regionAt(it) }.toSet()

            if (rowCandidates.size >= 2 && regions.size == 1) {
                val region = regions.first()
                val out = candidates
                    .filter { puzzle.regionAt(it) == region && it.row != r }
                    .toSet()

                if (out.isNotEmpty()) {
                    return SolveStep(
                        technique = SolveTechnique.REGION_LOCKED,
                        eliminated = out,
                        sourceCells = rowCandidates.toSet(),
                        sourceRegions = setOf(region),
                        axis = "ligne " + (r + 1)
                    )
                }
            }
        }

        for (c in 0 until puzzle.size) {
            if (placed.any { it.col == c }) continue
            val colCandidates = candidates.filter { it.col == c }
            val regions = colCandidates.map { puzzle.regionAt(it) }.toSet()

            if (colCandidates.size >= 2 && regions.size == 1) {
                val region = regions.first()
                val out = candidates
                    .filter { puzzle.regionAt(it) == region && it.col != c }
                    .toSet()

                if (out.isNotEmpty()) {
                    return SolveStep(
                        technique = SolveTechnique.REGION_LOCKED,
                        eliminated = out,
                        sourceCells = colCandidates.toSet(),
                        sourceRegions = setOf(region),
                        axis = "colonne " + (c + 1)
                    )
                }
            }
        }

        return null
    }

    private fun findRegionTouchProjection(
        puzzle: Puzzle,
        placed: Set<Cell>,
        candidates: Set<Cell>
    ): SolveStep? {
        for (region in 0 until puzzle.size) {
            if (placed.any { puzzle.regionAt(it) == region }) continue

            val regionCandidates =
                candidates.filter { puzzle.regionAt(it) == region }

            if (regionCandidates.size !in 2..4) continue

            val out = candidates.asSequence()
                .filter { puzzle.regionAt(it) != region }
                .filter { outside ->
                    regionCandidates.all { inside -> touches(outside, inside) }
                }
                .toSet()

            if (out.isNotEmpty()) {
                return SolveStep(
                    technique = SolveTechnique.REGION_TOUCH_PROJECTION,
                    eliminated = out,
                    sourceCells = regionCandidates.toSet(),
                    sourceRegions = setOf(region),
                    axis = regionCandidates.size.toString() + " candidats dans la zone"
                )
            }
        }

        return null
    }

    private fun findGeckoXWingElimination(
        puzzle: Puzzle,
        placed: Set<Cell>,
        candidates: Set<Cell>
    ): SolveStep? {
        val rowPairs = mutableMapOf<Set<Int>, MutableList<Int>>()

        for (r in 0 until puzzle.size) {
            if (placed.any { it.row == r }) continue
            val cols = candidates.filter { it.row == r }.map { it.col }.toSet()

            if (cols.size == 2) {
                rowPairs.getOrPut(cols) { mutableListOf() }.add(r)
            }
        }

        for ((cols, rows) in rowPairs) {
            if (rows.size == 2) {
                val source = rows.flatMap { r ->
                    cols.map { c -> Cell(r, c) }
                }.toSet()

                val out = candidates
                    .filter { it.row !in rows && it.col in cols }
                    .toSet()

                if (out.isNotEmpty()) {
                    return SolveStep(
                        technique = SolveTechnique.GECKO_X_WING,
                        eliminated = out,
                        sourceCells = source,
                        axis = "lignes " + (rows[0] + 1) + " et " + (rows[1] + 1)
                    )
                }
            }
        }

        val colPairs = mutableMapOf<Set<Int>, MutableList<Int>>()

        for (c in 0 until puzzle.size) {
            if (placed.any { it.col == c }) continue
            val rows = candidates.filter { it.col == c }.map { it.row }.toSet()

            if (rows.size == 2) {
                colPairs.getOrPut(rows) { mutableListOf() }.add(c)
            }
        }

        for ((rows, cols) in colPairs) {
            if (cols.size == 2) {
                val source = cols.flatMap { c ->
                    rows.map { r -> Cell(r, c) }
                }.toSet()

                val out = candidates
                    .filter { it.col !in cols && it.row in rows }
                    .toSet()

                if (out.isNotEmpty()) {
                    return SolveStep(
                        technique = SolveTechnique.GECKO_X_WING,
                        eliminated = out,
                        sourceCells = source,
                        axis = "colonnes " + (cols[0] + 1) + " et " + (cols[1] + 1)
                    )
                }
            }
        }

        val unsolvedRegions = (0 until puzzle.size)
            .filter { region ->
                placed.none { puzzle.regionAt(it) == region }
            }

        for (i in 0 until unsolvedRegions.size) {
            for (j in i + 1 until unsolvedRegions.size) {
                val a = unsolvedRegions[i]
                val b = unsolvedRegions[j]

                val pairCandidates = candidates.filter {
                    val region = puzzle.regionAt(it)
                    region == a || region == b
                }

                if (pairCandidates.isEmpty()) continue

                val rows = pairCandidates.map { it.row }.toSet()
                if (rows.size == 2) {
                    val out = candidates.filter {
                        it.row in rows &&
                            puzzle.regionAt(it) != a &&
                            puzzle.regionAt(it) != b
                    }.toSet()

                    if (out.isNotEmpty()) {
                        return SolveStep(
                            technique = SolveTechnique.GECKO_X_WING,
                            eliminated = out,
                            sourceCells = pairCandidates.toSet(),
                            sourceRegions = setOf(a, b),
                            axis = "deux zones sur deux lignes"
                        )
                    }
                }

                val cols = pairCandidates.map { it.col }.toSet()
                if (cols.size == 2) {
                    val out = candidates.filter {
                        it.col in cols &&
                            puzzle.regionAt(it) != a &&
                            puzzle.regionAt(it) != b
                    }.toSet()

                    if (out.isNotEmpty()) {
                        return SolveStep(
                            technique = SolveTechnique.GECKO_X_WING,
                            eliminated = out,
                            sourceCells = pairCandidates.toSet(),
                            sourceRegions = setOf(a, b),
                            axis = "deux zones sur deux colonnes"
                        )
                    }
                }
            }
        }

        return null
    }

    private fun allCellsOfRegion(
        puzzle: Puzzle,
        region: Int
    ): Set<Cell> {
        val result = linkedSetOf<Cell>()

        for (r in 0 until puzzle.size) {
            for (c in 0 until puzzle.size) {
                val cell = Cell(r, c)
                if (puzzle.regionAt(cell) == region) {
                    result.add(cell)
                }
            }
        }

        return result
    }

    private fun maxTechnique(steps: List<SolveStep>): SolveTechnique =
        steps.maxByOrNull { it.technique.ordinal }?.technique
            ?: SolveTechnique.GIVEN
}

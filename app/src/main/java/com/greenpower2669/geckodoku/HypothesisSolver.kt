package com.greenpower2669.geckodoku

data class HypothesisAnalysis(
    val solved: Boolean,
    val steps: List<SolveStep>,
    val hypothesisCount: Int,
    val maxHypothesisDepth: Int
)

private data class LogicState(
    val confirmed: LinkedHashSet<Cell>,
    val excluded: LinkedHashSet<Cell>
)

private data class BinaryChoice(
    val first: Cell,
    val second: Cell,
    val sourceCells: Set<Cell>,
    val axis: String
)

private data class ContradictionProof(
    val depth: Int
)

object HypothesisSolver {
    fun analyze(
        puzzle: Puzzle,
        maxDepth: Int,
        maxHypothesisSteps: Int
    ): HypothesisAnalysis {
        val state = LogicState(
            linkedSetOf<Cell>().apply {
                addAll(puzzle.givens)
            },
            linkedSetOf()
        )
        val steps = mutableListOf<SolveStep>()
        var hypotheses = 0
        var maxUsedDepth = 0

        repeat(puzzle.size * puzzle.size * 30) {
            val contradiction =
                hasContradiction(
                    puzzle,
                    state.confirmed,
                    state.excluded
                )

            if (contradiction) {
                return HypothesisAnalysis(
                    false,
                    steps,
                    hypotheses,
                    maxUsedDepth
                )
            }

            if (state.confirmed.size == puzzle.size) {
                return HypothesisAnalysis(
                    true,
                    steps,
                    hypotheses,
                    maxUsedDepth
                )
            }

            val deterministic =
                HumanSolver.nextStep(
                    puzzle,
                    state.confirmed,
                    state.excluded,
                    SolverRules.FULL
                )

            if (deterministic != null) {
                val step =
                    deterministic.copy(
                        beforeConfirmed =
                            state.confirmed.toSet(),
                        beforeExcluded =
                            state.excluded.toSet()
                    )

                applyStep(
                    step,
                    state
                )
                steps.add(step)
                return@repeat
            }

            if (hypotheses >= maxHypothesisSteps) {
                return HypothesisAnalysis(
                    false,
                    steps,
                    hypotheses,
                    maxUsedDepth
                )
            }

            val hypothesis =
                nextHypothesisStep(
                    puzzle,
                    state.confirmed,
                    state.excluded,
                    maxDepth
                ) ?: return HypothesisAnalysis(
                    false,
                    steps,
                    hypotheses,
                    maxUsedDepth
                )

            val recorded =
                hypothesis.copy(
                    beforeConfirmed =
                        state.confirmed.toSet(),
                    beforeExcluded =
                        state.excluded.toSet()
                )

            applyStep(
                recorded,
                state
            )

            hypotheses += 1
            maxUsedDepth =
                maxOf(
                    maxUsedDepth,
                    recorded.hypothesisDepth
                )
            steps.add(recorded)
        }

        return HypothesisAnalysis(
            state.confirmed.size == puzzle.size,
            steps,
            hypotheses,
            maxUsedDepth
        )
    }

    fun nextHypothesisStep(
        puzzle: Puzzle,
        confirmed: Set<Cell>,
        excluded: Set<Cell>,
        maxDepth: Int
    ): SolveStep? {
        if (maxDepth <= 0) return null

        val choices =
            binaryChoices(
                puzzle,
                confirmed,
                excluded
            )

        for (choice in choices) {
            val firstProof =
                proveContradiction(
                    puzzle,
                    confirmed + choice.first,
                    excluded,
                    maxDepth - 1
                )

            val secondProof =
                proveContradiction(
                    puzzle,
                    confirmed + choice.second,
                    excluded,
                    maxDepth - 1
                )

            if (firstProof != null &&
                secondProof == null
            ) {
                val depth =
                    1 + firstProof.depth

                return SolveStep(
                    technique =
                        if (depth <= 1) {
                            SolveTechnique.HYPOTHESIS_TEST
                        } else {
                            SolveTechnique.DOUBLE_HYPOTHESIS
                        },
                    cell = choice.second,
                    sourceCells =
                        choice.sourceCells,
                    axis = choice.axis,
                    hypothesisRejected =
                        choice.first,
                    hypothesisDepth = depth
                )
            }

            if (secondProof != null &&
                firstProof == null
            ) {
                val depth =
                    1 + secondProof.depth

                return SolveStep(
                    technique =
                        if (depth <= 1) {
                            SolveTechnique.HYPOTHESIS_TEST
                        } else {
                            SolveTechnique.DOUBLE_HYPOTHESIS
                        },
                    cell = choice.first,
                    sourceCells =
                        choice.sourceCells,
                    axis = choice.axis,
                    hypothesisRejected =
                        choice.second,
                    hypothesisDepth = depth
                )
            }
        }

        return null
    }

    private fun proveContradiction(
        puzzle: Puzzle,
        initialConfirmed: Set<Cell>,
        initialExcluded: Set<Cell>,
        remainingDepth: Int
    ): ContradictionProof? {
        val state =
            LogicState(
                linkedSetOf<Cell>().apply {
                    addAll(initialConfirmed)
                },
                linkedSetOf<Cell>().apply {
                    addAll(initialExcluded)
                }
            )

        repeat(puzzle.size * puzzle.size * 20) {
            if (hasContradiction(
                    puzzle,
                    state.confirmed,
                    state.excluded
                )
            ) {
                return ContradictionProof(0)
            }

            if (state.confirmed.size == puzzle.size) {
                return null
            }

            val step =
                HumanSolver.nextStep(
                    puzzle,
                    state.confirmed,
                    state.excluded,
                    SolverRules.FULL
                )

            if (step != null) {
                applyStep(
                    step,
                    state
                )
                return@repeat
            }

            if (remainingDepth <= 0) {
                return null
            }

            val choice =
                binaryChoices(
                    puzzle,
                    state.confirmed,
                    state.excluded
                ).firstOrNull()
                    ?: return null

            val a =
                proveContradiction(
                    puzzle,
                    state.confirmed +
                        choice.first,
                    state.excluded,
                    remainingDepth - 1
                )

            val b =
                proveContradiction(
                    puzzle,
                    state.confirmed +
                        choice.second,
                    state.excluded,
                    remainingDepth - 1
                )

            if (a != null && b != null) {
                return ContradictionProof(
                    1 +
                        maxOf(
                            a.depth,
                            b.depth
                        )
                )
            }

            if (a != null && b == null) {
                state.confirmed.add(
                    choice.second
                )
                return@repeat
            }

            if (b != null && a == null) {
                state.confirmed.add(
                    choice.first
                )
                return@repeat
            }

            return null
        }

        return if (
            hasContradiction(
                puzzle,
                state.confirmed,
                state.excluded
            )
        ) {
            ContradictionProof(0)
        } else {
            null
        }
    }

    private fun applyStep(
        step: SolveStep,
        state: LogicState
    ) {
        step.cell?.let {
            state.confirmed.add(it)
            state.excluded.remove(it)
        }

        state.excluded.addAll(
            step.eliminated
        )
    }

    private fun binaryChoices(
        puzzle: Puzzle,
        confirmed: Set<Cell>,
        excluded: Set<Cell>
    ): List<BinaryChoice> {
        val candidates =
            candidates(
                puzzle,
                confirmed,
                excluded
            )

        val result =
            mutableListOf<BinaryChoice>()

        for (row in 0 until puzzle.size) {
            if (confirmed.any { it.row == row }) {
                continue
            }

            val list =
                candidates
                    .filter { it.row == row }

            if (list.size == 2) {
                result.add(
                    BinaryChoice(
                        list[0],
                        list[1],
                        list.toSet(),
                        "ligne " + (row + 1)
                    )
                )
            }
        }

        for (col in 0 until puzzle.size) {
            if (confirmed.any { it.col == col }) {
                continue
            }

            val list =
                candidates
                    .filter { it.col == col }

            if (list.size == 2) {
                result.add(
                    BinaryChoice(
                        list[0],
                        list[1],
                        list.toSet(),
                        "colonne " + (col + 1)
                    )
                )
            }
        }

        for (region in 0 until puzzle.size) {
            if (
                confirmed.any {
                    puzzle.regionAt(it) ==
                        region
                }
            ) {
                continue
            }

            val list =
                candidates
                    .filter {
                        puzzle.regionAt(it) ==
                            region
                    }

            if (list.size == 2) {
                result.add(
                    BinaryChoice(
                        list[0],
                        list[1],
                        list.toSet(),
                        "zone à deux candidats"
                    )
                )
            }
        }

        return result.distinctBy {
            setOf(
                it.first,
                it.second
            )
        }
    }

    private fun candidates(
        puzzle: Puzzle,
        confirmed: Set<Cell>,
        excluded: Set<Cell>
    ): Set<Cell> {
        val result =
            linkedSetOf<Cell>()

        for (r in 0 until puzzle.size) {
            for (c in 0 until puzzle.size) {
                val cell =
                    Cell(r, c)

                if (
                    cell in confirmed ||
                    cell in excluded
                ) {
                    continue
                }

                if (
                    confirmed.any {
                        conflicts(
                            puzzle,
                            it,
                            cell
                        )
                    }
                ) {
                    continue
                }

                result.add(cell)
            }
        }

        return result
    }

    private fun hasContradiction(
        puzzle: Puzzle,
        confirmed: Set<Cell>,
        excluded: Set<Cell>
    ): Boolean {
        val confirmedList =
            confirmed.toList()

        for (i in confirmedList.indices) {
            for (j in i + 1 until confirmedList.size) {
                if (
                    conflicts(
                        puzzle,
                        confirmedList[i],
                        confirmedList[j]
                    )
                ) {
                    return true
                }
            }
        }

        val candidates =
            candidates(
                puzzle,
                confirmed,
                excluded
            )

        for (row in 0 until puzzle.size) {
            if (
                confirmed.none {
                    it.row == row
                } &&
                candidates.none {
                    it.row == row
                }
            ) {
                return true
            }
        }

        for (col in 0 until puzzle.size) {
            if (
                confirmed.none {
                    it.col == col
                } &&
                candidates.none {
                    it.col == col
                }
            ) {
                return true
            }
        }

        for (region in 0 until puzzle.size) {
            if (
                confirmed.none {
                    puzzle.regionAt(it) ==
                        region
                } &&
                candidates.none {
                    puzzle.regionAt(it) ==
                        region
                }
            ) {
                return true
            }
        }

        return false
    }

    private fun conflicts(
        puzzle: Puzzle,
        a: Cell,
        b: Cell
    ): Boolean {
        if (a == b) return false

        if (
            a.row == b.row ||
            a.col == b.col
        ) {
            return true
        }

        if (
            puzzle.regionAt(a) ==
            puzzle.regionAt(b)
        ) {
            return true
        }

        return kotlin.math.abs(
            a.row - b.row
        ) <= 1 &&
            kotlin.math.abs(
                a.col - b.col
            ) <= 1
    }
}

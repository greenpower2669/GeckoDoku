package com.greenpower2669.geckodoku

data class HypothesisAnalysis(
    val solved: Boolean,
    val steps: List<SolveStep>,
    val hypothesisCount: Int,
    val maxHypothesisDepth: Int,
    val exhaustedBudget: Boolean = false
)

private data class LogicState(
    val confirmed: LinkedHashSet<Cell>,
    val excluded: LinkedHashSet<Cell>
)

private data class BinaryChoice(
    val first: Cell,
    // null means the complementary branch excludes the first cell.
    val second: Cell?,
    val sourceCells: Set<Cell>,
    val axis: String
)

// A depth-exhausted branch is not a contradiction or a solution.
private sealed class BranchOutcome {
    data class Contradiction(val depth: Int) : BranchOutcome()
    object Solved : BranchOutcome()
    object Inconclusive : BranchOutcome()
}

// Cache of logically resolved subtrees. No result from a paused search is
// stored as a mathematical conclusion. Persistent maps are puzzle-scoped.
private class ProofSearchContext(
    private val shouldCancel: () -> Boolean,
    private val saved: MutableMap<String, String>,
    private val namespace: String,
    private val onPersist: (Map<String, String>) -> Unit
) {
    private var pending = 0
    var visited: Long = 0
        private set
    var truncated = false
        private set

    fun cached(key: String): BranchOutcome? =
        when (val code = saved[namespace + key]) {
            "S" -> BranchOutcome.Solved
            "I" -> BranchOutcome.Inconclusive
            null -> null
            else -> code.takeIf { it.startsWith("C") }
                ?.drop(1)?.toIntOrNull()?.let(BranchOutcome::Contradiction)
        }

    fun remember(key: String, value: BranchOutcome) {
        val code = when (value) {
            is BranchOutcome.Contradiction -> "C" + value.depth
            BranchOutcome.Solved -> "S"
            BranchOutcome.Inconclusive -> "I"
        }
        val fullKey = namespace + key
        if (saved[fullKey] == code) return
        saved[fullKey] = code
        pending += 1
        if (pending >= 64) flush()
    }

    fun flush() {
        if (pending > 0) {
            onPersist(saved)
            pending = 0
        }
    }

    fun allowVisit(): Boolean {
        if (Thread.currentThread().isInterrupted || shouldCancel()) {
            truncated = true
            return false
        }
        visited += 1L
        return true
    }
}

object HypothesisSolver {
    fun analyze(
        puzzle: Puzzle,
        maxDepth: Int,
        maxHypothesisSteps: Int,
        shouldCancel: () -> Boolean = { false },
        rules: SolverRules = SolverRules.FULL,
        proofMemo: MutableMap<String, String> = mutableMapOf(),
        onProofCheckpoint: (Map<String, String>) -> Unit = {}
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
        var proofBudgetExhausted = false

        while (true) {
            if (shouldCancel() || Thread.currentThread().isInterrupted) {
                return HypothesisAnalysis(false, steps, hypotheses, maxUsedDepth, true)
            }
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
                    rules
                )

            if (deterministic != null) {
                val beforeConfirmed = state.confirmed.toSet()
                val beforeExcluded = state.excluded.toSet()
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
                if (state.confirmed == beforeConfirmed &&
                    state.excluded == beforeExcluded
                ) {
                    return HypothesisAnalysis(
                        false, steps, hypotheses, maxUsedDepth
                    )
                }
                steps.add(step)
                continue
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
                    maxDepth,
                    shouldCancel,
                    onLimitHit = { proofBudgetExhausted = true },
                    rules = rules,
                    proofMemo = proofMemo,
                    onProofCheckpoint = onProofCheckpoint
                ) ?: return HypothesisAnalysis(
                    false,
                    steps,
                    hypotheses,
                    maxUsedDepth,
                    proofBudgetExhausted
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
        maxDepth: Int,
        shouldCancel: () -> Boolean = { false },
        onLimitHit: () -> Unit = {},
        rules: SolverRules = SolverRules.FULL,
        proofMemo: MutableMap<String, String> = mutableMapOf(),
        onProofCheckpoint: (Map<String, String>) -> Unit = {}
    ): SolveStep? {
        if (maxDepth <= 0 || shouldCancel() ||
            Thread.currentThread().isInterrupted) {
            return null
        }

        val choices = binaryChoices(puzzle, confirmed, excluded)
        if (choices.isEmpty()) return null

        // Try the minimum proof depth first. Three independent guesses
        // must NEVER be misclassified as a three-level nested proof.
        for (depth in 1..maxDepth) {
            val namespace = "${rules.regionLocked}:" +
                "${rules.regionTouchProjection}:${rules.xWing}:"
            val context = ProofSearchContext(
                shouldCancel, proofMemo, namespace, onProofCheckpoint
            )
            try {
                for (choice in choices) {
                if (context.truncated) break
                val first = proveContradiction(
                    puzzle,
                    confirmed + choice.first,
                    excluded,
                    depth - 1,
                    context, rules
                )
                val second = if (choice.second != null) {
                    proveContradiction(
                        puzzle, confirmed + choice.second,
                        excluded, depth - 1, context, rules
                    )
                } else {
                    proveContradiction(
                        puzzle, confirmed,
                        excluded + choice.first,
                        depth - 1, context, rules
                    )
                }

                // A shallow search that ran out of time cannot certify
                // that a deeper hypothesis is genuinely necessary.
                if (context.truncated) break
                if (first is BranchOutcome.Contradiction &&
                    second is BranchOutcome.Contradiction
                ) {
                    // Invalid state: neither alternative is possible.
                    continue
                }
                if (first is BranchOutcome.Contradiction) {
                    val proofDepth = 1 + first.depth
                    return SolveStep(
                        technique = if (proofDepth == 1) {
                            SolveTechnique.HYPOTHESIS_TEST
                        } else {
                            SolveTechnique.DOUBLE_HYPOTHESIS
                        },
                        cell = choice.second,
                        eliminated = if (choice.second == null) {
                            setOf(choice.first)
                        } else {
                            emptySet()
                        },
                        sourceCells = choice.sourceCells,
                        axis = choice.axis,
                        hypothesisRejected = choice.first,
                        hypothesisDepth = proofDepth
                    )
                }
                if (second is BranchOutcome.Contradiction) {
                    val proofDepth = 1 + second.depth
                    return SolveStep(
                        technique = if (proofDepth == 1) {
                            SolveTechnique.HYPOTHESIS_TEST
                        } else {
                            SolveTechnique.DOUBLE_HYPOTHESIS
                        },
                        cell = choice.first,
                        sourceCells = choice.sourceCells,
                        axis = choice.axis,
                        hypothesisRejected = choice.second,
                        hypothesisDepth = proofDepth
                    )
                }
            }

                if (context.truncated) {
                    onLimitHit()
                    return null
                }
            } finally {
                // Reached also by early returns from a successful deduction.
                context.flush()
            }
        }
        return null
    }

    private fun proveContradiction(
        puzzle: Puzzle,
        initialConfirmed: Set<Cell>,
        initialExcluded: Set<Cell>,
        remainingDepth: Int,
        context: ProofSearchContext,
        rules: SolverRules
    ): BranchOutcome {
        if (!context.allowVisit()) return BranchOutcome.Inconclusive

        val key = stateKey(
            puzzle, initialConfirmed, initialExcluded, remainingDepth
        )
        context.cached(key)?.let { return it }

        val state = LogicState(
            linkedSetOf<Cell>().apply { addAll(initialConfirmed) },
            linkedSetOf<Cell>().apply { addAll(initialExcluded) }
        )
        var nestedDepthUsed = 0
        var outcome: BranchOutcome = BranchOutcome.Inconclusive

        while (true) {
            if (!context.allowVisit()) break
            if (hasContradiction(
                    puzzle, state.confirmed, state.excluded
                )
            ) {
                outcome = BranchOutcome.Contradiction(nestedDepthUsed)
                break
            }
            if (state.confirmed.size == puzzle.size) {
                outcome = BranchOutcome.Solved
                break
            }

            val step = HumanSolver.nextStep(
                puzzle, state.confirmed, state.excluded, rules
            )
            if (step != null) {
                val previousConfirmed = state.confirmed.toSet()
                val previousExcluded = state.excluded.toSet()
                applyStep(step, state)
                if (previousConfirmed == state.confirmed &&
                    previousExcluded == state.excluded
                ) break
                continue
            }

            if (remainingDepth <= 0) break

            var bestChoice: BinaryChoice? = null
            var rejectFirst = false
            var bestDepth = Int.MAX_VALUE
            var foundSolution = false
            var contradictoryBoth: BranchOutcome.Contradiction? = null

            // Explore EVERY available bifurcation, not firstOrNull().
            for (choice in binaryChoices(
                puzzle, state.confirmed, state.excluded
            )) {
                if (context.truncated) break

                val first = proveContradiction(
                    puzzle, state.confirmed + choice.first,
                    state.excluded, remainingDepth - 1, context, rules
                )
                val second = if (choice.second != null) {
                    proveContradiction(
                        puzzle, state.confirmed + choice.second,
                        state.excluded, remainingDepth - 1, context, rules
                    )
                } else {
                    proveContradiction(
                        puzzle, state.confirmed,
                        state.excluded + choice.first,
                        remainingDepth - 1, context, rules
                    )
                }
                if (context.truncated) break

                if (first is BranchOutcome.Contradiction &&
                    second is BranchOutcome.Contradiction
                ) {
                    contradictoryBoth = BranchOutcome.Contradiction(
                        maxOf(nestedDepthUsed, 1 + maxOf(
                            first.depth, second.depth
                        ))
                    )
                    break
                }
                // Finding one fully valid completion makes this whole
                // branch satisfiable, but not necessarily unique.
                if (first is BranchOutcome.Solved ||
                    second is BranchOutcome.Solved
                ) {
                    foundSolution = true
                    break
                }

                val forcedDepth = when {
                    first is BranchOutcome.Contradiction ->
                        1 + first.depth
                    second is BranchOutcome.Contradiction ->
                        1 + second.depth
                    else -> continue
                }
                if (forcedDepth < bestDepth) {
                    bestDepth = forcedDepth
                    bestChoice = choice
                    rejectFirst = first is BranchOutcome.Contradiction
                }
            }

            if (context.truncated) break
            if (contradictoryBoth != null) {
                outcome = contradictoryBoth
                break
            }
            if (foundSolution) {
                outcome = BranchOutcome.Solved
                break
            }

            val forced = bestChoice ?: break
            nestedDepthUsed = maxOf(nestedDepthUsed, bestDepth)
            if (rejectFirst) {
                if (forced.second != null) {
                    state.confirmed.add(forced.second)
                } else {
                    state.excluded.add(forced.first)
                }
            } else {
                state.confirmed.add(forced.first)
            }
        }

        if (!context.truncated && outcome is BranchOutcome.Inconclusive &&
            hasContradiction(puzzle, state.confirmed, state.excluded)
        ) {
            outcome = BranchOutcome.Contradiction(nestedDepthUsed)
        }

        // An exhausted search at this exact remaining depth is a
        // definitive no-proof outcome, NOT an interrupted result.
        if (!context.truncated) {
            context.remember(key, outcome)
        }
        return outcome
    }

    private fun stateKey(
        puzzle: Puzzle,
        confirmed: Set<Cell>,
        excluded: Set<Cell>,
        remainingDepth: Int
    ): String {
        val marks = CharArray(puzzle.size * puzzle.size) { '.' }
        for (cell in excluded) {
            marks[cell.index(puzzle.size)] = 'x'
        }
        for (cell in confirmed) {
            val index = cell.index(puzzle.size)
            marks[index] = if (marks[index] == 'x') '!' else 'g'
        }
        return remainingDepth.toString() + ":" + String(marks)
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

        // Even when no row/column/region has exactly two places,
        // a hypothesis can branch on one candidate: present or absent.
        // Include non-paired cells too, so the solver cannot miss a
        // contradiction simply because it picked an unlucky pair.
        val paired = result.flatMap {
            listOfNotNull(it.first, it.second)
        }.toSet()
        for (cell in candidates) {
            if (cell !in paired) {
                result.add(
                    BinaryChoice(
                        first = cell,
                        second = null,
                        sourceCells = setOf(cell),
                        axis = "case (" + (cell.row + 1) +
                            ", " + (cell.col + 1) + ")"
                    )
                )
            }
        }

        return result.distinctBy {
            if (it.second == null) {
                "cell:" + it.first.index(puzzle.size)
            } else {
                "pair:" + listOf(
                    it.first.index(puzzle.size),
                    it.second.index(puzzle.size)
                ).sorted().joinToString("-")
            }
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
        if (confirmed.any { it in excluded }) return true

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

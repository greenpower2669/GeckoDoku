package com.greenpower2669.geckodoku

class GameEngine(val puzzle: Puzzle) {
    private val confirmed = linkedSetOf<Cell>().apply { addAll(puzzle.givens) }
    private val manualCrosses = linkedSetOf<Cell>()
    private val hypotheses = linkedMapOf<Cell, HypothesisMark>()
    private val customMarkers = linkedMapOf<Cell, CustomMarker>()

    var mistakes: Int = 0
        private set

    fun snapshot(): GameSnapshot = GameSnapshot(
        confirmed = confirmed.toSet(),
        givens = puzzle.givens,
        manualCrosses = manualCrosses.toSet(),
        autoCrosses = computeAutoCrosses(),
        hypotheses = hypotheses.toMap(),
        customMarkers = customMarkers.toMap(),
        mistakes = mistakes,
        complete = confirmed.size == puzzle.size
    )

    fun toggleCross(cell: Cell): ActionFeedback {
        if (puzzle.isGiven(cell)) return ActionFeedback.GIVEN_LOCKED
        if (confirmed.contains(cell)) return ActionFeedback.GECKO_PRESENT
        if (computeAutoCrosses().contains(cell)) return ActionFeedback.CROSS_BLOCKED

        hypotheses.remove(cell)
        return if (manualCrosses.remove(cell)) {
            ActionFeedback.CROSS_REMOVED
        } else {
            manualCrosses.add(cell)
            ActionFeedback.CROSS_SET
        }
    }

    fun toggleGecko(cell: Cell): ActionFeedback {
        if (puzzle.isGiven(cell)) return ActionFeedback.GIVEN_LOCKED
        if (confirmed.remove(cell)) {
            hypotheses.remove(cell)
            return ActionFeedback.GECKO_REMOVED
        }
        if (computeAutoCrosses().contains(cell)) return ActionFeedback.CROSS_BLOCKED

        manualCrosses.remove(cell)
        hypotheses.remove(cell)

        if (!puzzle.isSolution(cell)) {
            mistakes += 1
            manualCrosses.add(cell)
            return ActionFeedback.WRONG_GECKO
        }

        confirmed.add(cell)
        return if (confirmed.size == puzzle.size) ActionFeedback.COMPLETED else ActionFeedback.GECKO_CONFIRMED
    }

    fun longPress(cell: Cell): ActionFeedback {
        if (puzzle.isGiven(cell) || confirmed.contains(cell)) return ActionFeedback.GIVEN_LOCKED
        if (computeAutoCrosses().contains(cell)) return ActionFeedback.CROSS_BLOCKED

        manualCrosses.remove(cell)
        val next = when (hypotheses[cell] ?: HypothesisMark.NONE) {
            HypothesisMark.NONE -> HypothesisMark.GHOST_GECKO
            HypothesisMark.GHOST_GECKO -> HypothesisMark.ALERT_GECKO
            HypothesisMark.ALERT_GECKO -> HypothesisMark.NONE
        }
        if (next == HypothesisMark.NONE) hypotheses.remove(cell) else hypotheses[cell] = next
        return ActionFeedback.HYPOTHESIS_CHANGED
    }

    fun placeCustomMarker(cell: Cell, marker: CustomMarker?): ActionFeedback {
        if (marker == null) {
            customMarkers.remove(cell)
            return ActionFeedback.CUSTOM_MARKER_CLEARED
        }
        customMarkers[cell] = marker
        return ActionFeedback.CUSTOM_MARKER_SET
    }

    fun applyProfessorStep(
        step: SolveStep
    ): ActionFeedback {
        step.cell?.let { forced ->
            if (puzzle.isGiven(forced)) {
                return ActionFeedback.GIVEN_LOCKED
            }

            // Safety net only: the logical solver chooses the move.
            // If its conclusion ever disagrees with the unique solution,
            // refuse to mutate the player's grid.
            if (!puzzle.isSolution(forced)) {
                return ActionFeedback.WRONG_GECKO
            }
        }

        step.hypothesisRejected?.let { rejected ->
            if (!puzzle.isGiven(rejected) &&
                !confirmed.contains(rejected)
            ) {
                hypotheses.remove(rejected)
                manualCrosses.add(rejected)
            }
        }

        for (cell in step.eliminated) {
            if (!puzzle.isGiven(cell) &&
                !confirmed.contains(cell)
            ) {
                hypotheses.remove(cell)
                manualCrosses.add(cell)
            }
        }

        step.cell?.let { forced ->
            manualCrosses.remove(forced)
            hypotheses.remove(forced)
            confirmed.add(forced)

            return if (confirmed.size == puzzle.size) {
                ActionFeedback.COMPLETED
            } else {
                ActionFeedback.GECKO_CONFIRMED
            }
        }

        return ActionFeedback.CROSS_SET
    }

    private fun computeAutoCrosses(): Set<Cell> {
        val auto = linkedSetOf<Cell>()
        for (gecko in confirmed) {
            val region = puzzle.regionAt(gecko)
            for (r in 0 until puzzle.size) {
                for (c in 0 until puzzle.size) {
                    val cell = Cell(r, c)
                    if (cell == gecko || confirmed.contains(cell)) continue
                    val sameRow = r == gecko.row
                    val sameCol = c == gecko.col
                    val sameRegion = puzzle.regionAt(cell) == region
                    val touches = kotlin.math.abs(r - gecko.row) <= 1 &&
                        kotlin.math.abs(c - gecko.col) <= 1
                    if (sameRow || sameCol || sameRegion || touches) auto.add(cell)
                }
            }
        }
        return auto
    }
}

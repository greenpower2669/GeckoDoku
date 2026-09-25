package com.greenpower2669.geckodoku

class GameEngine(val puzzle: Puzzle) {
    private val confirmed = linkedSetOf<Cell>()
    private val manualCrosses = linkedSetOf<Cell>()
    private val hypotheses = linkedMapOf<Cell, HypothesisMark>()
    private val customMarkers = linkedMapOf<Cell, CustomMarker>()

    var mistakes: Int = 0
        private set

    fun snapshot(): GameSnapshot {
        return GameSnapshot(
            confirmed = confirmed.toSet(),
            manualCrosses = manualCrosses.toSet(),
            autoCrosses = computeAutoCrosses(),
            hypotheses = hypotheses.toMap(),
            customMarkers = customMarkers.toMap(),
            mistakes = mistakes,
            complete = confirmed.size == puzzle.size
        )
    }

    fun tap(cell: Cell): ActionFeedback {
        if (confirmed.contains(cell)) {
            confirmed.remove(cell)
            hypotheses.remove(cell)
            return ActionFeedback.GECKO_REMOVED
        }
        if (computeAutoCrosses().contains(cell)) return ActionFeedback.CROSS_BLOCKED

        if (manualCrosses.remove(cell)) return tryConfirmGecko(cell)

        manualCrosses.add(cell)
        hypotheses.remove(cell)
        return ActionFeedback.CROSS_SET
    }

    fun longPress(cell: Cell): ActionFeedback {
        if (confirmed.contains(cell)) return ActionFeedback.HYPOTHESIS_CHANGED
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

    private fun tryConfirmGecko(cell: Cell): ActionFeedback {
        if (!puzzle.isSolution(cell)) {
            mistakes += 1
            manualCrosses.add(cell)
            return ActionFeedback.WRONG_GECKO
        }
        confirmed.add(cell)
        hypotheses.remove(cell)
        manualCrosses.remove(cell)
        return if (confirmed.size == puzzle.size) ActionFeedback.COMPLETED else ActionFeedback.GECKO_CONFIRMED
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

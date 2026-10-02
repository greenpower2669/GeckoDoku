package com.greenpower2669.geckodoku

class GameEngine(
    val puzzle: Puzzle
) {
    private val confirmed =
        linkedSetOf<Cell>()
            .apply {
                addAll(
                    puzzle.givens
                )
            }

    private val manualCrosses =
        linkedSetOf<Cell>()

    private val hypothesisTrace =
        HypothesisBranchTrace<Cell>()

    private val customMarkers =
        linkedMapOf<
            Cell,
            CustomMarker
            >()

    private val axisGuides =
        linkedSetOf<
            ClassicAxisGuide
            >()

    var mistakes: Int = 0
        private set

    fun snapshot():
        GameSnapshot {
        val trace =
            hypothesisTrace
                .snapshot()

        val hypothesisMarks =
            trace.nodes
                .associate {
                    node ->

                    node.cell to
                        if (
                            node.state ==
                                HypothesisBranchState
                                    .CONTRADICTION
                        ) {
                            HypothesisMark
                                .ALERT_GECKO
                        } else {
                            HypothesisMark
                                .GHOST_GECKO
                        }
                }

        return GameSnapshot(
            confirmed =
                confirmed.toSet(),
            givens =
                puzzle.givens,
            manualCrosses =
                manualCrosses.toSet(),
            autoCrosses =
                computeAutoCrosses(),
            hypotheses =
                hypothesisMarks,
            customMarkers =
                customMarkers.toMap(),
            axisGuides =
                axisGuides.toSet(),
            mistakes =
                mistakes,
            complete =
                confirmed.size ==
                    puzzle.size,
            hypothesisTrace =
                trace
        )
    }

    fun toggleCross(
        cell: Cell
    ): ActionFeedback {
        if (puzzle.isGiven(cell)) {
            return ActionFeedback
                .GIVEN_LOCKED
        }

        if (confirmed.contains(cell)) {
            return ActionFeedback
                .GECKO_PRESENT
        }

        if (
            computeAutoCrosses()
                .contains(cell)
        ) {
            return ActionFeedback
                .CROSS_BLOCKED
        }

        if (
            hypothesisTrace
                .nodeAt(cell) !=
                null
        ) {
            removeHypothesisBranch(
                cell
            )

            return ActionFeedback
                .HYPOTHESIS_CHANGED
        }

        return when (
            hypothesisTrace
                .toggleCross(cell)
        ) {
            HypothesisCrossChange
                .ADDED -> {
                manualCrosses.add(
                    cell
                )

                ActionFeedback
                    .CROSS_SET
            }

            HypothesisCrossChange
                .REMOVED -> {
                manualCrosses.remove(
                    cell
                )

                ActionFeedback
                    .CROSS_REMOVED
            }

            HypothesisCrossChange
                .BLOCKED ->
                ActionFeedback
                    .CROSS_BLOCKED

            HypothesisCrossChange
                .NO_ACTIVE ->
                if (
                    manualCrosses
                        .remove(cell)
                ) {
                    ActionFeedback
                        .CROSS_REMOVED
                } else {
                    manualCrosses.add(
                        cell
                    )

                    ActionFeedback
                        .CROSS_SET
                }
        }
    }

    fun toggleGecko(
        cell: Cell
    ): ActionFeedback {
        if (puzzle.isGiven(cell)) {
            return ActionFeedback
                .GIVEN_LOCKED
        }

        if (confirmed.remove(cell)) {
            removeHypothesisBranch(
                cell
            )

            return ActionFeedback
                .GECKO_REMOVED
        }

        if (
            computeAutoCrosses()
                .contains(cell)
        ) {
            return ActionFeedback
                .CROSS_BLOCKED
        }

        removeHypothesisBranch(
            cell
        )

        hypothesisTrace
            .removeCross(cell)

        manualCrosses.remove(
            cell
        )

        if (!puzzle.isSolution(cell)) {
            mistakes += 1
            manualCrosses.add(
                cell
            )

            return ActionFeedback
                .WRONG_GECKO
        }

        confirmed.add(
            cell
        )

        return if (
            confirmed.size ==
                puzzle.size
        ) {
            ActionFeedback
                .COMPLETED
        } else {
            ActionFeedback
                .GECKO_CONFIRMED
        }
    }

    fun longPress(
        cell: Cell
    ): ActionFeedback {
        if (
            puzzle.isGiven(cell) ||
            confirmed.contains(cell)
        ) {
            return ActionFeedback
                .GIVEN_LOCKED
        }

        if (
            computeAutoCrosses()
                .contains(cell)
        ) {
            return ActionFeedback
                .CROSS_BLOCKED
        }

        val existing =
            hypothesisTrace
                .nodeAt(cell)

        if (existing == null) {
            manualCrosses.remove(
                cell
            )

            hypothesisTrace
                .removeCross(cell)

            hypothesisTrace
                .startHypothesis(
                    cell
                )
        } else if (
            existing.state ==
                HypothesisBranchState
                    .ACTIVE
        ) {
            hypothesisTrace
                .markContradiction(
                    cell
                )
        } else {
            removeHypothesisBranch(
                cell
            )
        }

        return ActionFeedback
            .HYPOTHESIS_CHANGED
    }

    fun rewindHypothesis(
        id: Int
    ): Boolean {
        val prune =
            hypothesisTrace
                .rewindTo(id)
                ?: return false

        manualCrosses
            .removeAll(
                prune.crossCells
            )

        return true
    }

    fun toggleAxisGuide(
        guide: ClassicAxisGuide
    ) {
        val existing =
            axisGuides
                .firstOrNull {
                    it.kind ==
                        guide.kind &&
                        it.index ==
                            guide.index
                }

        if (existing == guide) {
            axisGuides.remove(
                existing
            )
            return
        }

        if (existing != null) {
            axisGuides.remove(
                existing
            )
        }

        axisGuides.add(
            guide
        )
    }

    fun moveAxisGuide(
        from: ClassicAxisGuide,
        to: ClassicAxisGuide?
    ) {
        axisGuides.remove(
            from
        )

        if (
            to != null &&
            to.index in
                0 until puzzle.size
        ) {
            axisGuides.add(
                to
            )
        }
    }

    fun placeCustomMarker(
        cell: Cell,
        marker: CustomMarker?
    ): ActionFeedback {
        if (marker == null) {
            customMarkers.remove(
                cell
            )

            return ActionFeedback
                .CUSTOM_MARKER_CLEARED
        }

        customMarkers[cell] =
            marker

        return ActionFeedback
            .CUSTOM_MARKER_SET
    }

    fun applyProfessorStep(
        step: SolveStep
    ): ActionFeedback {
        step.cell
            ?.let {
                forced ->

                if (puzzle.isGiven(forced)) {
                    return ActionFeedback
                        .GIVEN_LOCKED
                }

                if (!puzzle.isSolution(forced)) {
                    return ActionFeedback
                        .WRONG_GECKO
                }
            }

        step.hypothesisRejected
            ?.let {
                rejected ->

                if (
                    !puzzle.isGiven(
                        rejected
                    ) &&
                    !confirmed.contains(
                        rejected
                    )
                ) {
                    removeHypothesisBranch(
                        rejected
                    )

                    hypothesisTrace
                        .removeCross(
                            rejected
                        )

                    manualCrosses.add(
                        rejected
                    )
                }
            }

        for (
            cell in
            step.eliminated
        ) {
            if (
                !puzzle.isGiven(cell) &&
                !confirmed.contains(
                    cell
                )
            ) {
                removeHypothesisBranch(
                    cell
                )

                hypothesisTrace
                    .removeCross(
                        cell
                    )

                manualCrosses.add(
                    cell
                )
            }
        }

        step.cell
            ?.let {
                forced ->

                removeHypothesisBranch(
                    forced
                )

                hypothesisTrace
                    .removeCross(
                        forced
                    )

                manualCrosses.remove(
                    forced
                )

                confirmed.add(
                    forced
                )

                return if (
                    confirmed.size ==
                        puzzle.size
                ) {
                    ActionFeedback
                        .COMPLETED
                } else {
                    ActionFeedback
                        .GECKO_CONFIRMED
                }
            }

        return ActionFeedback
            .CROSS_SET
    }

    private fun removeHypothesisBranch(
        cell: Cell
    ) {
        val prune =
            hypothesisTrace
                .removeBranch(
                    cell
                )

        if (
            prune.crossCells
                .isNotEmpty()
        ) {
            manualCrosses
                .removeAll(
                    prune.crossCells
                )
        }
    }

    private fun computeAutoCrosses():
        Set<Cell> {
        val auto =
            linkedSetOf<Cell>()

        for (gecko in confirmed) {
            val region =
                puzzle.regionAt(
                    gecko
                )

            for (
                r in
                0 until puzzle.size
            ) {
                for (
                    c in
                    0 until puzzle.size
                ) {
                    val cell =
                        Cell(
                            r,
                            c
                        )

                    if (
                        cell ==
                            gecko ||
                        confirmed.contains(
                            cell
                        )
                    ) {
                        continue
                    }

                    val sameRow =
                        r ==
                            gecko.row

                    val sameCol =
                        c ==
                            gecko.col

                    val sameRegion =
                        puzzle.regionAt(
                            cell
                        ) ==
                            region

                    val touches =
                        kotlin.math.abs(
                            r -
                                gecko.row
                        ) <=
                            1 &&
                            kotlin.math.abs(
                                c -
                                    gecko.col
                            ) <=
                            1

                    if (
                        sameRow ||
                        sameCol ||
                        sameRegion ||
                        touches
                    ) {
                        auto.add(
                            cell
                        )
                    }
                }
            }
        }

        return auto
    }
}

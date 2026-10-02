package com.greenpower2669.geckodoku

import java.util.ArrayDeque

class SudokuGameEngine(
    val puzzle: SudokuPuzzle
) {
    private var values =
        puzzle.givens.copyOf()

    private var notes:
        MutableList<MutableSet<Int>> =
        MutableList(
            SudokuPuzzle.CELL_COUNT
        ) {
            linkedSetOf<Int>()
        }

    private var geckoMarkers =
        BooleanArray(
            SudokuPuzzle.CELL_COUNT
        )

    private var customMarkers =
        linkedMapOf<
            Int,
            CustomMarker
        >()

    private var hypothesisTrace =
        HypothesisBranchTrace<
            SudokuHypothesisChoice
            >()

    private val givenMask =
        BooleanArray(
            SudokuPuzzle.CELL_COUNT
        ) {
            puzzle.givens[it] != 0
        }

    private val undoStack =
        ArrayDeque<EngineState>()

    private val redoStack =
        ArrayDeque<EngineState>()

    var mistakes: Int = 0
        private set

    private var lastMoveOrigin:
        SudokuMoveOrigin? = null

    fun snapshot(): SudokuSnapshot =
        SudokuSnapshot(
            values = values.copyOf(),
            notes =
                notes.map {
                    it.toSet()
                },
            geckoMarkers =
                geckoMarkers.copyOf(),
            givens =
                givenMask.copyOf(),
            mistakes = mistakes,
            complete =
                values.contentEquals(
                    puzzle.solution
                ),
            lastMoveOrigin =
                lastMoveOrigin,
            customMarkers =
                customMarkers.mapKeys {
                    (index, _) ->
                    Cell(
                        index /
                            SudokuPuzzle.SIZE,
                        index %
                            SudokuPuzzle.SIZE
                    )
                },
            hypothesisTrace =
                hypothesisTrace
                    .snapshot()
        )

    fun nextHypothesisColor():
        HypothesisColor =
        hypothesisTrace
            .nextColor()

    fun enterDigit(
        cell: Cell,
        digit: Int,
        notesMode: Boolean = false,
        origin:
            SudokuMoveOrigin =
            SudokuMoveOrigin.PLAYER
    ): SudokuActionFeedback {
        require(digit in 1..9)

        val index =
            puzzle.indexOf(cell)

        if (givenMask[index]) {
            return SudokuActionFeedback
                .GIVEN_LOCKED
        }

        if (notesMode) {
            if (values[index] != 0) {
                return SudokuActionFeedback
                    .NOTHING_CHANGED
            }

            pushUndo()

            if (!notes[index].add(digit)) {
                notes[index].remove(digit)
            }

            lastMoveOrigin =
                origin

            return SudokuActionFeedback
                .NOTE_TOGGLED
        }

        if (
            digit !=
                puzzle.solution[index]
        ) {
            mistakes += 1
            return SudokuActionFeedback
                .WRONG_VALUE
        }

        if (values[index] == digit) {
            return SudokuActionFeedback
                .NOTHING_CHANGED
        }

        pushUndo()

        removeHypothesisAtCell(
            cell
        )

        values[index] = digit
        notes[index].clear()
        geckoMarkers[index] = false
        customMarkers.remove(index)
        lastMoveOrigin =
            origin
        removePeerNote(
            cell,
            digit
        )

        return if (
            values.contentEquals(
                puzzle.solution
            )
        ) {
            SudokuActionFeedback
                .COMPLETED
        } else {
            SudokuActionFeedback
                .VALUE_SET
        }
    }

    fun cycleHypothesis(
        cell: Cell,
        digit: Int
    ): SudokuActionFeedback {
        require(digit in 1..9)

        val index =
            puzzle.indexOf(cell)

        if (givenMask[index]) {
            return SudokuActionFeedback
                .GIVEN_LOCKED
        }

        if (values[index] != 0) {
            return SudokuActionFeedback
                .NOTHING_CHANGED
        }

        pushUndo()

        val current =
            hypothesisTrace
                .snapshot()
                .nodes
                .filter {
                    it.cell.cell ==
                        cell
                }
                .maxByOrNull {
                    it.order
                }

        val choice =
            SudokuHypothesisChoice(
                cell =
                    cell,
                digit =
                    digit
            )

        if (
            current != null &&
            current.cell.digit !=
                digit
        ) {
            hypothesisTrace
                .removeBranch(
                    current.cell
                )

            hypothesisTrace
                .startHypothesis(
                    choice
                )
        } else if (
            current == null
        ) {
            hypothesisTrace
                .startHypothesis(
                    choice
                )
        } else if (
            current.state ==
                HypothesisBranchState
                    .ACTIVE
        ) {
            hypothesisTrace
                .markContradiction(
                    current.cell
                )
        } else {
            hypothesisTrace
                .removeBranch(
                    current.cell
                )
        }

        lastMoveOrigin =
            SudokuMoveOrigin.PLAYER

        return SudokuActionFeedback
            .HYPOTHESIS_CHANGED
    }

    fun rewindHypothesis(
        id: Int
    ): Boolean {
        val snapshot =
            hypothesisTrace
                .snapshot()

        if (
            snapshot.nodeById(id) ==
                null
        ) {
            return false
        }

        pushUndo()

        hypothesisTrace
            .rewindTo(id)

        lastMoveOrigin =
            SudokuMoveOrigin.PLAYER

        return true
    }

    fun toggleGeckoMarker(
        cell: Cell
    ): SudokuActionFeedback {
        val index =
            puzzle.indexOf(cell)

        if (givenMask[index]) {
            return SudokuActionFeedback
                .GIVEN_LOCKED
        }

        if (values[index] != 0) {
            return SudokuActionFeedback
                .NOTHING_CHANGED
        }

        pushUndo()

        geckoMarkers[index] =
            !geckoMarkers[index]

        lastMoveOrigin =
            SudokuMoveOrigin.PLAYER

        return SudokuActionFeedback
            .MARKER_TOGGLED
    }

    fun setCustomMarker(
        cell: Cell,
        marker: CustomMarker?
    ): SudokuActionFeedback {
        val index =
            puzzle.indexOf(cell)

        if (givenMask[index]) {
            return SudokuActionFeedback
                .GIVEN_LOCKED
        }

        if (values[index] != 0) {
            return SudokuActionFeedback
                .NOTHING_CHANGED
        }

        if (
            customMarkers[index] ==
                marker
        ) {
            return SudokuActionFeedback
                .NOTHING_CHANGED
        }

        pushUndo()

        if (marker == null) {
            customMarkers.remove(index)
            lastMoveOrigin =
                SudokuMoveOrigin.PLAYER

            return SudokuActionFeedback
                .PERSONAL_MARKER_CLEARED
        }

        customMarkers[index] =
            marker
        lastMoveOrigin =
            SudokuMoveOrigin.PLAYER

        return SudokuActionFeedback
            .PERSONAL_MARKER_SET
    }

    fun erase(
        cell: Cell
    ): SudokuActionFeedback {
        val index =
            puzzle.indexOf(cell)

        if (givenMask[index]) {
            return SudokuActionFeedback
                .GIVEN_LOCKED
        }

        val hasHypothesis =
            snapshot()
                .hypothesisAt(
                    cell
                ) !=
                null

        if (
            values[index] == 0 &&
            notes[index].isEmpty() &&
            !geckoMarkers[index] &&
            !customMarkers
                .containsKey(index) &&
            !hasHypothesis
        ) {
            return SudokuActionFeedback
                .NOTHING_CHANGED
        }

        pushUndo()
        values[index] = 0
        notes[index].clear()
        geckoMarkers[index] = false
        customMarkers.remove(index)
        removeHypothesisAtCell(
            cell
        )
        lastMoveOrigin =
            SudokuMoveOrigin.PLAYER

        return SudokuActionFeedback
            .ERASED
    }

    fun undo(): SudokuActionFeedback {
        if (undoStack.isEmpty()) {
            return SudokuActionFeedback
                .NOTHING_CHANGED
        }

        redoStack.addLast(
            capture()
        )

        restore(
            undoStack.removeLast()
        )

        return SudokuActionFeedback
            .UNDONE
    }

    fun redo(): SudokuActionFeedback {
        if (redoStack.isEmpty()) {
            return SudokuActionFeedback
                .NOTHING_CHANGED
        }

        undoStack.addLast(
            capture()
        )

        restore(
            redoStack.removeLast()
        )

        return SudokuActionFeedback
            .REDONE
    }

    private fun removeHypothesisAtCell(
        cell: Cell
    ) {
        val current =
            hypothesisTrace
                .snapshot()
                .nodes
                .filter {
                    it.cell.cell ==
                        cell
                }
                .maxByOrNull {
                    it.order
                }
                ?: return

        hypothesisTrace
            .removeBranch(
                current.cell
            )
    }

    private fun pushUndo() {
        undoStack.addLast(
            capture()
        )

        while (
            undoStack.size >
                MAX_HISTORY
        ) {
            undoStack.removeFirst()
        }

        redoStack.clear()
    }

    private fun capture(): EngineState =
        EngineState(
            values = values.copyOf(),
            notes =
                notes.map {
                    it.toSet()
                },
            geckoMarkers =
                geckoMarkers.copyOf(),
            customMarkers =
                customMarkers.toMap(),
            hypothesisTrace =
                hypothesisTrace
                    .snapshot(),
            lastMoveOrigin =
                lastMoveOrigin
        )

    private fun restore(
        state: EngineState
    ) {
        values =
            state.values.copyOf()

        notes =
            state.notes.map {
                it.toMutableSet()
            }.toMutableList()

        geckoMarkers =
            state.geckoMarkers.copyOf()

        customMarkers =
            linkedMapOf<Int, CustomMarker>()
                .apply {
                    putAll(
                        state.customMarkers
                    )
                }

        hypothesisTrace =
            HypothesisBranchTrace(
                state.hypothesisTrace
            )

        lastMoveOrigin =
            state.lastMoveOrigin
    }

    private fun removePeerNote(
        cell: Cell,
        digit: Int
    ) {
        for (
            row in 0 until
                SudokuPuzzle.SIZE
        ) {
            for (
                col in 0 until
                    SudokuPuzzle.SIZE
            ) {
                val sameRow =
                    row == cell.row

                val sameCol =
                    col == cell.col

                val sameBox =
                    row / 3 ==
                        cell.row / 3 &&
                        col / 3 ==
                        cell.col / 3

                if (
                    !sameRow &&
                    !sameCol &&
                    !sameBox
                ) {
                    continue
                }

                val index =
                    row *
                        SudokuPuzzle.SIZE +
                        col

                notes[index]
                    .remove(digit)
            }
        }
    }

    private data class EngineState(
        val values: IntArray,
        val notes: List<Set<Int>>,
        val geckoMarkers: BooleanArray,
        val customMarkers:
            Map<Int, CustomMarker>,
        val hypothesisTrace:
            HypothesisTraceSnapshot<
                SudokuHypothesisChoice
                >,
        val lastMoveOrigin:
            SudokuMoveOrigin?
    )

    companion object {
        private const val MAX_HISTORY =
            200
    }
}

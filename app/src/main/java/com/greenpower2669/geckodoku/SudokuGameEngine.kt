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
                }
        )

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

        if (
            values[index] == 0 &&
            notes[index].isEmpty() &&
            !geckoMarkers[index] &&
            !customMarkers
                .containsKey(index)
        ) {
            return SudokuActionFeedback
                .NOTHING_CHANGED
        }

        pushUndo()
        values[index] = 0
        notes[index].clear()
        geckoMarkers[index] = false
        customMarkers.remove(index)
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
        val lastMoveOrigin:
            SudokuMoveOrigin?
    )

    companion object {
        private const val MAX_HISTORY =
            200
    }
}

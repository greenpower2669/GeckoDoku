package com.greenpower2669.geckodoku

enum class SudokuCellTapAction {
    TOGGLE_GECKO_MARKER,
    SELECT_ONLY
}

class SudokuCellTapPolicy {
    fun actionFor(
        snapshot: SudokuSnapshot,
        cell: Cell
    ): SudokuCellTapAction =
        if (
            snapshot.isGiven(cell) ||
            snapshot.valueAt(cell) != 0
        ) {
            SudokuCellTapAction
                .SELECT_ONLY
        } else {
            SudokuCellTapAction
                .TOGGLE_GECKO_MARKER
        }
}

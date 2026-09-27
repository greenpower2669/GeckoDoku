package com.greenpower2669.geckodoku

enum class SudokuGesture {
    SINGLE_TAP,
    DOUBLE_TAP,
    LONG_PRESS
}

enum class SudokuGestureAction {
    SELECT_OR_TOGGLE_GECKO,
    OPEN_PERSONAL_MARKERS,
    OPEN_INPUT_PALETTE
}

class SudokuGesturePolicy {
    fun actionFor(
        gesture: SudokuGesture
    ): SudokuGestureAction =
        when (gesture) {
            SudokuGesture.SINGLE_TAP ->
                SudokuGestureAction
                    .SELECT_OR_TOGGLE_GECKO
            SudokuGesture.DOUBLE_TAP ->
                SudokuGestureAction
                    .OPEN_PERSONAL_MARKERS
            SudokuGesture.LONG_PRESS ->
                SudokuGestureAction
                    .OPEN_INPUT_PALETTE
        }
}

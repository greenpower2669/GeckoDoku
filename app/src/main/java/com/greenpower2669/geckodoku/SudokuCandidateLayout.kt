package com.greenpower2669.geckodoku

data class CandidateSlot(
    val row: Int,
    val col: Int,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

object SudokuCandidateLayout {
    fun slot(
        digit: Int
    ): CandidateSlot {
        require(digit in 1..9)

        val zero =
            digit - 1

        val row =
            zero / 3

        val col =
            zero % 3

        return CandidateSlot(
            row = row,
            col = col,
            left = col / 3f,
            top = row / 3f,
            right = (col + 1) / 3f,
            bottom = (row + 1) / 3f
        )
    }
}

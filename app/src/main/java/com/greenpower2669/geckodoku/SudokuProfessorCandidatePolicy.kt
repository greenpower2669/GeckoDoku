package com.greenpower2669.geckodoku

class SudokuProfessorCandidatePolicy {
    val affectsPlayerUndoHistory:
        Boolean = false

    fun candidatesFor(
        values: IntArray,
        cell: Cell
    ): Set<Int> {
        val index =
            cell.row *
                SudokuPuzzle.SIZE +
                cell.col

        return SudokuSolver
            .digitsFromMask(
                SudokuSolver
                    .candidateMask(
                        values,
                        index
                    )
            )
            .toSet()
    }
}

package com.greenpower2669.geckodoku

data class SudokuPuzzle(
    val solution: IntArray,
    val givens: IntArray,
    val difficulty: GameDifficulty,
    val seed: Long
) {
    init {
        require(solution.size == CELL_COUNT)
        require(givens.size == CELL_COUNT)
        require(
            SudokuSolver
                .isCompleteValidGrid(
                    solution
                )
        )

        for (i in 0 until CELL_COUNT) {
            val given = givens[i]
            require(given in 0..9)

            if (given != 0) {
                require(
                    given == solution[i]
                )
            }
        }
    }

    fun indexOf(
        cell: Cell
    ): Int {
        require(cell.row in 0..8)
        require(cell.col in 0..8)
        return cell.row * SIZE +
            cell.col
    }

    fun customMarkerAt(
        cell: Cell
    ): CustomMarker? =
        customMarkers[cell]

    fun isGiven(
        cell: Cell
    ): Boolean =
        givens[indexOf(cell)] != 0

    fun givenCount(): Int =
        givens.count {
            it != 0
        }

    companion object {
        const val SIZE = 9
        const val CELL_COUNT = 81
    }
}

enum class SudokuMoveOrigin {
    PLAYER,
    PROFESSOR
}

enum class SudokuActionFeedback {
    VALUE_SET,
    NOTE_TOGGLED,
    MARKER_TOGGLED,
    PERSONAL_MARKER_SET,
    PERSONAL_MARKER_CLEARED,
    ERASED,
    WRONG_VALUE,
    GIVEN_LOCKED,
    COMPLETED,
    UNDONE,
    REDONE,
    NOTHING_CHANGED
}

data class SudokuSnapshot(
    val values: IntArray,
    val notes: List<Set<Int>>,
    val geckoMarkers: BooleanArray,
    val givens: BooleanArray,
    val mistakes: Int,
    val complete: Boolean,
    val lastMoveOrigin:
        SudokuMoveOrigin? = null,
    val customMarkers:
        Map<Cell, CustomMarker> =
        emptyMap()
) {
    fun valueAt(
        cell: Cell
    ): Int =
        values[
            cell.row *
                SudokuPuzzle.SIZE +
                cell.col
        ]

    fun notesAt(
        cell: Cell
    ): Set<Int> =
        notes[
            cell.row *
                SudokuPuzzle.SIZE +
                cell.col
        ]

    fun hasGeckoMarker(
        cell: Cell
    ): Boolean =
        geckoMarkers[
            cell.row *
                SudokuPuzzle.SIZE +
                cell.col
        ]

    fun isGiven(
        cell: Cell
    ): Boolean =
        givens[
            cell.row *
                SudokuPuzzle.SIZE +
                cell.col
        ]
}

enum class SudokuTechnique(
    val label: String
) {
    NAKED_SINGLE(
        "candidat unique"
    ),
    HIDDEN_SINGLE_ROW(
        "chiffre unique dans la ligne"
    ),
    HIDDEN_SINGLE_COLUMN(
        "chiffre unique dans la colonne"
    ),
    HIDDEN_SINGLE_BOX(
        "chiffre unique dans le bloc"
    )
}

data class SudokuHint(
    val technique: SudokuTechnique,
    val cell: Cell,
    val digit: Int,
    val explanation: String,
    val reasoning:
        SudokuReasoningTrace? = null
)

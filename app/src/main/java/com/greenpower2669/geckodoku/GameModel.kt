package com.greenpower2669.geckodoku

data class Cell(val row: Int, val col: Int) {
    fun index(size: Int): Int = row * size + col
}

data class Puzzle(
    val id: String,
    val size: Int,
    val regions: IntArray,
    val solutionCols: IntArray
) {
    init {
        require(regions.size == size * size)
        require(solutionCols.size == size)
    }

    fun regionAt(cell: Cell): Int = regions[cell.index(size)]
    fun isSolution(cell: Cell): Boolean = solutionCols[cell.row] == cell.col

    companion object {
        fun demo5x5(): Puzzle = Puzzle(
            id = "demo-5x5-unique-001",
            size = 5,
            regions = intArrayOf(
                0, 0, 0, 1, 2,
                0, 1, 1, 1, 2,
                0, 1, 3, 2, 2,
                3, 3, 3, 2, 4,
                3, 4, 4, 4, 4
            ),
            solutionCols = intArrayOf(0, 2, 4, 1, 3)
        )
    }
}

enum class HypothesisMark { NONE, GHOST_GECKO, ALERT_GECKO }

enum class CustomMarker(val label: String, val symbol: String) {
    EXCLAMATION("Important", "!"),
    QUESTION("À revoir", "?"),
    STAR("Bonne piste", "★"),
    TARGET("Cible", "◎"),
    CIRCLE("Cercle", "●"),
    DIAMOND("Losange", "◆"),
    ARROW("Direction", "↗"),
    PLUS("Plus", "+"),
    EYE("Regarder", "◉"),
    RAINBOW_GECKO("Gecko arc-en-ciel", "🦎")
}

enum class ActionFeedback {
    CROSS_SET,
    CROSS_BLOCKED,
    GECKO_CONFIRMED,
    GECKO_REMOVED,
    WRONG_GECKO,
    HYPOTHESIS_CHANGED,
    CUSTOM_MARKER_SET,
    CUSTOM_MARKER_CLEARED,
    COMPLETED
}

data class GameSnapshot(
    val confirmed: Set<Cell>,
    val manualCrosses: Set<Cell>,
    val autoCrosses: Set<Cell>,
    val hypotheses: Map<Cell, HypothesisMark>,
    val customMarkers: Map<Cell, CustomMarker>,
    val mistakes: Int,
    val complete: Boolean
)

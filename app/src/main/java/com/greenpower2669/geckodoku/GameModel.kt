package com.greenpower2669.geckodoku

data class Cell(val row: Int, val col: Int) {
    fun index(size: Int): Int = row * size + col
}

enum class GameDifficulty(val label: String) {
    DISCOVERY("Découverte"),
    EASY("Facile"),
    THINKING("Réflexion"),
    HARD("Difficile"),
    EXPERT("Expert"),
    DEMENTIAL("Démentiel");

    fun minimumGivens(size: Int): Int = when (this) {
        DISCOVERY -> maxOf(2, size / 2)
        EASY -> maxOf(1, size / 3)
        THINKING -> maxOf(1, size / 5)
        HARD -> 0
        EXPERT -> 0
        DEMENTIAL -> 0
    }
}

data class Puzzle(
    val id: String,
    val size: Int,
    val regions: IntArray,
    val solutionCols: IntArray,
    val givens: Set<Cell> = emptySet(),
    val difficulty: GameDifficulty = GameDifficulty.EASY,
    val seed: Long = 0L
) {
    init {
        require(size in 5..12)
        require(regions.size == size * size)
        require(solutionCols.size == size)
        require(regions.toSet().size == size)
    }

    fun regionAt(cell: Cell): Int = regions[cell.index(size)]
    fun isSolution(cell: Cell): Boolean = solutionCols[cell.row] == cell.col
    fun isGiven(cell: Cell): Boolean = givens.contains(cell)
    fun solutionCells(): List<Cell> = solutionCols.mapIndexed { row, col -> Cell(row, col) }
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
    CROSS_REMOVED,
    CROSS_BLOCKED,
    GECKO_PRESENT,
    GECKO_CONFIRMED,
    GECKO_REMOVED,
    GIVEN_LOCKED,
    WRONG_GECKO,
    HYPOTHESIS_CHANGED,
    CUSTOM_MARKER_SET,
    CUSTOM_MARKER_CLEARED,
    COMPLETED
}

data class GameSnapshot(
    val confirmed: Set<Cell>,
    val givens: Set<Cell>,
    val manualCrosses: Set<Cell>,
    val autoCrosses: Set<Cell>,
    val hypotheses: Map<Cell, HypothesisMark>,
    val customMarkers: Map<Cell, CustomMarker>,
    val mistakes: Int,
    val complete: Boolean
)

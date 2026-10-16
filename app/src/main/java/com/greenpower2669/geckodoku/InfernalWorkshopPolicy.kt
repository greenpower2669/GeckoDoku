package com.greenpower2669.geckodoku

/**
 * The Classic grid has one region/colour and one Gecko per row.
 * A/B experiment: X-Wing is NEVER required to qualify any board.
 * The solver can still find and explain this technique naturally.
 *
 * A candidate remains unverified until the player tests it.
 */
object InfernalWorkshopPolicy {
    const val MIN_SIZE = 5
    const val MAX_SIZE = 12
    const val MIN_NESTED_DEPTH = 2
    const val MAX_SEARCH_DEPTH = 4

    val sizes: IntRange = MIN_SIZE..MAX_SIZE

    // No mandatory X-Wing gate for any size in this experiment.
    fun qualifies(size: Int, depth: Int, xWingCount: Int): Boolean =
        size in sizes &&
            depth in MIN_NESTED_DEPTH..MAX_SEARCH_DEPTH &&
            xWingCount >= 0

    fun nextSize(size: Int): Int {
        require(size in sizes)
        return if (size == MAX_SIZE) MIN_SIZE else size + 1
    }
}

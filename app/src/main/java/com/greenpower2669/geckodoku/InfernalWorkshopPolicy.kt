package com.greenpower2669.geckodoku

/**
 * The Classic grid has one region/colour and one Gecko per row.
 * Small grids are allowed to show deep dependent hypotheses without
 * an artificial X-Wing gate; X-Wing is required for larger candidates.
 *
 * A candidate remains unverified until the player tests it.
 */
object InfernalWorkshopPolicy {
    const val MIN_SIZE = 5
    const val MAX_SIZE = 12
    const val MIN_NESTED_DEPTH = 2
    const val MAX_SEARCH_DEPTH = 4

    val sizes: IntRange = MIN_SIZE..MAX_SIZE

    fun requiresXWing(size: Int): Boolean {
        require(size in sizes)
        return size >= 8
    }

    fun qualifies(size: Int, depth: Int, xWingCount: Int): Boolean =
        size in sizes &&
            depth in MIN_NESTED_DEPTH..MAX_SEARCH_DEPTH &&
            xWingCount >= 0 &&
            (!requiresXWing(size) || xWingCount >= 1)

    fun nextSize(size: Int): Int {
        require(size in sizes)
        return if (size == MAX_SIZE) MIN_SIZE else size + 1
    }
}

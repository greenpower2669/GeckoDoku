package com.greenpower2669.geckodoku

/**
 * Classic workshop puzzle contract.
 *
 * H2..H4 means genuinely nested hypotheses, not separate guesses.
 * The Gecko X-Wing is required in the proof for boards 8x8..12x12;
 * it remains optional for 5x5..7x7. A generated candidate still
 * requires the player's test and explicit manual approval.
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

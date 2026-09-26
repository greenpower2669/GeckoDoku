package com.greenpower2669.geckodoku

class EncouragementSelector {
    private var lastIndex: Int? = null
    val totalCount: Int get() = 13

    fun eligibleIndices(remaining: Int): List<Int> =
        (0 until totalCount).filter {
            it != NEAR_END_INDEX ||
                remaining in 1..2
        }

    fun choose(
        remaining: Int,
        randomValue: Int
    ): Int {
        val eligible = eligibleIndices(remaining)
        require(eligible.isNotEmpty())
        var position =
            Math.floorMod(
                randomValue,
                eligible.size
            )
        var selected = eligible[position]
        if (
            eligible.size > 1 &&
            selected == lastIndex
        ) {
            position =
                (position + 1) %
                    eligible.size
            selected = eligible[position]
        }
        lastIndex = selected
        return selected
    }

    fun reset() {
        lastIndex = null
    }

    companion object {
        private const val NEAR_END_INDEX = 7
    }
}

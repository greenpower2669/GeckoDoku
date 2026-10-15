package com.greenpower2669.geckodoku

/** An Infernal proof is a *nested* reasoning depth, never a guess count. */
object InfernalDepthPolicy {
    const val MINIMUM_DEPTH = 3
    const val SEARCH_DEPTH = 3

    fun accepts(solved: Boolean, depth: Int): Boolean =
        solved && depth >= MINIMUM_DEPTH
}

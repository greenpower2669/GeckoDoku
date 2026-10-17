package com.greenpower2669.geckodoku

/**
 * An archive of DEFINITIVE H2-H4 subtree results, scoped by the exact puzzle
 * and the solver rule set. An unfinished subtree is never cached as success.
 *
 * The recursion stack itself is reconstructed from the same deterministic
 * choices on resume, then already proved subtrees are skipped via this map.
 */
interface WorkshopProofMemoArchive {
    fun load(puzzle: Puzzle): MutableMap<String, String>
    fun persist(puzzle: Puzzle, entries: Map<String, String>)
    fun clear()
}

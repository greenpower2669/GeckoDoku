package com.greenpower2669.geckodoku

import android.content.Context
import org.json.JSONObject

/**
 * Synchronous, atomic Android SharedPreferences checkpoint. A process killed
 * after commit resumes the same board/seed/removal index instead of restarting.
 * The in-progress H2-H4 recursion is not serialized by this store.
 */
class WorkshopCheckpointStore(context: Context) : WorkshopProofMemoArchive {
    private val prefs = context.applicationContext.getSharedPreferences(
        "infernal_workshop_deletion_checkpoint_v6", Context.MODE_PRIVATE
    )

    fun load(): WorkshopDeletionProgress? =
        prefs.getString("cursor", null)?.let(WorkshopDeletionProgress::decode)

    fun save(state: WorkshopDeletionProgress) {
        check(prefs.edit().putString("cursor", state.encode()).commit()) {
            "Impossible de sauvegarder le curseur de recherche Infernal"
        }
    }

    override fun clear() {
        check(prefs.edit().remove("cursor")
            .remove("proof_key").remove("proof_cache").commit()) {
            "Impossible d'effacer le curseur de recherche terminé"
        }
    }

    private fun puzzleKey(puzzle: Puzzle): String =
        puzzle.id + ":" + puzzle.size + ":" +
            puzzle.givens.map { it.index(puzzle.size) }.sorted().joinToString(",")

    override fun load(puzzle: Puzzle): MutableMap<String, String> {
        val key = puzzleKey(puzzle)
        if (prefs.getString("proof_key", null) != key) return mutableMapOf()
        return try {
            val json = JSONObject(prefs.getString("proof_cache", "{}") ?: "{}")
            val result = mutableMapOf<String, String>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val next = keys.next()
                val value = json.optString(next, "")
                // Only definitive subtree outcomes can ever be restored.
                if (value == "S" || value == "I" ||
                    (value.startsWith("C") && value.drop(1).toIntOrNull() != null)
                ) {
                    result[next] = value
                }
            }
            result
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    override fun persist(puzzle: Puzzle, entries: Map<String, String>) {
        val json = JSONObject()
        for ((key, value) in entries) json.put(key, value)
        check(prefs.edit()
            .putString("proof_key", puzzleKey(puzzle))
            .putString("proof_cache", json.toString())
            .commit()) { "Impossible de sauvegarder les branches prouvées Infernal" }
    }
}

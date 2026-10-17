package com.greenpower2669.geckodoku

import android.content.Context

/**
 * Synchronous, atomic Android SharedPreferences checkpoint. A process killed
 * after commit resumes the same board/seed/removal index instead of restarting.
 * The in-progress H2-H4 recursion is not serialized by this store.
 */
class WorkshopCheckpointStore(context: Context) {
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

    fun clear() {
        check(prefs.edit().remove("cursor").commit()) {
            "Impossible d'effacer le curseur de recherche terminé"
        }
    }
}

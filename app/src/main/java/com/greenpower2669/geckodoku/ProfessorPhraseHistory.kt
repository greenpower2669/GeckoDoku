package com.greenpower2669.geckodoku

import android.content.Context

interface ProfessorPhraseHistoryStorage {
    fun readLastUsedAt(id: String): Long?
    fun writeLastUsedAt(
        id: String,
        timestampMs: Long
    )
    fun readLastPhraseId(): String?
    fun writeLastPhraseId(id: String?)
}

class ProfessorPhraseHistory(
    private val clock: ProfessorClock,
    private val storage:
        ProfessorPhraseHistoryStorage,
    val cooldownMs: Long =
        48L * 60L * 60L * 1000L
) {
    val lastPhraseId: String?
        get() =
            storage.readLastPhraseId()

    fun lastUsedAt(id: String): Long? =
        storage.readLastUsedAt(id)

    fun isAvailable(id: String): Boolean {
        val usedAt =
            storage.readLastUsedAt(id)
                ?: return true

        return (
            clock.nowMs() -
                usedAt
            ) >= cooldownMs
    }

    fun markUsed(id: String) {
        storage.writeLastUsedAt(
            id,
            clock.nowMs()
        )
        storage.writeLastPhraseId(id)
    }
}

class SharedPreferencesProfessorPhraseHistoryStorage(
    context: Context
) : ProfessorPhraseHistoryStorage {
    private val prefs =
        context.getSharedPreferences(
            "geckodoku_professor_phrase_history",
            Context.MODE_PRIVATE
        )

    override fun readLastUsedAt(
        id: String
    ): Long? {
        val key = usedKey(id)
        if (!prefs.contains(key)) {
            return null
        }
        return prefs.getLong(key, 0L)
    }

    override fun writeLastUsedAt(
        id: String,
        timestampMs: Long
    ) {
        prefs.edit()
            .putLong(
                usedKey(id),
                timestampMs
            )
            .apply()
    }

    override fun readLastPhraseId():
        String? =
        prefs.getString(
            LAST_ID_KEY,
            null
        )

    override fun writeLastPhraseId(
        id: String?
    ) {
        val edit = prefs.edit()

        if (id == null) {
            edit.remove(LAST_ID_KEY)
        } else {
            edit.putString(
                LAST_ID_KEY,
                id
            )
        }

        edit.apply()
    }

    private fun usedKey(id: String): String =
        "used_" + id

    companion object {
        private const val LAST_ID_KEY =
            "last_phrase_id"
    }
}

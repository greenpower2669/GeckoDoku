package com.greenpower2669.geckodoku

import java.io.File
import org.json.JSONArray
import org.json.JSONObject

enum class PendingScoreState {
    PENDING,
    BLOCKED
}

data class PendingScoreEntry(
    val runId: String,
    val payloadJson: String,
    val addedAt: Long,
    val attempts: Int = 0,
    val nextAttemptAt: Long = 0L,
    val state: PendingScoreState =
        PendingScoreState.PENDING,
    val lastHttpStatus: Int? = null,
    val lastErrorCode: String? = null
)

class PendingScoreStore(
    directory: File,
    private val maxEntries: Int = 1000
) {
    private val atomicStore =
        AtomicJsonFileStore(
            File(directory, FILE_NAME)
        )

    private var current:
        List<PendingScoreEntry> =
        load()

    @Synchronized
    fun enqueue(
        runId: String,
        payloadJson: String,
        now: Long
    ): Boolean {
        val existing =
            current.firstOrNull {
                it.runId == runId
            }

        if (existing != null) {
            return existing.payloadJson ==
                payloadJson
        }

        if (current.size >= maxEntries) {
            return false
        }

        val next =
            current +
                PendingScoreEntry(
                    runId = runId,
                    payloadJson = payloadJson,
                    addedAt = now
                )

        return replaceIfPersisted(next)
    }

    @Synchronized
    fun entries():
        List<PendingScoreEntry> =
        current.toList()

    @Synchronized
    fun find(
        runId: String
    ): PendingScoreEntry? =
        current.firstOrNull {
            it.runId == runId
        }

    @Synchronized
    fun markRetry(
        runId: String,
        nextAttemptAt: Long,
        httpStatus: Int? = null,
        errorCode: String? = null
    ): Boolean =
        update(runId) {
            entry ->
            entry.copy(
                attempts = entry.attempts + 1,
                nextAttemptAt = nextAttemptAt,
                state = PendingScoreState.PENDING,
                lastHttpStatus = httpStatus,
                lastErrorCode = errorCode
            )
        }

    @Synchronized
    fun markBlocked(
        runId: String,
        httpStatus: Int? = null,
        errorCode: String? = null
    ): Boolean =
        update(runId) {
            entry ->
            entry.copy(
                attempts = entry.attempts + 1,
                state = PendingScoreState.BLOCKED,
                lastHttpStatus = httpStatus,
                lastErrorCode = errorCode
            )
        }

    @Synchronized
    fun remove(
        runId: String
    ): Boolean {
        if (current.none { it.runId == runId }) {
            return false
        }

        return replaceIfPersisted(
            current.filterNot {
                it.runId == runId
            }
        )
    }

    private fun update(
        runId: String,
        transform:
            (PendingScoreEntry) ->
            PendingScoreEntry
    ): Boolean {
        var found = false

        val next =
            current.map {
                entry ->
                if (entry.runId == runId) {
                    found = true
                    transform(entry)
                } else {
                    entry
                }
            }

        return found &&
            replaceIfPersisted(next)
    }

    private fun replaceIfPersisted(
        next: List<PendingScoreEntry>
    ): Boolean {
        val encoded =
            encode(next)

        if (!atomicStore.writeAtomically(encoded)) {
            return false
        }

        current = next
        return true
    }

    private fun load():
        List<PendingScoreEntry> {
        val raw =
            atomicStore.read()
                ?: return emptyList()

        return try {
            val root = JSONObject(raw)
            val array =
                root.optJSONArray("entries")
                    ?: JSONArray()

            buildList {
                for (
                    index in
                    0 until array.length()
                ) {
                    decode(
                        array.optJSONObject(index)
                    )
                        ?.let(::add)
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun encode(
        entries: List<PendingScoreEntry>
    ): String {
        val array = JSONArray()

        entries.forEach {
            entry ->
            array.put(
                JSONObject().apply {
                    put("runId", entry.runId)
                    put("payloadJson", entry.payloadJson)
                    put("addedAt", entry.addedAt)
                    put("attempts", entry.attempts)
                    put(
                        "nextAttemptAt",
                        entry.nextAttemptAt
                    )
                    put("state", entry.state.name)
                    putNullable(
                        "lastHttpStatus",
                        entry.lastHttpStatus
                    )
                    putNullable(
                        "lastErrorCode",
                        entry.lastErrorCode
                    )
                }
            )
        }

        return JSONObject()
            .put("version", 1)
            .put("entries", array)
            .toString()
    }

    private fun decode(
        obj: JSONObject?
    ): PendingScoreEntry? {
        obj ?: return null

        val runId =
            obj.optString("runId")
                .takeIf { it.isNotEmpty() }
                ?: return null

        val payloadJson =
            if (obj.has("payloadJson")) {
                obj.optString("payloadJson")
            } else {
                return null
            }

        val state =
            try {
                PendingScoreState.valueOf(
                    obj.optString(
                        "state",
                        PendingScoreState
                            .PENDING.name
                    )
                )
            } catch (_: IllegalArgumentException) {
                PendingScoreState.PENDING
            }

        return PendingScoreEntry(
            runId = runId,
            payloadJson = payloadJson,
            addedAt = obj.optLong("addedAt", 0L),
            attempts =
                obj.optInt("attempts", 0)
                    .coerceAtLeast(0),
            nextAttemptAt =
                obj.optLong("nextAttemptAt", 0L),
            state = state,
            lastHttpStatus =
                if (
                    obj.has("lastHttpStatus") &&
                    !obj.isNull("lastHttpStatus")
                ) {
                    obj.getInt("lastHttpStatus")
                } else {
                    null
                },
            lastErrorCode =
                if (
                    obj.has("lastErrorCode") &&
                    !obj.isNull("lastErrorCode")
                ) {
                    obj.getString("lastErrorCode")
                } else {
                    null
                }
        )
    }

    private fun JSONObject.putNullable(
        key: String,
        value: Any?
    ) {
        put(
            key,
            value ?: JSONObject.NULL
        )
    }

    companion object {
        const val FILE_NAME =
            "pending-scores-v1.json"

        const val TEMP_FILE_NAME =
            "$FILE_NAME.tmp"
    }
}

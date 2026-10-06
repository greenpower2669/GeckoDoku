package com.greenpower2669.geckodoku

import java.io.File
import org.json.JSONArray
import org.json.JSONObject

data class GlobalScoreCacheEntry(
    val scoreId: String,
    val runId: String,
    val sequence: Long,
    val receivedAt: Long,
    val normalizedJson: String
)

data class GlobalScoreCacheState(
    val entries: List<GlobalScoreCacheEntry>,
    val cursor: String
)

class GlobalScoreCacheStore(
    directory: File
) {
    private val atomicStore =
        AtomicJsonFileStore(
            File(directory, FILE_NAME)
        )

    private var current = load()

    @Synchronized
    fun state(): GlobalScoreCacheState =
        current.copy(
            entries = current.entries.toList()
        )

    @Synchronized
    fun entry(
        scoreId: String
    ): GlobalScoreCacheEntry? =
        current.entries.firstOrNull {
            it.scoreId == scoreId
        }

    @Synchronized
    fun cursor(): String =
        current.cursor

    @Synchronized
    fun applyPage(
        entries: List<GlobalScoreCacheEntry>,
        nextCursor: String
    ): Boolean {
        val merged =
            LinkedHashMap<String, GlobalScoreCacheEntry>()

        current.entries.forEach {
            merged[it.scoreId] = it
        }

        entries.forEach {
            merged[it.scoreId] = it
        }

        val next =
            GlobalScoreCacheState(
                entries =
                    merged.values
                        .sortedBy {
                            it.sequence
                        },
                cursor = nextCursor
            )

        if (!atomicStore.writeAtomically(encode(next))) {
            return false
        }

        current = next
        return true
    }

    private fun load(): GlobalScoreCacheState {
        val raw =
            atomicStore.read()
                ?: return GlobalScoreCacheState(
                    emptyList(),
                    INITIAL_CURSOR
                )

        return try {
            val root = JSONObject(raw)
            val array =
                root.optJSONArray("entries")
                    ?: JSONArray()

            val entries =
                buildList {
                    for (
                        index in
                        0 until array.length()
                    ) {
                        decode(
                            array.optJSONObject(index)
                        )?.let(::add)
                    }
                }

            GlobalScoreCacheState(
                entries = entries,
                cursor =
                    root.optString(
                        "cursor",
                        INITIAL_CURSOR
                    )
            )
        } catch (_: Exception) {
            GlobalScoreCacheState(
                emptyList(),
                INITIAL_CURSOR
            )
        }
    }

    private fun encode(
        state: GlobalScoreCacheState
    ): String {
        val array = JSONArray()
        state.entries.forEach {
            entry ->
            array.put(
                JSONObject().apply {
                    put("scoreId", entry.scoreId)
                    put("runId", entry.runId)
                    put("sequence", entry.sequence)
                    put("receivedAt", entry.receivedAt)
                    put(
                        "normalizedJson",
                        entry.normalizedJson
                    )
                }
            )
        }

        return JSONObject()
            .put("version", 1)
            .put("cursor", state.cursor)
            .put("entries", array)
            .toString()
    }

    private fun decode(
        obj: JSONObject?
    ): GlobalScoreCacheEntry? {
        obj ?: return null

        val scoreId =
            obj.optString("scoreId")
                .takeIf { it.isNotBlank() }
                ?: return null

        val runId =
            obj.optString("runId")
                .takeIf { it.isNotBlank() }
                ?: return null

        if (!obj.has("sequence") || !obj.has("receivedAt")) {
            return null
        }

        return GlobalScoreCacheEntry(
            scoreId = scoreId,
            runId = runId,
            sequence = obj.getLong("sequence"),
            receivedAt = obj.getLong("receivedAt"),
            normalizedJson =
                obj.optString(
                    "normalizedJson",
                    "{}"
                )
        )
    }

    companion object {
        const val INITIAL_CURSOR = "0"
        const val FILE_NAME =
            "global-score-cache-v1.json"
        const val TEMP_FILE_NAME =
            "$FILE_NAME.tmp"
    }
}

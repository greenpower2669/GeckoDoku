package com.greenpower2669.geckodoku

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HallOfFameEntry(
    val playerName: String,
    val mode: GameMode,
    val size: Int,
    val difficulty: GameDifficulty,
    val stars: Int,
    val elapsedSeconds: Long,
    val completedAt: Long
)

class HallOfFameStore(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    fun add(
        entry: HallOfFameEntry
    ) {
        val all =
            readObjects()
                .toMutableList()

        all.add(
            0,
            JSONObject().apply {
                put(
                    "playerName",
                    entry.playerName
                )
                put(
                    "mode",
                    entry.mode.name
                )
                put(
                    "size",
                    entry.size
                )
                put(
                    "difficulty",
                    entry.difficulty.name
                )
                put(
                    "stars",
                    entry.stars
                        .coerceIn(
                            1,
                            5
                        )
                )
                put(
                    "elapsedSeconds",
                    entry.elapsedSeconds
                        .coerceAtLeast(
                            0L
                        )
                )
                put(
                    "completedAt",
                    entry.completedAt
                )
            }
        )

        writeObjects(
            all.take(
                MAX_ENTRIES
            )
        )
    }

    fun entries():
        List<HallOfFameEntry> =
        readObjects()
            .mapNotNull {
                obj ->
                decode(obj)
            }
            .sortedWith(
                compareBy<HallOfFameEntry> {
                    it.difficulty.ordinal
                }
                    .thenByDescending {
                        it.stars
                    }
                    .thenBy {
                        it.elapsedSeconds
                    }
                    .thenByDescending {
                        it.completedAt
                    }
            )

    fun clear() {
        prefs.edit()
            .remove(KEY_ENTRIES)
            .apply()
    }

    private fun decode(
        obj: JSONObject
    ): HallOfFameEntry? {
        val mode =
            GameMode.entries
                .firstOrNull {
                    it.name ==
                        obj.optString(
                            "mode"
                        )
                }
                ?: return null

        val difficulty =
            GameDifficulty.entries
                .firstOrNull {
                    it.name ==
                        obj.optString(
                            "difficulty"
                        )
                }
                ?: return null

        val name =
            obj.optString(
                "playerName",
                PlayerProfileStore
                    .DEFAULT_PLAYER_NAME
            )
                .trim()
                .ifBlank {
                    PlayerProfileStore
                        .DEFAULT_PLAYER_NAME
                }

        return HallOfFameEntry(
            playerName =
                name.take(32),
            mode = mode,
            size =
                obj.optInt(
                    "size",
                    0
                ),
            difficulty =
                difficulty,
            stars =
                obj.optInt(
                    "stars",
                    1
                )
                    .coerceIn(
                        1,
                        5
                    ),
            elapsedSeconds =
                obj.optLong(
                    "elapsedSeconds",
                    0L
                )
                    .coerceAtLeast(
                        0L
                    ),
            completedAt =
                obj.optLong(
                    "completedAt",
                    0L
                )
        )
    }

    private fun readObjects():
        List<JSONObject> {
        val raw =
            prefs.getString(
                KEY_ENTRIES,
                "[]"
            )
                ?: "[]"

        return try {
            val array =
                JSONArray(raw)

            buildList {
                for (
                    index in
                    0 until array.length()
                ) {
                    array.optJSONObject(
                        index
                    )
                        ?.let {
                            add(it)
                        }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun writeObjects(
        entries: List<JSONObject>
    ) {
        val array =
            JSONArray()

        entries.forEach {
            array.put(it)
        }

        prefs.edit()
            .putString(
                KEY_ENTRIES,
                array.toString()
            )
            .apply()
    }

    companion object {
        const val PREFERENCES_NAME =
            "geckodoku_hall_of_fame"

        private const val KEY_ENTRIES =
            "entries_v1"

        private const val MAX_ENTRIES =
            200
    }
}

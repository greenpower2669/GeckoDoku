package com.greenpower2669.geckodoku

import org.json.JSONObject

object GlobalHallProjection {
    fun fromCache(
        entries: List<GlobalScoreCacheEntry>
    ): List<HallOfFameEntry> =
        entries.mapNotNull {
            decode(it.normalizedJson)
        }

    fun merge(
        local: List<HallOfFameEntry>,
        globalCache: List<GlobalScoreCacheEntry>
    ): List<HallOfFameEntry> {
        val merged =
            LinkedHashMap<HallIdentity, HallOfFameEntry>()

        fromCache(globalCache).forEach {
            merged[identity(it)] = it
        }

        local.forEach {
            merged[identity(it)] = it
        }

        return merged.values.sortedWith(
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
    }

    private fun decode(
        raw: String
    ): HallOfFameEntry? =
        try {
            val obj = JSONObject(raw)

            if (!obj.optBoolean("completed", false)) {
                return null
            }

            val mode =
                GameMode.entries.firstOrNull {
                    it.name == obj.optString("mode")
                }
                    ?: return null

            val difficulty =
                GameDifficulty.entries.firstOrNull {
                    it.name == obj.optString("difficulty")
                }
                    ?: return null

            val playerName =
                obj.optString(
                    "playerName",
                    PlayerProfileStore.DEFAULT_PLAYER_NAME
                )
                    .trim()
                    .ifBlank {
                        PlayerProfileStore.DEFAULT_PLAYER_NAME
                    }
                    .take(32)

            if (
                !obj.has("size") ||
                !obj.has("stars") ||
                !obj.has("elapsedSeconds") ||
                !obj.has("completedAt")
            ) {
                return null
            }

            HallOfFameEntry(
                playerName = playerName,
                mode = mode,
                size = obj.getInt("size"),
                difficulty = difficulty,
                stars = obj.getInt("stars").coerceIn(1, 5),
                elapsedSeconds =
                    obj.getLong("elapsedSeconds")
                        .coerceAtLeast(0L),
                completedAt = obj.getLong("completedAt")
            )
        } catch (_: Exception) {
            null
        }

    private fun identity(
        entry: HallOfFameEntry
    ) =
        HallIdentity(
            playerName = entry.playerName,
            mode = entry.mode,
            size = entry.size,
            difficulty = entry.difficulty,
            stars = entry.stars,
            elapsedSeconds = entry.elapsedSeconds,
            completedAt = entry.completedAt
        )

    private data class HallIdentity(
        val playerName: String,
        val mode: GameMode,
        val size: Int,
        val difficulty: GameDifficulty,
        val stars: Int,
        val elapsedSeconds: Long,
        val completedAt: Long
    )
}

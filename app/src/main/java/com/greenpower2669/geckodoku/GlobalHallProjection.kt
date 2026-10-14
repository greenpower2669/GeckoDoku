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
        // Les scores mondiaux ont déjà été dédoublonnés par scoreId
        // dans GlobalScoreCacheStore. Ne JAMAIS utiliser le pseudo
        // comme clé unique : deux personnes peuvent porter le même nom
        // et même publier des scores statistiquement identiques.
        val global = fromCache(globalCache)
        val remainingLocal = local.toMutableList()

        global.forEach { remote ->
            // Une copie locale correspondante est retirée UNE seule fois.
            val index = remainingLocal.indexOfFirst {
                identity(it) == identity(remote)
            }
            if (index >= 0) remainingLocal.removeAt(index)
        }

        return (global + remainingLocal).sortedWith(
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
    ): HallOfFameEntry? {
        return try {
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

            val profileId =
                obj.optJSONObject(
                    "metadata"
                )
                    ?.optString(
                        "playerProfileId"
                    )
                    ?.trim()
                    ?.takeIf {
                        it.matches(
                            Regex(
                                "[a-fA-F0-9-]{36}"
                            )
                        )
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
                completedAt = obj.getLong("completedAt"),
                playerProfileId =
                    profileId
            )
        } catch (_: Exception) {
            null
        }
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

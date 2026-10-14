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
        val global =
            fromCache(
                globalCache
            )
        val remainingLocal =
            local.toMutableList()

        val hydratedGlobal =
            global.map {
                remote ->

                // Une copie locale correspondante est retirée UNE seule fois.
                // Si le serveur historique ne renvoie pas encore metadata,
                // l'identité locale exacte peut enrichir CE score sans
                // aucune déduction par pseudo.
                val index =
                    matchingLocalIndex(
                        remainingLocal,
                        remote
                    )

                if (
                    index <
                        0
                ) {
                    remote
                } else {
                    val localMatch =
                        remainingLocal
                            .removeAt(
                                index
                            )

                    if (
                        remote.playerProfileId ==
                            null &&
                        localMatch.playerProfileId !=
                            null
                    ) {
                        remote.copy(
                            playerProfileId =
                                localMatch
                                    .playerProfileId
                        )
                    } else {
                        remote
                    }
                }
            }

        return (hydratedGlobal + remainingLocal).sortedWith(
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

    private fun matchingLocalIndex(
        local: List<HallOfFameEntry>,
        remote: HallOfFameEntry
    ): Int {
        val candidates =
            local.indices
                .filter {
                    identity(
                        local[
                            it
                        ]
                    ) ==
                        identity(
                            remote
                        )
                }

        if (
            candidates.isEmpty()
        ) {
            return -1
        }

        val remoteProfile =
            remote.playerProfileId

        if (
            remoteProfile !=
                null
        ) {
            // Une identité serveur connue ne peut correspondre qu'au même
            // profil local, ou à une vieille ligne locale sans identité.
            candidates
                .firstOrNull {
                    local[
                        it
                    ].playerProfileId ==
                        remoteProfile
                }
                ?.let {
                    return it
                }

            return candidates
                .firstOrNull {
                    local[
                        it
                    ].playerProfileId ==
                        null
                }
                ?: -1
        }

        // Si le serveur historique ne fournit pas l'identité, préférer une
        // ancienne ligne locale elle aussi non attribuée.
        candidates
            .firstOrNull {
                local[
                    it
                ].playerProfileId ==
                    null
            }
            ?.let {
                return it
            }

        val knownProfiles =
            candidates
                .mapNotNull {
                    local[
                        it
                    ].playerProfileId
                }
                .distinct()

        // Hydrater n'est sûr que si toutes les copies candidates connues
        // appartiennent au même profil. Sinon on garde le score distant
        // non attribué plutôt que d'inventer un propriétaire.
        return if (
            knownProfiles.size ==
                1
        ) {
            candidates.first()
        } else {
            -1
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

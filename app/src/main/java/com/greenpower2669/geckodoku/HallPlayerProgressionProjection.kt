package com.greenpower2669.geckodoku

data class HallPlayerProgression(
    val playerProfileId: String,
    val displayName: String,
    val knownNames: List<String>,
    val entries: List<HallOfFameEntry>,
    val bestStars: Int,
    val bestSeconds: Long,
    val speedTrend: StatTrend,
    val starTrend: StatTrend
)

object HallPlayerProgressionProjection {
    fun groups(
        entries: List<HallOfFameEntry>
    ): List<HallPlayerProgression> =
        entries
            .filter {
                !it.playerProfileId
                    .isNullOrBlank()
            }
            .groupBy {
                requireNotNull(
                    it.playerProfileId
                )
            }
            .map {
                (profileId, values) ->

                val chronological =
                    values.sortedBy {
                        it.completedAt
                    }

                val latestName =
                    chronological
                        .lastOrNull()
                        ?.playerName
                        ?.trim()
                        ?.ifBlank {
                            PlayerProfileStore
                                .DEFAULT_PLAYER_NAME
                        }
                        ?: PlayerProfileStore
                            .DEFAULT_PLAYER_NAME

                val names =
                    chronological
                        .map {
                            it.playerName
                                .trim()
                        }
                        .filter {
                            it.isNotBlank()
                        }
                        .distinct()

                HallPlayerProgression(
                    playerProfileId =
                        profileId,
                    displayName =
                        latestName,
                    knownNames =
                        names,
                    entries =
                        chronological,
                    bestStars =
                        chronological
                            .maxOfOrNull {
                                it.stars
                            }
                            ?: 0,
                    bestSeconds =
                        chronological
                            .minOfOrNull {
                                it.elapsedSeconds
                            }
                            ?: 0L,
                    speedTrend =
                        trend(
                            chronological
                                .map {
                                    it.elapsedSeconds
                                },
                            lowerIsBetter =
                                true
                        ),
                    starTrend =
                        trend(
                            chronological
                                .map {
                                    it.stars
                                        .toLong()
                                },
                            lowerIsBetter =
                                false
                        )
                )
            }
            .sortedWith(
                compareBy<
                    HallPlayerProgression
                    > {
                    it.displayName
                        .lowercase()
                }
                    .thenBy {
                        it.playerProfileId
                    }
            )

    fun unidentifiedCount(
        entries: List<HallOfFameEntry>
    ): Int =
        entries.count {
            it.playerProfileId
                .isNullOrBlank()
        }

    private fun trend(
        values: List<Long>,
        lowerIsBetter: Boolean
    ): StatTrend {
        if (
            values.size <
                2
        ) {
            return StatTrend
                .INSUFFICIENT
        }

        val previous =
            values[
                values.lastIndex -
                    1
            ]
        val latest =
            values.last()

        if (
            latest ==
                previous
        ) {
            return StatTrend
                .STABLE
        }

        val improving =
            if (
                lowerIsBetter
            ) {
                latest <
                    previous
            } else {
                latest >
                    previous
            }

        return if (
            improving
        ) {
            StatTrend
                .IMPROVING
        } else {
            StatTrend
                .DECLINING
        }
    }
}

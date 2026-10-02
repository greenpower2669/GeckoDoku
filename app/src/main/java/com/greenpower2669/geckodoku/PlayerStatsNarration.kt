package com.greenpower2669.geckodoku

object PlayerStatsNarration {
    fun build(
        levels:
            List<
                ProfessorDifficultyStats
                >
    ): String {
        if (levels.isEmpty()) {
            return "Bonne partie !"
        }

        return buildString {
            levels
                .take(2)
                .forEachIndexed {
                        index,
                        level ->

                    if (index > 0) {
                        append(" ")
                    }

                    append(
                        level.difficulty
                            .label
                    )
                    append(" : ")

                    val messages =
                        mutableListOf<
                            String
                            >()

                    if (
                        level.stats
                            .speedTrend ==
                            StatTrend
                                .IMPROVING
                    ) {
                        messages.add(
                            "tu deviens plus rapide"
                        )
                    }

                    if (
                        level.stats
                            .starTrend ==
                            StatTrend
                                .IMPROVING
                    ) {
                        messages.add(
                            "tes réussites gagnent des étoiles"
                        )
                    }

                    if (
                        messages.isEmpty()
                    ) {
                        if (
                            level.stats
                                .completed <
                                2
                        ) {
                            append(
                                "encore trop peu de parties terminées pour voir une tendance."
                            )
                        } else {
                            append(
                                "pas encore de progression nette sur les deux dernières parties."
                            )
                        }
                    } else {
                        append(
                            messages.joinToString(
                                separator =
                                    " et "
                            )
                        )
                        append(".")
                    }
                }

            append(
                " Bonne partie !"
            )
        }
    }

    fun build(
        stats: LocalPlayerStats
    ): String =
        buildString {
            append(
                "Voici tes statistiques. "
            )
            append(
                stats.gamesCompleted
            )
            append(
                if (
                    stats.gamesCompleted <=
                        1
                ) {
                    " partie terminée. "
                } else {
                    " parties terminées. "
                }
            )
            append(
                stats.mistakes
            )
            append(
                if (
                    stats.mistakes <=
                        1
                ) {
                    " erreur enregistrée. "
                } else {
                    " erreurs enregistrées. "
                }
            )

            if (
                stats.gamesCompleted >
                    0
            ) {
                append(
                    "Ton temps moyen est de "
                )
                append(
                    spokenDuration(
                        stats.averageSeconds
                    )
                )
                append(". ")
            }

            append(
                "Bonne partie !"
            )
        }

    private fun spokenDuration(
        seconds: Long
    ): String {
        val safe =
            seconds.coerceAtLeast(
                0L
            )

        val minutes =
            safe /
                60L

        val remaining =
            safe %
                60L

        return if (
            minutes >
                0L
        ) {
            buildString {
                append(
                    minutes
                )
                append(
                    if (
                        minutes ==
                            1L
                    ) {
                        " minute"
                    } else {
                        " minutes"
                    }
                )

                if (
                    remaining >
                        0L
                ) {
                    append(
                        " et "
                    )
                    append(
                        remaining
                    )
                    append(
                        if (
                            remaining ==
                                1L
                        ) {
                            " seconde"
                        } else {
                            " secondes"
                        }
                    )
                }
            }
        } else {
            "$remaining " +
                if (
                    remaining ==
                        1L
                ) {
                    "seconde"
                } else {
                    "secondes"
                }
        }
    }
}

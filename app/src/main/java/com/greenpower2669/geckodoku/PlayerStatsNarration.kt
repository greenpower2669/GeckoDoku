package com.greenpower2669.geckodoku

object PlayerStatsNarration {
    fun build(
        stats: LocalPlayerStats
    ): String =
        buildString {
            append(
                "Voici tes statistiques. "
            )

            append(
                stats.gamesStarted
            )
            append(
                if (
                    stats.gamesStarted <= 1
                ) {
                    " partie lancée, "
                } else {
                    " parties lancées, "
                }
            )

            append(
                stats.gamesCompleted
            )
            append(
                if (
                    stats.gamesCompleted <= 1
                ) {
                    " terminée. "
                } else {
                    " terminées. "
                }
            )

            append(
                "Ton taux de réussite est de "
            )
            append(
                stats.completionRate
            )
            append(
                " pour cent. "
            )

            append(
                stats.mistakes
            )
            append(
                if (
                    stats.mistakes <= 1
                ) {
                    " erreur enregistrée. "
                } else {
                    " erreurs enregistrées. "
                }
            )

            if (
                stats.gamesCompleted > 0
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
            safe / 60L

        val remaining =
            safe % 60L

        return if (minutes > 0L) {
            buildString {
                append(minutes)
                append(
                    if (minutes == 1L) {
                        " minute"
                    } else {
                        " minutes"
                    }
                )

                if (remaining > 0L) {
                    append(" et ")
                    append(remaining)
                    append(
                        if (
                            remaining == 1L
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
                if (remaining == 1L) {
                    "seconde"
                } else {
                    "secondes"
                }
        }
    }
}

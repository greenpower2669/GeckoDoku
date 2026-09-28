package com.greenpower2669.geckodoku

enum class AssistanceKind(
    val points: Int
) {
    ADVICE(1),
    DIRECT_MOVE(2)
}

object CompletionRatingPolicy {
    fun starsFor(
        assistancePoints: Int,
        mistakes: Int = 0
    ): Int {
        val assistanceStars =
            when {
                assistancePoints <= 0 ->
                    5

                assistancePoints == 1 ->
                    4

                assistancePoints <= 3 ->
                    3

                assistancePoints <= 5 ->
                    2

                else ->
                    1
            }

        return (
            assistanceStars -
                mistakes
                    .coerceAtLeast(0) *
                    3
            )
            .coerceIn(
                1,
                5
            )
    }

    fun symbols(
        stars: Int
    ): String {
        val normalized =
            stars.coerceIn(
                1,
                5
            )

        return "★".repeat(
            normalized
        ) +
            "☆".repeat(
                5 - normalized
            )
    }
}

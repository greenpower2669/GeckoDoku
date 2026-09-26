package com.greenpower2669.geckodoku

enum class EncouragementSource {
    RECORDED,
    PIERRE
}

class EncouragementSourcePolicy {
    fun choose(
        randomValue: Int
    ): EncouragementSource =
        if (
            Math.floorMod(
                randomValue,
                2
            ) == 0
        ) {
            EncouragementSource
                .RECORDED
        } else {
            EncouragementSource
                .PIERRE
        }
}

object PierreEncouragements {
    val phrases =
        listOf(
            "Très joli raisonnement !",
            "Excellent, continue sur cette lancée !",
            "Bien vu, ce gecko était parfaitement placé !",
            "Super déduction !",
            "Bravo, tu avances vraiment bien !",
            "Très bon coup d'œil !",
            "Impeccable, garde ce rythme !",
            "Ça progresse très bien !",
            "Belle logique !",
            "Parfait, encore un gecko trouvé !",
            "Tu maîtrises de mieux en mieux la grille !",
            "Magnifique, continue comme ça !"
        )

    fun choose(
        remaining: Int,
        randomValue: Int
    ): String {
        if (remaining == 1) {
            return "Excellent ! Plus qu'un gecko à trouver !"
        }

        if (remaining == 2) {
            return "Très bien ! Plus que deux geckos !"
        }

        return phrases[
            Math.floorMod(
                randomValue,
                phrases.size
            )
        ]
    }
}

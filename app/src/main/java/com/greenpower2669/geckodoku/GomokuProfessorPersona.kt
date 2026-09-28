package com.greenpower2669.geckodoku

object GomokuProfessorPersona {
    fun moveStatus(
        difficulty: GameDifficulty
    ): String =
        when (difficulty) {
            GameDifficulty.DISCOVERY,
            GameDifficulty.EASY ->
                "Prof Gecko a joué. Observe ce que son coup laisse encore ouvert."

            GameDifficulty.THINKING,
            GameDifficulty.HARD ->
                "Prof Gecko a joué. À toi d'examiner les lignes actives."

            else ->
                "Prof Gecko a joué. À toi."
        }

    fun decorateDecision(
        decision: String,
        @Suppress("UNUSED_PARAMETER")
        difficulty: GameDifficulty
    ): String =
        decision

    fun decorateAdvice(
        advice: String,
        difficulty: GameDifficulty
    ): String {
        val intro =
            when (difficulty) {
                GameDifficulty.DISCOVERY ->
                    "Regarde la position avec moi. On va suivre le raisonnement pas à pas."

                GameDifficulty.EASY ->
                    "On regarde d'abord la menace la plus proche, puis ce qu'elle ouvre."

                GameDifficulty.THINKING,
                GameDifficulty.HARD ->
                    "Je te montre la ligne, les cases importantes et la suite que j'anticipe."

                else ->
                    "Je te montre la projection que j'ai calculée."
            }

        return intro +
            "\n\n" +
            advice
    }

    fun decorateResult(
        winner: GomokuPlayer?,
        base: String,
        difficulty: GameDifficulty
    ): String {
        val comment =
            when (winner) {
                GomokuPlayer.PLAYER ->
                    "Bien joué. Tu as trouvé la ligne avant moi."

                GomokuPlayer.PROFESSOR ->
                    when (difficulty) {
                        GameDifficulty.DISCOVERY,
                        GameDifficulty.EASY ->
                            "J'ai vu une ouverture avant toi. La prochaine fois, surveille surtout les extrémités des lignes."

                        else ->
                            "Cette fois, ma projection a tenu jusqu'au bout."
                    }

                null ->
                    "Match nul. La position a résisté aux deux plans."
            }

        return comment +
            "\n\n" +
            base
    }

    fun livingLine(
        event: ProfessorPlayerEvent,
        difficulty: GameDifficulty
    ): String =
        when (event) {
            ProfessorPlayerEvent.CORRECT_MOVE ->
                "Bien vu. Ce coup garde ton jeu vivant."

            ProfessorPlayerEvent.WRONG_MOVE,
            ProfessorPlayerEvent.RAPID_WRONG_MOVE ->
                "Oups. Celui-là ouvre une porte. Regarde les lignes avant de rejouer."

            ProfessorPlayerEvent.SMART_MOVE ->
                "Joli. Là, tu m'obliges à recalculer."

            ProfessorPlayerEvent.STREAK_STARTED,
            ProfessorPlayerEvent.STREAK_CONTINUED ->
                "Tu enchaînes bien. Continue à regarder les deux extrémités."

            ProfessorPlayerEvent.LONG_THINKING ->
                "Tu prends ton temps, et au Gomoku c'est souvent une bonne idée."

            ProfessorPlayerEvent.HINT_REQUESTED ->
                "D'accord. Je te montre ce que je regarde sur le plateau."

            ProfessorPlayerEvent.LEVEL_COMPLETED ->
                "Belle partie. Les lignes racontaient quelque chose, et tu l'as vu."

            ProfessorPlayerEvent.GAME_STARTED ->
                when (difficulty) {
                    GameDifficulty.DISCOVERY ->
                        "Découverte : je regarde moins loin. Profite-en pour lire les menaces simples."

                    GameDifficulty.EASY ->
                        "Facile : je reste cohérent, mais je ne calcule pas très loin."

                    GameDifficulty.THINKING ->
                        "Réflexion : je commence à anticiper plusieurs réponses."

                    GameDifficulty.HARD ->
                        "Difficile : surveille les doubles menaces."

                    else ->
                        "Ici, les projections deviennent plus longues. Observe bien les croisements."
                }

            ProfessorPlayerEvent.RETURN_AFTER_PAUSE ->
                "Te revoilà. On reprend la position là où elle en était."

            ProfessorPlayerEvent.AMBIENT ->
                "Regarde les lignes ouvertes : une case peut servir à attaquer et défendre en même temps."
        }
}

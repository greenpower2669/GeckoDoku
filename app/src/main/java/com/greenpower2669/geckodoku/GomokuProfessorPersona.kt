package com.greenpower2669.geckodoku

object GomokuProfessorPersona {
    fun moveStatus(
        @Suppress("UNUSED_PARAMETER")
        difficulty: GameDifficulty
    ): String =
        "Prof Gecko a joué. Essaie de suivre."

    fun decorateDecision(
        decision: String,
        @Suppress("UNUSED_PARAMETER")
        difficulty: GameDifficulty
    ): String =
        "Je ne vais pas ralentir pour toi.\n\n" +
            decision

    fun decorateAdvice(
        advice: String,
        @Suppress("UNUSED_PARAMETER")
        difficulty: GameDifficulty
    ): String =
        "Je t'aide, mais ne prends pas ça pour de la bonté.\n\n" +
            advice

    fun decorateResult(
        winner: GomokuPlayer?,
        base: String,
        @Suppress("UNUSED_PARAMETER")
        difficulty: GameDifficulty
    ): String {
        val sting =
            when (winner) {
                GomokuPlayer.PLAYER ->
                    "Profite de ta victoire. Je la classe dans les accidents statistiques."

                GomokuPlayer.PROFESSOR ->
                    "Même en Découverte, je ne fais pas de cadeaux."

                null ->
                    "Match nul. Tu as survécu ; ce n'est pas encore une victoire."
            }

        return sting +
            "\n\n" +
            base
    }

    fun livingLine(
        event: ProfessorPlayerEvent,
        @Suppress("UNUSED_PARAMETER")
        difficulty: GameDifficulty
    ): String =
        when (event) {
            ProfessorPlayerEvent.CORRECT_MOVE ->
                "Pas mal. Ne t'y habitue pas."

            ProfessorPlayerEvent.WRONG_MOVE,
            ProfessorPlayerEvent.RAPID_WRONG_MOVE ->
                "Merci pour l'ouverture. J'allais presque devoir réfléchir."

            ProfessorPlayerEvent.SMART_MOVE ->
                "C'était presque intelligent. Presque."

            ProfessorPlayerEvent.STREAK_STARTED,
            ProfessorPlayerEvent.STREAK_CONTINUED ->
                "Tu prends confiance ? Mauvaise idée."

            ProfessorPlayerEvent.LONG_THINKING ->
                "Tu peux réfléchir encore. Le Goban, lui, n'aura pas pitié."

            ProfessorPlayerEvent.HINT_REQUESTED ->
                "Encore un indice ? Je facture en dignité."

            ProfessorPlayerEvent.LEVEL_COMPLETED ->
                "Profite. Je ne compte pas te laisser recommencer."

            ProfessorPlayerEvent.GAME_STARTED ->
                "Même en Découverte, je ne fais pas de cadeaux."

            ProfessorPlayerEvent.RETURN_AFTER_PAUSE ->
                "Te revoilà. J'espérais presque une reddition."

            ProfessorPlayerEvent.AMBIENT ->
                "Je te regarde préparer ton prochain problème."
        }
}

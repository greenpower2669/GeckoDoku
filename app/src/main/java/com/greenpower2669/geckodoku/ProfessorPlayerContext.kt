package com.greenpower2669.geckodoku

enum class ProfessorPlayerEvent {
    CORRECT_MOVE,
    WRONG_MOVE,
    SMART_MOVE,
    STREAK_STARTED,
    STREAK_CONTINUED,
    LONG_THINKING,
    RAPID_WRONG_MOVE,
    HINT_REQUESTED,
    LEVEL_COMPLETED,
    GAME_STARTED,
    RETURN_AFTER_PAUSE,
    AMBIENT
}

data class ProfessorPlayerContext(
    val mastery: Int = 40,
    val impulsivity: Int = 0,
    val momentum: Int = 0,
    val successStreak: Int = 0,
    val rapidWrongStreak: Int = 0,
    val easyDominance: Int = 0,
    val recentLongThinking: Boolean = false,
    val hardPersistence: Int = 0,
    val hintRequests: Int = 0
)

class ProfessorPlayerContextTracker(
    initial: ProfessorPlayerContext =
        ProfessorPlayerContext()
) {
    var context: ProfessorPlayerContext =
        initial
        private set

    fun onEvent(
        event: ProfessorPlayerEvent,
        difficulty: GameDifficulty
    ): ProfessorPlayerContext {
        val current = context

        context =
            when (event) {
                ProfessorPlayerEvent.CORRECT_MOVE ->
                    current.copy(
                        mastery = clamp100(current.mastery + 2),
                        impulsivity = clamp100(current.impulsivity - 5),
                        momentum = clampMomentum(current.momentum + 8),
                        successStreak = current.successStreak + 1,
                        rapidWrongStreak = 0,
                        recentLongThinking = false
                    )

                ProfessorPlayerEvent.SMART_MOVE ->
                    current.copy(
                        mastery = clamp100(current.mastery + 6),
                        impulsivity = clamp100(current.impulsivity - 7),
                        momentum = clampMomentum(current.momentum + 14),
                        successStreak = current.successStreak + 1,
                        rapidWrongStreak = 0,
                        recentLongThinking = false
                    )

                ProfessorPlayerEvent.STREAK_STARTED,
                ProfessorPlayerEvent.STREAK_CONTINUED ->
                    current.copy(
                        mastery = clamp100(current.mastery + 3),
                        impulsivity = clamp100(current.impulsivity - 4),
                        momentum = clampMomentum(current.momentum + 12),
                        successStreak = current.successStreak + 1,
                        rapidWrongStreak = 0,
                        recentLongThinking = false
                    )

                ProfessorPlayerEvent.LONG_THINKING ->
                    current.copy(
                        impulsivity = clamp100(current.impulsivity - 12),
                        momentum = towardZero(current.momentum, 3),
                        recentLongThinking = true
                    )

                ProfessorPlayerEvent.WRONG_MOVE ->
                    current.copy(
                        mastery = clamp100(current.mastery - 1),
                        impulsivity =
                            clamp100(
                                current.impulsivity +
                                    if (current.recentLongThinking) 0 else 2
                            ),
                        momentum = clampMomentum(current.momentum - 8),
                        successStreak = 0,
                        rapidWrongStreak = 0,
                        hardPersistence =
                            current.hardPersistence +
                                if (
                                    difficulty.ordinal >=
                                    GameDifficulty.EXPERT.ordinal
                                ) 1 else 0
                    )

                ProfessorPlayerEvent.RAPID_WRONG_MOVE -> {
                    val rapid = current.rapidWrongStreak + 1
                    current.copy(
                        mastery = clamp100(current.mastery - 1),
                        impulsivity =
                            clamp100(
                                current.impulsivity +
                                    if (rapid >= 2) 20 else 5
                            ),
                        momentum = clampMomentum(current.momentum - 11),
                        successStreak = 0,
                        rapidWrongStreak = rapid,
                        recentLongThinking = false,
                        hardPersistence =
                            current.hardPersistence +
                                if (
                                    difficulty.ordinal >=
                                    GameDifficulty.EXPERT.ordinal
                                ) 1 else 0
                    )
                }

                ProfessorPlayerEvent.HINT_REQUESTED ->
                    current.copy(
                        impulsivity = clamp100(current.impulsivity - 5),
                        momentum = towardZero(current.momentum, 2),
                        hintRequests = current.hintRequests + 1,
                        recentLongThinking = false
                    )

                ProfessorPlayerEvent.LEVEL_COMPLETED -> {
                    val bonus =
                        when (difficulty) {
                            GameDifficulty.DISCOVERY -> 2
                            GameDifficulty.EASY -> 3
                            GameDifficulty.THINKING -> 4
                            GameDifficulty.HARD -> 5
                            GameDifficulty.EXPERT -> 7
                            GameDifficulty.DEMENTIAL -> 8
                            GameDifficulty.MISSION_IMPOSSIBLE -> 10
                            GameDifficulty.INFERNAL -> 12
                        }

                    val dominance =
                        if (
                            difficulty.ordinal <= GameDifficulty.EASY.ordinal &&
                            current.mastery >= 70
                        ) {
                            current.easyDominance + 1
                        } else {
                            (current.easyDominance - 1).coerceAtLeast(0)
                        }

                    current.copy(
                        mastery = clamp100(current.mastery + bonus),
                        impulsivity = clamp100(current.impulsivity - 8),
                        momentum = clampMomentum(current.momentum + 15),
                        successStreak = current.successStreak + 1,
                        rapidWrongStreak = 0,
                        easyDominance = dominance,
                        recentLongThinking = false
                    )
                }

                ProfessorPlayerEvent.GAME_STARTED -> {
                    decay(2)
                    context.copy(
                        successStreak = 0,
                        rapidWrongStreak = 0,
                        recentLongThinking = false
                    )
                }

                ProfessorPlayerEvent.RETURN_AFTER_PAUSE -> {
                    decay(10)
                    context.copy(
                        successStreak = 0,
                        rapidWrongStreak = 0,
                        recentLongThinking = false
                    )
                }

                ProfessorPlayerEvent.AMBIENT -> {
                    decay(1)
                    context
                }
            }

        return context
    }

    fun decay(
        steps: Int = 1
    ): ProfessorPlayerContext {
        var value = context
        repeat(steps.coerceAtLeast(0)) {
            value =
                value.copy(
                    impulsivity = clamp100(value.impulsivity - 3),
                    momentum = towardZero(value.momentum, 3),
                    rapidWrongStreak =
                        (value.rapidWrongStreak - 1).coerceAtLeast(0),
                    recentLongThinking = false
                )
        }
        context = value
        return context
    }

    private fun clamp100(value: Int): Int =
        value.coerceIn(0, 100)

    private fun clampMomentum(value: Int): Int =
        value.coerceIn(-100, 100)

    private fun towardZero(
        value: Int,
        amount: Int
    ): Int =
        when {
            value > 0 -> (value - amount).coerceAtLeast(0)
            value < 0 -> (value + amount).coerceAtMost(0)
            else -> 0
        }
}

data class PhraseCategoryWeight(
    val category: PhraseCategory,
    val weight: Int
)

class ProfessorMoodPolicy {
    val rareProbabilityPercent: Int = 4
    val fabProbabilityPercent: Int = 3

    fun moodFor(
        context: ProfessorPlayerContext,
        event: ProfessorPlayerEvent,
        difficulty: GameDifficulty
    ): ProfessorMood {
        if (
            event == ProfessorPlayerEvent.LEVEL_COMPLETED &&
            difficulty.ordinal <= GameDifficulty.EASY.ordinal &&
            context.mastery >= 75 &&
            context.easyDominance >= 2
        ) return ProfessorMood.TAQUIN

        if (
            event == ProfessorPlayerEvent.SMART_MOVE ||
            (
                event in
                    setOf(
                        ProfessorPlayerEvent.STREAK_STARTED,
                        ProfessorPlayerEvent.STREAK_CONTINUED
                    ) &&
                    context.successStreak >= 3
                ) ||
            (
                event == ProfessorPlayerEvent.CORRECT_MOVE &&
                difficulty.ordinal >= GameDifficulty.EXPERT.ordinal &&
                context.momentum >= 20
                )
        ) return ProfessorMood.IMPRESSED

        if (
            event == ProfessorPlayerEvent.WRONG_MOVE ||
            event == ProfessorPlayerEvent.RAPID_WRONG_MOVE
        ) {
            if (
                context.recentLongThinking ||
                difficulty.ordinal >= GameDifficulty.EXPERT.ordinal
            ) return ProfessorMood.ENCOURAGING

            if (
                event == ProfessorPlayerEvent.RAPID_WRONG_MOVE &&
                context.rapidWrongStreak >= 3 &&
                context.impulsivity >= 45
            ) return ProfessorMood.TAQUIN

            if (
                event == ProfessorPlayerEvent.RAPID_WRONG_MOVE &&
                context.rapidWrongStreak >= 2
            ) return ProfessorMood.PEDAGOGICAL

            return ProfessorMood.ENCOURAGING
        }

        if (event == ProfessorPlayerEvent.HINT_REQUESTED) {
            return ProfessorMood.PEDAGOGICAL
        }

        if (event == ProfessorPlayerEvent.RETURN_AFTER_PAUSE) {
            return ProfessorMood.CURIOUS
        }

        if (event == ProfessorPlayerEvent.LEVEL_COMPLETED) {
            return if (
                difficulty.ordinal >= GameDifficulty.EXPERT.ordinal
            ) ProfessorMood.IMPRESSED
            else ProfessorMood.PROUD
        }

        if (context.impulsivity >= 45) {
            return ProfessorMood.PEDAGOGICAL
        }

        if (context.momentum >= 30) {
            return ProfessorMood.PROUD
        }

        return ProfessorMood.NEUTRAL
    }

    fun categoryWeights(
        mood: ProfessorMood,
        event: ProfessorPlayerEvent
    ): List<PhraseCategoryWeight> {
        val weights = linkedMapOf<PhraseCategory, Int>()

        fun add(category: PhraseCategory, weight: Int) {
            weights[category] =
                (weights[category] ?: 0) + weight
        }

        when (event) {
            ProfessorPlayerEvent.CORRECT_MOVE -> {
                add(PhraseCategory.SUCCESS, 7)
                add(PhraseCategory.STREAK, 3)
                add(PhraseCategory.GENERAL, 2)
            }
            ProfessorPlayerEvent.SMART_MOVE -> {
                add(PhraseCategory.SMART, 9)
                add(PhraseCategory.SUCCESS, 4)
                add(PhraseCategory.STREAK, 2)
            }
            ProfessorPlayerEvent.STREAK_STARTED,
            ProfessorPlayerEvent.STREAK_CONTINUED -> {
                add(PhraseCategory.STREAK, 9)
                add(PhraseCategory.SUCCESS, 4)
                add(PhraseCategory.SMART, 2)
            }
            ProfessorPlayerEvent.WRONG_MOVE,
            ProfessorPlayerEvent.RAPID_WRONG_MOVE -> {
                add(PhraseCategory.ERROR, 5)
                add(PhraseCategory.HESITATION, 4)
                add(PhraseCategory.GENERAL, 2)
            }
            ProfessorPlayerEvent.HINT_REQUESTED,
            ProfessorPlayerEvent.LONG_THINKING -> {
                add(PhraseCategory.HESITATION, 7)
                add(PhraseCategory.GENERAL, 4)
            }
            ProfessorPlayerEvent.LEVEL_COMPLETED -> {
                add(PhraseCategory.FINISH, 9)
                add(PhraseCategory.SUCCESS, 4)
            }
            ProfessorPlayerEvent.RETURN_AFTER_PAUSE -> {
                add(PhraseCategory.RETURN, 9)
                add(PhraseCategory.GENERAL, 3)
            }
            ProfessorPlayerEvent.GAME_STARTED,
            ProfessorPlayerEvent.AMBIENT -> {
                add(PhraseCategory.GENERAL, 8)
                add(PhraseCategory.ABSURD, 3)
                add(PhraseCategory.SELF, 3)
                add(PhraseCategory.HESITATION, 2)
            }
        }

        when (mood) {
            ProfessorMood.IMPRESSED -> {
                add(PhraseCategory.SMART, 7)
                add(PhraseCategory.SUCCESS, 4)
                add(PhraseCategory.STREAK, 4)
            }
            ProfessorMood.TAQUIN -> {
                add(PhraseCategory.TAQUIN, 8)
                add(PhraseCategory.ABSURD, 5)
            }
            ProfessorMood.PEDAGOGICAL -> {
                add(PhraseCategory.HESITATION, 6)
                add(PhraseCategory.GENERAL, 5)
            }
            ProfessorMood.ENCOURAGING -> {
                add(PhraseCategory.SUCCESS, 4)
                add(PhraseCategory.HESITATION, 5)
                add(PhraseCategory.GENERAL, 4)
            }
            ProfessorMood.PROUD -> {
                add(PhraseCategory.SUCCESS, 4)
                add(PhraseCategory.STREAK, 2)
            }
            ProfessorMood.CURIOUS -> {
                add(PhraseCategory.GENERAL, 5)
                add(PhraseCategory.HESITATION, 3)
            }
            ProfessorMood.NEUTRAL -> Unit
        }

        return weights
            .filterValues { it > 0 }
            .map { PhraseCategoryWeight(it.key, it.value) }
    }

    fun fallbackCategories(
        requested: PhraseCategory
    ): List<PhraseCategory> =
        when (requested) {
            PhraseCategory.SMART ->
                listOf(PhraseCategory.SUCCESS, PhraseCategory.GENERAL)
            PhraseCategory.TAQUIN ->
                listOf(PhraseCategory.ABSURD, PhraseCategory.GENERAL)
            PhraseCategory.ERROR ->
                listOf(PhraseCategory.HESITATION, PhraseCategory.GENERAL)
            PhraseCategory.STREAK ->
                listOf(PhraseCategory.SUCCESS, PhraseCategory.GENERAL)
            PhraseCategory.HESITATION,
            PhraseCategory.SUCCESS,
            PhraseCategory.FINISH,
            PhraseCategory.RETURN,
            PhraseCategory.RARE,
            PhraseCategory.FAB,
            PhraseCategory.ABSURD,
            PhraseCategory.SELF ->
                listOf(PhraseCategory.GENERAL)
            PhraseCategory.GENERAL ->
                emptyList()
        }
}

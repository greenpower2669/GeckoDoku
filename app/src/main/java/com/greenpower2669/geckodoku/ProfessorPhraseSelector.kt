package com.greenpower2669.geckodoku

data class ProfessorPhraseSelection(
    val phrase: ProfessorPhrase,
    val mood: ProfessorMood,
    val requestedCategory:
        PhraseCategory,
    val selectedCategory:
        PhraseCategory,
    val eligibleCount: Int,
    val fallback: Boolean,
    val forcedOldest: Boolean
)

class ProfessorPhraseSelector(
    private val catalog:
        List<ProfessorPhrase>,
    private val history:
        ProfessorPhraseHistory,
    private val random:
        ProfessorRandom,
    private val moodPolicy:
        ProfessorMoodPolicy =
            ProfessorMoodPolicy()
) {
    private var lastCategory:
        PhraseCategory? = null

    fun select(
        event: ProfessorPlayerEvent,
        context: ProfessorPlayerContext,
        difficulty: GameDifficulty
    ): ProfessorPhraseSelection? {
        val mood =
            moodPolicy.moodFor(
                context,
                event,
                difficulty
            )

        maybeSpecial(
            category = PhraseCategory.RARE,
            probability =
                moodPolicy
                    .rareProbabilityPercent,
            mood = mood
        )?.let {
            return it
        }

        maybeSpecial(
            category = PhraseCategory.FAB,
            probability =
                moodPolicy
                    .fabProbabilityPercent,
            mood = mood
        )?.let {
            return it
        }

        val weights =
            moodPolicy
                .categoryWeights(
                    mood,
                    event
                )
                .map { item ->
                    if (
                        item.category ==
                            PhraseCategory.TAQUIN &&
                        lastCategory ==
                            PhraseCategory.TAQUIN
                    ) {
                        item.copy(
                            weight =
                                (
                                    item.weight /
                                        4
                                    ).coerceAtLeast(1)
                        )
                    } else {
                        item
                    }
                }

        val requested =
            chooseWeightedCategory(
                weights
            )
                ?: PhraseCategory.GENERAL

        chooseAvailable(
            requested
        )?.let { chosen ->
            return remember(
                phrase = chosen.first,
                mood = mood,
                requested = requested,
                eligibleCount =
                    chosen.second,
                fallback = false,
                forcedOldest = false
            )
        }

        for (
            fallbackCategory in
            moodPolicy
                .fallbackCategories(
                    requested
                )
        ) {
            chooseAvailable(
                fallbackCategory
            )?.let { chosen ->
                return remember(
                    phrase = chosen.first,
                    mood = mood,
                    requested = requested,
                    eligibleCount =
                        chosen.second,
                    fallback = true,
                    forcedOldest = false
                )
            }
        }

        val compatible =
            (
                listOf(requested) +
                    moodPolicy
                        .fallbackCategories(
                            requested
                        ) +
                    PhraseCategory.GENERAL
                )
                .distinct()

        val forcedPool =
            catalog
                .filter {
                    it.category in compatible &&
                        it.id !=
                            history.lastPhraseId
                }
                .ifEmpty {
                    catalog.filter {
                        it.id !=
                            history.lastPhraseId
                    }
                }

        val oldest =
            forcedPool
                .minWithOrNull(
                    compareBy<
                        ProfessorPhrase
                    > {
                        history
                            .lastUsedAt(
                                it.id
                            )
                            ?: Long.MIN_VALUE
                    }.thenBy {
                        it.id
                    }
                )
                ?: return null

        return remember(
            phrase = oldest,
            mood = mood,
            requested = requested,
            eligibleCount = 0,
            fallback = true,
            forcedOldest = true
        )
    }

    private fun maybeSpecial(
        category: PhraseCategory,
        probability: Int,
        mood: ProfessorMood
    ): ProfessorPhraseSelection? {
        if (
            probability <= 0 ||
            random.nextInt(100) >=
                probability
        ) {
            return null
        }

        val chosen =
            chooseAvailable(category)
                ?: return null

        return remember(
            phrase = chosen.first,
            mood = mood,
            requested = category,
            eligibleCount =
                chosen.second,
            fallback = false,
            forcedOldest = false
        )
    }

    private fun chooseWeightedCategory(
        weights:
            List<PhraseCategoryWeight>
    ): PhraseCategory? {
        val total =
            weights.sumOf {
                it.weight
                    .coerceAtLeast(0)
            }

        if (total <= 0) {
            return null
        }

        var ticket =
            random.nextInt(total)

        for (item in weights) {
            val weight =
                item.weight
                    .coerceAtLeast(0)

            if (ticket < weight) {
                return item.category
            }

            ticket -= weight
        }

        return weights.lastOrNull()
            ?.category
    }

    private fun chooseAvailable(
        category: PhraseCategory
    ): Pair<ProfessorPhrase, Int>? {
        val available =
            catalog.filter {
                it.category == category &&
                    it.id !=
                        history.lastPhraseId &&
                    history.isAvailable(
                        it.id
                    )
            }

        if (available.isEmpty()) {
            return null
        }

        return (
            available[
                random.nextInt(
                    available.size
                )
            ] to available.size
            )
    }

    private fun remember(
        phrase: ProfessorPhrase,
        mood: ProfessorMood,
        requested: PhraseCategory,
        eligibleCount: Int,
        fallback: Boolean,
        forcedOldest: Boolean
    ): ProfessorPhraseSelection {
        history.markUsed(phrase.id)
        lastCategory = phrase.category

        return ProfessorPhraseSelection(
            phrase = phrase,
            mood = mood,
            requestedCategory =
                requested,
            selectedCategory =
                phrase.category,
            eligibleCount =
                eligibleCount,
            fallback = fallback,
            forcedOldest =
                forcedOldest
        )
    }
}

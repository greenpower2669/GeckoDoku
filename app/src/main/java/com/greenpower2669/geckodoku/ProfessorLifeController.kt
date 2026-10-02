package com.greenpower2669.geckodoku

import android.content.Context

class ProfessorLifeController private constructor(
    private val tracker:
        ProfessorPlayerContextTracker,
    private val moodPolicy:
        ProfessorMoodPolicy,
    private val selector:
        ProfessorPhraseSelector
) {
    val context: ProfessorPlayerContext
        get() = tracker.context

    var mood: ProfessorMood =
        ProfessorMood.NEUTRAL
        private set

    fun observe(
        event: ProfessorPlayerEvent,
        difficulty: GameDifficulty
    ): ProfessorMood {
        val updated =
            tracker.onEvent(
                event,
                difficulty
            )

        mood =
            moodPolicy.moodFor(
                updated,
                event,
                difficulty
            )

        MediaTrace.event(
            source = "ProfessorLife",
            event = "PROF_CONTEXT",
            detail =
                "mastery=" +
                    updated.mastery +
                    " impulsivity=" +
                    updated.impulsivity +
                    " momentum=" +
                    updated.momentum +
                    " mood=" +
                    mood +
                    " playerEvent=" +
                    event +
                    " difficulty=" +
                    difficulty
        )

        return mood
    }

    fun choose(
        event: ProfessorPlayerEvent,
        difficulty: GameDifficulty
    ): ProfessorPhraseSelection? {
        val selection =
            selector.select(
                event = event,
                context = tracker.context,
                difficulty = difficulty
            )
                ?: return null

        mood = selection.mood

        MediaTrace.event(
            source = "ProfessorLife",
            event = "PROF_PHRASE_POOL",
            detail =
                "requested=" +
                    selection.requestedCategory +
                    " eligible=" +
                    selection.eligibleCount
        )

        if (selection.forcedOldest) {
            MediaTrace.event(
                source = "ProfessorLife",
                event =
                    "PROF_PHRASE_FORCED_OLDEST",
                detail =
                    "id=" +
                        selection.phrase.id
            )
        } else if (selection.fallback) {
            MediaTrace.event(
                source = "ProfessorLife",
                event =
                    "PROF_PHRASE_FALLBACK",
                detail =
                    "requested=" +
                        selection.requestedCategory +
                        " fallback=" +
                        selection.selectedCategory
            )
        }

        MediaTrace.event(
            source = "ProfessorLife",
            event = "PROF_PHRASE_SELECTED",
            detail =
                "id=" +
                    selection.phrase.id +
                    " category=" +
                    selection.selectedCategory +
                    " mood=" +
                    selection.mood
        )

        return selection
    }

    companion object {
        fun create(
            context: Context
        ): ProfessorLifeController {
            val moodPolicy =
                ProfessorMoodPolicy()

            val history =
                ProfessorPhraseHistory(
                    clock =
                        SystemProfessorClock,
                    storage =
                        SharedPreferencesProfessorPhraseHistoryStorage(
                            context.applicationContext
                        )
                )

            return ProfessorLifeController(
                tracker =
                    ProfessorPlayerContextTracker(),
                moodPolicy =
                    moodPolicy,
                selector =
                    ProfessorPhraseSelector(
                        catalog =
                            ProfessorPhraseCatalog.all,
                        history = history,
                        random =
                            KotlinProfessorRandom,
                        moodPolicy =
                            moodPolicy
                    )
            )
        }
    }
}

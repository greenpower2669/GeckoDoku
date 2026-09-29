package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Gecko037LivingProfessorTest {
    private class FakeClock(
        var now: Long = 0L
    ) : ProfessorClock {
        override fun nowMs(): Long = now
        fun advance(ms: Long) {
            now += ms
        }
    }

    private class FakeRandom(
        private val values: MutableList<Int> =
            mutableListOf()
    ) : ProfessorRandom {
        override fun nextInt(bound: Int): Int {
            val raw =
                if (values.isEmpty()) 50
                else values.removeAt(0)
            return Math.floorMod(raw, bound)
        }
    }

    private class MemoryStorage :
        ProfessorPhraseHistoryStorage {
        val used =
            mutableMapOf<String, Long>()
        var last: String? = null
        var recent:
            List<String> =
            emptyList()

        override fun readLastUsedAt(
            id: String
        ): Long? = used[id]

        override fun writeLastUsedAt(
            id: String,
            timestampMs: Long
        ) {
            used[id] = timestampMs
        }

        override fun readLastPhraseId():
            String? = last

        override fun writeLastPhraseId(
            id: String?
        ) {
            last = id
        }

        override fun readRecentPhraseIds():
            List<String> =
            recent

        override fun writeRecentPhraseIds(
            ids: List<String>
        ) {
            recent =
                ids.toList()
        }
    }

    @Test
    fun corpusContains599StableUniquePhrasesIncluding300Returns() {
        val all =
            ProfessorPhraseCatalog.all

        assertEquals(599, all.size)
        assertEquals(
            100,
            all.count {
                it.id.startsWith(
                    "legacy_smalltalk_"
                )
            }
        )
        assertEquals(
            "legacy_smalltalk_001",
            all.first().id
        )
        assertEquals(
            "legacy_smalltalk_100",
            all[99].id
        )
        assertEquals(
            all.size,
            all.map { it.id }.toSet().size
        )
        assertTrue(
            all.all {
                it.id.isNotBlank() &&
                    it.text.isNotBlank()
            }
        )

        val normalized =
            all.map {
                ProfessorPhraseCatalog
                    .normalizeText(it.text)
            }

        assertEquals(
            normalized.size,
            normalized.toSet().size
        )

        assertEquals(
            40,
            all.count {
                it.category ==
                    PhraseCategory.GENERAL &&
                    it.id.startsWith("prof_general_")
            }
        )
        assertEquals(
            300,
            all.count {
                it.category ==
                    PhraseCategory.RETURN
            }
        )

        assertEquals(
            20,
            all.filter {
                it.category ==
                    PhraseCategory.RETURN
            }
                .map {
                    it.id
                        .removePrefix(
                            "prof_return_"
                        )
                        .substringBefore("_")
                }
                .toSet()
                .size
        )

        assertEquals(
            10,
            all.count {
                it.category ==
                    PhraseCategory.RARE
            }
        )
        assertEquals(
            4,
            all.count {
                it.category ==
                    PhraseCategory.FAB
            }
        )
        assertEquals(
            5,
            all.count {
                it.category ==
                    PhraseCategory.TAQUIN
            }
        )
    }

    @Test
    fun returnEventAlwaysUsesTheDedicatedReturnPool() {
        val history =
            ProfessorPhraseHistory(
                clock =
                    FakeClock(),
                storage =
                    MemoryStorage()
            )

        val selector =
            ProfessorPhraseSelector(
                catalog =
                    ProfessorPhraseCatalog
                        .all,
                history = history,
                random =
                    FakeRandom(
                        mutableListOf(
                            0,
                            1,
                            2
                        )
                    )
            )

        repeat(3) {
            val selection =
                selector.select(
                    event =
                        ProfessorPlayerEvent
                            .RETURN_AFTER_PAUSE,
                    context =
                        ProfessorPlayerContext(),
                    difficulty =
                        GameDifficulty.EASY
                )

            assertNotNull(selection)
            assertEquals(
                PhraseCategory.RETURN,
                selection!!
                    .phrase
                    .category
            )
        }
    }

    @Test
    fun similarityDiagnosticFlagsCloseWording() {
        val ratio =
            ProfessorPhraseCatalog
                .textSimilarity(
                    "Très bien, continue comme ça.",
                    "Super, continue comme ça."
                )

        assertTrue(ratio >= 0.5)
    }

    @Test
    fun individualCooldownExpiresExactlyAt48Hours() {
        val clock = FakeClock()
        val storage = MemoryStorage()
        val history =
            ProfessorPhraseHistory(
                clock = clock,
                storage = storage
            )
        val id = "prof_general_001"

        assertTrue(history.isAvailable(id))

        history.markUsed(id)

        clock.advance(30_000L)
        assertFalse(history.isAvailable(id))

        clock.now =
            47L * 60L * 60L * 1000L +
                59L * 60L * 1000L
        assertFalse(history.isAvailable(id))

        clock.now =
            48L * 60L * 60L * 1000L
        assertTrue(history.isAvailable(id))
    }

    @Test
    fun historyAndLastPhraseSurviveStoreRecreation() {
        val clock = FakeClock(123_456L)
        val storage = MemoryStorage()

        ProfessorPhraseHistory(
            clock,
            storage
        ).markUsed("prof_general_002")

        val recreated =
            ProfessorPhraseHistory(
                clock,
                storage
            )

        assertFalse(
            recreated.isAvailable(
                "prof_general_002"
            )
        )
        assertEquals(
            "prof_general_002",
            recreated.lastPhraseId
        )
    }

    @Test
    fun lastPhraseCanNeverBeImmediateSelectionAgain() {
        val clock = FakeClock()
        val storage = MemoryStorage()
        val history =
            ProfessorPhraseHistory(
                clock,
                storage
            )

        val smallCatalog =
            listOf(
                ProfessorPhrase(
                    "a",
                    "Phrase A",
                    PhraseCategory.GENERAL
                ),
                ProfessorPhrase(
                    "b",
                    "Phrase B",
                    PhraseCategory.GENERAL
                )
            )

        val selector =
            ProfessorPhraseSelector(
                catalog = smallCatalog,
                history = history,
                random = FakeRandom(
                    mutableListOf(50, 50, 0)
                )
            )

        val first =
            selector.select(
                event =
                    ProfessorPlayerEvent.AMBIENT,
                context =
                    ProfessorPlayerContext(),
                difficulty =
                    GameDifficulty.EASY
            )

        assertNotNull(first)

        val second =
            selector.select(
                event =
                    ProfessorPlayerEvent.AMBIENT,
                context =
                    ProfessorPlayerContext(),
                difficulty =
                    GameDifficulty.EASY
            )

        assertNotNull(second)
        assertNotEquals(
            first!!.phrase.id,
            second!!.phrase.id
        )
    }

    @Test
    fun exhaustedCooldownUsesOldestFirstWithoutClearingHistory() {
        val hour = 60L * 60L * 1000L
        val clock = FakeClock()
        val storage = MemoryStorage()
        val history =
            ProfessorPhraseHistory(
                clock,
                storage
            )

        history.markUsed("a")
        clock.advance(35L * hour)
        history.markUsed("b")
        clock.advance(
            12L * hour -
                4L * 60L * 1000L
        )
        history.markUsed("c")

        val selector =
            ProfessorPhraseSelector(
                catalog =
                    listOf(
                        ProfessorPhrase(
                            "a",
                            "A",
                            PhraseCategory.GENERAL
                        ),
                        ProfessorPhrase(
                            "b",
                            "B",
                            PhraseCategory.GENERAL
                        ),
                        ProfessorPhrase(
                            "c",
                            "C",
                            PhraseCategory.GENERAL
                        )
                    ),
                history = history,
                random = FakeRandom()
            )

        val selected =
            selector.select(
                event =
                    ProfessorPlayerEvent.AMBIENT,
                context =
                    ProfessorPlayerContext(),
                difficulty =
                    GameDifficulty.EASY
            )

        assertNotNull(selected)
        assertEquals("a", selected!!.phrase.id)
        assertTrue(selected.forcedOldest)
        assertFalse(history.isAvailable("b"))
    }

    @Test
    fun streakBecomesImpressed() {
        val tracker =
            ProfessorPlayerContextTracker()

        repeat(4) {
            tracker.onEvent(
                ProfessorPlayerEvent
                    .STREAK_CONTINUED,
                GameDifficulty.HARD
            )
        }

        val mood =
            ProfessorMoodPolicy().moodFor(
                tracker.context,
                ProfessorPlayerEvent
                    .STREAK_CONTINUED,
                GameDifficulty.HARD
            )

        assertEquals(
            ProfessorMood.IMPRESSED,
            mood
        )
    }

    @Test
    fun severalRapidErrorsMayBecomeTaquinOrPedagogical() {
        val tracker =
            ProfessorPlayerContextTracker()

        repeat(4) {
            tracker.onEvent(
                ProfessorPlayerEvent
                    .RAPID_WRONG_MOVE,
                GameDifficulty.EASY
            )
        }

        val mood =
            ProfessorMoodPolicy().moodFor(
                tracker.context,
                ProfessorPlayerEvent
                    .RAPID_WRONG_MOVE,
                GameDifficulty.EASY
            )

        assertTrue(
            mood ==
                ProfessorMood.TAQUIN ||
                mood ==
                ProfessorMood.PEDAGOGICAL
        )
    }

    @Test
    fun reflectedErrorOnHardGridIsNeverImmediateTaquin() {
        val tracker =
            ProfessorPlayerContextTracker()

        tracker.onEvent(
            ProfessorPlayerEvent
                .LONG_THINKING,
            GameDifficulty.EXPERT
        )
        tracker.onEvent(
            ProfessorPlayerEvent
                .WRONG_MOVE,
            GameDifficulty.EXPERT
        )

        val mood =
            ProfessorMoodPolicy().moodFor(
                tracker.context,
                ProfessorPlayerEvent
                    .WRONG_MOVE,
                GameDifficulty.EXPERT
            )

        assertEquals(
            ProfessorMood.ENCOURAGING,
            mood
        )
    }

    @Test
    fun repeatedEasyDominanceMayBecomeTaquin() {
        val context =
            ProfessorPlayerContext(
                mastery = 88,
                impulsivity = 0,
                momentum = 70,
                easyDominance = 3
            )

        assertEquals(
            ProfessorMood.TAQUIN,
            ProfessorMoodPolicy().moodFor(
                context,
                ProfessorPlayerEvent
                    .LEVEL_COMPLETED,
                GameDifficulty.EASY
            )
        )
    }

    @Test
    fun contextNaturallyDecaysTowardNeutral() {
        val tracker =
            ProfessorPlayerContextTracker(
                initial =
                    ProfessorPlayerContext(
                        mastery = 60,
                        impulsivity = 72,
                        momentum = -55,
                        rapidWrongStreak = 4
                    )
            )

        tracker.decay(steps = 24)

        assertTrue(
            tracker.context.impulsivity <= 10
        )
        assertEquals(
            0,
            tracker.context.momentum
        )
        assertEquals(
            ProfessorMood.NEUTRAL,
            ProfessorMoodPolicy().moodFor(
                tracker.context,
                ProfessorPlayerEvent.AMBIENT,
                GameDifficulty.EASY
            )
        )
    }

    @Test
    fun rareProbabilityStaysAtMostFivePercent() {
        val policy = ProfessorMoodPolicy()
        assertTrue(
            policy.rareProbabilityPercent
                in 3..5
        )
    }

    @Test
    fun quickBubbleClosesOneSecondAfterRealCompletionOnly() {
        val policy =
            ProfessorQuickBubbleClosePolicy()

        val token =
            policy.onSimpleBubbleShown()

        val schedule =
            policy.onSpeechCompleted(token)

        assertNotNull(schedule)
        assertEquals(
            1_000L,
            schedule!!.delayMs
        )
        assertTrue(
            policy.canClose(
                schedule.token
            )
        )
    }

    @Test
    fun obsoleteCloseCannotCloseNewBubbleAndPedagogyNeverAutoCloses() {
        val policy =
            ProfessorQuickBubbleClosePolicy()

        val old =
            policy.onSimpleBubbleShown()
        val oldSchedule =
            policy.onSpeechCompleted(old)

        val newer =
            policy.onSimpleBubbleShown()

        assertNotNull(oldSchedule)
        assertFalse(
            policy.canClose(
                oldSchedule!!.token
            )
        )

        val pedagogical =
            policy.onPedagogicalBubbleShown()

        assertTrue(pedagogical > newer)
        assertEquals(
            null,
            policy.onSpeechCompleted(
                pedagogical
            )
        )
    }
}

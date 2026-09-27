package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfessorSimpleSpeechCoordinatorTest {
    @Test
    fun ambientKeepsExactBubbleAndSpeechTextWithShortStatus() {
        val coordinator =
            ProfessorSimpleSpeechCoordinator(
                ProfessorQuickBubbleClosePolicy()
            )

        val text =
            "Vous réfléchissez depuis un moment."

        val plan =
            coordinator.begin(
                text = text,
                origin = SpeechOrigin.AMBIENT,
                canAccept = true
            )

        requireNotNull(plan)

        assertEquals(text, plan.bubbleText)
        assertEquals(text, plan.speechText)
        assertEquals("Prof Gecko", plan.statusText)
        assertEquals(
            SpeechOrigin.AMBIENT,
            plan.origin
        )
        assertTrue(plan.autoClose)
    }

    @Test
    fun statsKeepsExactNarrationAndStatsOrigin() {
        val coordinator =
            ProfessorSimpleSpeechCoordinator(
                ProfessorQuickBubbleClosePolicy()
            )

        val narration =
            "Vous avez résolu trois grilles."

        val plan =
            coordinator.begin(
                text = narration,
                origin = SpeechOrigin.STATS,
                canAccept = true
            )

        requireNotNull(plan)

        assertEquals(
            narration,
            plan.bubbleText
        )
        assertEquals(
            narration,
            plan.speechText
        )
        assertEquals(
            SpeechOrigin.STATS,
            plan.origin
        )
        assertEquals(
            1,
            plan.voiceRequestCount
        )
    }

    @Test
    fun rejectedRequestCreatesNoPresentationPlan() {
        val coordinator =
            ProfessorSimpleSpeechCoordinator(
                ProfessorQuickBubbleClosePolicy()
            )

        assertNull(
            coordinator.begin(
                text = "Phrase refusée",
                origin = SpeechOrigin.AMBIENT,
                canAccept = false
            )
        )
    }

    @Test
    fun completionSchedulesCloseAfterRealCompletionPlusOneSecond() {
        val coordinator =
            ProfessorSimpleSpeechCoordinator(
                ProfessorQuickBubbleClosePolicy()
            )

        val plan =
            requireNotNull(
                coordinator.begin(
                    text = "Bonjour",
                    origin =
                        SpeechOrigin.QUICK_TALK,
                    canAccept = true
                )
            )

        val schedule =
            coordinator.onSpeechCompleted(
                plan.token
            )

        requireNotNull(schedule)

        assertEquals(
            1_000L,
            schedule.delayMs
        )
        assertTrue(
            coordinator.canClose(
                schedule.token
            )
        )
    }

    @Test
    fun staleCompletionOrRejectionCannotCloseNewBubble() {
        val coordinator =
            ProfessorSimpleSpeechCoordinator(
                ProfessorQuickBubbleClosePolicy()
            )

        val old =
            requireNotNull(
                coordinator.begin(
                    text = "Ancienne",
                    origin = SpeechOrigin.AMBIENT,
                    canAccept = true
                )
            )

        val newer =
            requireNotNull(
                coordinator.begin(
                    text = "Nouvelle",
                    origin = SpeechOrigin.STATS,
                    canAccept = true
                )
            )

        assertNull(
            coordinator.onSpeechCompleted(
                old.token
            )
        )
        assertFalse(
            coordinator.shouldCloseAfterRejection(
                old.token
            )
        )
        assertTrue(
            coordinator.shouldCloseAfterRejection(
                newer.token
            )
        )
    }

    @Test
    fun pedagogicalBubbleRemainsOutsideSimpleAutoCloseContract() {
        val policy =
            ProfessorQuickBubbleClosePolicy()

        val pedagogical =
            policy.onPedagogicalBubbleShown()

        assertNull(
            policy.onSpeechCompleted(
                pedagogical
            )
        )
    }
}

package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfessorAmbientPolicyTest {
    private val policy =
        ProfessorAmbientPolicy()

    @Test
    fun helpIsOfferedAfterNinetySecondsOnlyOncePerIdleEpisode() {
        assertFalse(
            policy.shouldOfferHelp(
                nowMs = 89_999L,
                lastBoardActionAtMs = 0L,
                alreadyOffered = false,
                blocked = false
            )
        )

        assertTrue(
            policy.shouldOfferHelp(
                nowMs = 90_000L,
                lastBoardActionAtMs = 0L,
                alreadyOffered = false,
                blocked = false
            )
        )

        assertFalse(
            policy.shouldOfferHelp(
                nowMs = 180_000L,
                lastBoardActionAtMs = 0L,
                alreadyOffered = true,
                blocked = false
            )
        )
    }

    @Test
    fun longGameOfferUsesTimeOrActionThresholdAndNeverWhenAlreadySaved() {
        assertTrue(
            policy.shouldOfferSave(
                nowMs = 600_000L,
                gameStartedAtMs = 0L,
                boardActionCount = 0,
                alreadyOffered = false,
                alreadySaved = false,
                blocked = false
            )
        )

        assertTrue(
            policy.shouldOfferSave(
                nowMs = 1_000L,
                gameStartedAtMs = 0L,
                boardActionCount = 25,
                alreadyOffered = false,
                alreadySaved = false,
                blocked = false
            )
        )

        assertFalse(
            policy.shouldOfferSave(
                nowMs = 700_000L,
                gameStartedAtMs = 0L,
                boardActionCount = 40,
                alreadyOffered = false,
                alreadySaved = true,
                blocked = false
            )
        )
    }

    @Test
    fun ambientSpeechIsBlockedByHigherPriorityContexts() {
        assertFalse(
            policy.shouldOfferHelp(
                nowMs = 100_000L,
                lastBoardActionAtMs = 0L,
                alreadyOffered = false,
                blocked = true
            )
        )

        assertFalse(
            policy.canSpeakSmallTalk(
                nowMs = 500_000L,
                nextSmallTalkAtMs = 400_000L,
                blocked = true
            )
        )
    }

    @Test
    fun smallTalkDelayStaysBetweenFourAndEightMinutes() {
        repeat(1000) {
            val value =
                policy.smallTalkDelayMs(it)

            assertTrue(
                value >= 240_000L
            )
            assertTrue(
                value <= 480_000L
            )
        }
    }
}

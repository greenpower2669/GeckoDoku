package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gecko033CoexistenceTest {
    @Test
    fun ambientAndStatsNeverPreemptAnActivePhrase() {
        val policy =
            ProfessorSpeechRequestPolicy()

        assertEquals(
            SpeechRequestDecision.REJECT_BUSY,
            policy.decide(
                activeOrigin =
                    SpeechOrigin.PROF_BUTTON,
                incomingOrigin =
                    SpeechOrigin.AMBIENT
            )
        )

        assertEquals(
            SpeechRequestDecision.REJECT_BUSY,
            policy.decide(
                activeOrigin =
                    SpeechOrigin.PROF_BUTTON,
                incomingOrigin =
                    SpeechOrigin.STATS
            )
        )
    }

    @Test
    fun professorButtonMayReplaceCurrentSpeech() {
        val policy =
            ProfessorSpeechRequestPolicy()

        SpeechOrigin.entries.forEach {
            active ->

            assertEquals(
                SpeechRequestDecision.REPLACE,
                policy.decide(
                    activeOrigin = active,
                    incomingOrigin =
                        SpeechOrigin.PROF_BUTTON
                )
            )
        }
    }

    @Test
    fun normalPlayerActionsNeverRequestSpeechStop() {
        val policy =
            ProfessorSessionPolicy()

        listOf(
            PlayerInteraction.SINGLE_TAP,
            PlayerInteraction.DOUBLE_TAP,
            PlayerInteraction.CROSS,
            PlayerInteraction.GECKO_ADD,
            PlayerInteraction.GECKO_REMOVE,
            PlayerInteraction.LONG_PRESS
        ).forEach {
            interaction ->

            assertFalse(
                policy.shouldStopSpeech(
                    interaction
                )
            )
        }
    }

    @Test
    fun independentVideoSessionsCoexistAndCompleteIndependently() {
        val registry =
            RichMediaPlaybackRegistry()

        registry.started(
            id = "prof",
            kind =
                RichMediaKind
                    .PROF_LONG_ACTION
        )
        registry.started(
            id = "gecko",
            kind =
                RichMediaKind
                    .GECKO_APPEARANCE
        )

        assertEquals(
            setOf(
                "prof",
                "gecko"
            ),
            registry.activeIds()
        )

        registry.completed("gecko")

        assertEquals(
            setOf("prof"),
            registry.activeIds()
        )
        assertTrue(
            registry.isActive("prof")
        )
    }

    @Test
    fun introOneIsAudibleAndProfessorWaitsOnlyForIntroOne() {
        val policy =
            IntroLifecyclePolicy()

        assertEquals(
            IntroPhase.FIRST,
            policy.initialPhase
        )
        assertFalse(
            policy.professorEligible(
                IntroPhase.FIRST
            )
        )
        assertTrue(
            policy.professorEligible(
                IntroPhase.SECOND
            )
        )
        assertFalse(
            policy.mustMuteIntro(
                phase =
                    IntroPhase.FIRST,
                fxEnabled = true
            )
        )
        assertTrue(
            policy.mustMuteIntro(
                phase =
                    IntroPhase.FIRST,
                fxEnabled = false
            )
        )
    }

    @Test
    fun introNaturalCompletionAdvancesButSkipEndsSequence() {
        val policy =
            IntroLifecyclePolicy()

        assertEquals(
            IntroPhase.SECOND,
            policy.onNaturalCompletion(
                IntroPhase.FIRST
            )
        )
        assertEquals(
            IntroPhase.DONE,
            policy.onNaturalCompletion(
                IntroPhase.SECOND
            )
        )
        assertEquals(
            IntroPhase.DONE,
            policy.onSkip(
                IntroPhase.FIRST
            )
        )
    }

    @Test
    fun transparentKeyedSurfaceUsesTopCompositionInsteadOfOpaqueMediaOverlay() {
        val policy =
            VideoSurfaceLayerPolicy()

        assertTrue(
            policy.useZOrderOnTop
        )
        assertFalse(
            policy.useMediaOverlay
        )
    }

    @Test
    fun previousSpeechVideoFailureDoesNotBlockNextPhraseRetry() {
        val policy =
            ProfessorSpeechVideoStartPolicy()

        assertTrue(
            policy.canStart(
                animationsEnabled = true,
                playVideoInButton = true,
                viewReady = true,
                speechActive = true,
                previousAttemptFailed = true
            )
        )
    }
}

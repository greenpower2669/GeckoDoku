package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gecko036ContractTest {
    @Test
    fun portraitStaysVisibleUntilVideoRevealReallySucceeds() {
        val policy =
            ProfessorPortraitContinuityPolicy()

        assertTrue(policy.portraitVisible)
        assertFalse(policy.videoVisible)

        policy.onPrepareStarted()
        assertTrue(policy.portraitVisible)
        assertFalse(policy.videoVisible)

        policy.onFirstFrameHeld()
        assertTrue(policy.portraitVisible)
        assertFalse(policy.videoVisible)

        policy.onRevealSucceeded()
        assertFalse(policy.portraitVisible)
        assertTrue(policy.videoVisible)
    }

    @Test
    fun portraitRemainsOrReturnsOnEveryVisualFailure() {
        val policy =
            ProfessorPortraitContinuityPolicy()

        policy.onPrepareStarted()
        policy.onFirstFrameHeld()
        policy.onRevealFailed()

        assertTrue(policy.portraitVisible)
        assertFalse(policy.videoVisible)

        policy.onPrepareStarted()
        policy.onTimeout()

        assertTrue(policy.portraitVisible)
        assertFalse(policy.videoVisible)

        policy.onPrepareStarted()
        policy.onFirstFrameHeld()
        policy.onRevealSucceeded()
        policy.onVideoError()

        assertTrue(policy.portraitVisible)
        assertFalse(policy.videoVisible)
    }

    @Test
    fun staleFrameFromPreviousPlaybackCannotArmNewSpeechVideo() {
        val gate =
            FreshPlaybackFrameGate()

        val old =
            gate.beginPlayback()

        gate.onPlayerStarted(old)
        gate.onRenderingStart(old)
        assertTrue(
            gate.onFrameRendered(old)
        )

        val current =
            gate.beginPlayback()

        assertFalse(
            gate.onFrameRendered(old)
        )
        assertFalse(
            gate.onFrameRendered(current)
        )

        gate.onPlayerStarted(current)
        assertFalse(
            gate.onFrameRendered(current)
        )

        gate.onRenderingStart(current)
        assertTrue(
            gate.onFrameRendered(current)
        )
    }

    @Test
    fun quickTalkPresentationUsesBubbleAndExactlyOneQuickTalkSpeech() {
        val policy =
            QuickTalkPresentationPolicy()

        val result =
            policy.present(
                "Bonjour biloute"
            )

        assertEquals(
            "Bonjour biloute",
            result.bubbleText
        )
        assertEquals(
            "Prof Gecko",
            result.statusText
        )
        assertEquals(
            SpeechOrigin.QUICK_TALK,
            result.origin
        )
        assertEquals(
            1,
            result.speechRequestCount
        )
        assertFalse(
            result.affectsBoardLayout
        )
    }
}

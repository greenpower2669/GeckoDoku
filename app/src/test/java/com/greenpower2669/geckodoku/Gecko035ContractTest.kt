package com.greenpower2669.geckodoku

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gecko035ContractTest {
    @Test
    fun videoFailureNeverBlocksOrStopsPierre() {
        val policy = ProfessorSpeechLaunchPolicy()

        assertEquals(
            ProfessorSpeechLaunchDecision.START_VOICE_AND_REVEAL,
            policy.onVisualReady()
        )
        assertEquals(
            ProfessorSpeechLaunchDecision.START_VOICE_WITH_PNG,
            policy.onVisualFailed()
        )
        assertEquals(
            ProfessorSpeechLaunchDecision.START_VOICE_WITH_PNG,
            policy.onVisualTimeout()
        )
        assertFalse(policy.videoFailureMayStopVoice)
    }

    @Test
    fun everyProfessorSpeechOriginRequestsSpeakingVisual() {
        val policy = ProfessorSpeechVisualPolicy()

        SpeechOrigin.entries.forEach { origin ->
            assertTrue(
                "origin=$origin",
                policy.shouldAnimate(origin)
            )
        }

        assertTrue(
            policy.shouldAnimateRecordedEncouragement
        )
    }

    @Test
    fun settingsContainsSoundAnimationsAndMediaLogWithoutBoardReflow() {
        val policy = SettingsMenuPolicy()

        assertEquals(
            listOf(
                SettingsEntry.SOUND,
                SettingsEntry.ANIMATIONS,
                SettingsEntry.MEDIA_LOG
            ),
            policy.entries
        )
        assertFalse(policy.affectsBoardLayout)
    }

    @Test
    fun quickTalkUsesTheHundredLinesWithoutImmediateRepeat() {
        val policy = ProfessorQuickTalkPolicy()

        assertEquals(100, PierreSmallTalk.lines.size)

        val first =
            policy.chooseIndex(
                randomValue = 0,
                previousIndex = null
            )

        val second =
            policy.chooseIndex(
                randomValue = 0,
                previousIndex = first
            )

        assertTrue(first in PierreSmallTalk.lines.indices)
        assertTrue(second in PierreSmallTalk.lines.indices)
        assertTrue(first != second)
        assertFalse(policy.affectsBoardLayout)
    }

    @Test
    fun persistentMediaLogIsBoundedReadableAndClearable() {
        val file =
            File.createTempFile(
                "geckodoku-media",
                ".log"
            )

        try {
            val log =
                PersistentMediaLog(
                    file = file,
                    maxBytes = 180
                )

            repeat(20) { index ->
                log.append(
                    "entry-$index-" +
                        "x".repeat(24)
                )
            }

            val text = log.readText()

            assertTrue(text.isNotBlank())
            assertTrue(
                file.length() <= 180L
            )

            log.clear()

            assertEquals("", log.readText())
        } finally {
            file.delete()
        }
    }
}

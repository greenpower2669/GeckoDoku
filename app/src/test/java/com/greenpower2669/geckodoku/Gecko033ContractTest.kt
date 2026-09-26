package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Gecko033ContractTest {
    @Test
    fun mediaCatalogPointsAtUploadedAssets() {
        assertEquals(
            "gecko/IconGeckoGD.png",
            AssetMediaCatalog.GECKO_ICON
        )
        assertEquals(
            "gecko/IntroGeckoGD.mp4",
            AssetMediaCatalog.GECKO_INTRO_GD
        )
        assertEquals(
            "prof/ProfParle.mp4",
            AssetMediaCatalog.PROF_SPEECH
        )
    }

    @Test
    fun everyGeckoVideoIsMutedEvenWhenFxIsOn() {
        listOf(
            RichMediaKind.INTRO,
            RichMediaKind.GECKO_APPEARANCE,
            RichMediaKind.GECKO_DISAPPEARANCE,
            RichMediaKind.GECKO_LONG_ACTION
        ).forEach {
            kind ->

            assertTrue(
                GeckoMediaAudioPolicy
                    .mustMute(kind)
            )
        }

        assertFalse(
            GeckoMediaAudioPolicy
                .mustMute(
                    RichMediaKind
                        .PROF_LONG_ACTION
                )
        )
    }

    @Test
    fun speechVideoStartsStopsAndRestartsWithRealSpeech() {
        val policy =
            ProfessorSpeechVideoPolicy()

        assertEquals(
            ProfessorSpeechVideoCommand
                .START_SPEECH_FROM_ZERO,
            policy.onSpeechStarted(
                ProfessorVideoMode.NONE
            )
        )

        assertEquals(
            ProfessorSpeechVideoCommand
                .KEEP_PLAYING,
            policy.onSpeechStarted(
                ProfessorVideoMode.SPEECH
            )
        )

        assertEquals(
            ProfessorSpeechVideoCommand
                .START_SPEECH_FROM_ZERO,
            policy.onSpeechStarted(
                ProfessorVideoMode.ACTION
            )
        )

        assertEquals(
            ProfessorSpeechVideoCommand
                .STOP_SPEECH,
            policy.onSpeechEnded(
                ProfessorVideoMode.SPEECH
            )
        )

        assertEquals(
            ProfessorSpeechVideoCommand
                .RESTART_SPEECH_FROM_ZERO,
            policy.onSpeechClipCompleted(
                speechActive = true
            )
        )

        assertEquals(
            ProfessorSpeechVideoCommand
                .STOP_SPEECH,
            policy.onSpeechClipCompleted(
                speechActive = false
            )
        )
    }


    @Test
    fun introSequenceStartsWithGdThenKeepsExistingIntro() {
        assertEquals(
            listOf(
                AssetMediaCatalog.GECKO_INTRO_GD,
                AssetMediaCatalog.GECKO_INTRO
            ),
            IntroSequencePolicy.assets
        )
    }

    @Test
    fun titleIdentityIsOverlayOnlyAndNeverUsesIntroVideo() {
        val policy =
            TitleIdentityPolicy()

        assertEquals(
            32,
            policy.iconSizeDp
        )
        assertFalse(
            policy.affectsBoardLayout
        )
        assertFalse(
            policy.usesIntroVideoForMedallion
        )
        assertTrue(
            policy.animationDurationMs >=
                1_500L
        )
    }

    @Test
    fun smallTalkHasExactlyOneHundredLinesAndNoImmediateRepeat() {
        assertEquals(
            100,
            PierreSmallTalk.lines.size
        )

        val selector =
            PierreSmallTalkSelector()

        val first =
            selector.chooseIndex(
                randomValue = 0,
                previousIndex = null
            )

        val second =
            selector.chooseIndex(
                randomValue = 0,
                previousIndex = first
            )

        assertNotEquals(
            first,
            second
        )
        assertTrue(
            second in
                PierreSmallTalk.lines.indices
        )
    }
}

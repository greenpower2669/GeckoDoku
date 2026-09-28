package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeckoMediaAudioPolicyTest {
    @Test
    fun cellAnimationsStayHardMuted() {
        assertTrue(
            GeckoMediaAudioPolicy
                .mustMute(
                    RichMediaKind
                        .GECKO_APPEARANCE
                )
        )

        assertTrue(
            GeckoMediaAudioPolicy
                .mustMute(
                    RichMediaKind
                        .BEE_APPEARANCE
                )
        )
    }

    @Test
    fun introAndProfessorLongActionMayCarryAudio() {
        assertFalse(
            GeckoMediaAudioPolicy
                .mustMute(
                    RichMediaKind
                        .INTRO
                )
        )

        assertFalse(
            GeckoMediaAudioPolicy
                .mustMute(
                    RichMediaKind
                        .PROF_LONG_ACTION
                )
        )
    }
}

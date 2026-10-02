package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class PierrePronunciationPolicyTest {
    @Test
    fun churchIsSentToPierreAsEglize() {
        assertEquals(
            "eglize",
            PierrePronunciationPolicy
                .forSpeech(
                    "église"
                )
        )

        assertEquals(
            "Une eglize est ici.",
            PierrePronunciationPolicy
                .forSpeech(
                    "Une église est ici."
                )
        )
    }

    @Test
    fun displayedTextCanRemainUntouchedUpstream() {
        val original =
            "Cette église est dans la zone verte."

        val spoken =
            PierrePronunciationPolicy
                .forSpeech(
                    original
                )

        assertEquals(
            "Cette eglize est dans la zone verte.",
            spoken
        )
        assertEquals(
            "Cette église est dans la zone verte.",
            original
        )
    }
}

package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class PierreVoiceConfigTest {
    @Test
    fun upmcMediumUsesPierreSpeakerOne() {
        val config = PierreVoiceConfig

        assertEquals(
            "fr_FR-upmc-medium.onnx",
            config.modelFile
        )
        assertEquals(1, config.speakerId)
        assertEquals(76_733_615L, config.modelSizeBytes)
    }
}

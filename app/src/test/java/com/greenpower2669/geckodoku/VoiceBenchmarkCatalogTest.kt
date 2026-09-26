package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceBenchmarkCatalogTest {
    @Test
    fun experimentUsesSamePhraseForAllThreeEngines() {
        val variants = VoiceBenchmarkVariant.entries
        assertEquals(3, variants.size)
        assertTrue(VoiceBenchmarkCatalog.TEST_PHRASE.isNotBlank())
    }

    @Test
    fun lowAndMediumUseSameFrenchSpeakerFamily() {
        val low = VoiceBenchmarkCatalog.piper(VoiceBenchmarkVariant.PIPER_LOW)
        val medium = VoiceBenchmarkCatalog.piper(VoiceBenchmarkVariant.PIPER_MEDIUM)

        assertEquals("siwis", low.speaker)
        assertEquals(low.speaker, medium.speaker)
        assertEquals("fr_FR", low.locale)
        assertEquals(low.locale, medium.locale)
        assertTrue(low.modelSizeBytes < medium.modelSizeBytes)
        assertEquals(28_130_791L, low.modelSizeBytes)
        assertEquals(63_201_294L, medium.modelSizeBytes)
    }
}

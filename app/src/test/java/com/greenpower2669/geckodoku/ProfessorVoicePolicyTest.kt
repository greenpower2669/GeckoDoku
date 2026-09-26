package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfessorVoicePolicyTest {
    @Test
    fun explicitMaleVoiceNamesArePreferred() {
        val p = ProfessorVoicePolicy()
        assertTrue(p.looksMasculine("fr-fr-male-local"))
        assertTrue(p.looksMasculine("voix_masculin_fr"))
        assertFalse(p.looksMasculine("fr-fr-female-local"))
    }

    @Test
    fun fallbackPitchIsClearlyLowerThanNeutral() {
        assertEquals(0.78f, ProfessorVoicePolicy().fallbackPitch, 0.0001f)
    }
}

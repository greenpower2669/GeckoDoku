package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfessorDialogTextPolicyTest {
    @Test
    fun removesColonSpeakerPrefix() {
        assertEquals(
            "Cette case est intéressante.",
            ProfessorDialogTextPolicy
                .normalize(
                    "Prof Gecko : Cette case est intéressante."
                )
        )
    }

    @Test
    fun removesBulletSpeakerPrefix() {
        assertEquals(
            "Candidat unique.",
            ProfessorDialogTextPolicy
                .normalize(
                    "Prof Gecko • Candidat unique."
                )
        )
    }

    @Test
    fun leavesNaturalSentenceAndMidSentenceNameUntouched() {
        val text =
            "Appuie encore sur Prof Gecko si tu veux que je joue."

        assertEquals(
            text,
            ProfessorDialogTextPolicy
                .normalize(text)
        )
    }
}

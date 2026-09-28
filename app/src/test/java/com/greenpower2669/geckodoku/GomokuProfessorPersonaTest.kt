package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GomokuProfessorPersonaTest {
    @Test
    fun discoveryAndInfernalDescribeDifferentLearningContext() {
        assertNotEquals(
            GomokuProfessorPersona
                .livingLine(
                    ProfessorPlayerEvent
                        .GAME_STARTED,
                    GameDifficulty
                        .DISCOVERY
                ),
            GomokuProfessorPersona
                .livingLine(
                    ProfessorPlayerEvent
                        .GAME_STARTED,
                    GameDifficulty
                        .INFERNAL
                )
        )
    }

    @Test
    fun discoveryAdviceIsPedagogicalNotHostile() {
        val advice =
            GomokuProfessorPersona
                .decorateAdvice(
                    "La ligne 8 doit être surveillée.",
                    GameDifficulty
                        .DISCOVERY
                )

        assertTrue(
            advice.contains(
                "pas à pas",
                ignoreCase = true
            )
        )

        assertFalse(
            advice.contains(
                "dignité",
                ignoreCase = true
            )
        )

        assertFalse(
            advice.contains(
                "pas de cadeaux",
                ignoreCase = true
            )
        )
    }

    @Test
    fun lossMessageDoesNotPretendDiscoveryIsRuthless() {
        val message =
            GomokuProfessorPersona
                .decorateResult(
                    winner =
                        GomokuPlayer
                            .PROFESSOR,
                    base =
                        "Cinq Geckos.",
                    difficulty =
                        GameDifficulty
                            .DISCOVERY
                )

        assertTrue(
            message.contains(
                "ouverture",
                ignoreCase = true
            )
        )

        assertFalse(
            message.contains(
                "pas de cadeaux",
                ignoreCase = true
            )
        )
    }
}

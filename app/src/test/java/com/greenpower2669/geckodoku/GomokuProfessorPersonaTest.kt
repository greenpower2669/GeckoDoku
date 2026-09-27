package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GomokuProfessorPersonaTest {
    @Test
    fun discoveryNeverSoftensProfessorPersona() {
        for (
            event in
            ProfessorPlayerEvent.entries
        ) {
            assertEquals(
                GomokuProfessorPersona
                    .livingLine(
                        event,
                        GameDifficulty.DISCOVERY
                    ),
                GomokuProfessorPersona
                    .livingLine(
                        event,
                        GameDifficulty.INFERNAL
                    )
            )
        }
    }

    @Test
    fun discoveryAdviceAndVictoryStayTeasing() {
        val advice =
            GomokuProfessorPersona
                .decorateAdvice(
                    "Joue au centre.",
                    GameDifficulty.DISCOVERY
                )

        val win =
            GomokuProfessorPersona
                .decorateResult(
                    winner =
                        GomokuPlayer.PROFESSOR,
                    base =
                        "Cinq Geckos.",
                    difficulty =
                        GameDifficulty.DISCOVERY
                )

        assertTrue(
            advice.contains(
                "pas ça pour de la bonté"
            )
        )
        assertTrue(
            win.contains(
                "je ne fais pas de cadeaux"
            )
        )
    }
}

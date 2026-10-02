package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStatsNarrationTest {
    @Test
    fun professorNarratesOnlyProgressTrendsForAtMostTwoLevels() {
        val text =
            PlayerStatsNarration.build(
                listOf(
                    ProfessorDifficultyStats(
                        GameDifficulty
                            .INFERNAL,
                        DifficultyStats(
                            started = 5,
                            completed = 4,
                            assistedCompleted = 0,
                            bestStars = 5,
                            averageStars = 4,
                            speedTrend =
                                StatTrend
                                    .IMPROVING,
                            starTrend =
                                StatTrend
                                    .IMPROVING
                        )
                    ),
                    ProfessorDifficultyStats(
                        GameDifficulty
                            .MISSION_IMPOSSIBLE,
                        DifficultyStats(
                            started = 3,
                            completed = 2,
                            assistedCompleted = 0,
                            bestStars = 4,
                            averageStars = 3,
                            speedTrend =
                                StatTrend
                                    .STABLE,
                            starTrend =
                                StatTrend
                                    .STABLE
                        )
                    ),
                    ProfessorDifficultyStats(
                        GameDifficulty
                            .DEMENTIAL,
                        DifficultyStats(
                            started = 8,
                            completed = 8,
                            assistedCompleted = 0,
                            bestStars = 5,
                            averageStars = 5,
                            speedTrend =
                                StatTrend
                                    .IMPROVING,
                            starTrend =
                                StatTrend
                                    .IMPROVING
                        )
                    )
                )
            )

        assertTrue(
            text.contains(
                "Infernal"
            )
        )
        assertTrue(
            text.contains(
                "plus rapide"
            )
        )
        assertTrue(
            text.contains(
                "gagnent des étoiles"
            )
        )
        assertTrue(
            text.contains(
                "Mission Impossible"
            )
        )
        assertFalse(
            text.contains(
                "Démentiel"
            )
        )
        assertFalse(
            text.contains(
                "taux"
            )
        )
    }
}

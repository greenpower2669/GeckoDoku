package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HallPlayerProgressionProjectionTest {
    private fun entry(
        profileId: String?,
        name: String,
        seconds: Long,
        stars: Int,
        at: Long
    ) =
        HallOfFameEntry(
            playerName =
                name,
            mode =
                GameMode.SUDOKU,
            size =
                9,
            difficulty =
                GameDifficulty.EXPERT,
            stars =
                stars,
            elapsedSeconds =
                seconds,
            completedAt =
                at,
            playerProfileId =
                profileId
        )

    @Test
    fun samePseudoDifferentProfilesNeverMerge() {
        val result =
            HallPlayerProgressionProjection
                .groups(
                    listOf(
                        entry(
                            "11111111-1111-4111-8111-111111111111",
                            "GeckoTétu",
                            100,
                            4,
                            1
                        ),
                        entry(
                            "22222222-2222-4222-8222-222222222222",
                            "GeckoTétu",
                            90,
                            5,
                            2
                        )
                    )
                )

        assertEquals(
            2,
            result.size
        )

        assertTrue(
            result.all {
                it.entries.size ==
                    1
            }
        )
    }

    @Test
    fun renameKeepsOneContinuousProgression() {
        val id =
            "11111111-1111-4111-8111-111111111111"

        val result =
            HallPlayerProgressionProjection
                .groups(
                    listOf(
                        entry(
                            id,
                            "GeckoTétu",
                            120,
                            3,
                            1
                        ),
                        entry(
                            id,
                            "GeckoFou",
                            80,
                            5,
                            2
                        )
                    )
                )
                .single()

        assertEquals(
            "GeckoFou",
            result.displayName
        )

        assertEquals(
            listOf(
                "GeckoTétu",
                "GeckoFou"
            ),
            result.knownNames
        )

        assertEquals(
            StatTrend.IMPROVING,
            result.speedTrend
        )

        assertEquals(
            StatTrend.IMPROVING,
            result.starTrend
        )
    }

    @Test
    fun unidentifiedHistoricalScoresStayOutOfProgressions() {
        val values =
            listOf(
                entry(
                    null,
                    "Ancien",
                    200,
                    2,
                    1
                ),
                entry(
                    "11111111-1111-4111-8111-111111111111",
                    "Fab",
                    100,
                    4,
                    2
                )
            )

        val groups =
            HallPlayerProgressionProjection
                .groups(
                    values
                )

        assertEquals(
            1,
            groups.size
        )

        assertFalse(
            groups.single()
                .entries
                .any {
                    it.playerName ==
                        "Ancien"
                }
        )

        assertEquals(
            1,
            HallPlayerProgressionProjection
                .unidentifiedCount(
                    values
                )
        )
    }

    @Test
    fun fullProgressionKeepsMoreThanTenScores() {
        val id =
            "11111111-1111-4111-8111-111111111111"

        val values =
            (1..25).map {
                index ->

                entry(
                    profileId =
                        id,
                    name =
                        "Fab",
                    seconds =
                        200L -
                            index,
                    stars =
                        5,
                    at =
                        index.toLong()
                )
            }

        val progression =
            HallPlayerProgressionProjection
                .groups(
                    values
                )
                .single()

        assertEquals(
            25,
            progression
                .entries
                .size
        )
    }

}

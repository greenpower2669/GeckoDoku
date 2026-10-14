package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlobalHallProjectionTest {
    @Test
    fun `global cache entry is projected into Hall entry`() {
        val cached =
            GlobalScoreCacheEntry(
                scoreId = "score-1",
                runId = "run-1",
                sequence = 7L,
                receivedAt = 1000L,
                normalizedJson =
                    """{"schemaVersion":1,"scoreVersion":1,"runId":"run-1","playerName":"GeckoTétu","mode":"GECKODOKU","difficulty":"EASY","size":5,"completed":true,"stars":5,"elapsedSeconds":42,"mistakes":0,"assistancePoints":0,"usedProfessor":false,"completedAt":123456789,"appVersion":"0.15.43-dev","scoreId":"score-1","sequence":7,"receivedAt":1000}"""
            )

        val result =
            GlobalHallProjection.fromCache(
                listOf(cached)
            )

        assertEquals(1, result.size)
        assertEquals("GeckoTétu", result.single().playerName)
        assertEquals(GameMode.GECKODOKU, result.single().mode)
        assertEquals(GameDifficulty.EASY, result.single().difficulty)
        assertEquals(5, result.single().size)
        assertEquals(5, result.single().stars)
        assertEquals(42L, result.single().elapsedSeconds)
        assertEquals(123456789L, result.single().completedAt)
    }

    @Test
    fun `merge restores global score after local store is empty`() {
        val cached =
            GlobalScoreCacheEntry(
                scoreId = "score-1",
                runId = "run-1",
                sequence = 7L,
                receivedAt = 1000L,
                normalizedJson =
                    """{"runId":"run-1","playerName":"GeckoTétu","mode":"GECKODOKU","difficulty":"EASY","size":5,"completed":true,"stars":5,"elapsedSeconds":42,"mistakes":0,"assistancePoints":0,"usedProfessor":false,"completedAt":123456789,"appVersion":"0.15.43-dev","scoreId":"score-1","sequence":7,"receivedAt":1000}"""
            )

        val merged =
            GlobalHallProjection.merge(
                local = emptyList(),
                globalCache = listOf(cached)
            )

        assertEquals(1, merged.size)
        assertEquals("GeckoTétu", merged.single().playerName)
    }

    @Test
    fun `same local and global completion is shown only once`() {
        val local =
            HallOfFameEntry(
                playerName = "GeckoTétu",
                mode = GameMode.GECKODOKU,
                size = 5,
                difficulty = GameDifficulty.EASY,
                stars = 5,
                elapsedSeconds = 42L,
                completedAt = 123456789L
            )

        val cached =
            GlobalScoreCacheEntry(
                scoreId = "score-1",
                runId = "run-1",
                sequence = 7L,
                receivedAt = 1000L,
                normalizedJson =
                    """{"runId":"run-1","playerName":"GeckoTétu","mode":"GECKODOKU","difficulty":"EASY","size":5,"completed":true,"stars":5,"elapsedSeconds":42,"completedAt":123456789,"appVersion":"0.15.43-dev","scoreId":"score-1","sequence":7,"receivedAt":1000}"""
            )

        val merged =
            GlobalHallProjection.merge(
                local = listOf(local),
                globalCache = listOf(cached)
            )

        assertEquals(1, merged.size)
    }

    @Test
    fun `two separate global scores with same pseudo and time remain separate`() {
        val raw =
            """{"playerName":"GeckoTétu","mode":"GECKODOKU","difficulty":"EASY","size":5,"completed":true,"stars":5,"elapsedSeconds":42,"completedAt":123456789}"""
        val a = GlobalScoreCacheEntry(
            scoreId = "score-profile-a",
            runId = "run-profile-a",
            sequence = 1L,
            receivedAt = 1000L,
            normalizedJson = raw
        )
        val b = GlobalScoreCacheEntry(
            scoreId = "score-profile-b",
            runId = "run-profile-b",
            sequence = 2L,
            receivedAt = 1001L,
            normalizedJson = raw
        )
        // Les entrées sont légitimement distinctes malgré un rendu identique.
        assertEquals(
            2,
            GlobalHallProjection.merge(
                local = emptyList(),
                globalCache = listOf(a, b)
            ).size
        )
    }

    @Test
    fun `invalid or unfinished server rows are ignored`() {
        val invalid =
            GlobalScoreCacheEntry(
                scoreId = "score-bad",
                runId = "run-bad",
                sequence = 8L,
                receivedAt = 1001L,
                normalizedJson = """{"playerName":"X","mode":"UNKNOWN","difficulty":"EASY","size":5,"completed":true,"stars":5,"elapsedSeconds":1,"completedAt":1}"""
            )
        val unfinished =
            GlobalScoreCacheEntry(
                scoreId = "score-unfinished",
                runId = "run-unfinished",
                sequence = 9L,
                receivedAt = 1002L,
                normalizedJson = """{"playerName":"X","mode":"GECKODOKU","difficulty":"EASY","size":5,"completed":false,"stars":5,"elapsedSeconds":1,"completedAt":1}"""
            )

        val result =
            GlobalHallProjection.fromCache(
                listOf(invalid, unfinished)
            )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `global metadata carries stable player profile id`() {
        val cached =
            GlobalScoreCacheEntry(
                scoreId =
                    "score-profile",
                runId =
                    "run-profile",
                sequence =
                    10L,
                receivedAt =
                    1003L,
                normalizedJson =
                    """{"playerName":"GeckoTétu","mode":"SUDOKU","difficulty":"EXPERT","size":9,"completed":true,"stars":5,"elapsedSeconds":88,"completedAt":123456790,"metadata":{"playerProfileId":"11111111-1111-4111-8111-111111111111"}}"""
            )

        val result =
            GlobalHallProjection
                .fromCache(
                    listOf(
                        cached
                    )
                )
                .single()

        assertEquals(
            "11111111-1111-4111-8111-111111111111",
            result.playerProfileId
        )
    }

    @Test
    fun `exact local match can hydrate missing remote profile id without guessing by pseudo`() {
        val local =
            HallOfFameEntry(
                playerName =
                    "Fab",
                mode =
                    GameMode.SUDOKU,
                size =
                    9,
                difficulty =
                    GameDifficulty.EXPERT,
                stars =
                    4,
                elapsedSeconds =
                    91L,
                completedAt =
                    123456791L,
                playerProfileId =
                    "11111111-1111-4111-8111-111111111111"
            )

        val cached =
            GlobalScoreCacheEntry(
                scoreId =
                    "score-hydrate",
                runId =
                    "run-hydrate",
                sequence =
                    11L,
                receivedAt =
                    1004L,
                normalizedJson =
                    """{"playerName":"Fab","mode":"SUDOKU","difficulty":"EXPERT","size":9,"completed":true,"stars":4,"elapsedSeconds":91,"completedAt":123456791}"""
            )

        val merged =
            GlobalHallProjection
                .merge(
                    local =
                        listOf(
                            local
                        ),
                    globalCache =
                        listOf(
                            cached
                        )
                )

        assertEquals(
            1,
            merged.size
        )

        assertEquals(
            local.playerProfileId,
            merged.single()
                .playerProfileId
        )
    }


    @Test
    fun `known different profiles with identical visible score are never deduplicated`() {
        val local =
            HallOfFameEntry(
                playerName =
                    "GeckoTétu",
                mode =
                    GameMode.GECKODOKU,
                size =
                    5,
                difficulty =
                    GameDifficulty.EASY,
                stars =
                    5,
                elapsedSeconds =
                    42L,
                completedAt =
                    123456789L,
                playerProfileId =
                    "11111111-1111-4111-8111-111111111111"
            )

        val cached =
            GlobalScoreCacheEntry(
                scoreId =
                    "score-profile-b",
                runId =
                    "run-profile-b",
                sequence =
                    12L,
                receivedAt =
                    1005L,
                normalizedJson =
                    """{"playerName":"GeckoTétu","mode":"GECKODOKU","difficulty":"EASY","size":5,"completed":true,"stars":5,"elapsedSeconds":42,"completedAt":123456789,"metadata":{"playerProfileId":"22222222-2222-4222-8222-222222222222"}}"""
            )

        val merged =
            GlobalHallProjection
                .merge(
                    local =
                        listOf(
                            local
                        ),
                    globalCache =
                        listOf(
                            cached
                        )
                )

        assertEquals(
            2,
            merged.size
        )

        assertTrue(
            merged.mapNotNull {
                it.playerProfileId
            }
                .containsAll(
                    listOf(
                        "11111111-1111-4111-8111-111111111111",
                        "22222222-2222-4222-8222-222222222222"
                    )
                )
        )
    }

}

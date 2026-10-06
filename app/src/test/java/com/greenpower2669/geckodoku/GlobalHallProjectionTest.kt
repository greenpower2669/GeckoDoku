package com.greenpower2669.geckodoku

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
}

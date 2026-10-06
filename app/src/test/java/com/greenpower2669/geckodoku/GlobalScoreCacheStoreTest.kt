package com.greenpower2669.geckodoku

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GlobalScoreCacheStoreTest {
    @get:Rule
    val temp = TemporaryFolder()

    private fun entry(
        scoreId: String,
        runId: String = "run-$scoreId",
        sequence: Long = 1L,
        receivedAt: Long = 100L,
        stars: Int = 5
    ) = GlobalScoreCacheEntry(
        scoreId = scoreId,
        runId = runId,
        sequence = sequence,
        receivedAt = receivedAt,
        normalizedJson = "{\"scoreId\":\"$scoreId\",\"runId\":\"$runId\",\"sequence\":$sequence,\"receivedAt\":$receivedAt,\"stars\":$stars}"
    )

    @Test
    fun pageMergeAndCursorSurviveReload() {
        val dir = temp.newFolder()
        val store = GlobalScoreCacheStore(dir)

        assertTrue(store.applyPage(listOf(entry("s1"), entry("s2", sequence = 2)), "opaque:2"))

        val reloaded = GlobalScoreCacheStore(dir)
        assertEquals("opaque:2", reloaded.cursor())
        assertNotNull(reloaded.entry("s1"))
        assertNotNull(reloaded.entry("s2"))
        assertEquals(2, reloaded.state().entries.size)
    }

    @Test
    fun replayingSamePageIsIdempotentByScoreId() {
        val dir = temp.newFolder()
        val store = GlobalScoreCacheStore(dir)
        val page = listOf(entry("s1", sequence = 1), entry("s2", sequence = 2))

        assertTrue(store.applyPage(page, "2"))
        assertTrue(store.applyPage(page, "2"))

        assertEquals(2, store.state().entries.size)
        assertEquals("2", store.cursor())
    }

    @Test
    fun sameScoreIdUpdatesStoredServerRepresentationWithoutDuplicate() {
        val dir = temp.newFolder()
        val store = GlobalScoreCacheStore(dir)

        assertTrue(store.applyPage(listOf(entry("s1", stars = 4)), "1"))
        assertTrue(store.applyPage(listOf(entry("s1", stars = 5, receivedAt = 200)), "2"))

        assertEquals(1, store.state().entries.size)
        assertTrue(store.entry("s1")!!.normalizedJson.contains("\"stars\":5"))
        assertEquals(200L, store.entry("s1")!!.receivedAt)
        assertEquals("2", store.cursor())
    }

    @Test
    fun emptyPageCanAdvanceOpaqueCursor() {
        val dir = temp.newFolder()
        val store = GlobalScoreCacheStore(dir)

        assertTrue(store.applyPage(emptyList(), "empty-but-more:7"))
        assertEquals("empty-but-more:7", GlobalScoreCacheStore(dir).cursor())
        assertEquals(0, store.state().entries.size)
    }

    @Test
    fun incompleteTemporaryFileNeverOverridesCommittedEntriesOrCursor() {
        val dir = temp.newFolder()
        val store = GlobalScoreCacheStore(dir)
        assertTrue(store.applyPage(listOf(entry("safe")), "safe-cursor"))

        File(dir, GlobalScoreCacheStore.TEMP_FILE_NAME).writeText("{broken")

        val reloaded = GlobalScoreCacheStore(dir)
        assertEquals("safe-cursor", reloaded.cursor())
        assertNotNull(reloaded.entry("safe"))
        assertNull(reloaded.entry("missing"))
    }
}

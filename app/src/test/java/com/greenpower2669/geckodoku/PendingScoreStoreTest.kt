package com.greenpower2669.geckodoku

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PendingScoreStoreTest {
    @get:Rule
    val temp = TemporaryFolder()

    private fun directory(): File =
        temp.newFolder()

    @Test
    fun enqueueSurvivesStoreRecreationAndPreservesExactJson() {
        val dir = directory()
        val payload =
            "{\"runId\":\"run-1\",\"playerName\":\"GeckoTétu\",\"metadata\":{\"z\":1,\"a\":2}}"

        val first = PendingScoreStore(dir)
        assertTrue(
            first.enqueue(
                runId = "run-1",
                payloadJson = payload,
                now = 1000L
            )
        )

        val reloaded = PendingScoreStore(dir)
        val entry = reloaded.find("run-1")

        assertNotNull(entry)
        assertEquals(payload, entry!!.payloadJson)
        assertEquals("run-1", entry.runId)
        assertEquals(1000L, entry.addedAt)
        assertEquals(0, entry.attempts)
        assertEquals(PendingScoreState.PENDING, entry.state)
    }

    @Test
    fun sameRunIdAndSameJsonIsIdempotentNoOp() {
        val dir = directory()
        val store = PendingScoreStore(dir)
        val payload = "{\"runId\":\"same\",\"stars\":5}"

        assertTrue(store.enqueue("same", payload, 10L))
        assertTrue(store.enqueue("same", payload, 99L))

        val entries = store.entries()
        assertEquals(1, entries.size)
        assertEquals(10L, entries.single().addedAt)
        assertEquals(payload, entries.single().payloadJson)
    }

    @Test
    fun sameRunIdWithDifferentJsonIsRejectedWithoutMutation() {
        val dir = directory()
        val store = PendingScoreStore(dir)

        assertTrue(
            store.enqueue(
                "conflict",
                "{\"runId\":\"conflict\",\"stars\":5}",
                10L
            )
        )

        assertFalse(
            store.enqueue(
                "conflict",
                "{\"runId\":\"conflict\",\"stars\":4}",
                20L
            )
        )

        assertEquals(
            "{\"runId\":\"conflict\",\"stars\":5}",
            store.find("conflict")!!.payloadJson
        )
    }

    @Test
    fun retryMetadataPersistsWithoutChangingPayloadOrRunId() {
        val dir = directory()
        val payload = "{\"runId\":\"retry-1\",\"stars\":4}"
        val store = PendingScoreStore(dir)

        assertTrue(store.enqueue("retry-1", payload, 100L))
        assertTrue(
            store.markRetry(
                runId = "retry-1",
                nextAttemptAt = 5100L,
                httpStatus = 503,
                errorCode = "STORAGE_UNAVAILABLE"
            )
        )

        val entry = PendingScoreStore(dir).find("retry-1")!!
        assertEquals("retry-1", entry.runId)
        assertEquals(payload, entry.payloadJson)
        assertEquals(1, entry.attempts)
        assertEquals(5100L, entry.nextAttemptAt)
        assertEquals(503, entry.lastHttpStatus)
        assertEquals("STORAGE_UNAVAILABLE", entry.lastErrorCode)
        assertEquals(PendingScoreState.PENDING, entry.state)
    }

    @Test
    fun blockedEntryIsRetainedAcrossReload() {
        val dir = directory()
        val store = PendingScoreStore(dir)

        assertTrue(store.enqueue("blocked", "{\"runId\":\"blocked\"}", 1L))
        assertTrue(
            store.markBlocked(
                runId = "blocked",
                httpStatus = 409,
                errorCode = "RUN_ID_CONFLICT"
            )
        )

        val entry = PendingScoreStore(dir).find("blocked")!!
        assertEquals(PendingScoreState.BLOCKED, entry.state)
        assertEquals(409, entry.lastHttpStatus)
        assertEquals("RUN_ID_CONFLICT", entry.lastErrorCode)
        assertEquals("{\"runId\":\"blocked\"}", entry.payloadJson)
    }

    @Test
    fun maxEntriesRefusesNewScoreWithoutEvictingExistingScores() {
        val dir = directory()
        val store = PendingScoreStore(dir, maxEntries = 2)

        assertTrue(store.enqueue("one", "{\"runId\":\"one\"}", 1L))
        assertTrue(store.enqueue("two", "{\"runId\":\"two\"}", 2L))
        assertFalse(store.enqueue("three", "{\"runId\":\"three\"}", 3L))

        assertEquals(2, store.entries().size)
        assertNotNull(store.find("one"))
        assertNotNull(store.find("two"))
        assertNull(store.find("three"))
    }

    @Test
    fun removeDeletesOnlyRequestedConfirmedEntry() {
        val dir = directory()
        val store = PendingScoreStore(dir)

        assertTrue(store.enqueue("one", "{\"runId\":\"one\"}", 1L))
        assertTrue(store.enqueue("two", "{\"runId\":\"two\"}", 2L))
        assertTrue(store.remove("one"))

        assertNull(PendingScoreStore(dir).find("one"))
        assertNotNull(PendingScoreStore(dir).find("two"))
    }

    @Test
    fun incompleteTemporaryFileDoesNotReplaceLastValidSnapshot() {
        val dir = directory()
        val store = PendingScoreStore(dir)
        val payload = "{\"runId\":\"safe\",\"stars\":5}"

        assertTrue(store.enqueue("safe", payload, 1L))

        File(dir, PendingScoreStore.TEMP_FILE_NAME)
            .writeText("{broken")

        val reloaded = PendingScoreStore(dir)
        assertEquals(payload, reloaded.find("safe")!!.payloadJson)
    }
}

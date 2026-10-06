package com.greenpower2669.geckodoku

import java.io.File
import java.util.ArrayDeque
import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GlobalScoreSyncCoordinatorTest {
    @get:Rule
    val temp = TemporaryFolder()

    private class FakeRemote : GeckoDokuHallRemote {
        val posts = mutableListOf<String>()
        val postResults = ArrayDeque<ScorePostResult>()
        val syncResults = ArrayDeque<SyncPageResult>()
        var syncCalls = 0

        override fun postScore(payloadJson: String): ScorePostResult {
            posts += payloadJson
            return postResults.removeFirst()
        }

        override fun fetchSync(cursor: String, limit: Int): SyncPageResult {
            syncCalls += 1
            return syncResults.removeFirst()
        }
    }

    private class QueueExecutor : Executor {
        val tasks = ArrayDeque<Runnable>()
        override fun execute(command: Runnable) {
            tasks.addLast(command)
        }
        fun runNext() = tasks.removeFirst().run()
    }

    private class FakeScheduler : SyncDelayScheduler {
        val delays = mutableListOf<Long>()
        val tasks = mutableListOf<() -> Unit>()
        override fun schedule(delayMs: Long, task: () -> Unit) {
            delays += delayMs
            tasks += task
        }
        override fun shutdown() = Unit
    }

    private data class Fixture(
        val pending: PendingScoreStore,
        val cache: GlobalScoreCacheStore,
        val remote: FakeRemote,
        val executor: QueueExecutor,
        val scheduler: FakeScheduler,
        val coordinator: GlobalScoreSyncCoordinator,
        val now: LongArray
    )

    private fun fixture(): Fixture {
        val root = temp.newFolder()
        val pending = PendingScoreStore(File(root, "pending"))
        val cache = GlobalScoreCacheStore(File(root, "cache"))
        val remote = FakeRemote()
        val executor = QueueExecutor()
        val scheduler = FakeScheduler()
        val now = longArrayOf(1_000L)
        val coordinator = GlobalScoreSyncCoordinator(
            pendingStore = pending,
            cacheStore = cache,
            remote = remote,
            clock = { now[0] },
            executor = executor,
            scheduler = scheduler
        )
        return Fixture(pending, cache, remote, executor, scheduler, coordinator, now)
    }

    @Test
    fun simultaneousTriggersCoalesceToOneWorkerAndOnePost() {
        val f = fixture()
        f.pending.enqueue("r1", "{\"runId\":\"r1\"}", 1L)
        f.remote.postResults += ScorePostResult.Accepted(false, "s1", "r1", 1L)
        f.remote.syncResults += SyncPageResult.Success(
            HallSyncPage(emptyList(), "0", false, "0", 1L)
        )

        f.coordinator.start()
        f.coordinator.triggerNow()
        f.coordinator.triggerNow()

        assertEquals(1, f.executor.tasks.size)
        f.executor.runNext()
        assertEquals(1, f.remote.posts.size)
        assertNull(f.pending.find("r1"))
    }

    @Test
    fun existingPendingIsSentOnStartAndAcceptedIsRemoved() {
        val f = fixture()
        val payload = "{\"runId\":\"boot\",\"stars\":5}"
        f.pending.enqueue("boot", payload, 1L)
        f.remote.postResults += ScorePostResult.Accepted(false, "s1", "boot", 1L)
        f.remote.syncResults += SyncPageResult.Success(
            HallSyncPage(emptyList(), "0", false, "0", 1L)
        )

        f.coordinator.start()
        f.executor.runNext()

        assertEquals(listOf(payload), f.remote.posts)
        assertNull(f.pending.find("boot"))
    }

    @Test
    fun blocked409IsKeptAndNeverRegeneratesRunId() {
        val f = fixture()
        val payload = "{\"runId\":\"conflict\",\"stars\":5}"
        f.pending.enqueue("conflict", payload, 1L)
        f.remote.postResults += ScorePostResult.Blocked(409, "RUN_ID_CONFLICT")
        f.remote.syncResults += SyncPageResult.Success(
            HallSyncPage(emptyList(), "0", false, "0", 1L)
        )

        f.coordinator.start()
        f.executor.runNext()

        val entry = f.pending.find("conflict")!!
        assertEquals(PendingScoreState.BLOCKED, entry.state)
        assertEquals(payload, entry.payloadJson)
        assertEquals("conflict", entry.runId)
    }

    @Test
    fun rateLimitUsesRetryAfterAnd503UsesProgressiveBackoff() {
        val rate = fixture()
        rate.pending.enqueue("rate", "{\"runId\":\"rate\"}", 1L)
        rate.remote.postResults += ScorePostResult.Retry(429, 60L, "RATE_LIMITED")
        rate.remote.syncResults += SyncPageResult.Success(
            HallSyncPage(emptyList(), "0", false, "0", 1L)
        )
        rate.coordinator.start()
        rate.executor.runNext()
        assertEquals(61_000L, rate.pending.find("rate")!!.nextAttemptAt)
        assertEquals(60_000L, rate.scheduler.delays.single())

        val service = fixture()
        service.pending.enqueue("svc", "{\"runId\":\"svc\"}", 1L)
        service.remote.postResults += ScorePostResult.Retry(503, null, "STORAGE_UNAVAILABLE")
        service.remote.syncResults += SyncPageResult.Success(
            HallSyncPage(emptyList(), "0", false, "0", 1L)
        )
        service.coordinator.start()
        service.executor.runNext()
        assertEquals(6_000L, service.pending.find("svc")!!.nextAttemptAt)
        assertEquals(5_000L, service.scheduler.delays.single())
    }

    @Test
    fun retryPostsExactSamePayloadAndRunId() {
        val f = fixture()
        val payload = "{\"runId\":\"same\",\"metadata\":{\"z\":1,\"a\":2}}"
        f.pending.enqueue("same", payload, 1L)
        f.remote.postResults += ScorePostResult.Retry(503, null, "STORAGE_UNAVAILABLE")
        f.remote.syncResults += SyncPageResult.Success(
            HallSyncPage(emptyList(), "0", false, "0", 1L)
        )
        f.coordinator.start()
        f.executor.runNext()

        f.now[0] = 6_000L
        f.remote.postResults += ScorePostResult.Accepted(true, "s1", "same", 1L)
        f.remote.syncResults += SyncPageResult.Success(
            HallSyncPage(emptyList(), "0", false, "0", 1L)
        )
        f.scheduler.tasks.single().invoke()
        f.executor.runNext()

        assertEquals(listOf(payload, payload), f.remote.posts)
        assertNull(f.pending.find("same"))
    }

    @Test
    fun syncContinuesAcrossEmptyPageWhenHasMoreAndPersistsCursor() {
        val f = fixture()
        f.remote.syncResults += SyncPageResult.Success(
            HallSyncPage(emptyList(), "cursor-1", true, "2", 1L)
        )
        f.remote.syncResults += SyncPageResult.Success(
            HallSyncPage(
                listOf(
                    HallSyncEntry("s2", "r2", 2L, 200L, "{\"scoreId\":\"s2\"}")
                ),
                "cursor-2",
                false,
                "2",
                2L
            )
        )

        f.coordinator.syncGlobalNowBlocking()

        assertEquals(2, f.remote.syncCalls)
        assertEquals("cursor-2", f.cache.cursor())
        assertEquals("r2", f.cache.entry("s2")!!.runId)
    }

    @Test
    fun syncReconcilesOwnRunIdAndRemovesConfirmedPending() {
        val f = fixture()
        f.pending.enqueue("mine", "{\"runId\":\"mine\"}", 1L)
        f.remote.syncResults += SyncPageResult.Success(
            HallSyncPage(
                listOf(
                    HallSyncEntry("server-score", "mine", 9L, 900L, "{\"scoreId\":\"server-score\"}")
                ),
                "9",
                false,
                "9",
                900L
            )
        )

        f.coordinator.syncGlobalNowBlocking()

        assertNull(f.pending.find("mine"))
        assertTrue(f.cache.entry("server-score") != null)
    }
}

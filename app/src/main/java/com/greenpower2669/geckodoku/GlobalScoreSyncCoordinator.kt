package com.greenpower2669.geckodoku

import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

interface GeckoDokuHallRemote {
    fun postScore(payloadJson: String): ScorePostResult
    fun fetchSync(cursor: String, limit: Int = 100): SyncPageResult
}

class ApiClientHallRemote(
    private val client: GeckoDokuHallApiClient
) : GeckoDokuHallRemote {
    override fun postScore(payloadJson: String): ScorePostResult =
        client.postScore(payloadJson)

    override fun fetchSync(cursor: String, limit: Int): SyncPageResult =
        client.fetchSync(cursor, limit)
}

interface SyncDelayScheduler {
    fun schedule(delayMs: Long, task: () -> Unit)
    fun shutdown()
}

class ScheduledSyncDelayScheduler(
    private val executor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor()
) : SyncDelayScheduler {
    override fun schedule(delayMs: Long, task: () -> Unit) {
        executor.schedule(
            { task() },
            delayMs.coerceAtLeast(0L),
            TimeUnit.MILLISECONDS
        )
    }

    override fun shutdown() {
        executor.shutdownNow()
    }
}

class GlobalScoreSyncCoordinator(
    private val pendingStore: PendingScoreStore,
    private val cacheStore: GlobalScoreCacheStore,
    private val remote: GeckoDokuHallRemote,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val executor: Executor = Executors.newSingleThreadExecutor(),
    private val scheduler: SyncDelayScheduler = ScheduledSyncDelayScheduler()
) {
    private val started = AtomicBoolean(false)
    private val workerScheduled = AtomicBoolean(false)

    fun start() {
        if (started.compareAndSet(false, true)) {
            triggerNow()
        }
    }

    fun stop() {
        started.set(false)
        scheduler.shutdown()
        (executor as? ExecutorService)?.shutdownNow()
    }

    fun enqueue(
        runId: String,
        payloadJson: String,
        now: Long = clock()
    ): Boolean {
        val stored = pendingStore.enqueue(runId, payloadJson, now)
        if (stored) {
            triggerNow()
        }
        return stored
    }

    fun triggerNow() {
        if (!started.get()) {
            return
        }

        if (!workerScheduled.compareAndSet(false, true)) {
            return
        }

        executor.execute {
            try {
                drainPendingNow()
                syncGlobalNowBlocking()
            } finally {
                workerScheduled.set(false)
                if (started.get() && hasDuePending()) {
                    triggerNow()
                }
            }
        }
    }

    fun syncGlobalNow() {
        triggerNow()
    }

    internal fun drainPendingNow() {
        val now = clock()
        var earliestFuture: Long? = null

        for (entry in pendingStore.entries()) {
            if (entry.state != PendingScoreState.PENDING) {
                continue
            }

            if (entry.nextAttemptAt > now) {
                earliestFuture = minOfNullable(earliestFuture, entry.nextAttemptAt)
                continue
            }

            when (val result = remote.postScore(entry.payloadJson)) {
                is ScorePostResult.Accepted -> {
                    if (result.runId == entry.runId) {
                        pendingStore.remove(entry.runId)
                    } else {
                        pendingStore.markBlocked(
                            entry.runId,
                            httpStatus = 200,
                            errorCode = "INVALID_ACK_RUN_ID"
                        )
                    }
                }

                is ScorePostResult.Blocked -> {
                    pendingStore.markBlocked(
                        entry.runId,
                        httpStatus = result.httpStatus,
                        errorCode = result.errorCode
                    )
                }

                is ScorePostResult.Retry -> {
                    val delay = GlobalScoreRetryPolicy.delayMs(
                        attemptNumber = entry.attempts + 1,
                        retryAfterSeconds = result.retryAfterSeconds
                    )
                    pendingStore.markRetry(
                        runId = entry.runId,
                        nextAttemptAt = safeAdd(now, delay),
                        httpStatus = result.httpStatus,
                        errorCode = result.errorCode
                    )
                    scheduleTrigger(delay)
                    return
                }
            }
        }

        earliestFuture?.let {
            scheduleTrigger((it - now).coerceAtLeast(0L))
        }
    }

    internal fun syncGlobalNowBlocking() {
        var pages = 0

        while (pages < MAX_SYNC_PAGES_PER_RUN) {
            pages += 1
            val cursor = cacheStore.cursor()

            when (val result = remote.fetchSync(cursor, 100)) {
                is SyncPageResult.Success -> {
                    val page = result.page
                    val cacheEntries = page.entries.map {
                        GlobalScoreCacheEntry(
                            scoreId = it.scoreId,
                            runId = it.runId,
                            sequence = it.sequence,
                            receivedAt = it.receivedAt,
                            normalizedJson = it.normalizedJson
                        )
                    }

                    if (!cacheStore.applyPage(cacheEntries, page.nextCursor)) {
                        return
                    }

                    page.entries.forEach {
                        serverEntry ->
                        if (pendingStore.find(serverEntry.runId) != null) {
                            pendingStore.remove(serverEntry.runId)
                        }
                    }

                    if (!page.hasMore) {
                        return
                    }
                }

                is SyncPageResult.Retry,
                is SyncPageResult.Blocked ->
                    return
            }
        }
    }

    private fun hasDuePending(): Boolean {
        val now = clock()
        return pendingStore.entries().any {
            it.state == PendingScoreState.PENDING &&
                it.nextAttemptAt <= now
        }
    }

    private fun scheduleTrigger(delayMs: Long) {
        if (!started.get()) {
            return
        }
        scheduler.schedule(delayMs.coerceAtLeast(0L)) {
            triggerNow()
        }
    }

    private fun safeAdd(first: Long, second: Long): Long =
        if (second > 0L && first > Long.MAX_VALUE - second) {
            Long.MAX_VALUE
        } else {
            first + second
        }

    private fun minOfNullable(first: Long?, second: Long): Long =
        first?.let { minOf(it, second) } ?: second

    companion object {
        private const val MAX_SYNC_PAGES_PER_RUN = 1000
    }
}

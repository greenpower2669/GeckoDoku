package com.greenpower2669.geckodoku

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * The next grid size and attempt counts survive activity recreation,
 * app restarts and Android's rescheduling of WorkManager.
 */
object InfernalWorkshopScheduler {
    private const val PREF = "infernal_workshop_state"
    private const val PERIODIC = "infernal_workshop_periodic_v1"
    private const val ONCE = "infernal_workshop_once_v1"
    private const val NEXT_SIZE = "next_size_v2"
    private const val ATTEMPTS = "attempts"
    private val cursorLock = Any()
    internal val workerLock = ReentrantLock()

    fun running(context: Context): Boolean =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean("enabled", false)

    fun nextSize(context: Context): Int =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(NEXT_SIZE, InfernalWorkshopPolicy.MIN_SIZE)
            .takeIf { it in InfernalWorkshopPolicy.sizes }
            ?: InfernalWorkshopPolicy.MIN_SIZE

    fun attempts(context: Context): Long =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getLong(ATTEMPTS, 0L)

    fun attemptsFor(context: Context, size: Int): Long {
        require(size in InfernalWorkshopPolicy.sizes)
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getLong("attempts_v2_$size", 0L)
    }

    fun lastAttemptAt(context: Context, size: Int): Long {
        require(size in InfernalWorkshopPolicy.sizes)
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getLong("last_attempt_v2_$size", 0L)
    }

    fun start(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", true).commit()
        val manager = WorkManager.getInstance(context.applicationContext)
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiresBatteryNotLow(true)
            .build()
        val periodic = PeriodicWorkRequestBuilder<InfernalWorkshopWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints).build()
        // KEEP preserves the existing periodic job after an APK update.
        manager.enqueueUniquePeriodicWork(
            PERIODIC, ExistingPeriodicWorkPolicy.KEEP, periodic
        )
        runNow(context)
    }

    fun runNow(context: Context) {
        if (!running(context)) return
        val job = OneTimeWorkRequestBuilder<InfernalWorkshopWorker>()
            .setConstraints(Constraints.Builder()
                .setRequiresBatteryNotLow(true).build())
            .build()
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(ONCE, ExistingWorkPolicy.KEEP, job)
    }

    fun pause(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", false).commit()
        val manager = WorkManager.getInstance(context.applicationContext)
        manager.cancelUniqueWork(ONCE)
        manager.cancelUniqueWork(PERIODIC)
    }

    /** Reserve the next size BEFORE work begins to avoid starvation on timeout. */
    internal fun claimNextSize(context: Context): Int = synchronized(cursorLock) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val size = nextSize(context)
        val key = "attempts_v2_$size"
        prefs.edit()
            .putInt(NEXT_SIZE, InfernalWorkshopPolicy.nextSize(size))
            .putLong(ATTEMPTS, prefs.getLong(ATTEMPTS, 0L) + 1L)
            .putLong(key, prefs.getLong(key, 0L) + 1L)
            .putLong("last_attempt_v2_$size", System.currentTimeMillis())
            .commit()
        size
    }
}

class InfernalWorkshopWorker(
    context: Context,
    parameters: WorkerParameters
) : Worker(context, parameters) {
    override fun doWork(): Result {
        val context = applicationContext
        if (!InfernalWorkshopScheduler.running(context)) return Result.success()
        // A periodic job and a manual one must not run simultaneous searches.
        if (!InfernalWorkshopScheduler.workerLock.tryLock()) return Result.success()
        try {
            val store = InfernalWorkshopStore(context)
            val deadline = System.nanoTime() + 25_000_000_000L
            var turn = 0
            while (turn < 3 && !isStopped &&
                InfernalWorkshopScheduler.running(context) &&
                System.nanoTime() < deadline
            ) {
                val size = InfernalWorkshopScheduler.claimNextSize(context)
                val seed = System.nanoTime() xor
                    (System.currentTimeMillis() + turn * 104_729L)
                try {
                    val proof = PuzzleGenerator.generateWorkshopCandidate(
                        size, seed
                    ) {
                        isStopped ||
                            !InfernalWorkshopScheduler.running(context) ||
                            System.nanoTime() >= deadline
                    }
                    if (proof != null && !isStopped &&
                        InfernalWorkshopScheduler.running(context) &&
                        System.nanoTime() < deadline
                    ) {
                        store.add(proof)
                    }
                } catch (_: Exception) {
                    // A bad candidate must not destroy the next size's turn.
                }
                turn++
            }
            return Result.success()
        } finally {
            InfernalWorkshopScheduler.workerLock.unlock()
        }
    }
}

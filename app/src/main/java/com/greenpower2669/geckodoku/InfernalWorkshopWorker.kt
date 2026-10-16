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

/**
 * Android schedules background jobs opportunistically; there is no promise
 * of continuous execution after the screen turns off.
 */
object InfernalWorkshopScheduler {
    private const val PREF = "infernal_workshop_state"
    private const val PERIODIC = "infernal_workshop_periodic_v1"
    private const val ONCE = "infernal_workshop_once_v1"

    fun running(context: Context): Boolean =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean("enabled", false)

    fun size(context: Context): Int =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt("size", 9).coerceIn(5, 12)

    fun attempts(context: Context): Long =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getLong("attempts", 0L)

    fun start(context: Context, size: Int) {
        require(size in 5..12)
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", true).putInt("size", size).commit()
        val workManager = WorkManager.getInstance(context.applicationContext)
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiresBatteryNotLow(true)
            .build()
        val periodic = PeriodicWorkRequestBuilder<InfernalWorkshopWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints).build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC, ExistingPeriodicWorkPolicy.KEEP, periodic
        )
        runNow(context)
    }

    fun runNow(context: Context) {
        if (!running(context)) return
        val work = OneTimeWorkRequestBuilder<InfernalWorkshopWorker>()
            .setConstraints(Constraints.Builder()
                .setRequiresBatteryNotLow(true).build())
            .build()
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(ONCE, ExistingWorkPolicy.KEEP, work)
    }

    fun pause(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", false).commit()
        val manager = WorkManager.getInstance(context.applicationContext)
        manager.cancelUniqueWork(ONCE)
        manager.cancelUniqueWork(PERIODIC)
    }

    internal fun countAttempt(context: Context) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        synchronized(this) {
            prefs.edit()
                .putLong("attempts", prefs.getLong("attempts", 0) + 1L)
                .commit()
        }
    }
}

class InfernalWorkshopWorker(
    context: Context,
    parameters: WorkerParameters
) : Worker(context, parameters) {
    override fun doWork(): Result {
        val ctx = applicationContext
        if (!InfernalWorkshopScheduler.running(ctx)) return Result.success()
        val store = InfernalWorkshopStore(ctx)
        val size = InfernalWorkshopScheduler.size(ctx)
        // Persist after each attempt; JobScheduler may terminate this worker.
        val deadline = System.nanoTime() + 25_000_000_000L
        var turn = 0
        while (turn < 3 && !isStopped &&
            InfernalWorkshopScheduler.running(ctx) &&
            System.nanoTime() < deadline
        ) {
            val seed = System.nanoTime() xor
                (System.currentTimeMillis() + turn * 104_729L)
            InfernalWorkshopScheduler.countAttempt(ctx)
            try {
                val proof = PuzzleGenerator.generateWorkshopCandidate(
                    size, seed
                ) {
                    isStopped ||
                        !InfernalWorkshopScheduler.running(ctx) ||
                        System.nanoTime() >= deadline
                }
                if (proof != null && !isStopped &&
                    InfernalWorkshopScheduler.running(ctx)
                ) store.add(proof)
            } catch (_: Exception) {
                // Do not destroy saved candidates or retry forever on bad data.
            }
            turn++
        }
        return Result.success()
    }
}

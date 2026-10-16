package com.greenpower2669.geckodoku

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.WorkManager

/** Continuous user-initiated service, with durable per-size progress. */
object InfernalWorkshopScheduler {
    private const val PREF = "infernal_workshop_state"
    private const val PERIODIC = "infernal_workshop_periodic_v1"
    private const val ONCE = "infernal_workshop_once_v1"
    private const val NEXT_SIZE = "next_size_v2"
    private const val ATTEMPTS = "attempts"
    private val lock = Any()

    fun enabled(context: Context): Boolean =
        prefs(context).getBoolean("enabled", false)

    /** Backward-compatible UI alias, NOT proof a service is alive. */
    fun running(context: Context): Boolean = enabled(context)

    fun status(context: Context): ContinuousWorkshopStatus =
        ContinuousWorkshopStatus.from(
            enabled(context),
            prefs(context).getBoolean("service_active_v3", false),
            prefs(context).getLong("heartbeat_v3", 0L),
            System.currentTimeMillis()
        )

    fun nextSize(context: Context): Int =
        prefs(context).getInt(NEXT_SIZE, InfernalWorkshopPolicy.MIN_SIZE)
            .takeIf { it in InfernalWorkshopPolicy.sizes }
            ?: InfernalWorkshopPolicy.MIN_SIZE

    fun attempts(context: Context): Long = prefs(context).getLong(ATTEMPTS, 0L)

    fun attemptsFor(context: Context, size: Int): Long =
        prefs(context).getLong("attempts_v2_$size", 0L)

    fun completedFor(context: Context, size: Int): Long =
        prefs(context).getLong("completed_v3_$size", 0L)

    fun savedFor(context: Context, size: Int): Long =
        prefs(context).getLong("saved_v3_$size", 0L)

    fun errorsFor(context: Context, size: Int): Long =
        prefs(context).getLong("errors_v3_$size", 0L)

    fun lastError(context: Context): String =
        prefs(context).getString("last_error_v3", "") ?: ""

    fun lastHeartbeat(context: Context): Long =
        prefs(context).getLong("heartbeat_v3", 0L)

    fun lastAttemptAt(context: Context, size: Int): Long =
        prefs(context).getLong("last_attempt_v2_$size", 0L)

    fun currentSize(context: Context): Int =
        prefs(context).getInt("current_size_v3", 0)

    fun start(context: Context) {
        // The old periodic jobs had battery/charging constraints and
        // could remain at zero attempts indefinitely. Retire both.
        WorkManager.getInstance(context.applicationContext).apply {
            cancelUniqueWork(PERIODIC)
            cancelUniqueWork(ONCE)
        }
        prefs(context).edit()
            .putBoolean("enabled", true)
            .putString("last_error_v3", "")
            .commit()
        val intent = Intent(context, InfernalWorkshopService::class.java)
            .setAction(InfernalWorkshopService.ACTION_START)
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.i("InfernalWorkshop", "START_REQUEST user action")
            MediaTrace.install(context)
            MediaTrace.event(source = "InfernalWorkshop", event = "START_REQUEST")
        } catch (error: RuntimeException) {
            prefs(context).edit()
                .putBoolean("enabled", false)
                .putBoolean("service_active_v3", false)
                .putString("last_error_v3",
                    "START_FAILED: " + error.javaClass.simpleName + ": " +
                        (error.message ?: "").take(180))
                .commit()
            Log.e("InfernalWorkshop", "START_FAILED", error)
            throw error
        }
    }

    fun pause(context: Context) {
        prefs(context).edit().putBoolean("enabled", false).commit()
        context.startService(
            Intent(context, InfernalWorkshopService::class.java)
                .setAction(InfernalWorkshopService.ACTION_STOP)
        )
        WorkManager.getInstance(context.applicationContext).apply {
            cancelUniqueWork(PERIODIC)
            cancelUniqueWork(ONCE)
        }
        Log.i("InfernalWorkshop", "STOP_REQUEST user action")
        MediaTrace.event(source = "InfernalWorkshop", event = "STOP_REQUEST")
    }

    internal fun isEnabled(context: Context): Boolean = enabled(context)

    internal fun serviceStarted(context: Context) {
        prefs(context).edit()
            .putBoolean("service_active_v3", true)
            .putLong("heartbeat_v3", System.currentTimeMillis())
            .commit()
    }

    internal fun serviceStopped(context: Context) {
        prefs(context).edit()
            .putBoolean("service_active_v3", false)
            .putInt("current_size_v3", 0)
            .commit()
    }

    internal fun heartbeat(context: Context) {
        prefs(context).edit()
            .putLong("heartbeat_v3", System.currentTimeMillis()).commit()
    }

    internal fun reportProblem(context: Context, message: String) {
        prefs(context).edit()
            .putString("last_error_v3", message.take(240))
            .commit()
        Log.e("InfernalWorkshop", message.take(240))
        MediaTrace.event(
            source = "InfernalWorkshop", event = "ERROR",
            detail = message.take(240)
        )
    }

    /** A counter increment means a search has actually started. */
    internal fun claimNextSize(context: Context): Int = synchronized(lock) {
        val p = prefs(context)
        val size = nextSize(context)
        val key = "attempts_v2_$size"
        val ok = p.edit()
            .putInt(NEXT_SIZE, InfernalWorkshopPolicy.nextSize(size))
            .putInt("current_size_v3", size)
            .putLong(ATTEMPTS, p.getLong(ATTEMPTS, 0L) + 1)
            .putLong(key, p.getLong(key, 0L) + 1)
            .putLong("last_attempt_v2_$size", System.currentTimeMillis())
            .putLong("heartbeat_v3", System.currentTimeMillis())
            .commit()
        check(ok) { "Le compteur d'essais n'a pas pu être sauvegardé." }
        size
    }

    internal fun recordFinish(context: Context, size: Int, saved: Boolean, error: String? = null) =
        synchronized(lock) {
            val p = prefs(context)
            val key = if (error != null) "errors_v3_$size" else "completed_v3_$size"
            val edit = p.edit()
                .putLong(key, p.getLong(key, 0L) + 1L)
                .putLong("heartbeat_v3", System.currentTimeMillis())
            if (saved) {
                val foundKey = "saved_v3_$size"
                edit.putLong(foundKey, p.getLong(foundKey, 0L) + 1L)
            }
            if (error != null) edit.putString("last_error_v3", error.take(240))
            edit.commit()
        }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
}

enum class ContinuousWorkshopStatus {
    STOPPED, STARTING, RUNNING, INTERRUPTED;

    companion object {
        fun from(
            enabled: Boolean,
            serviceActive: Boolean,
            heartbeat: Long,
            now: Long
        ): ContinuousWorkshopStatus = when {
            !enabled -> STOPPED
            serviceActive && heartbeat > 0 &&
                now >= heartbeat && now - heartbeat <= 120_000L -> RUNNING
            heartbeat == 0L -> STARTING
            else -> INTERRUPTED
        }
    }
}

/** Disable the legacy jobs from installed earlier test builds. */
class InfernalWorkshopWorker(
    context: Context,
    parameters: androidx.work.WorkerParameters
) : androidx.work.Worker(context, parameters) {
    override fun doWork(): Result = Result.success()
}

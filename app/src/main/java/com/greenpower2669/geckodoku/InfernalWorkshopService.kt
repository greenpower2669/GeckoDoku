package com.greenpower2669.geckodoku

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A foreground service started by an explicit user action.
 *
 * It searches serially while running, including with the screen off, but
 * Android can still kill or throttle a foreground service. User can stop
 * from either the persistent notification or the workshop menu.
 */
class InfernalWorkshopService : Service() {
    companion object {
        const val ACTION_START = "com.greenpower2669.geckodoku.WORKSHOP_START"
        const val ACTION_STOP = "com.greenpower2669.geckodoku.WORKSHOP_STOP"
        private const val CHANNEL_ID = "geckodoku_workshop_continuous_v3"
        private const val NOTIFICATION_ID = 4327
        private const val LOG = "InfernalWorkshop"
    }

    private val canceled = AtomicBoolean(false)
    @Volatile private var workThread: Thread? = null
    private var lastNoticeAt = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID, "Recherche GeckoDoku en continu",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Atelier de génération des grilles 5×5 à 12×12"
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP || !InfernalWorkshopScheduler.isEnabled(this)) {
            canceled.set(true)
            workThread?.interrupt()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        // Promote the service immediately to foreground.
        try {
            val notification = notification("Démarrage de l'exploration…")
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(
                    NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (error: Exception) {
            InfernalWorkshopScheduler.reportProblem(
                this, "NOTIFICATION_START_FAILED: " +
                    error.javaClass.simpleName + ": " + error.message
            )
            InfernalWorkshopScheduler.serviceStopped(this)
            stopSelf()
            return START_NOT_STICKY
        }

        if (workThread?.isAlive != true) {
            canceled.set(false)
            InfernalWorkshopScheduler.serviceStarted(this)
            workThread = Thread({ exploreContinuously() }, "GeckoWorkshopExplore").apply {
                isDaemon = true
                start()
            }
            Log.i(LOG, "SERVICE_ACTIVE persistent foreground notification")
        }
        return START_STICKY
    }

    private fun exploreContinuously() {
        val store = InfernalWorkshopStore(this)
        val manager = getSystemService(NotificationManager::class.java)
        val power = getSystemService(PowerManager::class.java)
        try {
            while (!canceled.get() && InfernalWorkshopScheduler.isEnabled(this)) {
                InfernalWorkshopScheduler.heartbeat(this)
                if (Build.VERSION.SDK_INT >= 29 &&
                    power.currentThermalStatus >= PowerManager.THERMAL_STATUS_SEVERE
                ) {
                    notifyProgress(manager, "Pause thermique • reprise automatique", true)
                    Log.w(LOG, "THERMAL_PAUSE")
                    if (!rest(10_000L)) break
                    continue
                }
                if (power.isPowerSaveMode) {
                    notifyProgress(manager, "Économie de batterie • pause de recherche", true)
                    Log.i(LOG, "BATTERY_SAVER_PAUSE")
                    if (!rest(10_000L)) break
                    continue
                }

                val size = InfernalWorkshopScheduler.claimNextSize(this)
                val total = InfernalWorkshopScheduler.attempts(this)
                Log.i(LOG, "SEARCH_START size=${size}x${size} attempt=$total")
                notifyProgress(manager, "Recherche ${size}×${size} • essai $total", false)

                var error: String? = null
                var saved = false
                try {
                    // A bounded *single attempt* avoids starving the next size.
                    val cutoff = System.nanoTime() + 20_000_000_000L
                    val seed = System.nanoTime() xor System.currentTimeMillis()
                    val result = PuzzleGenerator.generateWorkshopCandidate(
                        size, seed
                    ) {
                        canceled.get() ||
                            !InfernalWorkshopScheduler.isEnabled(this) ||
                            Thread.currentThread().isInterrupted ||
                            System.nanoTime() >= cutoff
                    }
                    if (result != null && !canceled.get() &&
                        InfernalWorkshopScheduler.isEnabled(this)
                    ) {
                        saved = store.add(result)
                    }
                } catch (ex: Exception) {
                    error = "SEARCH_ERROR ${size}x${size} " +
                        ex.javaClass.simpleName + ": " + ex.message
                    Log.e(LOG, error ?: "SEARCH_ERROR", ex)
                } finally {
                    if (!canceled.get() && InfernalWorkshopScheduler.isEnabled(this)) {
                        InfernalWorkshopScheduler.recordFinish(this, size, saved, error)
                    }
                }
                Log.i(LOG, "SEARCH_END size=${size}x${size} saved=$saved error=${error ?: "none"}")
                if (error != null) InfernalWorkshopScheduler.reportProblem(this, error)
                if (!rest(250L)) break
            }
        } catch (fatal: Exception) {
            InfernalWorkshopScheduler.reportProblem(
                this, "SERVICE_ERROR " + fatal.javaClass.simpleName + ": " + fatal.message
            )
            Log.e(LOG, "SERVICE_CRASH", fatal)
        } finally {
            InfernalWorkshopScheduler.serviceStopped(this)
            Log.i(LOG, "SERVICE_STOPPED total=" + InfernalWorkshopScheduler.attempts(this))
            stopSelf()
        }
    }

    private fun rest(ms: Long): Boolean {
        var remaining = ms
        while (remaining > 0 && !canceled.get() &&
            InfernalWorkshopScheduler.isEnabled(this)
        ) {
            try {
                val slice = minOf(remaining, 1_000L)
                Thread.sleep(slice)
                remaining -= slice
                InfernalWorkshopScheduler.heartbeat(this)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return false
            }
        }
        return !canceled.get() && InfernalWorkshopScheduler.isEnabled(this)
    }

    private fun notifyProgress(manager: NotificationManager, message: String, force: Boolean) {
        val now = System.currentTimeMillis()
        if (force || now - lastNoticeAt > 7_000L) {
            try {
                manager.notify(NOTIFICATION_ID, notification(message))
                lastNoticeAt = now
            } catch (error: RuntimeException) {
                Log.w(LOG, "NOTIFICATION_UPDATE_FAILED", error)
            }
        }
    }

    private fun notification(message: String): Notification {
        val activity = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stop = PendingIntent.getService(
            this, 1,
            Intent(this, InfernalWorkshopService::class.java)
                .setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle("GeckoDoku • recherche continue")
            .setContentText(message)
            .setContentIntent(activity)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, "Arrêter", stop)
            .build()
    }

    override fun onDestroy() {
        canceled.set(true)
        workThread?.interrupt()
        InfernalWorkshopScheduler.serviceStopped(this)
        super.onDestroy()
    }
}

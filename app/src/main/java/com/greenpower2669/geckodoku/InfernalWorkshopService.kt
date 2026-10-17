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
        MediaTrace.install(this)
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
        if (intent?.action == ACTION_STOP) {
            InfernalWorkshopScheduler.stoppedByNotification(this)
        }
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
            MediaTrace.event(
                source = LOG, event = "SERVICE_ACTIVE",
                detail = "mode=CONTINUOUS foreground=true"
            )
        }
        return START_STICKY
    }

    private fun exploreContinuously() {
        val store = InfernalWorkshopStore(this)
        val manager = getSystemService(NotificationManager::class.java)
        val power = getSystemService(PowerManager::class.java)
        var exploreComparisons = true
        var preferH1 = true
        val xWingQueue = WorkshopXWingQueueStore(this)
        try {
            while (!canceled.get() && InfernalWorkshopScheduler.isEnabled(this)) {
                InfernalWorkshopScheduler.heartbeat(this)
                if (Build.VERSION.SDK_INT >= 29 &&
                    power.currentThermalStatus >= PowerManager.THERMAL_STATUS_SEVERE
                ) {
                    InfernalWorkshopScheduler.setPhase(this, "PAUSE THERMIQUE")
                    notifyProgress(manager, "Pause thermique • reprise automatique", true)
                    Log.w(LOG, "THERMAL_PAUSE")
                    MediaTrace.event(
                        source = LOG, event = "THERMAL_PAUSE",
                        detail = "thermalStatus=${power.currentThermalStatus}"
                    )
                    if (!rest(10_000L)) break
                    continue
                }
                if (power.isPowerSaveMode) {
                    InfernalWorkshopScheduler.setPhase(this, "PAUSE ÉCONOMIE DE BATTERIE")
                    notifyProgress(manager, "Économie de batterie • pause de recherche", true)
                    Log.i(LOG, "BATTERY_SAVER_PAUSE")
                    MediaTrace.event(source = LOG, event = "BATTERY_SAVER_PAUSE")
                    if (!rest(10_000L)) break
                    continue
                }

                // A/B tests: same durable X-Wing puzzles, two independent
                // proof contracts, with generation interleaved between
                // completed evaluations. Old v7 outcome fields are preserved.
                val checkpoints = WorkshopCheckpointStore(this)
                val preferred = if (preferH1) {
                    WorkshopComparisonLane.H1_XWING
                } else {
                    WorkshopComparisonLane.H2_NO_XWING
                }
                val secondary = if (preferH1) {
                    WorkshopComparisonLane.H2_NO_XWING
                } else {
                    WorkshopComparisonLane.H1_XWING
                }
                val firstBoard = if (exploreComparisons) {
                    xWingQueue.nextUntried(preferred)
                } else null
                val otherBoard = if (exploreComparisons && firstBoard == null) {
                    xWingQueue.nextUntried(secondary)
                } else null
                val experimental = firstBoard ?: otherBoard
                val lane = if (firstBoard != null) preferred else secondary

                if (experimental != null) {
                    exploreComparisons = false
                    preferH1 = !preferH1
                    val board = experimental.puzzle
                    var lastProofPulse = System.nanoTime()
                    fun proofPaused(): Boolean {
                        val now = System.nanoTime()
                        if (now - lastProofPulse >= 5_000_000_000L) {
                            lastProofPulse = now
                            InfernalWorkshopScheduler.heartbeat(this)
                        }
                        return canceled.get() ||
                            !InfernalWorkshopScheduler.isEnabled(this) ||
                            Thread.currentThread().isInterrupted ||
                            power.isPowerSaveMode ||
                            (Build.VERSION.SDK_INT >= 29 &&
                                power.currentThermalStatus >= PowerManager.THERMAL_STATUS_SEVERE)
                    }
                    InfernalWorkshopScheduler.setPhase(
                        this, "COMPARATIF ${lane.label} • ${board.size}×${board.size}"
                    )
                    notifyProgress(
                        manager, "Atelier • ${lane.label} • ${board.size}×${board.size}",
                        true
                    )
                    try {
                        val outcome = PuzzleGenerator.proveArchivedComparison(
                            board, lane,
                            shouldCancel = { proofPaused() },
                            proofArchive = checkpoints
                        )
                        if (!proofPaused() && outcome != null) {
                            xWingQueue.completeExperiment(
                                experimental.id, lane, outcome
                            )
                            Log.i(LOG,
                                "COMPARISON_COMPLETE lane=${lane.name} size=${board.size} " +
                                    "match=${outcome.match} depth=${outcome.depth}"
                            )
                            MediaTrace.event(
                                source = LOG, event = "COMPARISON_COMPLETE",
                                detail = "lane=${lane.name} size=${board.size} " +
                                    "match=${outcome.match}"
                            )
                        } else {
                            Log.i(LOG,
                                "COMPARISON_PAUSED lane=${lane.name} id=${experimental.id.take(12)}"
                            )
                        }
                    } catch (error: Exception) {
                        val label = "COMPARISON_ERROR ${lane.name} ${board.size}×${board.size}: " +
                            error.javaClass.simpleName + ": " + error.message
                        InfernalWorkshopScheduler.reportProblem(this, label)
                        Log.e(LOG, label, error)
                        // Preserve the existing puzzle without writing a verdict.
                        if (!rest(2_000L)) break
                    }
                    if (!rest(250L)) break
                    continue
                }
                exploreComparisons = true

                val previous = checkpoints.load()
                val size = previous?.size ?: InfernalWorkshopScheduler.claimNextSize(this)
                val seed = previous?.seed ?:
                    (System.nanoTime() xor System.currentTimeMillis())
                InfernalWorkshopScheduler.setPhase(this, "RECHERCHE ${size}×${size}")
                val total = InfernalWorkshopScheduler.attempts(this)
                Log.i(LOG, "SEARCH_START size=${size}x${size} attempt=$total resumed=${previous != null}")
                notifyProgress(manager,
                    "Recherche ${size}×${size} • ${if (previous != null) "reprise" else "essai $total"}",
                    false
                )

                fun environmentPause(): Boolean =
                    (Build.VERSION.SDK_INT >= 29 &&
                        power.currentThermalStatus >= PowerManager.THERMAL_STATUS_SEVERE) ||
                        power.isPowerSaveMode

                var error: String? = null
                var saved = false
                var lastPulse = System.nanoTime()
                try {
                    val result = PuzzleGenerator.generateWorkshopCandidate(
                        size, seed,
                        shouldCancel = {
                            // Heartbeat is for UI liveness only; NEVER a
                            // search deadline or a reason to reject a puzzle.
                            val now = System.nanoTime()
                            if (now - lastPulse >= 5_000_000_000L) {
                                lastPulse = now
                                InfernalWorkshopScheduler.heartbeat(this)
                            }
                            canceled.get() ||
                                !InfernalWorkshopScheduler.isEnabled(this) ||
                                Thread.currentThread().isInterrupted ||
                                environmentPause()
                        },
                        onFirstUnique = {
                            InfernalWorkshopScheduler.markDeletionUnique(this, size)
                        },
                        resume = previous,
                        onCheckpoint = { checkpoints.save(it) },
                        proofArchive = checkpoints,
                        onXWingDetected = { board, step ->
                            if (xWingQueue.enqueue(board, step)) {
                                InfernalWorkshopScheduler.recordXWingDetected(
                                    this, board.size
                                )
                                Log.i(LOG,
                                    "XWING_ARCHIVED size=${board.size} givens=${board.givens.size}"
                                )
                                MediaTrace.event(
                                    source = LOG, event = "XWING_ARCHIVED",
                                    detail = "size=${board.size}"
                                )
                            }
                        },
                        deferDetectedXWingProof = true
                    )

                    val paused = canceled.get() ||
                        !InfernalWorkshopScheduler.isEnabled(this) ||
                        Thread.currentThread().isInterrupted ||
                        environmentPause()
                    if (!paused) {
                        if (result != null) saved = store.add(result)
                        // No result without cancellation means these
                        // geometries were really exhausted. Advance size.
                        checkpoints.clear()
                        InfernalWorkshopScheduler.recordFinish(this, size, saved)
                    } else {
                        InfernalWorkshopScheduler.setPhase(this, "RECHERCHE SUSPENDUE")
                        Log.i(LOG, "SEARCH_PAUSED size=${size}x${size} checkpoint retained")
                    }
                } catch (ex: Exception) {
                    error = "SEARCH_ERROR ${size}x${size} " +
                        ex.javaClass.simpleName + ": " + ex.message
                    Log.e(LOG, error, ex)
                    InfernalWorkshopScheduler.reportProblem(this, error)
                    // Keep the last committed checkpoint for the next attempt.
                }
                Log.i(LOG, "SEARCH_END size=${size}x${size} saved=$saved error=${error ?: "none"}")
                MediaTrace.event(
                    source = LOG, event = if (error == null) "SEARCH_END" else "SEARCH_ERROR",
                    detail = "size=${size}x${size} saved=$saved error=${error ?: "none"}"
                )
                if (environmentPause()) {
                    InfernalWorkshopScheduler.setPhase(this, "PAUSE THERMIQUE / BATTERIE")
                    if (!rest(10_000L)) break
                    continue
                }
                if (error != null) {
                    if (!rest(2_000L)) break
                    continue
                }
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
            MediaTrace.event(
                source = LOG, event = "SERVICE_STOPPED",
                detail = "total=" + InfernalWorkshopScheduler.attempts(this)
            )
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
        val stop = PendingIntent.getForegroundService(
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

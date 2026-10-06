package com.greenpower2669.geckodoku

import android.content.Context
import java.io.File

class GlobalScoreRuntime(
    context: Context
) {
    private val directory =
        File(
            context.applicationContext.filesDir,
            DIRECTORY_NAME
        )

    private val pendingStore =
        PendingScoreStore(directory)

    private val cacheStore =
        GlobalScoreCacheStore(directory)

    private val apiClient =
        GeckoDokuHallApiClient(
            UrlConnectionHallHttpTransport()
        )

    private val coordinator =
        GlobalScoreSyncCoordinator(
            pendingStore = pendingStore,
            cacheStore = cacheStore,
            remote = ApiClientHallRemote(apiClient)
        )

    val publisher =
        GlobalScoreCompletionPublisher(
            pendingStore = pendingStore,
            triggerSync = {
                coordinator.triggerNow()
            }
        )

    private val networkMonitor =
        AndroidNetworkMonitor(
            context = context.applicationContext,
            onAvailable = {
                coordinator.triggerNow()
            }
        )

    private var started = false

    @Synchronized
    fun start() {
        if (started) {
            return
        }

        coordinator.start()
        networkMonitor.start()
        started = true
    }

    @Synchronized
    fun stop() {
        if (!started) {
            return
        }

        networkMonitor.stop()
        coordinator.stop()
        started = false
    }

    companion object {
        const val DIRECTORY_NAME =
            "global-hall-v1"
    }
}

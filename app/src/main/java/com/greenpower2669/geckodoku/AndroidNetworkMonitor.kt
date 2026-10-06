package com.greenpower2669.geckodoku

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network

class AndroidNetworkMonitor(
    context: Context,
    private val onAvailable: () -> Unit
) {
    private val connectivityManager =
        context.applicationContext
            .getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

    private val callback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onAvailable()
            }
        }

    private var registered = false

    fun start() {
        if (registered) {
            return
        }
        connectivityManager.registerDefaultNetworkCallback(
            callback
        )
        registered = true
    }

    fun stop() {
        if (!registered) {
            return
        }
        try {
            connectivityManager.unregisterNetworkCallback(
                callback
            )
        } catch (_: IllegalArgumentException) {
            // Already unregistered by the platform.
        }
        registered = false
    }
}

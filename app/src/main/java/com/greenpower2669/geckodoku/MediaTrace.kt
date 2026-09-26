package com.greenpower2669.geckodoku

import android.os.SystemClock
import android.util.Log
import java.util.concurrent.atomic.AtomicLong

object MediaTrace {
    private const val TAG =
        "GeckoDokuMediaTrace"

    private val sequence =
        AtomicLong(0L)

    fun event(
        source: String,
        event: String,
        assetPath: String? = null,
        detail: String? = null
    ) {
        val line =
            buildString {
                append("#")
                append(
                    sequence
                        .incrementAndGet()
                )
                append(" t=")
                append(
                    SystemClock
                        .elapsedRealtime()
                )
                append(" source=")
                append(source)
                append(" event=")
                append(event)

                if (!assetPath.isNullOrBlank()) {
                    append(" asset=")
                    append(assetPath)
                }

                if (!detail.isNullOrBlank()) {
                    append(" ")
                    append(detail)
                }
            }

        Log.i(
            TAG,
            line
        )
    }
}

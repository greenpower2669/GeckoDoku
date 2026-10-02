package com.greenpower2669.geckodoku

import android.content.Context
import android.os.SystemClock
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

object MediaTrace {
    private const val TAG =
        "GeckoDokuMediaTrace"

    private const val LOG_FILE =
        "geckodoku-media.log"

    private val sequence =
        AtomicLong(0L)

    @Volatile
    private var persistent:
        PersistentMediaLog? = null

    fun install(
        context: Context
    ) {
        if (persistent != null) return

        synchronized(this) {
            if (persistent == null) {
                persistent =
                    PersistentMediaLog(
                        File(
                            context.applicationContext
                                .filesDir,
                            LOG_FILE
                        )
                    )
            }
        }
    }

    fun event(
        source: String,
        event: String,
        assetPath: String? = null,
        detail: String? = null
    ) {
        val line =
            buildString {
                append(
                    SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss.SSS",
                        Locale.getDefault()
                    ).format(Date())
                )
                append(" #")
                append(
                    sequence.incrementAndGet()
                )
                append(" t=")
                append(
                    SystemClock.elapsedRealtime()
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

        Log.i(TAG, line)
        persistent?.append(line)
    }

    fun readPersistent(): String =
        persistent?.readText().orEmpty()

    fun clearPersistent() {
        persistent?.clear()
    }

    fun persistentFileName(): String =
        LOG_FILE
}

package com.greenpower2669.geckodoku

import java.io.File

class PersistentMediaLog(
    private val file: File,
    private val maxBytes: Int =
        DEFAULT_MAX_BYTES
) {
    @Synchronized
    fun append(line: String) {
        try {
            file.parentFile?.mkdirs()
            file.appendText(
                line.trimEnd() + "\n"
            )
            trimIfNeeded()
        } catch (_: Exception) {
            // Diagnostics must never affect gameplay.
        }
    }

    @Synchronized
    fun readText(): String =
        try {
            if (file.exists()) file.readText() else ""
        } catch (_: Exception) {
            ""
        }

    @Synchronized
    fun clear() {
        try {
            file.parentFile?.mkdirs()
            file.writeText("")
        } catch (_: Exception) {
            // Diagnostics must never affect gameplay.
        }
    }

    private fun trimIfNeeded() {
        if (
            maxBytes <= 0 ||
            !file.exists() ||
            file.length() <= maxBytes
        ) return

        val bytes = file.readBytes()
        var start =
            (bytes.size - maxBytes)
                .coerceAtLeast(0)

        while (
            start < bytes.size &&
            bytes[start] != '\n'.code.toByte()
        ) {
            start += 1
        }

        if (start < bytes.size) {
            start += 1
        }

        val kept =
            if (start < bytes.size) {
                bytes.copyOfRange(
                    start,
                    bytes.size
                )
            } else {
                ByteArray(0)
            }

        file.writeBytes(kept)
    }

    companion object {
        const val DEFAULT_MAX_BYTES =
            256 * 1024
    }
}

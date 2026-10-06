package com.greenpower2669.geckodoku

import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class AtomicJsonFileStore(
    private val file: File
) {
    fun read(): String? =
        try {
            if (file.isFile) {
                file.readText(
                    StandardCharsets.UTF_8
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }

    fun writeAtomically(
        content: String
    ): Boolean {
        val parent =
            file.parentFile
                ?: return false

        if (!parent.exists() && !parent.mkdirs()) {
            return false
        }

        val temp =
            File(
                parent,
                file.name + ".tmp"
            )

        return try {
            FileOutputStream(temp, false)
                .use {
                    stream ->
                    stream.write(
                        content.toByteArray(
                            StandardCharsets.UTF_8
                        )
                    )
                    stream.flush()
                    stream.fd.sync()
                }

            try {
                Files.move(
                    temp.toPath(),
                    file.toPath(),
                    StandardCopyOption
                        .ATOMIC_MOVE,
                    StandardCopyOption
                        .REPLACE_EXISTING
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(
                    temp.toPath(),
                    file.toPath(),
                    StandardCopyOption
                        .REPLACE_EXISTING
                )
            }

            true
        } catch (_: Exception) {
            false
        }
    }
}

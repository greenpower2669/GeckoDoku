package com.greenpower2669.geckodoku

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.io.OutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SpriteBankExporter {
    fun write(
        context: Context,
        output: OutputStream
    ) {
        val appContext =
            context.applicationContext

        ZipOutputStream(
            output.buffered()
        ).use {
            zip ->
            putText(
                zip,
                "sprites/index.json",
                SpriteBankFactory
                    .globalIndexJson(
                        appContext
                    )
                    .toString(2)
            )

            putText(
                zip,
                "sprites/sprite-factory-report.json",
                SpriteBankFactory
                    .reportJson(
                        appContext
                    )
                    .toString(2)
            )

            val exportedBanks =
                SpriteBankFactory
                    .validatedBankDirectories(
                        appContext
                    )

            exportedBanks.forEach {
                dir ->
                addDirectory(
                    zip = zip,
                    source = dir,
                    prefix =
                        "sprites/" +
                            dir.name +
                            "/"
                )
            }

            val stableRoot =
                SpriteBankFactory
                    .stableRoot(
                        appContext
                    )

            if (stableRoot.isDirectory) {
                addDirectory(
                    zip = zip,
                    source =
                        stableRoot,
                    prefix =
                        "sprites/stable-frames/"
                )
            }

            val integrity =
                JSONObject()
                    .apply {
                        put(
                            "exportedBanks",
                            exportedBanks.size
                        )
                        put(
                            "generatedAt",
                            System
                                .currentTimeMillis()
                        )
                        put(
                            "note",
                            "Place the exported sprites/ directory under app assets for APK integration."
                        )
                    }

            putText(
                zip,
                "sprites/export-manifest.json",
                integrity.toString(2)
            )
        }
    }

    private fun addDirectory(
        zip: ZipOutputStream,
        source: File,
        prefix: String
    ) {
        source.listFiles()
            .orEmpty()
            .sortedBy {
                it.name
            }
            .forEach {
                file ->
                if (file.isDirectory) {
                    addDirectory(
                        zip,
                        file,
                        prefix +
                            file.name +
                            "/"
                    )
                } else if (
                    !file.name
                        .endsWith(
                            ".tmp"
                        ) &&
                    file.name !=
                        "building.json"
                ) {
                    addFile(
                        zip,
                        file,
                        prefix +
                            file.name
                    )
                }
            }
    }

    private fun addFile(
        zip: ZipOutputStream,
        source: File,
        name: String
    ) {
        zip.putNextEntry(
            ZipEntry(name)
        )

        source.inputStream()
            .buffered()
            .use {
                input ->
                input.copyTo(zip)
            }

        zip.closeEntry()
    }

    private fun putText(
        zip: ZipOutputStream,
        name: String,
        value: String
    ) {
        zip.putNextEntry(
            ZipEntry(name)
        )
        zip.write(
            value.toByteArray(
                Charsets.UTF_8
            )
        )
        zip.closeEntry()
    }
}

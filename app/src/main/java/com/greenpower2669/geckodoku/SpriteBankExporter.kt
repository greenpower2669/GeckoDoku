package com.greenpower2669.geckodoku

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SpriteBankExporter {
    fun write(
        context: Context,
        output: OutputStream
    ) {
        val heights =
            SpriteBankFactory
                .resolutionBankSummaries(
                    context
                )
                .filter {
                    it.generatedFrames > 0
                }
                .map {
                    it.resolutionHeight
                }

        writeInternal(
            context = context,
            output = output,
            resolutionHeights =
                heights
        )
    }

    fun write(
        context: Context,
        output: OutputStream,
        resolutionHeight: Int
    ) {
        writeInternal(
            context = context,
            output = output,
            resolutionHeights =
                listOf(
                    resolutionHeight
                )
        )
    }

    private fun writeInternal(
        context: Context,
        output: OutputStream,
        resolutionHeights: List<Int>
    ) {
        val appContext =
            context.applicationContext
        val selected =
            resolutionHeights
                .distinct()
                .sorted()

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

            val exported =
                JSONArray()

            selected.forEach {
                height ->
                val summary =
                    SpriteBankFactory
                        .resolutionBankSummary(
                            appContext,
                            height
                        )
                val prefix =
                    "sprites/banks/" +
                        height +
                        "p/"

                putText(
                    zip,
                    prefix +
                        "bank-manifest.json",
                    SpriteBankFactory
                        .resolutionBankManifestJson(
                            appContext,
                            height
                        )
                        .toString(2)
                )

                val directories =
                    SpriteBankFactory
                        .resolutionBankDirectories(
                            appContext,
                            height
                        )

                directories.forEach {
                    dir ->
                    addDirectory(
                        zip = zip,
                        source = dir,
                        prefix =
                            prefix +
                                dir.name +
                                "/"
                    )
                }

                exported.put(
                    JSONObject()
                        .apply {
                            put(
                                "resolutionHeight",
                                height
                            )
                            put(
                                "state",
                                summary.state.name
                            )
                            put(
                                "percentage",
                                summary.percentage
                            )
                            put(
                                "generatedFrames",
                                summary.generatedFrames
                            )
                            put(
                                "expectedFrames",
                                summary.expectedFrames
                            )
                            put(
                                "sizeBytes",
                                summary.sizeBytes
                            )
                            put(
                                "directories",
                                directories.size
                            )
                        }
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
                            "formatVersion",
                            SpriteBankFactory
                                .FORMAT_VERSION
                        )
                        put(
                            "generatorVersion",
                            SpriteBankFactory
                                .GENERATOR_VERSION
                        )
                        put(
                            "generatedAt",
                            System
                                .currentTimeMillis()
                        )
                        put(
                            "resolutionBanks",
                            exported
                        )
                        put(
                            "note",
                            "Each sprites/banks/<resolution>p directory is an independent logical bank. Complete banks can be copied directly under app assets."
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
                        )
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

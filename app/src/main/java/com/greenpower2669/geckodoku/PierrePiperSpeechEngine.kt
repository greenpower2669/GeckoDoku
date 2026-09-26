package com.greenpower2669.geckodoku

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class PierrePiperSpeechEngine(
    context: Context
) {
    private val appContext =
        context.applicationContext

    private val handler =
        Handler(Looper.getMainLooper())

    private val executor =
        Executors
            .newSingleThreadExecutor()

    private val player =
        VoicePcmPlayer()

    @Volatile
    private var generation = 0

    @Volatile
    private var released = false

    private var tts:
        OfflineTts? = null

    fun speak(
        text: String,
        onStarted:
            (() -> Unit)? = null,
        onCompletion:
            (() -> Unit)? = null,
        onFailure:
            (() -> Unit)? = null
    ): Boolean {
        if (
            released ||
            text.isBlank()
        ) {
            return false
        }

        val token =
            synchronized(this) {
                generation += 1
                player.stop()
                generation
            }

        executor.execute {
            try {
                val engine =
                    ensureTts()

                val audio =
                    engine.generate(
                        text = text,
                        sid =
                            PierreVoiceConfig
                                .speakerId,
                        speed = 1.0f
                    )

                handler.post {
                    if (
                        released ||
                        token != generation
                    ) {
                        return@post
                    }

                    player.play(
                        samples =
                            audio.samples,
                        sampleRate =
                            audio.sampleRate,
                        onStarted = {
                            if (
                                !released &&
                                token ==
                                    generation
                            ) {
                                onStarted
                                    ?.invoke()
                            }
                        },
                        onCompletion = {
                            if (
                                !released &&
                                token ==
                                    generation
                            ) {
                                onCompletion
                                    ?.invoke()
                            }
                        }
                    )
                }
            } catch (_: Throwable) {
                handler.post {
                    if (
                        !released &&
                        token == generation
                    ) {
                        onFailure?.invoke()
                    }
                }
            }
        }

        return true
    }

    fun stop() {
        synchronized(this) {
            generation += 1
            player.stop()
        }
    }

    fun release() {
        if (released) {
            return
        }

        released = true
        stop()
        player.release()

        executor.execute {
            tts?.release()
            tts = null
        }

        executor.shutdown()
    }

    @Synchronized
    private fun ensureTts():
        OfflineTts {
        tts?.let {
            return it
        }

        ensureAsset(
            PierreVoiceConfig
                .modelAssetPath
        )

        ensureAsset(
            PierreVoiceConfig
                .tokensAssetPath
        )

        val dataDir =
            prepareEspeakData()

        val vits =
            OfflineTtsVitsModelConfig(
                model =
                    PierreVoiceConfig
                        .modelAssetPath,
                tokens =
                    PierreVoiceConfig
                        .tokensAssetPath,
                dataDir =
                    dataDir
                        .absolutePath
            )

        val created =
            OfflineTts(
                assetManager =
                    appContext.assets,
                config =
                    OfflineTtsConfig(
                        model =
                            OfflineTtsModelConfig(
                                vits = vits,
                                numThreads = 2,
                                debug = false,
                                provider = "cpu"
                            ),
                        maxNumSentences = 1,
                        silenceScale = 0.2f
                    )
            )

        tts = created

        return created
    }

    private fun ensureAsset(
        path: String
    ) {
        appContext.assets
            .open(path)
            .use {
                // Validation only.
            }
    }

    private fun prepareEspeakData():
        File {
        val root =
            File(
                appContext.filesDir,
                "tts_pierre"
            )

        val destination =
            File(
                root,
                PierreVoiceConfig
                    .dataDirName
            )

        val marker =
            File(
                destination,
                ".geckodoku-ready-v1"
            )

        if (!marker.exists()) {
            if (destination.exists()) {
                destination
                    .deleteRecursively()
            }

            destination.mkdirs()

            copyAssetTree(
                assetPath =
                    PierreVoiceConfig
                        .dataAssetPath,
                destination =
                    destination
            )

            marker.writeText(
                "sherpa-onnx-v1.13.8-upmc-medium-pierre"
            )
        }

        return destination
    }

    private fun copyAssetTree(
        assetPath: String,
        destination: File
    ) {
        val children =
            appContext.assets
                .list(assetPath)
                .orEmpty()

        if (children.isEmpty()) {
            destination.parentFile
                ?.mkdirs()

            appContext.assets
                .open(assetPath)
                .use {
                    input ->

                    FileOutputStream(
                        destination
                    ).use {
                        output ->

                        input.copyTo(
                            output
                        )
                    }
                }

            return
        }

        destination.mkdirs()

        children.forEach {
            name ->

            copyAssetTree(
                assetPath =
                    "$assetPath/$name",
                destination =
                    File(
                        destination,
                        name
                    )
            )
        }
    }
}

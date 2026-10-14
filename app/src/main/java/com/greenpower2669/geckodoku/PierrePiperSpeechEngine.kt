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

    @Volatile
    private var warmupComplete =
        false

    init {
        // Amorçage réel dès la création du moteur. Le même worker sérialise
        // cette phase et les demandes de parole : une première vraie phrase
        // ne peut donc jamais passer devant l'amorce.
        executor.execute {
            try {
                val engine =
                    ensureTts()

                ensureWarmup(
                    engine
                )
            } catch (error: Throwable) {
                AudioCapturePolicy.log(
                    source = "PIERRE",
                    detail =
                        "WARMUP_INIT_ERROR type=" +
                            error
                                .javaClass
                                .simpleName
                )
            }
        }
    }

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

                if (
                    !ensureWarmup(
                        engine
                    )
                ) {
                    if (
                        !released &&
                        token ==
                            generation
                    ) {
                        error(
                            "Pierre warmup did not complete"
                        )
                    }

                    return@execute
                }

                if (
                    released ||
                    token !=
                        generation
                ) {
                    return@execute
                }

                val spokenText =
                    PierrePronunciationPolicy
                        .forSpeech(
                            text
                        )

                val audio =
                    engine.generate(
                        text =
                            spokenText,
                        sid =
                            PierreVoiceConfig
                                .speakerId,
                        speed = 1.0f
                    )

                if (
                    released ||
                    token !=
                        generation
                ) {
                    return@execute
                }

                val played =
                    player.playBlocking(
                        samples =
                            audio.samples,
                        sampleRate =
                            audio.sampleRate,
                        muted = false,
                        onStarted = {
                            handler.post {
                                if (
                                    !released &&
                                    token ==
                                        generation
                                ) {
                                    onStarted
                                        ?.invoke()
                                }
                            }
                        }
                    )

                if (!played) {
                    if (
                        released ||
                        token !=
                            generation
                    ) {
                        return@execute
                    }

                    error(
                        "Pierre AudioTrack playback failed"
                    )
                }

                handler.post {
                    if (
                        !released &&
                        token ==
                            generation
                    ) {
                        onCompletion
                            ?.invoke()
                    }
                }
            } catch (error: Throwable) {
                AudioCapturePolicy.log(
                    source = "PIERRE",
                    detail =
                        "SPEAK_ERROR type=" +
                            error
                                .javaClass
                                .simpleName
                )

                handler.post {
                    if (
                        !released &&
                        token ==
                            generation
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

        synchronized(this) {
            generation += 1
            player.stop()
        }

        player.release()

        executor.execute {
            tts?.release()
            tts = null
        }

        executor.shutdown()
    }

    /**
     * Amorçage Piper/Sherpa réel.
     *
     * - synthèse d'une courte phrase ;
     * - passage par le même AudioTrack persistant que la vraie voix ;
     * - volume nul uniquement pendant cette amorce ;
     * - validation par la progression réelle du playback head.
     *
     * Aucun timer n'est utilisé pour décider que l'amorce est terminée.
     */
    private fun ensureWarmup(
        engine: OfflineTts
    ): Boolean {
        if (warmupComplete) {
            return true
        }

        if (released) {
            return false
        }

        AudioCapturePolicy.log(
            source = "PIERRE",
            detail =
                "WARMUP_GENERATE_START"
        )

        val warmupAudio =
            engine.generate(
                text =
                    PierrePronunciationPolicy
                        .forSpeech(
                            WARMUP_TEXT
                        ),
                sid =
                    PierreVoiceConfig
                        .speakerId,
                speed = 1.0f
            )

        if (released) {
            return false
        }

        AudioCapturePolicy.log(
            source = "PIERRE",
            detail =
                "WARMUP_PLAYBACK_START muted=true frames=" +
                    warmupAudio
                        .samples
                        .size +
                    " sampleRate=" +
                    warmupAudio
                        .sampleRate
        )

        val completed =
            player.playBlocking(
                samples =
                    warmupAudio.samples,
                sampleRate =
                    warmupAudio.sampleRate,
                muted = true
            )

        if (
            completed &&
            !released
        ) {
            warmupComplete =
                true

            AudioCapturePolicy.log(
                source = "PIERRE",
                detail =
                    "WARMUP_COMPLETE persistentTrack=true"
            )
        } else {
            AudioCapturePolicy.log(
                source = "PIERRE",
                detail =
                    "WARMUP_INCOMPLETE"
            )
        }

        return warmupComplete
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

        AudioCapturePolicy.log(
            source = "PIERRE",
            detail =
                "TTS_READY engine=SherpaPiper speakerId=" +
                    PierreVoiceConfig
                        .speakerId
        )

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
    private companion object {
        const val WARMUP_TEXT =
            "Bonjour, je suis prêt."
    }
}

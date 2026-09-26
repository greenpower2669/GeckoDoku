package com.greenpower2669.geckodoku

enum class VoiceBenchmarkVariant(
    val label: String
) {
    ANDROID_TTS("1. Android TTS"),
    PIPER_LOW("2. Piper LOW"),
    PIPER_MEDIUM("3. Piper MEDIUM")
}

data class PiperModelSpec(
    val variant: VoiceBenchmarkVariant,
    val speaker: String,
    val locale: String,
    val assetDir: String,
    val modelFile: String,
    val tokensFile: String,
    val dataDirName: String,
    val modelSizeBytes: Long,
    val nominalSampleRate: Int
) {
    val modelAssetPath: String
        get() = "$assetDir/$modelFile"

    val tokensAssetPath: String
        get() = "$assetDir/$tokensFile"

    val dataAssetPath: String
        get() = "$assetDir/$dataDirName"
}

object VoiceBenchmarkCatalog {
    const val TEST_PHRASE =
        "Bonjour, je suis le Prof Gecko. Bravo, continue comme ça !"

    private val low =
        PiperModelSpec(
            variant =
                VoiceBenchmarkVariant
                    .PIPER_LOW,
            speaker = "siwis",
            locale = "fr_FR",
            assetDir =
                "tts/piper/low",
            modelFile =
                "fr_FR-siwis-low.onnx",
            tokensFile = "tokens.txt",
            dataDirName =
                "espeak-ng-data",
            modelSizeBytes =
                28_130_791L,
            nominalSampleRate =
                16_000
        )

    private val medium =
        PiperModelSpec(
            variant =
                VoiceBenchmarkVariant
                    .PIPER_MEDIUM,
            speaker = "siwis",
            locale = "fr_FR",
            assetDir =
                "tts/piper/medium",
            modelFile =
                "fr_FR-siwis-medium.onnx",
            tokensFile = "tokens.txt",
            dataDirName =
                "espeak-ng-data",
            modelSizeBytes =
                63_201_294L,
            nominalSampleRate =
                22_050
        )

    fun piper(
        variant: VoiceBenchmarkVariant
    ): PiperModelSpec =
        when (variant) {
            VoiceBenchmarkVariant
                .PIPER_LOW -> low

            VoiceBenchmarkVariant
                .PIPER_MEDIUM -> medium

            VoiceBenchmarkVariant
                .ANDROID_TTS ->
                error(
                    "Android TTS has no Piper model"
                )
        }
}

data class VoiceBenchmarkMetrics(
    val generationMs: Long,
    val loadMs: Long? = null,
    val modelLoadedNow: Boolean? = null,
    val modelSizeBytes: Long? = null,
    val approximatePssDeltaKb: Long? = null,
    val sampleRate: Int? = null,
    val audioDurationMs: Long? = null
)

data class VoiceBenchmarkResult(
    val variant: VoiceBenchmarkVariant,
    val metrics: VoiceBenchmarkMetrics? = null,
    val error: String? = null
) {
    val success: Boolean
        get() =
            metrics != null &&
                error == null
}

interface VoiceBenchmarkEngine {
    fun synthesizeAndPlay(
        text: String,
        callback: (VoiceBenchmarkResult) -> Unit
    )

    fun stop()
}

class PiperSingleModelSlot<T>(
    private val create:
        (VoiceBenchmarkVariant) -> T,
    private val release:
        (T) -> Unit
) {
    data class Required<T>(
        val value: T,
        val loadedNow: Boolean
    )

    private var activeVariant:
        VoiceBenchmarkVariant? = null

    private var activeValue:
        T? = null

    @Synchronized
    fun require(
        variant: VoiceBenchmarkVariant
    ): T =
        requireWithStatus(
            variant
        ).value

    @Synchronized
    fun requireWithStatus(
        variant: VoiceBenchmarkVariant
    ): Required<T> {
        require(
            variant ==
                VoiceBenchmarkVariant
                    .PIPER_LOW ||
                variant ==
                VoiceBenchmarkVariant
                    .PIPER_MEDIUM
        )

        val existing =
            activeValue

        if (
            existing != null &&
            activeVariant == variant
        ) {
            return Required(
                value = existing,
                loadedNow = false
            )
        }

        if (existing != null) {
            release(existing)
        }

        activeValue = null
        activeVariant = null

        val created =
            create(variant)

        activeValue = created
        activeVariant = variant

        return Required(
            value = created,
            loadedNow = true
        )
    }

    @Synchronized
    fun releaseAll() {
        activeValue?.let(release)
        activeValue = null
        activeVariant = null
    }
}

package com.greenpower2669.geckodoku

object PierreVoiceConfig {
    const val assetDir =
        "tts/piper/pierre"

    const val modelFile =
        "fr_FR-upmc-medium.onnx"

    const val tokensFile =
        "tokens.txt"

    const val dataDirName =
        "espeak-ng-data"

    const val modelSizeBytes =
        76_733_615L

    const val speakerId =
        1

    const val nominalSampleRate =
        22_050

    val modelAssetPath:
        String
        get() =
            "$assetDir/$modelFile"

    val tokensAssetPath:
        String
        get() =
            "$assetDir/$tokensFile"

    val dataAssetPath:
        String
        get() =
            "$assetDir/$dataDirName"
}

package com.greenpower2669.geckodoku

interface ChromaKeyPlayback {
    var logicalLayer: String

    fun play(
        assetPath: String,
        muted: Boolean,
        onCompletion: () -> Unit,
        onError: (String) -> Unit,
        onStarted: () -> Unit = {},
        revealOnFirstFrame: Boolean = true,
        holdOnFirstFrame: Boolean = false,
        onFirstFrameRendered: () -> Unit = {}
    )

    fun revealHeldFirstFrame(): Boolean

    fun setMuted(value: Boolean)

    fun setYellowTint(enabled: Boolean)

    fun setKeyColor(color: ChromaKeyColor)

    fun stopPlayback()

    fun release()
}

object AliveVideoBackendPolicy {
    fun useSpritePlayback(
        ownerKey: String
    ): Boolean =
        ownerKey.startsWith(
            "bee:"
        ) ||
            ownerKey.startsWith(
                "gomoku:"
            ) ||
            ownerKey.startsWith(
                "classic:"
            ) ||
            ownerKey.startsWith(
                "sudoku:"
            )

    fun useTextureView(
        ownerKey: String,
        sdkInt: Int
    ): Boolean =
        sdkInt >= 33 &&
            (
                ownerKey.startsWith(
                    "bee:"
                ) ||
                ownerKey.startsWith(
                    "gomoku:"
                )
            )
}

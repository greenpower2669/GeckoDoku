package com.greenpower2669.geckodoku

data class TitleIdentityPolicy(
    val iconSizeDp: Int = 32,
    val frameExtraDp: Int = 6,
    val gapDp: Int = 6,
    val animationDurationMs: Long = 2_600L,
    val affectsBoardLayout: Boolean = false,
    val usesIntroVideoForMedallion: Boolean = false
)

package com.greenpower2669.geckodoku

data class ProfessorUiPolicy(
    val keepControlsVisibleWhileBubbleOpen: Boolean = true,
    val showPortraitInsideBubble: Boolean = false,
    val playVideoInsideBubble: Boolean = false,
    val showPortraitInButton: Boolean = true,
    val animatePortraitOnInteraction: Boolean = true,
    val playVideoInButton: Boolean = true,
    val playProfVideoFromStartToEnd: Boolean = true,
    val muteProfVideoEmbeddedAudio: Boolean = true,
    val buttonHostHeightDp: Int = 58,
    val buttonPortraitOverhangDp: Int = 10,
    val buttonElevationDp: Int = 0,
    val portraitElevationDp: Int = 18
)

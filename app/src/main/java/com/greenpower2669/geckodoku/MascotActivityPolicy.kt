package com.greenpower2669.geckodoku

object MascotActivityPolicy {
    // Diagnostic 0.15.12-dev:
    // no runtime concurrency cap is applied by AliveMascotOverlayView.
    // One video slot is allocated lazily for every visible Presence.
    const val ALL_VISIBLE_VIDEO_DIAGNOSTIC =
        true

    // Historical target values kept only as reference for the
    // future bounded scheduler after the diagnostic is understood.
    const val GECKO_CONCURRENT = 3
    const val BEE_CONCURRENT = 2
    const val PLANT_CONCURRENT = 1

    fun capacity(
        kind: MascotKind
    ): Int =
        when (kind) {
            MascotKind.GECKO ->
                GECKO_CONCURRENT
            MascotKind.BEE ->
                BEE_CONCURRENT
            MascotKind.PLANT ->
                PLANT_CONCURRENT
        }
}

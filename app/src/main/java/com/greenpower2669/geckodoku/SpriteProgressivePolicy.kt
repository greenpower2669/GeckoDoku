package com.greenpower2669.geckodoku

object SpriteProgressivePolicy {
    const val INTERNAL_LOW_HEIGHT = 240
    const val MAX_HEIGHT = 240

    fun stagesFor(
        target: SpriteResolution
    ): List<Int> =
        listOf(MAX_HEIGHT)

    fun isSecondary(
        heightPx: Int
    ): Boolean =
        false
}

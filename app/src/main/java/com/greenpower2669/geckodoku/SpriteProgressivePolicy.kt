package com.greenpower2669.geckodoku

object SpriteProgressivePolicy {
    const val INTERNAL_LOW_HEIGHT = 60
    const val MAX_HEIGHT = 240

    fun stagesFor(
        target: SpriteResolution
    ): List<Int> =
        when (target) {
            SpriteResolution.P120 ->
                listOf(
                    INTERNAL_LOW_HEIGHT,
                    120
                )

            SpriteResolution.P240 ->
                listOf(
                    INTERNAL_LOW_HEIGHT,
                    120,
                    MAX_HEIGHT
                )
        }

    fun isSecondary(
        heightPx: Int
    ): Boolean =
        false
}

package com.greenpower2669.geckodoku

object SpriteProgressivePolicy {
    const val INTERNAL_LOW_HEIGHT = 60

    fun stagesFor(
        target: SpriteResolution
    ): List<Int> =
        buildList {
            add(INTERNAL_LOW_HEIGHT)

            if (target.heightPx >= 120) {
                add(120)
            }

            when {
                target.heightPx <= 120 -> Unit
                target.heightPx < 240 ->
                    add(target.heightPx)
                else -> {
                    add(240)

                    if (target.heightPx > 240) {
                        add(target.heightPx)
                    }
                }
            }
        }.distinct()

    fun isSecondary(
        heightPx: Int
    ): Boolean =
        heightPx > 240
}

package com.greenpower2669.geckodoku

object GeckoBoardPalette {
    private val colors =
        intArrayOf(
            0xFFE8F8E8.toInt(),
            0xFFDEEFFF.toInt(),
            0xFFFFF1CC.toInt(),
            0xFFF4E1FF.toInt(),
            0xFFFFDEE2.toInt(),
            0xFFDAF7F2.toInt(),
            0xFFFFE5CC.toInt(),
            0xFFE1E5FF.toInt(),
            0xFFEDF6CD.toInt(),
            0xFFFFDAF2.toInt(),
            0xFFD8F5FF.toInt(),
            0xFFEFE1CF.toInt()
        )

    fun colorFor(regionId: Int): Int =
        colors[
            Math.floorMod(
                regionId,
                colors.size
            )
        ]
}

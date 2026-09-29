package com.greenpower2669.geckodoku

object MascotRenderPolicy {
    const val CLASSIC_GECKO_SCALE =
        .80f

    const val SUDOKU_GECKO_SCALE =
        .66f

    const val BEE_GECKO_BASE_SCALE =
        .61f

    const val PLANT_SIZE_DP =
        72

    const val PLANT_MARGIN_DP =
        8

    fun beeGeckoScale(
        piece: BeeGeckoPiece
    ): Float =
        BEE_GECKO_BASE_SCALE *
            if (
                piece ==
                    BeeGeckoPiece.BEE
            ) {
                BeeGeckoVisualPolicy
                    .BEE_SCALE
            } else {
                1f
            }
}

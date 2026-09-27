package com.greenpower2669.geckodoku

import kotlin.math.min

data class ClassicGridGeometry(
    val left: Int,
    val top: Int,
    val side: Int
)

class ClassicBoardReadabilityPolicy(
    private val horizontalMarginPx:
        Int = 3,
    private val topMarginPx:
        Int = 3
) {
    init {
        require(horizontalMarginPx >= 0)
        require(topMarginPx >= 0)
    }

    fun geometry(
        viewWidthPx: Int,
        viewHeightPx: Int,
        gutterPx: Int
    ): ClassicGridGeometry {
        val widthLimited =
            (
                viewWidthPx -
                    horizontalMarginPx * 2
                ).coerceAtLeast(1)

        val heightLimited =
            (
                viewHeightPx -
                    gutterPx -
                    topMarginPx
                ).coerceAtLeast(1)

        return ClassicGridGeometry(
            left =
                horizontalMarginPx,
            top =
                topMarginPx,
            side =
                min(
                    widthLimited,
                    heightLimited
                )
        )
    }
}

package com.greenpower2669.geckodoku

class SudokuFullWidthBoardPolicy(
    val horizontalMarginPx: Int = 3
) {
    init {
        require(horizontalMarginPx >= 0)
    }

    fun geometry(
        usefulWidthPx: Int,
        topPx: Int
    ): BoardGeometry {
        val side =
            (
                usefulWidthPx -
                    horizontalMarginPx * 2
                ).coerceAtLeast(1)

        return BoardGeometry(
            left = horizontalMarginPx,
            top = topPx,
            width = side,
            height = side
        )
    }

    companion object {
        const val INNER_GRID_MARGIN_PX = 0
    }
}

package com.greenpower2669.geckodoku

sealed class SudokuPaletteAction {
    data class Value(
        val digit: Int
    ) : SudokuPaletteAction()

    data class Candidate(
        val digit: Int
    ) : SudokuPaletteAction()

    data object Erase :
        SudokuPaletteAction()
}

data class PixelBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

data class PixelPoint(
    val x: Int,
    val y: Int
)

class SudokuPaletteLayoutPolicy {
    fun actionAt(
        x: Float,
        y: Float,
        width: Int,
        height: Int
    ): SudokuPaletteAction? {
        if (
            width <= 0 ||
            height <= 0 ||
            x < 0f ||
            y < 0f ||
            x >= width ||
            y >= height
        ) {
            return null
        }

        val header =
            height * HEADER_FRACTION

        val footerTop =
            height *
                (1f - FOOTER_FRACTION)

        if (y >= footerTop) {
            return SudokuPaletteAction
                .Erase
        }

        if (y < header) {
            return null
        }

        val gridHeight =
            footerTop - header

        val row =
            (
                (y - header) /
                    gridHeight *
                    3f
                ).toInt()
                .coerceIn(0, 2)

        val leftHalf =
            x < width / 2f

        val localX =
            if (leftHalf) {
                x
            } else {
                x - width / 2f
            }

        val halfWidth =
            width / 2f

        val col =
            (
                localX /
                    halfWidth *
                    3f
                ).toInt()
                .coerceIn(0, 2)

        val digit =
            row * 3 +
                col +
                1

        return if (leftHalf) {
            SudokuPaletteAction
                .Value(digit)
        } else {
            SudokuPaletteAction
                .Candidate(digit)
        }
    }

    fun tileBounds(
        digit: Int,
        candidate: Boolean,
        width: Int,
        height: Int
    ): PixelBox {
        require(digit in 1..9)
        require(width > 0)
        require(height > 0)

        val slot =
            SudokuCandidateLayout
                .slot(digit)

        val header =
            height * HEADER_FRACTION

        val footerTop =
            height *
                (1f - FOOTER_FRACTION)

        val halfWidth =
            width / 2f

        val baseX =
            if (candidate) {
                halfWidth
            } else {
                0f
            }

        return PixelBox(
            left =
                (
                    baseX +
                        slot.left *
                        halfWidth
                    ).toInt(),
            top =
                (
                    header +
                        slot.top *
                        (footerTop - header)
                    ).toInt(),
            right =
                (
                    baseX +
                        slot.right *
                        halfWidth
                    ).toInt(),
            bottom =
                (
                    header +
                        slot.bottom *
                        (footerTop - header)
                    ).toInt()
        )
    }

    fun footerTop(
        height: Int
    ): Int =
        (
            height *
                (1f - FOOTER_FRACTION)
            ).toInt()

    companion object {
        private const val HEADER_FRACTION =
            .13f

        private const val FOOTER_FRACTION =
            .17f
    }
}

class SudokuPopupPlacementPolicy {
    fun place(
        screenWidth: Int,
        screenHeight: Int,
        anchor: PixelBox,
        popupWidth: Int,
        popupHeight: Int,
        margin: Int
    ): PixelPoint {
        val safeWidth =
            screenWidth
                .coerceAtLeast(
                    margin * 2 +
                        popupWidth
                )

        val safeHeight =
            screenHeight
                .coerceAtLeast(
                    margin * 2 +
                        popupHeight
                )

        val rightCandidate =
            anchor.right +
                margin

        val leftCandidate =
            anchor.left -
                margin -
                popupWidth

        val x =
            when {
                rightCandidate +
                    popupWidth <=
                    safeWidth -
                        margin ->
                    rightCandidate

                leftCandidate >=
                    margin ->
                    leftCandidate

                else ->
                    (
                        anchor.left +
                            anchor.right -
                            popupWidth
                        ) / 2
            }.coerceIn(
                margin,
                (
                    safeWidth -
                        margin -
                        popupWidth
                    ).coerceAtLeast(
                    margin
                )
            )

        val centeredY =
            (
                anchor.top +
                    anchor.bottom -
                    popupHeight
                ) / 2

        val y =
            centeredY
                .coerceIn(
                    margin,
                    (
                        safeHeight -
                            margin -
                            popupHeight
                        ).coerceAtLeast(
                        margin
                    )
                )

        return PixelPoint(
            x = x,
            y = y
        )
    }
}

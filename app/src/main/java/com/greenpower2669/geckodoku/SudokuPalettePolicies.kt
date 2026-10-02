package com.greenpower2669.geckodoku

sealed class SudokuPaletteAction {
    data class Value(
        val digit: Int
    ) : SudokuPaletteAction()

    data class Candidate(
        val digit: Int
    ) : SudokuPaletteAction()

    data class Hypothesis(
        val digit: Int
    ) : SudokuPaletteAction()

    data object ConfirmYes :
        SudokuPaletteAction()

    data object ConfirmNo :
        SudokuPaletteAction()

    data object Help :
        SudokuPaletteAction()

    data object Close :
        SudokuPaletteAction()
}

enum class SudokuPalettePanel {
    VALUE,
    CANDIDATE,
    HYPOTHESIS,
    PREVIEW
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
        height: Int,
        confirmationActive:
            Boolean = false
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

        val close =
            closeBounds(
                width,
                height
            )

        if (
            !confirmationActive &&
            contains(
                close,
                x,
                y
            )
        ) {
            return SudokuPaletteAction
                .Close
        }

        val help =
            helpBounds(
                width,
                height
            )

        if (
            !confirmationActive &&
            contains(
                help,
                x,
                y
            )
        ) {
            return SudokuPaletteAction
                .Help
        }

        if (
            y <
                headerHeight(
                    height
                )
        ) {
            return null
        }

        if (confirmationActive) {
            val yes =
                confirmYesBounds(
                    width,
                    height
                )

            if (
                contains(
                    yes,
                    x,
                    y
                )
            ) {
                return SudokuPaletteAction
                    .ConfirmYes
            }

            val no =
                confirmNoBounds(
                    width,
                    height
                )

            if (
                contains(
                    no,
                    x,
                    y
                )
            ) {
                return SudokuPaletteAction
                    .ConfirmNo
            }

            return null
        }

        return when (
            val panel =
                panelAt(
                    x,
                    y,
                    width,
                    height
                )
        ) {
            SudokuPalettePanel.VALUE ->
                digitAt(
                    x,
                    y,
                    panel,
                    width,
                    height
                )
                    ?.let {
                        SudokuPaletteAction
                            .Value(it)
                    }

            SudokuPalettePanel.CANDIDATE ->
                digitAt(
                    x,
                    y,
                    panel,
                    width,
                    height
                )
                    ?.let {
                        SudokuPaletteAction
                            .Candidate(it)
                    }

            SudokuPalettePanel.HYPOTHESIS ->
                digitAt(
                    x,
                    y,
                    panel,
                    width,
                    height
                )
                    ?.let {
                        SudokuPaletteAction
                            .Hypothesis(it)
                    }

            SudokuPalettePanel.PREVIEW,
            null ->
                null
        }
    }

    fun panelBounds(
        panel: SudokuPalettePanel,
        width: Int,
        height: Int
    ): PixelBox {
        require(width > 0)
        require(height > 0)

        val header =
            headerHeight(
                height
            )

        val contentHeight =
            (
                height -
                    header
                )
                .coerceAtLeast(1)

        val halfWidth =
            width / 2

        val halfHeight =
            contentHeight / 2

        val left =
            when (panel) {
                SudokuPalettePanel.VALUE,
                SudokuPalettePanel.HYPOTHESIS ->
                    0

                SudokuPalettePanel.CANDIDATE,
                SudokuPalettePanel.PREVIEW ->
                    halfWidth
            }

        val top =
            when (panel) {
                SudokuPalettePanel.VALUE,
                SudokuPalettePanel.CANDIDATE ->
                    header

                SudokuPalettePanel.HYPOTHESIS,
                SudokuPalettePanel.PREVIEW ->
                    header +
                        halfHeight
            }

        val right =
            when (panel) {
                SudokuPalettePanel.VALUE,
                SudokuPalettePanel.HYPOTHESIS ->
                    halfWidth

                SudokuPalettePanel.CANDIDATE,
                SudokuPalettePanel.PREVIEW ->
                    width
            }

        val bottom =
            when (panel) {
                SudokuPalettePanel.VALUE,
                SudokuPalettePanel.CANDIDATE ->
                    header +
                        halfHeight

                SudokuPalettePanel.HYPOTHESIS,
                SudokuPalettePanel.PREVIEW ->
                    height
            }

        return PixelBox(
            left = left,
            top = top,
            right = right,
            bottom = bottom
        )
    }

    fun tileBounds(
        digit: Int,
        panel: SudokuPalettePanel,
        width: Int,
        height: Int
    ): PixelBox {
        require(digit in 1..9)
        require(
            panel !=
                SudokuPalettePanel
                    .PREVIEW
        )

        val bounds =
            panelBounds(
                panel,
                width,
                height
            )

        val titleHeight =
            (
                (
                    bounds.bottom -
                        bounds.top
                    ) *
                    PANEL_TITLE_FRACTION
                )
                .toInt()

        val gridTop =
            bounds.top +
                titleHeight

        val gridHeight =
            (
                bounds.bottom -
                    gridTop
                )
                .coerceAtLeast(1)

        val gridWidth =
            (
                bounds.right -
                    bounds.left
                )
                .coerceAtLeast(1)

        val slot =
            SudokuCandidateLayout
                .slot(
                    digit
                )

        return PixelBox(
            left =
                (
                    bounds.left +
                        slot.left *
                            gridWidth
                    )
                    .toInt(),
            top =
                (
                    gridTop +
                        slot.top *
                            gridHeight
                    )
                    .toInt(),
            right =
                (
                    bounds.left +
                        slot.right *
                            gridWidth
                    )
                    .toInt(),
            bottom =
                (
                    gridTop +
                        slot.bottom *
                            gridHeight
                    )
                    .toInt()
        )
    }

    fun helpBounds(
        width: Int,
        height: Int
    ): PixelBox {
        val header =
            headerHeight(
                height
            )

        val size =
            header
                .coerceAtMost(
                    width /
                        4
                )

        return PixelBox(
            left =
                width -
                    size * 2,
            top = 0,
            right =
                width -
                    size,
            bottom =
                header
        )
    }

    fun closeBounds(
        width: Int,
        height: Int
    ): PixelBox {
        val header =
            headerHeight(
                height
            )

        val size =
            header
                .coerceAtMost(
                    width /
                        4
                )

        return PixelBox(
            left =
                width -
                    size,
            top = 0,
            right =
                width,
            bottom =
                header
        )
    }

    fun confirmYesBounds(
        width: Int,
        height: Int
    ): PixelBox =
        confirmBounds(
            width,
            height,
            yes = true
        )

    fun confirmNoBounds(
        width: Int,
        height: Int
    ): PixelBox =
        confirmBounds(
            width,
            height,
            yes = false
        )

    fun headerHeight(
        height: Int
    ): Int =
        (
            height *
                HEADER_FRACTION
            )
            .toInt()
            .coerceAtLeast(
                14
            )
            .coerceAtMost(
                (
                    height *
                        .18f
                    )
                    .toInt()
                    .coerceAtLeast(
                        14
                    )
            )

    private fun confirmBounds(
        width: Int,
        height: Int,
        yes: Boolean
    ): PixelBox {
        val panel =
            panelBounds(
                SudokuPalettePanel
                    .PREVIEW,
                width,
                height
            )

        val center =
            (
                panel.left +
                    panel.right
                ) /
                2

        val top =
            panel.top +
                (
                    (
                        panel.bottom -
                            panel.top
                        ) *
                        .62f
                    )
                    .toInt()

        return if (yes) {
            PixelBox(
                left =
                    panel.left,
                top =
                    top,
                right =
                    center,
                bottom =
                    panel.bottom
            )
        } else {
            PixelBox(
                left =
                    center,
                top =
                    top,
                right =
                    panel.right,
                bottom =
                    panel.bottom
            )
        }
    }

    fun panelAt(
        x: Float,
        y: Float,
        width: Int,
        height: Int
    ): SudokuPalettePanel? =
        SudokuPalettePanel
            .entries
            .firstOrNull {
                contains(
                    panelBounds(
                        it,
                        width,
                        height
                    ),
                    x,
                    y
                )
            }

    private fun digitAt(
        x: Float,
        y: Float,
        panel: SudokuPalettePanel,
        width: Int,
        height: Int
    ): Int? =
        (
            1..9
            )
            .firstOrNull {
                contains(
                    tileBounds(
                        it,
                        panel,
                        width,
                        height
                    ),
                    x,
                    y
                )
            }

    private fun contains(
        box: PixelBox,
        x: Float,
        y: Float
    ): Boolean =
        x >=
            box.left &&
            x <
                box.right &&
            y >=
                box.top &&
            y <
                box.bottom

    companion object {
        private const val HEADER_FRACTION =
            .105f

        private const val PANEL_TITLE_FRACTION =
            .20f
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
                        ) /
                        2
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

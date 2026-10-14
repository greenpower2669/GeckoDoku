package com.greenpower2669.geckodoku

data class SudokuFloatingWindowGeometry(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

data class SudokuFloatingWindowBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = (right - left).coerceAtLeast(0)
    val height: Int get() = (bottom - top).coerceAtLeast(0)
}

object SudokuFloatingWindowGeometryPolicy {
    /**
     * All coordinates are PopupWindow.showAtLocation(GRAVITY_TOP | START)
     * window coordinates, never root-view-local coordinates.
     *
     * Intersect the measured root with the safe area of the decor window.
     * This avoids counting system bars twice when Android has already
     * inset the content view and also works with edge-to-edge layouts.
     */
    fun safeBounds(
        rootX: Int,
        rootY: Int,
        rootWidth: Int,
        rootHeight: Int,
        windowWidth: Int,
        windowHeight: Int,
        insetLeft: Int,
        insetTop: Int,
        insetRight: Int,
        insetBottom: Int
    ): SudokuFloatingWindowBounds {
        val left = maxOf(rootX, insetLeft)
        val top = maxOf(rootY, insetTop)
        val right = minOf(
            rootX + rootWidth.coerceAtLeast(0),
            windowWidth - insetRight
        ).coerceAtLeast(left)
        val bottom = minOf(
            rootY + rootHeight.coerceAtLeast(0),
            windowHeight - insetBottom
        ).coerceAtLeast(top)
        return SudokuFloatingWindowBounds(left, top, right, bottom)
    }

    fun clampToBounds(
        geometry: SudokuFloatingWindowGeometry,
        bounds: SudokuFloatingWindowBounds,
        minimumWidth: Int,
        minimumHeight: Int,
        margin: Int
    ): SudokuFloatingWindowGeometry {
        val inset = margin.coerceAtLeast(0)
        val usableWidth = (bounds.width - inset * 2).coerceAtLeast(1)
        val usableHeight = (bounds.height - inset * 2).coerceAtLeast(1)
        // A minimum size can never be larger than the usable screen.
        val width = geometry.width.coerceIn(
            minimumWidth.coerceIn(1, usableWidth), usableWidth
        )
        val height = geometry.height.coerceIn(
            minimumHeight.coerceIn(1, usableHeight), usableHeight
        )
        val minX = bounds.left + inset
        val minY = bounds.top + inset
        return SudokuFloatingWindowGeometry(
            x = geometry.x.coerceIn(minX, bounds.right - inset - width),
            y = geometry.y.coerceIn(minY, bounds.bottom - inset - height),
            width = width,
            height = height
        )
    }

    fun bottomCentered(
        bounds: SudokuFloatingWindowBounds,
        width: Int,
        height: Int,
        minWidth: Int,
        minHeight: Int,
        margin: Int,
        bottomGap: Int
    ): SudokuFloatingWindowGeometry {
        val preferred = SudokuFloatingWindowGeometry(
            x = bounds.left + (bounds.width - width) / 2,
            y = bounds.bottom - height - bottomGap,
            width = width,
            height = height
        )
        return clampToBounds(preferred, bounds, minWidth, minHeight, margin)
    }

    fun compactAboveBoard(
        rootWidth: Int,
        rootHeight: Int,
        boardLeft: Int,
        boardTop: Int,
        boardWidth: Int,
        preferredHeight: Int,
        minimumWidth: Int,
        margin: Int,
        gap: Int
    ): SudokuFloatingWindowGeometry {
        val safeRootWidth =
            rootWidth.coerceAtLeast(
                minimumWidth +
                    margin * 2
            )
        val safeRootHeight =
            rootHeight.coerceAtLeast(
                preferredHeight +
                    margin * 2
            )

        val width =
            boardWidth
                .takeIf {
                    it >
                        0
                }
                ?.coerceIn(
                    minimumWidth,
                    safeRootWidth -
                        margin * 2
                )
                ?: (
                    safeRootWidth -
                        margin * 2
                    )

        val height =
            preferredHeight.coerceAtMost(
                safeRootHeight -
                    margin * 2
            )

        val maxX =
            (
                safeRootWidth -
                    width -
                    margin
                ).coerceAtLeast(
                margin
            )

        val x =
            boardLeft.coerceIn(
                margin,
                maxX
            )

        val maxY =
            (
                safeRootHeight -
                    height -
                    margin
                ).coerceAtLeast(
                margin
            )

        val y =
            (
                boardTop -
                    height -
                    gap
                ).coerceIn(
                margin,
                maxY
            )

        return SudokuFloatingWindowGeometry(
            x =
                x,
            y =
                y,
            width =
                width,
            height =
                height
        )
    }

    fun clamp(
        geometry: SudokuFloatingWindowGeometry,
        rootWidth: Int,
        rootHeight: Int,
        minimumWidth: Int,
        minimumHeight: Int,
        margin: Int
    ): SudokuFloatingWindowGeometry {
        val width =
            geometry.width.coerceIn(
                minimumWidth,
                (
                    rootWidth -
                        margin * 2
                    ).coerceAtLeast(
                    minimumWidth
                )
            )

        val height =
            geometry.height.coerceIn(
                minimumHeight,
                (
                    rootHeight -
                        margin * 2
                    ).coerceAtLeast(
                    minimumHeight
                )
            )

        val maxX =
            (
                rootWidth -
                    width -
                    margin
                ).coerceAtLeast(
                margin
            )

        val maxY =
            (
                rootHeight -
                    height -
                    margin
                ).coerceAtLeast(
                margin
            )

        return SudokuFloatingWindowGeometry(
            x =
                geometry.x.coerceIn(
                    margin,
                    maxX
                ),
            y =
                geometry.y.coerceIn(
                    margin,
                    maxY
                ),
            width =
                width,
            height =
                height
        )
    }
}

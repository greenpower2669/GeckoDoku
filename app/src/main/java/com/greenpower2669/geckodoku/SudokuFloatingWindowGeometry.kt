package com.greenpower2669.geckodoku

data class SudokuFloatingWindowGeometry(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

object SudokuFloatingWindowGeometryPolicy {
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

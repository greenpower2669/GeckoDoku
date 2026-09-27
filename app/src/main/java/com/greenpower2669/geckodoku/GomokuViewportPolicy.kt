package com.greenpower2669.geckodoku

import kotlin.math.max
import kotlin.math.min

data class GomokuViewport(
    val originRow: Float,
    val originCol: Float,
    val visibleSpan: Float
)

class GomokuViewportPolicy(
    private val boardSize: Int,
    private val defaultVisibleSpan:
        Float = 12f,
    private val minimumVisibleSpan:
        Float = 5f
) {
    init {
        require(boardSize >= 5)
        require(
            minimumVisibleSpan > 0f
        )
    }

    fun initial(): GomokuViewport {
        val span =
            min(
                boardSize.toFloat(),
                defaultVisibleSpan
            )
                .coerceAtLeast(
                    min(
                        boardSize.toFloat(),
                        minimumVisibleSpan
                    )
                )

        val origin =
            (
                boardSize -
                    span
                ) / 2f

        return clamp(
            GomokuViewport(
                originRow = origin,
                originCol = origin,
                visibleSpan = span
            )
        )
    }

    fun pan(
        viewport: GomokuViewport,
        deltaCols: Float,
        deltaRows: Float
    ): GomokuViewport =
        clamp(
            viewport.copy(
                originCol =
                    viewport.originCol +
                        deltaCols,
                originRow =
                    viewport.originRow +
                        deltaRows
            )
        )

    fun zoom(
        viewport: GomokuViewport,
        scaleFactor: Float,
        focusXFraction: Float,
        focusYFraction: Float
    ): GomokuViewport {
        if (
            !scaleFactor.isFinite() ||
            scaleFactor <= 0f
        ) {
            return clamp(viewport)
        }

        val old =
            clamp(viewport)

        val fx =
            focusXFraction
                .coerceIn(0f, 1f)

        val fy =
            focusYFraction
                .coerceIn(0f, 1f)

        val logicalFocusCol =
            old.originCol +
                old.visibleSpan * fx

        val logicalFocusRow =
            old.originRow +
                old.visibleSpan * fy

        val minSpan =
            min(
                minimumVisibleSpan,
                boardSize.toFloat()
            )

        val newSpan =
            (
                old.visibleSpan /
                    scaleFactor
                ).coerceIn(
                minSpan,
                boardSize.toFloat()
            )

        return clamp(
            GomokuViewport(
                originRow =
                    logicalFocusRow -
                        newSpan * fy,
                originCol =
                    logicalFocusCol -
                        newSpan * fx,
                visibleSpan =
                    newSpan
            )
        )
    }

    fun centerOn(
        viewport: GomokuViewport,
        cell: Cell
    ): GomokuViewport {
        val span =
            viewport.visibleSpan
                .coerceIn(
                    min(
                        minimumVisibleSpan,
                        boardSize.toFloat()
                    ),
                    boardSize.toFloat()
                )

        return clamp(
            GomokuViewport(
                originRow =
                    cell.row +
                        .5f -
                        span / 2f,
                originCol =
                    cell.col +
                        .5f -
                        span / 2f,
                visibleSpan =
                    span
            )
        )
    }

    fun clamp(
        viewport: GomokuViewport
    ): GomokuViewport {
        val minSpan =
            min(
                minimumVisibleSpan,
                boardSize.toFloat()
            )

        val span =
            viewport.visibleSpan
                .coerceIn(
                    minSpan,
                    boardSize.toFloat()
                )

        val maxOrigin =
            max(
                0f,
                boardSize -
                    span
            )

        return GomokuViewport(
            originRow =
                viewport.originRow
                    .coerceIn(
                        0f,
                        maxOrigin
                    ),
            originCol =
                viewport.originCol
                    .coerceIn(
                        0f,
                        maxOrigin
                    ),
            visibleSpan =
                span
        )
    }
}

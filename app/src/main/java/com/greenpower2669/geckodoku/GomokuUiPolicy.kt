package com.greenpower2669.geckodoku

enum class GomokuGestureAction {
    PLAY,
    PAN,
    ZOOM
}

class GomokuGesturePolicy(
    private val dragThresholdPx:
        Float = 12f
) {
    init {
        require(
            dragThresholdPx >= 0f
        )
    }

    fun actionFor(
        pointerCount: Int,
        distancePx: Float,
        scaleInProgress: Boolean
    ): GomokuGestureAction =
        when {
            scaleInProgress ||
                pointerCount >= 2 ->
                GomokuGestureAction
                    .ZOOM

            distancePx >=
                dragThresholdPx ->
                GomokuGestureAction
                    .PAN

            else ->
                GomokuGestureAction
                    .PLAY
        }
}

object GomokuYellowFilterPolicy {
    fun colorMatrixValues():
        FloatArray =
        floatArrayOf(
            .35f, .95f, 0f, 0f, 0f,
            .05f, 1.00f, 0f, 0f, 0f,
            0f, .08f, .18f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
}

class GomokuBoardLayoutPolicy(
    private val horizontalMarginPx:
        Int = 3
) {
    init {
        require(
            horizontalMarginPx >= 0
        )
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
            left =
                horizontalMarginPx,
            top =
                topPx,
            width =
                side,
            height =
                side
        )
    }
}

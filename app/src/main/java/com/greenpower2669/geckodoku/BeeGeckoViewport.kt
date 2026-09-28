package com.greenpower2669.geckodoku

import kotlin.math.max
import kotlin.math.min

enum class BeeGeckoGestureAction {
    TAP,
    PAN,
    ZOOM
}

class BeeGeckoGesturePolicy(
    private val dragThresholdPx: Float
) {
    fun actionFor(
        pointerCount: Int,
        distancePx: Float,
        scaleInProgress: Boolean
    ): BeeGeckoGestureAction =
        when {
            scaleInProgress ||
                pointerCount >= 2 ->
                BeeGeckoGestureAction
                    .ZOOM

            distancePx >
                dragThresholdPx ->
                BeeGeckoGestureAction
                    .PAN

            else ->
                BeeGeckoGestureAction
                    .TAP
        }
}

data class BeeGeckoCamera(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

data class BeeGeckoBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float
        get() =
            right - left

    val height: Float
        get() =
            bottom - top

    val centerX: Float
        get() =
            (left + right) /
                2f

    val centerY: Float
        get() =
            (top + bottom) /
                2f
}

object BeeGeckoViewportPolicy {
    const val MIN_SCALE = 0.42f
    const val MAX_SCALE = 2.8f

    fun zoom(
        camera: BeeGeckoCamera,
        factor: Float,
        focusX: Float,
        focusY: Float,
        minimumScale:
            Float = MIN_SCALE
    ): BeeGeckoCamera {
        val effectiveMinimum =
            minimumScale
                .coerceIn(
                    MIN_SCALE,
                    MAX_SCALE
                )

        val newScale =
            (
                camera.scale *
                    factor
                ).coerceIn(
                effectiveMinimum,
                MAX_SCALE
            )

        if (
            newScale ==
                camera.scale
        ) {
            return camera
        }

        val worldX =
            (
                focusX -
                    camera.offsetX
                ) /
                camera.scale

        val worldY =
            (
                focusY -
                    camera.offsetY
                ) /
                camera.scale

        return BeeGeckoCamera(
            scale = newScale,
            offsetX =
                focusX -
                    worldX *
                        newScale,
            offsetY =
                focusY -
                    worldY *
                        newScale
        )
    }

    fun pan(
        camera: BeeGeckoCamera,
        dx: Float,
        dy: Float
    ): BeeGeckoCamera =
        camera.copy(
            offsetX =
                camera.offsetX +
                    dx,
            offsetY =
                camera.offsetY +
                    dy
        )

    fun fitScale(
        viewWidth: Float,
        viewHeight: Float,
        content:
            BeeGeckoBounds,
        marginPx: Float = 0f
    ): Float {
        if (
            viewWidth <= 0f ||
            viewHeight <= 0f ||
            content.width <= 0f ||
            content.height <= 0f
        ) {
            return MIN_SCALE
        }

        val usableWidth =
            (
                viewWidth -
                    marginPx *
                        2f
                )
                .coerceAtLeast(1f)

        val usableHeight =
            (
                viewHeight -
                    marginPx *
                        2f
                )
                .coerceAtLeast(1f)

        return min(
            usableWidth /
                content.width,
            usableHeight /
                content.height
        )
            .coerceIn(
                MIN_SCALE,
                MAX_SCALE
            )
    }

    fun centered(
        viewWidth: Float,
        viewHeight: Float,
        content:
            BeeGeckoBounds,
        preferredMinScale:
            Float = .72f
    ): BeeGeckoCamera {
        if (
            viewWidth <= 0f ||
            viewHeight <= 0f ||
            content.width <= 0f ||
            content.height <= 0f
        ) {
            return BeeGeckoCamera()
        }

        val fit =
            min(
                viewWidth /
                    content.width,
                viewHeight /
                    content.height
            ) *
                .92f

        val scale =
            max(
                fit,
                preferredMinScale
            )
                .coerceIn(
                    MIN_SCALE,
                    MAX_SCALE
                )

        return BeeGeckoCamera(
            scale = scale,
            offsetX =
                viewWidth /
                    2f -
                    content.centerX *
                        scale,
            offsetY =
                viewHeight /
                    2f -
                    content.centerY *
                        scale
        )
    }

    fun clamp(
        camera: BeeGeckoCamera,
        viewWidth: Float,
        viewHeight: Float,
        content:
            BeeGeckoBounds,
        visibleMarginPx:
            Float
    ): BeeGeckoCamera {
        if (
            viewWidth <= 0f ||
            viewHeight <= 0f ||
            content.width <= 0f ||
            content.height <= 0f
        ) {
            return camera
        }

        val margin =
            visibleMarginPx
                .coerceAtLeast(0f)

        val scaledWidth =
            content.width *
                camera.scale

        val scaledHeight =
            content.height *
                camera.scale

        val usableWidth =
            (
                viewWidth -
                    margin *
                        2f
                )
                .coerceAtLeast(1f)

        val usableHeight =
            (
                viewHeight -
                    margin *
                        2f
                )
                .coerceAtLeast(1f)

        val desiredLeft =
            if (
                scaledWidth <=
                    usableWidth
            ) {
                margin +
                    (
                        usableWidth -
                            scaledWidth
                        ) /
                        2f
            } else {
                val currentLeft =
                    content.left *
                        camera.scale +
                        camera.offsetX

                val minimumLeft =
                    viewWidth -
                        margin -
                        scaledWidth

                val maximumLeft =
                    margin

                currentLeft
                    .coerceIn(
                        minimumLeft,
                        maximumLeft
                    )
            }

        val desiredTop =
            if (
                scaledHeight <=
                    usableHeight
            ) {
                margin +
                    (
                        usableHeight -
                            scaledHeight
                        ) /
                        2f
            } else {
                val currentTop =
                    content.top *
                        camera.scale +
                        camera.offsetY

                val minimumTop =
                    viewHeight -
                        margin -
                        scaledHeight

                val maximumTop =
                    margin

                currentTop
                    .coerceIn(
                        minimumTop,
                        maximumTop
                    )
            }

        return camera.copy(
            offsetX =
                desiredLeft -
                    content.left *
                        camera.scale,
            offsetY =
                desiredTop -
                    content.top *
                        camera.scale
        )
    }
}

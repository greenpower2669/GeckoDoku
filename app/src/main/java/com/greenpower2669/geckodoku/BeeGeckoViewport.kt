package com.greenpower2669.geckodoku

import kotlin.math.max
import kotlin.math.min

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
        focusY: Float
    ): BeeGeckoCamera {
        val newScale =
            (
                camera.scale *
                    factor
                ).coerceIn(
                MIN_SCALE,
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
            viewHeight <= 0f
        ) {
            return camera
        }

        val left =
            content.left *
                camera.scale +
                camera.offsetX

        val right =
            content.right *
                camera.scale +
                camera.offsetX

        val top =
            content.top *
                camera.scale +
                camera.offsetY

        val bottom =
            content.bottom *
                camera.scale +
                camera.offsetY

        var dx = 0f
        var dy = 0f

        if (
            right <
                visibleMarginPx
        ) {
            dx =
                visibleMarginPx -
                    right
        } else if (
            left >
                viewWidth -
                    visibleMarginPx
        ) {
            dx =
                viewWidth -
                    visibleMarginPx -
                    left
        }

        if (
            bottom <
                visibleMarginPx
        ) {
            dy =
                visibleMarginPx -
                    bottom
        } else if (
            top >
                viewHeight -
                    visibleMarginPx
        ) {
            dy =
                viewHeight -
                    visibleMarginPx -
                    top
        }

        return camera.copy(
            offsetX =
                camera.offsetX +
                    dx,
            offsetY =
                camera.offsetY +
                    dy
        )
    }
}

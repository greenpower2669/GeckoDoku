package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BeeGeckoViewportPolicyTest {
    private val content =
        BeeGeckoBounds(
            left = 0f,
            top = 0f,
            right = 500f,
            bottom = 400f
        )

    @Test
    fun boardRecentersWhenSmallerThanViewport() {
        val camera =
            BeeGeckoViewportPolicy
                .clamp(
                    camera =
                        BeeGeckoCamera(
                            scale = 1f,
                            offsetX = -200f,
                            offsetY = 130f
                        ),
                    viewWidth = 800f,
                    viewHeight = 700f,
                    content = content,
                    visibleMarginPx = 10f
                )

        assertEquals(
            150f,
            camera.offsetX,
            .001f
        )

        assertEquals(
            150f,
            camera.offsetY,
            .001f
        )
    }

    @Test
    fun zoomedBoardStaysInsideViewportEdgesLikeGomoku() {
        val camera =
            BeeGeckoViewportPolicy
                .clamp(
                    camera =
                        BeeGeckoCamera(
                            scale = 2f,
                            offsetX = 1000f,
                            offsetY = -1000f
                        ),
                    viewWidth = 800f,
                    viewHeight = 700f,
                    content = content,
                    visibleMarginPx = 10f
                )

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

        assertTrue(left <= 10.001f)
        assertTrue(right >= 789.999f)
        assertTrue(top <= 10.001f)
        assertTrue(bottom >= 689.999f)
    }

    @Test
    fun pinchCannotZoomOutSmallerThanFit() {
        val fit =
            BeeGeckoViewportPolicy
                .fitScale(
                    viewWidth = 800f,
                    viewHeight = 700f,
                    content = content,
                    marginPx = 10f
                )

        val zoomed =
            BeeGeckoViewportPolicy
                .zoom(
                    camera =
                        BeeGeckoCamera(
                            scale = fit,
                            offsetX = 0f,
                            offsetY = 0f
                        ),
                    factor = .1f,
                    focusX = 400f,
                    focusY = 350f,
                    minimumScale = fit
                )

        assertEquals(
            fit,
            zoomed.scale,
            .001f
        )
    }

    @Test
    fun gesturePolicyMatchesGomokuSemantics() {
        val policy =
            BeeGeckoGesturePolicy(
                dragThresholdPx = 12f
            )

        assertEquals(
            BeeGeckoGestureAction.TAP,
            policy.actionFor(
                pointerCount = 1,
                distancePx = 5f,
                scaleInProgress = false
            )
        )

        assertEquals(
            BeeGeckoGestureAction.PAN,
            policy.actionFor(
                pointerCount = 1,
                distancePx = 30f,
                scaleInProgress = false
            )
        )

        assertEquals(
            BeeGeckoGestureAction.ZOOM,
            policy.actionFor(
                pointerCount = 2,
                distancePx = 0f,
                scaleInProgress = true
            )
        )
    }
}

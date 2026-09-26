package com.greenpower2669.geckodoku

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class MediaRenderGeometryTest {
    @Test
    fun surfaceTextureMatrixGetsCanonicalUvsWithoutSecondVerticalFlip() {
        assertArrayEquals(
            floatArrayOf(
                0f, 0f,
                1f, 0f,
                0f, 1f,
                1f, 1f
            ),
            MediaRenderGeometry.textureCoordinates(),
            0.0001f
        )
    }
}

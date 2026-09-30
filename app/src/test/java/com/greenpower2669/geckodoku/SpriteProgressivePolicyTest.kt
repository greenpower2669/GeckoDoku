package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpriteProgressivePolicyTest {
    @Test
    fun p240UsesInternal60Then120Then240() {
        assertEquals(
            listOf(60, 120, 240),
            SpriteProgressivePolicy
                .stagesFor(
                    SpriteResolution.P240
                )
        )
    }

    @Test
    fun p120StopsAt120() {
        assertEquals(
            listOf(60, 120),
            SpriteProgressivePolicy
                .stagesFor(
                    SpriteResolution.P120
                )
        )
    }

    @Test
    fun p180UsesIntermediate120ThenExactTarget() {
        assertEquals(
            listOf(60, 120, 180),
            SpriteProgressivePolicy
                .stagesFor(
                    SpriteResolution.P180
                )
        )
    }

    @Test
    fun highResolutionAlwaysPassesThrough240() {
        assertEquals(
            listOf(60, 120, 240, 480),
            SpriteProgressivePolicy
                .stagesFor(
                    SpriteResolution.P480
                )
        )
        assertTrue(
            SpriteProgressivePolicy
                .isSecondary(480)
        )
        assertFalse(
            SpriteProgressivePolicy
                .isSecondary(240)
        )
    }
}

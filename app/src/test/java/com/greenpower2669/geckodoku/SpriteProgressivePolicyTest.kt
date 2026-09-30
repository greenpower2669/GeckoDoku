package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun spritePipelineHasNoSecondaryResolutionAbove240() {
        assertEquals(
            240,
            SpriteProgressivePolicy.MAX_HEIGHT
        )
        assertFalse(
            SpriteProgressivePolicy
                .isSecondary(480)
        )
    }
}

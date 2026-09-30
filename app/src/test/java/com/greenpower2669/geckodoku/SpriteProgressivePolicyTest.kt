package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SpriteProgressivePolicyTest {
    @Test
    fun p240UsesOnlyPackaged240Bank() {
        assertEquals(
            listOf(240),
            SpriteProgressivePolicy
                .stagesFor(
                    SpriteResolution.P240
                )
        )
    }

    @Test
    fun spritePipelineIsFixedAt240() {
        assertEquals(
            240,
            SpriteProgressivePolicy.INTERNAL_LOW_HEIGHT
        )
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

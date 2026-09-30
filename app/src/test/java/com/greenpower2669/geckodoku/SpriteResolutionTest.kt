package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class SpriteResolutionTest {
    @Test
    fun runtimeOffersOnly240p() {
        assertEquals(
            listOf(240),
            SpriteResolution
                .values()
                .map {
                    it.heightPx
                }
        )
    }

    @Test
    fun anyStoredHeightFallsBackTo240p() {
        assertEquals(
            SpriteResolution.P240,
            SpriteResolution.fromHeight(
                120
            )
        )
        assertEquals(
            SpriteResolution.P240,
            SpriteResolution.fromHeight(
                480
            )
        )
    }
}

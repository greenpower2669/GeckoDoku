package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class SpriteResolutionTest {
    @Test
    fun selectorOffersOnly120And240() {
        assertEquals(
            listOf(
                120,
                240
            ),
            SpriteResolution
                .values()
                .map {
                    it.heightPx
                }
        )
    }

    @Test
    fun unknownHeightFallsBackTo240p() {
        assertEquals(
            SpriteResolution.P240,
            SpriteResolution.fromHeight(
                -1
            )
        )
    }

    @Test
    fun obsoleteStoredHeightFallsBackTo240p() {
        assertEquals(
            SpriteResolution.P240,
            SpriteResolution.fromHeight(
                480
            )
        )
    }
}

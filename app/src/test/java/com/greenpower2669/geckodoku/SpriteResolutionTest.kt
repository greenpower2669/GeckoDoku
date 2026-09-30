package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class SpriteResolutionTest {
    @Test
    fun selectorOffersTwoLevelsBelow240p() {
        assertEquals(
            listOf(
                120,
                180,
                240,
                360,
                480
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
}

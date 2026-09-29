package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GivenFogVisualPolicyTest {
    @Test
    fun fogIsDeterministicSoftAndIrregular() {
        val first =
            GivenFogVisualPolicy
                .puffs(
                    seed = 47,
                    phase = .37f
                )

        val second =
            GivenFogVisualPolicy
                .puffs(
                    seed = 47,
                    phase = .37f
                )

        assertEquals(
            first,
            second
        )
        assertEquals(
            7,
            first.size
        )
        assertTrue(
            first.all {
                it.alpha in 8..21
            }
        )
        assertTrue(
            first.map {
                it.halfWidth
            }.distinct().size >
                3
        )
        assertTrue(
            first.any {
                kotlin.math.abs(
                    it.offsetX
                ) >
                    .05f
            }
        )
        assertTrue(
            first.any {
                kotlin.math.abs(
                    it.offsetY
                ) >
                    .03f
            }
        )
    }
}

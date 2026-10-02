package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpriteWarmupPolicyTest {
    @Test
    fun hotSetUsesTwoPhysicalBanksForThreeLogicalRoles() {
        val plan =
            SpriteWarmupPolicy.plan()

        assertEquals(
            2,
            plan.hot.size
        )

        val gecko =
            plan.hot.single {
                it.role ==
                    SpriteWarmRole
                        .GECKO_SHARED
            }

        val bee =
            plan.hot.single {
                it.role ==
                    SpriteWarmRole.BEE
            }

        assertEquals(
            AssetMediaCatalog
                .GECKO_IDLE
                .first(),
            gecko.assetPath
        )
        assertEquals(
            ChromaKeyColor.BLUE,
            gecko.keyColor
        )
        assertEquals(
            AssetMediaCatalog
                .BEE_IDLE
                .first(),
            bee.assetPath
        )
        assertEquals(
            ChromaKeyColor.GREEN,
            bee.keyColor
        )
    }

    @Test
    fun backgroundWarmupContainsOnlyRemainingIdleClips() {
        val plan =
            SpriteWarmupPolicy.plan()

        val expected =
            (
                AssetMediaCatalog
                    .GECKO_IDLE
                    .drop(1) +
                    AssetMediaCatalog
                        .BEE_IDLE
                        .drop(1)
                ).toSet()

        assertEquals(
            expected,
            plan.backgroundIdle
                .map {
                    it.assetPath
                }
                .toSet()
        )

        val allWarmAssets =
            (
                plan.hot +
                    plan.backgroundIdle
                ).map {
                it.assetPath
            }

        assertFalse(
            allWarmAssets.contains(
                AssetMediaCatalog
                    .GECKO_LONG_ACTIONS
            )
        )
        assertFalse(
            allWarmAssets.contains(
                AssetMediaCatalog
                    .GECKO_APPEARANCE
            )
        )
        assertFalse(
            allWarmAssets.contains(
                AssetMediaCatalog
                    .GECKO_DISAPPEARANCE
            )
        )
        assertTrue(
            allWarmAssets.all {
                it in
                    AssetMediaCatalog
                        .GECKO_IDLE ||
                    it in
                        AssetMediaCatalog
                            .BEE_IDLE
            }
        )
    }
}

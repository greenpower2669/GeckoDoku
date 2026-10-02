package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpriteCatalogTest {
    @Test
    fun catalogContainsAllSpriteCompatibleGeckoAndBeeAssets() {
        val expected =
            buildSet {
                add(AssetMediaCatalog.GECKO_APPEARANCE)
                add(AssetMediaCatalog.GECKO_DISAPPEARANCE)
                add(AssetMediaCatalog.GECKO_LONG_ACTIONS)
                addAll(AssetMediaCatalog.GECKO_IDLE)
                add(AssetMediaCatalog.BEE_APPEARANCE)
                addAll(AssetMediaCatalog.BEE_IDLE)
            }

        assertEquals(
            expected,
            SpriteCatalog.entries
                .mapTo(linkedSetOf()) {
                    it.assetPath
                }
        )
    }

    @Test
    fun plantAndProfessorStayOutsideSpriteFactory() {
        assertFalse(
            SpriteCatalog.entries.any {
                it.assetPath.startsWith("plante/")
            }
        )
        assertFalse(
            SpriteCatalog.entries.any {
                it.assetPath.startsWith("prof/")
            }
        )
    }

    @Test
    fun geckoAndBeeUseTheirCanonicalKeyColors() {
        assertTrue(
            SpriteCatalog.entries
                .filter {
                    it.family ==
                        SpriteCatalogFamily.GECKO
                }
                .all {
                    it.keyColor ==
                        ChromaKeyColor.BLUE
                }
        )
        assertTrue(
            SpriteCatalog.entries
                .filter {
                    it.family ==
                        SpriteCatalogFamily.BEE
                }
                .all {
                    it.keyColor ==
                        ChromaKeyColor.GREEN
                }
        )
    }
}

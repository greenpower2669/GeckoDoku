package com.greenpower2669.geckodoku

enum class SpriteCatalogFamily {
    GECKO,
    BEE
}

data class SpriteCatalogEntry(
    val assetPath: String,
    val keyColor: ChromaKeyColor,
    val family: SpriteCatalogFamily
)

object SpriteCatalog {
    val entries: List<SpriteCatalogEntry> =
        buildList {
            fun gecko(path: String) {
                add(
                    SpriteCatalogEntry(
                        assetPath = path,
                        keyColor = ChromaKeyColor.BLUE,
                        family = SpriteCatalogFamily.GECKO
                    )
                )
            }

            fun bee(path: String) {
                add(
                    SpriteCatalogEntry(
                        assetPath = path,
                        keyColor = ChromaKeyColor.GREEN,
                        family = SpriteCatalogFamily.BEE
                    )
                )
            }

            // The most likely visible clips come first.
            gecko(AssetMediaCatalog.GECKO_IDLE[0])
            bee(AssetMediaCatalog.BEE_IDLE[0])
            gecko(AssetMediaCatalog.GECKO_IDLE[1])
            bee(AssetMediaCatalog.BEE_IDLE[1])
            gecko(AssetMediaCatalog.GECKO_IDLE[2])
            bee(AssetMediaCatalog.BEE_IDLE[2])
            gecko(AssetMediaCatalog.GECKO_IDLE[3])
            bee(AssetMediaCatalog.BEE_IDLE[3])

            gecko(AssetMediaCatalog.GECKO_APPEARANCE)
            bee(AssetMediaCatalog.BEE_APPEARANCE)
            gecko(AssetMediaCatalog.GECKO_DISAPPEARANCE)
            gecko(AssetMediaCatalog.GECKO_LONG_ACTIONS)
        }

    fun contains(
        assetPath: String,
        keyColor: ChromaKeyColor
    ): Boolean =
        entries.any {
            it.assetPath == assetPath &&
                it.keyColor == keyColor
        }
}

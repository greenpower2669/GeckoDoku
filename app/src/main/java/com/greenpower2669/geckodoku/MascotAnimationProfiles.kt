package com.greenpower2669.geckodoku

object MascotAnimationProfiles {
    val gecko =
        MascotAnimationProfile(
            kind = MascotKind.GECKO,
            pngAsset =
                AssetMediaCatalog
                    .GECKO_PORTRAIT,
            appearanceAsset =
                AssetMediaCatalog
                    .GECKO_APPEARANCE,
            disappearanceAsset =
                AssetMediaCatalog
                    .GECKO_DISAPPEARANCE,
            idleAssets =
                AssetMediaCatalog
                    .GECKO_IDLE,
            cuteAssets =
                listOf(
                    AssetMediaCatalog
                        .GECKO_LONG_ACTIONS
                ),
            keyColor =
                ChromaKeyColor.BLUE
        )

    val bee =
        MascotAnimationProfile(
            kind = MascotKind.BEE,
            pngAsset =
                AssetMediaCatalog
                    .BEE_PORTRAIT,
            appearanceAsset =
                AssetMediaCatalog
                    .BEE_APPEARANCE,
            disappearanceAsset = null,
            idleAssets =
                AssetMediaCatalog
                    .BEE_IDLE,
            cuteAssets =
                emptyList(),
            keyColor =
                ChromaKeyColor.GREEN
        )

    val plant =
        MascotAnimationProfile(
            kind = MascotKind.PLANT,
            pngAsset =
                AssetMediaCatalog
                    .PLANT_PORTRAIT,
            appearanceAsset = null,
            disappearanceAsset = null,
            idleAssets =
                AssetMediaCatalog
                    .PLANT_IDLE,
            cuteAssets =
                listOf(
                    AssetMediaCatalog
                        .PLANT_LONG_ACTION
                ),
            keyColor =
                ChromaKeyColor.BLUE
        )

    val all =
        listOf(
            gecko,
            bee,
            plant
        )
}

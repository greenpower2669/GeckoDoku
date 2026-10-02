package com.greenpower2669.geckodoku

object GeckoCellAnimationAssetPolicy {
    fun assetFor(
        kind: RichMediaKind
    ): String? =
        when (kind) {
            RichMediaKind
                .GECKO_APPEARANCE ->
                AssetMediaCatalog
                    .GECKO_APPEARANCE

            RichMediaKind
                .GECKO_DISAPPEARANCE ->
                AssetMediaCatalog
                    .GECKO_DISAPPEARANCE

            RichMediaKind
                .GECKO_LONG_ACTION ->
                AssetMediaCatalog
                    .GECKO_LONG_ACTIONS

            else -> null
        }
}

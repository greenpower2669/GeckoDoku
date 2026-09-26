package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class AssetMediaCatalogTest {
    @Test
    fun canonicalStaticGeckoUsesUploadedTransparentAsset() {
        assertEquals(
            "gecko/Gecko_tr.png",
            AssetMediaCatalog.GECKO_PORTRAIT
        )
    }
}

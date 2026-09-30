package com.greenpower2669.geckodoku

data class StableFrameSpec(
    val assetPath: String,
    val keyColor: ChromaKeyColor
)

object StableFramePolicy {
    fun specFor(
        kind: MascotKind
    ): StableFrameSpec? =
        when (kind) {
            MascotKind.GECKO ->
                StableFrameSpec(
                    assetPath =
                        AssetMediaCatalog
                            .GECKO_IDLE
                            .first(),
                    keyColor =
                        ChromaKeyColor.BLUE
                )

            MascotKind.BEE ->
                StableFrameSpec(
                    assetPath =
                        AssetMediaCatalog
                            .BEE_IDLE
                            .first(),
                    keyColor =
                        ChromaKeyColor.GREEN
                )

            MascotKind.PLANT ->
                null
        }

    fun useStableFrame(
        kind: MascotKind,
        animationsEnabled: Boolean
    ): Boolean =
        animationsEnabled &&
            specFor(kind) != null
}

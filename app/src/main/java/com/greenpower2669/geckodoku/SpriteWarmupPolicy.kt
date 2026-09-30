package com.greenpower2669.geckodoku

enum class SpriteWarmRole {
    GECKO_SHARED,
    BEE
}

data class SpriteWarmAsset(
    val role: SpriteWarmRole,
    val assetPath: String,
    val keyColor: ChromaKeyColor
)

data class SpriteWarmupPlan(
    val hot: List<SpriteWarmAsset>,
    val backgroundIdle: List<SpriteWarmAsset>
)

/**
 * Three logical warm roles are represented by two physical frame banks:
 * green Gecko + yellow Gecko share GECKO_SHARED, while yellow stays
 * a draw-time ColorMatrix; Bee owns the second bank.
 */
object SpriteWarmupPolicy {
    fun plan(): SpriteWarmupPlan {
        val geckoIdle =
            AssetMediaCatalog.GECKO_IDLE
        val beeIdle =
            AssetMediaCatalog.BEE_IDLE

        val hot =
            listOf(
                SpriteWarmAsset(
                    role =
                        SpriteWarmRole
                            .GECKO_SHARED,
                    assetPath =
                        geckoIdle.first(),
                    keyColor =
                        ChromaKeyColor.BLUE
                ),
                SpriteWarmAsset(
                    role =
                        SpriteWarmRole.BEE,
                    assetPath =
                        beeIdle.first(),
                    keyColor =
                        ChromaKeyColor.GREEN
                )
            )

        val backgroundIdle =
            buildList {
                geckoIdle.drop(1)
                    .forEach { asset ->
                        add(
                            SpriteWarmAsset(
                                role =
                                    SpriteWarmRole
                                        .GECKO_SHARED,
                                assetPath =
                                    asset,
                                keyColor =
                                    ChromaKeyColor
                                        .BLUE
                            )
                        )
                    }

                beeIdle.drop(1)
                    .forEach { asset ->
                        add(
                            SpriteWarmAsset(
                                role =
                                    SpriteWarmRole
                                        .BEE,
                                assetPath =
                                    asset,
                                keyColor =
                                    ChromaKeyColor
                                        .GREEN
                            )
                        )
                    }
            }

        return SpriteWarmupPlan(
            hot = hot,
            backgroundIdle =
                backgroundIdle
        )
    }
}

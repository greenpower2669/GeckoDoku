package com.greenpower2669.geckodoku

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class GivenFogPuff(
    val offsetX: Float,
    val offsetY: Float,
    val halfWidth: Float,
    val halfHeight: Float,
    val alpha: Int
)

object GivenFogVisualPolicy {
    private const val PUFF_COUNT = 7

    fun puffs(
        seed: Int,
        phase: Float
    ): List<GivenFogPuff> {
        val normalizedPhase =
            phase - kotlin.math.floor(
                phase.toDouble()
            ).toFloat()

        return List(PUFF_COUNT) {
            index ->
            if (index == 0) {
                // Very faint horizontal haze. It prevents the
                // cluster from looking like isolated circles.
                GivenFogPuff(
                    offsetX =
                        sin(
                            normalizedPhase *
                                PI *
                                2.0 +
                                seed *
                                    .017
                        ).toFloat() *
                            .035f,
                    offsetY = .025f,
                    halfWidth = .34f,
                    halfHeight = .095f,
                    alpha = 8
                )
            } else {
                val localSeed =
                    seed *
                        .173 +
                        index *
                            1.61803398875

                val angle =
                    normalizedPhase *
                        PI *
                        2.0 *
                        (
                            .42 +
                                index *
                                    .027
                            ) +
                        localSeed

                val irregular =
                    sin(
                        seed *
                            .071 +
                            index *
                                2.31
                    ).toFloat()

                val widthVariant =
                    Math.floorMod(
                        seed +
                            index *
                                11,
                        7
                    )

                val heightVariant =
                    Math.floorMod(
                        seed *
                            3 +
                            index *
                                5,
                        6
                    )

                val alphaVariant =
                    Math.floorMod(
                        seed +
                            index *
                                13,
                        9
                    )

                GivenFogPuff(
                    offsetX =
                        cos(angle)
                            .toFloat() *
                            .16f +
                            irregular *
                                .035f,
                    offsetY =
                        sin(
                            angle *
                                .83 +
                                index *
                                    .61
                        ).toFloat() *
                            .085f +
                            cos(localSeed)
                                .toFloat() *
                                .022f,
                    halfWidth =
                        .105f +
                            widthVariant *
                                .010f,
                    halfHeight =
                        .070f +
                            heightVariant *
                                .009f,
                    alpha =
                        13 +
                            alphaVariant
                )
            }
        }
    }
}

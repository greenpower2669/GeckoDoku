package com.greenpower2669.geckodoku

/**
 * Single source of truth for the pointy-top Bee & Gecko hex geometry.
 *
 * Screen coordinates use +Y downward:
 * - Q constant follows +60°  : ↖↘
 * - R constant follows   0°  : ←→
 * - S constant follows -60°  : ↙↗
 */
object BeeGeckoAxisGeometry {
    val paletteOrder:
        List<HexAxis> =
        listOf(
            HexAxis.Q,
            HexAxis.S,
            HexAxis.R
        )

    fun screenAngleDegrees(
        axis: HexAxis
    ): Int =
        when (axis) {
            HexAxis.Q -> 60
            HexAxis.R -> 0
            HexAxis.S -> -60
        }

    fun symbol(
        axis: HexAxis
    ): String =
        when (
            screenAngleDegrees(
                axis
            )
        ) {
            60 -> "↖↘"
            0 -> "←→"
            -60 -> "↙↗"
            else ->
                error(
                    "Unsupported Bee/Gecko axis angle"
                )
        }

    fun menuLabel(
        axis: HexAxis
    ): String =
        symbol(axis) +
            "  Axe " +
            axis.name

    fun legend(): String =
        paletteOrder
            .joinToString(
                separator = "  "
            ) {
                axis ->
                axis.name +
                    " " +
                    symbol(axis)
            }

    fun spokenLabel(
        axis: HexAxis
    ): String =
        "l'axe " +
            axis.name +
            " " +
            symbol(axis)

    fun sharedAxis(
        first: HexCoord,
        second: HexCoord
    ): HexAxis? =
        HexAxis.entries
            .firstOrNull {
                axis ->
                first.axisValue(axis) ==
                    second.axisValue(axis)
            }

    fun center(
        cell: HexCoord,
        horizontalStep: Float,
        verticalStep: Float
    ): Pair<Float, Float> {
        val x =
            horizontalStep *
                (
                    cell.q +
                        cell.r /
                            2f
                    )

        val y =
            verticalStep *
                cell.r

        return x to y
    }
}

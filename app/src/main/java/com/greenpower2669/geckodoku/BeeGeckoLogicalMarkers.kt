package com.greenpower2669.geckodoku

enum class BeeGeckoCrossState(
    val label: String
) {
    HYPOTHESIS("Hypothèse"),
    CONFIRMED("Déduction sûre"),
    IMPOSSIBLE("Impossible");

    fun next():
        BeeGeckoCrossState? =
        when (this) {
            HYPOTHESIS ->
                CONFIRMED

            CONFIRMED ->
                IMPOSSIBLE

            IMPOSSIBLE ->
                null
        }
}

data class BeeGeckoLogicalMarks(
    val geckoCandidate:
        Boolean = false,
    val beeCandidate:
        Boolean = false,
    val excludedAxes:
        Set<HexAxis> =
        emptySet()
) {
    val isEmpty: Boolean
        get() =
            !geckoCandidate &&
                !beeCandidate &&
                excludedAxes.isEmpty()

    fun toggleGecko():
        BeeGeckoLogicalMarks =
        copy(
            geckoCandidate =
                !geckoCandidate
        )

    fun toggleBee():
        BeeGeckoLogicalMarks =
        copy(
            beeCandidate =
                !beeCandidate
        )

    fun toggleAxis(
        axis: HexAxis
    ): BeeGeckoLogicalMarks {
        val nextAxes =
            excludedAxes
                .toMutableSet()

        if (!nextAxes.add(axis)) {
            nextAxes.remove(axis)
        }

        return copy(
            excludedAxes =
                nextAxes.toSet()
        )
    }
}

data class BeeGeckoProfessorVisuals(
    val markers:
        Map<
            HexCoord,
            BeeGeckoLogicalMarks
            >,
    val crosses:
        Map<
            HexCoord,
            BeeGeckoCrossState
            >
)

object BeeGeckoProfessorMarkerPolicy {
    fun forStep(
        snapshot:
            BeeGeckoSnapshot,
        step:
            BeeGeckoSolveStep
    ): BeeGeckoProfessorVisuals {
        val markerMap =
            linkedMapOf<
                HexCoord,
                BeeGeckoLogicalMarks
                >()

        fun update(
            cell: HexCoord,
            transform:
                (BeeGeckoLogicalMarks) ->
                    BeeGeckoLogicalMarks
        ) {
            markerMap[cell] =
                transform(
                    markerMap[cell]
                        ?: BeeGeckoLogicalMarks()
                )
        }

        step.gecko
            ?.let {
                cell ->
                update(cell) {
                    it.copy(
                        geckoCandidate =
                            true
                    )
                }
            }

        step.bee
            ?.let {
                cell ->
                update(cell) {
                    it.copy(
                        beeCandidate =
                            true
                    )
                }
            }

        step.eliminated
            .forEach {
                cell ->
                val axes =
                    axesBlockedByConfirmedPieces(
                        snapshot,
                        cell
                    )

                if (axes.isNotEmpty()) {
                    update(cell) {
                        it.copy(
                            excludedAxes =
                                it.excludedAxes +
                                    axes
                        )
                    }
                }
            }

        val crossMap =
            step.eliminated
                .associateWith {
                    BeeGeckoCrossState
                        .IMPOSSIBLE
                }

        return BeeGeckoProfessorVisuals(
            markers =
                markerMap
                    .filterValues {
                        !it.isEmpty
                    },
            crosses =
                crossMap
        )
    }

    private fun axesBlockedByConfirmedPieces(
        snapshot: BeeGeckoSnapshot,
        cell: HexCoord
    ): Set<HexAxis> =
        buildSet {
            HexAxis.entries
                .forEach {
                    axis ->
                    val value =
                        cell.axisValue(
                            axis
                        )

                    val blocked =
                        snapshot
                            .confirmedGeckos
                            .any {
                                it != cell &&
                                    it.axisValue(
                                        axis
                                    ) ==
                                    value
                            } ||
                            snapshot
                                .confirmedBees
                                .any {
                                    it != cell &&
                                        it.axisValue(
                                            axis
                                        ) ==
                                        value
                                }

                    if (blocked) {
                        add(axis)
                    }
                }
        }
}

object BeeGeckoFogPolicy {
    const val DURATION_MS =
        2400L

    fun phase(
        nowMs: Long,
        seed: Int
    ): Float {
        val shifted =
            (
                nowMs +
                    seed *
                        137L
                ) %
                DURATION_MS

        return shifted
            .toFloat() /
            DURATION_MS
                .toFloat()
    }
}

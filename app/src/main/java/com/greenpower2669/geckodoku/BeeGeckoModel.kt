package com.greenpower2669.geckodoku

import java.util.Random
import kotlin.math.abs

data class HexCoord(
    val q: Int,
    val r: Int
) {
    val s: Int
        get() =
            -q - r

    fun neighbors():
        List<HexCoord> =
        HEX_DIRECTIONS.map {
            (dq, dr) ->
            HexCoord(
                q + dq,
                r + dr
            )
        }

    fun axisValue(
        axis: HexAxis
    ): Int =
        when (axis) {
            HexAxis.Q -> q
            HexAxis.R -> r
            HexAxis.S -> s
        }

    companion object {
        private val HEX_DIRECTIONS =
            listOf(
                1 to 0,
                1 to -1,
                0 to -1,
                -1 to 0,
                -1 to 1,
                0 to 1
            )
    }
}

enum class HexAxis {
    Q,
    R,
    S
}

enum class BeeGeckoPiece {
    GECKO,
    BEE
}

data class BeeGeckoPair(
    val gecko: HexCoord,
    val bee: HexCoord,
    val region: Int
)

data class BeeGeckoPuzzle(
    val id: String,
    val radius: Int,
    val regions: Map<HexCoord, Int>,
    val solutionGeckos: Set<HexCoord>,
    val solutionBees: Set<HexCoord>,
    val solutionPairs: List<BeeGeckoPair>,
    val givenGeckos: Set<HexCoord>,
    val givenBees: Set<HexCoord>,
    val difficulty: GameDifficulty,
    val seed: Long,
    val solverTrace:
        List<BeeGeckoSolveStep> =
        emptyList()
) {
    init {
        require(radius in 1..6)
        require(regions.isNotEmpty())
        require(solutionGeckos.size == solutionPairs.size)
        require(solutionBees.size == solutionPairs.size)
        require(regions.values.toSet().size == solutionPairs.size)
        require(givenGeckos.all { it in solutionGeckos })
        require(givenBees.all { it in solutionBees })

        solutionPairs.forEach {
            pair ->
            require(pair.gecko in solutionGeckos)
            require(pair.bee in solutionBees)
            require(regionAt(pair.gecko) == pair.region)
            require(regionAt(pair.bee) == pair.region)
            require(areNeighbors(pair.gecko, pair.bee))
        }

        require(
            solutionBees.all {
                bee ->
                solutionGeckos.count {
                    gecko ->
                    areNeighbors(
                        gecko,
                        bee
                    )
                } == 1
            }
        )

        require(
            solutionGeckos.all {
                gecko ->
                solutionBees.count {
                    bee ->
                    areNeighbors(
                        gecko,
                        bee
                    )
                } == 1
            }
        )

        for (
            typeCells in
            listOf(
                solutionGeckos,
                solutionBees
            )
        ) {
            for (axis in HexAxis.entries) {
                require(
                    typeCells
                        .groupBy {
                            it.axisValue(axis)
                        }
                        .values
                        .all {
                            it.size <= 1
                        }
                )
            }
        }

        for (
            region in
            regions.values.toSet()
        ) {
            require(
                solutionGeckos.count {
                    regionAt(it) ==
                        region
                } == 1
            )

            require(
                solutionBees.count {
                    regionAt(it) ==
                        region
                } == 1
            )
        }
    }

    val cells: Set<HexCoord>
        get() =
            regions.keys

    val regionCount: Int
        get() =
            solutionPairs.size

    fun contains(
        cell: HexCoord
    ): Boolean =
        regions.containsKey(cell)

    fun regionAt(
        cell: HexCoord
    ): Int =
        requireNotNull(
            regions[cell]
        )

    fun solutionPieceAt(
        cell: HexCoord
    ): BeeGeckoPiece? =
        when (cell) {
            in solutionGeckos ->
                BeeGeckoPiece.GECKO

            in solutionBees ->
                BeeGeckoPiece.BEE

            else ->
                null
        }

    fun givenPieceAt(
        cell: HexCoord
    ): BeeGeckoPiece? =
        when (cell) {
            in givenGeckos ->
                BeeGeckoPiece.GECKO

            in givenBees ->
                BeeGeckoPiece.BEE

            else ->
                null
        }

    fun cellsInRegion(
        region: Int
    ): Set<HexCoord> =
        regions
            .filterValues {
                it == region
            }
            .keys

    fun pairForRegion(
        region: Int
    ): BeeGeckoPair? =
        solutionPairs
            .firstOrNull {
                it.region == region
            }

    companion object {
        fun areNeighbors(
            first: HexCoord,
            second: HexCoord
        ): Boolean =
            second in
                first.neighbors()
    }
}

data class BeeGeckoSnapshot(
    val puzzle: BeeGeckoPuzzle,
    val confirmedGeckos: Set<HexCoord>,
    val confirmedBees: Set<HexCoord>,
    val manualCrosses: Set<HexCoord>,
    val crossStates:
        Map<
            HexCoord,
            BeeGeckoCrossState
            > =
        manualCrosses.associateWith {
            BeeGeckoCrossState
                .IMPOSSIBLE
        },
    val logicalMarkers:
        Map<
            HexCoord,
            BeeGeckoLogicalMarks
            > =
        emptyMap(),
    val markers:
        Map<HexCoord, CustomMarker>,
    val mistakes: Int,
    val complete: Boolean
) {
    fun pieceAt(
        cell: HexCoord
    ): BeeGeckoPiece? =
        when (cell) {
            in confirmedGeckos ->
                BeeGeckoPiece.GECKO

            in confirmedBees ->
                BeeGeckoPiece.BEE

            else ->
                null
        }

    fun isGiven(
        cell: HexCoord
    ): Boolean =
        cell in
            puzzle.givenGeckos ||
            cell in
                puzzle.givenBees

    val confirmedPairCount: Int
        get() =
            puzzle.solutionPairs
                .count {
                    it.gecko in
                        confirmedGeckos &&
                        it.bee in
                            confirmedBees
                }
}

enum class BeeGeckoActionFeedback {
    CROSS_SET,
    CROSS_REMOVED,
    CROSS_BLOCKED,
    PIECE_CONFIRMED,
    PIECE_REMOVED,
    WRONG_PIECE,
    GIVEN_LOCKED,
    COMPLETED,
    MARKER_SET,
    MARKER_CLEARED
}

class BeeGeckoGameEngine(
    private val puzzle: BeeGeckoPuzzle,
    initialGeckos:
        Set<HexCoord> =
        puzzle.givenGeckos,
    initialBees:
        Set<HexCoord> =
        puzzle.givenBees,
    initialCrosses:
        Set<HexCoord> =
        emptySet(),
    initialCrossStates:
        Map<
            HexCoord,
            BeeGeckoCrossState
            > =
        emptyMap(),
    initialLogicalMarkers:
        Map<
            HexCoord,
            BeeGeckoLogicalMarks
            > =
        emptyMap(),
    initialMarkers:
        Map<HexCoord, CustomMarker> =
        emptyMap(),
    initialMistakes:
        Int = 0
) {
    private val confirmedGeckos =
        linkedSetOf<HexCoord>()
            .apply {
                addAll(
                    initialGeckos
                        .filter {
                            it in
                                puzzle.solutionGeckos
                        }
                )
                addAll(
                    puzzle.givenGeckos
                )
            }

    private val confirmedBees =
        linkedSetOf<HexCoord>()
            .apply {
                addAll(
                    initialBees
                        .filter {
                            it in
                                puzzle.solutionBees
                        }
                )
                addAll(
                    puzzle.givenBees
                )
            }

    private val crossStates =
        linkedMapOf<
            HexCoord,
            BeeGeckoCrossState
            >()
            .apply {
                initialCrosses
                    .filter {
                        puzzle.contains(it) &&
                            it !in confirmedGeckos &&
                            it !in confirmedBees
                    }
                    .forEach {
                        put(
                            it,
                            BeeGeckoCrossState
                                .IMPOSSIBLE
                        )
                    }

                initialCrossStates
                    .filterKeys {
                        puzzle.contains(it) &&
                            it !in confirmedGeckos &&
                            it !in confirmedBees
                    }
                    .forEach {
                        (cell, state) ->
                        put(
                            cell,
                            state
                        )
                    }
            }

    private val manualCrosses:
        Set<HexCoord>
        get() =
            crossStates
                .filterValues {
                    it !=
                        BeeGeckoCrossState
                            .HYPOTHESIS
                }
                .keys

    private val logicalMarkers =
        linkedMapOf<
            HexCoord,
            BeeGeckoLogicalMarks
            >()
            .apply {
                initialLogicalMarkers
                    .filterKeys {
                        puzzle.contains(it)
                    }
                    .filterValues {
                        !it.isEmpty
                    }
                    .forEach {
                        (cell, value) ->
                        put(
                            cell,
                            value
                        )
                    }
            }

    private val markers =
        linkedMapOf<
            HexCoord,
            CustomMarker
            >()
            .apply {
                initialMarkers
                    .filterKeys {
                        puzzle.contains(it)
                    }
                    .forEach {
                        (cell, marker) ->
                        put(
                            cell,
                            marker
                        )
                    }
            }

    var mistakes: Int =
        initialMistakes
            .coerceAtLeast(0)
        private set

    fun snapshot():
        BeeGeckoSnapshot =
        BeeGeckoSnapshot(
            puzzle = puzzle,
            confirmedGeckos =
                confirmedGeckos
                    .toSet(),
            confirmedBees =
                confirmedBees
                    .toSet(),
            manualCrosses =
                manualCrosses
                    .toSet(),
            crossStates =
                crossStates
                    .toMap(),
            logicalMarkers =
                logicalMarkers
                    .toMap(),
            markers =
                markers.toMap(),
            mistakes =
                mistakes,
            complete =
                confirmedGeckos ==
                    puzzle.solutionGeckos &&
                    confirmedBees ==
                        puzzle.solutionBees
        )

    fun toggleCross(
        cell: HexCoord
    ): BeeGeckoActionFeedback {
        if (!puzzle.contains(cell)) {
            return BeeGeckoActionFeedback
                .CROSS_BLOCKED
        }

        if (
            cell in puzzle.givenGeckos ||
            cell in puzzle.givenBees
        ) {
            return BeeGeckoActionFeedback
                .GIVEN_LOCKED
        }

        if (
            cell in confirmedGeckos ||
            cell in confirmedBees
        ) {
            return BeeGeckoActionFeedback
                .CROSS_BLOCKED
        }

        val current =
            crossStates[cell]

        val next =
            current
                ?.next()
                ?: BeeGeckoCrossState
                    .HYPOTHESIS

        if (current ==
            BeeGeckoCrossState
                .IMPOSSIBLE
        ) {
            crossStates.remove(
                cell
            )

            return BeeGeckoActionFeedback
                .CROSS_REMOVED
        }

        crossStates[cell] =
            next

        return BeeGeckoActionFeedback
            .CROSS_SET
    }

    fun setCrossState(
        cell: HexCoord,
        state:
            BeeGeckoCrossState?
    ): BeeGeckoActionFeedback {
        if (
            !puzzle.contains(cell) ||
            cell in confirmedGeckos ||
            cell in confirmedBees
        ) {
            return BeeGeckoActionFeedback
                .CROSS_BLOCKED
        }

        if (state == null) {
            crossStates.remove(cell)

            return BeeGeckoActionFeedback
                .CROSS_REMOVED
        }

        crossStates[cell] = state

        return BeeGeckoActionFeedback
            .CROSS_SET
    }

    fun toggleLogicalPieceMarker(
        cell: HexCoord,
        piece: BeeGeckoPiece
    ): BeeGeckoActionFeedback {
        if (!puzzle.contains(cell)) {
            return BeeGeckoActionFeedback
                .CROSS_BLOCKED
        }

        val current =
            logicalMarkers[cell]
                ?: BeeGeckoLogicalMarks()

        val next =
            if (
                piece ==
                    BeeGeckoPiece.GECKO
            ) {
                current.toggleGecko()
            } else {
                current.toggleBee()
            }

        if (next.isEmpty) {
            logicalMarkers.remove(cell)
        } else {
            logicalMarkers[cell] =
                next
        }

        return BeeGeckoActionFeedback
            .MARKER_SET
    }

    fun toggleAxisMarker(
        cell: HexCoord,
        axis: HexAxis
    ): BeeGeckoActionFeedback {
        if (!puzzle.contains(cell)) {
            return BeeGeckoActionFeedback
                .CROSS_BLOCKED
        }

        val current =
            logicalMarkers[cell]
                ?: BeeGeckoLogicalMarks()

        val next =
            current.toggleAxis(axis)

        if (next.isEmpty) {
            logicalMarkers.remove(cell)
        } else {
            logicalMarkers[cell] =
                next
        }

        return BeeGeckoActionFeedback
            .MARKER_SET
    }

    fun clearLogicalMarkers(
        cell: HexCoord
    ): BeeGeckoActionFeedback {
        logicalMarkers.remove(cell)

        return BeeGeckoActionFeedback
            .MARKER_CLEARED
    }

    fun placePiece(
        cell: HexCoord,
        piece: BeeGeckoPiece
    ): BeeGeckoActionFeedback {
        if (!puzzle.contains(cell)) {
            return BeeGeckoActionFeedback
                .CROSS_BLOCKED
        }

        val given =
            puzzle.givenPieceAt(
                cell
            )

        if (given != null) {
            return BeeGeckoActionFeedback
                .GIVEN_LOCKED
        }

        val already =
            snapshot()
                .pieceAt(cell)

        if (already == piece) {
            return removePiece(cell)
        }

        if (
            puzzle.solutionPieceAt(
                cell
            ) !=
                piece
        ) {
            mistakes += 1

            confirmedGeckos.remove(
                cell
            )
            confirmedBees.remove(
                cell
            )
            crossStates[cell] =
                BeeGeckoCrossState
                    .IMPOSSIBLE

            return BeeGeckoActionFeedback
                .WRONG_PIECE
        }

        confirmedGeckos.remove(
            cell
        )
        confirmedBees.remove(
            cell
        )
        crossStates.remove(
            cell
        )

        if (
            piece ==
                BeeGeckoPiece.GECKO
        ) {
            confirmedGeckos.add(
                cell
            )
        } else {
            confirmedBees.add(
                cell
            )
        }

        return if (
            snapshot().complete
        ) {
            BeeGeckoActionFeedback
                .COMPLETED
        } else {
            BeeGeckoActionFeedback
                .PIECE_CONFIRMED
        }
    }

    fun removePiece(
        cell: HexCoord
    ): BeeGeckoActionFeedback {
        if (
            puzzle.givenPieceAt(
                cell
            ) !=
                null
        ) {
            return BeeGeckoActionFeedback
                .GIVEN_LOCKED
        }

        val removed =
            confirmedGeckos.remove(
                cell
            ) ||
                confirmedBees.remove(
                    cell
                )

        return if (removed) {
            BeeGeckoActionFeedback
                .PIECE_REMOVED
        } else {
            BeeGeckoActionFeedback
                .CROSS_BLOCKED
        }
    }

    fun setMarker(
        cell: HexCoord,
        marker: CustomMarker?
    ): BeeGeckoActionFeedback {
        if (!puzzle.contains(cell)) {
            return BeeGeckoActionFeedback
                .CROSS_BLOCKED
        }

        if (marker == null) {
            markers.remove(cell)

            return BeeGeckoActionFeedback
                .MARKER_CLEARED
        }

        markers[cell] =
            marker

        return BeeGeckoActionFeedback
            .MARKER_SET
    }

    fun applyProfessorStep(
        step: BeeGeckoSolveStep
    ): BeeGeckoActionFeedback {
        var changed = false

        step.eliminated
            .forEach {
                cell ->
                if (
                    cell !in
                        confirmedGeckos &&
                    cell !in
                        confirmedBees &&
                    puzzle.contains(cell)
                ) {
                    crossStates[cell] =
                        BeeGeckoCrossState
                            .CONFIRMED
                }
            }

        step.gecko
            ?.let {
                if (
                    it in
                        puzzle.solutionGeckos
                ) {
                    confirmedGeckos.add(
                        it
                    )
                    crossStates.remove(
                        it
                    )
                    changed = true
                }
            }

        step.bee
            ?.let {
                if (
                    it in
                        puzzle.solutionBees
                ) {
                    confirmedBees.add(
                        it
                    )
                    crossStates.remove(
                        it
                    )
                    changed = true
                }
            }

        return when {
            snapshot().complete ->
                BeeGeckoActionFeedback
                    .COMPLETED

            changed ->
                BeeGeckoActionFeedback
                    .PIECE_CONFIRMED

            else ->
                BeeGeckoActionFeedback
                    .CROSS_SET
        }
    }
}

enum class BeeGeckoTechnique {
    ZONE_SINGLE,
    AXIS_ELIMINATION,
    ADJACENCY_SINGLE,
    CHAIN_PROPAGATION,
    PROJECTION
}

data class BeeGeckoSolveStep(
    val technique:
        BeeGeckoTechnique,
    val region: Int,
    val gecko: HexCoord? = null,
    val bee: HexCoord? = null,
    val analyzed:
        Set<HexCoord> =
        emptySet(),
    val candidates:
        Set<HexCoord> =
        emptySet(),
    val eliminated:
        Set<HexCoord> =
        emptySet(),
    val explanation: String
)

data class BeeGeckoHint(
    val step:
        BeeGeckoSolveStep,
    val blue:
        Set<HexCoord>,
    val orange:
        Set<HexCoord>,
    val red:
        Set<HexCoord>,
    val green:
        Set<HexCoord>,
    val message: String,
    val logicalMarkers:
        Map<
            HexCoord,
            BeeGeckoLogicalMarks
            > =
        emptyMap(),
    val crossStates:
        Map<
            HexCoord,
            BeeGeckoCrossState
            > =
        emptyMap()
)

data class BeeGeckoDifficultyReport(
    val ratedDifficulty:
        GameDifficulty,
    val logicallySolvable: Boolean,
    val directSteps: Int,
    val adjacencySteps: Int,
    val projectionSteps: Int,
    val maxPairOptions: Int,
    val steps:
        List<BeeGeckoSolveStep>
)

private data class BeeGeckoPairOption(
    val gecko: HexCoord,
    val bee: HexCoord
)

object BeeGeckoRules {
    fun getHexNeighbors(
        cell: HexCoord,
        puzzle: BeeGeckoPuzzle
    ): List<HexCoord> =
        cell.neighbors()
            .filter {
                puzzle.contains(it)
            }

    fun areNeighbors(
        first: HexCoord,
        second: HexCoord
    ): Boolean =
        BeeGeckoPuzzle
            .areNeighbors(
                first,
                second
            )

    fun sameAxis(
        first: HexCoord,
        second: HexCoord
    ): Boolean =
        HexAxis.entries.any {
            first.axisValue(it) ==
                second.axisValue(it)
        }

    fun axisLabel(
        first: HexCoord,
        second: HexCoord
    ): String =
        when {
            first.q == second.q ->
                "l'axe ↖↘"

            first.r == second.r ->
                "l'axe ↗↙"

            first.s == second.s ->
                "l'axe ↑↓"

            else ->
                "un autre axe"
        }

    fun validateComplete(
        puzzle: BeeGeckoPuzzle,
        geckos: Set<HexCoord>,
        bees: Set<HexCoord>
    ): Boolean {
        if (
            geckos.size !=
                puzzle.regionCount ||
            bees.size !=
                puzzle.regionCount
        ) {
            return false
        }

        for (
            region in
            0 until puzzle.regionCount
        ) {
            val regionGeckos =
                geckos.filter {
                    puzzle.regionAt(it) ==
                        region
                }

            val regionBees =
                bees.filter {
                    puzzle.regionAt(it) ==
                        region
                }

            if (
                regionGeckos.size != 1 ||
                regionBees.size != 1 ||
                !areNeighbors(
                    regionGeckos.single(),
                    regionBees.single()
                )
            ) {
                return false
            }
        }

        if (
            bees.any {
                bee ->
                geckos.count {
                    gecko ->
                    areNeighbors(
                        gecko,
                        bee
                    )
                } != 1
            } ||
            geckos.any {
                gecko ->
                bees.count {
                    bee ->
                    areNeighbors(
                        gecko,
                        bee
                    )
                } != 1
            }
        ) {
            return false
        }

        for (axis in HexAxis.entries) {
            if (
                geckos
                    .groupBy {
                        it.axisValue(axis)
                    }
                    .values
                    .any {
                        it.size > 1
                    } ||
                bees
                    .groupBy {
                        it.axisValue(axis)
                    }
                    .values
                    .any {
                        it.size > 1
                    }
            ) {
                return false
            }
        }

        return true
    }
}

object BeeGeckoSolver {
    fun nextHint(
        snapshot: BeeGeckoSnapshot
    ): BeeGeckoHint? {
        val report =
            analyze(
                puzzle =
                    snapshot.puzzle,
                snapshot =
                    snapshot,
                stopAfterFirstStep =
                    true
            )

        val step =
            report.steps
                .firstOrNull()
                ?: return null

        val green =
            buildSet {
                step.gecko
                    ?.let {
                        add(it)
                    }
                step.bee
                    ?.let {
                        add(it)
                    }
            }

        val professorVisuals =
            BeeGeckoProfessorMarkerPolicy
                .forStep(
                    snapshot,
                    step
                )

        return BeeGeckoHint(
            step = step,
            blue =
                step.analyzed,
            orange =
                step.candidates -
                    green,
            red =
                step.eliminated,
            green =
                green,
            message =
                step.explanation,
            logicalMarkers =
                professorVisuals
                    .markers,
            crossStates =
                professorVisuals
                    .crosses
        )
    }

    fun analyze(
        puzzle: BeeGeckoPuzzle,
        snapshot:
            BeeGeckoSnapshot =
            BeeGeckoSnapshot(
                puzzle =
                    puzzle,
                confirmedGeckos =
                    puzzle.givenGeckos,
                confirmedBees =
                    puzzle.givenBees,
                manualCrosses =
                    emptySet(),
                markers =
                    emptyMap(),
                mistakes = 0,
                complete = false
            ),
        stopAfterFirstStep:
            Boolean = false
    ): BeeGeckoDifficultyReport {
        val geckos =
            snapshot.confirmedGeckos
                .toMutableSet()

        val bees =
            snapshot.confirmedBees
                .toMutableSet()

        val crosses =
            snapshot.manualCrosses
                .toSet()

        val steps =
            mutableListOf<
                BeeGeckoSolveStep
                >()

        var directSteps = 0
        var adjacencySteps = 0
        var projectionSteps = 0
        var maxPairOptions = 0

        fun isComplete():
            Boolean =
            BeeGeckoRules
                .validateComplete(
                    puzzle,
                    geckos,
                    bees
                )

        var guard = 0

        while (
            !isComplete() &&
            guard < 256
        ) {
            guard += 1

            val options =
                buildOptions(
                    puzzle =
                        puzzle,
                    confirmedGeckos =
                        geckos,
                    confirmedBees =
                        bees,
                    crosses =
                        crosses
                )

            if (
                options.values
                    .any {
                        it.isEmpty()
                    }
            ) {
                break
            }

            maxPairOptions =
                maxOf(
                    maxPairOptions,
                    options.values
                        .maxOfOrNull {
                            it.size
                        }
                        ?: 0
                )

            var step:
                BeeGeckoSolveStep? =
                null

            for (
                region in
                0 until puzzle.regionCount
            ) {
                val regionOptions =
                    options[region]
                        ?: emptyList()

                if (regionOptions.isEmpty()) {
                    continue
                }

                val knownG =
                    geckos
                        .firstOrNull {
                            puzzle.regionAt(it) ==
                                region
                        }

                val knownB =
                    bees
                        .firstOrNull {
                            puzzle.regionAt(it) ==
                                region
                        }

                val geckoChoices =
                    regionOptions
                        .map {
                            it.gecko
                        }
                        .toSet()

                val beeChoices =
                    regionOptions
                        .map {
                            it.bee
                        }
                        .toSet()

                val regionCells =
                    puzzle.cellsInRegion(
                        region
                    )

                val forcedG =
                    if (
                        knownG == null &&
                        geckoChoices.size ==
                            1
                    ) {
                        geckoChoices.single()
                    } else {
                        null
                    }

                val forcedB =
                    if (
                        knownB == null &&
                        beeChoices.size ==
                            1
                    ) {
                        beeChoices.single()
                    } else {
                        null
                    }

                if (
                    forcedG != null ||
                    forcedB != null
                ) {
                    val typeWord =
                        when {
                            forcedG != null &&
                                forcedB != null ->
                                "le Gecko et l'Abeille"

                            forcedG != null ->
                                "le Gecko"

                            else ->
                                "l'Abeille"
                        }

                    val technique =
                        when {
                            regionOptions.size ==
                                1 ->
                                BeeGeckoTechnique
                                    .ADJACENCY_SINGLE

                            geckoChoices.size ==
                                1 ||
                                beeChoices.size ==
                                    1 ->
                                BeeGeckoTechnique
                                    .AXIS_ELIMINATION

                            else ->
                                BeeGeckoTechnique
                                    .ZONE_SINGLE
                        }

                    val candidateCells =
                        (
                            geckoChoices +
                                beeChoices
                            )

                    val eliminated =
                        regionCells -
                            candidateCells -
                            geckos -
                            bees

                    val axisDetail =
                        forcedG
                            ?.let {
                                axisReason(
                                    puzzle,
                                    it,
                                    geckos
                                )
                            }
                            ?: forcedB
                                ?.let {
                                    axisReason(
                                        puzzle,
                                        it,
                                        bees
                                    )
                                }

                    val explanation =
                        buildString {
                            append(
                                "J'analyse la zone "
                            )
                            append(
                                region + 1
                            )
                            append(
                                ". Elle doit contenir exactement un Gecko et une Abeille, et ces deux pièces doivent se toucher. "
                            )
                            append(
                                "Après les exclusions de zone, d'axes et de voisinage, il reste "
                            )
                            append(
                                regionOptions.size
                            )
                            append(
                                if (
                                    regionOptions.size ==
                                        1
                                ) {
                                    " couple local possible. "
                                } else {
                                    " couples locaux possibles. "
                                }
                            )

                            if (
                                axisDetail != null
                            ) {
                                append(
                                    axisDetail
                                )
                                append(" ")
                            }

                            append(
                                "Il ne reste qu'une position possible pour "
                            )
                            append(
                                typeWord
                            )
                            append(
                                ". La conclusion certaine est indiquée en vert."
                            )
                        }

                    step =
                        BeeGeckoSolveStep(
                            technique =
                                technique,
                            region =
                                region,
                            gecko =
                                forcedG,
                            bee =
                                forcedB,
                            analyzed =
                                regionCells,
                            candidates =
                                candidateCells,
                            eliminated =
                                eliminated,
                            explanation =
                                explanation
                        )

                    break
                }
            }

            if (step == null) {
                val projection =
                    findProjection(
                        puzzle =
                            puzzle,
                        geckos =
                            geckos,
                        bees =
                            bees,
                        crosses =
                            crosses,
                        options =
                            options
                    )

                if (projection != null) {
                    projectionSteps += 1
                    step = projection
                }
            }

            if (step == null) {
                break
            }

            if (
                step.technique ==
                    BeeGeckoTechnique
                        .ADJACENCY_SINGLE
            ) {
                adjacencySteps += 1
            } else if (
                step.technique !=
                    BeeGeckoTechnique
                        .PROJECTION
            ) {
                directSteps += 1
            }

            step.gecko
                ?.let {
                    geckos.add(it)
                }

            step.bee
                ?.let {
                    bees.add(it)
                }

            steps.add(step)

            if (stopAfterFirstStep) {
                break
            }
        }

        val solved =
            BeeGeckoRules
                .validateComplete(
                    puzzle,
                    geckos,
                    bees
                )

        val rated =
            rateDifficulty(
                solved =
                    solved,
                directSteps =
                    directSteps,
                adjacencySteps =
                    adjacencySteps,
                projectionSteps =
                    projectionSteps,
                maxPairOptions =
                    maxPairOptions,
                givens =
                    puzzle.givenGeckos
                        .size +
                        puzzle.givenBees
                            .size
            )

        return BeeGeckoDifficultyReport(
            ratedDifficulty =
                rated,
            logicallySolvable =
                solved,
            directSteps =
                directSteps,
            adjacencySteps =
                adjacencySteps,
            projectionSteps =
                projectionSteps,
            maxPairOptions =
                maxPairOptions,
            steps =
                steps
        )
    }

    fun countSolutions(
        puzzle: BeeGeckoPuzzle,
        confirmedGeckos:
            Set<HexCoord> =
            puzzle.givenGeckos,
        confirmedBees:
            Set<HexCoord> =
            puzzle.givenBees,
        crosses:
            Set<HexCoord> =
            emptySet(),
        limit: Int = 2
    ): Int {
        val options =
            buildOptions(
                puzzle =
                    puzzle,
                confirmedGeckos =
                    confirmedGeckos,
                confirmedBees =
                    confirmedBees,
                crosses =
                    crosses,
                includeAxisAgainstConfirmedOnly =
                    false
            )

        if (
            options.values
                .any {
                    it.isEmpty()
                }
        ) {
            return 0
        }

        val orderedRegions =
            options.keys
                .sortedBy {
                    options[it]
                        ?.size
                        ?: Int.MAX_VALUE
                }

        val usedGQ =
            mutableSetOf<Int>()
        val usedGR =
            mutableSetOf<Int>()
        val usedGS =
            mutableSetOf<Int>()
        val usedBQ =
            mutableSetOf<Int>()
        val usedBR =
            mutableSetOf<Int>()
        val usedBS =
            mutableSetOf<Int>()

        val chosenGeckos =
            linkedSetOf<HexCoord>()

        val chosenBees =
            linkedSetOf<HexCoord>()

        var count = 0

        fun search(
            index: Int
        ) {
            if (count >= limit) {
                return
            }

            if (
                index ==
                    orderedRegions.size
            ) {
                count += 1
                return
            }

            val region =
                orderedRegions[index]

            val regionOptions =
                options[region]
                    ?: return

            for (option in regionOptions) {
                val g =
                    option.gecko
                val b =
                    option.bee

                if (
                    g.q in usedGQ ||
                    g.r in usedGR ||
                    g.s in usedGS ||
                    b.q in usedBQ ||
                    b.r in usedBR ||
                    b.s in usedBS ||
                    chosenBees.any {
                        otherBee ->
                        BeeGeckoRules
                            .areNeighbors(
                                g,
                                otherBee
                            )
                    } ||
                    chosenGeckos.any {
                        otherGecko ->
                        BeeGeckoRules
                            .areNeighbors(
                                otherGecko,
                                b
                            )
                    }
                ) {
                    continue
                }

                usedGQ.add(g.q)
                usedGR.add(g.r)
                usedGS.add(g.s)
                usedBQ.add(b.q)
                usedBR.add(b.r)
                usedBS.add(b.s)
                chosenGeckos.add(g)
                chosenBees.add(b)

                search(
                    index + 1
                )

                chosenGeckos.remove(g)
                chosenBees.remove(b)
                usedGQ.remove(g.q)
                usedGR.remove(g.r)
                usedGS.remove(g.s)
                usedBQ.remove(b.q)
                usedBR.remove(b.r)
                usedBS.remove(b.s)

                if (count >= limit) {
                    return
                }
            }
        }

        search(0)
        return count
    }

    private fun buildOptions(
        puzzle: BeeGeckoPuzzle,
        confirmedGeckos:
            Set<HexCoord>,
        confirmedBees:
            Set<HexCoord>,
        crosses:
            Set<HexCoord>,
        includeAxisAgainstConfirmedOnly:
            Boolean = true
    ): Map<
        Int,
        List<BeeGeckoPairOption>
        > {
        val result =
            linkedMapOf<
                Int,
                List<BeeGeckoPairOption>
                >()

        for (
            region in
            0 until puzzle.regionCount
        ) {
            val cells =
                puzzle.cellsInRegion(
                    region
                )

            val knownG =
                confirmedGeckos
                    .firstOrNull {
                        puzzle.regionAt(it) ==
                            region
                    }

            val knownB =
                confirmedBees
                    .firstOrNull {
                        puzzle.regionAt(it) ==
                            region
                    }

            val geckoCandidates =
                cells.filter {
                    cell ->
                    cell !in crosses &&
                        cell !in
                            confirmedBees &&
                        (
                            knownG == null ||
                                cell ==
                                    knownG
                            ) &&
                        !axisBlocked(
                            cell =
                                cell,
                            confirmed =
                                confirmedGeckos,
                            sameCellAllowed =
                                knownG
                        ) &&
                        confirmedBees.none {
                            other ->
                            puzzle.regionAt(
                                other
                            ) != region &&
                                BeeGeckoRules
                                    .areNeighbors(
                                        cell,
                                        other
                                    )
                        }
                }

            val beeCandidates =
                cells.filter {
                    cell ->
                    cell !in crosses &&
                        cell !in
                            confirmedGeckos &&
                        (
                            knownB == null ||
                                cell ==
                                    knownB
                            ) &&
                        !axisBlocked(
                            cell =
                                cell,
                            confirmed =
                                confirmedBees,
                            sameCellAllowed =
                                knownB
                        ) &&
                        confirmedGeckos.none {
                            other ->
                            puzzle.regionAt(
                                other
                            ) != region &&
                                BeeGeckoRules
                                    .areNeighbors(
                                        other,
                                        cell
                                    )
                        }
                }

            val options =
                mutableListOf<
                    BeeGeckoPairOption
                    >()

            for (g in geckoCandidates) {
                for (b in beeCandidates) {
                    if (
                        g != b &&
                        BeeGeckoRules
                            .areNeighbors(
                                g,
                                b
                            )
                    ) {
                        options.add(
                            BeeGeckoPairOption(
                                gecko = g,
                                bee = b
                            )
                        )
                    }
                }
            }

            result[region] =
                options
        }

        if (
            includeAxisAgainstConfirmedOnly
        ) {
            return result
        }

        return result
    }

    private fun axisBlocked(
        cell: HexCoord,
        confirmed: Set<HexCoord>,
        sameCellAllowed:
            HexCoord?
    ): Boolean =
        confirmed.any {
            other ->
            other !=
                sameCellAllowed &&
                BeeGeckoRules
                    .sameAxis(
                        cell,
                        other
                    )
        }

    private fun axisReason(
        puzzle: BeeGeckoPuzzle,
        cell: HexCoord,
        confirmed:
            Set<HexCoord>
    ): String? {
        val blocker =
            confirmed
                .firstOrNull {
                    other ->
                    other != cell &&
                        BeeGeckoRules
                            .sameAxis(
                                cell,
                                other
                            )
                }
                ?: return null

        return "Une autre position est exclue parce que " +
            BeeGeckoRules
                .axisLabel(
                    cell,
                    blocker
                ) +
            " possède déjà cette famille de pièce."
    }

    private fun findProjection(
        puzzle: BeeGeckoPuzzle,
        geckos: Set<HexCoord>,
        bees: Set<HexCoord>,
        crosses: Set<HexCoord>,
        options:
            Map<
                Int,
                List<BeeGeckoPairOption>
                >
    ): BeeGeckoSolveStep? {
        val candidateRegion =
            options
                .entries
                .filter {
                    it.value.size > 1
                }
                .minByOrNull {
                    it.value.size
                }
                ?: return null

        val viable =
            candidateRegion.value
                .filter {
                    option ->
                    countSolutions(
                        puzzle =
                            puzzle,
                        confirmedGeckos =
                            geckos +
                                option.gecko,
                        confirmedBees =
                            bees +
                                option.bee,
                        crosses =
                            crosses,
                        limit = 1
                    ) >
                        0
                }

        if (viable.size != 1) {
            return null
        }

        val winner =
            viable.single()

        val allCells =
            candidateRegion.value
                .flatMap {
                    listOf(
                        it.gecko,
                        it.bee
                    )
                }
                .toSet()

        val green =
            setOf(
                winner.gecko,
                winner.bee
            )

        return BeeGeckoSolveStep(
            technique =
                BeeGeckoTechnique
                    .PROJECTION,
            region =
                candidateRegion.key,
            gecko =
                if (
                    winner.gecko !in
                        geckos
                ) {
                    winner.gecko
                } else {
                    null
                },
            bee =
                if (
                    winner.bee !in
                        bees
                ) {
                    winner.bee
                } else {
                    null
                },
            analyzed =
                puzzle.cellsInRegion(
                    candidateRegion.key
                ),
            candidates =
                allCells,
            eliminated =
                allCells -
                    green,
            explanation =
                "Je teste les possibilités de la zone " +
                    (candidateRegion.key + 1) +
                    ". Les hypothèses orange conduisent chacune à une contradiction de zone, d'axe ou de voisinage. Une seule branche reste cohérente : le Gecko et l'Abeille verts, voisins l'un de l'autre."
        )
    }

    private fun rateDifficulty(
        solved: Boolean,
        directSteps: Int,
        adjacencySteps: Int,
        projectionSteps: Int,
        maxPairOptions: Int,
        givens: Int
    ): GameDifficulty {
        if (!solved) {
            return GameDifficulty
                .INFERNAL
        }

        val score =
            directSteps +
                adjacencySteps *
                    3 +
                projectionSteps *
                    12 +
                maxOf(
                    0,
                    maxPairOptions - 1
                ) *
                    2 +
                maxOf(
                    0,
                    8 - givens
                )

        return when {
            score <= 8 ->
                GameDifficulty
                    .DISCOVERY

            score <= 14 ->
                GameDifficulty
                    .EASY

            score <= 22 ->
                GameDifficulty
                    .THINKING

            score <= 32 ->
                GameDifficulty
                    .HARD

            score <= 44 ->
                GameDifficulty
                    .EXPERT

            score <= 58 ->
                GameDifficulty
                    .DEMENTIAL

            score <= 74 ->
                GameDifficulty
                    .MISSION_IMPOSSIBLE

            else ->
                GameDifficulty
                    .INFERNAL
        }
    }
}

data class BeeGeckoDifficultyProfile(
    val radius: Int,
    val pairCount: Int,
    val targetGivenCount: Int,
    val attemptsPerBatch: Int
)

object BeeGeckoGenerator {
    fun profile(
        difficulty:
            GameDifficulty
    ): BeeGeckoDifficultyProfile =
        when (difficulty) {
            GameDifficulty.DISCOVERY ->
                BeeGeckoDifficultyProfile(
                    radius = 1,
                    pairCount = 2,
                    targetGivenCount = 3,
                    attemptsPerBatch = 70
                )

            GameDifficulty.EASY ->
                BeeGeckoDifficultyProfile(
                    radius = 2,
                    pairCount = 3,
                    targetGivenCount = 4,
                    attemptsPerBatch = 90
                )

            GameDifficulty.THINKING ->
                BeeGeckoDifficultyProfile(
                    radius = 2,
                    pairCount = 4,
                    targetGivenCount = 3,
                    attemptsPerBatch = 120
                )

            GameDifficulty.HARD ->
                BeeGeckoDifficultyProfile(
                    radius = 3,
                    pairCount = 5,
                    targetGivenCount = 4,
                    attemptsPerBatch = 140
                )

            GameDifficulty.EXPERT ->
                BeeGeckoDifficultyProfile(
                    radius = 3,
                    pairCount = 6,
                    targetGivenCount = 3,
                    attemptsPerBatch = 160
                )

            GameDifficulty.DEMENTIAL ->
                BeeGeckoDifficultyProfile(
                    radius = 4,
                    pairCount = 7,
                    targetGivenCount = 3,
                    attemptsPerBatch = 190
                )

            GameDifficulty.MISSION_IMPOSSIBLE ->
                BeeGeckoDifficultyProfile(
                    radius = 4,
                    pairCount = 8,
                    targetGivenCount = 2,
                    attemptsPerBatch = 220
                )

            GameDifficulty.INFERNAL ->
                BeeGeckoDifficultyProfile(
                    radius = 4,
                    pairCount = 9,
                    targetGivenCount = 1,
                    attemptsPerBatch = 260
                )
        }

    fun generate(
        requested:
            GameDifficulty,
        seed: Long =
            System.nanoTime()
    ): BeeGeckoPuzzle {
        val profile =
            profile(requested)

        val random =
            Random(seed)

        var best:
            BeeGeckoPuzzle? = null

        var bestDistance =
            Int.MAX_VALUE

        repeat(
            profile.attemptsPerBatch
        ) {
            attempt ->

            val solution =
                generateSolution(
                    profile =
                        profile,
                    random =
                        random
                )
                    ?: return@repeat

            val regions =
                growRegions(
                    radius =
                        profile.radius,
                    pairs =
                        solution,
                    random =
                        random
                )
                    ?: return@repeat

            val allGeckos =
                solution
                    .mapTo(
                        linkedSetOf()
                    ) {
                        it.gecko
                    }

            val allBees =
                solution
                    .mapTo(
                        linkedSetOf()
                    ) {
                        it.bee
                    }

            val full =
                BeeGeckoPuzzle(
                    id =
                        "bee-classic-" +
                            requested.name +
                            "-" +
                            seed +
                            "-" +
                            attempt,
                    radius =
                        profile.radius,
                    regions =
                        regions,
                    solutionGeckos =
                        allGeckos,
                    solutionBees =
                        allBees,
                    solutionPairs =
                        solution,
                    givenGeckos =
                        allGeckos,
                    givenBees =
                        allBees,
                    difficulty =
                        requested,
                    seed =
                        seed
                )

            val tuned =
                tuneGivens(
                    full =
                        full,
                    requested =
                        requested,
                    targetGivenCount =
                        profile
                            .targetGivenCount,
                    random =
                        random
                )

            val report =
                BeeGeckoSolver
                    .analyze(
                        tuned
                    )

            if (
                !report.logicallySolvable ||
                BeeGeckoSolver
                    .countSolutions(
                        tuned,
                        limit = 2
                    ) != 1
            ) {
                return@repeat
            }

            val rated =
                tuned.copy(
                    difficulty =
                        report
                            .ratedDifficulty,
                    solverTrace =
                        report.steps
                )

            if (
                report.ratedDifficulty ==
                    requested
            ) {
                return rated
            }

            val distance =
                abs(
                    report
                        .ratedDifficulty
                        .ordinal -
                        requested.ordinal
                )

            if (
                distance <
                    bestDistance
            ) {
                bestDistance =
                    distance
                best = rated
            }
        }

        return best
            ?: safePuzzle(
                requested =
                    requested,
                seed =
                    seed
            )
    }

    fun generateExact(
        requested:
            GameDifficulty,
        seed: Long =
            System.nanoTime(),
        shouldCancel:
            () -> Boolean = {
                false
            },
        onBatchCompleted:
            ((Int) -> Unit)? =
            null
    ): BeeGeckoPuzzle? {
        var batch = 0

        while (!shouldCancel()) {
            val candidate =
                generate(
                    requested =
                        requested,
                    seed =
                        seed +
                            batch.toLong() *
                                65_537L
                )

            if (shouldCancel()) {
                return null
            }

            val report =
                BeeGeckoSolver
                    .analyze(
                        candidate
                    )

            if (
                report.logicallySolvable &&
                report.ratedDifficulty ==
                    requested &&
                BeeGeckoSolver
                    .countSolutions(
                        candidate,
                        limit = 2
                    ) == 1
            ) {
                return candidate.copy(
                    difficulty =
                        requested,
                    solverTrace =
                        report.steps
                )
            }

            batch += 1
            onBatchCompleted
                ?.invoke(batch)
        }

        return null
    }

    private fun tuneGivens(
        full: BeeGeckoPuzzle,
        requested:
            GameDifficulty,
        targetGivenCount: Int,
        random: Random
    ): BeeGeckoPuzzle {
        val all =
            (
                full.solutionGeckos
                    .map {
                        it to
                            BeeGeckoPiece
                                .GECKO
                    } +
                    full.solutionBees
                        .map {
                            it to
                                BeeGeckoPiece
                                    .BEE
                        }
                )
                .shuffled(random)

        var givenG =
            full.solutionGeckos
                .toMutableSet()

        var givenB =
            full.solutionBees
                .toMutableSet()

        var best =
            full

        var bestDistance =
            Int.MAX_VALUE

        for (
            (cell, piece) in
            all
        ) {
            if (
                givenG.size +
                    givenB.size <=
                    targetGivenCount
            ) {
                break
            }

            if (
                piece ==
                    BeeGeckoPiece.GECKO
            ) {
                givenG.remove(cell)
            } else {
                givenB.remove(cell)
            }

            val probe =
                full.copy(
                    givenGeckos =
                        givenG.toSet(),
                    givenBees =
                        givenB.toSet()
                )

            if (
                BeeGeckoSolver
                    .countSolutions(
                        probe,
                        limit = 2
                    ) != 1
            ) {
                if (
                    piece ==
                        BeeGeckoPiece.GECKO
                ) {
                    givenG.add(cell)
                } else {
                    givenB.add(cell)
                }
                continue
            }

            val report =
                BeeGeckoSolver
                    .analyze(
                        probe
                    )

            if (!report.logicallySolvable) {
                if (
                    piece ==
                        BeeGeckoPiece.GECKO
                ) {
                    givenG.add(cell)
                } else {
                    givenB.add(cell)
                }
                continue
            }

            val distance =
                abs(
                    report
                        .ratedDifficulty
                        .ordinal -
                        requested.ordinal
                )

            if (
                distance <
                    bestDistance
            ) {
                bestDistance =
                    distance
                best =
                    probe.copy(
                        difficulty =
                            report
                                .ratedDifficulty,
                        solverTrace =
                            report.steps
                    )
            }

            if (
                report.ratedDifficulty ==
                    requested
            ) {
                return probe.copy(
                    difficulty =
                        requested,
                    solverTrace =
                        report.steps
                )
            }
        }

        return best
    }

    private fun generateSolution(
        profile:
            BeeGeckoDifficultyProfile,
        random: Random
    ): List<BeeGeckoPair>? {
        val cells =
            boardCells(
                profile.radius
            )

        repeat(180) {
            val geckos =
                chooseNonAttacking(
                    cells =
                        cells,
                    count =
                        profile.pairCount,
                    random =
                        random
                )
                    ?: return@repeat

            val order =
                geckos.indices
                    .shuffled(random)

            val chosenBees =
                arrayOfNulls<
                    HexCoord
                    >(
                    geckos.size
                )

            val usedBeeCells =
                linkedSetOf<
                    HexCoord
                    >()

            val usedQ =
                linkedSetOf<Int>()
            val usedR =
                linkedSetOf<Int>()
            val usedS =
                linkedSetOf<Int>()

            fun placeBee(
                index: Int
            ): Boolean {
                if (
                    index ==
                        order.size
                ) {
                    return true
                }

                val geckoIndex =
                    order[index]

                val gecko =
                    geckos[
                        geckoIndex
                    ]

                val choices =
                    gecko.neighbors()
                        .filter {
                            bee ->
                            bee in cells &&
                                bee !in geckos &&
                                bee !in
                                    usedBeeCells &&
                                geckos.count {
                                    otherGecko ->
                                    BeeGeckoRules
                                        .areNeighbors(
                                            otherGecko,
                                            bee
                                        )
                                } == 1
                        }
                        .shuffled(random)

                for (bee in choices) {
                    if (
                        bee.q in usedQ ||
                        bee.r in usedR ||
                        bee.s in usedS
                    ) {
                        continue
                    }

                    usedBeeCells.add(
                        bee
                    )
                    usedQ.add(bee.q)
                    usedR.add(bee.r)
                    usedS.add(bee.s)
                    chosenBees[
                        geckoIndex
                    ] =
                        bee

                    if (
                        placeBee(
                            index + 1
                        )
                    ) {
                        return true
                    }

                    chosenBees[
                        geckoIndex
                    ] =
                        null
                    usedBeeCells.remove(
                        bee
                    )
                    usedQ.remove(bee.q)
                    usedR.remove(bee.r)
                    usedS.remove(bee.s)
                }

                return false
            }

            if (!placeBee(0)) {
                return@repeat
            }

            return geckos
                .mapIndexed {
                    region,
                    gecko ->

                    BeeGeckoPair(
                        gecko =
                            gecko,
                        bee =
                            requireNotNull(
                                chosenBees[
                                    region
                                ]
                            ),
                        region =
                            region
                    )
                }
        }

        return null
    }

    private fun chooseNonAttacking(
        cells: Set<HexCoord>,
        count: Int,
        random: Random
    ): List<HexCoord>? {
        val shuffled =
            cells
                .toList()
                .shuffled(random)

        val chosen =
            mutableListOf<
                HexCoord
                >()

        val usedQ =
            linkedSetOf<Int>()
        val usedR =
            linkedSetOf<Int>()
        val usedS =
            linkedSetOf<Int>()

        fun search(
            start: Int
        ): Boolean {
            if (
                chosen.size ==
                    count
            ) {
                return true
            }

            if (
                shuffled.size -
                    start <
                    count -
                        chosen.size
            ) {
                return false
            }

            for (
                index in
                start until
                    shuffled.size
            ) {
                val cell =
                    shuffled[index]

                if (
                    cell.q in usedQ ||
                    cell.r in usedR ||
                    cell.s in usedS
                ) {
                    continue
                }

                chosen.add(cell)
                usedQ.add(cell.q)
                usedR.add(cell.r)
                usedS.add(cell.s)

                if (
                    search(
                        index + 1
                    )
                ) {
                    return true
                }

                chosen.removeAt(
                    chosen.lastIndex
                )
                usedQ.remove(cell.q)
                usedR.remove(cell.r)
                usedS.remove(cell.s)
            }

            return false
        }

        return if (search(0)) {
            chosen.toList()
        } else {
            null
        }
    }

    private fun growRegions(
        radius: Int,
        pairs:
            List<BeeGeckoPair>,
        random: Random
    ): Map<HexCoord, Int>? {
        val all =
            boardCells(radius)
                .toMutableSet()

        val regions =
            linkedMapOf<
                HexCoord,
                Int
                >()

        for (pair in pairs) {
            if (
                regions.containsKey(
                    pair.gecko
                ) ||
                regions.containsKey(
                    pair.bee
                )
            ) {
                return null
            }

            regions[
                pair.gecko
            ] =
                pair.region

            regions[
                pair.bee
            ] =
                pair.region

            all.remove(
                pair.gecko
            )
            all.remove(
                pair.bee
            )
        }

        var guard = 0

        while (
            all.isNotEmpty() &&
            guard < 4096
        ) {
            guard += 1

            val frontier =
                all
                    .mapNotNull {
                        cell ->

                        val touching =
                            cell.neighbors()
                                .mapNotNull {
                                    regions[it]
                                }
                                .distinct()

                        if (
                            touching.isEmpty()
                        ) {
                            null
                        } else {
                            cell to
                                touching
                        }
                    }

            if (frontier.isEmpty()) {
                return null
            }

            val pick =
                frontier[
                    random.nextInt(
                        frontier.size
                    )
                ]

            val regionChoices =
                pick.second

            regions[
                pick.first
            ] =
                regionChoices[
                    random.nextInt(
                        regionChoices.size
                    )
                ]

            all.remove(
                pick.first
            )
        }

        return if (all.isEmpty()) {
            regions
        } else {
            null
        }
    }

    private fun boardCells(
        radius: Int
    ): Set<HexCoord> =
        buildSet {
            for (
                q in
                -radius..radius
            ) {
                for (
                    r in
                    -radius..radius
                ) {
                    val cell =
                        HexCoord(
                            q,
                            r
                        )

                    if (
                        abs(cell.s) <=
                            radius
                    ) {
                        add(cell)
                    }
                }
            }
        }

    private fun safePuzzle(
        requested:
            GameDifficulty,
        seed: Long
    ): BeeGeckoPuzzle {
        val easyProfile =
            BeeGeckoDifficultyProfile(
                radius = 1,
                pairCount = 2,
                targetGivenCount = 4,
                attemptsPerBatch = 100
            )

        val random =
            Random(
                seed xor
                    0xBEE6ECL
            )

        repeat(500) {
            val pairs =
                generateSolution(
                    easyProfile,
                    random
                )
                    ?: return@repeat

            val regions =
                growRegions(
                    radius =
                        easyProfile.radius,
                    pairs =
                        pairs,
                    random =
                        random
                )
                    ?: return@repeat

            val geckos =
                pairs.mapTo(
                    linkedSetOf()
                ) {
                    it.gecko
                }

            val bees =
                pairs.mapTo(
                    linkedSetOf()
                ) {
                    it.bee
                }

            return BeeGeckoPuzzle(
                id =
                    "bee-safe-" +
                        seed,
                radius =
                    easyProfile.radius,
                regions =
                    regions,
                solutionGeckos =
                    geckos,
                solutionBees =
                    bees,
                solutionPairs =
                    pairs,
                givenGeckos =
                    geckos,
                givenBees =
                    bees,
                difficulty =
                    GameDifficulty
                        .DISCOVERY,
                seed =
                    seed
            )
        }

        throw IllegalStateException(
            "Impossible de générer une grille Abeilles & Geckos sûre."
        )
    }
}

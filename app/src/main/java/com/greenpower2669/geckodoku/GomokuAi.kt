package com.greenpower2669.geckodoku

import kotlin.math.abs
import kotlin.math.max

object GomokuAi {
    private const val WIN_SCORE =
        1_000_000_000

    private val directions =
        arrayOf(
            intArrayOf(0, 1),
            intArrayOf(1, 0),
            intArrayOf(1, 1),
            intArrayOf(1, -1)
        )

    fun chooseMoveFor(
        snapshot: GomokuSnapshot,
        difficulty: GameDifficulty,
        player: GomokuPlayer
    ): GomokuAiDecision? {
        if (
            snapshot.gameOver ||
            snapshot.currentPlayer !=
                player
        ) {
            return null
        }

        if (
            player ==
                GomokuPlayer
                    .PROFESSOR
        ) {
            return chooseMove(
                snapshot,
                difficulty
            )
        }

        val swapped =
            snapshot.copy(
                stones =
                    snapshot.stones
                        .mapValues {
                            (_, value) ->
                            value.other()
                        },
                currentPlayer =
                    GomokuPlayer
                        .PROFESSOR,
                winner =
                    snapshot.winner
                        ?.other()
            )

        val decision =
            chooseMove(
                swapped,
                difficulty
            )
                ?: return null

        return decision
    }

    fun chooseMove(
        snapshot: GomokuSnapshot,
        difficulty: GameDifficulty
    ): GomokuAiDecision? {
        if (
            snapshot.gameOver ||
            snapshot.currentPlayer !=
                GomokuPlayer
                    .PROFESSOR
        ) {
            return null
        }

        val profile =
            GomokuDifficultyPolicy
                .profile(difficulty)

        val candidates =
            candidateMoves(
                snapshot,
                profile
                    .neighborhoodRadius
            )

        if (candidates.isEmpty()) {
            return null
        }

        val immediateWins =
            candidates
                .filter {
                    GomokuWinDetector
                        .wouldWin(
                            snapshot,
                            it,
                            GomokuPlayer
                                .PROFESSOR
                        )
                }
                .sortedWith(
                    cellComparator()
                )

        val forcedBlocks =
            candidates
                .filter {
                    GomokuWinDetector
                        .wouldWin(
                            snapshot,
                            it,
                            GomokuPlayer
                                .PLAYER
                        )
                }
                .sortedWith(
                    compareByDescending<Cell> {
                        staticMoveScore(
                            snapshot,
                            it,
                            GomokuPlayer
                                .PROFESSOR,
                            profile
                        )
                    }.then(
                        cellComparator()
                    )
                )

        val seesWin =
            seesTactic(
                snapshot =
                    snapshot,
                profile =
                    profile,
                salt = 11
            )

        val seesBlock =
            seesTactic(
                snapshot =
                    snapshot,
                profile =
                    profile,
                salt = 29
            )

        if (
            seesWin &&
            immediateWins
                .isNotEmpty()
        ) {
            val chosen =
                immediateWins.first()

            return decisionFor(
                snapshot =
                    snapshot,
                chosen =
                    chosen,
                score =
                    WIN_SCORE,
                profile =
                    profile,
                forcedKind =
                    GomokuReasonKind
                        .WIN
            )
        }

        if (
            seesBlock &&
            forcedBlocks
                .isNotEmpty()
        ) {
            val chosen =
                forcedBlocks.first()

            return decisionFor(
                snapshot =
                    snapshot,
                chosen =
                    chosen,
                score =
                    WIN_SCORE /
                        2,
                profile =
                    profile,
                forcedKind =
                    GomokuReasonKind
                        .DEFENSE
            )
        }

        val temporarilyUnseen =
            buildSet {
                if (!seesWin) {
                    addAll(
                        immediateWins
                    )
                }

                if (!seesBlock) {
                    addAll(
                        forcedBlocks
                    )
                }
            }

        val scored =
            candidates
                .map {
                    cell ->
                    cell to
                        staticMoveScore(
                            snapshot,
                            cell,
                            GomokuPlayer
                                .PROFESSOR,
                            profile
                        )
                }
                .sortedWith(
                    compareByDescending<
                        Pair<Cell, Int>
                        > {
                        it.second
                    }.thenBy {
                        it.first.row
                    }.thenBy {
                        it.first.col
                    }
                )

        val visibleScored =
            if (
                temporarilyUnseen
                    .isNotEmpty()
            ) {
                val filtered =
                    scored.filter {
                        it.first !in
                            temporarilyUnseen
                    }

                if (filtered.isNotEmpty()) {
                    filtered
                } else {
                    scored
                }
            } else {
                scored
            }

        val ordered =
            visibleScored
                .take(
                    profile.beamWidth
                )

        val evaluated =
            mutableListOf<
                Pair<Cell, Int>
                >()

        for (
            (cell, staticScore) in
            ordered
        ) {
            val next =
                place(
                    snapshot,
                    cell,
                    GomokuPlayer
                        .PROFESSOR
                )

            val future =
                if (
                    profile.searchDepth <=
                        1
                ) {
                    0
                } else {
                    minimax(
                        snapshot =
                            next,
                        player =
                            GomokuPlayer
                                .PLAYER,
                        depth =
                            profile
                                .searchDepth -
                                1,
                        alpha =
                            -WIN_SCORE,
                        beta =
                            WIN_SCORE,
                        profile =
                            profile
                    )
                }

            evaluated.add(
                cell to
                    (
                        staticScore +
                            future
                        )
            )
        }

        val ranked =
            evaluated
                .sortedWith(
                    compareByDescending<
                        Pair<Cell, Int>
                        > {
                        it.second
                    }.thenBy {
                        it.first.row
                    }.thenBy {
                        it.first.col
                    }
                )

        if (ranked.isEmpty()) {
            return null
        }

        val rank =
            deterministicChoiceRank(
                snapshot =
                    snapshot,
                window =
                    profile
                        .choiceWindow,
                available =
                    ranked.size
            )

        val chosen =
            ranked[rank]

        return decisionFor(
            snapshot =
                snapshot,
            chosen =
                chosen.first,
            score =
                chosen.second,
            profile =
                profile,
            forcedKind =
                null
        )
    }

    private fun decisionFor(
        snapshot: GomokuSnapshot,
        chosen: Cell,
        score: Int,
        profile:
            GomokuDifficultyProfile,
        forcedKind:
            GomokuReasonKind?
    ): GomokuAiDecision {
        val reasoning =
            buildReasoning(
                snapshot =
                    snapshot,
                chosen =
                    chosen,
                profile =
                    profile,
                forcedKind =
                    forcedKind
            )

        return GomokuAiDecision(
            cell =
                chosen,
            reason =
                reasoning.steps
                    .joinToString(
                        separator =
                            "\n\n"
                    ),
            score =
                score,
            reasoning =
                reasoning
        )
    }

    private fun buildReasoning(
        snapshot: GomokuSnapshot,
        chosen: Cell,
        profile:
            GomokuDifficultyProfile,
        forcedKind:
            GomokuReasonKind?
    ): GomokuReasoningTrace {
        val own =
            strongestLine(
                snapshot =
                    snapshot,
                cell =
                    chosen,
                player =
                    GomokuPlayer
                        .PROFESSOR
            )

        val opponent =
            strongestLine(
                snapshot =
                    snapshot,
                cell =
                    chosen,
                player =
                    GomokuPlayer
                        .PLAYER
            )

        val threats =
            threatCountAfter(
                snapshot =
                    snapshot,
                cell =
                    chosen,
                player =
                    GomokuPlayer
                        .PROFESSOR
            )

        val kind =
            forcedKind
                ?: when {
                    opponent.length >= 4 &&
                        opponent.length >
                            own.length ->
                        GomokuReasonKind
                            .DEFENSE

                    profile.trapAware &&
                        threats >= 2 ->
                        GomokuReasonKind
                            .TRAP

                    own.length >= 3 ->
                        GomokuReasonKind
                            .ATTACK

                    else ->
                        GomokuReasonKind
                            .POSITIONAL
                }

        val relevant =
            if (
                kind ==
                    GomokuReasonKind
                        .DEFENSE
            ) {
                opponent
            } else {
                own
            }

        val steps =
            mutableListOf<String>()

        when (kind) {
            GomokuReasonKind.WIN -> {
                steps.add(
                    "Je regarde " +
                        relevant.label +
                        ". J'ai déjà " +
                        (relevant.length - 1)
                            .coerceAtLeast(1) +
                        " Gecko(s) alignés autour de " +
                        coord(chosen) +
                        ". Ce coup complète la ligne à cinq : c'est une victoire immédiate."
                )
            }

            GomokuReasonKind.DEFENSE -> {
                steps.add(
                    "Je regarde " +
                        opponent.label +
                        ". Le camp adverse a " +
                        (opponent.length - 1)
                            .coerceAtLeast(1) +
                        " Gecko(s) qui convergent vers " +
                        coord(chosen) +
                        "."
                )

                steps.add(
                    "Si cette intersection reste libre, la ligne adverse peut devenir une menace directe. Le coup conseillé bloque donc " +
                        coord(chosen) +
                        "."
                )

                if (
                    opponent.openEnds
                        .isNotEmpty()
                ) {
                    steps.add(
                        "Les extrémités encore ouvertes de cette ligne sont " +
                            opponent.openEnds
                                .joinToString {
                                    coord(it)
                                } +
                            ". Ce sont elles que je surveille."
                    )
                }
            }

            GomokuReasonKind.TRAP -> {
                steps.add(
                    "Le coup " +
                        coord(chosen) +
                        " agit sur plusieurs directions à la fois."
                )

                steps.add(
                    "Après ce coup, le camp conseillé crée " +
                        threats +
                        " menaces actives. Si l'adversaire répond à une seule, l'autre peut rester ouverte."
                )
            }

            GomokuReasonKind.ATTACK -> {
                steps.add(
                    "Ce coup prolonge " +
                        own.label +
                        " en " +
                        coord(chosen) +
                        ". Cette ligne atteindra " +
                        own.length +
                        " Gecko(s) avec " +
                        own.openEnds.size +
                        " extrémité(s) encore ouverte(s)."
                )
            }

            GomokuReasonKind.POSITIONAL -> {
                steps.add(
                    "Il n'y a pas encore de combinaison forcée. " +
                        coord(chosen) +
                        " garde plusieurs directions actives sans fermer la construction du camp conseillé."
                )
            }
        }

        val projection =
            projectionSequence(
                snapshot =
                    snapshot,
                first =
                    chosen,
                profile =
                    profile
            )

        projection
            .forEachIndexed {
                index,
                cell ->

                val actor =
                    if (
                        index % 2 ==
                            0
                    ) {
                        "Réponse adverse plausible"
                    } else {
                        "Suite prévue du camp conseillé"
                    }

                steps.add(
                    actor +
                        " : " +
                        coord(cell) +
                        "."
                )
            }

        return GomokuReasoningTrace(
            kind =
                kind,
            focusCell =
                chosen,
            lineCells =
                relevant.cells,
            threatCells =
                relevant.openEnds,
            projectedCells =
                projection,
            steps =
                steps
        )
    }

    private fun projectionSequence(
        snapshot: GomokuSnapshot,
        first: Cell,
        profile:
            GomokuDifficultyProfile
    ): List<Cell> {
        val remaining =
            (
                profile
                    .explanationHorizon -
                    1
                )
                .coerceAtLeast(0)

        if (remaining == 0) {
            return emptyList()
        }

        var state =
            place(
                snapshot,
                first,
                GomokuPlayer
                    .PROFESSOR
            )

        var player =
            GomokuPlayer.PLAYER

        val result =
            mutableListOf<Cell>()

        repeat(remaining) {
            val next =
                candidateMoves(
                    state,
                    profile
                        .neighborhoodRadius
                )
                    .maxWithOrNull(
                        compareBy<Cell> {
                            staticMoveScore(
                                state,
                                it,
                                player,
                                profile
                            )
                        }.thenByDescending {
                            -it.row
                        }.thenByDescending {
                            -it.col
                        }
                    )
                    ?: return@repeat

            result.add(next)

            state =
                place(
                    state,
                    next,
                    player
                )

            player =
                player.other()
        }

        return result
    }

    private fun seesTactic(
        snapshot: GomokuSnapshot,
        profile:
            GomokuDifficultyProfile,
        salt: Int
    ): Boolean {
        if (
            profile
                .tacticalReliabilityPercent >=
                100
        ) {
            return true
        }

        val signature =
            snapshot.stones
                .entries
                .fold(
                    snapshot.moveCount *
                        37 +
                        salt *
                            17
                ) {
                    acc,
                    entry ->

                    acc +
                        entry.key.row *
                            11 +
                        entry.key.col *
                            7 +
                        if (
                            entry.value ==
                                GomokuPlayer
                                    .PROFESSOR
                        ) {
                            3
                        } else {
                            5
                        }
                }

        val roll =
            Math.floorMod(
                signature,
                100
            )

        return roll <
            profile
                .tacticalReliabilityPercent
    }

    private fun deterministicChoiceRank(
        snapshot: GomokuSnapshot,
        window: Int,
        available: Int
    ): Int {
        val effective =
            minOf(
                window
                    .coerceAtLeast(1),
                available
            )

        if (effective <= 1) {
            return 0
        }

        val signature =
            snapshot.stones
                .entries
                .fold(
                    snapshot.moveCount *
                        19 +
                        7
                ) {
                    acc,
                    entry ->

                    acc +
                        entry.key.row *
                            5 +
                        entry.key.col *
                            13
                }

        return Math.floorMod(
            signature,
            effective
        )
    }

    private fun minimax(
        snapshot: GomokuSnapshot,
        player: GomokuPlayer,
        depth: Int,
        alpha: Int,
        beta: Int,
        profile:
            GomokuDifficultyProfile
    ): Int {
        if (depth <= 0) {
            return evaluate(
                snapshot,
                profile
            )
        }

        val candidates =
            candidateMoves(
                snapshot,
                profile
                    .neighborhoodRadius
            )
                .map {
                    it to
                        staticMoveScore(
                            snapshot,
                            it,
                            player,
                            profile
                        )
                }
                .sortedWith(
                    compareByDescending<
                        Pair<Cell, Int>
                        > {
                        it.second
                    }.thenBy {
                        it.first.row
                    }.thenBy {
                        it.first.col
                    }
                )
                .take(
                    max(
                        3,
                        profile.beamWidth -
                            (
                                profile.searchDepth -
                                    depth
                                )
                    )
                )

        if (candidates.isEmpty()) {
            return evaluate(
                snapshot,
                profile
            )
        }

        var a = alpha
        var b = beta

        if (
            player ==
                GomokuPlayer
                    .PROFESSOR
        ) {
            var best =
                -WIN_SCORE

            for ((cell, _) in candidates) {
                if (
                    GomokuWinDetector
                        .wouldWin(
                            snapshot,
                            cell,
                            player
                        )
                ) {
                    return WIN_SCORE -
                        (
                            profile.searchDepth -
                                depth
                            )
                }

                val score =
                    minimax(
                        snapshot =
                            place(
                                snapshot,
                                cell,
                                player
                            ),
                        player =
                            player.other(),
                        depth =
                            depth - 1,
                        alpha = a,
                        beta = b,
                        profile =
                            profile
                    )

                best =
                    max(
                        best,
                        score
                    )
                a =
                    max(
                        a,
                        best
                    )

                if (b <= a) {
                    break
                }
            }

            return best
        }

        var best =
            WIN_SCORE

        for ((cell, _) in candidates) {
            if (
                GomokuWinDetector
                    .wouldWin(
                        snapshot,
                        cell,
                        player
                    )
            ) {
                return -WIN_SCORE +
                    (
                        profile.searchDepth -
                            depth
                        )
            }

            val score =
                minimax(
                    snapshot =
                        place(
                            snapshot,
                            cell,
                            player
                        ),
                    player =
                        player.other(),
                    depth =
                        depth - 1,
                    alpha = a,
                    beta = b,
                    profile =
                        profile
                )

            best =
                kotlin.math.min(
                    best,
                    score
                )
            b =
                kotlin.math.min(
                    b,
                    best
                )

            if (b <= a) {
                break
            }
        }

        return best
    }

    private fun evaluate(
        snapshot: GomokuSnapshot,
        profile:
            GomokuDifficultyProfile
    ): Int {
        val candidates =
            candidateMoves(
                snapshot,
                profile
                    .neighborhoodRadius
            )

        if (candidates.isEmpty()) {
            return 0
        }

        val professorBest =
            candidates
                .maxOfOrNull {
                    staticMoveScore(
                        snapshot,
                        it,
                        GomokuPlayer
                            .PROFESSOR,
                        profile
                    )
                }
                ?: 0

        val playerBest =
            candidates
                .maxOfOrNull {
                    staticMoveScore(
                        snapshot,
                        it,
                        GomokuPlayer
                            .PLAYER,
                        profile
                    )
                }
                ?: 0

        return professorBest -
            (
                playerBest *
                    profile
                        .defenseWeightPercent /
                    100
                )
    }

    private fun staticMoveScore(
        snapshot: GomokuSnapshot,
        cell: Cell,
        player: GomokuPlayer,
        profile:
            GomokuDifficultyProfile
    ): Int {
        if (!snapshot.isFree(cell)) {
            return Int.MIN_VALUE /
                4
        }

        if (
            GomokuWinDetector
                .wouldWin(
                    snapshot,
                    cell,
                    player
                )
        ) {
            return WIN_SCORE
        }

        val ownPotential =
            linePatternScore(
                snapshot,
                cell,
                player
            )

        val opponent =
            player.other()

        val blockPotential =
            linePatternScore(
                snapshot,
                cell,
                opponent
            ) *
                profile
                    .defenseWeightPercent /
                100

        val trapBonus =
            if (profile.trapAware) {
                val threats =
                    threatCountAfter(
                        snapshot,
                        cell,
                        player
                    )

                when {
                    threats >= 3 ->
                        140_000

                    threats == 2 ->
                        75_000

                    else ->
                        0
                }
            } else {
                0
            }

        val center =
            (
                snapshot.size -
                    1
                ) /
                2f

        val centerPenalty =
            (
                abs(
                    cell.row -
                        center
                ) +
                    abs(
                        cell.col -
                            center
                    )
                )
                .toInt() *
                2

        return ownPotential +
            blockPotential +
            trapBonus -
            centerPenalty
    }

    private fun linePatternScore(
        snapshot: GomokuSnapshot,
        cell: Cell,
        player: GomokuPlayer
    ): Int {
        var total = 0

        for (direction in directions) {
            val pattern =
                linePattern(
                    snapshot,
                    cell,
                    player,
                    direction[0],
                    direction[1]
                )

            total +=
                when {
                    pattern.length >= 5 ->
                        WIN_SCORE

                    pattern.length == 4 &&
                        pattern.openEnds ==
                            2 ->
                        160_000

                    pattern.length == 4 ->
                        80_000

                    pattern.length == 3 &&
                        pattern.openEnds ==
                            2 ->
                        28_000

                    pattern.length == 3 ->
                        9_000

                    pattern.length == 2 &&
                        pattern.openEnds ==
                            2 ->
                        2_500

                    pattern.length == 2 ->
                        800

                    else ->
                        80 *
                            pattern.openEnds
                }
        }

        return total
    }

    private fun threatCountAfter(
        snapshot: GomokuSnapshot,
        cell: Cell,
        player: GomokuPlayer
    ): Int {
        if (!snapshot.isFree(cell)) {
            return 0
        }

        return directions.count {
            val pattern =
                linePattern(
                    snapshot,
                    cell,
                    player,
                    it[0],
                    it[1]
                )

            pattern.length >= 3 &&
                pattern.openEnds >=
                    1
        }
    }

    private data class LinePattern(
        val length: Int,
        val openEnds: Int
    )

    private data class LineAnalysis(
        val label: String,
        val length: Int,
        val cells: List<Cell>,
        val openEnds: List<Cell>
    )

    private fun strongestLine(
        snapshot: GomokuSnapshot,
        cell: Cell,
        player: GomokuPlayer
    ): LineAnalysis =
        directions
            .map {
                direction ->
                lineAnalysis(
                    snapshot =
                        snapshot,
                    cell =
                        cell,
                    player =
                        player,
                    dr =
                        direction[0],
                    dc =
                        direction[1]
                )
            }
            .maxWithOrNull(
                compareBy<
                    LineAnalysis
                    > {
                    it.length
                }.thenBy {
                    it.openEnds
                        .size
                }
            )
            ?: LineAnalysis(
                label =
                    "cette zone",
                length = 1,
                cells =
                    listOf(cell),
                openEnds =
                    emptyList()
            )

    private fun lineAnalysis(
        snapshot: GomokuSnapshot,
        cell: Cell,
        player: GomokuPlayer,
        dr: Int,
        dc: Int
    ): LineAnalysis {
        val before =
            mutableListOf<Cell>()

        var row =
            cell.row - dr
        var col =
            cell.col - dc

        while (
            row in
                0 until snapshot.size &&
            col in
                0 until snapshot.size &&
            snapshot.stones[
                Cell(
                    row,
                    col
                )
            ] ==
                player
        ) {
            before.add(
                Cell(
                    row,
                    col
                )
            )
            row -= dr
            col -= dc
        }

        val openBefore =
            if (
                row in
                    0 until snapshot.size &&
                col in
                    0 until snapshot.size &&
                snapshot.stones[
                    Cell(
                        row,
                        col
                    )
                ] ==
                    null
            ) {
                Cell(
                    row,
                    col
                )
            } else {
                null
            }

        val after =
            mutableListOf<Cell>()

        row =
            cell.row + dr
        col =
            cell.col + dc

        while (
            row in
                0 until snapshot.size &&
            col in
                0 until snapshot.size &&
            snapshot.stones[
                Cell(
                    row,
                    col
                )
            ] ==
                player
        ) {
            after.add(
                Cell(
                    row,
                    col
                )
            )
            row += dr
            col += dc
        }

        val openAfter =
            if (
                row in
                    0 until snapshot.size &&
                col in
                    0 until snapshot.size &&
                snapshot.stones[
                    Cell(
                        row,
                        col
                    )
                ] ==
                    null
            ) {
                Cell(
                    row,
                    col
                )
            } else {
                null
            }

        val cells =
            before
                .asReversed() +
                cell +
                after

        return LineAnalysis(
            label =
                directionLabel(
                    dr,
                    dc
                ),
            length =
                cells.size,
            cells =
                cells,
            openEnds =
                listOfNotNull(
                    openBefore,
                    openAfter
                )
        )
    }

    private fun linePattern(
        snapshot: GomokuSnapshot,
        cell: Cell,
        player: GomokuPlayer,
        dr: Int,
        dc: Int
    ): LinePattern {
        val analysis =
            lineAnalysis(
                snapshot =
                    snapshot,
                cell =
                    cell,
                player =
                    player,
                dr =
                    dr,
                dc =
                    dc
            )

        return LinePattern(
            length =
                analysis.length,
            openEnds =
                analysis
                    .openEnds
                    .size
        )
    }

    private fun directionLabel(
        dr: Int,
        dc: Int
    ): String =
        when {
            dr == 0 ->
                "la ligne horizontale"

            dc == 0 ->
                "la ligne verticale"

            dr == dc ->
                "la diagonale descendante"

            else ->
                "la diagonale montante"
        }

    private fun coord(
        cell: Cell
    ): String =
        "ligne " +
            (cell.row + 1) +
            ", colonne " +
            (cell.col + 1)

    private fun candidateMoves(
        snapshot: GomokuSnapshot,
        radius: Int
    ): List<Cell> {
        if (snapshot.stones.isEmpty()) {
            val center =
                snapshot.size /
                    2

            return listOf(
                Cell(
                    center,
                    center
                )
            )
        }

        val result =
            linkedSetOf<Cell>()

        for (stone in snapshot.stones.keys) {
            for (
                dr in
                -radius..radius
            ) {
                for (
                    dc in
                    -radius..radius
                ) {
                    if (
                        dr == 0 &&
                        dc == 0
                    ) {
                        continue
                    }

                    val row =
                        stone.row +
                            dr

                    val col =
                        stone.col +
                            dc

                    if (
                        row !in
                            0 until
                                snapshot.size ||
                        col !in
                            0 until
                                snapshot.size
                    ) {
                        continue
                    }

                    val cell =
                        Cell(
                            row,
                            col
                        )

                    if (
                        !snapshot.stones
                            .containsKey(
                                cell
                            )
                    ) {
                        result.add(
                            cell
                        )
                    }
                }
            }
        }

        return result
            .sortedWith(
                cellComparator()
            )
    }

    private fun place(
        snapshot: GomokuSnapshot,
        cell: Cell,
        player: GomokuPlayer
    ): GomokuSnapshot =
        snapshot.copy(
            stones =
                snapshot.stones +
                    (
                        cell to
                            player
                        ),
            currentPlayer =
                player.other()
        )

    private fun cellComparator():
        Comparator<Cell> =
        compareBy<Cell> {
            it.row
        }.thenBy {
            it.col
        }
}

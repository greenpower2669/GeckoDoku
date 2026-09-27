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
            snapshot.currentPlayer != player
        ) {
            return null
        }

        if (
            player ==
                GomokuPlayer.PROFESSOR
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
                    GomokuPlayer.PROFESSOR,
                winner =
                    snapshot.winner
                        ?.other()
            )

        return chooseMove(
            swapped,
            difficulty
        )
    }

    fun chooseMove(
        snapshot: GomokuSnapshot,
        difficulty: GameDifficulty
    ): GomokuAiDecision? {
        if (
            snapshot.gameOver ||
            snapshot.currentPlayer !=
                GomokuPlayer.PROFESSOR
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
            .firstOrNull()
            ?.let {
                return GomokuAiDecision(
                    cell = it,
                    reason =
                        "Ce coup permet de gagner tout de suite en complétant cette ligne.",
                    score =
                        WIN_SCORE
                )
            }

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
                            GomokuPlayer.PROFESSOR,
                            profile
                        )
                    }.then(
                        cellComparator()
                    )
                )

        forcedBlocks
            .firstOrNull()
            ?.let {
                return GomokuAiDecision(
                    cell = it,
                    reason =
                        "Ce coup bloque une victoire adverse au prochain tour.",
                    score =
                        WIN_SCORE / 2
                )
            }

        val ordered =
            candidates
                .map {
                    cell ->
                    cell to
                        staticMoveScore(
                            snapshot,
                            cell,
                            GomokuPlayer.PROFESSOR,
                            profile
                        )
                }
                .sortedWith(
                    compareByDescending<Pair<Cell, Int>> {
                        it.second
                    }.thenBy {
                        it.first.row
                    }.thenBy {
                        it.first.col
                    }
                )
                .take(
                    profile.beamWidth
                )

        var bestCell:
            Cell? = null

        var bestScore =
            Int.MIN_VALUE

        for ((cell, staticScore) in ordered) {
            val next =
                place(
                    snapshot,
                    cell,
                    GomokuPlayer.PROFESSOR
                )

            val future =
                if (
                    profile.searchDepth <= 1
                ) {
                    0
                } else {
                    minimax(
                        snapshot = next,
                        player =
                            GomokuPlayer.PLAYER,
                        depth =
                            profile.searchDepth -
                                1,
                        alpha =
                            -WIN_SCORE,
                        beta =
                            WIN_SCORE,
                        profile =
                            profile
                    )
                }

            val score =
                staticScore +
                    future

            if (
                score > bestScore ||
                (
                    score == bestScore &&
                    (
                        bestCell == null ||
                            cellComparator()
                                .compare(
                                    cell,
                                    bestCell
                                ) < 0
                        )
                    )
            ) {
                bestScore = score
                bestCell = cell
            }
        }

        val chosen =
            bestCell
                ?: return null

        val threats =
            threatCountAfter(
                snapshot,
                chosen,
                GomokuPlayer.PROFESSOR
            )

        val reason =
            when {
                profile.trapAware &&
                    threats >= 2 ->
                    "Ce coup prépare un piège en créant plusieurs menaces à surveiller."

                profile.searchDepth >= 3 ->
                    "Ce coup prépare la suite sur plusieurs coups plutôt que de répondre seulement au dernier."

                linePotential(
                    snapshot,
                    chosen,
                    GomokuPlayer.PROFESSOR
                ) >= 4 ->
                    "Ce coup construit une ligne de quatre qui force une réaction."

                else ->
                    "Ce coup renforce une zone active tout en gardant plusieurs suites possibles."
            }

        return GomokuAiDecision(
            cell = chosen,
            reason = reason,
            score = bestScore
        )
    }

    private fun minimax(
        snapshot: GomokuSnapshot,
        player: GomokuPlayer,
        depth: Int,
        alpha: Int,
        beta: Int,
        profile: GomokuDifficultyProfile
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
                    compareByDescending<Pair<Cell, Int>> {
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
                GomokuPlayer.PROFESSOR
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
                    max(best, score)
                a =
                    max(a, best)

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
                    11 /
                    10
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
            return Int.MIN_VALUE / 4
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
            ) * 9 / 10

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

                    else -> 0
                }
            } else {
                0
            }

        val center =
            (snapshot.size - 1) /
                2f

        val centerPenalty =
            (
                abs(cell.row - center) +
                    abs(cell.col - center)
                ).toInt() *
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
                        pattern.openEnds == 2 ->
                        160_000

                    pattern.length == 4 ->
                        80_000

                    pattern.length == 3 &&
                        pattern.openEnds == 2 ->
                        28_000

                    pattern.length == 3 ->
                        9_000

                    pattern.length == 2 &&
                        pattern.openEnds == 2 ->
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

    private fun linePotential(
        snapshot: GomokuSnapshot,
        cell: Cell,
        player: GomokuPlayer
    ): Int =
        directions
            .maxOf {
                linePattern(
                    snapshot,
                    cell,
                    player,
                    it[0],
                    it[1]
                ).length
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
                pattern.openEnds >= 1
        }
    }

    private data class LinePattern(
        val length: Int,
        val openEnds: Int
    )

    private fun linePattern(
        snapshot: GomokuSnapshot,
        cell: Cell,
        player: GomokuPlayer,
        dr: Int,
        dc: Int
    ): LinePattern {
        var length = 1
        var openEnds = 0

        var row =
            cell.row + dr

        var col =
            cell.col + dc

        while (
            row in 0 until snapshot.size &&
            col in 0 until snapshot.size &&
            snapshot.stones[
                Cell(row, col)
            ] == player
        ) {
            length += 1
            row += dr
            col += dc
        }

        if (
            row in 0 until snapshot.size &&
            col in 0 until snapshot.size &&
            snapshot.stones[
                Cell(row, col)
            ] == null
        ) {
            openEnds += 1
        }

        row =
            cell.row - dr
        col =
            cell.col - dc

        while (
            row in 0 until snapshot.size &&
            col in 0 until snapshot.size &&
            snapshot.stones[
                Cell(row, col)
            ] == player
        ) {
            length += 1
            row -= dr
            col -= dc
        }

        if (
            row in 0 until snapshot.size &&
            col in 0 until snapshot.size &&
            snapshot.stones[
                Cell(row, col)
            ] == null
        ) {
            openEnds += 1
        }

        return LinePattern(
            length = length,
            openEnds = openEnds
        )
    }

    private fun candidateMoves(
        snapshot: GomokuSnapshot,
        radius: Int
    ): List<Cell> {
        if (snapshot.stones.isEmpty()) {
            val center =
                snapshot.size / 2

            return listOf(
                Cell(center, center)
            )
        }

        val result =
            linkedSetOf<Cell>()

        for (stone in snapshot.stones.keys) {
            for (dr in -radius..radius) {
                for (dc in -radius..radius) {
                    if (
                        dr == 0 &&
                        dc == 0
                    ) {
                        continue
                    }

                    val row =
                        stone.row + dr

                    val col =
                        stone.col + dc

                    if (
                        row !in 0 until
                            snapshot.size ||
                        col !in 0 until
                            snapshot.size
                    ) {
                        continue
                    }

                    val cell =
                        Cell(row, col)

                    if (
                        !snapshot.stones
                            .containsKey(cell)
                    ) {
                        result.add(cell)
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
                    (cell to player),
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

package com.greenpower2669.geckodoku

class GomokuGameEngine(
    val size: Int = DEFAULT_SIZE
) {
    private val stones =
        linkedMapOf<Cell, GomokuPlayer>()

    private var currentPlayer =
        GomokuPlayer.PLAYER

    private var winner:
        GomokuPlayer? = null

    private var winningLine:
        List<Cell> =
        emptyList()

    private var draw =
        false

    init {
        require(size in 5..31)
    }

    fun snapshot(): GomokuSnapshot =
        GomokuSnapshot(
            size = size,
            stones = stones.toMap(),
            currentPlayer = currentPlayer,
            winner = winner,
            winningLine =
                winningLine.toList(),
            draw = draw
        )

    fun play(
        cell: Cell
    ): GomokuMoveResult {
        if (
            winner != null ||
            draw
        ) {
            return GomokuMoveResult
                .GAME_OVER
        }

        if (
            cell.row !in 0 until size ||
            cell.col !in 0 until size
        ) {
            return GomokuMoveResult
                .OUT_OF_BOUNDS
        }

        if (stones.containsKey(cell)) {
            return GomokuMoveResult
                .OCCUPIED
        }

        val playedBy =
            currentPlayer

        stones[cell] =
            playedBy

        val line =
            GomokuWinDetector
                .winningLine(
                    size = size,
                    stones = stones,
                    origin = cell,
                    player = playedBy
                )

        if (line.isNotEmpty()) {
            winner = playedBy
            winningLine = line

            return GomokuMoveResult
                .WIN
        }

        if (
            stones.size ==
                size * size
        ) {
            draw = true

            return GomokuMoveResult
                .DRAW
        }

        currentPlayer =
            currentPlayer.other()

        return GomokuMoveResult
            .PLACED
    }

    companion object {
        const val DEFAULT_SIZE =
            19
    }
}

object GomokuWinDetector {
    private val directions =
        arrayOf(
            intArrayOf(0, 1),
            intArrayOf(1, 0),
            intArrayOf(1, 1),
            intArrayOf(1, -1)
        )

    fun winningLine(
        size: Int,
        stones: Map<Cell, GomokuPlayer>,
        origin: Cell,
        player: GomokuPlayer
    ): List<Cell> {
        if (stones[origin] != player) {
            return emptyList()
        }

        for (direction in directions) {
            val dr =
                direction[0]

            val dc =
                direction[1]

            val negative =
                collect(
                    size = size,
                    stones = stones,
                    origin = origin,
                    player = player,
                    dr = -dr,
                    dc = -dc
                ).reversed()

            val positive =
                collect(
                    size = size,
                    stones = stones,
                    origin = origin,
                    player = player,
                    dr = dr,
                    dc = dc
                )

            val line =
                negative +
                    origin +
                    positive

            if (line.size >= 5) {
                return line
            }
        }

        return emptyList()
    }

    fun wouldWin(
        snapshot: GomokuSnapshot,
        cell: Cell,
        player: GomokuPlayer
    ): Boolean {
        if (!snapshot.isFree(cell)) {
            return false
        }

        val next =
            snapshot.stones +
                (cell to player)

        return winningLine(
            size = snapshot.size,
            stones = next,
            origin = cell,
            player = player
        ).isNotEmpty()
    }

    private fun collect(
        size: Int,
        stones: Map<Cell, GomokuPlayer>,
        origin: Cell,
        player: GomokuPlayer,
        dr: Int,
        dc: Int
    ): List<Cell> {
        val result =
            mutableListOf<Cell>()

        var row =
            origin.row + dr

        var col =
            origin.col + dc

        while (
            row in 0 until size &&
            col in 0 until size
        ) {
            val cell =
                Cell(row, col)

            if (stones[cell] != player) {
                break
            }

            result.add(cell)

            row += dr
            col += dc
        }

        return result
    }
}

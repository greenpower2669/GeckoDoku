package com.greenpower2669.geckodoku

enum class GomokuPlayer {
    PLAYER,
    PROFESSOR;

    fun other(): GomokuPlayer =
        if (this == PLAYER) {
            PROFESSOR
        } else {
            PLAYER
        }
}

enum class GomokuMoveResult {
    PLACED,
    WIN,
    DRAW,
    OCCUPIED,
    OUT_OF_BOUNDS,
    GAME_OVER
}

data class GomokuSnapshot(
    val size: Int,
    val stones: Map<Cell, GomokuPlayer>,
    val currentPlayer: GomokuPlayer,
    val winner: GomokuPlayer? = null,
    val winningLine: List<Cell> = emptyList(),
    val draw: Boolean = false
) {
    val gameOver: Boolean
        get() =
            winner != null ||
                draw

    val moveCount: Int
        get() = stones.size

    fun playerAt(
        cell: Cell
    ): GomokuPlayer? =
        stones[cell]

    fun isFree(
        cell: Cell
    ): Boolean =
        cell.row in 0 until size &&
            cell.col in 0 until size &&
            !stones.containsKey(cell)
}

class GomokuSnapshotBuilder(
    private val size: Int
) {
    private val stones =
        linkedMapOf<Cell, GomokuPlayer>()

    init {
        require(size >= 5)
    }

    fun put(
        player: GomokuPlayer,
        vararg cells: Cell
    ): GomokuSnapshotBuilder =
        apply {
            cells.forEach {
                require(
                    it.row in 0 until size &&
                        it.col in 0 until size
                )
                stones[it] = player
            }
        }

    fun build(
        currentPlayer: GomokuPlayer =
            GomokuPlayer.PLAYER
    ): GomokuSnapshot =
        GomokuSnapshot(
            size = size,
            stones = stones.toMap(),
            currentPlayer = currentPlayer
        )
}

data class GomokuAiDecision(
    val cell: Cell,
    val reason: String,
    val score: Int
)

data class GomokuDifficultyProfile(
    val searchDepth: Int,
    val beamWidth: Int,
    val trapAware: Boolean,
    val neighborhoodRadius: Int,
    val explanationHorizon: Int
)

object GomokuDifficultyPolicy {
    fun profile(
        difficulty: GameDifficulty
    ): GomokuDifficultyProfile =
        when (difficulty) {
            GameDifficulty.DISCOVERY ->
                GomokuDifficultyProfile(
                    searchDepth = 1,
                    beamWidth = 5,
                    trapAware = false,
                    neighborhoodRadius = 1,
                    explanationHorizon = 1
                )

            GameDifficulty.EASY ->
                GomokuDifficultyProfile(
                    searchDepth = 1,
                    beamWidth = 6,
                    trapAware = false,
                    neighborhoodRadius = 2,
                    explanationHorizon = 1
                )

            GameDifficulty.THINKING ->
                GomokuDifficultyProfile(
                    searchDepth = 2,
                    beamWidth = 7,
                    trapAware = false,
                    neighborhoodRadius = 2,
                    explanationHorizon = 2
                )

            GameDifficulty.HARD ->
                GomokuDifficultyProfile(
                    searchDepth = 2,
                    beamWidth = 8,
                    trapAware = true,
                    neighborhoodRadius = 2,
                    explanationHorizon = 2
                )

            GameDifficulty.EXPERT ->
                GomokuDifficultyProfile(
                    searchDepth = 3,
                    beamWidth = 8,
                    trapAware = true,
                    neighborhoodRadius = 2,
                    explanationHorizon = 3
                )

            GameDifficulty.DEMENTIAL ->
                GomokuDifficultyProfile(
                    searchDepth = 3,
                    beamWidth = 10,
                    trapAware = true,
                    neighborhoodRadius = 2,
                    explanationHorizon = 4
                )

            GameDifficulty.MISSION_IMPOSSIBLE ->
                GomokuDifficultyProfile(
                    searchDepth = 4,
                    beamWidth = 9,
                    trapAware = true,
                    neighborhoodRadius = 2,
                    explanationHorizon = 5
                )

            GameDifficulty.INFERNAL ->
                GomokuDifficultyProfile(
                    searchDepth = 4,
                    beamWidth = 11,
                    trapAware = true,
                    neighborhoodRadius = 2,
                    explanationHorizon = 6
                )
        }
}

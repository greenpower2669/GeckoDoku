package com.greenpower2669.geckodoku

import org.json.JSONObject

data class GlobalScoreCommon(
    val runId: String,
    val playerName: String,
    val mode: GameMode,
    val difficulty: GameDifficulty,
    val size: Int,
    val stars: Int,
    val elapsedSeconds: Long,
    val mistakes: Int,
    val assistancePoints: Int,
    val usedProfessor: Boolean,
    val completedAt: Long,
    val appVersion: String,
    val puzzleId: String? = null,
    val seed: Long? = null,
    val metadata: Map<String, Any?> = emptyMap()
)

sealed interface GlobalScoreModeDetails {
    data object Classic : GlobalScoreModeDetails

    data class Sudoku(
        val seed: Long?,
        val visualStyle: SudokuVisualStyle?
    ) : GlobalScoreModeDetails

    data class Gomoku(
        val matchMode: GomokuMatchMode?,
        val winner: GomokuPlayer?,
        val draw: Boolean?,
        val moveCount: Int?
    ) : GlobalScoreModeDetails

    data class BeeGecko(
        val puzzleId: String?,
        val seed: Long?,
        val radius: Int?,
        val pairCount: Int?
    ) : GlobalScoreModeDetails
}

data class GlobalScorePayload(
    val schemaVersion: Int = 1,
    val scoreVersion: Int = 1,
    val runId: String,
    val playerName: String,
    val mode: String,
    val difficulty: String,
    val size: Int,
    val completed: Boolean = true,
    val stars: Int,
    val elapsedSeconds: Long,
    val mistakes: Int,
    val assistancePoints: Int,
    val usedProfessor: Boolean,
    val completedAt: Long,
    val appVersion: String,
    val puzzleId: String? = null,
    val seed: String? = null,
    val sudokuVisualStyle: String? = null,
    val gomokuMatchMode: String? = null,
    val gomokuWinner: String? = null,
    val gomokuDraw: Boolean? = null,
    val gomokuMoveCount: Int? = null,
    val beeGeckoRadius: Int? = null,
    val beeGeckoPairCount: Int? = null,
    val metadata: Map<String, Any?> = emptyMap()
)

object GlobalScorePayloadCodec {
    fun encode(
        payload: GlobalScorePayload
    ): String =
        JSONObject().apply {
            put("schemaVersion", payload.schemaVersion)
            put("scoreVersion", payload.scoreVersion)
            put("runId", payload.runId)
            put("playerName", payload.playerName)
            put("mode", payload.mode)
            put("difficulty", payload.difficulty)
            put("size", payload.size)
            put("completed", payload.completed)
            put("stars", payload.stars)
            put("elapsedSeconds", payload.elapsedSeconds)
            put("mistakes", payload.mistakes)
            put("assistancePoints", payload.assistancePoints)
            put("usedProfessor", payload.usedProfessor)
            put("completedAt", payload.completedAt)
            put("appVersion", payload.appVersion)
            putNullable("puzzleId", payload.puzzleId)
            putNullable("seed", payload.seed)
            putNullable(
                "sudokuVisualStyle",
                payload.sudokuVisualStyle
            )
            putNullable(
                "gomokuMatchMode",
                payload.gomokuMatchMode
            )
            putNullable(
                "gomokuWinner",
                payload.gomokuWinner
            )
            putNullable(
                "gomokuDraw",
                payload.gomokuDraw
            )
            putNullable(
                "gomokuMoveCount",
                payload.gomokuMoveCount
            )
            putNullable(
                "beeGeckoRadius",
                payload.beeGeckoRadius
            )
            putNullable(
                "beeGeckoPairCount",
                payload.beeGeckoPairCount
            )
            put(
                "metadata",
                JSONObject(payload.metadata)
            )
        }.toString()

    private fun JSONObject.putNullable(
        key: String,
        value: Any?
    ) {
        put(
            key,
            value ?: JSONObject.NULL
        )
    }
}

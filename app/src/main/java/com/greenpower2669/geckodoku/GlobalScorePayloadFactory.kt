package com.greenpower2669.geckodoku

object GlobalScorePayloadFactory {
    fun create(
        common: GlobalScoreCommon,
        details: GlobalScoreModeDetails
    ): GlobalScorePayload {
        var puzzleId =
            common.puzzleId

        var seed =
            common.seed
                ?.toString()

        var sudokuVisualStyle:
            String? = null

        var gomokuMatchMode:
            String? = null

        var gomokuWinner:
            String? = null

        var gomokuDraw:
            Boolean? = null

        var gomokuMoveCount:
            Int? = null

        var beeGeckoRadius:
            Int? = null

        var beeGeckoPairCount:
            Int? = null

        when (details) {
            GlobalScoreModeDetails.Classic ->
                Unit

            is GlobalScoreModeDetails.Sudoku -> {
                seed =
                    details.seed
                        ?.toString()
                        ?: seed

                sudokuVisualStyle =
                    details.visualStyle
                        ?.name
            }

            is GlobalScoreModeDetails.Gomoku -> {
                gomokuMatchMode =
                    details.matchMode
                        ?.name

                gomokuWinner =
                    details.winner
                        ?.name

                gomokuDraw =
                    details.draw

                gomokuMoveCount =
                    details.moveCount
            }

            is GlobalScoreModeDetails.BeeGecko -> {
                puzzleId =
                    details.puzzleId
                        ?: puzzleId

                seed =
                    details.seed
                        ?.toString()
                        ?: seed

                beeGeckoRadius =
                    details.radius

                beeGeckoPairCount =
                    details.pairCount
            }
        }

        return GlobalScorePayload(
            runId = common.runId,
            playerName = common.playerName,
            mode = common.mode.name,
            difficulty =
                common.difficulty.name,
            size = common.size,
            stars = common.stars,
            elapsedSeconds =
                common.elapsedSeconds,
            mistakes = common.mistakes,
            assistancePoints =
                common.assistancePoints,
            usedProfessor =
                common.usedProfessor,
            completedAt =
                common.completedAt,
            appVersion =
                common.appVersion,
            puzzleId = puzzleId,
            seed = seed,
            sudokuVisualStyle =
                sudokuVisualStyle,
            gomokuMatchMode =
                gomokuMatchMode,
            gomokuWinner =
                gomokuWinner,
            gomokuDraw =
                gomokuDraw,
            gomokuMoveCount =
                gomokuMoveCount,
            beeGeckoRadius =
                beeGeckoRadius,
            beeGeckoPairCount =
                beeGeckoPairCount,
            metadata =
                common.metadata
        )
    }
}

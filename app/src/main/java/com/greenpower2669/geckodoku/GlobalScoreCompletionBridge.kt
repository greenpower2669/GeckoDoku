package com.greenpower2669.geckodoku

object GlobalScoreCompletionBridge {
    fun publish(
        publisher: GlobalScoreCompletionPublisher,
        playerName: String,
        mode: GameMode,
        size: Int,
        difficulty: GameDifficulty,
        stars: Int,
        elapsedSeconds: Long,
        mistakes: Int,
        assistancePoints: Int,
        appVersion: String,
        classicPuzzle: Puzzle?,
        sudokuPuzzle: SudokuPuzzle?,
        sudokuVisualStyle: SudokuVisualStyle?,
        gomokuSnapshot: GomokuSnapshot?,
        gomokuMatchMode: GomokuMatchMode?,
        beeGeckoPuzzle: BeeGeckoPuzzle?
    ): String? {
        val puzzleId: String?
        val seed: Long?
        val details: GlobalScoreModeDetails

        when (mode) {
            GameMode.GECKODOKU -> {
                puzzleId = classicPuzzle?.id
                seed = classicPuzzle?.seed
                details =
                    GlobalScoreModeDetails.Classic
            }

            GameMode.SUDOKU -> {
                puzzleId = null
                seed = sudokuPuzzle?.seed
                details =
                    GlobalScoreModeDetails.Sudoku(
                        seed = sudokuPuzzle?.seed,
                        visualStyle = sudokuVisualStyle
                    )
            }

            GameMode.GOMOKU -> {
                puzzleId = null
                seed = null
                details =
                    GlobalScoreModeDetails.Gomoku(
                        matchMode = gomokuMatchMode,
                        winner = gomokuSnapshot?.winner,
                        draw = gomokuSnapshot?.draw,
                        moveCount =
                            gomokuSnapshot?.moveCount
                    )
            }

            GameMode.BEES_GECKOS -> {
                puzzleId = beeGeckoPuzzle?.id
                seed = beeGeckoPuzzle?.seed
                details =
                    GlobalScoreModeDetails.BeeGecko(
                        puzzleId =
                            beeGeckoPuzzle?.id,
                        seed =
                            beeGeckoPuzzle?.seed,
                        radius =
                            beeGeckoPuzzle?.radius,
                        pairCount =
                            beeGeckoPuzzle
                                ?.regionCount
                    )
            }
        }

        return publisher.publish(
            playerName = playerName,
            mode = mode,
            size = size,
            difficulty = difficulty,
            stars = stars,
            elapsedSeconds = elapsedSeconds,
            mistakes = mistakes,
            assistancePoints = assistancePoints,
            appVersion = appVersion,
            puzzleId = puzzleId,
            seed = seed,
            details = details
        )
    }
}

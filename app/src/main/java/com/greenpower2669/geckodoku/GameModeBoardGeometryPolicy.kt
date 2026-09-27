package com.greenpower2669.geckodoku

class GameModeBoardGeometryPolicy {
    private val geckoPolicy =
        BoardGeometryPolicy()

    private val sudokuPolicy =
        BoardGeometryPolicy()

    fun resolve(
        mode: GameMode,
        windowWidth: Int,
        windowHeight: Int,
        proposed: BoardGeometry
    ): BoardGeometry =
        policyFor(mode)
            .resolve(
                windowWidth =
                    windowWidth,
                windowHeight =
                    windowHeight,
                proposed =
                    proposed
            )

    fun reset(
        mode: GameMode
    ) {
        policyFor(mode)
            .reset()
    }

    fun reset() {
        resetAll()
    }

    fun resetAll() {
        geckoPolicy.reset()
        sudokuPolicy.reset()
    }

    private fun policyFor(
        mode: GameMode
    ): BoardGeometryPolicy =
        when (mode) {
            GameMode.GECKODOKU ->
                geckoPolicy

            GameMode.SUDOKU ->
                sudokuPolicy
        }
}

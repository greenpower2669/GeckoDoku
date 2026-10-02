package com.greenpower2669.geckodoku

object GomokuMatchPolicy {
    fun professorControlsCurrentTurn(
        mode: GomokuMatchMode,
        snapshot: GomokuSnapshot
    ): Boolean =
        mode ==
            GomokuMatchMode.VS_PROFESSOR &&
            !snapshot.gameOver &&
            snapshot.currentPlayer ==
                GomokuPlayer.PROFESSOR

    fun professorCanAdvise(
        mode: GomokuMatchMode,
        snapshot: GomokuSnapshot,
        thinking: Boolean
    ): Boolean =
        !snapshot.gameOver &&
            !thinking &&
            (
                mode ==
                    GomokuMatchMode.HUMAN_VS_HUMAN ||
                snapshot.currentPlayer ==
                    GomokuPlayer.PLAYER
                )

    fun longPressAppliesHumanMove(
        mode: GomokuMatchMode,
        snapshot: GomokuSnapshot
    ): Boolean =
        mode ==
            GomokuMatchMode.VS_PROFESSOR &&
            !snapshot.gameOver &&
            snapshot.currentPlayer ==
                GomokuPlayer.PLAYER
}

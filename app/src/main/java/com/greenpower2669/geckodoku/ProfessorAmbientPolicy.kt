package com.greenpower2669.geckodoku

class ProfessorAmbientPolicy {
    val idleHelpDelayMs: Long = 90_000L
    val longGameDelayMs: Long = 600_000L
    val longGameActionCount: Int = 25
    val smallTalkMinDelayMs: Long = 240_000L
    val smallTalkMaxDelayMs: Long = 480_000L
    val minimumAmbientGapMs: Long = 60_000L
    val tickMs: Long = 5_000L

    fun shouldOfferHelp(
        nowMs: Long,
        lastBoardActionAtMs: Long,
        alreadyOffered: Boolean,
        blocked: Boolean
    ): Boolean =
        !blocked &&
            !alreadyOffered &&
            nowMs - lastBoardActionAtMs >= idleHelpDelayMs

    fun shouldOfferSave(
        nowMs: Long,
        gameStartedAtMs: Long,
        boardActionCount: Int,
        alreadyOffered: Boolean,
        alreadySaved: Boolean,
        blocked: Boolean
    ): Boolean =
        !blocked &&
            !alreadyOffered &&
            !alreadySaved &&
            (
                nowMs - gameStartedAtMs >= longGameDelayMs ||
                    boardActionCount >= longGameActionCount
                )

    fun canSpeakSmallTalk(
        nowMs: Long,
        nextSmallTalkAtMs: Long,
        blocked: Boolean
    ): Boolean =
        !blocked &&
            nowMs >= nextSmallTalkAtMs

    fun smallTalkDelayMs(
        randomValue: Int
    ): Long {
        val span =
            (
                smallTalkMaxDelayMs -
                    smallTalkMinDelayMs +
                    1L
                ).toInt()

        return smallTalkMinDelayMs +
            Math.floorMod(
                randomValue,
                span
            )
    }
}

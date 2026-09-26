package com.greenpower2669.geckodoku

class ProfessorQuickTalkPolicy {
    private val selector =
        PierreSmallTalkSelector()

    val affectsBoardLayout: Boolean = false

    fun chooseIndex(
        randomValue: Int,
        previousIndex: Int?
    ): Int =
        selector.chooseIndex(
            randomValue = randomValue,
            previousIndex = previousIndex
        )
}

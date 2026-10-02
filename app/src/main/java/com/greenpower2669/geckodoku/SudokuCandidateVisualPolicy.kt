package com.greenpower2669.geckodoku

class SudokuCandidateVisualPolicy {
    val forceBlackGlyph:
        Boolean = true

    val candidateArgb:
        Int = 0xFF000000.toInt()

    fun styleForCandidate(
        @Suppress("UNUSED_PARAMETER")
        mainStyle:
            SudokuVisualStyle
    ): SudokuVisualStyle =
        SudokuVisualStyle
            .CLASSIC_NUMBERS
}

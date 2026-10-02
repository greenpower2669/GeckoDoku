package com.greenpower2669.geckodoku

enum class SudokuProjectionType {
    ROW,
    COLUMN,
    BOX
}

data class SudokuReasoningElimination(
    val cell: Cell,
    val digit: Int,
    val sourceCell: Cell,
    val projection:
        SudokuProjectionType
)

data class SudokuReasoningStep(
    val sourceCell: Cell?,
    val projection:
        SudokuProjectionType?,
    val eliminations:
        List<SudokuReasoningElimination>,
    val narration: String,
    val finalCell: Cell? = null
)

data class SudokuReasoningTrace(
    val technique: SudokuTechnique,
    val targetCell: Cell,
    val targetDigit: Int,
    val candidateCells: List<Cell>,
    val steps:
        List<SudokuReasoningStep>
) {
    val sourceCells:
        Set<Cell>
        get() =
            steps.mapNotNull {
                it.sourceCell
            }.toSet()

    val detailedExplanation:
        String
        get() =
            steps.joinToString(
                separator = "\n\n"
            ) {
                it.narration
            }
}

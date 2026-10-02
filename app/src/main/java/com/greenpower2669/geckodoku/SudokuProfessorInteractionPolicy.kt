package com.greenpower2669.geckodoku

data class PendingProfessorSudokuMove(
    val fingerprint: Int,
    val cell: Cell,
    val digit: Int,
    val technique: SudokuTechnique
)

sealed class SudokuProfessorDecision {
    data class Explain(
        val hint: SudokuHint,
        val pending:
            PendingProfessorSudokuMove
    ) : SudokuProfessorDecision()

    data class Apply(
        val hint: SudokuHint
    ) : SudokuProfessorDecision()

    data object NoHint :
        SudokuProfessorDecision()
}

class SudokuProfessorInteractionPolicy {
    private var pending:
        PendingProfessorSudokuMove? =
        null

    fun onTap(
        puzzle: SudokuPuzzle,
        snapshot: SudokuSnapshot
    ): SudokuProfessorDecision {
        val current =
            pending

        if (
            current != null &&
            current.fingerprint ==
                fingerprint(snapshot)
        ) {
            val fresh =
                SudokuHintEngine
                    .nextHint(
                        puzzle,
                        snapshot
                    )

            if (
                fresh != null &&
                fresh.cell ==
                    current.cell &&
                fresh.digit ==
                    current.digit &&
                fresh.technique ==
                    current.technique
            ) {
                pending = null

                return SudokuProfessorDecision
                    .Apply(fresh)
            }
        }

        val hint =
            SudokuHintEngine
                .nextHint(
                    puzzle,
                    snapshot
                )
                ?: run {
                    pending = null
                    return SudokuProfessorDecision
                        .NoHint
                }

        val next =
            PendingProfessorSudokuMove(
                fingerprint =
                    fingerprint(snapshot),
                cell = hint.cell,
                digit = hint.digit,
                technique =
                    hint.technique
            )

        pending = next

        return SudokuProfessorDecision
            .Explain(
                hint = hint,
                pending = next
            )
    }

    fun onLongPress(
        puzzle: SudokuPuzzle,
        snapshot: SudokuSnapshot
    ): SudokuProfessorDecision {
        pending = null

        val hint =
            SudokuHintEngine
                .nextHint(
                    puzzle,
                    snapshot
                )
                ?: return SudokuProfessorDecision
                    .NoHint

        return SudokuProfessorDecision
            .Apply(hint)
    }

    fun invalidate() {
        pending = null
    }

    private fun fingerprint(
        snapshot: SudokuSnapshot
    ): Int {
        var result =
            snapshot.values
                .contentHashCode()

        for (
            cellNotes in
                snapshot.notes
        ) {
            result =
                result * 31 +
                    cellNotes
                        .sorted()
                        .hashCode()
        }

        result =
            result * 31 +
                snapshot.mistakes

        return result
    }
}

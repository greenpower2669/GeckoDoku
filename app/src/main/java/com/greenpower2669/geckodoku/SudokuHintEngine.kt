package com.greenpower2669.geckodoku

object SudokuHintEngine {
    fun nextHint(
        puzzle: SudokuPuzzle,
        snapshot: SudokuSnapshot
    ): SudokuHint? {
        val values =
            snapshot.values

        for (
            index in
                values.indices
        ) {
            if (values[index] != 0) {
                continue
            }

            val candidates =
                SudokuSolver
                    .digitsFromMask(
                        SudokuSolver
                            .candidateMask(
                                values,
                                index
                            )
                    )

            if (
                candidates.size == 1
            ) {
                return SudokuHint(
                    technique =
                        SudokuTechnique
                            .NAKED_SINGLE,
                    cell =
                        Cell(
                            index / 9,
                            index % 9
                        ),
                    digit =
                        candidates.single(),
                    explanation =
                        "Cette case n'a plus qu'un seul candidat possible. Le " +
                            candidates.single() +
                            " est donc certain."
                )
            }
        }

        hiddenSingle(
            values,
            rowUnits(),
            SudokuTechnique
                .HIDDEN_SINGLE_ROW,
            "cette ligne"
        )?.let {
            return it
        }

        hiddenSingle(
            values,
            columnUnits(),
            SudokuTechnique
                .HIDDEN_SINGLE_COLUMN,
            "cette colonne"
        )?.let {
            return it
        }

        hiddenSingle(
            values,
            boxUnits(),
            SudokuTechnique
                .HIDDEN_SINGLE_BOX,
            "ce bloc 3 par 3"
        )?.let {
            return it
        }

        return null
    }

    private fun hiddenSingle(
        values: IntArray,
        units: List<IntArray>,
        technique: SudokuTechnique,
        unitLabel: String
    ): SudokuHint? {
        for (unit in units) {
            for (digit in 1..9) {
                var found = -1
                var count = 0

                for (index in unit) {
                    if (
                        values[index] != 0
                    ) {
                        continue
                    }

                    val mask =
                        SudokuSolver
                            .candidateMask(
                                values,
                                index
                            )

                    if (
                        mask and
                            (1 shl digit) !=
                            0
                    ) {
                        found = index
                        count += 1
                    }
                }

                if (
                    count == 1 &&
                    found >= 0
                ) {
                    return SudokuHint(
                        technique =
                            technique,
                        cell =
                            Cell(
                                found / 9,
                                found % 9
                            ),
                        digit =
                            digit,
                        explanation =
                            "Dans " +
                                unitLabel +
                                ", le " +
                                digit +
                                " ne peut aller qu'à cet endroit."
                    )
                }
            }
        }

        return null
    }

    private fun rowUnits():
        List<IntArray> =
        (0..8).map {
            row ->

            IntArray(9) {
                col ->

                row * 9 +
                    col
            }
        }

    private fun columnUnits():
        List<IntArray> =
        (0..8).map {
            col ->

            IntArray(9) {
                row ->

                row * 9 +
                    col
            }
        }

    private fun boxUnits():
        List<IntArray> =
        buildList {
            for (br in 0..2) {
                for (bc in 0..2) {
                    add(
                        IntArray(9) {
                            offset ->

                            val dr =
                                offset / 3

                            val dc =
                                offset % 3

                            (
                                br * 3 +
                                    dr
                                ) * 9 +
                                bc * 3 +
                                dc
                        }
                    )
                }
            }
        }
}

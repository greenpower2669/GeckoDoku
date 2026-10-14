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
                        "On regarde cette case tranquillement. Après les repères de ligne, colonne et bloc, il ne reste que le " +
                            candidates.single() +
                            ".",
                    reasoning =
                        nakedSingleTrace(
                            values = values,
                            index = index,
                            targetDigit =
                                candidates.single()
                        )
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
                            "Regarde " +
                                unitLabel +
                                ". Pour le " +
                                digit +
                                ", toutes les autres places sont écartées : il reste celle-ci.",
                        reasoning =
                            hiddenSingleTrace(
                                values = values,
                                unit = unit,
                                found = found,
                                digit = digit,
                                technique =
                                    technique,
                                unitLabel =
                                    unitLabel
                            )
                    )
                }
            }
        }

        return null
    }

    private fun nakedSingleTrace(
        values: IntArray,
        index: Int,
        targetDigit: Int
    ): SudokuReasoningTrace {
        val target =
            Cell(
                index / 9,
                index % 9
            )

        val eliminations =
            (1..9)
                .filter {
                    it != targetDigit
                }
                .mapNotNull {
                    digit ->
                    conflictFor(
                        values,
                        index,
                        digit
                    )?.let {
                        (source, projection) ->
                        SudokuReasoningElimination(
                            cell = target,
                            digit = digit,
                            sourceCell =
                                source,
                            projection =
                                projection
                        )
                    }
                }

        val steps =
            listOf(
                SudokuProjectionType.ROW,
                SudokuProjectionType.COLUMN,
                SudokuProjectionType.BOX
            )
                .mapNotNull {
                    projection ->

                    val blocked =
                        eliminations
                            .filter {
                                it.projection ==
                                    projection
                            }

                    if (blocked.isEmpty()) {
                        return@mapNotNull null
                    }

                    val source =
                        blocked.first()
                            .sourceCell

                    val digits =
                        blocked
                            .map {
                                it.digit
                            }
                            .distinct()
                            .sorted()
                            .joinToString(
                                ", "
                            )

                    val narration =
                        when (projection) {
                            SudokuProjectionType.ROW ->
                                "On commence par la ligne. Je la projette doucement : elle écarte " +
                                    digits +
                                    " de cette case."

                            SudokuProjectionType.COLUMN ->
                                "Puis la colonne. Même repère : elle écarte " +
                                    digits +
                                    "."

                            SudokuProjectionType.BOX ->
                                "Enfin le bloc trois par trois. Il écarte " +
                                    digits +
                                    "."
                        }

                    SudokuReasoningStep(
                        sourceCell =
                            source,
                        projection =
                            projection,
                        eliminations =
                            blocked,
                        narration =
                            narration
                    )
                } +
                SudokuReasoningStep(
                    sourceCell = null,
                    projection = null,
                    eliminations =
                        emptyList(),
                    narration =
                        "Voilà. Il ne reste que le " +
                            targetDigit +
                            " ici. Tu peux le poser tranquillement.",
                    finalCell = target
                )

        return SudokuReasoningTrace(
            technique =
                SudokuTechnique
                    .NAKED_SINGLE,
            targetCell = target,
            targetDigit =
                targetDigit,
            candidateCells =
                listOf(target),
            steps = steps
        )
    }

    private fun hiddenSingleTrace(
        values: IntArray,
        unit: IntArray,
        found: Int,
        digit: Int,
        technique:
            SudokuTechnique,
        unitLabel: String
    ): SudokuReasoningTrace {
        val target =
            Cell(
                found / 9,
                found % 9
            )

        val initialCandidates =
            unit.filter {
                index ->
                values[index] == 0
            }.map {
                Cell(
                    it / 9,
                    it % 9
                )
            }

        val eliminated =
            unit.filter {
                it != found &&
                    values[it] == 0
            }.mapNotNull {
                index ->
                conflictFor(
                    values,
                    index,
                    digit
                )?.let {
                    (source, projection) ->
                    SudokuReasoningElimination(
                        cell =
                            Cell(
                                index / 9,
                                index % 9
                            ),
                        digit = digit,
                        sourceCell =
                            source,
                        projection =
                            projection
                    )
                }
            }

        val steps =
            listOf(
                SudokuProjectionType.ROW,
                SudokuProjectionType.COLUMN,
                SudokuProjectionType.BOX
            )
                .mapNotNull {
                    projection ->

                    val blocked =
                        eliminated
                            .filter {
                                it.projection ==
                                    projection
                            }

                    if (blocked.isEmpty()) {
                        return@mapNotNull null
                    }

                    val source =
                        blocked.first()
                            .sourceCell

                    val narration =
                        when (projection) {
                            SudokuProjectionType.ROW ->
                                "On garde le " +
                                    digit +
                                    " en vue. La projection de ligne écarte " +
                                    blocked.size +
                                    " place" +
                                    if (
                                        blocked.size >
                                            1
                                    ) {
                                        "s."
                                    } else {
                                        "."
                                    }

                            SudokuProjectionType.COLUMN ->
                                "Ensuite la colonne. Elle enlève encore " +
                                    blocked.size +
                                    " possibilité" +
                                    if (
                                        blocked.size >
                                            1
                                    ) {
                                        "s."
                                    } else {
                                        "."
                                    }

                            SudokuProjectionType.BOX ->
                                "Puis le bloc trois par trois. Il enlève " +
                                    blocked.size +
                                    " possibilité" +
                                    if (
                                        blocked.size >
                                            1
                                    ) {
                                        "s."
                                    } else {
                                        "."
                                    }
                        }

                    SudokuReasoningStep(
                        sourceCell =
                            source,
                        projection =
                            projection,
                        eliminations =
                            blocked,
                        narration =
                            narration
                    )
                } +
                SudokuReasoningStep(
                    sourceCell = null,
                    projection = null,
                    eliminations =
                        emptyList(),
                    narration =
                        "Regarde maintenant " +
                            unitLabel +
                            " : pour le " +
                            digit +
                            ", il ne reste qu'une place. C'est celle-ci.",
                    finalCell = target
                )

        return SudokuReasoningTrace(
            technique = technique,
            targetCell = target,
            targetDigit = digit,
            candidateCells =
                initialCandidates,
            steps = steps
        )
    }

    private fun conflictFor(
        values: IntArray,
        index: Int,
        digit: Int
    ): Pair<
        Cell,
        SudokuProjectionType
        >? {
        val row = index / 9
        val col = index % 9

        for (c in 0..8) {
            val source =
                row * 9 + c

            if (
                source != index &&
                values[source] ==
                    digit
            ) {
                return Cell(
                    row,
                    c
                ) to
                    SudokuProjectionType
                        .ROW
            }
        }

        for (r in 0..8) {
            val source =
                r * 9 + col

            if (
                source != index &&
                values[source] ==
                    digit
            ) {
                return Cell(
                    r,
                    col
                ) to
                    SudokuProjectionType
                        .COLUMN
            }
        }

        val boxRow =
            row / 3 * 3
        val boxCol =
            col / 3 * 3

        for (dr in 0..2) {
            for (dc in 0..2) {
                val r =
                    boxRow + dr
                val c =
                    boxCol + dc
                val source =
                    r * 9 + c

                if (
                    source != index &&
                    values[source] ==
                        digit
                ) {
                    return Cell(
                        r,
                        c
                    ) to
                        SudokuProjectionType
                            .BOX
                }
            }
        }

        return null
    }

    private fun projectionText(
        projection:
            SudokuProjectionType
    ): String =
        when (projection) {
            SudokuProjectionType.ROW ->
                "Il se projette sur cette ligne."

            SudokuProjectionType.COLUMN ->
                "Il se projette sur cette colonne."

            SudokuProjectionType.BOX ->
                "Il verrouille aussi ce bloc 3 par 3."
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

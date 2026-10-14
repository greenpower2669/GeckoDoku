package com.greenpower2669.geckodoku

/** Annotations du joueur, distinctes des notes logiques et du solveur. */
data class SudokuCandidateRef(
    val cell: Cell,
    val digit: Int
) {
    init {
        require(digit in 1..9)
    }
}

data class SudokuVisualLink(
    val first: SudokuCandidateRef,
    val second: SudokuCandidateRef
) {
    init {
        require(first.digit == second.digit)
        require(first.cell != second.cell)
    }
}

data class SudokuNakedGroup(
    val cells: Set<Cell>,
    val digits: Set<Int>,
    val unit: String
)

/**
 * Un candidat "binaire" existe exactement deux fois dans une unité Sudoku
 * (ligne, colonne ou bloc 3x3). Une même paire peut appartenir à plusieurs
 * unités ; elle n'est dessinée qu'une fois mais conserve ses provenances.
 */
data class SudokuBinaryGroup(
    val digit: Int,
    val cells: Set<Cell>,
    val units: Set<String>
) {
    init {
        require(digit in 1..9)
        require(cells.size == 2)
        require(units.isNotEmpty())
    }
}

private data class SudokuBinaryAccumulator(
    val digit: Int,
    val cells: Set<Cell>,
    val units:
        LinkedHashSet<String>
)

/** Calcul de tracés et de motifs logiques sans élimination automatique. */
object SudokuVisualReasoning {
    /**
     * Candidats réellement possibles à partir des valeurs confirmées.
     *
     * Cette carte est purement calculée : elle ne modifie jamais les notes
     * personnelles du joueur et ne dépend pas des suggestions temporaires du
     * Professeur.
     */
    fun logicalCandidates(
        values: IntArray
    ): Map<Cell, Set<Int>> {
        require(
            values.size ==
                SudokuPuzzle.CELL_COUNT
        )

        return buildMap {
            for (index in values.indices) {
                if (values[index] != 0) {
                    continue
                }

                val digits =
                    SudokuSolver
                        .digitsFromMask(
                            SudokuSolver
                                .candidateMask(
                                    values,
                                    index
                                )
                        )
                        .toSet()

                if (digits.isNotEmpty()) {
                    put(
                        Cell(
                            index /
                                SudokuPuzzle.SIZE,
                            index %
                                SudokuPuzzle.SIZE
                        ),
                        digits
                    )
                }
            }
        }
    }

    fun projectionSegments(
        notes: Map<Cell, Set<Int>>
    ): List<SudokuVisualLink> =
        buildList {
            for (row in 0..8) {
                for (digit in 1..9) {
                    val cells =
                        notes.entries
                            .filter {
                                (cell, digits) ->
                                cell.row ==
                                    row &&
                                    digit in
                                        digits
                            }
                            .map {
                                it.key
                            }
                            .sortedBy {
                                it.col
                            }

                    for (
                        index in
                            0 until
                                cells.lastIndex
                    ) {
                        add(
                            SudokuVisualLink(
                                SudokuCandidateRef(
                                    cells[index],
                                    digit
                                ),
                                SudokuCandidateRef(
                                    cells[index + 1],
                                    digit
                                )
                            )
                        )
                    }
                }
            }
        }

    /**
     * Paires et triplets NUS potentiels dans UNE même unité.
     * Une note manquante ne prouve jamais une élimination : uniquement un
     * repère pédagogique, aucune mutation de plateau.
     */
    fun nakedGroups(
        notes: Map<Cell, Set<Int>>,
        groupSize: Int
    ): List<SudokuNakedGroup> {
        require(
            groupSize in
                2..3
        )

        val result =
            linkedMapOf<
                String,
                SudokuNakedGroup
                >()

        for (
            (unitName, cells) in
                units()
        ) {
            val options =
                cells.filter {
                    cell ->

                    val digits =
                        notes[cell]
                            ?: emptySet()

                    digits.size in
                        2..groupSize &&
                        digits.all {
                            it in 1..9
                        }
                }

            // 9 cellules au maximum ; 2/3 combinaisons, coût constant.
            for (i in options.indices) {
                for (
                    j in
                        i + 1 until
                            options.size
                ) {
                    if (groupSize == 2) {
                        addGroup(
                            cells =
                                listOf(
                                    options[i],
                                    options[j]
                                ),
                            notes = notes,
                            unit =
                                unitName,
                            into =
                                result
                        )
                    } else {
                        for (
                            k in
                                j + 1 until
                                    options.size
                        ) {
                            addGroup(
                                cells =
                                    listOf(
                                        options[i],
                                        options[j],
                                        options[k]
                                    ),
                                notes = notes,
                                unit =
                                    unitName,
                                into =
                                    result
                            )
                        }
                    }
                }
            }
        }

        return result.values
            .toList()
    }

    /**
     * Renvoie toutes les paires conjuguées : pour un chiffre donné, exactement
     * deux cellules candidates dans une ligne, colonne ou bloc.
     *
     * Si un même couple est binaire dans plusieurs unités (par exemple une
     * ligne ET son bloc), les unités sont fusionnées afin d'éviter des cercles
     * superposés.
     */
    fun binaryGroups(
        candidates:
            Map<Cell, Set<Int>>
    ): List<SudokuBinaryGroup> {
        val grouped =
            linkedMapOf<
                String,
                SudokuBinaryAccumulator
                >()

        for (
            (unitName, cells) in
                units()
        ) {
            for (digit in 1..9) {
                val occurrences =
                    cells.filter {
                        cell ->
                        digit in
                            candidates[
                                cell
                            ].orEmpty()
                    }

                if (
                    occurrences.size !=
                        2
                ) {
                    continue
                }

                val sorted =
                    occurrences
                        .sortedWith(
                            compareBy(
                                {
                                    it.row
                                },
                                {
                                    it.col
                                }
                            )
                        )

                val key =
                    buildString {
                        append(
                            digit
                        )
                        append(':')
                        sorted.forEach {
                            append(
                                it.row
                            )
                            append(',')
                            append(
                                it.col
                            )
                            append('|')
                        }
                    }

                val existing =
                    grouped[key]

                if (existing == null) {
                    grouped[key] =
                        SudokuBinaryAccumulator(
                            digit =
                                digit,
                            cells =
                                sorted
                                    .toSet(),
                            units =
                                linkedSetOf(
                                    unitName
                                )
                        )
                } else {
                    existing.units
                        .add(
                            unitName
                        )
                }
            }
        }

        return grouped.values
            .map {
                SudokuBinaryGroup(
                    digit =
                        it.digit,
                    cells =
                        it.cells,
                    units =
                        it.units
                            .toSet()
                )
            }
    }

    private fun addGroup(
        cells: List<Cell>,
        notes: Map<Cell, Set<Int>>,
        unit: String,
        into:
            MutableMap<
                String,
                SudokuNakedGroup
                >
    ) {
        val digits =
            cells.flatMap {
                notes[it].orEmpty()
            }.toSet()

        if (
            digits.size !=
                cells.size
        ) {
            return
        }

        // Identité de cellules et chiffres, sans confusion entre unités.
        val key =
            cells.sortedWith(
                compareBy(
                    {
                        it.row
                    },
                    {
                        it.col
                    }
                )
            )
                .joinToString(
                    "|"
                ) {
                    "${it.row},${it.col}"
                } +
                ":" +
                digits.sorted()
                    .joinToString(
                        ","
                    )

        into.putIfAbsent(
            key,
            SudokuNakedGroup(
                cells =
                    cells.toSet(),
                digits =
                    digits,
                unit =
                    unit
            )
        )
    }

    fun isGenuineXWing(
        notes: Map<Cell, Set<Int>>,
        digit: Int,
        rows: Pair<Int, Int>,
        columns: Pair<Int, Int>
    ): Boolean {
        if (
            digit !in 1..9 ||
            rows.first ==
                rows.second ||
            columns.first ==
                columns.second ||
            listOf(
                rows.first,
                rows.second,
                columns.first,
                columns.second
            ).any {
                it !in 0..8
            }
        ) {
            return false
        }

        val horizontal =
            listOf(
                rows.first,
                rows.second
            ).all {
                row ->

                val cols =
                    (0..8).filter {
                        col ->
                        digit in
                            notes[
                                Cell(
                                    row,
                                    col
                                )
                            ].orEmpty()
                    }

                cols.toSet() ==
                    setOf(
                        columns.first,
                        columns.second
                    )
            }

        val vertical =
            listOf(
                columns.first,
                columns.second
            ).all {
                col ->

                val rr =
                    (0..8).filter {
                        row ->
                        digit in
                            notes[
                                Cell(
                                    row,
                                    col
                                )
                            ].orEmpty()
                    }

                rr.toSet() ==
                    setOf(
                        rows.first,
                        rows.second
                    )
            }

        return horizontal ||
            vertical
    }

    private fun units():
        List<
            Pair<
                String,
                List<Cell>
                >
            > =
        buildList {
            for (row in 0..8) {
                add(
                    "Ligne ${row + 1}" to
                        (0..8).map {
                            Cell(
                                row,
                                it
                            )
                        }
                )
            }

            for (col in 0..8) {
                add(
                    "Colonne ${col + 1}" to
                        (0..8).map {
                            Cell(
                                it,
                                col
                            )
                        }
                )
            }

            for (br in 0..2) {
                for (bc in 0..2) {
                    add(
                        "Bloc ${br * 3 + bc + 1}" to
                            buildList {
                                for (
                                    row in
                                        br * 3 until
                                            br * 3 +
                                            3
                                ) {
                                    for (
                                        col in
                                            bc * 3 until
                                                bc * 3 +
                                                3
                                    ) {
                                        add(
                                            Cell(
                                                row,
                                                col
                                            )
                                        )
                                    }
                                }
                            }
                    )
                }
            }
        }
}

package com.greenpower2669.geckodoku

import org.junit.Assert.*
import org.junit.Test

class SudokuVisualReasoningTest {
    @Test fun projectionUsesExactlyTwoCandidatesInRowOrColumn() {
        val candidates =
            mutableMapOf(
                Cell(0, 1) to
                    setOf(
                        1
                    ),
                Cell(0, 7) to
                    setOf(
                        1
                    ),
                Cell(2, 4) to
                    setOf(
                        2
                    ),
                Cell(8, 4) to
                    setOf(
                        2
                    )
            )

        val projections =
            SudokuVisualReasoning
                .projectionSegments(
                    candidates
                )

        assertTrue(
            projections.any {
                it.first.digit ==
                    1 &&
                    it.first.cell.row ==
                    0 &&
                    it.second.cell.row ==
                    0
            }
        )

        assertTrue(
            projections.any {
                it.first.digit ==
                    2 &&
                    it.first.cell.col ==
                    4 &&
                    it.second.cell.col ==
                    4
            }
        )

        // Une troisième possibilité annule la projection de cet axe.
        candidates[
            Cell(
                0,
                5
            )
        ] =
            setOf(
                1
            )

        assertFalse(
            SudokuVisualReasoning
                .projectionSegments(
                    candidates
                )
                .any {
                    it.first.digit ==
                        1 &&
                    it.first.cell.row ==
                        0 &&
                    it.second.cell.row ==
                        0
                }
        )
    }

    @Test fun pairRequiresSharedUnitAndTwoCells() {
        val notes = mapOf(
            Cell(0, 0) to setOf(1, 3),
            Cell(0, 5) to setOf(1, 3),
            Cell(4, 8) to setOf(1, 3)
        )
        val result = SudokuVisualReasoning.nakedGroups(notes, 2)
        assertTrue(result.any { it.cells == setOf(Cell(0, 0), Cell(0, 5)) })
        assertFalse(result.any { it.cells == setOf(Cell(0, 5), Cell(4, 8)) })
    }

    @Test fun tripletsCanUseThreePairsInOneBlock() {
        val notes = mapOf(
            Cell(0, 0) to setOf(1, 2),
            Cell(1, 1) to setOf(1, 3),
            Cell(2, 2) to setOf(2, 3)
        )
        assertTrue(
            SudokuVisualReasoning.nakedGroups(notes, 3).any {
                it.cells == notes.keys && it.digits == setOf(1, 2, 3)
            }
        )
    }

    @Test fun xWingNeedsTwoExclusiveColumnsOnEachRow() {
        val notes = mutableMapOf(
            Cell(0, 2) to setOf(1), Cell(0, 7) to setOf(1),
            Cell(5, 2) to setOf(1), Cell(5, 7) to setOf(1)
        )
        assertTrue(SudokuVisualReasoning.isGenuineXWing(notes, 1, 0 to 5, 2 to 7))
        notes[Cell(0, 4)] = setOf(1)
        // Pour invalider aussi le X-Wing par colonnes, une troisième
        // ligne doit contenir le même candidat dans la colonne 2.
        notes[Cell(1, 2)] = setOf(1)
        assertFalse(SudokuVisualReasoning.isGenuineXWing(notes, 1, 0 to 5, 2 to 7))
    }

    @Test fun logicalCandidatesComeFromConfirmedGridWithoutMutatingNotes() {
        val values =
            IntArray(
                81
            )

        // Ligne 1 contient déjà 1..8 : la dernière case ne peut être que 9.
        for (col in 0..7) {
            values[col] =
                col + 1
        }

        val candidates =
            SudokuVisualReasoning
                .logicalCandidates(
                    values
                )

        assertEquals(
            setOf(
                9
            ),
            candidates[
                Cell(
                    0,
                    8
                )
            ]
        )
    }

    @Test fun binaryRequiresExactlyTwoOccurrencesInsideOneUnit() {
        val candidates =
            mutableMapOf<Cell, Set<Int>>(
                Cell(0, 1) to
                    setOf(
                        4
                    ),
                Cell(0, 7) to
                    setOf(
                        4
                    ),
                Cell(4, 1) to
                    setOf(
                        4
                    )
            )

        val first =
            SudokuVisualReasoning
                .binaryGroups(
                    candidates
                )

        assertTrue(
            first.any {
                it.digit ==
                    4 &&
                    it.cells ==
                    setOf(
                        Cell(
                            0,
                            1
                        ),
                        Cell(
                            0,
                            7
                        )
                    ) &&
                    it.units.any {
                        unit ->
                        unit.startsWith(
                            "Ligne"
                        )
                    }
            }
        )

        // Une troisième occurrence dans la même ligne annule le binaire
        // de cette ligne.
        candidates[
            Cell(
                0,
                4
            )
        ] =
            setOf(
                4
            )

        val second =
            SudokuVisualReasoning
                .binaryGroups(
                    candidates
                )

        assertFalse(
            second.any {
                it.digit ==
                    4 &&
                    it.cells ==
                    setOf(
                        Cell(
                            0,
                            1
                        ),
                        Cell(
                            0,
                            7
                        )
                    ) &&
                    it.units.any {
                        unit ->
                        unit.startsWith(
                            "Ligne"
                        )
                    }
            }
        )
    }

    @Test fun binaryMergesSamePairWhenItQualifiesInSeveralUnits() {
        val candidates =
            mapOf(
                Cell(0, 0) to
                    setOf(
                        7
                    ),
                Cell(0, 2) to
                    setOf(
                        7
                    )
            )

        val groups =
            SudokuVisualReasoning
                .binaryGroups(
                    candidates
                )
                .filter {
                    it.digit ==
                        7 &&
                    it.cells ==
                        setOf(
                            Cell(
                                0,
                                0
                            ),
                            Cell(
                                0,
                                2
                            )
                        )
                }

        assertEquals(
            1,
            groups.size
        )

        assertTrue(
            groups.single()
                .units.any {
                    it.startsWith(
                        "Ligne"
                    )
                }
        )

        assertTrue(
            groups.single()
                .units.any {
                    it.startsWith(
                        "Bloc"
                    )
                }
        )
    }

}

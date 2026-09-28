package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BeeGeckoLogicTest {
    private fun puzzle(
        givenGeckos:
            Set<HexCoord> =
            setOf(
                HexCoord(-1, 0),
                HexCoord(0, 1)
            ),
        givenBees:
            Set<HexCoord> =
            setOf(
                HexCoord(-1, 1)
            )
    ): BeeGeckoPuzzle {
        val g0 =
            HexCoord(-1, 0)
        val b0 =
            HexCoord(-1, 1)
        val g1 =
            HexCoord(0, 1)
        val b1 =
            HexCoord(1, 0)

        val regions =
            mapOf(
                HexCoord(-1, 0) to 0,
                HexCoord(-1, 1) to 0,
                HexCoord(0, 0) to 0,
                HexCoord(0, 1) to 1,
                HexCoord(1, 0) to 1,
                HexCoord(1, -1) to 1,
                HexCoord(0, -1) to 1
            )

        return BeeGeckoPuzzle(
            id = "test",
            radius = 1,
            regions = regions,
            solutionGeckos =
                setOf(
                    g0,
                    g1
                ),
            solutionBees =
                setOf(
                    b0,
                    b1
                ),
            solutionPairs =
                listOf(
                    BeeGeckoPair(
                        gecko = g0,
                        bee = b0,
                        region = 0
                    ),
                    BeeGeckoPair(
                        gecko = g1,
                        bee = b1,
                        region = 1
                    )
                ),
            givenGeckos =
                givenGeckos,
            givenBees =
                givenBees,
            difficulty =
                GameDifficulty
                    .DISCOVERY,
            seed = 1L
        )
    }

    @Test
    fun hexCellHasSixLogicalNeighbors() {
        assertEquals(
            6,
            HexCoord(
                0,
                0
            )
                .neighbors()
                .distinct()
                .size
        )
    }

    @Test
    fun completeSolutionRespectsZonesAxesAndLocalAdjacency() {
        val puzzle =
            puzzle()

        assertTrue(
            BeeGeckoRules
                .validateComplete(
                    puzzle,
                    puzzle.solutionGeckos,
                    puzzle.solutionBees
                )
        )

        puzzle.solutionPairs
            .forEach {
                pair ->
                assertEquals(
                    puzzle.regionAt(
                        pair.gecko
                    ),
                    puzzle.regionAt(
                        pair.bee
                    )
                )

                assertTrue(
                    BeeGeckoRules
                        .areNeighbors(
                            pair.gecko,
                            pair.bee
                        )
                )
            }

        HexAxis.entries
            .forEach {
                axis ->
                assertTrue(
                    puzzle.solutionGeckos
                        .map {
                            it.axisValue(
                                axis
                            )
                        }
                        .distinct()
                        .size ==
                        puzzle
                            .solutionGeckos
                            .size
                )

                assertTrue(
                    puzzle.solutionBees
                        .map {
                            it.axisValue(
                                axis
                            )
                        }
                        .distinct()
                        .size ==
                        puzzle
                            .solutionBees
                            .size
                )
            }
    }

    @Test
    fun wrongPieceCostsOneMistakeAndLeavesCross() {
        val puzzle =
            puzzle(
                givenGeckos =
                    emptySet(),
                givenBees =
                    emptySet()
            )

        val engine =
            BeeGeckoGameEngine(
                puzzle
            )

        val wrongCell =
            HexCoord(
                0,
                0
            )

        assertEquals(
            BeeGeckoActionFeedback
                .WRONG_PIECE,
            engine.placePiece(
                wrongCell,
                BeeGeckoPiece
                    .GECKO
            )
        )

        val snapshot =
            engine.snapshot()

        assertEquals(
            1,
            snapshot.mistakes
        )

        assertTrue(
            wrongCell in
                snapshot
                    .manualCrosses
        )
    }

    @Test
    fun solverExplainsForcedLocalBee() {
        val puzzle =
            puzzle()

        val engine =
            BeeGeckoGameEngine(
                puzzle
            )

        val hint =
            BeeGeckoSolver
                .nextHint(
                    engine.snapshot()
                )

        assertNotNull(hint)
        assertTrue(
            hint!!.green
                .isNotEmpty()
        )
        assertTrue(
            hint.message
                .contains(
                    "zone",
                    ignoreCase = true
                )
        )
    }

    @Test
    fun solutionCounterRejectsAmbiguityAndAcceptsForcedGrid() {
        val puzzle =
            puzzle()

        assertEquals(
            1,
            BeeGeckoSolver
                .countSolutions(
                    puzzle,
                    limit = 2
                )
        )
    }

    @Test
    fun generatedDiscoveryPuzzleKeepsCanonicalRules() {
        val generated =
            BeeGeckoGenerator
                .generate(
                    requested =
                        GameDifficulty
                            .DISCOVERY,
                    seed = 42L
                )

        assertTrue(
            BeeGeckoRules
                .validateComplete(
                    generated,
                    generated
                        .solutionGeckos,
                    generated
                        .solutionBees
                )
        )

        assertEquals(
            generated.regionCount,
            generated
                .solutionPairs
                .size
        )

        assertTrue(
            BeeGeckoSolver
                .countSolutions(
                    generated,
                    limit = 2
                ) <= 1
        )
    }

    @Test
    fun aDistantBeeIsNeverAValidPartner() {
        assertFalse(
            BeeGeckoRules
                .areNeighbors(
                    HexCoord(-1, 0),
                    HexCoord(1, 0)
                )
        )
    }
}

package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BeeGeckoLogicTest {
    private fun simplePuzzle():
        BeeGeckoPuzzle =
        BeeGeckoPuzzle(
            id = "test",
            columns = 5,
            rows = 5,
            pieces =
                mapOf(
                    HexCoord(1, 1) to
                        BeeGeckoPiece.GECKO,
                    HexCoord(2, 1) to
                        BeeGeckoPiece.BEE
                ),
            difficulty =
                GameDifficulty.DISCOVERY
        )

    @Test
    fun hexCellHasSixLogicalNeighbors() {
        assertEquals(
            6,
            HexCoord(
                4,
                4
            )
                .neighbors()
                .distinct()
                .size
        )
    }

    @Test
    fun uniquePairIsValid() {
        val puzzle =
            simplePuzzle()

        val pair =
            BeeGeckoPair(
                HexCoord(1, 1),
                HexCoord(2, 1)
            )

        assertTrue(
            BeeGeckoRules
                .validatePairs(
                    puzzle,
                    setOf(pair)
                )
        )

        assertTrue(
            BeeGeckoRules
                .isComplete(
                    puzzle,
                    setOf(pair)
                )
        )
    }

    @Test
    fun geckoWithoutBeeCannotBeCompleted() {
        val puzzle =
            BeeGeckoPuzzle(
                id = "zero-bee",
                columns = 4,
                rows = 4,
                pieces =
                    mapOf(
                        HexCoord(1, 1) to
                            BeeGeckoPiece.GECKO
                    ),
                difficulty =
                    GameDifficulty.DISCOVERY
            )

        assertEquals(
            0,
            BeeGeckoSolver
                .countSolutions(
                    puzzle
                )
        )
    }

    @Test
    fun sameGeckoCannotUseTwoBees() {
        val puzzle =
            BeeGeckoPuzzle(
                id = "double-bee",
                columns = 5,
                rows = 5,
                pieces =
                    mapOf(
                        HexCoord(2, 2) to
                            BeeGeckoPiece.GECKO,
                        HexCoord(3, 2) to
                            BeeGeckoPiece.BEE,
                        HexCoord(2, 3) to
                            BeeGeckoPiece.BEE
                    ),
                difficulty =
                    GameDifficulty.EASY
            )

        val pairs =
            setOf(
                BeeGeckoPair(
                    HexCoord(2, 2),
                    HexCoord(3, 2)
                ),
                BeeGeckoPair(
                    HexCoord(2, 2),
                    HexCoord(2, 3)
                )
            )

        assertFalse(
            BeeGeckoRules
                .validatePairs(
                    puzzle,
                    pairs
                )
        )
    }

    @Test
    fun sameBeeCannotUseTwoGeckos() {
        val puzzle =
            BeeGeckoPuzzle(
                id = "double-gecko",
                columns = 5,
                rows = 5,
                pieces =
                    mapOf(
                        HexCoord(2, 2) to
                            BeeGeckoPiece.BEE,
                        HexCoord(3, 2) to
                            BeeGeckoPiece.GECKO,
                        HexCoord(2, 3) to
                            BeeGeckoPiece.GECKO
                    ),
                difficulty =
                    GameDifficulty.EASY
            )

        val pairs =
            setOf(
                BeeGeckoPair(
                    HexCoord(3, 2),
                    HexCoord(2, 2)
                ),
                BeeGeckoPair(
                    HexCoord(2, 3),
                    HexCoord(2, 2)
                )
            )

        assertFalse(
            BeeGeckoRules
                .validatePairs(
                    puzzle,
                    pairs
                )
        )
    }

    @Test
    fun solverPropagatesReservationBothWays() {
        val puzzle =
            BeeGeckoPuzzle(
                id = "chain",
                columns = 8,
                rows = 4,
                pieces =
                    mapOf(
                        HexCoord(1, 1) to
                            BeeGeckoPiece.GECKO,
                        HexCoord(2, 1) to
                            BeeGeckoPiece.BEE,
                        HexCoord(3, 1) to
                            BeeGeckoPiece.GECKO,
                        HexCoord(4, 1) to
                            BeeGeckoPiece.BEE
                    ),
                difficulty =
                    GameDifficulty.THINKING
            )

        val first =
            BeeGeckoSolver
                .nextHint(
                    BeeGeckoSnapshot(
                        puzzle,
                        emptySet(),
                        null
                    )
                )

        assertNotNull(first)

        val second =
            BeeGeckoSolver
                .nextHint(
                    BeeGeckoSnapshot(
                        puzzle,
                        setOf(
                            requireNotNull(
                                first
                            ).pair
                        ),
                        null
                    )
                )

        assertNotNull(second)
        assertTrue(
            first!!.pair !=
                second!!.pair
        )
    }

    @Test
    fun beeWithoutGeckoCannotBeCompleted() {
        val puzzle =
            BeeGeckoPuzzle(
                id = "zero-gecko",
                columns = 4,
                rows = 4,
                pieces =
                    mapOf(
                        HexCoord(1, 1) to
                            BeeGeckoPiece.BEE
                    ),
                difficulty =
                    GameDifficulty.DISCOVERY
            )

        assertEquals(
            0,
            BeeGeckoSolver
                .countSolutions(
                    puzzle
                )
        )
    }

    @Test
    fun forcedPairIsDetectedWhenOnlyOneCandidateRemains() {
        val puzzle =
            simplePuzzle()

        val hint =
            BeeGeckoSolver
                .nextHint(
                    BeeGeckoSnapshot(
                        puzzle,
                        emptySet(),
                        null
                    )
                )

        assertNotNull(hint)
        assertEquals(
            HexCoord(1, 1),
            hint!!.pair.gecko
        )
        assertEquals(
            HexCoord(2, 1),
            hint.pair.bee
        )
    }

    @Test
    fun reservedBeeDisappearsFromAnotherGeckosCandidates() {
        val puzzle =
            BeeGeckoPuzzle(
                id = "reserved",
                columns = 7,
                rows = 5,
                pieces =
                    mapOf(
                        HexCoord(1, 2) to
                            BeeGeckoPiece.GECKO,
                        HexCoord(2, 2) to
                            BeeGeckoPiece.BEE,
                        HexCoord(3, 2) to
                            BeeGeckoPiece.GECKO,
                        HexCoord(4, 2) to
                            BeeGeckoPiece.BEE
                    ),
                difficulty =
                    GameDifficulty.THINKING
            )

        val first =
            BeeGeckoPair(
                HexCoord(1, 2),
                HexCoord(2, 2)
            )

        val hint =
            BeeGeckoSolver
                .nextHint(
                    BeeGeckoSnapshot(
                        puzzle,
                        setOf(first),
                        null
                    )
                )

        assertNotNull(hint)
        assertEquals(
            HexCoord(3, 2),
            hint!!.pair.gecko
        )
        assertEquals(
            HexCoord(4, 2),
            hint.pair.bee
        )
    }

    @Test
    fun incompleteMatchingIsNotACompleteGrid() {
        val puzzle =
            BeeGeckoPuzzle(
                id = "incomplete",
                columns = 7,
                rows = 5,
                pieces =
                    mapOf(
                        HexCoord(1, 2) to
                            BeeGeckoPiece.GECKO,
                        HexCoord(2, 2) to
                            BeeGeckoPiece.BEE,
                        HexCoord(4, 2) to
                            BeeGeckoPiece.GECKO,
                        HexCoord(5, 2) to
                            BeeGeckoPiece.BEE
                    ),
                difficulty =
                    GameDifficulty.EASY
            )

        assertFalse(
            BeeGeckoRules
                .isComplete(
                    puzzle,
                    setOf(
                        BeeGeckoPair(
                            HexCoord(1, 2),
                            HexCoord(2, 2)
                        )
                    )
                )
        )
    }

    @Test
    fun generatedPuzzleHasUniqueMatching() {
        for (
            difficulty in
            GameDifficulty.entries
        ) {
            val puzzle =
                BeeGeckoGenerator
                    .generate(
                        difficulty,
                        seed = 42
                    )

            assertEquals(
                1,
                BeeGeckoSolver
                    .countSolutions(
                        puzzle,
                        limit = 2
                    )
            )
        }
    }

    @Test
    fun zoomKeepsFocusedWorldPointStable() {
        val before =
            BeeGeckoCamera(
                scale = 1f,
                offsetX = 20f,
                offsetY = 30f
            )

        val focusX = 240f
        val focusY = 320f

        val worldX =
            (
                focusX -
                    before.offsetX
                ) /
                before.scale

        val worldY =
            (
                focusY -
                    before.offsetY
                ) /
                before.scale

        val after =
            BeeGeckoViewportPolicy
                .zoom(
                    before,
                    factor = 1.7f,
                    focusX = focusX,
                    focusY = focusY
                )

        assertEquals(
            focusX,
            worldX *
                after.scale +
                after.offsetX,
            .001f
        )

        assertEquals(
            focusY,
            worldY *
                after.scale +
                after.offsetY,
            .001f
        )
    }

    @Test
    fun cameraClampKeepsBoardReachable() {
        val camera =
            BeeGeckoViewportPolicy
                .clamp(
                    camera =
                        BeeGeckoCamera(
                            scale = 1f,
                            offsetX =
                                -10_000f,
                            offsetY =
                                -10_000f
                        ),
                    viewWidth = 500f,
                    viewHeight = 800f,
                    content =
                        BeeGeckoBounds(
                            left = 0f,
                            top = 0f,
                            right = 1200f,
                            bottom = 1400f
                        ),
                    visibleMarginPx =
                        50f
                )

        assertTrue(
            camera.offsetX >
                -10_000f
        )
        assertTrue(
            camera.offsetY >
                -10_000f
        )
    }
}

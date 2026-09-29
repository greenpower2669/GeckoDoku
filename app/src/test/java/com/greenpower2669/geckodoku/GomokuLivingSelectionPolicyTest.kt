package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GomokuLivingSelectionPolicyTest {
    @Test
    fun capsEachTeamAtThree() {
        val stones =
            linkedMapOf<Cell, GomokuPlayer>().apply {
                repeat(6) {
                    put(
                        Cell(0, it),
                        GomokuPlayer.PLAYER
                    )
                    put(
                        Cell(1, it),
                        GomokuPlayer.PROFESSOR
                    )
                }
            }

        val selected =
            GomokuLivingSelectionPolicy.select(
                stones = stones,
                previous = emptySet(),
                redistribute = true,
                randomValue = 12
            )

        assertEquals(
            3,
            selected.count {
                stones[it] ==
                    GomokuPlayer.PLAYER
            }
        )
        assertEquals(
            3,
            selected.count {
                stones[it] ==
                    GomokuPlayer.PROFESSOR
            }
        )
    }

    @Test
    fun keepsSelectionStableBetweenGrandCycles() {
        val stones =
            (0 until 8)
                .associate {
                    Cell(0, it) to
                        GomokuPlayer.PLAYER
                }

        val first =
            GomokuLivingSelectionPolicy.select(
                stones = stones,
                previous = emptySet(),
                redistribute = true,
                randomValue = 3
            )

        val stable =
            GomokuLivingSelectionPolicy.select(
                stones = stones,
                previous = first,
                redistribute = false,
                randomValue = 99
            )

        assertEquals(
            first,
            stable
        )
    }

    @Test
    fun grandCycleRedistributesWhenAlternativesExist() {
        val stones =
            (0 until 8)
                .associate {
                    Cell(0, it) to
                        GomokuPlayer.PLAYER
                }

        val previous =
            linkedSetOf(
                Cell(0, 0),
                Cell(0, 1),
                Cell(0, 2)
            )

        val redistributed =
            GomokuLivingSelectionPolicy.select(
                stones = stones,
                previous = previous,
                redistribute = true,
                randomValue = 0
            )

        assertEquals(
            3,
            redistributed.size
        )
        assertNotEquals(
            previous,
            redistributed
        )
        assertTrue(
            redistributed.all {
                stones[it] ==
                    GomokuPlayer.PLAYER
            }
        )
    }

    @Test
    fun animatesAllPiecesUntilTeamHasThree() {
        val stones =
            linkedMapOf(
                Cell(0, 0) to
                    GomokuPlayer.PLAYER,
                Cell(0, 1) to
                    GomokuPlayer.PLAYER,
                Cell(1, 0) to
                    GomokuPlayer.PROFESSOR
            )

        val selected =
            GomokuLivingSelectionPolicy.select(
                stones = stones,
                previous = emptySet(),
                redistribute = true,
                randomValue = 5
            )

        assertEquals(
            stones.keys,
            selected
        )
    }
}

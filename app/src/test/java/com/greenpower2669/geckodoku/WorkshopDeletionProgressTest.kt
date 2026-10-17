package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkshopDeletionProgressTest {
    private val solution = setOf(
        Cell(0, 0), Cell(1, 2), Cell(2, 4), Cell(3, 1), Cell(4, 3)
    )

    @Test fun rejectRemovalRestoresTheGeckoAndAdvances() {
        val before = WorkshopDeletionProgress.start(5, 101L, 0, solution)
        val after = WorkshopDeletionProgress.afterTest(
            before, Cell(1, 2), unique = false
        )
        assertEquals(solution, after.kept)
        assertEquals(1, after.nextRemoval)
    }

    @Test fun uniqueRemovalKeepsItDeletedAndCanResume() {
        val before = WorkshopDeletionProgress.start(5, 101L, 0, solution)
        val after = WorkshopDeletionProgress.afterTest(
            before, Cell(1, 2), unique = true
        )
        assertEquals(solution - Cell(1, 2), after.kept)
        assertEquals(1, after.nextRemoval)
        assertEquals(101L, after.seed)
        assertEquals(0, after.attempt)
        val resumed = WorkshopDeletionProgress.afterTest(
            after, Cell(2, 4), unique = false
        )
        assertEquals(after.kept, resumed.kept)
        assertEquals(2, resumed.nextRemoval)
    }

    @Test fun checkpointForNextGeometryWithZeroCluesRoundTrips() {
        val next = WorkshopDeletionProgress(5, 42L, 4, 0, emptySet())
        assertEquals(next, WorkshopDeletionProgress.decode(next.encode()))
    }

    @Test fun progressionCanRoundTripAndRejectCorruption() {
        val state = WorkshopDeletionProgress.afterTest(
            WorkshopDeletionProgress.start(5, 42L, 3, solution),
            Cell(1, 2), unique = true
        )
        assertEquals(state, WorkshopDeletionProgress.decode(state.encode()))
        assertTrue(WorkshopDeletionProgress.decode("bad-data") == null)
    }
}

package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class ContinuousWorkshopStatusTest {
    @Test fun notEnabledIsAlwaysStopped() {
        assertEquals(ContinuousWorkshopStatus.STOPPED,
            ContinuousWorkshopStatus.from(false, true, 500L, 600L))
    }

    @Test fun pendingUserStartIsVisible() {
        assertEquals(ContinuousWorkshopStatus.STARTING,
            ContinuousWorkshopStatus.from(true, false, 0L, 3_000L))
    }

    @Test fun freshServiceHeartbeatShowsRunning() {
        assertEquals(ContinuousWorkshopStatus.RUNNING,
            ContinuousWorkshopStatus.from(true, true, 900L, 1_000L))
    }

    @Test fun deadOrStaleServiceIsNotMisrepresentedAsRunning() {
        assertEquals(ContinuousWorkshopStatus.INTERRUPTED,
            ContinuousWorkshopStatus.from(true, true, 100L, 140_100L))
        assertEquals(ContinuousWorkshopStatus.INTERRUPTED,
            ContinuousWorkshopStatus.from(true, false, 300L, 310L))
    }
}

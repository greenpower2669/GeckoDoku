package com.greenpower2669.geckodoku

import org.junit.Assert.assertTrue
import org.junit.Test

class MascotActivityPolicyTest {
    @Test
    fun diagnosticBuildAnimatesEveryVisiblePresenceWithoutPoolCap() {
        assertTrue(
            MascotActivityPolicy
                .ALL_VISIBLE_VIDEO_DIAGNOSTIC
        )
    }
}

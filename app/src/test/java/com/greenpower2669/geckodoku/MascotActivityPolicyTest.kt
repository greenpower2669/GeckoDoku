package com.greenpower2669.geckodoku

import org.junit.Assert.assertTrue
import org.junit.Test

class MascotActivityPolicyTest {
    @Test
    fun everyVisiblePresenceOwnsItsVideoWithoutSharedScheduler() {
        assertTrue(
            MascotActivityPolicy
                .PER_PRESENCE_VIDEO
        )
    }
}

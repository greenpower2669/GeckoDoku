package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Test

class CelebrationAudioPolicyTest {
    @Test
    fun visualCelebrationEndDoesNotStopMusic() {
        val policy = CelebrationAudioPolicy()

        assertFalse(
            policy.stopMusicWhenVisualCelebrationStops
        )
    }
}

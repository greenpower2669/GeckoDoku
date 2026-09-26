package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfessorVideoRecoveryPolicyTest {
    @Test
    fun mediaPlayerFailureAllowsRetryWithoutRecreatingRenderer() {
        assertFalse(
            ProfessorVideoRecoveryPolicy
                .requiresViewRecreation(
                    "MediaPlayer error 1/-1004 for assets/prof/ProfParle.mp4"
                )
        )
    }

    @Test
    fun rendererFailuresRequireFreshVideoView() {
        assertTrue(
            ProfessorVideoRecoveryPolicy
                .requiresViewRecreation(
                    "Keycolor shader could not be created"
                )
        )
        assertTrue(
            ProfessorVideoRecoveryPolicy
                .requiresViewRecreation(
                    "External video texture unavailable"
                )
        )
    }
}

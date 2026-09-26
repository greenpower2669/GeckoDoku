package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfessorUiPolicyTest {
    @Test
    fun floatingBubbleNeverReflowsGameLayout() {
        val policy = ProfessorUiPolicy()

        assertTrue(policy.keepControlsVisibleWhileBubbleOpen)
        assertFalse(policy.showPortraitInsideBubble)
        assertFalse(policy.playVideoInsideBubble)
    }

    @Test
    fun professorLivesInFixedButtonHostWithSmallOverhang() {
        val policy = ProfessorUiPolicy()

        assertTrue(policy.showPortraitInButton)
        assertTrue(policy.animatePortraitOnInteraction)
        assertEquals(58, policy.buttonHostHeightDp)
        assertEquals(10, policy.buttonPortraitOverhangDp)
    }

    @Test
    fun professorPortraitAlwaysStaysAboveButtonSurface() {
        val policy = ProfessorUiPolicy()

        assertEquals(0, policy.buttonElevationDp)
        assertTrue(
            policy.portraitElevationDp >
                policy.buttonElevationDp
        )
        assertTrue(
            policy.portraitElevationDp >= 16
        )
    }
}

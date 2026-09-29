package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AliveAnimatorTest {
    private fun profile(
        cute: Boolean = true
    ) =
        MascotAnimationProfile(
            kind = MascotKind.GECKO,
            pngAsset = "gecko.png",
            appearanceAsset =
                "appear.mp4",
            disappearanceAsset =
                "disappear.mp4",
            idleAssets =
                listOf(
                    "idle1.mp4",
                    "idle2.mp4",
                    "idle3.mp4",
                    "idle4.mp4"
                ),
            cuteAssets =
                if (cute) {
                    listOf(
                        "cute.mp4"
                    )
                } else {
                    emptyList()
                },
            keyColor =
                ChromaKeyColor.BLUE
        )

    @Test
    fun appearanceFlowsIntoAmbientWithoutPngFlashState() {
        val animator =
            AliveAnimator(
                profile()
            )

        val first =
            animator.start(
                animationsEnabled = true,
                randomValue = 0
            )

        assertEquals(
            AliveVisualState.APPEARING,
            first.state
        )

        val second =
            animator.afterCurrentClip(
                animationsEnabled = true,
                randomValue = 1
            )

        assertEquals(
            AliveVisualState.IDLE,
            second.state
        )
        assertFalse(
            second.showPng
        )
    }

    @Test
    fun oneIdleChoiceRepeatsFourCyclesThenRerollsToDifferentChoice() {
        val animator =
            AliveAnimator(
                profile(
                    cute = false
                )
            )

        animator.start(
            animationsEnabled = true,
            randomValue = 0
        )

        val firstChoice =
            animator.afterCurrentClip(
                animationsEnabled = true,
                randomValue = 0
            )

        repeat(3) {
            val repeated =
                animator.afterCurrentClip(
                    animationsEnabled = true,
                    randomValue = 0
                )

            assertEquals(
                firstChoice.assetPath,
                repeated.assetPath
            )
        }

        val nextChoice =
            animator.afterCurrentClip(
                animationsEnabled = true,
                randomValue = 0
            )

        assertNotEquals(
            firstChoice.assetPath,
            nextChoice.assetPath
        )
        assertTrue(
            nextChoice
                .requestGroupRefresh
        )
    }

    @Test
    fun cuteActionCannotImmediatelySelectItselfAgainWhenIdleExists() {
        val animator =
            AliveAnimator(
                profile()
            )

        animator.start(
            animationsEnabled = true,
            randomValue = 0
        )

        val cute =
            animator.afterCurrentClip(
                animationsEnabled = true,
                randomValue = 4
            )

        assertEquals(
            AliveVisualState.CUTE,
            cute.state
        )

        val afterCute =
            animator.afterCurrentClip(
                animationsEnabled = true,
                randomValue = 4
            )

        assertNotEquals(
            cute.assetPath,
            afterCute.assetPath
        )
        assertTrue(
            afterCute
                .requestGroupRefresh
        )
    }

    @Test
    fun fifthRandomSlotCanSelectCuteAction() {
        val animator =
            AliveAnimator(
                profile()
            )

        animator.start(
            animationsEnabled = true,
            randomValue = 0
        )

        val decision =
            animator.afterCurrentClip(
                animationsEnabled = true,
                randomValue = 4
            )

        assertEquals(
            AliveVisualState.CUTE,
            decision.state
        )
        assertEquals(
            "cute.mp4",
            decision.assetPath
        )
    }

    @Test
    fun fourIdleCompletionsRequestGroupRefresh() {
        val animator =
            AliveAnimator(
                profile(
                    cute = false
                )
            )

        animator.start(
            animationsEnabled = true,
            randomValue = 0
        )

        // Finish APPEARING first: this selects the first IDLE,
        // but it is not itself an idle completion.
        animator.afterCurrentClip(
            animationsEnabled = true,
            randomValue = 0
        )

        var refreshSeen = false

        repeat(4) {
            index ->
            val decision =
                animator.afterCurrentClip(
                    animationsEnabled =
                        true,
                    randomValue =
                        index
                )

            refreshSeen =
                refreshSeen ||
                    decision
                        .requestGroupRefresh
        }

        assertTrue(
            refreshSeen
        )
    }

    @Test
    fun animationsOffAlwaysUsesAnimatorOwnedTransparentPng() {
        val animator =
            AliveAnimator(
                profile()
            )

        val decision =
            animator.start(
                animationsEnabled =
                    false,
                randomValue = 0
            )

        assertEquals(
            AliveVisualState.STATIC_PNG,
            decision.state
        )
        assertTrue(
            decision.showPng
        )
    }

    @Test
    fun geckoBeePlantProfilesExposeExpectedKeyColors() {
        assertEquals(
            ChromaKeyColor.BLUE,
            MascotAnimationProfiles
                .gecko
                .keyColor
        )

        assertEquals(
            ChromaKeyColor.GREEN,
            MascotAnimationProfiles
                .bee
                .keyColor
        )

        assertEquals(
            ChromaKeyColor.BLUE,
            MascotAnimationProfiles
                .plant
                .keyColor
        )
    }
}

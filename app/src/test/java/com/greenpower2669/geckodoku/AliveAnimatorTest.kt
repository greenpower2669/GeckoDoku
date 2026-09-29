package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AliveAnimatorTest {
    private fun profile(
        cute: Boolean = true,
        boardOwnsStatic:
            Boolean = true
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
                ChromaKeyColor.BLUE,
            boardOwnsStaticPng =
                boardOwnsStatic
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
    fun twoConsecutiveAmbientChoicesDoNotRepeat() {
        val animator =
            AliveAnimator(
                profile()
            )

        animator.start(
            animationsEnabled = true,
            randomValue = 0
        )

        val first =
            animator.afterCurrentClip(
                animationsEnabled = true,
                randomValue = 0
            )

        val second =
            animator.afterCurrentClip(
                animationsEnabled = true,
                randomValue = 0
            )

        assertNotEquals(
            first.assetPath,
            second.assetPath
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
    fun animationsOffUsesBoardPngOrOwnedPngWithoutVideo() {
        val boardAnimator =
            AliveAnimator(
                profile(
                    boardOwnsStatic =
                        true
                )
            )

        val boardDecision =
            boardAnimator.start(
                animationsEnabled =
                    false,
                randomValue = 0
            )

        assertEquals(
            AliveVisualState.STATIC_PNG,
            boardDecision.state
        )
        assertFalse(
            boardDecision.showPng
        )

        val decorationAnimator =
            AliveAnimator(
                profile(
                    boardOwnsStatic =
                        false
                )
            )

        val decorationDecision =
            decorationAnimator.start(
                animationsEnabled =
                    false,
                randomValue = 0
            )

        assertTrue(
            decorationDecision
                .showPng
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

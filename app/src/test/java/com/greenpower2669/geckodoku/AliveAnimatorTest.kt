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
            kind =
                MascotKind.GECKO,
            pngAsset =
                "gecko.png",
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
                animationsEnabled =
                    true,
                randomValue = 0
            )

        assertEquals(
            AliveVisualState.APPEARING,
            first.state
        )

        val second =
            animator.afterCurrentClip(
                animationsEnabled =
                    true,
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
    fun idleChangesEveryClipInsteadOfRepeatingSameSeries() {
        val animator =
            AliveAnimator(
                profile(
                    cute = false
                )
            )

        animator.start(
            animationsEnabled =
                true,
            randomValue = 0
        )

        var previous =
            animator.afterCurrentClip(
                animationsEnabled =
                    true,
                randomValue = 0
            )

        repeat(8) {
            index ->
            val next =
                animator.afterCurrentClip(
                    animationsEnabled =
                        true,
                    randomValue =
                        index + 1
                )

            assertNotEquals(
                previous.assetPath,
                next.assetPath
            )

            previous =
                next
        }
    }

    @Test
    fun completedGrandCycleRequestsFriendRefresh() {
        val animator =
            AliveAnimator(
                profile(
                    cute = false
                )
            )

        animator.start(
            animationsEnabled =
                true,
            randomValue = 0
        )

        animator.afterCurrentClip(
            animationsEnabled =
                true,
            randomValue = 0
        )

        var refreshSeen =
            false

        repeat(8) {
            index ->
            val decision =
                animator.afterCurrentClip(
                    animationsEnabled =
                        true,
                    randomValue =
                        index + 10
                )

            refreshSeen =
                refreshSeen ||
                    decision
                        .requestGroupRefresh
        }

        assertTrue(
            refreshSeen
        )
        assertTrue(
            animator.seriesHistory()
                .isNotEmpty()
        )
    }

    @Test
    fun consecutiveCompletedSeriesAreNotIdenticalWhenAlternativesExist() {
        val animator =
            AliveAnimator(
                profile(
                    cute = false
                )
            )

        animator.startAmbient(
            animationsEnabled =
                true,
            randomValue = 0
        )

        repeat(18) {
            index ->
            animator.afterCurrentClip(
                animationsEnabled =
                    true,
                randomValue =
                    index
            )
        }

        val history =
            animator.seriesHistory()

        history
            .zipWithNext()
            .forEach {
                (first, second) ->
                assertNotEquals(
                    first,
                    second
                )
            }
    }

    @Test
    fun cuteCanBeExplicitlyRequestedForAFriend() {
        val animator =
            AliveAnimator(
                profile()
            )

        animator.startAmbient(
            animationsEnabled =
                true,
            randomValue = 0
        )

        val cute =
            animator.requestCute(
                animationsEnabled =
                    true,
                randomValue = 4
            )

        assertEquals(
            AliveVisualState.CUTE,
            cute.state
        )
        assertEquals(
            "cute.mp4",
            cute.assetPath
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

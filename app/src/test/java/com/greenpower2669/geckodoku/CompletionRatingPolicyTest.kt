package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class CompletionRatingPolicyTest {
    @Test
    fun autonomousCompletionGetsFiveStars() {
        assertEquals(
            5,
            CompletionRatingPolicy
                .starsFor(0)
        )
        assertEquals(
            "★★★★★",
            CompletionRatingPolicy
                .symbols(5)
        )
    }


    @Test
    fun oneMistakeCostsExactlyThreeStars() {
        assertEquals(
            2,
            CompletionRatingPolicy
                .starsFor(
                    assistancePoints = 0,
                    mistakes = 1
                )
        )

        assertEquals(
            1,
            CompletionRatingPolicy
                .starsFor(
                    assistancePoints = 1,
                    mistakes = 1
                )
        )
    }

    @Test
    fun assistanceProgressivelyReducesStars() {
        assertEquals(
            4,
            CompletionRatingPolicy
                .starsFor(1)
        )
        assertEquals(
            3,
            CompletionRatingPolicy
                .starsFor(2)
        )
        assertEquals(
            2,
            CompletionRatingPolicy
                .starsFor(4)
        )
        assertEquals(
            1,
            CompletionRatingPolicy
                .starsFor(8)
        )
    }
}

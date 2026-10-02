package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuCandidateVisualPolicyTest {
    @Test
    fun candidatesStayBlackNumbersForEveryMainVisualStyle() {
        val policy =
            SudokuCandidateVisualPolicy()

        for (
            style in
                SudokuVisualStyle.entries
        ) {
            assertEquals(
                SudokuVisualStyle
                    .CLASSIC_NUMBERS,
                policy.styleForCandidate(
                    style
                )
            )

            assertTrue(
                policy.forceBlackGlyph
            )
        }

        assertEquals(
            0xFF000000.toInt(),
            policy.candidateArgb
        )
    }
}

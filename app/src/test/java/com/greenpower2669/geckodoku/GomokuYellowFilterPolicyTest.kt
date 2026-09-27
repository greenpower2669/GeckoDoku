package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GomokuYellowFilterPolicyTest {
    @Test
    fun yellowFilterBoostsRedFromGreenAndPreservesAlpha() {
        val matrix =
            GomokuYellowFilterPolicy
                .colorMatrixValues()

        assertEquals(
            20,
            matrix.size
        )

        assertTrue(
            matrix[1] > 0.5f
        )
        assertTrue(
            matrix[6] >= 0.8f
        )
        assertTrue(
            matrix[12] < 0.5f
        )

        assertEquals(0f, matrix[15], .001f)
        assertEquals(0f, matrix[16], .001f)
        assertEquals(0f, matrix[17], .001f)
        assertEquals(1f, matrix[18], .001f)
        assertEquals(0f, matrix[19], .001f)
    }
}

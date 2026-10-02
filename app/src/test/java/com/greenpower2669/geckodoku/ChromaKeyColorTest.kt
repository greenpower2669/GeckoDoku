package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class ChromaKeyColorTest {
    @Test
    fun beeCanUseGreenWithoutChangingDefaultBlueKey() {
        assertEquals(
            0f,
            ChromaKeyColor.BLUE
                .greenStrength,
            .001f
        )

        assertEquals(
            1f,
            ChromaKeyColor.GREEN
                .greenStrength,
            .001f
        )
    }
}

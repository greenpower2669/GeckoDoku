package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class CellAnimationStyleTest {
    @Test
    fun greenRegionKeepsItsExactBackgroundColor() {
        assertEquals(
            0xFFE8F8E8.toInt(),
            GeckoBoardPalette.colorFor(0)
        )
    }

    @Test
    fun maskIsSlightlyInsetFromCell() {
        val style = CellAnimationStyle()
        assertEquals(0.04f, style.maskInsetFraction, 0.0001f)
    }
}

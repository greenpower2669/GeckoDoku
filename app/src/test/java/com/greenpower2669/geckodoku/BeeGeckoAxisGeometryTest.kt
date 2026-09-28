package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class BeeGeckoAxisGeometryTest {
    @Test
    fun pointyTopAxesExposeTheirRealScreenAngles() {
        assertEquals(60, BeeGeckoAxisGeometry.screenAngleDegrees(HexAxis.Q))
        assertEquals(0, BeeGeckoAxisGeometry.screenAngleDegrees(HexAxis.R))
        assertEquals(-60, BeeGeckoAxisGeometry.screenAngleDegrees(HexAxis.S))

        assertEquals("↖↘", BeeGeckoAxisGeometry.symbol(HexAxis.Q))
        assertEquals("←→", BeeGeckoAxisGeometry.symbol(HexAxis.R))
        assertEquals("↙↗", BeeGeckoAxisGeometry.symbol(HexAxis.S))
    }

    @Test
    fun canonicalProjectionMatchesDisplayedDirections() {
        val h = sqrt(3f)
        val v = 1.5f
        val origin = BeeGeckoAxisGeometry.center(HexCoord(0, 0), h, v)
        val qLine = BeeGeckoAxisGeometry.center(HexCoord(0, 1), h, v)
        val rLine = BeeGeckoAxisGeometry.center(HexCoord(1, 0), h, v)
        val sLine = BeeGeckoAxisGeometry.center(HexCoord(1, -1), h, v)

        assertTrue(qLine.first > origin.first && qLine.second > origin.second)
        assertEquals(origin.second, rLine.second, 0.0001f)
        assertTrue(rLine.first > origin.first)
        assertTrue(sLine.first > origin.first && sLine.second < origin.second)
    }

    @Test
    fun legendPaletteAndProfessorUseSameCanonicalMapping() {
        assertEquals("Q ↖↘  S ↙↗  R ←→", BeeGeckoAxisGeometry.legend())
        assertEquals("↙↗  Axe S", BeeGeckoAxisGeometry.menuLabel(HexAxis.S))
        assertEquals(
            "l'axe R ←→",
            BeeGeckoRules.axisLabel(HexCoord(0, 0), HexCoord(1, 0))
        )
        assertEquals(
            "l'axe S ↙↗",
            BeeGeckoRules.axisLabel(HexCoord(0, 0), HexCoord(1, -1))
        )
    }
}

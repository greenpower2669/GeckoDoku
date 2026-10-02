package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StableFramePolicyTest {
    @Test
    fun geckoUsesFrameOneOfStayOne() {
        val spec =
            StableFramePolicy
                .specFor(
                    MascotKind.GECKO
                )

        assertEquals(
            AssetMediaCatalog
                .GECKO_IDLE
                .first(),
            spec?.assetPath
        )
        assertEquals(
            ChromaKeyColor.BLUE,
            spec?.keyColor
        )
    }

    @Test
    fun beeUsesFrameOneOfStayOne() {
        val spec =
            StableFramePolicy
                .specFor(
                    MascotKind.BEE
                )

        assertEquals(
            AssetMediaCatalog
                .BEE_IDLE
                .first(),
            spec?.assetPath
        )
        assertEquals(
            ChromaKeyColor.GREEN,
            spec?.keyColor
        )
    }

    @Test
    fun yellowGeckoSharesTheSamePhysicalSource() {
        val green =
            StableFramePolicy
                .specFor(
                    MascotKind.GECKO
                )
        val yellow =
            StableFramePolicy
                .specFor(
                    MascotKind.GECKO
                )

        assertEquals(
            green,
            yellow
        )
    }

    @Test
    fun plantIsOutsideThisMission() {
        assertNull(
            StableFramePolicy
                .specFor(
                    MascotKind.PLANT
                )
        )
        assertFalse(
            StableFramePolicy
                .useStableFrame(
                    MascotKind.PLANT,
                    animationsEnabled = true
                )
        )
    }

    @Test
    fun legacyPngModeWinsWhenAnimationsAreDisabled() {
        assertTrue(
            StableFramePolicy
                .useStableFrame(
                    MascotKind.GECKO,
                    animationsEnabled = true
                )
        )
        assertFalse(
            StableFramePolicy
                .useStableFrame(
                    MascotKind.GECKO,
                    animationsEnabled = false
                )
        )
    }
}

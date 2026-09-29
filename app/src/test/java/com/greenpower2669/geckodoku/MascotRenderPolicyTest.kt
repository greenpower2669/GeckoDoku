package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class MascotRenderPolicyTest {
    @Test
    fun classicVideoMatchesStaticGeckoInset() {
        assertEquals(
            .80f,
            MascotRenderPolicy
                .CLASSIC_GECKO_SCALE,
            0.0001f
        )
    }

    @Test
    fun beeVideoMatchesBoardPieceScale() {
        assertEquals(
            .61f,
            MascotRenderPolicy
                .beeGeckoScale(
                    BeeGeckoPiece.GECKO
                ),
            0.0001f
        )

        assertEquals(
            .61f *
                BeeGeckoVisualPolicy
                    .BEE_SCALE,
            MascotRenderPolicy
                .beeGeckoScale(
                    BeeGeckoPiece.BEE
                ),
            0.0001f
        )
    }
}

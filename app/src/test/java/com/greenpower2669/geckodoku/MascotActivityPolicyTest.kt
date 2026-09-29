package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class MascotActivityPolicyTest {
    @Test
    fun severalMascotsCanStayAliveWithoutUnlimitedVideoPlayers() {
        assertEquals(
            3,
            MascotActivityPolicy.capacity(
                MascotKind.GECKO
            )
        )
        assertEquals(
            2,
            MascotActivityPolicy.capacity(
                MascotKind.BEE
            )
        )
        assertEquals(
            1,
            MascotActivityPolicy.capacity(
                MascotKind.PLANT
            )
        )
    }
}

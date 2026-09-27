package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RecordedEncouragementRemovalTest {
    @Test
    fun encouragementDeliveryIsPierreOnly() {
        val policy =
            EncouragementDeliveryPolicy()

        assertEquals(
            EncouragementDelivery.PIERRE,
            policy.delivery()
        )
        assertFalse(
            policy.usesRecordedAudio
        )
    }
}

package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class PiperSingleModelSlotTest {
    @Test
    fun switchingModelReleasesOldBeforeCreatingNew() {
        val events = mutableListOf<String>()

        val slot =
            PiperSingleModelSlot<String>(
                create = { variant ->
                    events += "create:$variant"
                    variant.name
                },
                release = { model ->
                    events += "release:$model"
                }
            )

        slot.require(VoiceBenchmarkVariant.PIPER_LOW)
        slot.require(VoiceBenchmarkVariant.PIPER_MEDIUM)

        assertEquals(
            listOf(
                "create:PIPER_LOW",
                "release:PIPER_LOW",
                "create:PIPER_MEDIUM"
            ),
            events
        )
    }

    @Test
    fun requestingSameModelDoesNotReloadIt() {
        var creates = 0
        var releases = 0

        val slot =
            PiperSingleModelSlot<String>(
                create = {
                    creates += 1
                    it.name
                },
                release = {
                    releases += 1
                }
            )

        slot.require(VoiceBenchmarkVariant.PIPER_LOW)
        slot.require(VoiceBenchmarkVariant.PIPER_LOW)

        assertEquals(1, creates)
        assertEquals(0, releases)
    }
}

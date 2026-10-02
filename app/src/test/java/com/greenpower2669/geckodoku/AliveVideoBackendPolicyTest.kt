package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AliveVideoBackendPolicyTest {
    @Test
    fun allBoardGeckosAndBeesUseSpritePlayback() {
        assertTrue(
            AliveVideoBackendPolicy
                .useSpritePlayback(
                    "gomoku:10:11"
                )
        )
        assertTrue(
            AliveVideoBackendPolicy
                .useSpritePlayback(
                    "bee:GECKO:1:-1"
                )
        )
        assertTrue(
            AliveVideoBackendPolicy
                .useSpritePlayback(
                    "classic:4:4"
                )
        )
        assertTrue(
            AliveVideoBackendPolicy
                .useSpritePlayback(
                    "sudoku:4:4"
                )
        )
        assertFalse(
            AliveVideoBackendPolicy
                .useSpritePlayback(
                    "plant:decoration"
                )
        )
    }

    @Test
    fun legacyTexturePolicyRemainsAvailableButIsBypassedBySprites() {
        assertTrue(
            AliveVideoBackendPolicy
                .useTextureView(
                    ownerKey =
                        "bee:GECKO:1:-1",
                    sdkInt = 33
                )
        )
        assertFalse(
            AliveVideoBackendPolicy
                .useTextureView(
                    ownerKey =
                        "plant:decoration",
                    sdkInt = 36
                )
        )
    }
}

package com.greenpower2669.geckodoku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AliveVideoBackendPolicyTest {
    @Test
    fun boardModesUseTextureViewOnModernAndroid() {
        assertTrue(
            AliveVideoBackendPolicy
                .useTextureView(
                    ownerKey =
                        "bee:GECKO:1:-1",
                    sdkInt = 33
                )
        )

        assertTrue(
            AliveVideoBackendPolicy
                .useTextureView(
                    ownerKey =
                        "gomoku:10:11",
                    sdkInt = 36
                )
        )
    }

    @Test
    fun legacyAliveModesKeepGlSurfaceBackend() {
        assertFalse(
            AliveVideoBackendPolicy
                .useTextureView(
                    ownerKey =
                        "classic:4:4",
                    sdkInt = 36
                )
        )

        assertFalse(
            AliveVideoBackendPolicy
                .useTextureView(
                    ownerKey =
                        "sudoku:4:4",
                    sdkInt = 36
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

    @Test
    fun oldAndroidFallsBackEvenForBoardModes() {
        assertFalse(
            AliveVideoBackendPolicy
                .useTextureView(
                    ownerKey =
                        "bee:BEE:0:0",
                    sdkInt = 32
                )
        )

        assertFalse(
            AliveVideoBackendPolicy
                .useTextureView(
                    ownerKey =
                        "gomoku:5:5",
                    sdkInt = 31
                )
        )
    }
}

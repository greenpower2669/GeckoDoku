package com.greenpower2669.geckodoku

import org.junit.Assert.*
import org.junit.Test

class PlayerProfileIsolationTest {
    @Test fun legacyProfileKeepsAllHistoricalLocalStatistics() {
        assertEquals(
            PlayerStatsStore.PREFERENCES_NAME,
            PlayerStatsStore.preferenceNameForProfile(
                PlayerProfileStore.LEGACY_PROFILE_ID
            )
        )
    }

    @Test fun distinctProfilesNeverShareTheSameStatsPreferences() {
        val a = "11111111-1111-4111-8111-111111111111"
        val b = "22222222-2222-4222-8222-222222222222"
        val storeA = PlayerStatsStore.preferenceNameForProfile(a)
        val storeB = PlayerStatsStore.preferenceNameForProfile(b)
        assertNotEquals(storeA, storeB)
        assertNotEquals(PlayerStatsStore.PREFERENCES_NAME, storeA)
        assertNotEquals(PlayerStatsStore.PREFERENCES_NAME, storeB)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectUntrustedProfileIdentifiers() {
        PlayerStatsStore.preferenceNameForProfile("../shared_preferences")
    }
}

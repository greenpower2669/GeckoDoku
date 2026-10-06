package com.greenpower2669.geckodoku

import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GlobalScoreCompletionBridgeTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun forwardsExplicitCompletedAtIntoFrozenPayload() {
        val store =
            PendingScoreStore(
                File(temp.newFolder(), "pending")
            )

        val publisher =
            GlobalScoreCompletionPublisher(
                pendingStore = store,
                triggerSync = {},
                runIdFactory = { "bridge-run" },
                clock = { 999L }
            )

        GlobalScoreCompletionBridge.publish(
            publisher = publisher,
            playerName = "GeckoTétu",
            mode = GameMode.SUDOKU,
            size = 9,
            difficulty = GameDifficulty.EXPERT,
            stars = 5,
            elapsedSeconds = 42L,
            mistakes = 0,
            assistancePoints = 0,
            appVersion = "0.15.43-dev",
            classicPuzzle = null,
            sudokuPuzzle = null,
            sudokuVisualStyle =
                SudokuVisualStyle.CLASSIC_NUMBERS,
            gomokuSnapshot = null,
            gomokuMatchMode = null,
            beeGeckoPuzzle = null,
            completedAt = 1_791_266_500_123L
        )

        val frozen =
            JSONObject(
                requireNotNull(
                    store.find("bridge-run")
                ).payloadJson
            )

        assertEquals(
            1_791_266_500_123L,
            frozen.getLong("completedAt")
        )
    }
}

package com.greenpower2669.geckodoku

import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GlobalScoreCompletionPublisherTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun persistsFrozenPayloadBeforeTriggeringNetwork() {
        val store = PendingScoreStore(File(temp.newFolder(), "pending"))
        var triggerSawPersistedScore = false

        val publisher = GlobalScoreCompletionPublisher(
            pendingStore = store,
            triggerSync = {
                triggerSawPersistedScore = store.find("fixed-run") != null
            },
            runIdFactory = { "fixed-run" },
            clock = { 1_791_266_400_000L }
        )

        val runId = publisher.publish(
            playerName = "GeckoTétu",
            mode = GameMode.GECKODOKU,
            size = 7,
            difficulty = GameDifficulty.HARD,
            stars = 4,
            elapsedSeconds = 321L,
            mistakes = 1,
            assistancePoints = 2,
            appVersion = "0.15.43-dev",
            puzzleId = "classic-7-hard",
            seed = Long.MAX_VALUE,
            details = GlobalScoreModeDetails.Classic,
            metadata = mapOf("generator" to "classic")
        )

        assertEquals("fixed-run", runId)
        assertTrue(triggerSawPersistedScore)

        val stored = store.find("fixed-run")
        assertNotNull(stored)
        val root = JSONObject(requireNotNull(stored).payloadJson)
        assertEquals("fixed-run", root.getString("runId"))
        assertEquals("GeckoTétu", root.getString("playerName"))
        assertEquals("GECKODOKU", root.getString("mode"))
        assertEquals("HARD", root.getString("difficulty"))
        assertEquals(7, root.getInt("size"))
        assertEquals(4, root.getInt("stars"))
        assertEquals(321L, root.getLong("elapsedSeconds"))
        assertEquals(1, root.getInt("mistakes"))
        assertEquals(2, root.getInt("assistancePoints"))
        assertTrue(root.getBoolean("usedProfessor"))
        assertEquals(1_791_266_400_000L, root.getLong("completedAt"))
        assertEquals("0.15.43-dev", root.getString("appVersion"))
        assertEquals("classic-7-hard", root.getString("puzzleId"))
        assertEquals(Long.MAX_VALUE.toString(), root.getString("seed"))
        assertEquals("classic", root.getJSONObject("metadata").getString("generator"))
    }

    @Test
    fun preservesModeSpecificFieldsInFrozenJson() {
        val store = PendingScoreStore(File(temp.newFolder(), "pending"))
        val runIds = ArrayDeque(listOf("sudoku-run", "gomoku-run", "bee-run"))
        val publisher = GlobalScoreCompletionPublisher(
            pendingStore = store,
            triggerSync = {},
            runIdFactory = { runIds.removeFirst() },
            clock = { 123L }
        )

        publisher.publish(
            playerName = "P",
            mode = GameMode.SUDOKU,
            size = 9,
            difficulty = GameDifficulty.EXPERT,
            stars = 5,
            elapsedSeconds = 10,
            mistakes = 0,
            assistancePoints = 0,
            appVersion = "v",
            seed = Long.MIN_VALUE,
            details = GlobalScoreModeDetails.Sudoku(
                seed = Long.MIN_VALUE,
                visualStyle = SudokuVisualStyle.GECKO_COLORED
            )
        )

        publisher.publish(
            playerName = "P",
            mode = GameMode.GOMOKU,
            size = 19,
            difficulty = GameDifficulty.HARD,
            stars = 5,
            elapsedSeconds = 20,
            mistakes = 0,
            assistancePoints = 1,
            appVersion = "v",
            details = GlobalScoreModeDetails.Gomoku(
                matchMode = GomokuMatchMode.VS_PROFESSOR,
                winner = GomokuPlayer.PLAYER,
                draw = false,
                moveCount = 47
            )
        )

        publisher.publish(
            playerName = "P",
            mode = GameMode.BEES_GECKOS,
            size = 6,
            difficulty = GameDifficulty.EXPERT,
            stars = 3,
            elapsedSeconds = 30,
            mistakes = 1,
            assistancePoints = 0,
            appVersion = "v",
            puzzleId = "bee-1",
            seed = 42L,
            details = GlobalScoreModeDetails.BeeGecko(
                puzzleId = "bee-1",
                seed = 42L,
                radius = 3,
                pairCount = 6
            )
        )

        val sudoku = JSONObject(store.find("sudoku-run")!!.payloadJson)
        assertEquals(Long.MIN_VALUE.toString(), sudoku.getString("seed"))
        assertEquals("GECKO_COLORED", sudoku.getString("sudokuVisualStyle"))

        val gomoku = JSONObject(store.find("gomoku-run")!!.payloadJson)
        assertEquals("VS_PROFESSOR", gomoku.getString("gomokuMatchMode"))
        assertEquals("PLAYER", gomoku.getString("gomokuWinner"))
        assertFalse(gomoku.getBoolean("gomokuDraw"))
        assertEquals(47, gomoku.getInt("gomokuMoveCount"))

        val bee = JSONObject(store.find("bee-run")!!.payloadJson)
        assertEquals("bee-1", bee.getString("puzzleId"))
        assertEquals("42", bee.getString("seed"))
        assertEquals(3, bee.getInt("beeGeckoRadius"))
        assertEquals(6, bee.getInt("beeGeckoPairCount"))
    }

    @Test
    fun doesNotTriggerNetworkWhenPendingQueueCannotPersist() {
        val store = PendingScoreStore(
            directory = File(temp.newFolder(), "pending"),
            maxEntries = 0
        )
        var triggered = false
        val publisher = GlobalScoreCompletionPublisher(
            pendingStore = store,
            triggerSync = { triggered = true },
            runIdFactory = { "no-room" },
            clock = { 456L }
        )

        val runId = publisher.publish(
            playerName = "P",
            mode = GameMode.SUDOKU,
            size = 9,
            difficulty = GameDifficulty.EASY,
            stars = 5,
            elapsedSeconds = 1,
            mistakes = 0,
            assistancePoints = 0,
            appVersion = "v",
            details = GlobalScoreModeDetails.Sudoku(
                seed = 7L,
                visualStyle = SudokuVisualStyle.CLASSIC_NUMBERS
            )
        )

        assertNull(runId)
        assertFalse(triggered)
        assertTrue(store.entries().isEmpty())
    }
}

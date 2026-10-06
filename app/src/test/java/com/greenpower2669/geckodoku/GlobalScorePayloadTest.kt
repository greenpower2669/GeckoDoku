package com.greenpower2669.geckodoku

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlobalScorePayloadTest {
    private fun common(
        mode: GameMode,
        size: Int = 9
    ) =
        GlobalScoreCommon(
            runId = "550e8400-e29b-41d4-a716-446655440000",
            playerName = "GeckoTétu",
            mode = mode,
            difficulty = GameDifficulty.EXPERT,
            size = size,
            stars = 5,
            elapsedSeconds = 327L,
            mistakes = 0,
            assistancePoints = 0,
            usedProfessor = false,
            completedAt = 1791266400000L,
            appVersion = "0.15.43-dev"
        )

    private fun json(
        common: GlobalScoreCommon,
        details: GlobalScoreModeDetails
    ): JSONObject =
        JSONObject(
            GlobalScorePayloadCodec.encode(
                GlobalScorePayloadFactory.create(
                    common = common,
                    details = details
                )
            )
        )

    @Test
    fun commonPayloadContainsAllRequiredFields() {
        val obj =
            json(
                common(GameMode.GECKODOKU, 7),
                GlobalScoreModeDetails.Classic
            )

        assertEquals(1, obj.getInt("schemaVersion"))
        assertEquals(1, obj.getInt("scoreVersion"))
        assertEquals("550e8400-e29b-41d4-a716-446655440000", obj.getString("runId"))
        assertEquals("GeckoTétu", obj.getString("playerName"))
        assertEquals("GECKODOKU", obj.getString("mode"))
        assertEquals("EXPERT", obj.getString("difficulty"))
        assertEquals(7, obj.getInt("size"))
        assertTrue(obj.getBoolean("completed"))
        assertEquals(5, obj.getInt("stars"))
        assertEquals(327L, obj.getLong("elapsedSeconds"))
        assertEquals(0, obj.getInt("mistakes"))
        assertEquals(0, obj.getInt("assistancePoints"))
        assertEquals(false, obj.getBoolean("usedProfessor"))
        assertEquals(1791266400000L, obj.getLong("completedAt"))
        assertEquals("0.15.43-dev", obj.getString("appVersion"))
        assertEquals(0, obj.getJSONObject("metadata").length())
    }

    @Test
    fun classicLeavesForeignModeFieldsNull() {
        val obj =
            json(
                common(GameMode.GECKODOKU, 12),
                GlobalScoreModeDetails.Classic
            )

        listOf(
            "puzzleId",
            "seed",
            "sudokuVisualStyle",
            "gomokuMatchMode",
            "gomokuWinner",
            "gomokuDraw",
            "gomokuMoveCount",
            "beeGeckoRadius",
            "beeGeckoPairCount"
        ).forEach {
            assertTrue("$it should be null", obj.isNull(it))
        }
    }

    @Test
    fun sudokuUsesSize9SeedAndVisualStyle() {
        val obj =
            json(
                common(GameMode.SUDOKU, 9),
                GlobalScoreModeDetails.Sudoku(
                    seed = 9876543210L,
                    visualStyle = SudokuVisualStyle.GECKO_COLORED
                )
            )

        assertEquals(9, obj.getInt("size"))
        assertEquals("9876543210", obj.getString("seed"))
        assertEquals("GECKO_COLORED", obj.getString("sudokuVisualStyle"))
        assertTrue(obj.isNull("gomokuMatchMode"))
        assertTrue(obj.isNull("beeGeckoRadius"))
    }

    @Test
    fun gomokuProfessorCarriesMatchWinnerDrawAndMoveCount() {
        val obj =
            json(
                common(GameMode.GOMOKU, 19),
                GlobalScoreModeDetails.Gomoku(
                    matchMode = GomokuMatchMode.VS_PROFESSOR,
                    winner = GomokuPlayer.PLAYER,
                    draw = false,
                    moveCount = 47
                )
            )

        assertEquals("VS_PROFESSOR", obj.getString("gomokuMatchMode"))
        assertEquals("PLAYER", obj.getString("gomokuWinner"))
        assertEquals(false, obj.getBoolean("gomokuDraw"))
        assertEquals(47, obj.getInt("gomokuMoveCount"))
    }

    @Test
    fun gomokuHumanModeIsRepresentableWithoutChangingGameRules() {
        val obj =
            json(
                common(GameMode.GOMOKU, 13),
                GlobalScoreModeDetails.Gomoku(
                    matchMode = GomokuMatchMode.HUMAN_VS_HUMAN,
                    winner = null,
                    draw = true,
                    moveCount = 89
                )
            )

        assertEquals("HUMAN_VS_HUMAN", obj.getString("gomokuMatchMode"))
        assertTrue(obj.isNull("gomokuWinner"))
        assertTrue(obj.getBoolean("gomokuDraw"))
        assertEquals(89, obj.getInt("gomokuMoveCount"))
    }

    @Test
    fun beeCarriesPuzzleIdRadiusPairsAndSeed() {
        val obj =
            json(
                common(GameMode.BEES_GECKOS, 6),
                GlobalScoreModeDetails.BeeGecko(
                    puzzleId = "bee-puzzle-42",
                    seed = -123456789L,
                    radius = 3,
                    pairCount = 6
                )
            )

        assertEquals("bee-puzzle-42", obj.getString("puzzleId"))
        assertEquals("-123456789", obj.getString("seed"))
        assertEquals(3, obj.getInt("beeGeckoRadius"))
        assertEquals(6, obj.getInt("beeGeckoPairCount"))
    }

    @Test
    fun longMinSeedIsDecimalString() {
        val obj =
            json(
                common(GameMode.SUDOKU),
                GlobalScoreModeDetails.Sudoku(
                    seed = Long.MIN_VALUE,
                    visualStyle = SudokuVisualStyle.CLASSIC_NUMBERS
                )
            )

        assertEquals(Long.MIN_VALUE.toString(), obj.getString("seed"))
        assertTrue(obj.get("seed") is String)
    }

    @Test
    fun longMaxSeedIsDecimalString() {
        val obj =
            json(
                common(GameMode.SUDOKU),
                GlobalScoreModeDetails.Sudoku(
                    seed = Long.MAX_VALUE,
                    visualStyle = SudokuVisualStyle.GECKO_NB
                )
            )

        assertEquals(Long.MAX_VALUE.toString(), obj.getString("seed"))
        assertTrue(obj.get("seed") is String)
    }
}

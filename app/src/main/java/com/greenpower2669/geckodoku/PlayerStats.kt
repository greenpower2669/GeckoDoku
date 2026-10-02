package com.greenpower2669.geckodoku

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class StatTrend {
    IMPROVING,
    STABLE,
    DECLINING,
    INSUFFICIENT
}

data class PlayerStatEvent(
    val mode: GameMode,
    val size: Int,
    val difficulty: GameDifficulty,
    val completed: Boolean,
    val mistakes: Int,
    val elapsedSeconds: Long,
    val stars: Int,
    val usedProfessor: Boolean,
    val occurredAt: Long
)

data class LocalPlayerStats(
    val gamesStarted: Int,
    val gamesCompleted: Int,
    val assistedCompleted: Int,
    val mistakes: Int,
    val totalSeconds: Long
) {
    val completionRate: Int
        get() =
            if (gamesStarted == 0) {
                0
            } else {
                (
                    gamesCompleted *
                        100.0 /
                        gamesStarted
                    ).toInt()
            }

    val averageSeconds: Long
        get() =
            if (gamesCompleted == 0) {
                0
            } else {
                totalSeconds /
                    gamesCompleted
            }
}

data class DifficultyStats(
    val started: Int,
    val completed: Int,
    val assistedCompleted: Int,
    val bestStars: Int,
    val averageStars: Int,
    val mistakes: Int = 0,
    val abandonedWithMistakes: Int = 0,
    val averageSeconds: Long = 0L,
    val speedTrend:
        StatTrend =
        StatTrend.INSUFFICIENT,
    val starTrend:
        StatTrend =
        StatTrend.INSUFFICIENT
) {
    val completionRate: Int
        get() =
            if (started == 0) {
                0
            } else {
                (
                    completed *
                        100.0 /
                        started
                    ).toInt()
            }
}

data class ProfessorDifficultyStats(
    val difficulty: GameDifficulty,
    val stats: DifficultyStats
)

class PlayerStatsStore(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    fun recordStart(
        size: Int,
        difficulty: GameDifficulty,
        mode: GameMode =
            GameMode.GECKODOKU
    ) {
        finalizePendingAbandoned()

        prefs.edit()
            .putBoolean(
                KEY_ACTIVE,
                true
            )
            .putInt(
                KEY_ACTIVE_SIZE,
                size
            )
            .putString(
                KEY_ACTIVE_DIFFICULTY,
                difficulty.name
            )
            .putString(
                KEY_ACTIVE_MODE,
                mode.name
            )
            .putInt(
                KEY_ACTIVE_MISTAKES,
                0
            )
            .putLong(
                KEY_ACTIVE_STARTED_AT,
                System.currentTimeMillis()
            )
            .apply()
    }

    fun recordMistake() {
        if (
            prefs.getBoolean(
                KEY_ACTIVE,
                false
            )
        ) {
            prefs.edit()
                .putInt(
                    KEY_ACTIVE_MISTAKES,
                    prefs.getInt(
                        KEY_ACTIVE_MISTAKES,
                        0
                    ) +
                        1
                )
                .apply()
        } else {
            increment(
                "mistakes"
            )
        }
    }

    fun recordComplete(
        size: Int,
        difficulty: GameDifficulty,
        elapsedSeconds: Long,
        usedProfessor: Boolean = false,
        stars: Int = 5,
        mode: GameMode =
            GameMode.GECKODOKU
    ) {
        val activeMistakes =
            activeMistakesFor(
                mode =
                    mode,
                difficulty =
                    difficulty,
                size =
                    size
            )

        addEvent(
            PlayerStatEvent(
                mode =
                    mode,
                size =
                    size,
                difficulty =
                    difficulty,
                completed =
                    true,
                mistakes =
                    activeMistakes,
                elapsedSeconds =
                    elapsedSeconds
                        .coerceAtLeast(
                            0L
                        ),
                stars =
                    stars.coerceIn(
                        1,
                        5
                    ),
                usedProfessor =
                    usedProfessor,
                occurredAt =
                    System.currentTimeMillis()
            )
        )

        recordLegacyCompletion(
            size =
                size,
            difficulty =
                difficulty,
            elapsedSeconds =
                elapsedSeconds,
            usedProfessor =
                usedProfessor,
            stars =
                stars,
            mode =
                mode,
            mistakes =
                activeMistakes
        )

        clearActiveAttempt()
    }

    fun read(): LocalPlayerStats {
        val events =
            events()

        if (events.isNotEmpty()) {
            val completed =
                events.filter {
                    it.completed
                }

            return LocalPlayerStats(
                gamesStarted =
                    events.size,
                gamesCompleted =
                    completed.size,
                assistedCompleted =
                    completed.count {
                        it.usedProfessor
                    },
                mistakes =
                    events.sumOf {
                        it.mistakes
                    },
                totalSeconds =
                    completed.sumOf {
                        it.elapsedSeconds
                    }
            )
        }

        return LocalPlayerStats(
            gamesStarted =
                prefs.getInt(
                    "started",
                    0
                ),
            gamesCompleted =
                prefs.getInt(
                    "completed",
                    0
                ),
            assistedCompleted =
                prefs.getInt(
                    "completed_with_prof",
                    0
                ),
            mistakes =
                prefs.getInt(
                    "mistakes",
                    0
                ),
            totalSeconds =
                prefs.getLong(
                    "total_seconds",
                    0L
                )
        )
    }

    fun events():
        List<PlayerStatEvent> =
        readEventObjects()
            .mapNotNull {
                decodeEvent(it)
            }
            .sortedBy {
                it.occurredAt
            }

    fun eventsForDifficulty(
        difficulty: GameDifficulty
    ): List<PlayerStatEvent> =
        events()
            .filter {
                it.difficulty ==
                    difficulty
            }

    fun eventsForModeAndDifficulty(
        mode: GameMode,
        difficulty: GameDifficulty
    ): List<PlayerStatEvent> =
        events()
            .filter {
                it.mode ==
                    mode &&
                    it.difficulty ==
                        difficulty
            }

    fun professorLevels():
        List<ProfessorDifficultyStats> {
        val all =
            GameDifficulty.entries
                .map {
                    it to
                        statsForDifficulty(
                            it
                        )
                }

        val hardest =
            all.lastOrNull {
                (_, stats) ->
                stats.started > 0 ||
                    stats.completed > 0
            }
                ?: return emptyList()

        val result =
            mutableListOf(
                ProfessorDifficultyStats(
                    difficulty =
                        hardest.first,
                    stats =
                        hardest.second
                )
            )

        val previousOrdinal =
            hardest.first.ordinal -
                1

        if (previousOrdinal >= 0) {
            val previous =
                GameDifficulty.entries[
                    previousOrdinal
                ]

            val stats =
                statsForDifficulty(
                    previous
                )

            if (
                stats.started > 0 ||
                stats.completed > 0
            ) {
                result.add(
                    ProfessorDifficultyStats(
                        difficulty =
                            previous,
                        stats =
                            stats
                    )
                )
            }
        }

        return result
    }

    fun completedForSize(
        size: Int
    ): Int {
        val history =
            events()

        if (history.isNotEmpty()) {
            return history.count {
                it.completed &&
                    it.size ==
                        size
            }
        }

        return prefs.getInt(
            "completed_size_" +
                size,
            0
        )
    }

    fun startedForDifficulty(
        difficulty: GameDifficulty
    ): Int =
        statsForDifficulty(
            difficulty
        ).started

    fun completedForDifficulty(
        difficulty: GameDifficulty
    ): Int =
        statsForDifficulty(
            difficulty
        ).completed

    fun statsForDifficulty(
        difficulty: GameDifficulty
    ): DifficultyStats {
        val history =
            eventsForDifficulty(
                difficulty
            )

        if (history.isNotEmpty()) {
            return statsFromEvents(
                history
            )
        }

        val completed =
            prefs.getInt(
                "completed_diff_" +
                    difficulty.name,
                0
            )

        return DifficultyStats(
            started =
                prefs.getInt(
                    "started_diff_" +
                        difficulty.name,
                    0
                ),
            completed =
                completed,
            assistedCompleted =
                prefs.getInt(
                    "completed_with_prof_diff_" +
                        difficulty.name,
                    0
                ),
            bestStars =
                prefs.getInt(
                    "best_stars_diff_" +
                        difficulty.name,
                    0
                ),
            averageStars =
                if (completed <= 0) {
                    0
                } else {
                    prefs.getInt(
                        "stars_total_diff_" +
                            difficulty.name,
                        0
                    ) /
                        completed
                },
            mistakes =
                0,
            abandonedWithMistakes =
                0,
            averageSeconds =
                0L
        )
    }

    fun statsForModeAndDifficulty(
        mode: GameMode,
        difficulty: GameDifficulty
    ): DifficultyStats {
        val history =
            eventsForModeAndDifficulty(
                mode,
                difficulty
            )

        if (history.isNotEmpty()) {
            return statsFromEvents(
                history
            )
        }

        val completed =
            prefs.getInt(
                modeDifficultyKey(
                    prefix =
                        "completed",
                    mode =
                        mode,
                    difficulty =
                        difficulty
                ),
                0
            )

        return DifficultyStats(
            started =
                prefs.getInt(
                    modeDifficultyKey(
                        prefix =
                            "started",
                        mode =
                            mode,
                        difficulty =
                            difficulty
                    ),
                    0
                ),
            completed =
                completed,
            assistedCompleted =
                prefs.getInt(
                    modeDifficultyKey(
                        prefix =
                            "completed_with_prof",
                        mode =
                            mode,
                        difficulty =
                            difficulty
                    ),
                    0
                ),
            bestStars =
                prefs.getInt(
                    modeDifficultyKey(
                        prefix =
                            "best_stars",
                        mode =
                            mode,
                        difficulty =
                            difficulty
                    ),
                    0
                ),
            averageStars =
                if (completed <= 0) {
                    0
                } else {
                    prefs.getInt(
                        modeDifficultyKey(
                            prefix =
                                "stars_total",
                            mode =
                                mode,
                            difficulty =
                                difficulty
                        ),
                        0
                    ) /
                        completed
                }
        )
    }

    private fun statsFromEvents(
        history: List<PlayerStatEvent>
    ): DifficultyStats {
        val completed =
            history.filter {
                it.completed
            }

        val lastTwo =
            completed
                .sortedBy {
                    it.occurredAt
                }
                .takeLast(
                    2
                )

        val speedTrend =
            if (lastTwo.size < 2) {
                StatTrend
                    .INSUFFICIENT
            } else {
                when {
                    lastTwo[1]
                        .elapsedSeconds <
                        lastTwo[0]
                            .elapsedSeconds ->
                        StatTrend
                            .IMPROVING

                    lastTwo[1]
                        .elapsedSeconds >
                        lastTwo[0]
                            .elapsedSeconds ->
                        StatTrend
                            .DECLINING

                    else ->
                        StatTrend
                            .STABLE
                }
            }

        val starTrend =
            if (lastTwo.size < 2) {
                StatTrend
                    .INSUFFICIENT
            } else {
                when {
                    lastTwo[1]
                        .stars >
                        lastTwo[0]
                            .stars ->
                        StatTrend
                            .IMPROVING

                    lastTwo[1]
                        .stars <
                        lastTwo[0]
                            .stars ->
                        StatTrend
                            .DECLINING

                    else ->
                        StatTrend
                            .STABLE
                }
            }

        return DifficultyStats(
            started =
                history.size,
            completed =
                completed.size,
            assistedCompleted =
                completed.count {
                    it.usedProfessor
                },
            bestStars =
                completed.maxOfOrNull {
                    it.stars
                }
                    ?: 0,
            averageStars =
                if (completed.isEmpty()) {
                    0
                } else {
                    completed.sumOf {
                        it.stars
                    } /
                        completed.size
                },
            mistakes =
                history.sumOf {
                    it.mistakes
                },
            abandonedWithMistakes =
                history.count {
                    !it.completed &&
                        it.mistakes >
                            0
                },
            averageSeconds =
                if (completed.isEmpty()) {
                    0L
                } else {
                    completed.sumOf {
                        it.elapsedSeconds
                    } /
                        completed.size
                },
            speedTrend =
                speedTrend,
            starTrend =
                starTrend
        )
    }

    private fun activeMistakesFor(
        mode: GameMode,
        difficulty: GameDifficulty,
        size: Int
    ): Int {
        if (
            !prefs.getBoolean(
                KEY_ACTIVE,
                false
            )
        ) {
            return 0
        }

        val activeMode =
            prefs.getString(
                KEY_ACTIVE_MODE,
                null
            )

        val activeDifficulty =
            prefs.getString(
                KEY_ACTIVE_DIFFICULTY,
                null
            )

        val activeSize =
            prefs.getInt(
                KEY_ACTIVE_SIZE,
                -1
            )

        return if (
            activeMode ==
                mode.name &&
            activeDifficulty ==
                difficulty.name &&
            activeSize ==
                size
        ) {
            prefs.getInt(
                KEY_ACTIVE_MISTAKES,
                0
            )
                .coerceAtLeast(
                    0
                )
        } else {
            0
        }
    }

    private fun finalizePendingAbandoned() {
        if (
            !prefs.getBoolean(
                KEY_ACTIVE,
                false
            )
        ) {
            return
        }

        val mistakes =
            prefs.getInt(
                KEY_ACTIVE_MISTAKES,
                0
            )
                .coerceAtLeast(
                    0
                )

        if (mistakes > 0) {
            val mode =
                enumOrNull<GameMode>(
                    prefs.getString(
                        KEY_ACTIVE_MODE,
                        null
                    )
                )

            val difficulty =
                enumOrNull<
                    GameDifficulty
                    >(
                    prefs.getString(
                        KEY_ACTIVE_DIFFICULTY,
                        null
                    )
                )

            val size =
                prefs.getInt(
                    KEY_ACTIVE_SIZE,
                    0
                )

            if (
                mode != null &&
                difficulty != null
            ) {
                addEvent(
                    PlayerStatEvent(
                        mode =
                            mode,
                        size =
                            size,
                        difficulty =
                            difficulty,
                        completed =
                            false,
                        mistakes =
                            mistakes,
                        elapsedSeconds =
                            0L,
                        stars =
                            0,
                        usedProfessor =
                            false,
                        occurredAt =
                            System.currentTimeMillis()
                    )
                )

                recordLegacyAbandoned(
                    size =
                        size,
                    difficulty =
                        difficulty,
                    mode =
                        mode,
                    mistakes =
                        mistakes
                )
            }
        }

        clearActiveAttempt()
    }

    private fun clearActiveAttempt() {
        prefs.edit()
            .remove(
                KEY_ACTIVE
            )
            .remove(
                KEY_ACTIVE_SIZE
            )
            .remove(
                KEY_ACTIVE_DIFFICULTY
            )
            .remove(
                KEY_ACTIVE_MODE
            )
            .remove(
                KEY_ACTIVE_MISTAKES
            )
            .remove(
                KEY_ACTIVE_STARTED_AT
            )
            .apply()
    }

    private fun addEvent(
        event: PlayerStatEvent
    ) {
        val objects =
            readEventObjects()
                .toMutableList()

        objects.add(
            JSONObject()
                .put(
                    "mode",
                    event.mode.name
                )
                .put(
                    "size",
                    event.size
                )
                .put(
                    "difficulty",
                    event.difficulty
                        .name
                )
                .put(
                    "completed",
                    event.completed
                )
                .put(
                    "mistakes",
                    event.mistakes
                )
                .put(
                    "elapsedSeconds",
                    event.elapsedSeconds
                )
                .put(
                    "stars",
                    event.stars
                )
                .put(
                    "usedProfessor",
                    event.usedProfessor
                )
                .put(
                    "occurredAt",
                    event.occurredAt
                )
        )

        val trimmed =
            objects.takeLast(
                MAX_EVENTS
            )

        val array =
            JSONArray()

        trimmed.forEach {
            array.put(it)
        }

        prefs.edit()
            .putString(
                KEY_EVENTS,
                array.toString()
            )
            .apply()
    }

    private fun readEventObjects():
        List<JSONObject> {
        val raw =
            prefs.getString(
                KEY_EVENTS,
                "[]"
            )
                ?: "[]"

        return try {
            val array =
                JSONArray(
                    raw
                )

            buildList {
                for (
                    index in
                    0 until
                        array.length()
                ) {
                    array
                        .optJSONObject(
                            index
                        )
                        ?.let {
                            add(it)
                        }
                }
            }
        } catch (
            _: Exception
        ) {
            emptyList()
        }
    }

    private fun decodeEvent(
        obj: JSONObject
    ): PlayerStatEvent? {
        val mode =
            enumOrNull<GameMode>(
                obj.optString(
                    "mode"
                )
            )
                ?: return null

        val difficulty =
            enumOrNull<
                GameDifficulty
                >(
                obj.optString(
                    "difficulty"
                )
            )
                ?: return null

        return PlayerStatEvent(
            mode =
                mode,
            size =
                obj.optInt(
                    "size",
                    0
                ),
            difficulty =
                difficulty,
            completed =
                obj.optBoolean(
                    "completed",
                    false
                ),
            mistakes =
                obj.optInt(
                    "mistakes",
                    0
                )
                    .coerceAtLeast(
                        0
                    ),
            elapsedSeconds =
                obj.optLong(
                    "elapsedSeconds",
                    0L
                )
                    .coerceAtLeast(
                        0L
                    ),
            stars =
                obj.optInt(
                    "stars",
                    0
                )
                    .coerceIn(
                        0,
                        5
                    ),
            usedProfessor =
                obj.optBoolean(
                    "usedProfessor",
                    false
                ),
            occurredAt =
                obj.optLong(
                    "occurredAt",
                    0L
                )
        )
    }

    private fun recordLegacyCompletion(
        size: Int,
        difficulty: GameDifficulty,
        elapsedSeconds: Long,
        usedProfessor: Boolean,
        stars: Int,
        mode: GameMode,
        mistakes: Int
    ) {
        increment(
            "started"
        )
        increment(
            "started_size_" +
                size
        )
        increment(
            "started_diff_" +
                difficulty.name
        )
        increment(
            "started_mode_" +
                mode.name
        )
        increment(
            modeDifficultyKey(
                prefix =
                    "started",
                mode =
                    mode,
                difficulty =
                    difficulty
            )
        )

        increment(
            "completed"
        )
        increment(
            "completed_size_" +
                size
        )
        increment(
            "completed_diff_" +
                difficulty.name
        )
        increment(
            "completed_mode_" +
                mode.name
        )
        increment(
            modeDifficultyKey(
                prefix =
                    "completed",
                mode =
                    mode,
                difficulty =
                    difficulty
            )
        )

        repeat(
            mistakes
        ) {
            increment(
                "mistakes"
            )
        }

        if (usedProfessor) {
            increment(
                "completed_with_prof"
            )
            increment(
                "completed_with_prof_diff_" +
                    difficulty.name
            )
            increment(
                modeDifficultyKey(
                    prefix =
                        "completed_with_prof",
                    mode =
                        mode,
                    difficulty =
                        difficulty
                )
            )
        }

        val normalizedStars =
            stars.coerceIn(
                1,
                5
            )

        val starTotalKey =
            "stars_total_diff_" +
                difficulty.name

        val bestStarsKey =
            "best_stars_diff_" +
                difficulty.name

        val modeStarTotalKey =
            modeDifficultyKey(
                prefix =
                    "stars_total",
                mode =
                    mode,
                difficulty =
                    difficulty
            )

        val modeBestStarsKey =
            modeDifficultyKey(
                prefix =
                    "best_stars",
                mode =
                    mode,
                difficulty =
                    difficulty
            )

        prefs.edit()
            .putLong(
                "total_seconds",
                prefs.getLong(
                    "total_seconds",
                    0L
                ) +
                    elapsedSeconds
                        .coerceAtLeast(
                            0L
                        )
            )
            .putInt(
                starTotalKey,
                prefs.getInt(
                    starTotalKey,
                    0
                ) +
                    normalizedStars
            )
            .putInt(
                bestStarsKey,
                kotlin.math.max(
                    prefs.getInt(
                        bestStarsKey,
                        0
                    ),
                    normalizedStars
                )
            )
            .putInt(
                modeStarTotalKey,
                prefs.getInt(
                    modeStarTotalKey,
                    0
                ) +
                    normalizedStars
            )
            .putInt(
                modeBestStarsKey,
                kotlin.math.max(
                    prefs.getInt(
                        modeBestStarsKey,
                        0
                    ),
                    normalizedStars
                )
            )
            .apply()
    }

    private fun recordLegacyAbandoned(
        size: Int,
        difficulty: GameDifficulty,
        mode: GameMode,
        mistakes: Int
    ) {
        increment(
            "started"
        )
        increment(
            "started_size_" +
                size
        )
        increment(
            "started_diff_" +
                difficulty.name
        )
        increment(
            "started_mode_" +
                mode.name
        )
        increment(
            modeDifficultyKey(
                prefix =
                    "started",
                mode =
                    mode,
                difficulty =
                    difficulty
            )
        )

        repeat(
            mistakes
        ) {
            increment(
                "mistakes"
            )
        }
    }

    private fun modeDifficultyKey(
        prefix: String,
        mode: GameMode,
        difficulty: GameDifficulty
    ): String =
        prefix +
            "_mode_" +
            mode.name +
            "_diff_" +
            difficulty.name

    private fun increment(
        key: String
    ) {
        prefs.edit()
            .putInt(
                key,
                prefs.getInt(
                    key,
                    0
                ) +
                    1
            )
            .apply()
    }

    private inline fun <
        reified T : Enum<T>
        >
        enumOrNull(
            raw: String?
        ): T? =
        try {
            if (
                raw.isNullOrBlank()
            ) {
                null
            } else {
                enumValueOf<T>(
                    raw
                )
            }
        } catch (
            _: Exception
        ) {
            null
        }

    companion object {
        const val PREFERENCES_NAME =
            "geckodoku_local_stats"

        private const val KEY_EVENTS =
            "events_v2"

        private const val KEY_ACTIVE =
            "active_attempt"

        private const val KEY_ACTIVE_SIZE =
            "active_attempt_size"

        private const val KEY_ACTIVE_DIFFICULTY =
            "active_attempt_difficulty"

        private const val KEY_ACTIVE_MODE =
            "active_attempt_mode"

        private const val KEY_ACTIVE_MISTAKES =
            "active_attempt_mistakes"

        private const val KEY_ACTIVE_STARTED_AT =
            "active_attempt_started_at"

        private const val MAX_EVENTS =
            500
    }
}

package com.greenpower2669.geckodoku

import android.content.Context

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
    val averageStars: Int
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

class PlayerStatsStore(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            "geckodoku_local_stats",
            Context.MODE_PRIVATE
        )

    fun recordStart(
        size: Int,
        difficulty: GameDifficulty,
        mode: GameMode =
            GameMode.GECKODOKU
    ) {
        increment("started")
        increment(
            "started_size_" + size
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
                prefix = "started",
                mode = mode,
                difficulty = difficulty
            )
        )
    }

    fun recordMistake() {
        increment("mistakes")
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
        increment("completed")
        increment(
            "completed_size_" + size
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
                prefix = "completed",
                mode = mode,
                difficulty = difficulty
            )
        )

        if (usedProfessor) {
            increment("completed_with_prof")
            increment(
                "completed_with_prof_diff_" +
                    difficulty.name
            )
            increment(
                modeDifficultyKey(
                    prefix = "completed_with_prof",
                    mode = mode,
                    difficulty = difficulty
                )
            )
        }

        val normalizedStars =
            stars.coerceIn(
                1,
                5
            )

        increment(
            "stars_" +
                normalizedStars +
                "_diff_" +
                difficulty.name
        )

        val starTotalKey =
            "stars_total_diff_" +
                difficulty.name

        val bestStarsKey =
            "best_stars_diff_" +
                difficulty.name

        val modeStarTotalKey =
            modeDifficultyKey(
                prefix = "stars_total",
                mode = mode,
                difficulty = difficulty
            )

        val modeBestStarsKey =
            modeDifficultyKey(
                prefix = "best_stars",
                mode = mode,
                difficulty = difficulty
            )

        prefs.edit()
            .putLong(
                "total_seconds",
                prefs.getLong(
                    "total_seconds",
                    0L
                ) +
                    elapsedSeconds
                        .coerceAtLeast(0L)
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

    fun read(): LocalPlayerStats =
        LocalPlayerStats(
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

    fun completedForSize(
        size: Int
    ): Int =
        prefs.getInt(
            "completed_size_" + size,
            0
        )

    fun startedForDifficulty(
        difficulty: GameDifficulty
    ): Int =
        prefs.getInt(
            "started_diff_" +
                difficulty.name,
            0
        )

    fun completedForDifficulty(
        difficulty: GameDifficulty
    ): Int =
        prefs.getInt(
            "completed_diff_" +
                difficulty.name,
            0
        )

    fun statsForDifficulty(
        difficulty: GameDifficulty
    ): DifficultyStats =
        DifficultyStats(
            started =
                startedForDifficulty(
                    difficulty
                ),
            completed =
                completedForDifficulty(
                    difficulty
                ),
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
                completedForDifficulty(
                    difficulty
                ).let {
                    completed ->

                    if (completed <= 0) {
                        0
                    } else {
                        prefs.getInt(
                            "stars_total_diff_" +
                                difficulty.name,
                            0
                        ) /
                            completed
                    }
                }
        )

    fun statsForModeAndDifficulty(
        mode: GameMode,
        difficulty: GameDifficulty
    ): DifficultyStats {
        val completed =
            prefs.getInt(
                modeDifficultyKey(
                    prefix = "completed",
                    mode = mode,
                    difficulty = difficulty
                ),
                0
            )

        return DifficultyStats(
            started =
                prefs.getInt(
                    modeDifficultyKey(
                        prefix = "started",
                        mode = mode,
                        difficulty = difficulty
                    ),
                    0
                ),
            completed = completed,
            assistedCompleted =
                prefs.getInt(
                    modeDifficultyKey(
                        prefix = "completed_with_prof",
                        mode = mode,
                        difficulty = difficulty
                    ),
                    0
                ),
            bestStars =
                prefs.getInt(
                    modeDifficultyKey(
                        prefix = "best_stars",
                        mode = mode,
                        difficulty = difficulty
                    ),
                    0
                ),
            averageStars =
                if (completed <= 0) {
                    0
                } else {
                    prefs.getInt(
                        modeDifficultyKey(
                            prefix = "stars_total",
                            mode = mode,
                            difficulty = difficulty
                        ),
                        0
                    ) /
                        completed
                }
        )
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
                ) + 1
            )
            .apply()
    }
}

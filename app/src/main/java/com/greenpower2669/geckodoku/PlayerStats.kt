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
    val assistedCompleted: Int
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
        difficulty: GameDifficulty
    ) {
        increment("started")
        increment(
            "started_size_" + size
        )
        increment(
            "started_diff_" +
                difficulty.name
        )
    }

    fun recordMistake() {
        increment("mistakes")
    }

    fun recordComplete(
        size: Int,
        difficulty: GameDifficulty,
        elapsedSeconds: Long,
        usedProfessor: Boolean = false
    ) {
        increment("completed")
        increment(
            "completed_size_" + size
        )
        increment(
            "completed_diff_" +
                difficulty.name
        )

        if (usedProfessor) {
            increment("completed_with_prof")
            increment(
                "completed_with_prof_diff_" +
                    difficulty.name
            )
        }

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
                )
        )

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

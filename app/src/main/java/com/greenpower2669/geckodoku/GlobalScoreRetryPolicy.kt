package com.greenpower2669.geckodoku

object GlobalScoreRetryPolicy {
    fun delayMs(
        attemptNumber: Int,
        retryAfterSeconds: Long?
    ): Long {
        if (retryAfterSeconds != null) {
            return retryAfterSeconds
                .coerceAtLeast(0L)
                .coerceAtMost(
                    Long.MAX_VALUE / 1000L
                ) * 1000L
        }

        return when {
            attemptNumber <= 1 -> 5_000L
            attemptNumber == 2 -> 15_000L
            attemptNumber == 3 -> 30_000L
            attemptNumber == 4 -> 60_000L
            else -> 300_000L
        }
    }
}

package com.greenpower2669.geckodoku

enum class ProfessorAnimationAction {
    BOUNCE,
    TILT,
    NOD
}

class ProfessorIdleAnimationPolicy {
    val minDelayMs: Long = 2_000L
    val maxDelayMs: Long = 3_000L

    fun delayMs(
        randomValue: Int
    ): Long {
        val span =
            (
                maxDelayMs -
                    minDelayMs +
                    1L
                ).toInt()

        return minDelayMs +
            Math.floorMod(
                randomValue,
                span
            )
    }

    fun actionFor(
        randomValue: Int
    ): ProfessorAnimationAction {
        val actions =
            ProfessorAnimationAction.entries

        return actions[
            Math.floorMod(
                randomValue,
                actions.size
            )
        ]
    }
}

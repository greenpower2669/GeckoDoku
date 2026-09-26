package com.greenpower2669.geckodoku

class RichMediaScheduler(
    private val cooldownMs: Long = 45_000L,
    private val chancePercent: Int = 45
) {
    private var lastAcceptedAt: Long? = null

    fun shouldPlayLongAction(
        nowMs: Long,
        randomValue: Int,
        eligible: Boolean,
        busy: Boolean
    ): Boolean {
        if (!eligible || busy) {
            return false
        }

        val last = lastAcceptedAt

        if (last != null &&
            nowMs - last < cooldownMs
        ) {
            return false
        }

        val roll =
            randomValue.coerceIn(
                0,
                99
            )

        if (roll >= chancePercent) {
            return false
        }

        lastAcceptedAt = nowMs
        return true
    }
}

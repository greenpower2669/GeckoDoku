package com.greenpower2669.geckodoku

data class ProfessorQuickBubbleCloseSchedule(
    val token: Long,
    val delayMs: Long
)

class ProfessorQuickBubbleClosePolicy {
    private var generation = 0L
    private var simple = false

    fun onSimpleBubbleShown(): Long {
        generation += 1L
        simple = true
        return generation
    }

    fun onPedagogicalBubbleShown(): Long {
        generation += 1L
        simple = false
        return generation
    }

    fun onSpeechCompleted(
        token: Long
    ): ProfessorQuickBubbleCloseSchedule? {
        if (token != generation || !simple) {
            return null
        }
        return ProfessorQuickBubbleCloseSchedule(
            token = token,
            delayMs = 1_000L
        )
    }

    fun canClose(token: Long): Boolean =
        simple && token == generation

    fun invalidate() {
        generation += 1L
        simple = false
    }
}

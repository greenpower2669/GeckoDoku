package com.greenpower2669.geckodoku

data class ProfessorSimpleSpeechPlan(
    val bubbleText: String,
    val speechText: String,
    val statusText: String,
    val origin: SpeechOrigin,
    val token: Long,
    val autoClose: Boolean = true,
    val voiceRequestCount: Int = 1
)

class ProfessorSimpleSpeechCoordinator(
    private val closePolicy:
        ProfessorQuickBubbleClosePolicy
) {
    fun begin(
        text: String,
        origin: SpeechOrigin,
        canAccept: Boolean
    ): ProfessorSimpleSpeechPlan? {
        if (
            !canAccept ||
            text.isBlank() ||
            origin ==
                SpeechOrigin.PROF_BUTTON
        ) {
            return null
        }

        val token =
            closePolicy
                .onSimpleBubbleShown()

        return ProfessorSimpleSpeechPlan(
            bubbleText = text,
            speechText = text,
            statusText = "Prof Gecko",
            origin = origin,
            token = token
        )
    }

    fun onSpeechCompleted(
        token: Long
    ): ProfessorQuickBubbleCloseSchedule? =
        closePolicy
            .onSpeechCompleted(token)

    fun shouldCloseAfterRejection(
        token: Long
    ): Boolean =
        closePolicy
            .canClose(token)

    fun canClose(
        token: Long
    ): Boolean =
        closePolicy
            .canClose(token)
}

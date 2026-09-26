package com.greenpower2669.geckodoku

data class QuickTalkPresentation(
    val bubbleText: String,
    val statusText: String,
    val origin: SpeechOrigin,
    val speechRequestCount: Int,
    val affectsBoardLayout: Boolean
)

class QuickTalkPresentationPolicy {
    fun present(
        line: String
    ): QuickTalkPresentation =
        QuickTalkPresentation(
            bubbleText = line,
            statusText = "Prof Gecko",
            origin = SpeechOrigin.QUICK_TALK,
            speechRequestCount = 1,
            affectsBoardLayout = false
        )
}

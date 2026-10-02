package com.greenpower2669.geckodoku

object ProfessorDialogTextPolicy {
    private val leadingSpeaker =
        Regex(
            "^\\s*Prof\\s+Gecko\\s*[:•]\\s*"
        )

    fun normalize(
        text: String
    ): String =
        text.replaceFirst(
            leadingSpeaker,
            ""
        )
}

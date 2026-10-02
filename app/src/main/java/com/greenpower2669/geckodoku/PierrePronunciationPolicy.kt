package com.greenpower2669.geckodoku

object PierrePronunciationPolicy {
    private val replacements =
        listOf(
            Regex(
                pattern = """\béglise\b""",
                option =
                    RegexOption
                        .IGNORE_CASE
            ) to
                "eglize"
        )

    fun forSpeech(
        text: String
    ): String {
        var result =
            text

        replacements
            .forEach {
                (pattern, replacement) ->
                result =
                    pattern.replace(
                        result,
                        replacement
                    )
            }

        return result
    }
}

package com.greenpower2669.geckodoku

object AssetMediaCatalog {
    const val GECKO_PORTRAIT =
        "gecko/Gecko.png"

    const val GECKO_INTRO =
        "gecko/Gecko_Intro.mp4"

    const val GECKO_APPEARANCE =
        "gecko/Gecko_apparition.mp4"

    const val GECKO_DISAPPEARANCE =
        "gecko/Gecko_disparition.mp4"

    const val GECKO_LONG_ACTIONS =
        "gecko/Gecko_actions_plusieurs.mp4"

    const val PROF_PORTRAIT =
        "prof/Prof.png"

    const val PROF_BLUE_SOURCE =
        "prof/Prof_fb.png"

    const val PROF_LONG_ACTIONS =
        "prof/Prof_actions.mp4"

    const val KEY_THRESHOLD = 0.22f
    const val KEY_SOFTNESS = 0.18f
    const val KEY_DESPILL = 0.85f
}

enum class RichMediaKind {
    INTRO,
    GECKO_APPEARANCE,
    GECKO_DISAPPEARANCE,
    GECKO_LONG_ACTION,
    PROF_LONG_ACTION
}

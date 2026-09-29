package com.greenpower2669.geckodoku

object AssetMediaCatalog {
    const val GECKO_PORTRAIT =
        "gecko/Gecko_tr.png"

    const val GECKO_ICON =
        "gecko/IconGeckoGD.png"

    const val GECKO_NUMBER_NB =
        "gecko/PlancheGeckoDeNombreNB.png"

    const val GECKO_NUMBER_COLORED =
        "gecko/PlancheGeckoDeNombreColored.png"

    const val GECKO_INTRO_GD =
        "gecko/IntroGeckoGD.mp4"

    const val GECKO_INTRO =
        "gecko/Gecko_Intro.mp4"

    const val GECKO_APPEARANCE =
        "gecko/Gecko_apparition.mp4"

    const val GECKO_DISAPPEARANCE =
        "gecko/Gecko_disparition.mp4"

    const val GECKO_LONG_ACTIONS =
        "gecko/Gecko_actions_plusieurs.mp4"

    val GECKO_IDLE =
        listOf(
            "gecko/alive/stay1.mp4",
            "gecko/alive/stay2.mp4",
            "gecko/alive/stay3.mp4",
            "gecko/alive/stay4.mp4"
        )

    const val BEE_PORTRAIT =
        "abeille/AbeilleTr.png"

    const val BEE_APPEARANCE =
        "abeille/Abeillefondvert.mp4"

    val BEE_IDLE =
        listOf(
            "abeille/alive/stay1.mp4",
            "abeille/alive/stay2.mp4",
            "abeille/alive/stay3.mp4",
            "abeille/alive/stay4.mp4"
        )

    const val PLANT_PORTRAIT =
        "plante/alive/PlanteTr.png"

    val PLANT_IDLE =
        listOf(
            "plante/alive/stay1.mp4",
            "plante/alive/stay2.mp4",
            "plante/alive/stay3.mp4",
            "plante/alive/stay4.mp4"
        )

    const val PLANT_LONG_ACTION =
        "plante/alive/cute.mp4"

    const val PROF_PORTRAIT =
        "prof/Prof.png"

    const val PROF_BLUE_SOURCE =
        "prof/Prof_fb.png"

    const val PROF_LONG_ACTIONS =
        "prof/Prof_actions.mp4"

    const val PROF_SPEECH =
        "prof/ProfParle.mp4"

    const val KEY_THRESHOLD = 0.22f
    const val KEY_SOFTNESS = 0.18f
    const val KEY_DESPILL = 0.85f
}

enum class RichMediaKind {
    INTRO,
    GECKO_APPEARANCE,
    GECKO_DISAPPEARANCE,
    GECKO_LONG_ACTION,
    PROF_LONG_ACTION,
    BEE_APPEARANCE
}

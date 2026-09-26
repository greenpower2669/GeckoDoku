package com.greenpower2669.geckodoku

data class EncouragementSegment(
    val text: String,
    val startMs: Int,
    val endMs: Int
)

object AssetAudioCatalog {
    const val LEVEL_START =
        "audio/intro/jungle intro GeckoD.mp3"
    const val CELEBRATION =
        "audio/celebration/jungle cebration GeckoD.mp3"
    const val ENCOURAGEMENT_MASTER =
        "audio/encouragements/master/Voix_encouragements.mp3"

    val ENCOURAGEMENTS =
        listOf(
            EncouragementSegment("Bravo.", 0, 508),
            EncouragementSegment("Super.", 1023, 1518),
            EncouragementSegment("Excellente.", 2113, 2683),
            EncouragementSegment("Génial.", 3108, 3717),
            EncouragementSegment("Bien joué.", 4246, 4859),
            EncouragementSegment("Magnifique.", 5111, 5745),
            EncouragementSegment("Continue comme ça.", 6327, 7169),
            EncouragementSegment("Tu y es presque.", 7878, 8644),
            EncouragementSegment("Très bien.", 9019, 9468),
            EncouragementSegment("Formidable.", 9790, 10524),
            EncouragementSegment("Quel talent.", 10974, 11604),
            EncouragementSegment("Impressionnant.", 11991, 12741),
            EncouragementSegment("Ça, c'était vraiment bon.", 13152, 14441)
        )
}

package com.greenpower2669.geckodoku

enum class SudokuVisualToolMode(
    val label: String
) {
    NONE("Aucun"),
    PROJECTION("Projection"),
    PAIR("Couple"),
    TRIPLET("Triplet"),
    CANDIDATE("Candidat")
}

package com.greenpower2669.geckodoku

import kotlin.math.abs
import kotlin.math.roundToInt

data class DifficultyFeatures(
    val size: Int,
    val regionBoundaryCount: Int,
    val meanSolutionSpacing: Double,
    val constraintDensity: Double
)

data class DifficultyReport(
    val index: Int,
    val label: String,
    val features: DifficultyFeatures,
    val model: String
)

interface DifficultyModel {
    fun score(features: DifficultyFeatures): Int
}

class HeuristicDifficultyModel : DifficultyModel {
    override fun score(features: DifficultyFeatures): Int {
        val sizePart = (features.size - 4).coerceAtLeast(0) * 8
        val boundaryPart = (features.regionBoundaryCount * 1.6).roundToInt()
        val spacingPart = ((3.0 - features.meanSolutionSpacing).coerceAtLeast(0.0) * 8).roundToInt()
        val densityPart = (features.constraintDensity * 40).roundToInt()
        return (sizePart + boundaryPart + spacingPart + densityPart).coerceIn(0, 100)
    }
}

object DifficultyIndexer {
    fun analyze(puzzle: Puzzle, model: DifficultyModel = HeuristicDifficultyModel()): DifficultyReport {
        var boundaries = 0
        for (r in 0 until puzzle.size) {
            for (c in 0 until puzzle.size) {
                val here = puzzle.regions[r * puzzle.size + c]
                if (c + 1 < puzzle.size && here != puzzle.regions[r * puzzle.size + c + 1]) boundaries++
                if (r + 1 < puzzle.size && here != puzzle.regions[(r + 1) * puzzle.size + c]) boundaries++
            }
        }
        val spacing = if (puzzle.size <= 1) 0.0 else {
            (0 until puzzle.size - 1)
                .map { r -> abs(puzzle.solutionCols[r + 1] - puzzle.solutionCols[r]).toDouble() }
                .average()
        }
        val denom = (2.0 * puzzle.size * (puzzle.size - 1)).coerceAtLeast(1.0)
        val density = boundaries.toDouble() / denom
        val features = DifficultyFeatures(puzzle.size, boundaries, spacing, density)
        val index = model.score(features)
        val label = when (index) {
            in 0..20 -> "Détente"
            in 21..40 -> "Facile"
            in 41..60 -> "Réflexion"
            in 61..80 -> "Difficile"
            else -> "Expert"
        }
        return DifficultyReport(index, label, features, model::class.simpleName ?: "model")
    }
}

package com.greenpower2669.geckodoku

data class DifficultyFeatures(
    val size: Int,
    val givenCount: Int,
    val logicalSteps: Int,
    val maxTechnique: SolveTechnique,
    val xWingCount: Int
)

data class DifficultyReport(
    val index: Int,
    val label: String,
    val features: DifficultyFeatures,
    val model: String,
    val logicallySolvable: Boolean
)

interface DifficultyModel {
    fun score(features: DifficultyFeatures): Int
}

class HeuristicDifficultyModel : DifficultyModel {
    override fun score(features: DifficultyFeatures): Int {
        val sizePart = (features.size - 5) * 6
        val cluePart = (features.size - features.givenCount).coerceAtLeast(0) * 5
        val stepPart = (features.logicalSteps * 2).coerceAtMost(25)
        val techniquePart = features.maxTechnique.weight
        val xWingPart = features.xWingCount * 8
        return (sizePart + cluePart + stepPart + techniquePart + xWingPart).coerceIn(0, 100)
    }
}

object DifficultyIndexer {
    fun analyze(
        puzzle: Puzzle,
        model: DifficultyModel = HeuristicDifficultyModel()
    ): DifficultyReport {
        val solve = HumanSolver.analyze(puzzle, puzzle.givens, SolveTechnique.X_WING)
        val features = DifficultyFeatures(
            size = puzzle.size,
            givenCount = puzzle.givens.size,
            logicalSteps = solve.steps.size,
            maxTechnique = solve.maxTechnique,
            xWingCount = solve.xWingCount
        )
        val index = model.score(features)
        val label = when (index) {
            in 0..20 -> "Détente"
            in 21..40 -> "Facile"
            in 41..60 -> "Réflexion"
            in 61..80 -> "Difficile"
            else -> "Expert"
        }
        return DifficultyReport(index, label, features, model::class.simpleName ?: "model", solve.solved)
    }
}

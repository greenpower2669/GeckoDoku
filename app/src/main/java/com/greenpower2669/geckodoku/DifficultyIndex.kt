package com.greenpower2669.geckodoku

data class DifficultyFeatures(
    val size: Int,
    val givenCount: Int,
    val logicalSteps: Int,
    val regionLogicCount: Int,
    val projectionCount: Int,
    val xWingCount: Int,
    val maxTechnique: SolveTechnique,
    val xWingRequired: Boolean,
    val projectionRequired: Boolean
)

data class DifficultyReport(
    val index: Int,
    val label: String,
    val ratedDifficulty: GameDifficulty,
    val features: DifficultyFeatures,
    val model: String,
    val logicallySolvable: Boolean
)

interface DifficultyModel {
    fun score(features: DifficultyFeatures): Int
}

class HeuristicDifficultyModel : DifficultyModel {
    override fun score(features: DifficultyFeatures): Int {
        val sizePart = (features.size - 5) * 3
        val stepPart = (features.logicalSteps * 2).coerceAtMost(25)
        val regionPart = (features.regionLogicCount * 5).coerceAtMost(25)
        val projectionPart = features.projectionCount * 7
        val xWingPart = if (features.xWingRequired) 22 + features.xWingCount * 6 else features.xWingCount * 3
        val comboPart = if (features.xWingRequired && features.projectionRequired) 18 else 0
        return (sizePart + stepPart + regionPart + projectionPart + xWingPart + comboPart).coerceIn(0, 100)
    }
}

object DifficultyIndexer {
    fun analyze(
        puzzle: Puzzle,
        model: DifficultyModel = HeuristicDifficultyModel()
    ): DifficultyReport {
        val full = HumanSolver.analyze(puzzle, puzzle.givens, SolverRules.FULL)
        val singles = HumanSolver.analyze(puzzle, puzzle.givens, SolverRules.SINGLES)
        val region = HumanSolver.analyze(puzzle, puzzle.givens, SolverRules.REGION)
        val noProjection = HumanSolver.analyze(puzzle, puzzle.givens, SolverRules.NO_PROJECTION)

        val xWingRequired = full.solved && !region.solved
        val projectionRequired = full.solved && !noProjection.solved

        val rated = when {
            !full.solved -> GameDifficulty.DEMENTIAL
            xWingRequired && projectionRequired -> GameDifficulty.DEMENTIAL
            xWingRequired -> GameDifficulty.EXPERT
            singles.solved -> GameDifficulty.DISCOVERY
            region.regionLogicCount <= 1 -> GameDifficulty.EASY
            region.regionLogicCount <= 3 -> GameDifficulty.THINKING
            else -> GameDifficulty.HARD
        }

        val features = DifficultyFeatures(
            size = puzzle.size,
            givenCount = puzzle.givens.size,
            logicalSteps = full.steps.size,
            regionLogicCount = full.regionLogicCount,
            projectionCount = full.projectionCount,
            xWingCount = full.xWingCount,
            maxTechnique = full.maxTechnique,
            xWingRequired = xWingRequired,
            projectionRequired = projectionRequired
        )
        val index = model.score(features)

        return DifficultyReport(
            index = index,
            label = rated.label,
            ratedDifficulty = rated,
            features = features,
            model = model::class.simpleName ?: "model",
            logicallySolvable = full.solved
        )
    }
}

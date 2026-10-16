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
    val projectionRequired: Boolean,
    val hypothesisCount: Int,
    val hypothesisDepth: Int
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
    fun score(
        features: DifficultyFeatures
    ): Int
}

class HeuristicDifficultyModel :
    DifficultyModel {

    override fun score(
        features: DifficultyFeatures
    ): Int {
        val sizePart =
            (features.size - 5) * 3

        val stepPart =
            (features.logicalSteps * 2)
                .coerceAtMost(22)

        val regionPart =
            (features.regionLogicCount * 5)
                .coerceAtMost(22)

        val projectionPart =
            features.projectionCount * 6

        val xWingPart =
            if (features.xWingRequired) {
                20 +
                    features.xWingCount * 5
            } else {
                features.xWingCount * 3
            }

        val comboPart =
            if (
                features.xWingRequired &&
                features.projectionRequired
            ) {
                14
            } else {
                0
            }

        val hypothesisPart =
            when {
                features.hypothesisDepth >= 3 ->
                    48 +
                        features.hypothesisCount * 8

                features.hypothesisDepth >= 2 ->
                    38 +
                        features.hypothesisCount * 8

                features.hypothesisCount >= 1 ->
                    28 +
                        features.hypothesisCount * 6

                else -> 0
            }

        return (
            sizePart +
                stepPart +
                regionPart +
                projectionPart +
                xWingPart +
                comboPart +
                hypothesisPart
            ).coerceIn(
                0,
                100
            )
    }
}

object DifficultyIndexer {
    fun analyze(
        puzzle: Puzzle,
        model: DifficultyModel =
            HeuristicDifficultyModel(),
        shouldCancel: () -> Boolean = { false }
    ): DifficultyReport {
        val full =
            HumanSolver.analyze(
                puzzle,
                puzzle.givens,
                SolverRules.FULL
            )

        val singles =
            HumanSolver.analyze(
                puzzle,
                puzzle.givens,
                SolverRules.SINGLES
            )

        val region =
            HumanSolver.analyze(
                puzzle,
                puzzle.givens,
                SolverRules.REGION
            )

        val noProjection =
            HumanSolver.analyze(
                puzzle,
                puzzle.givens,
                SolverRules.NO_PROJECTION
            )

        val mission =
            if (!full.solved) {
                HypothesisSolver.analyze(
                    puzzle,
                    maxDepth = 1,
                    maxHypothesisSteps = 1,
                    shouldCancel = shouldCancel
                )
            } else {
                null
            }

        // Two independent guesses or depth two are not Infernal.
        val depthTwo =
            if (!full.solved && mission?.solved != true) {
                HypothesisSolver.analyze(
                    puzzle,
                    maxDepth = 2,
                    maxHypothesisSteps = puzzle.size,
                    shouldCancel = shouldCancel
                )
            } else {
                null
            }

        val infernal =
            if (!full.solved &&
                mission?.solved != true &&
                depthTwo?.solved != true
            ) {
                HypothesisSolver.analyze(
                    puzzle,
                    maxDepth = InfernalDepthPolicy.SEARCH_DEPTH,
                    maxHypothesisSteps = puzzle.size,
                    shouldCancel = shouldCancel
                )
            } else {
                null
            }

        val xWingRequired =
            full.solved &&
                !region.solved

        val projectionRequired =
            full.solved &&
                !noProjection.solved

        val rated =
            when {
                full.solved &&
                    xWingRequired &&
                    projectionRequired ->
                    GameDifficulty.DEMENTIAL

                full.solved &&
                    xWingRequired ->
                    GameDifficulty.EXPERT

                full.solved &&
                    singles.solved ->
                    GameDifficulty.DISCOVERY

                full.solved &&
                    region.regionLogicCount <= 1 ->
                    GameDifficulty.EASY

                full.solved &&
                    region.regionLogicCount <= 3 ->
                    GameDifficulty.THINKING

                full.solved ->
                    GameDifficulty.HARD

                mission?.solved == true ->
                    GameDifficulty.MISSION_IMPOSSIBLE

                infernal?.solved == true &&
                    InfernalDepthPolicy.accepts(
                        true,
                        infernal.maxHypothesisDepth
                    ) ->
                    GameDifficulty.INFERNAL

                // Provisional label only: exact generation rejects an
                // unproved grid or a depth-two intermediate puzzle.
                else ->
                    GameDifficulty.INFERNAL
            }

        val chosenSteps =
            when {
                full.solved ->
                    full.steps

                mission?.solved == true ->
                    mission.steps

                infernal?.solved == true ->
                    infernal.steps

                depthTwo?.solved == true ->
                    depthTwo.steps

                else ->
                    emptyList()
            }

        val hypothesisCount =
            when {
                mission?.solved == true ->
                    mission.hypothesisCount

                infernal?.solved == true ->
                    infernal.hypothesisCount

                depthTwo?.solved == true ->
                    depthTwo.hypothesisCount

                else -> 0
            }

        val hypothesisDepth =
            when {
                mission?.solved == true ->
                    mission.maxHypothesisDepth

                infernal?.solved == true ->
                    infernal.maxHypothesisDepth

                depthTwo?.solved == true ->
                    depthTwo.maxHypothesisDepth

                else -> 0
            }

        val maxTechnique =
            chosenSteps
                .maxByOrNull {
                    it.technique.weight
                }
                ?.technique
                ?: full.maxTechnique

        val features =
            DifficultyFeatures(
                size = puzzle.size,
                givenCount =
                    puzzle.givens.size,
                logicalSteps =
                    chosenSteps.size,
                regionLogicCount =
                    chosenSteps.count {
                        it.technique ==
                            SolveTechnique.REGION_SINGLE ||
                            it.technique ==
                            SolveTechnique.REGION_LOCKED ||
                            it.technique ==
                            SolveTechnique.REGION_TOUCH_PROJECTION
                    },
                projectionCount =
                    chosenSteps.count {
                        it.technique ==
                            SolveTechnique.REGION_TOUCH_PROJECTION
                    },
                xWingCount =
                    chosenSteps.count {
                        it.technique ==
                            SolveTechnique.GECKO_X_WING
                    },
                maxTechnique =
                    maxTechnique,
                xWingRequired =
                    xWingRequired,
                projectionRequired =
                    projectionRequired,
                hypothesisCount =
                    hypothesisCount,
                hypothesisDepth =
                    hypothesisDepth
            )

        val logicallySolvable =
            full.solved ||
                mission?.solved == true ||
                infernal?.solved == true ||
                depthTwo?.solved == true

        return DifficultyReport(
            index =
                model.score(features),
            label =
                rated.label,
            ratedDifficulty =
                rated,
            features =
                features,
            model =
                model::class.simpleName
                    ?: "model",
            logicallySolvable =
                logicallySolvable
        )
    }
}

package com.greenpower2669.geckodoku

data class ProfessorHint(
    val step: SolveStep,
    val focusText: String,
    val explanationText: String,
    val actionText: String,
    val fromCachedTrace: Boolean
)

object ProfessorGecko {
    fun nextHint(
        puzzle: Puzzle,
        snapshot: GameSnapshot
    ): ProfessorHint? {
        val excluded = linkedSetOf<Cell>().apply {
            addAll(snapshot.manualCrosses)
            addAll(snapshot.autoCrosses)
        }

        val cached = puzzle.solverTrace.firstOrNull { step ->
            cachedStepFits(step, snapshot.confirmed, excluded)
        }

        val step =
            cached
                ?: HumanSolver.nextStep(
                    puzzle = puzzle,
                    confirmed = snapshot.confirmed,
                    excluded = excluded,
                    rules = SolverRules.FULL
                )
                ?: HypothesisSolver.nextHypothesisStep(
                    puzzle = puzzle,
                    confirmed = snapshot.confirmed,
                    excluded = excluded,
                    maxDepth =
                        if (
                            puzzle.difficulty ==
                            GameDifficulty.INFERNAL
                        ) {
                            2
                        } else {
                            1
                        }
                )
                ?: return null

        return ProfessorHint(
            step = step,
            focusText = focusText(step),
            explanationText = explanationText(step),
            actionText = actionText(step),
            fromCachedTrace = cached != null
        )
    }

    private fun cachedStepFits(
        step: SolveStep,
        confirmed: Set<Cell>,
        excluded: Set<Cell>
    ): Boolean {
        if (!confirmed.containsAll(step.beforeConfirmed)) return false
        if (!excluded.containsAll(step.beforeExcluded)) return false

        step.cell?.let { target ->
            if (target in confirmed || target in excluded) return false
        }

        if (step.cell == null &&
            step.eliminated.none { it !in excluded }
        ) {
            return false
        }

        return true
    }

    private fun focusText(step: SolveStep): String =
        when (step.technique) {
            SolveTechnique.ROW_SINGLE ->
                "Prof Gecko : regarde " + (step.axis ?: "cette ligne") +
                    ". Il ne reste qu'une place possible."

            SolveTechnique.COLUMN_SINGLE ->
                "Prof Gecko : regarde " + (step.axis ?: "cette colonne") +
                    ". Il ne reste qu'une place possible."

            SolveTechnique.REGION_SINGLE ->
                "Prof Gecko : regarde la zone colorée surlignée. Il ne lui reste qu'un candidat."

            SolveTechnique.REGION_LOCKED ->
                "Prof Gecko : regarde les candidats surlignés et " +
                    (step.axis ?: "leur axe commun") + "."

            SolveTechnique.REGION_TOUCH_PROJECTION ->
                "Prof Gecko : regarde cette zone. Ses " +
                    step.sourceCells.size +
                    " candidats projettent la même interdiction."

            SolveTechnique.GECKO_X_WING ->
                "Prof Gecko : cherche le Gecko X-Wing sur " +
                    (step.axis ?: "les cases surlignées") + "."

            SolveTechnique.HYPOTHESIS_TEST ->
                "Prof Gecko : les déductions certaines sont épuisées. Testons les deux candidats de " +
                    (step.axis ?: "cette paire") + "."

            SolveTechnique.DOUBLE_HYPOTHESIS ->
                "Prof Gecko : niveau Infernal… cette paire demande une hypothèse dans l'hypothèse. Suis bien les cases surlignées."

            SolveTechnique.GIVEN ->
                "Prof Gecko : pars des geckos déjà donnés."
        }

    private fun explanationText(step: SolveStep): String =
        when (step.technique) {
            SolveTechnique.ROW_SINGLE ->
                "Toutes les autres cases de cette ligne sont impossibles. Son gecko est donc forcé."

            SolveTechnique.COLUMN_SINGLE ->
                "Toutes les autres cases de cette colonne sont impossibles. Son gecko est donc forcé."

            SolveTechnique.REGION_SINGLE ->
                "Une zone contient exactement un gecko. Toutes ses autres cases sont exclues : le dernier candidat est forcé."

            SolveTechnique.REGION_LOCKED ->
                "Les " + step.sourceCells.size +
                    " candidats restants sont enfermés sur " +
                    (step.axis ?: "le même axe") +
                    ". Le gecko devra occuper cet axe, donc les autres candidats concernés sont impossibles."

            SolveTechnique.REGION_TOUCH_PROJECTION ->
                "Le gecko de cette zone est forcément dans l'une de ces " +
                    step.sourceCells.size +
                    " cases. Chaque case cible toucherait le gecko quel que soit le candidat choisi, y compris en diagonale : elle est donc impossible."

            SolveTechnique.GECKO_X_WING ->
                "Deux geckos obligatoires sont enfermés dans les mêmes deux axes. On ne sait pas lequel prend quelle position, mais ces axes leur sont réservés : les autres candidats de ces axes sont impossibles."

            SolveTechnique.HYPOTHESIS_TEST -> {
                val bad = step.hypothesisRejected
                if (bad != null) {
                    "Si on suppose un gecko en ligne " +
                        (bad.row + 1) +
                        ", colonne " +
                        (bad.col + 1) +
                        ", les conséquences mènent à une contradiction. L'autre candidat est donc certain."
                } else {
                    "On teste une des deux possibilités. Une branche devient impossible, donc l'autre est forcée."
                }
            }

            SolveTechnique.DOUBLE_HYPOTHESIS -> {
                val bad = step.hypothesisRejected
                if (bad != null) {
                    "La première hypothèse en ligne " +
                        (bad.row + 1) +
                        ", colonne " +
                        (bad.col + 1) +
                        " ne se contredit qu'après un second test logique. Cette branche entière est impossible : l'autre candidat est forcé."
                } else {
                    "Il faut deux niveaux de test pour obtenir la contradiction. C'est une vraie déduction Infernal, pas un hasard."
                }
            }

            SolveTechnique.GIVEN ->
                "Un gecko donné est certain. Ses exclusions servent de point de départ."
        }

    private fun actionText(step: SolveStep): String {
        step.cell?.let { cell ->
            return "Action : gecko forcé en ligne " +
                (cell.row + 1) +
                ", colonne " +
                (cell.col + 1) +
                ". Fais un double-clic sur cette case."
        }

        val remainingTargets = step.eliminated.size
        if (remainingTargets == 1) {
            val cell = step.eliminated.first()
            return "Action : pose une croix en ligne " +
                (cell.row + 1) +
                ", colonne " +
                (cell.col + 1) +
                "."
        }

        return "Action : les " +
            remainingTargets +
            " cases encadrées sont impossibles. Tu peux y poser des croix."
    }
}

package com.greenpower2669.geckodoku

data class ProfessorHint(
    val step: SolveStep,
    val focusText: String,
    val explanationText: String,
    val actionText: String,
    val appliedText: String,
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
            appliedText = appliedText(step),
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
                "Prof Gecko : regarde les quatre positions qui forment le rectangle logique. Je vais te montrer les deux axes réellement réservés."

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
                xWingExplanation(
                    step
                )

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

    private fun appliedText(
        step: SolveStep
    ): String {
        if (
            step.technique ==
                SolveTechnique.HYPOTHESIS_TEST ||
            step.technique ==
                SolveTechnique.DOUBLE_HYPOTHESIS
        ) {
            val good = step.cell
            val bad =
                step.hypothesisRejected

            if (good != null &&
                bad != null
            ) {
                return "Je barre la branche contradictoire en ligne " +
                    (bad.row + 1) +
                    ", colonne " +
                    (bad.col + 1) +
                    ", puis je confirme l'autre gecko en ligne " +
                    (good.row + 1) +
                    ", colonne " +
                    (good.col + 1) +
                    "."
            }
        }

        step.cell?.let { cell ->
            return "Je place le gecko certain en ligne " +
                (cell.row + 1) +
                ", colonne " +
                (cell.col + 1) +
                "."
        }

        if (step.eliminated.size == 1) {
            val cell =
                step.eliminated.first()

            return "Cette case devient certainement impossible : ligne " +
                (cell.row + 1) +
                ", colonne " +
                (cell.col + 1) +
                "."
        }

        return "Je confirme " +
            step.eliminated.size +
            " exclusions certaines. Les barres montrent les axes concernés lorsqu'il y en a."
    }

    private fun xWingExplanation(
        step: SolveStep
    ): String {
        val rows =
            step.sourceCells
                .map {
                    it.row
                }
                .toSortedSet()

        val cols =
            step.sourceCells
                .map {
                    it.col
                }
                .toSortedSet()

        if (
            step.sourceCells.size ==
                4 &&
            rows.size ==
                2 &&
            cols.size ==
                2
        ) {
            val rowText =
                rows.joinToString(
                    " et "
                ) {
                    "ligne " +
                        (it + 1)
                }

            val colText =
                cols.joinToString(
                    " et "
                ) {
                    "colonne " +
                        (it + 1)
                }

            return if (
                step.sourceRegions.size ==
                    2
            ) {
                "La zone colorée A n’a plus que deux positions possibles et la zone colorée B possède les mêmes deux axes. Les quatre positions sont donc le rectangle formé par " +
                    rowText +
                    " et " +
                    colText +
                    ". On ne sait pas quel gecko prend quelle position, mais les deux axes sont réservés à A et B. Par projection, aucun autre candidat ne peut rester sur les axes d’exclusion montrés."
            } else {
                "Il y a exactement quatre positions : deux sur " +
                    rowText +
                    " et les mêmes deux sur " +
                    colText +
                    ". Le choix peut se croiser dans un sens ou dans l’autre, mais les deux axes sont forcément occupés par ces deux geckos. Par projection, les autres candidats de ces axes sont exclus."
            }
        }

        return "Projection : deux groupes de candidats réservent les mêmes axes. Je montre les axes concernés avant d’appliquer les exclusions."
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
            return "Action : cette case est exclue, ligne " +
                (cell.row + 1) +
                ", colonne " +
                (cell.col + 1) +
                "."
        }

        return "Action : les " +
            remainingTargets +
            " cases encadrées sont impossibles. Observe les axes réservés avant de continuer."
    }
}

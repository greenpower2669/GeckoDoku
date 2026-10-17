package com.greenpower2669.geckodoku

/**
 * Two comparable workshop experiments on the same archived X-Wing boards.
 * These are LABORATORY results, not automatic Infernal promotions.
 *
 * H1 + X-Wing: exactly one hypothesis (one level), and at least one
 * effective Gecko X-Wing elimination in the complete solution trace.
 *
 * H2 without X-Wing: a genuine nested depth-2 proof, strictly without
 * invoking the X-Wing solver rule, with an exhausted unsuccessful H1
 * search using the same no-X-Wing rules.
 */
enum class WorkshopComparisonLane(val label: String, val depth: Int) {
    H1_XWING("H1 + X-Wing", 1),
    H2_NO_XWING("H2 sans X-Wing", 2);

    val rules: SolverRules
        get() = if (this == H2_NO_XWING) {
            SolverRules.NO_X_WING
        } else {
            SolverRules.FULL
        }
}

enum class WorkshopComparisonVerdict { MATCH, NO_MATCH }

object WorkshopComparisonPolicy {
    fun qualifies(
        lane: WorkshopComparisonLane,
        proof: HypothesisAnalysis,
        shallow: HypothesisAnalysis? = null
    ): Boolean {
        if (!proof.solved || proof.exhaustedBudget) return false
        val wingSteps = proof.steps.count {
            it.technique == SolveTechnique.GECKO_X_WING &&
                it.eliminated.isNotEmpty()
        }
        return when (lane) {
            WorkshopComparisonLane.H1_XWING ->
                proof.maxHypothesisDepth == 1 &&
                    proof.hypothesisCount == 1 &&
                    wingSteps >= 1
            WorkshopComparisonLane.H2_NO_XWING ->
                proof.maxHypothesisDepth == 2 &&
                    proof.hypothesisCount > 0 &&
                    wingSteps == 0 &&
                    shallow != null &&
                    !shallow.solved &&
                    !shallow.exhaustedBudget
        }
    }
}

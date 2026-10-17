package com.greenpower2669.geckodoku

/** A detected Gecko X-Wing is evidence for exploration, not proof of Infernal difficulty. */
enum class WorkshopXWingStatus { PENDING, EXPLORED, PROMISING }

object WorkshopXWingTwoLayer {
    fun isProspect(step: SolveStep?): Boolean =
        step?.technique == SolveTechnique.GECKO_X_WING &&
            step.eliminated.isNotEmpty()

    /** Detection itself never promotes or approves a board. */
    fun isPromotedAutomatically(step: SolveStep?): Boolean = false

    fun afterProof(
        previous: WorkshopXWingStatus,
        interrupted: Boolean,
        accepted: Boolean
    ): WorkshopXWingStatus = when {
        interrupted -> previous
        accepted -> WorkshopXWingStatus.PROMISING
        else -> WorkshopXWingStatus.EXPLORED
    }
}

package com.greenpower2669.geckodoku

class ProfessorAnimationPolicy {
    fun shouldAnimate(
        animationsEnabled: Boolean,
        mediaBusy: Boolean,
        hypothesisPending: Boolean,
        celebrationVisible: Boolean
    ): Boolean =
        animationsEnabled &&
            !mediaBusy &&
            !hypothesisPending &&
            !celebrationVisible
}

package com.greenpower2669.geckodoku

enum class IntroPhase {
    FIRST,
    SECOND,
    DONE
}

class IntroLifecyclePolicy {
    val initialPhase =
        IntroPhase.FIRST

    fun professorEligible(
        phase: IntroPhase
    ): Boolean =
        phase != IntroPhase.FIRST

    fun mustMuteIntro(
        phase: IntroPhase,
        fxEnabled: Boolean
    ): Boolean =
        !fxEnabled

    fun onNaturalCompletion(
        phase: IntroPhase
    ): IntroPhase =
        when (phase) {
            IntroPhase.FIRST ->
                IntroPhase.SECOND

            IntroPhase.SECOND,
            IntroPhase.DONE ->
                IntroPhase.DONE
        }

    fun onSkip(
        phase: IntroPhase
    ): IntroPhase =
        IntroPhase.DONE
}

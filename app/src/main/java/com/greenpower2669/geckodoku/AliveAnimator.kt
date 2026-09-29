package com.greenpower2669.geckodoku

import java.util.Collections

enum class MascotKind {
    GECKO,
    BEE,
    PLANT
}

enum class AliveVisualState {
    HIDDEN,
    APPEARING,
    IDLE,
    CUTE,
    DISAPPEARING,
    STATIC_PNG
}

data class MascotAnimationProfile(
    val kind: MascotKind,
    val pngAsset: String?,
    val appearanceAsset: String?,
    val disappearanceAsset: String?,
    val idleAssets: List<String>,
    val cuteAssets: List<String>,
    val keyColor: ChromaKeyColor,
    val renderScale: Float = 1f
) {
    init {
        require(renderScale > 0f)
        require(
            idleAssets.isNotEmpty() ||
                cuteAssets.isNotEmpty() ||
                pngAsset != null
        )
    }
}

data class AliveAnimationDecision(
    val state: AliveVisualState,
    val assetPath: String? = null,
    val showPng: Boolean = false,
    val requestGroupRefresh: Boolean = false
)

class AliveAnimator(
    val profile: MascotAnimationProfile,
    private val recentLimit: Int = 3
) {
    private val recentAssets =
        mutableListOf<String>()

    private val completedSeries =
        mutableListOf<List<String>>()

    private val currentSeries =
        mutableListOf<String>()

    private var targetSeriesLength =
        4

    private var peerRefreshRequested =
        false

    var state: AliveVisualState =
        AliveVisualState.HIDDEN
        private set

    var currentAsset: String? = null
        private set

    fun start(
        animationsEnabled: Boolean,
        randomValue: Int
    ): AliveAnimationDecision {
        if (!animationsEnabled) {
            return staticDecision()
        }

        resetSeries(
            randomValue
        )

        val appearance =
            profile.appearanceAsset

        return if (appearance != null) {
            clipDecision(
                AliveVisualState.APPEARING,
                appearance,
                remember = false
            )
        } else {
            chooseIdle(
                randomValue
            )
        }
    }

    fun startAmbient(
        animationsEnabled: Boolean,
        randomValue: Int
    ): AliveAnimationDecision {
        if (!animationsEnabled) {
            return staticDecision()
        }

        resetSeries(
            randomValue
        )

        return chooseIdle(
            randomValue
        )
    }

    fun afterCurrentClip(
        animationsEnabled: Boolean,
        randomValue: Int
    ): AliveAnimationDecision {
        if (!animationsEnabled) {
            return staticDecision()
        }

        return when (state) {
            AliveVisualState.APPEARING ->
                chooseIdle(
                    randomValue
                )

            AliveVisualState.IDLE -> {
                if (peerRefreshRequested) {
                    peerRefreshRequested =
                        false
                    finalizeSeries()
                    resetSeries(
                        randomValue
                    )
                    chooseIdle(
                        randomValue
                    )
                } else if (
                    currentSeries.size >=
                        targetSeriesLength
                ) {
                    finalizeSeries()
                    resetSeries(
                        randomValue + 37
                    )
                    chooseIdle(
                        randomValue,
                        requestGroupRefresh =
                            true
                    )
                } else {
                    chooseIdle(
                        randomValue
                    )
                }
            }

            AliveVisualState.CUTE -> {
                finalizeSeries()
                resetSeries(
                    randomValue + 19
                )
                chooseIdle(
                    randomValue
                )
            }

            AliveVisualState.DISAPPEARING ->
                hiddenDecision()

            AliveVisualState.STATIC_PNG ->
                staticDecision()

            AliveVisualState.HIDDEN ->
                hiddenDecision()
        }
    }

    fun requestCute(
        animationsEnabled: Boolean,
        randomValue: Int
    ): AliveAnimationDecision {
        if (!animationsEnabled) {
            return staticDecision()
        }

        val candidates =
            profile.cuteAssets

        if (candidates.isEmpty()) {
            return chooseIdle(
                randomValue
            )
        }

        val asset =
            chooseWithoutImmediateRepeat(
                candidates,
                randomValue
            )

        return clipDecision(
            state =
                AliveVisualState.CUTE,
            asset = asset,
            remember = true
        )
    }

    fun requestDisappear(
        animationsEnabled: Boolean
    ): AliveAnimationDecision {
        val disappearance =
            profile.disappearanceAsset

        return if (
            animationsEnabled &&
            disappearance != null
        ) {
            clipDecision(
                AliveVisualState.DISAPPEARING,
                disappearance,
                remember = false
            )
        } else {
            hiddenDecision()
        }
    }

    fun requestPeerRefresh() {
        peerRefreshRequested = true
    }

    fun refreshAfterPeerRequest(
        animationsEnabled: Boolean,
        randomValue: Int
    ): AliveAnimationDecision {
        peerRefreshRequested = false
        finalizeSeries()
        return startAmbient(
            animationsEnabled,
            randomValue
        )
    }

    fun fallbackToStatic():
        AliveAnimationDecision =
        staticDecision()

    fun hideImmediately():
        AliveAnimationDecision =
        hiddenDecision()

    fun recentHistory():
        List<String> =
        Collections.unmodifiableList(
            recentAssets.toList()
        )

    fun seriesHistory():
        List<List<String>> =
        Collections.unmodifiableList(
            completedSeries
                .map {
                    it.toList()
                }
        )

    private fun chooseIdle(
        randomValue: Int,
        requestGroupRefresh:
            Boolean = false
    ): AliveAnimationDecision {
        val candidates =
            profile.idleAssets

        if (candidates.isEmpty()) {
            return if (
                profile.cuteAssets
                    .isNotEmpty()
            ) {
                requestCute(
                    animationsEnabled =
                        true,
                    randomValue =
                        randomValue
                )
            } else {
                staticDecision()
            }
        }

        val previousSeries =
            completedSeries
                .lastOrNull()
                .orEmpty()

        var pool =
            candidates.filter {
                it != currentAsset
            }

        if (pool.isEmpty()) {
            pool =
                candidates
        }

        if (
            currentSeries.isEmpty() &&
            previousSeries.isNotEmpty()
        ) {
            val withoutSameStart =
                pool.filter {
                    it !=
                        previousSeries
                            .first()
                }

            if (
                withoutSameStart
                    .isNotEmpty()
            ) {
                pool =
                    withoutSameStart
            }
        }

        if (
            currentSeries.size ==
                targetSeriesLength - 1 &&
            previousSeries.size ==
                targetSeriesLength &&
            currentSeries ==
                previousSeries
                    .take(
                        currentSeries
                            .size
                    )
        ) {
            val withoutSameEnd =
                pool.filter {
                    it !=
                        previousSeries
                            .last()
                }

            if (
                withoutSameEnd
                    .isNotEmpty()
            ) {
                pool =
                    withoutSameEnd
            }
        }

        val asset =
            chooseWithoutImmediateRepeat(
                pool,
                randomValue
            )

        currentSeries.add(
            asset
        )

        return clipDecision(
            state =
                AliveVisualState.IDLE,
            asset = asset,
            remember = true,
            requestGroupRefresh =
                requestGroupRefresh
        )
    }

    private fun chooseWithoutImmediateRepeat(
        candidates: List<String>,
        randomValue: Int
    ): String {
        val current =
            currentAsset

        val fresh =
            candidates.filter {
                it != current &&
                    it !in recentAssets
            }

        val notCurrent =
            candidates.filter {
                it != current
            }

        val pool =
            when {
                fresh.isNotEmpty() ->
                    fresh

                notCurrent.isNotEmpty() ->
                    notCurrent

                else ->
                    candidates
            }

        return pool[
            Math.floorMod(
                randomValue,
                pool.size
            )
        ]
    }

    private fun finalizeSeries() {
        if (
            currentSeries.isEmpty()
        ) {
            return
        }

        completedSeries.add(
            currentSeries.toList()
        )

        while (
            completedSeries.size >
                4
        ) {
            completedSeries.removeAt(0)
        }

        currentSeries.clear()
    }

    private fun resetSeries(
        randomValue: Int
    ) {
        currentSeries.clear()
        targetSeriesLength =
            3 +
                Math.floorMod(
                    randomValue,
                    3
                )
    }

    private fun clipDecision(
        state: AliveVisualState,
        asset: String,
        remember: Boolean,
        requestGroupRefresh:
            Boolean = false
    ): AliveAnimationDecision {
        this.state = state
        currentAsset = asset

        if (remember) {
            recentAssets.remove(asset)
            recentAssets.add(asset)

            while (
                recentAssets.size >
                    recentLimit
            ) {
                recentAssets.removeAt(0)
            }
        }

        return AliveAnimationDecision(
            state = state,
            assetPath = asset,
            requestGroupRefresh =
                requestGroupRefresh
        )
    }

    private fun staticDecision():
        AliveAnimationDecision {
        state =
            AliveVisualState.STATIC_PNG
        currentAsset = null

        return AliveAnimationDecision(
            state =
                AliveVisualState.STATIC_PNG,
            showPng =
                profile.pngAsset != null
        )
    }

    private fun hiddenDecision():
        AliveAnimationDecision {
        state =
            AliveVisualState.HIDDEN
        currentAsset = null

        return AliveAnimationDecision(
            state =
                AliveVisualState.HIDDEN
        )
    }
}

class MascotLifeCoordinator(
    animators:
        Collection<AliveAnimator>
) {
    private val byKind =
        animators.associateBy {
            it.profile.kind
        }

    fun start(
        kind: MascotKind,
        animationsEnabled: Boolean,
        randomValue: Int
    ): AliveAnimationDecision =
        requireNotNull(
            byKind[kind]
        ).start(
            animationsEnabled,
            randomValue
        )

    fun afterClip(
        kind: MascotKind,
        animationsEnabled: Boolean,
        randomValue: Int
    ): AliveAnimationDecision {
        val animator =
            requireNotNull(
                byKind[kind]
            )

        val decision =
            animator.afterCurrentClip(
                animationsEnabled,
                randomValue
            )

        if (
            decision
                .requestGroupRefresh
        ) {
            byKind.values
                .filter {
                    it !== animator
                }
                .forEach {
                    it.requestPeerRefresh()
                }
        }

        return decision
    }

    fun disappear(
        kind: MascotKind,
        animationsEnabled: Boolean
    ): AliveAnimationDecision =
        requireNotNull(
            byKind[kind]
        ).requestDisappear(
            animationsEnabled
        )

    fun fallback(
        kind: MascotKind
    ): AliveAnimationDecision =
        requireNotNull(
            byKind[kind]
        ).fallbackToStatic()

    fun hide(
        kind: MascotKind
    ): AliveAnimationDecision =
        requireNotNull(
            byKind[kind]
        ).hideImmediately()
}

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
    val boardOwnsStaticPng: Boolean,
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
    private val recentLimit: Int = 3,
    private val refreshAfterIdleCycles: Int = 4
) {
    private val recentAssets =
        mutableListOf<String>()

    private var idleCyclesSinceRefresh =
        0

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

        idleCyclesSinceRefresh = 0

        val appearance =
            profile.appearanceAsset

        return if (appearance != null) {
            clipDecision(
                AliveVisualState.APPEARING,
                appearance,
                remember = false
            )
        } else {
            chooseAmbient(
                randomValue
            )
        }
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
                chooseAmbient(
                    randomValue
                )

            AliveVisualState.IDLE -> {
                idleCyclesSinceRefresh += 1

                val requestRefresh =
                    idleCyclesSinceRefresh >=
                        refreshAfterIdleCycles

                if (requestRefresh) {
                    idleCyclesSinceRefresh = 0
                }

                chooseAmbient(
                    randomValue,
                    requestGroupRefresh =
                        requestRefresh
                )
            }

            AliveVisualState.CUTE -> {
                idleCyclesSinceRefresh = 0

                chooseAmbient(
                    randomValue,
                    requestGroupRefresh = true
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

    private fun chooseAmbient(
        randomValue: Int,
        requestGroupRefresh:
            Boolean = false
    ): AliveAnimationDecision {
        val preferCute =
            profile.cuteAssets
                .isNotEmpty() &&
                Math.floorMod(
                    randomValue,
                    5
                ) == 4

        val preferred =
            if (preferCute) {
                profile.cuteAssets
            } else {
                profile.idleAssets
            }

        val fallback =
            if (preferred.isNotEmpty()) {
                preferred
            } else if (
                profile.idleAssets
                    .isNotEmpty()
            ) {
                profile.idleAssets
            } else {
                profile.cuteAssets
            }

        if (fallback.isEmpty()) {
            return staticDecision()
        }

        val asset =
            chooseWithoutImmediateRepeat(
                candidates = fallback,
                randomValue =
                    if (peerRefreshRequested) {
                        randomValue + 1
                    } else {
                        randomValue
                    }
            )

        peerRefreshRequested = false

        val nextState =
            if (
                asset in
                    profile.cuteAssets
            ) {
                AliveVisualState.CUTE
            } else {
                AliveVisualState.IDLE
            }

        return clipDecision(
            state = nextState,
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
                !profile
                    .boardOwnsStaticPng
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

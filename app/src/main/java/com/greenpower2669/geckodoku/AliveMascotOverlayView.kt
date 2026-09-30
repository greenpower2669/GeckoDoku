package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.roundToInt
import kotlin.random.Random

class AliveMascotOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private data class VideoBackend(
        val view: View,
        val playback: ChromaKeyPlayback,
        val name: String
    )

    private data class Placement(
        val ownerKey: String,
        val targetProvider: () -> RectF?,
        val clipProvider: (() -> RectF?)?,
        val setStaticSuppressed: (Boolean) -> Unit,
        val eligible: () -> Boolean,
        val yellowTint: Boolean,
        val draggable: Boolean
    )

    private inner class Presence(
        val profile: MascotAnimationProfile,
        val ownerKey: String,
        var placement: Placement
    ) {
        val animator =
            AliveAnimator(profile)

        val pngContainer =
            FrameLayout(context).apply {
                importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable =
                    placement.draggable
            }

        val videoContainer =
            FrameLayout(context).apply {
                visibility =
                    View.INVISIBLE
                importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable =
                    placement.draggable
            }

        val image =
            ImageView(context).apply {
                scaleType =
                    ImageView.ScaleType.FIT_CENTER
                importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_NO

                profile.pngAsset
                    ?.let(::bitmapFor)
                    ?.let(::setImageBitmap)

                scaleX =
                    profile.pngScale
                scaleY =
                    profile.pngScale
            }

        private val videoBackend =
            createVideoBackend(
                ownerKey
            )

        val video =
            videoBackend.view.apply {
                visibility =
                    View.INVISIBLE
                importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable = false
            }

        val playback =
            videoBackend.playback.apply {
                logicalLayer =
                    "ALIVE_" +
                        profile.kind.name +
                        "_" +
                        ownerKey.replace(':', '_')
                setKeyColor(
                    profile.keyColor
                )
            }

        var pendingDecision:
            AliveAnimationDecision? = null

        var currentDecision:
            AliveAnimationDecision? = null

        var videoActive = false
        var videoFirstFrameVisible = false
        var removing = false
        var forceCute = false
        var staticSuppressed = false
        var hideStaticUntilAppearanceEnds = false
        var generation = 0

        var stableFrame: Bitmap? = null
        var stableFrameReadyHeight = -1
        var stableFrameRequestedHeight = -1
        var stableFrameRequestGeneration = 0
        var stableFrameVisibleLoggedHeight = -1
        var usingStableFrame = false
        var legacyPngLogged = false

        var cycleCallbackArmed = false

        lateinit var cycleRunnable:
            Runnable

        var dragCenterXFraction:
            Float? =
            if (
                profile.kind ==
                    MascotKind.PLANT
            ) {
                storedFraction(PLANT_X_KEY)
            } else {
                null
            }

        var dragCenterYFraction:
            Float? =
            if (
                profile.kind ==
                    MascotKind.PLANT
            ) {
                storedFraction(PLANT_Y_KEY)
            } else {
                null
            }

        var dragStartRawX = 0f
        var dragStartRawY = 0f
        var dragStartLeft = 0
        var dragStartTop = 0

        init {
            pngContainer.addView(
                image,
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )

            videoContainer.addView(
                video,
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )

            val dragListener =
                View.OnTouchListener {
                    _,
                    event ->
                if (placement.draggable) {
                    handleDrag(this, event)
                } else {
                    false
                }
            }

            pngContainer.setOnTouchListener(
                dragListener
            )
            videoContainer.setOnTouchListener(
                dragListener
            )

            cycleRunnable =
                Runnable {
                    cycleCallbackArmed = false
                    advancePresence(this)
                }
        }
    }

    private val positionPrefs =
        context.getSharedPreferences(
            "alive_mascot_positions",
            Context.MODE_PRIVATE
        )

    private val bitmapCache =
        mutableMapOf<String, Bitmap?>()

    private val yellowImageFilter =
        ColorMatrixColorFilter(
            ColorMatrix(
                GomokuYellowFilterPolicy
                    .colorMatrixValues()
            )
        )

    private val presences =
        linkedMapOf<String, Presence>()

    private var introSuppressed = false

    private var lastCuteOwnerKey:
        String? = null

    var onGroupCycleCompleted:
        ((MascotKind, String) -> Unit)? =
        null

    var animationsEnabled =
        true
        set(value) {
            if (field == value) {
                return
            }

            field = value

            presences.values
                .toList()
                .forEach {
                    presence ->
                    cancelPresencePlayback(
                        presence,
                        preserveCurrentDecision = false
                    )

                    presence.forceCute = false
                    presence.hideStaticUntilAppearanceEnds =
                        false

                    presence.pendingDecision =
                        if (value) {
                            presence.animator
                                .startAmbient(
                                    animationsEnabled = true,
                                    randomValue =
                                        Random.nextInt()
                                )
                        } else {
                            presence.animator
                                .start(
                                    animationsEnabled = false,
                                    randomValue = 0
                                )
                        }

                    if (value) {
                        presence.legacyPngLogged =
                            false
                    } else {
                        presence
                            .stableFrameVisibleLoggedHeight =
                            -1
                    }

                    applyStaticPlaceholder(
                        presence
                    )
                    refreshPresenceTarget(presence)

                    if (value) {
                        schedulePresence(
                            presence,
                            randomPauseMs()
                        )
                    }
                }

            refreshVisibility()
        }

    private fun createVideoBackend(
        ownerKey: String
    ): VideoBackend {
        val useSprite =
            AliveVideoBackendPolicy
                .useSpritePlayback(
                    ownerKey
                )

        val useTexture =
            AliveVideoBackendPolicy
                .useTextureView(
                    ownerKey =
                        ownerKey,
                    sdkInt =
                        Build.VERSION.SDK_INT
                )

        val backend =
            when {
                useSprite -> {
                    val view =
                        ChromaKeySpriteView(
                            context
                        )

                    VideoBackend(
                        view = view,
                        playback = view,
                        name =
                            "SpriteRGBA"
                    )
                }

                useTexture &&
                    Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.TIRAMISU -> {
                    val view =
                        ChromaKeyTextureView(
                            context
                        )

                    VideoBackend(
                        view = view,
                        playback = view,
                        name =
                            "TextureView"
                    )
                }

                else -> {
                    val view =
                        ChromaKeyVideoView(
                            context
                        )

                    VideoBackend(
                        view = view,
                        playback = view,
                        name =
                            "GLSurfaceView"
                    )
                }
            }

        MediaTrace.event(
            source =
                "AliveMascotOverlay",
            event =
                "ALIVE_VIDEO_BACKEND",
            detail =
                "owner=" +
                    ownerKey +
                    " backend=" +
                    backend.name +
                    " sdk=" +
                    Build.VERSION.SDK_INT
        )

        return backend
    }

    init {
        clipChildren = false
        clipToPadding = false
        isClickable = false
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    fun show(
        kind: MascotKind,
        ownerKey: String,
        targetProvider: () -> RectF?,
        clipProvider:
            (() -> RectF?)? = null,
        setStaticSuppressed:
            (Boolean) -> Unit = {},
        eligible: () -> Boolean = {
            true
        },
        yellowTint: Boolean = false,
        draggable: Boolean = false,
        animateAppearance: Boolean = true
    ) {
        val id =
            presenceId(kind, ownerKey)

        val placement =
            Placement(
                ownerKey = ownerKey,
                targetProvider = targetProvider,
                clipProvider =
                    clipProvider,
                setStaticSuppressed =
                    setStaticSuppressed,
                eligible = eligible,
                yellowTint = yellowTint,
                draggable = draggable
            )

        val existing =
            presences[id]

        if (existing != null) {
            existing.placement =
                placement
            existing.pngContainer.isClickable =
                draggable
            existing.videoContainer.isClickable =
                draggable
            applyTint(existing)
            val hasTarget =
                refreshPresenceTarget(existing)

            if (
                animationsEnabled &&
                !introSuppressed &&
                !existing.videoActive &&
                existing.pendingDecision == null &&
                !existing.removing
            ) {
                existing.pendingDecision =
                    existing.animator
                        .startAmbient(
                            animationsEnabled = true,
                            randomValue =
                                Random.nextInt()
                        )
                schedulePresence(
                    existing,
                    if (hasTarget) {
                        randomPauseMs()
                    } else {
                        TARGET_RETRY_MS
                    }
                )
            }
            return
        }

        val presence =
            Presence(
                profile =
                    profileFor(kind),
                ownerKey = ownerKey,
                placement = placement
            )

        presences[id] =
            presence

        addView(
            presence.pngContainer,
            LayoutParams(1, 1)
        )
        addView(
            presence.videoContainer,
            LayoutParams(1, 1)
        )

        applyTint(presence)

        val initialDecision =
            if (animationsEnabled) {
                if (animateAppearance) {
                    presence.animator
                        .start(
                            animationsEnabled = true,
                            randomValue =
                                Random.nextInt()
                        )
                } else {
                    presence.animator
                        .startAmbient(
                            animationsEnabled = true,
                            randomValue =
                                Random.nextInt()
                        )
                }
            } else {
                presence.animator
                    .start(
                        animationsEnabled = false,
                        randomValue = 0
                    )
            }

        presence.pendingDecision =
            initialDecision
        presence.hideStaticUntilAppearanceEnds =
            initialDecision.state ==
                AliveVisualState.APPEARING

        applyStaticPlaceholder(
            presence
        )

        val hasTarget =
            refreshPresenceTarget(presence)

        if (
            animationsEnabled &&
            !introSuppressed
        ) {
            schedulePresence(
                presence,
                when {
                    presence
                        .hideStaticUntilAppearanceEnds ->
                        0L
                    hasTarget ->
                        Random.nextLong(
                            80L,
                            700L
                        )
                    else ->
                        TARGET_RETRY_MS
                }
            )
        }

        refreshVisibility()
    }

    fun requestCute(
        kind: MascotKind,
        ownerKey: String
    ) {
        val presence =
            presences[
                presenceId(
                    kind,
                    ownerKey
                )
            ] ?: return

        if (
            !animationsEnabled ||
            introSuppressed ||
            presence.removing ||
            !presence.placement
                .eligible()
        ) {
            return
        }

        if (presence.videoActive) {
            presence.forceCute = true
        } else {
            presence.pendingDecision =
                presence.animator
                    .requestCute(
                        animationsEnabled = true,
                        randomValue =
                            Random.nextInt()
                    )

            schedulePresence(
                presence,
                0L
            )
        }
    }

    fun retainOwners(
        kind: MascotKind,
        ownerPrefix: String,
        ownerKeys: Set<String>
    ) {
        presences.values
            .filter {
                it.profile.kind == kind &&
                    it.ownerKey
                        .startsWith(ownerPrefix) &&
                    it.ownerKey !in
                        ownerKeys
            }
            .toList()
            .forEach(::removePresence)

        refreshVisibility()
    }

    fun disappear(
        kind: MascotKind,
        ownerKey: String,
        targetProvider: () -> RectF?,
        clipProvider:
            (() -> RectF?)? = null,
        setStaticSuppressed:
            (Boolean) -> Unit = {},
        yellowTint: Boolean = false
    ) {
        val presence =
            presences[
                presenceId(kind, ownerKey)
            ]
                ?: return

        presence.placement =
            presence.placement.copy(
                targetProvider =
                    targetProvider,
                clipProvider =
                    clipProvider,
                setStaticSuppressed =
                    setStaticSuppressed,
                eligible = {
                    false
                },
                yellowTint = yellowTint
            )

        presence.removing = true
        presence.hideStaticUntilAppearanceEnds =
            false

        cancelPresencePlayback(
            presence,
            preserveCurrentDecision = false
        )

        val decision =
            presence.animator
                .requestDisappear(
                    animationsEnabled
                )

        if (
            decision.state ==
                AliveVisualState.HIDDEN
        ) {
            removePresence(presence)
            return
        }

        presence.pendingDecision =
            decision
        presence.image.visibility =
            View.VISIBLE
        refreshPresenceTarget(presence)

        if (
            animationsEnabled &&
            !introSuppressed
        ) {
            schedulePresence(
                presence,
                0L
            )
        }
    }

    fun setIntroSuppressed(
        suppressed: Boolean
    ) {
        if (
            introSuppressed ==
                suppressed
        ) {
            return
        }

        introSuppressed =
            suppressed

        if (suppressed) {
            presences.values
                .toList()
                .forEach {
                    cancelPresencePlayback(
                        it,
                        preserveCurrentDecision =
                            false
                    )
                }
            visibility =
                View.INVISIBLE
            return
        }

        presences.values
            .toList()
            .forEach {
                presence ->
                if (presence.removing) {
                    removePresence(presence)
                } else {
                    presence.hideStaticUntilAppearanceEnds =
                        false
                    presence.forceCute = false
                    applyStaticPlaceholder(
                        presence
                    )
                    presence.pendingDecision =
                        if (animationsEnabled) {
                            presence.animator
                                .startAmbient(
                                    animationsEnabled = true,
                                    randomValue =
                                        Random.nextInt()
                                )
                        } else {
                            presence.animator
                                .start(
                                    animationsEnabled = false,
                                    randomValue = 0
                                )
                        }

                    refreshPresenceTarget(presence)

                    if (animationsEnabled) {
                        schedulePresence(
                            presence,
                            Random.nextLong(
                                50L,
                                450L
                            )
                        )
                    }
                }
            }

        refreshVisibility()
    }

    fun refreshStableFramesForResolution() {
        presences.values
            .toList()
            .forEach { presence ->
                if (
                    StableFramePolicy
                        .specFor(
                            presence.profile.kind
                        ) == null
                ) {
                    return@forEach
                }

                presence.stableFrame = null
                presence.stableFrameReadyHeight = -1
                presence.stableFrameRequestedHeight = -1
                presence.stableFrameVisibleLoggedHeight = -1
                presence.usingStableFrame = false

                applyStaticPlaceholder(
                    presence
                )
            }

        refreshVisibility()
    }

    fun stop(
        kind: MascotKind
    ) {
        presences.values
            .filter {
                it.profile.kind == kind
            }
            .toList()
            .forEach(::removePresence)
        refreshVisibility()
    }

    fun stopBoardMascots() {
        stop(MascotKind.GECKO)
        stop(MascotKind.BEE)
    }

    fun stopAll() {
        presences.values
            .toList()
            .forEach(::removePresence)
        refreshVisibility()
    }

    fun refreshDynamicTargets() {
        presences.values
            .toList()
            .forEach {
                presence ->
                if (
                    !presence.removing &&
                    !presence.placement
                        .eligible()
                ) {
                    removePresence(presence)
                } else {
                    val hasTarget =
                        refreshPresenceTarget(
                            presence
                        )

                    if (
                        !hasTarget &&
                        presence.videoActive
                    ) {
                        cancelPresencePlayback(
                            presence,
                            preserveCurrentDecision =
                                true
                        )
                    }

                    if (
                        animationsEnabled &&
                        !introSuppressed &&
                        !presence.videoActive &&
                        presence.pendingDecision !=
                            null
                    ) {
                        schedulePresence(
                            presence,
                            if (hasTarget) {
                                40L
                            } else {
                                TARGET_RETRY_MS
                            }
                        )
                    }
                }
            }

        refreshVisibility()
    }

    fun release() {
        presences.values
            .toList()
            .forEach(::removePresence)
        bitmapCache.clear()
        visibility = View.GONE
    }

    private fun advancePresence(
        presence: Presence
    ) {
        if (
            introSuppressed ||
            !presences.values
                .contains(presence)
        ) {
            return
        }

        if (
            !presence.removing &&
            !presence.placement
                .eligible()
        ) {
            removePresence(presence)
            return
        }

        val hasTarget =
            refreshPresenceTarget(presence)

        if (!hasTarget) {
            if (presence.videoActive) {
                cancelPresencePlayback(
                    presence,
                    preserveCurrentDecision =
                        true
                )
            }
            if (animationsEnabled) {
                schedulePresence(
                    presence,
                    TARGET_RETRY_MS
                )
            }
            return
        }

        if (!animationsEnabled) {
            showLegacyPng(
                presence
            )
            refreshVisibility()
            return
        }

        if (presence.videoActive) {
            return
        }

        val decision =
            presence.pendingDecision
                ?: if (presence.forceCute) {
                    presence.forceCute = false
                    presence.animator
                        .requestCute(
                            animationsEnabled = true,
                            randomValue =
                                Random.nextInt()
                        )
                } else {
                    presence.animator
                        .startAmbient(
                            animationsEnabled = true,
                            randomValue =
                                Random.nextInt()
                        )
                }

        presence.pendingDecision = null

        when (decision.state) {
            AliveVisualState.HIDDEN ->
                removePresence(presence)

            AliveVisualState.STATIC_PNG -> {
                applyStaticPlaceholder(
                    presence
                )
                presence.video.visibility =
                    View.INVISIBLE
                presence.videoContainer.visibility =
                    View.INVISIBLE
                presence.currentDecision =
                    null
                refreshVisibility()
            }

            AliveVisualState.APPEARING,
            AliveVisualState.IDLE,
            AliveVisualState.CUTE,
            AliveVisualState.DISAPPEARING -> {
                val asset =
                    decision.assetPath

                if (asset == null) {
                    fallbackAfterMediaFailure(
                        presence
                    )
                } else {
                    playDecision(
                        presence,
                        decision,
                        asset
                    )
                }
            }
        }
    }

    private fun playDecision(
        presence: Presence,
        decision: AliveAnimationDecision,
        assetPath: String
    ) {
        if (
            !refreshPresenceTarget(
                presence
            )
        ) {
            presence.pendingDecision =
                decision
            schedulePresence(
                presence,
                TARGET_RETRY_MS
            )
            return
        }

        presence.generation += 1
        val generation =
            presence.generation

        presence.currentDecision =
            decision
        presence.videoActive = true
        presence.videoFirstFrameVisible =
            false

        val strictAppearance =
            decision.state ==
                AliveVisualState.APPEARING &&
                presence
                    .hideStaticUntilAppearanceEnds

        if (strictAppearance) {
            presence.image.visibility =
                View.INVISIBLE
        } else {
            applyStaticPlaceholder(
                presence
            )
        }

        presence.videoContainer.visibility =
            View.VISIBLE

        presence.playback
            .setKeyColor(
                presence.profile
                    .keyColor
            )
        presence.playback
            .setYellowTint(
                presence.placement
                    .yellowTint
            )
        presence.video.visibility =
            View.VISIBLE

        MediaTrace.event(
            source = "AliveMascotOverlay",
            event = "ALIVE_PLAY",
            assetPath = assetPath,
            detail =
                "kind=" +
                    presence.profile.kind +
                    " owner=" +
                    presence.ownerKey +
                    " state=" +
                    decision.state
        )

        presence.playback.play(
            assetPath = assetPath,
            muted = true,
            revealOnFirstFrame = true,
            onFirstFrameRendered = {
                if (
                    generation !=
                        presence.generation ||
                    !presences.values
                        .contains(presence)
                ) {
                    return@play
                }

                presence.videoFirstFrameVisible =
                    true

                if (
                    presence.usingStableFrame
                ) {
                    val spec =
                        StableFramePolicy
                            .specFor(
                                presence
                                    .profile
                                    .kind
                            )

                    MediaTrace.event(
                        source =
                            "AliveMascotOverlay",
                        event =
                            "STABLE_FRAME_TO_SPRITE",
                        assetPath =
                            assetPath,
                        detail =
                            "stableSource=" +
                                (
                                    spec
                                        ?.assetPath
                                        ?: "none"
                                    ) +
                                " species=" +
                                presence
                                    .profile
                                    .kind +
                                " resolution=" +
                                RichMediaSettings
                                    .spriteResolutionFor(
                                        context
                                    )
                                    .label
                    )
                }

                presence.image.visibility =
                    View.INVISIBLE
            },
            onStarted = {},
            onCompletion = {
                if (
                    generation !=
                        presence.generation ||
                    !presences.values
                        .contains(presence)
                ) {
                    return@play
                }

                onPresenceClipCompleted(
                    presence,
                    decision
                )
            },
            onError = {
                    _ ->
                if (
                    generation !=
                        presence.generation ||
                    !presences.values
                        .contains(presence)
                ) {
                    return@play
                }

                fallbackAfterMediaFailure(
                    presence
                )
            }
        )

        refreshVisibility()
    }

    private fun onPresenceClipCompleted(
        presence: Presence,
        completed:
            AliveAnimationDecision
    ) {
        presence.videoActive = false
        presence.videoFirstFrameVisible =
            false
        presence.currentDecision = null
        presence.video.visibility =
            View.INVISIBLE
        presence.videoContainer.visibility =
            View.INVISIBLE

        if (
            completed.state ==
                AliveVisualState.APPEARING
        ) {
            presence.hideStaticUntilAppearanceEnds =
                false
        }

        if (
            presence.removing ||
            completed.state ==
                AliveVisualState.DISAPPEARING
        ) {
            removePresence(presence)
            return
        }

        if (
            !presence.placement
                .eligible()
        ) {
            removePresence(presence)
            return
        }

        applyStaticPlaceholder(
            presence
        )

        val normalDecision =
            presence.animator
                .afterCurrentClip(
                    animationsEnabled =
                        animationsEnabled,
                    randomValue =
                        Random.nextInt()
                )

        if (
            normalDecision
                .requestGroupRefresh
        ) {
            coordinateGrandCycle(
                origin = presence
            )
        }

        presence.pendingDecision =
            if (presence.forceCute) {
                presence.forceCute = false
                presence.animator
                    .requestCute(
                        animationsEnabled =
                            animationsEnabled,
                        randomValue =
                            Random.nextInt()
                    )
            } else {
                normalDecision
            }

        schedulePresence(
            presence,
            randomPauseMs()
        )

        if (
            normalDecision
                .requestGroupRefresh
        ) {
            val kind =
                presence.profile.kind
            val ownerKey =
                presence.ownerKey

            post {
                onGroupCycleCompleted
                    ?.invoke(
                        kind,
                        ownerKey
                    )
            }
        }
    }

    private fun fallbackAfterMediaFailure(
        presence: Presence
    ) {
        presence.generation += 1
        presence.playback.stopPlayback()
        presence.video.visibility =
            View.INVISIBLE
        presence.videoContainer.visibility =
            View.INVISIBLE
        presence.videoActive = false
        presence.videoFirstFrameVisible =
            false
        presence.currentDecision = null
        presence.hideStaticUntilAppearanceEnds =
            false
        applyStaticPlaceholder(
            presence
        )

        if (presence.removing) {
            removePresence(presence)
            return
        }

        if (
            animationsEnabled &&
            presence.placement
                .eligible()
        ) {
            presence.pendingDecision =
                presence.animator
                    .startAmbient(
                        animationsEnabled = true,
                        randomValue =
                            Random.nextInt()
                    )
            schedulePresence(
                presence,
                MEDIA_RETRY_MS
            )
        } else {
            presence.pendingDecision =
                presence.animator
                    .fallbackToStatic()
        }

        refreshVisibility()
    }

    private fun applyStaticPlaceholder(
        presence: Presence
    ) {
        val useStable =
            StableFramePolicy
                .useStableFrame(
                    kind =
                        presence.profile.kind,
                    animationsEnabled =
                        animationsEnabled
                )

        if (!useStable) {
            showLegacyPng(
                presence
            )
            return
        }

        val resolution =
            RichMediaSettings
                .spriteResolutionFor(
                    context
                )

        val stable =
            presence.stableFrame
                ?.takeIf {
                    presence
                        .stableFrameReadyHeight ==
                        resolution.heightPx
                }

        if (stable == null) {
            presence.usingStableFrame =
                false
            presence.image.visibility =
                View.INVISIBLE

            requestStableFrame(
                presence = presence,
                resolution = resolution
            )
            return
        }

        presence.image.setImageBitmap(
            stable
        )
        presence.usingStableFrame = true

        val mayShow =
            !presence
                .hideStaticUntilAppearanceEnds &&
                (
                    !presence.videoActive ||
                        !presence
                            .videoFirstFrameVisible
                    )

        presence.image.visibility =
            if (mayShow) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

        if (
            mayShow &&
            presence
                .stableFrameVisibleLoggedHeight !=
                resolution.heightPx
        ) {
            val spec =
                StableFramePolicy
                    .specFor(
                        presence.profile.kind
                    )

            MediaTrace.event(
                source =
                    "AliveMascotOverlay",
                event =
                    "STABLE_FRAME_SHOW",
                assetPath =
                    spec?.assetPath,
                detail =
                    "species=" +
                        presence.profile.kind +
                        " resolution=" +
                        resolution.label +
                        " owner=" +
                        presence.ownerKey
            )

            presence
                .stableFrameVisibleLoggedHeight =
                resolution.heightPx
        }
    }

    private fun requestStableFrame(
        presence: Presence,
        resolution: SpriteResolution
    ) {
        if (
            presence.stableFrameRequestedHeight ==
                resolution.heightPx
        ) {
            return
        }

        val spec =
            StableFramePolicy
                .specFor(
                    presence.profile.kind
                )
                ?: return

        presence.stableFrameRequestedHeight =
            resolution.heightPx
        presence.stableFrameRequestGeneration += 1

        val requestGeneration =
            presence
                .stableFrameRequestGeneration

        MediaTrace.event(
            source =
                "AliveMascotOverlay",
            event =
                "STABLE_FRAME_REQUEST",
            assetPath =
                spec.assetPath,
            detail =
                "species=" +
                    presence.profile.kind +
                    " resolution=" +
                    resolution.label +
                    " cache=CHECK" +
                    " owner=" +
                    presence.ownerKey
        )

        SpriteFrameCache
            .requestStableFrame(
                context = context,
                assetPath =
                    spec.assetPath,
                keyColor =
                    spec.keyColor,
                resolution =
                    resolution
            ) stableCallback@ { result ->
                if (
                    !presences.values
                        .contains(presence) ||
                    requestGeneration !=
                        presence
                            .stableFrameRequestGeneration
                ) {
                    return@stableCallback
                }

                result.fold(
                    onSuccess = { stable ->
                        presence.stableFrame =
                            stable.bitmap
                        presence
                            .stableFrameReadyHeight =
                            resolution.heightPx

                        MediaTrace.event(
                            source =
                                "AliveMascotOverlay",
                            event =
                                "STABLE_FRAME_READY",
                            assetPath =
                                spec.assetPath,
                            detail =
                                "species=" +
                                    presence
                                        .profile
                                        .kind +
                                    " resolution=" +
                                    resolution
                                        .label +
                                    " extractionMs=" +
                                    stable.elapsedMs +
                                    " cache=" +
                                    (
                                        if (
                                            stable.cacheHit
                                        ) {
                                            "HIT"
                                        } else {
                                            "MISS"
                                        }
                                    ) +
                                    " layer=" +
                                    stable.cacheLayer +
                                    " size=" +
                                    stable.width +
                                    "x" +
                                    stable.height
                        )

                        applyStaticPlaceholder(
                            presence
                        )
                        refreshVisibility()
                    },
                    onFailure = { error ->
                        presence
                            .stableFrameRequestedHeight =
                            -1

                        MediaTrace.event(
                            source =
                                "AliveMascotOverlay",
                            event =
                                "STABLE_FRAME_ERROR",
                            assetPath =
                                spec.assetPath,
                            detail =
                                "species=" +
                                    presence
                                        .profile
                                        .kind +
                                    " resolution=" +
                                    resolution
                                        .label +
                                    " error=" +
                                    error.javaClass
                                        .simpleName
                        )
                    }
                )
            }
    }

    private fun showLegacyPng(
        presence: Presence
    ) {
        presence.profile
            .pngAsset
            ?.let(::bitmapFor)
            ?.let(
                presence.image::setImageBitmap
            )

        presence.usingStableFrame = false

        presence.image.visibility =
            if (
                presence
                    .hideStaticUntilAppearanceEnds
            ) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }

        if (
            !animationsEnabled &&
            StableFramePolicy
                .specFor(
                    presence.profile.kind
                ) != null &&
            !presence.legacyPngLogged
        ) {
            MediaTrace.event(
                source =
                    "AliveMascotOverlay",
                event =
                    "LEGACY_PNG_SHOW",
                assetPath =
                    presence.profile
                        .pngAsset,
                detail =
                    "reason=ANIMATIONS_DISABLED" +
                        " species=" +
                        presence.profile.kind +
                        " owner=" +
                        presence.ownerKey
            )

            presence.legacyPngLogged = true
        }
    }

    private fun coordinateGrandCycle(
        origin: Presence
    ) {
        val peers =
            presences.values
                .filter {
                    it !== origin &&
                        !it.removing &&
                        it.placement
                            .eligible()
                }

        peers.forEach {
            peer ->
            if (peer.videoActive) {
                peer.animator
                    .requestPeerRefresh()
            } else {
                peer.pendingDecision =
                    peer.animator
                        .refreshAfterPeerRequest(
                            animationsEnabled =
                                animationsEnabled,
                            randomValue =
                                Random.nextInt()
                        )
                schedulePresence(
                    peer,
                    Random.nextLong(
                        250L,
                        1200L
                    )
                )
            }
        }

        val cuteCandidates =
            peers.filter {
                it.profile
                    .cuteAssets
                    .isNotEmpty()
            }

        if (cuteCandidates.isEmpty()) {
            return
        }

        val withoutLast =
            cuteCandidates.filter {
                it.ownerKey !=
                    lastCuteOwnerKey
            }

        val pool =
            if (
                withoutLast.isNotEmpty()
            ) {
                withoutLast
            } else {
                cuteCandidates
            }

        val friend =
            pool[
                Random.nextInt(pool.size)
            ]

        lastCuteOwnerKey =
            friend.ownerKey

        if (friend.videoActive) {
            friend.forceCute = true
        } else {
            friend.pendingDecision =
                friend.animator
                    .requestCute(
                        animationsEnabled =
                            animationsEnabled,
                        randomValue =
                            Random.nextInt()
                    )
            schedulePresence(
                friend,
                Random.nextLong(
                    100L,
                    500L
                )
            )
        }
    }

    private fun schedulePresence(
        presence: Presence,
        delayMs: Long
    ) {
        if (
            !animationsEnabled ||
            introSuppressed ||
            !presences.values
                .contains(presence)
        ) {
            return
        }

        // A geometry/layout refresh must never postpone an already
        // armed mascot cycle. Each Presence owns exactly one local
        // callback; repeated refreshes only update its target.
        if (presence.cycleCallbackArmed) {
            return
        }

        presence.cycleCallbackArmed = true

        val posted =
            postDelayed(
                presence.cycleRunnable,
                delayMs
            )

        if (!posted) {
            presence.cycleCallbackArmed = false
        }
    }

    private fun cancelPresencePlayback(
        presence: Presence,
        preserveCurrentDecision:
            Boolean
    ) {
        removeCallbacks(
            presence.cycleRunnable
        )
        presence.cycleCallbackArmed = false

        presence.generation += 1

        if (
            preserveCurrentDecision &&
            presence.currentDecision != null
        ) {
            presence.pendingDecision =
                presence.currentDecision
        }

        presence.currentDecision = null
        presence.videoActive = false
        presence.videoFirstFrameVisible =
            false
        presence.playback.stopPlayback()
        presence.video.visibility =
            View.INVISIBLE
        presence.videoContainer.visibility =
            View.INVISIBLE

        applyStaticPlaceholder(
            presence
        )
    }

    private fun refreshPresenceTarget(
        presence: Presence
    ): Boolean {
        val raw =
            presence.placement
                .targetProvider()

        if (raw == null) {
            // Keep visual ownership in the Presence once handed off.
            // A temporary off-board target hides both its PNG and video;
            // it must not resurrect the board's legacy PNG renderer.
            presence.pngContainer.visibility =
                View.INVISIBLE
            presence.videoContainer.visibility =
                View.INVISIBLE
            return false
        }

        val scaled =
            scaledRect(
                raw,
                presence.profile
                    .renderScale
            )

        val target =
            if (
                presence.placement
                    .draggable &&
                presence.dragCenterXFraction !=
                    null &&
                presence.dragCenterYFraction !=
                    null &&
                width > 0 &&
                height > 0
            ) {
                rectAtStoredDragPosition(
                    presence,
                    scaled
                )
            } else {
                scaled
            }

        val targetWidth =
            target.width()
                .roundToInt()
                .coerceAtLeast(1)

        val targetHeight =
            target.height()
                .roundToInt()
                .coerceAtLeast(1)

        val layout =
            LayoutParams(
                targetWidth,
                targetHeight
            ).apply {
                leftMargin =
                    target.left
                        .roundToInt()
                topMargin =
                    target.top
                        .roundToInt()
            }

        applyLayoutIfChanged(
            presence.pngContainer,
            layout
        )
        applyLayoutIfChanged(
            presence.videoContainer,
            layout
        )

        applyClipBounds(
            presence,
            target
        )

        applyTint(presence)

        // One-time ownership handoff:
        // once the Presence has a valid target, the board stops
        // drawing its legacy PNG. From here, the Presence's own
        // PNG/video pair owns the complete visual lifecycle.
        setStaticSuppressed(
            presence,
            true
        )

        presence.pngContainer.visibility =
            View.VISIBLE
        presence.videoContainer.visibility =
            if (presence.videoActive) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        return true
    }

    private fun applyClipBounds(
        presence: Presence,
        target: RectF
    ) {
        val clip =
            presence.placement
                .clipProvider
                ?.invoke()

        if (clip == null) {
            presence.pngContainer.clipBounds =
                null
            presence.videoContainer.clipBounds =
                null
            return
        }

        val intersection =
            RectF(target)

        val intersects =
            intersection.intersect(
                clip
            )

        val localClip =
            if (intersects) {
                intersection.offset(
                    -target.left,
                    -target.top
                )

                Rect().also {
                    intersection.roundOut(it)
                }
            } else {
                Rect(
                    0,
                    0,
                    0,
                    0
                )
            }

        presence.pngContainer.clipBounds =
            Rect(localClip)
        presence.videoContainer.clipBounds =
            Rect(localClip)
    }

    private fun applyLayoutIfChanged(
        view: View,
        target: LayoutParams
    ) {
        val current =
            view.layoutParams
                as? LayoutParams

        val unchanged =
            current != null &&
                current.width ==
                    target.width &&
                current.height ==
                    target.height &&
                current.leftMargin ==
                    target.leftMargin &&
                current.topMargin ==
                    target.topMargin

        if (!unchanged) {
            view.layoutParams =
                LayoutParams(target)
        }
    }

    private fun removePresence(
        presence: Presence
    ) {
        removeCallbacks(
            presence.cycleRunnable
        )
        presence.cycleCallbackArmed = false

        presence.generation += 1
        presence.playback.stopPlayback()
        presence.playback.release()
        presence.videoActive = false
        presence.videoFirstFrameVisible =
            false
        presence.currentDecision = null
        presence.pendingDecision = null

        setStaticSuppressed(
            presence,
            false
        )

        presences.remove(
            presenceId(
                presence.profile.kind,
                presence.ownerKey
            )
        )

        removeView(
            presence.pngContainer
        )
        removeView(
            presence.videoContainer
        )
    }

    private fun scaledRect(
        raw: RectF,
        scale: Float
    ): RectF {
        if (scale == 1f) {
            return RectF(raw)
        }

        val halfWidth =
            raw.width() *
                scale /
                2f
        val halfHeight =
            raw.height() *
                scale /
                2f

        return RectF(
            raw.centerX() -
                halfWidth,
            raw.centerY() -
                halfHeight,
            raw.centerX() +
                halfWidth,
            raw.centerY() +
                halfHeight
        )
    }

    private fun rectAtStoredDragPosition(
        presence: Presence,
        template: RectF
    ): RectF {
        val xFraction =
            presence.dragCenterXFraction
                ?: return template
        val yFraction =
            presence.dragCenterYFraction
                ?: return template

        val centerX =
            xFraction * width
        val centerY =
            yFraction * height
        val halfWidth =
            template.width() / 2f
        val halfHeight =
            template.height() / 2f

        return RectF(
            centerX - halfWidth,
            centerY - halfHeight,
            centerX + halfWidth,
            centerY + halfHeight
        )
    }

    private fun handleDrag(
        presence: Presence,
        event: MotionEvent
    ): Boolean {
        if (
            !presence.placement
                .draggable
        ) {
            return false
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                presence.dragStartRawX =
                    event.rawX
                presence.dragStartRawY =
                    event.rawY

                val params =
                    presence.pngContainer
                        .layoutParams
                        as? LayoutParams
                        ?: return false

                presence.dragStartLeft =
                    params.leftMargin
                presence.dragStartTop =
                    params.topMargin

                parent
                    ?.requestDisallowInterceptTouchEvent(
                        true
                    )
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx =
                    event.rawX -
                        presence.dragStartRawX
                val dy =
                    event.rawY -
                        presence.dragStartRawY

                val childWidth =
                    presence.pngContainer
                        .width
                        .coerceAtLeast(1)
                val childHeight =
                    presence.pngContainer
                        .height
                        .coerceAtLeast(1)

                val maxLeft =
                    (
                        width -
                            childWidth
                        )
                        .coerceAtLeast(0)
                val maxTop =
                    (
                        height -
                            childHeight
                        )
                        .coerceAtLeast(0)

                val left =
                    (
                        presence.dragStartLeft +
                            dx
                        )
                        .roundToInt()
                        .coerceIn(
                            0,
                            maxLeft
                        )
                val top =
                    (
                        presence.dragStartTop +
                            dy
                        )
                        .roundToInt()
                        .coerceIn(
                            0,
                            maxTop
                        )

                val movedLayout =
                    LayoutParams(
                        childWidth,
                        childHeight
                    ).apply {
                        leftMargin = left
                        topMargin = top
                    }

                presence.pngContainer.layoutParams =
                    LayoutParams(movedLayout)
                presence.videoContainer.layoutParams =
                    LayoutParams(movedLayout)

                updateDragFractions(
                    presence,
                    left,
                    top,
                    childWidth,
                    childHeight
                )
                return true
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                persistPlantPosition(
                    presence
                )
                parent
                    ?.requestDisallowInterceptTouchEvent(
                        false
                    )
                return true
            }

            else ->
                return true
        }
    }

    private fun updateDragFractions(
        presence: Presence,
        left: Int,
        top: Int,
        childWidth: Int,
        childHeight: Int
    ) {
        if (
            width <= 0 ||
            height <= 0
        ) {
            return
        }

        presence.dragCenterXFraction =
            (
                left +
                    childWidth / 2f
                ) /
                width
        presence.dragCenterYFraction =
            (
                top +
                    childHeight / 2f
                ) /
                height
    }

    private fun persistPlantPosition(
        presence: Presence
    ) {
        val x =
            presence.dragCenterXFraction
                ?: return
        val y =
            presence.dragCenterYFraction
                ?: return

        positionPrefs.edit()
            .putFloat(
                PLANT_X_KEY,
                x.coerceIn(0f, 1f)
            )
            .putFloat(
                PLANT_Y_KEY,
                y.coerceIn(0f, 1f)
            )
            .apply()
    }

    private fun storedFraction(
        key: String
    ): Float? {
        val value =
            positionPrefs.getFloat(
                key,
                Float.NaN
            )

        return if (value.isNaN()) {
            null
        } else {
            value.coerceIn(
                0f,
                1f
            )
        }
    }

    private fun applyTint(
        presence: Presence
    ) {
        presence.image.colorFilter =
            if (
                presence.placement
                    .yellowTint
            ) {
                yellowImageFilter
            } else {
                null
            }

        presence.playback
            .setYellowTint(
                presence.placement
                    .yellowTint
            )
    }

    private fun setStaticSuppressed(
        presence: Presence,
        suppressed: Boolean
    ) {
        if (
            presence.staticSuppressed ==
                suppressed
        ) {
            return
        }

        presence.placement
            .setStaticSuppressed(
                suppressed
            )
        presence.staticSuppressed =
            suppressed
    }

    private fun profileFor(
        kind: MascotKind
    ): MascotAnimationProfile =
        requireNotNull(
            MascotAnimationProfiles
                .all
                .firstOrNull {
                    it.kind == kind
                }
        )

    private fun presenceId(
        kind: MascotKind,
        ownerKey: String
    ): String =
        kind.name +
            ":" +
            ownerKey

    private fun randomPauseMs():
        Long =
        Random.nextLong(
            450L,
            2100L
        )

    private fun refreshVisibility() {
        if (introSuppressed) {
            visibility =
                View.INVISIBLE
            return
        }

        visibility =
            if (
                presences.values
                    .any {
                        it.pngContainer
                            .visibility ==
                            View.VISIBLE ||
                            it.videoContainer
                                .visibility ==
                                View.VISIBLE
                    }
            ) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
    }

    private fun bitmapFor(
        path: String
    ): Bitmap? {
        if (
            bitmapCache
                .containsKey(path)
        ) {
            return bitmapCache[path]
        }

        val bitmap =
            try {
                context.assets
                    .open(path)
                    .use {
                        BitmapFactory
                            .decodeStream(it)
                    }
            } catch (_: Exception) {
                null
            }

        bitmapCache[path] =
            bitmap
        return bitmap
    }

    companion object {
        private const val PLANT_X_KEY =
            "plant_center_x"
        private const val PLANT_Y_KEY =
            "plant_center_y"
        private const val TARGET_RETRY_MS =
            240L
        private const val MEDIA_RETRY_MS =
            900L
    }
}

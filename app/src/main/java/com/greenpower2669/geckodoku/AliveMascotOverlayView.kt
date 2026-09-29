package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RectF
import android.os.SystemClock
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
    private data class Placement(
        val ownerKey: String,
        val targetProvider:
            () -> RectF?,
        val setStaticSuppressed:
            (Boolean) -> Unit,
        val eligible:
            () -> Boolean,
        val yellowTint: Boolean,
        val draggable: Boolean
    )

    private inner class Presence(
        val profile:
            MascotAnimationProfile,
        val ownerKey: String,
        var placement:
            Placement
    ) {
        val animator =
            AliveAnimator(
                profile
            )

        val container =
            FrameLayout(context).apply {
                importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable =
                    placement.draggable
            }

        val image =
            ImageView(context).apply {
                scaleType =
                    ImageView.ScaleType
                        .FIT_CENTER
                importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_NO

                profile.pngAsset
                    ?.let {
                        bitmapFor(it)
                    }
                    ?.let {
                        setImageBitmap(it)
                    }
            }

        var pendingDecision:
            AliveAnimationDecision? =
            null

        var nextAnimationAt =
            0L

        var videoActive =
            false

        var staticSuppressed =
            false

        var removing =
            false

        var forceCute =
            false

        var lastAnimatedOrder =
            0L

        var dragCenterXFraction:
            Float? =
            if (
                profile.kind ==
                    MascotKind.PLANT
            ) {
                storedFraction(
                    PLANT_X_KEY
                )
            } else {
                null
            }

        var dragCenterYFraction:
            Float? =
            if (
                profile.kind ==
                    MascotKind.PLANT
            ) {
                storedFraction(
                    PLANT_Y_KEY
                )
            } else {
                null
            }

        var dragStartRawX = 0f
        var dragStartRawY = 0f
        var dragStartLeft = 0
        var dragStartTop = 0

        init {
            container.addView(
                image,
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )

            container.setOnTouchListener {
                    _,
                    event ->
                if (
                    placement.draggable
                ) {
                    handleDrag(
                        this,
                        event
                    )
                } else {
                    false
                }
            }
        }
    }

    private inner class VideoSlot(
        val profile:
            MascotAnimationProfile,
        val slotIndex: Int
    ) {
        val container =
            FrameLayout(context).apply {
                visibility =
                    View.INVISIBLE
                importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_NO
            }

        val video =
            ChromaKeyVideoView(
                context
            ).apply {
                visibility =
                    View.INVISIBLE
                logicalLayer =
                    "ALIVE_" +
                        profile.kind.name +
                        "_" +
                        slotIndex
                importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable = false
                setKeyColor(
                    profile.keyColor
                )
            }

        var presence:
            Presence? =
            null

        var generation =
            0

        init {
            container.addView(
                video,
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )

            container.setOnTouchListener {
                    _,
                    event ->
                val current =
                    presence

                if (
                    current != null &&
                    current.placement
                        .draggable
                ) {
                    handleDrag(
                        current,
                        event
                    )
                } else {
                    false
                }
            }

            addView(
                container,
                LayoutParams(
                    1,
                    1
                )
            )
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

    private val videoSlots =
        mutableListOf<VideoSlot>()

    private var introSuppressed =
        false

    private var animationSerial =
        0L

    private var lastCuteOwnerKey:
        String? =
        null

    private val updateRunnable =
        Runnable {
            update()
        }

    var animationsEnabled =
        true
        set(value) {
            if (
                field ==
                    value
            ) {
                return
            }

            field = value

            videoSlots
                .forEach {
                    unbindSlot(
                        it,
                        keepPresence = true
                    )
                }

            presences.values
                .toList()
                .forEach {
                    presence ->
                    presence.forceCute =
                        false

                    presence.pendingDecision =
                        if (value) {
                            presence.animator
                                .startAmbient(
                                    animationsEnabled =
                                        true,
                                    randomValue =
                                        Random.nextInt()
                                )
                        } else {
                            presence.animator
                                .start(
                                    animationsEnabled =
                                        false,
                                    randomValue = 0
                                )
                        }

                    presence.nextAnimationAt =
                        SystemClock
                            .uptimeMillis() +
                            if (value) {
                                randomPauseMs()
                            } else {
                                0L
                            }

                    presence.image
                        .visibility =
                        View.VISIBLE
                }

            scheduleUpdate(
                40L
            )
            refreshVisibility()
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
        targetProvider:
            () -> RectF?,
        setStaticSuppressed:
            (Boolean) -> Unit = {},
        eligible:
            () -> Boolean = {
                true
            },
        yellowTint: Boolean = false,
        draggable: Boolean = false,
        animateAppearance: Boolean = true
    ) {
        val id =
            presenceId(
                kind,
                ownerKey
            )

        val placement =
            Placement(
                ownerKey =
                    ownerKey,
                targetProvider =
                    targetProvider,
                setStaticSuppressed =
                    setStaticSuppressed,
                eligible =
                    eligible,
                yellowTint =
                    yellowTint,
                draggable =
                    draggable
            )

        val existing =
            presences[id]

        if (
            existing != null
        ) {
            existing.placement =
                placement
            existing.container
                .isClickable =
                draggable
            applyImageTint(
                existing
            )
            setStaticSuppressed(
                existing,
                true
            )
            refreshPresenceTarget(
                existing
            )
            videoSlotFor(
                existing
            )
                ?.let {
                    refreshSlotTarget(
                        it
                    )
                }
            return
        }

        val profile =
            profileFor(
                kind
            )

        val presence =
            Presence(
                profile =
                    profile,
                ownerKey =
                    ownerKey,
                placement =
                    placement
            )

        presences[id] =
            presence

        addView(
            presence.container,
            0,
            LayoutParams(
                1,
                1
            )
        )

        applyImageTint(
            presence
        )
        setStaticSuppressed(
            presence,
            true
        )
        refreshPresenceTarget(
            presence
        )

        presence.pendingDecision =
            if (
                animationsEnabled
            ) {
                if (
                    animateAppearance
                ) {
                    presence.animator
                        .start(
                            animationsEnabled =
                                true,
                            randomValue =
                                Random.nextInt()
                        )
                } else {
                    presence.animator
                        .startAmbient(
                            animationsEnabled =
                                true,
                            randomValue =
                                Random.nextInt()
                        )
                }
            } else {
                presence.animator
                    .start(
                        animationsEnabled =
                            false,
                        randomValue = 0
                    )
            }

        presence.nextAnimationAt =
            SystemClock
                .uptimeMillis() +
                if (
                    animationsEnabled &&
                    !animateAppearance
                ) {
                    Random.nextLong(
                        250L,
                        1600L
                    )
                } else {
                    0L
                }

        scheduleUpdate(
            20L
        )
        refreshVisibility()
    }

    fun retainOwners(
        kind: MascotKind,
        ownerPrefix: String,
        ownerKeys: Set<String>
    ) {
        presences.values
            .filter {
                it.profile.kind ==
                    kind &&
                    it.ownerKey
                        .startsWith(
                            ownerPrefix
                        ) &&
                    it.ownerKey !in
                        ownerKeys
            }
            .toList()
            .forEach {
                removePresence(
                    it
                )
            }

        scheduleUpdate(
            20L
        )
    }

    fun disappear(
        kind: MascotKind,
        ownerKey: String,
        targetProvider:
            () -> RectF?,
        setStaticSuppressed:
            (Boolean) -> Unit = {},
        yellowTint: Boolean = false
    ) {
        val presence =
            presences[
                presenceId(
                    kind,
                    ownerKey
                )
            ]
                ?: return

        presence.placement =
            presence.placement
                .copy(
                    targetProvider =
                        targetProvider,
                    setStaticSuppressed =
                        setStaticSuppressed,
                    eligible = {
                        false
                    },
                    yellowTint =
                        yellowTint
                )

        presence.removing =
            true

        videoSlotFor(
            presence
        )
            ?.let {
                unbindSlot(
                    it,
                    keepPresence =
                        true
                )
            }

        val decision =
            presence.animator
                .requestDisappear(
                    animationsEnabled
                )

        if (
            decision.state ==
                AliveVisualState
                    .HIDDEN
        ) {
            removePresence(
                presence
            )
        } else {
            presence.pendingDecision =
                decision
            presence.nextAnimationAt =
                SystemClock
                    .uptimeMillis()
            scheduleUpdate(
                10L
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

        removeCallbacks(
            updateRunnable
        )

        if (suppressed) {
            videoSlots
                .forEach {
                    unbindSlot(
                        it,
                        keepPresence =
                            true
                    )
                }

            visibility =
                View.INVISIBLE
            return
        }

        val now =
            SystemClock
                .uptimeMillis()

        presences.values
            .toList()
            .forEach {
                presence ->
                if (
                    presence.removing
                ) {
                    removePresence(
                        presence
                    )
                } else {
                    presence.pendingDecision =
                        if (
                            animationsEnabled
                        ) {
                            presence.animator
                                .startAmbient(
                                    animationsEnabled =
                                        true,
                                    randomValue =
                                        Random.nextInt()
                                )
                        } else {
                            presence.animator
                                .start(
                                    animationsEnabled =
                                        false,
                                    randomValue = 0
                                )
                        }

                    presence.nextAnimationAt =
                        now +
                            if (
                                animationsEnabled
                            ) {
                                Random.nextLong(
                                    50L,
                                    450L
                                )
                            } else {
                                0L
                            }

                    presence.image
                        .visibility =
                        View.VISIBLE

                    refreshPresenceTarget(
                        presence
                    )
                }
            }

        scheduleUpdate(
            20L
        )
        refreshVisibility()
    }

    fun stop(
        kind: MascotKind
    ) {
        presences.values
            .filter {
                it.profile.kind ==
                    kind
            }
            .toList()
            .forEach {
                removePresence(
                    it
                )
            }

        refreshVisibility()
    }

    fun stopBoardMascots() {
        stop(
            MascotKind.GECKO
        )
        stop(
            MascotKind.BEE
        )
    }

    fun stopAll() {
        MascotKind.entries
            .forEach {
                stop(it)
            }
    }

    fun refreshDynamicTargets() {
        presences.values
            .toList()
            .forEach {
                presence ->
                if (
                    presence.placement
                        .eligible() ||
                    presence.removing
                ) {
                    refreshPresenceTarget(
                        presence
                    )
                } else {
                    removePresence(
                        presence
                    )
                }
            }

        videoSlots
            .filter {
                it.presence !=
                    null
            }
            .forEach {
                refreshSlotTarget(
                    it
                )
            }

        refreshVisibility()
    }

    fun update() {
        removeCallbacks(
            updateRunnable
        )

        if (
            !animationsEnabled
        ) {
            refreshVisibility()
            return
        }

        val now =
            SystemClock
                .uptimeMillis()

        val invalid =
            presences.values
                .filter {
                    !it.removing &&
                        !it.placement
                            .eligible()
                }
                .toList()

        invalid.forEach {
            removePresence(
                it
            )
        }

        ensureVideoSlotForEveryPresence()

        videoSlots
            .filter {
                it.presence ==
                    null
            }
            .forEach {
                slot ->
                val candidate =
                    nextCandidate(
                        kind =
                            slot.profile
                                .kind,
                        now =
                            now
                    )

                if (
                    candidate !=
                        null
                ) {
                    bindAndPlay(
                        slot,
                        candidate
                    )
                }
            }

        scheduleUpdate(
            280L
        )
        refreshVisibility()
    }

    fun release() {
        removeCallbacks(
            updateRunnable
        )

        presences.values
            .toList()
            .forEach {
                removePresence(
                    it
                )
            }

        videoSlots.forEach {
            it.generation += 1
            it.video.release()
            it.presence = null
        }

        visibility =
            View.GONE
    }

    private fun ensureVideoSlotForEveryPresence() {
        MascotAnimationProfiles
            .all
            .forEach {
                profile ->
                val required =
                    presences.values
                        .count {
                            presence ->
                            presence.profile
                                .kind ==
                                profile.kind &&
                                (
                                    presence.removing ||
                                        presence.placement
                                            .eligible()
                                    )
                        }

                var existing =
                    videoSlots
                        .count {
                            it.profile.kind ==
                                profile.kind
                        }

                while (
                    existing <
                        required
                ) {
                    videoSlots.add(
                        VideoSlot(
                            profile =
                                profile,
                            slotIndex =
                                existing
                        )
                    )
                    existing += 1
                }
            }
    }

    private fun nextCandidate(
        kind: MascotKind,
        now: Long
    ): Presence? {
        val candidates =
            presences.values
                .filter {
                    it.profile.kind ==
                        kind &&
                        !it.videoActive &&
                        it.pendingDecision
                            ?.assetPath !=
                            null &&
                        it.nextAnimationAt <=
                            now &&
                        (
                            it.removing ||
                                it.placement
                                    .eligible()
                            )
                }

        if (
            candidates.isEmpty()
        ) {
            return null
        }

        val removing =
            candidates
                .filter {
                    it.removing
                }

        val pool =
            if (
                removing.isNotEmpty()
            ) {
                removing
            } else {
                candidates
            }

        val oldestOrder =
            pool.minOf {
                it.lastAnimatedOrder
            }

        val oldest =
            pool.filter {
                it.lastAnimatedOrder ==
                    oldestOrder
            }

        return oldest[
            Random.nextInt(
                oldest.size
            )
        ]
    }

    private fun bindAndPlay(
        slot: VideoSlot,
        presence: Presence
    ) {
        val decision =
            presence.pendingDecision
                ?: return

        val asset =
            decision.assetPath
                ?: return

        slot.generation += 1
        val generation =
            slot.generation

        slot.presence =
            presence
        presence.videoActive =
            true
        presence.lastAnimatedOrder =
            ++animationSerial
        presence.pendingDecision =
            null

        refreshPresenceTarget(
            presence
        )
        refreshSlotTarget(
            slot
        )

        val hidePngUntilFrame =
            decision.state ==
                AliveVisualState
                    .APPEARING &&
                presence.lastAnimatedOrder <=
                    1L

        presence.image
            .visibility =
            if (
                hidePngUntilFrame
            ) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }

        slot.video
            .setKeyColor(
                presence.profile
                    .keyColor
            )
        slot.video
            .setYellowTint(
                presence.placement
                    .yellowTint
            )
        slot.video.visibility =
            View.VISIBLE
        slot.container.visibility =
            View.VISIBLE
        bringChildToFront(
            slot.container
        )

        slot.video.play(
            assetPath =
                asset,
            muted = true,
            revealOnFirstFrame =
                true,
            onFirstFrameRendered = {
                if (
                    generation !=
                        slot.generation ||
                    slot.presence !==
                        presence
                ) {
                    return@play
                }

                presence.image
                    .visibility =
                    View.INVISIBLE
            },
            onStarted = {},
            onCompletion = {
                if (
                    generation !=
                        slot.generation ||
                    slot.presence !==
                        presence
                ) {
                    return@play
                }

                handleClipCompleted(
                    slot,
                    presence
                )
            },
            onError = {
                    _ ->
                if (
                    generation !=
                        slot.generation ||
                    slot.presence !==
                        presence
                ) {
                    return@play
                }

                presence.pendingDecision =
                    presence.animator
                        .fallbackToStatic()
                unbindSlot(
                    slot,
                    keepPresence =
                        true
                )
                presence.nextAnimationAt =
                    SystemClock
                        .uptimeMillis() +
                        randomPauseMs()
                scheduleUpdate(
                    100L
                )
            }
        )
    }

    private fun handleClipCompleted(
        slot: VideoSlot,
        presence: Presence
    ) {
        presence.image
            .visibility =
            View.VISIBLE

        val completedState =
            presence.animator
                .state

        unbindSlot(
            slot,
            keepPresence =
                true
        )

        if (
            presence.removing ||
            completedState ==
                AliveVisualState
                    .DISAPPEARING
        ) {
            removePresence(
                presence
            )
            scheduleUpdate(
                20L
            )
            return
        }

        val randomValue =
            Random.nextInt()

        val normalDecision =
            presence.animator
                .afterCurrentClip(
                    animationsEnabled =
                        animationsEnabled,
                    randomValue =
                        randomValue
                )

        if (
            normalDecision
                .requestGroupRefresh
        ) {
            handleGrandCycle(
                presence
            )
        }

        presence.pendingDecision =
            if (
                presence.forceCute
            ) {
                presence.forceCute =
                    false
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

        presence.nextAnimationAt =
            SystemClock
                .uptimeMillis() +
                randomPauseMs()

        scheduleUpdate(
            80L
        )
    }

    private fun handleGrandCycle(
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
            if (
                peer.videoActive
            ) {
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
                peer.nextAnimationAt =
                    SystemClock
                        .uptimeMillis() +
                        Random.nextLong(
                            250L,
                            1200L
                        )
            }
        }

        val cuteCandidates =
            peers.filter {
                it.profile
                    .cuteAssets
                    .isNotEmpty()
            }

        if (
            cuteCandidates.isEmpty()
        ) {
            return
        }

        val withoutLast =
            cuteCandidates
                .filter {
                    it.ownerKey !=
                        lastCuteOwnerKey
                }

        val pool =
            if (
                withoutLast
                    .isNotEmpty()
            ) {
                withoutLast
            } else {
                cuteCandidates
            }

        val friend =
            pool[
                Random.nextInt(
                    pool.size
                )
            ]

        lastCuteOwnerKey =
            friend.ownerKey

        if (
            friend.videoActive
        ) {
            friend.forceCute =
                true
        } else {
            friend.pendingDecision =
                friend.animator
                    .requestCute(
                        animationsEnabled =
                            animationsEnabled,
                        randomValue =
                            Random.nextInt()
                    )
            friend.nextAnimationAt =
                SystemClock
                    .uptimeMillis() +
                    Random.nextLong(
                        100L,
                        500L
                    )
        }
    }

    private fun unbindSlot(
        slot: VideoSlot,
        keepPresence: Boolean
    ) {
        val presence =
            slot.presence

        slot.generation += 1
        slot.video.stopPlayback()
        slot.video.visibility =
            View.INVISIBLE
        slot.container.visibility =
            View.INVISIBLE
        slot.presence =
            null

        if (
            presence != null
        ) {
            presence.videoActive =
                false

            if (
                keepPresence &&
                presences.values
                    .contains(
                        presence
                    )
            ) {
                presence.image
                    .visibility =
                    View.VISIBLE
                refreshPresenceTarget(
                    presence
                )
            }
        }
    }

    private fun removePresence(
        presence: Presence
    ) {
        videoSlotFor(
            presence
        )
            ?.let {
                unbindSlot(
                    it,
                    keepPresence =
                        false
                )
            }

        setStaticSuppressed(
            presence,
            false
        )

        presences.remove(
            presenceId(
                presence.profile
                    .kind,
                presence.ownerKey
            )
        )

        removeView(
            presence.container
        )
    }

    private fun refreshPresenceTarget(
        presence: Presence
    ) {
        val raw =
            presence.placement
                .targetProvider()
                ?: run {
                    setStaticSuppressed(
                        presence,
                        false
                    )
                    presence.container
                        .visibility =
                        View.INVISIBLE
                    return
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

        presence.container
            .layoutParams =
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

        applyImageTint(
            presence
        )
        setStaticSuppressed(
            presence,
            true
        )
        presence.container
            .visibility =
            View.VISIBLE

        videoSlotFor(
            presence
        )
            ?.let {
                refreshSlotTarget(
                    it
                )
            }

        if (
            visibility !=
                View.VISIBLE
        ) {
            visibility =
                View.VISIBLE
        }
    }

    private fun refreshSlotTarget(
        slot: VideoSlot
    ) {
        val presence =
            slot.presence
                ?: return

        val params =
            presence.container
                .layoutParams
                as? LayoutParams
                ?: return

        slot.container
            .layoutParams =
            LayoutParams(
                params.width,
                params.height
            ).apply {
                leftMargin =
                    params.leftMargin
                topMargin =
                    params.topMargin
            }

        slot.container.visibility =
            if (
                presence.container
                    .visibility ==
                    View.VISIBLE
            ) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
    }

    private fun scaledRect(
        raw: RectF,
        scale: Float
    ): RectF {
        if (
            scale ==
                1f
        ) {
            return RectF(
                raw
            )
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
            xFraction *
                width
        val centerY =
            yFraction *
                height

        val halfWidth =
            template.width() /
                2f
        val halfHeight =
            template.height() /
                2f

        return RectF(
            centerX -
                halfWidth,
            centerY -
                halfHeight,
            centerX +
                halfWidth,
            centerY +
                halfHeight
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

        when (
            event.actionMasked
        ) {
            MotionEvent.ACTION_DOWN -> {
                presence.dragStartRawX =
                    event.rawX
                presence.dragStartRawY =
                    event.rawY

                val params =
                    presence.container
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
                    presence.container
                        .width
                        .coerceAtLeast(1)
                val childHeight =
                    presence.container
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

                presence.container
                    .layoutParams =
                    LayoutParams(
                        childWidth,
                        childHeight
                    ).apply {
                        leftMargin =
                            left
                        topMargin =
                            top
                    }

                updateDragFractions(
                    presence,
                    left,
                    top,
                    childWidth,
                    childHeight
                )

                videoSlotFor(
                    presence
                )
                    ?.let {
                        refreshSlotTarget(
                            it
                        )
                    }

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
                    childWidth /
                        2f
                ) /
                width

        presence.dragCenterYFraction =
            (
                top +
                    childHeight /
                        2f
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

        positionPrefs
            .edit()
            .putFloat(
                PLANT_X_KEY,
                x.coerceIn(
                    0f,
                    1f
                )
            )
            .putFloat(
                PLANT_Y_KEY,
                y.coerceIn(
                    0f,
                    1f
                )
            )
            .apply()
    }

    private fun storedFraction(
        key: String
    ): Float? {
        val value =
            positionPrefs
                .getFloat(
                    key,
                    Float.NaN
                )

        return if (
            value.isNaN()
        ) {
            null
        } else {
            value.coerceIn(
                0f,
                1f
            )
        }
    }

    private fun applyImageTint(
        presence: Presence
    ) {
        presence.image
            .colorFilter =
            if (
                presence.placement
                    .yellowTint
            ) {
                yellowImageFilter
            } else {
                null
            }
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

    private fun videoSlotFor(
        presence: Presence
    ): VideoSlot? =
        videoSlots
            .firstOrNull {
                it.presence ===
                    presence
            }

    private fun profileFor(
        kind: MascotKind
    ): MascotAnimationProfile =
        requireNotNull(
            MascotAnimationProfiles
                .all
                .firstOrNull {
                    it.kind ==
                        kind
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

    private fun scheduleUpdate(
        delayMs: Long
    ) {
        removeCallbacks(
            updateRunnable
        )

        if (
            animationsEnabled &&
            !introSuppressed
        ) {
            postDelayed(
                updateRunnable,
                delayMs
            )
        }
    }

    private fun refreshVisibility() {
        if (
            introSuppressed
        ) {
            visibility =
                View.INVISIBLE
            return
        }

        visibility =
            if (
                presences.values
                    .any {
                        it.container
                            .visibility ==
                            View.VISIBLE
                    } ||
                videoSlots.any {
                    it.container
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
                .containsKey(
                    path
                )
        ) {
            return bitmapCache[
                path
            ]
        }

        val bitmap =
            try {
                context.assets
                    .open(
                        path
                    )
                    .use {
                        BitmapFactory
                            .decodeStream(
                                it
                            )
                    }
            } catch (
                _: Exception
            ) {
                null
            }

        bitmapCache[
            path
        ] =
            bitmap

        return bitmap
    }

    companion object {
        private const val PLANT_X_KEY =
            "plant_center_x"
        private const val PLANT_Y_KEY =
            "plant_center_y"
    }
}

package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
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
        val maskColorProvider:
            (() -> Int)?,
        val eligible:
            () -> Boolean,
        val yellowTint: Boolean
    )

    private inner class Slot(
        val profile:
            MascotAnimationProfile,
        val animator:
            AliveAnimator
    ) {
        val container =
            FrameLayout(context).apply {
                visibility =
                    View.INVISIBLE
                isClickable = false
                importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_NO
            }

        val mask =
            View(context).apply {
                visibility =
                    View.GONE
                importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_NO
            }

        val image =
            ImageView(context).apply {
                visibility =
                    View.GONE
                scaleType =
                    ImageView.ScaleType
                        .FIT_CENTER
                importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_NO

                profile.pngAsset
                    ?.let {
                        loadBitmap(it)
                    }
                    ?.let {
                        setImageBitmap(it)
                    }
            }

        val video =
            ChromaKeyVideoView(
                context
            ).apply {
                visibility =
                    View.INVISIBLE
                logicalLayer =
                    "ALIVE_" +
                        profile.kind.name
                importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable = false
                setKeyColor(
                    profile.keyColor
                )
            }

        var placement:
            Placement? = null

        var generation = 0
        var active = false
        var maskLatched = false

        init {
            container.addView(
                mask,
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )

            container.addView(
                image,
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )

            container.addView(
                video,
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )

            addView(
                container,
                LayoutParams(
                    1,
                    1
                )
            )
        }
    }

    private val animators =
        MascotAnimationProfiles
            .all
            .associate {
                profile ->
                profile.kind to
                    AliveAnimator(
                        profile
                    )
            }

    private val coordinator =
        MascotLifeCoordinator(
            animators.values
        )

    private val slots =
        animators.mapValues {
            (_, animator) ->
            Slot(
                profile =
                    animator.profile,
                animator = animator
            )
        }

    var animationsEnabled =
        true
        set(value) {
            if (field == value) {
                return
            }

            field = value

            slots.values
                .filter {
                    it.active
                }
                .forEach {
                    slot ->
                    slot.generation += 1
                    slot.video
                        .stopPlayback()
                    slot.maskLatched =
                        false

                    val placement =
                        slot.placement

                    if (
                        placement != null &&
                        placement.eligible()
                    ) {
                        applyDecision(
                            slot =
                                slot,
                            decision =
                                coordinator
                                    .start(
                                        kind =
                                            slot
                                                .profile
                                                .kind,
                                        animationsEnabled =
                                            value,
                                        randomValue =
                                            Random.nextInt()
                                    ),
                            generation =
                                slot.generation
                        )
                    } else {
                        finishSlot(
                            slot
                        )
                    }
                }

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
        maskColorProvider:
            (() -> Int)? = null,
        eligible:
            () -> Boolean = {
                true
            },
        yellowTint: Boolean = false
    ) {
        val slot =
            requireNotNull(
                slots[kind]
            )

        val existing =
            slot.placement

        if (
            slot.active &&
            existing
                ?.ownerKey ==
                ownerKey
        ) {
            slot.placement =
                Placement(
                    ownerKey,
                    targetProvider,
                    maskColorProvider,
                    eligible,
                    yellowTint
                )

            refreshSlotTarget(
                slot
            )
            return
        }

        slot.generation += 1
        slot.video.stopPlayback()
        slot.active = true
        slot.maskLatched = false
        slot.placement =
            Placement(
                ownerKey,
                targetProvider,
                maskColorProvider,
                eligible,
                yellowTint
            )

        applyDecision(
            slot =
                slot,
            decision =
                coordinator.start(
                    kind = kind,
                    animationsEnabled =
                        animationsEnabled,
                    randomValue =
                        Random.nextInt()
                ),
            generation =
                slot.generation
        )

        refreshVisibility()
    }

    fun disappear(
        kind: MascotKind,
        ownerKey: String,
        targetProvider:
            () -> RectF?,
        maskColorProvider:
            (() -> Int)? = null,
        yellowTint: Boolean = false
    ) {
        val slot =
            requireNotNull(
                slots[kind]
            )

        slot.generation += 1
        slot.video.stopPlayback()
        slot.active = true
        slot.maskLatched = false
        slot.placement =
            Placement(
                ownerKey,
                targetProvider,
                maskColorProvider,
                eligible = {
                    false
                },
                yellowTint =
                    yellowTint
            )

        applyDecision(
            slot =
                slot,
            decision =
                coordinator.disappear(
                    kind,
                    animationsEnabled
                ),
            generation =
                slot.generation
        )

        refreshVisibility()
    }

    fun stop(
        kind: MascotKind
    ) {
        val slot =
            requireNotNull(
                slots[kind]
            )

        slot.generation += 1
        coordinator.hide(kind)
        finishSlot(slot)
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
        slots.values
            .filter {
                it.active
            }
            .forEach {
                refreshSlotTarget(
                    it
                )
            }
    }

    fun release() {
        slots.values
            .forEach {
                slot ->
                slot.generation += 1
                slot.video.release()
                slot.active = false
                slot.placement = null
            }

        visibility = View.GONE
    }

    private fun applyDecision(
        slot: Slot,
        decision:
            AliveAnimationDecision,
        generation: Int
    ) {
        if (
            generation !=
                slot.generation
        ) {
            return
        }

        when (decision.state) {
            AliveVisualState.HIDDEN -> {
                finishSlot(slot)
            }

            AliveVisualState.STATIC_PNG -> {
                slot.video.stopPlayback()
                slot.video.visibility =
                    View.INVISIBLE
                slot.mask.visibility =
                    View.GONE

                if (
                    decision.showPng &&
                    slot.profile
                        .pngAsset != null
                ) {
                    slot.image.visibility =
                        View.VISIBLE
                    refreshSlotTarget(
                        slot
                    )
                } else {
                    slot.image.visibility =
                        View.GONE
                    slot.container
                        .visibility =
                        View.INVISIBLE
                }
            }

            AliveVisualState.APPEARING,
            AliveVisualState.IDLE,
            AliveVisualState.CUTE,
            AliveVisualState.DISAPPEARING -> {
                val asset =
                    decision.assetPath
                        ?: run {
                            applyDecision(
                                slot,
                                coordinator
                                    .fallback(
                                        slot
                                            .profile
                                            .kind
                                    ),
                                generation
                            )
                            return
                        }

                playClip(
                    slot =
                        slot,
                    assetPath =
                        asset,
                    generation =
                        generation
                )
            }
        }

        refreshVisibility()
    }

    private fun playClip(
        slot: Slot,
        assetPath: String,
        generation: Int
    ) {
        val placement =
            slot.placement
                ?: run {
                    finishSlot(slot)
                    return
                }

        refreshSlotTarget(slot)

        slot.image.visibility =
            View.GONE

        val maskColor =
            placement
                .maskColorProvider
                ?.invoke()

        if (maskColor != null) {
            slot.mask.visibility =
                View.VISIBLE
            slot.mask
                .setBackgroundColor(
                    maskColor
                )
            slot.mask.alpha =
                if (
                    slot.maskLatched
                ) {
                    1f
                } else {
                    0f
                }
        } else {
            slot.mask.visibility =
                View.GONE
        }

        slot.video
            .setKeyColor(
                slot.profile
                    .keyColor
            )
        slot.video
            .setYellowTint(
                placement.yellowTint
            )
        slot.video.visibility =
            View.VISIBLE

        slot.video.play(
            assetPath =
                assetPath,
            muted = true,
            revealOnFirstFrame = true,
            onFirstFrameRendered = {
                if (
                    generation !=
                        slot.generation
                ) {
                    return@play
                }

                slot.maskLatched =
                    true

                if (
                    slot.mask.visibility ==
                        View.VISIBLE
                ) {
                    slot.mask.alpha = 1f
                }

                refreshSlotTarget(
                    slot
                )
            },
            onStarted = {},
            onCompletion = {
                if (
                    generation !=
                        slot.generation
                ) {
                    return@play
                }

                handleClipCompleted(
                    slot,
                    generation
                )
            },
            onError = {
                    _ ->

                if (
                    generation !=
                        slot.generation
                ) {
                    return@play
                }

                applyDecision(
                    slot =
                        slot,
                    decision =
                        coordinator
                            .fallback(
                                slot
                                    .profile
                                    .kind
                            ),
                    generation =
                        generation
                )
            }
        )
    }

    private fun handleClipCompleted(
        slot: Slot,
        generation: Int
    ) {
        if (
            generation !=
                slot.generation
        ) {
            return
        }

        val previousState =
            slot.animator.state

        if (
            previousState !=
                AliveVisualState
                    .DISAPPEARING
        ) {
            val placement =
                slot.placement

            if (
                placement == null ||
                !placement.eligible()
            ) {
                finishSlot(slot)
                refreshVisibility()
                return
            }
        }

        val decision =
            coordinator.afterClip(
                kind =
                    slot.profile.kind,
                animationsEnabled =
                    animationsEnabled,
                randomValue =
                    Random.nextInt()
            )

        applyDecision(
            slot =
                slot,
            decision =
                decision,
            generation =
                generation
        )
    }

    private fun refreshSlotTarget(
        slot: Slot
    ) {
        val placement =
            slot.placement
                ?: return

        val raw =
            placement
                .targetProvider()
                ?: run {
                    slot.container
                        .visibility =
                        View.INVISIBLE
                    return
                }

        val scale =
            slot.profile
                .renderScale

        val target =
            if (scale == 1f) {
                RectF(raw)
            } else {
                val halfWidth =
                    raw.width() *
                        scale /
                        2f
                val halfHeight =
                    raw.height() *
                        scale /
                        2f

                RectF(
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

        val width =
            target.width()
                .roundToInt()
                .coerceAtLeast(1)

        val height =
            target.height()
                .roundToInt()
                .coerceAtLeast(1)

        slot.container
            .layoutParams =
            LayoutParams(
                width,
                height
            ).apply {
                leftMargin =
                    target.left
                        .roundToInt()
                topMargin =
                    target.top
                        .roundToInt()
            }

        slot.container
            .visibility =
            View.VISIBLE
    }

    private fun finishSlot(
        slot: Slot
    ) {
        slot.video.stopPlayback()
        slot.video.visibility =
            View.INVISIBLE
        slot.image.visibility =
            View.GONE
        slot.mask.visibility =
            View.GONE
        slot.mask.alpha = 0f
        slot.maskLatched = false
        slot.container.visibility =
            View.INVISIBLE
        slot.active = false
        slot.placement = null
    }

    private fun refreshVisibility() {
        visibility =
            if (
                slots.values.any {
                    it.active &&
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

    private fun loadBitmap(
        path: String
    ): Bitmap? =
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
}

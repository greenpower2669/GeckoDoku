package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RectF
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

    private var activationSerial =
        0L

    private inner class Slot(
        val profile:
            MascotAnimationProfile,
        val slotIndex: Int
    ) {
        val animator =
            AliveAnimator(profile)

        val container =
            FrameLayout(context).apply {
                visibility =
                    View.INVISIBLE
                isClickable =
                    profile.kind ==
                        MascotKind.PLANT
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
                        bitmapFor(it)
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

        var placement:
            Placement? = null

        var generation = 0
        var active = false
        var hasRenderedFrame = false
        var staticSuppressed = false
        var lastActivatedOrder = 0L

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
            container.addView(
                video,
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )

            if (
                profile.kind ==
                    MascotKind.PLANT
            ) {
                container.setOnTouchListener {
                        _,
                        event ->
                    handlePlantDrag(
                        this,
                        event
                    )
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

    private val slots:
        List<Slot> =
        MascotAnimationProfiles
            .all
            .flatMap {
                profile ->
                List(
                    MascotActivityPolicy
                        .capacity(
                            profile.kind
                        )
                ) {
                    index ->
                    Slot(
                        profile,
                        index
                    )
                }
            }

    var animationsEnabled =
        true
        set(value) {
            if (field == value) {
                return
            }

            field = value

            slots
                .filter {
                    it.active
                }
                .forEach {
                    slot ->
                    slot.generation += 1
                    slot.video.stopPlayback()
                    slot.hasRenderedFrame =
                        false

                    val placement =
                        slot.placement

                    if (
                        placement != null &&
                        placement.eligible()
                    ) {
                        setStaticSuppressed(
                            slot,
                            true
                        )
                        applyDecision(
                            slot,
                            slot.animator.start(
                                animationsEnabled =
                                    value,
                                randomValue =
                                    Random.nextInt()
                            ),
                            slot.generation
                        )
                    } else {
                        retireSlot(slot)
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
        setStaticSuppressed:
            (Boolean) -> Unit = {},
        eligible:
            () -> Boolean = {
                true
            },
        yellowTint: Boolean = false,
        draggable: Boolean = false
    ) {
        val existing =
            slots.firstOrNull {
                it.active &&
                    it.profile.kind ==
                        kind &&
                    it.placement
                        ?.ownerKey ==
                        ownerKey
            }

        if (existing != null) {
            existing.placement =
                Placement(
                    ownerKey,
                    targetProvider,
                    setStaticSuppressed,
                    eligible,
                    yellowTint,
                    draggable
                )
            applyImageTint(existing)
            setStaticSuppressed(
                existing,
                true
            )
            refreshSlotTarget(
                existing
            )
            return
        }

        val slot =
            acquireSlot(kind)

        retireSlot(slot)

        slot.generation += 1
        slot.active = true
        slot.hasRenderedFrame = false
        slot.lastActivatedOrder =
            ++activationSerial
        slot.placement =
            Placement(
                ownerKey,
                targetProvider,
                setStaticSuppressed,
                eligible,
                yellowTint,
                draggable
            )

        applyImageTint(slot)
        setStaticSuppressed(
            slot,
            true
        )
        bringChildToFront(
            slot.container
        )

        applyDecision(
            slot,
            slot.animator.start(
                animationsEnabled =
                    animationsEnabled,
                randomValue =
                    Random.nextInt()
            ),
            slot.generation
        )

        refreshVisibility()
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
        val slot =
            slots.firstOrNull {
                it.active &&
                    it.profile.kind ==
                        kind &&
                    it.placement
                        ?.ownerKey ==
                        ownerKey
            }
                ?: return

        slot.generation += 1
        slot.video.stopPlayback()
        slot.placement =
            Placement(
                ownerKey,
                targetProvider,
                setStaticSuppressed,
                eligible = {
                    false
                },
                yellowTint =
                    yellowTint,
                draggable = false
            )

        applyImageTint(slot)
        setStaticSuppressed(
            slot,
            true
        )

        applyDecision(
            slot,
            slot.animator
                .requestDisappear(
                    animationsEnabled
                ),
            slot.generation
        )

        refreshVisibility()
    }

    fun stop(
        kind: MascotKind
    ) {
        slots
            .filter {
                it.profile.kind ==
                    kind
            }
            .forEach {
                slot ->
                    slot.generation += 1
                    slot.animator
                        .hideImmediately()
                    retireSlot(slot)
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
        slots
            .filter {
                it.active
            }
            .forEach {
                refreshSlotTarget(it)
            }

        refreshVisibility()
    }

    fun release() {
        slots.forEach {
            slot ->
            slot.generation += 1
            setStaticSuppressed(
                slot,
                false
            )
            slot.video.release()
            slot.active = false
            slot.placement = null
        }

        visibility = View.GONE
    }

    private fun acquireSlot(
        kind: MascotKind
    ): Slot {
        slots.firstOrNull {
            !it.active &&
                it.profile.kind ==
                    kind
        }?.let {
            return it
        }

        return requireNotNull(
            slots
                .filter {
                    it.profile.kind ==
                        kind
                }
                .minByOrNull {
                    it.lastActivatedOrder
                }
        )
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
            AliveVisualState.HIDDEN ->
                retireSlot(slot)

            AliveVisualState.STATIC_PNG -> {
                slot.video.stopPlayback()
                slot.video.visibility =
                    View.INVISIBLE

                if (
                    decision.showPng &&
                    slot.profile
                        .pngAsset != null
                ) {
                    applyImageTint(slot)
                    slot.image.visibility =
                        View.VISIBLE
                    setStaticSuppressed(
                        slot,
                        true
                    )
                    refreshSlotTarget(slot)
                } else {
                    retireSlot(slot)
                }
            }

            AliveVisualState.APPEARING,
            AliveVisualState.IDLE,
            AliveVisualState.CUTE,
            AliveVisualState.DISAPPEARING -> {
                val asset =
                    decision.assetPath

                if (asset == null) {
                    applyDecision(
                        slot,
                        slot.animator
                            .fallbackToStatic(),
                        generation
                    )
                    return
                }

                playClip(
                    slot,
                    asset,
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
                    retireSlot(slot)
                    return
                }

        refreshSlotTarget(slot)
        setStaticSuppressed(
            slot,
            true
        )

        val usePngBridge =
            slot.profile.pngAsset !=
                null &&
                (
                    slot.hasRenderedFrame ||
                        slot.animator.state !=
                            AliveVisualState
                                .APPEARING
                    )

        applyImageTint(slot)
        slot.image.visibility =
            if (usePngBridge) {
                View.VISIBLE
            } else {
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

                slot.hasRenderedFrame =
                    true
                slot.image.visibility =
                    View.GONE
                refreshSlotTarget(slot)
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
                    slot,
                    slot.animator
                        .fallbackToStatic(),
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

        if (
            slot.animator.state !=
                AliveVisualState
                    .DISAPPEARING
        ) {
            val placement =
                slot.placement

            if (
                placement == null ||
                !placement.eligible()
            ) {
                retireSlot(slot)
                refreshVisibility()
                return
            }
        }

        val decision =
            slot.animator
                .afterCurrentClip(
                    animationsEnabled =
                        animationsEnabled,
                    randomValue =
                        Random.nextInt()
                )

        if (
            decision
                .requestGroupRefresh
        ) {
            slots
                .filter {
                    it.active &&
                        it !== slot
                }
                .forEach {
                    it.animator
                        .requestPeerRefresh()
                }
        }

        applyDecision(
            slot,
            decision,
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
                    setStaticSuppressed(
                        slot,
                        false
                    )
                    slot.container
                        .visibility =
                        View.INVISIBLE
                    return
                }

        val scaled =
            scaledRect(
                raw,
                slot.profile
                    .renderScale
            )

        val target =
            if (
                placement.draggable &&
                slot.dragCenterXFraction !=
                    null &&
                slot.dragCenterYFraction !=
                    null &&
                width > 0 &&
                height > 0
            ) {
                rectAtStoredDragPosition(
                    slot,
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

        slot.container
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

        setStaticSuppressed(
            slot,
            true
        )
        slot.container.visibility =
            View.VISIBLE

        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
        }
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
        slot: Slot,
        reference: RectF
    ): RectF {
        val targetWidth =
            reference.width()
        val targetHeight =
            reference.height()

        val centerX =
            (
                requireNotNull(
                    slot.dragCenterXFraction
                ) *
                    width
                )
                .coerceIn(
                    targetWidth / 2f,
                    width -
                        targetWidth / 2f
                )
        val centerY =
            (
                requireNotNull(
                    slot.dragCenterYFraction
                ) *
                    height
                )
                .coerceIn(
                    targetHeight / 2f,
                    height -
                        targetHeight / 2f
                )

        return RectF(
            centerX -
                targetWidth / 2f,
            centerY -
                targetHeight / 2f,
            centerX +
                targetWidth / 2f,
            centerY +
                targetHeight / 2f
        )
    }

    private fun handlePlantDrag(
        slot: Slot,
        event: MotionEvent
    ): Boolean {
        val placement =
            slot.placement

        if (
            !slot.active ||
            placement == null ||
            !placement.draggable
        ) {
            return false
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val layout =
                    slot.container
                        .layoutParams
                        as? LayoutParams
                        ?: return false

                slot.dragStartRawX =
                    event.rawX
                slot.dragStartRawY =
                    event.rawY
                slot.dragStartLeft =
                    layout.leftMargin
                slot.dragStartTop =
                    layout.topMargin

                bringChildToFront(
                    slot.container
                )
                parent
                    ?.requestDisallowInterceptTouchEvent(
                        true
                    )
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx =
                    event.rawX -
                        slot.dragStartRawX
                val dy =
                    event.rawY -
                        slot.dragStartRawY

                val childWidth =
                    slot.container.width
                        .coerceAtLeast(1)
                val childHeight =
                    slot.container.height
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
                        slot.dragStartLeft +
                            dx
                        )
                        .roundToInt()
                        .coerceIn(
                            0,
                            maxLeft
                        )
                val top =
                    (
                        slot.dragStartTop +
                            dy
                        )
                        .roundToInt()
                        .coerceIn(
                            0,
                            maxTop
                        )

                slot.container
                    .layoutParams =
                    LayoutParams(
                        childWidth,
                        childHeight
                    ).apply {
                        leftMargin = left
                        topMargin = top
                    }

                updateDragFractions(
                    slot,
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
                    slot
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
        slot: Slot,
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

        slot.dragCenterXFraction =
            (
                left +
                    childWidth / 2f
                ) /
                width
        slot.dragCenterYFraction =
            (
                top +
                    childHeight / 2f
                ) /
                height
    }

    private fun persistPlantPosition(
        slot: Slot
    ) {
        val x =
            slot.dragCenterXFraction
                ?: return
        val y =
            slot.dragCenterYFraction
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

    private fun applyImageTint(
        slot: Slot
    ) {
        slot.image.colorFilter =
            if (
                slot.placement
                    ?.yellowTint ==
                    true
            ) {
                yellowImageFilter
            } else {
                null
            }
    }

    private fun setStaticSuppressed(
        slot: Slot,
        suppressed: Boolean
    ) {
        if (
            slot.staticSuppressed ==
                suppressed
        ) {
            return
        }

        slot.placement
            ?.setStaticSuppressed
            ?.invoke(
                suppressed
            )
        slot.staticSuppressed =
            suppressed
    }

    private fun retireSlot(
        slot: Slot
    ) {
        setStaticSuppressed(
            slot,
            false
        )
        slot.video.stopPlayback()
        slot.video.visibility =
            View.INVISIBLE
        slot.image.visibility =
            View.GONE
        slot.container.visibility =
            View.INVISIBLE
        slot.active = false
        slot.hasRenderedFrame = false
        slot.placement = null
    }

    private fun refreshVisibility() {
        visibility =
            if (
                slots.any {
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
    }
}

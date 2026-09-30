package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.widget.ImageView

class ChromaKeySpriteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ImageView(
    context,
    attrs
), ChromaKeyPlayback {
    override var logicalLayer: String =
        "SPRITE"

    private val handler =
        Handler(
            Looper.getMainLooper()
        )

    private var keyColor =
        ChromaKeyColor.BLUE

    private var yellowTint =
        false

    private var generation = 0

    private var currentSequence:
        SpriteSequence? = null

    private var currentFrame = 0
    private var startedAtMs = 0L
    private var logicalStartedAtMs = 0L
    private var currentStageHeight = 0
    private var heldFirstFrame = false
    private var waitingForFullSequence = false
    private var performanceActive = false
    private var completion:
        (() -> Unit)? = null
    private var error:
        ((String) -> Unit)? = null

    private val frameRunnable =
        Runnable {
            renderFrame()
        }

    init {
        scaleType =
            ScaleType.FIT_CENTER
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun play(
        assetPath: String,
        muted: Boolean,
        onCompletion: () -> Unit,
        onError: (String) -> Unit,
        onStarted: () -> Unit,
        revealOnFirstFrame: Boolean,
        holdOnFirstFrame: Boolean,
        onFirstFrameRendered: () -> Unit
    ) {
        stopPlayback()
        generation += 1
        val playGeneration =
            generation

        completion =
            onCompletion
        error =
            onError
        heldFirstFrame =
            holdOnFirstFrame

        val targetResolution =
            RichMediaSettings
                .spriteResolutionFor(
                    context
                )
        val stages =
            SpriteProgressivePolicy
                .stagesFor(
                    targetResolution
                )

        MediaTrace.event(
            source = traceSource(),
            event = "PLAY_REQUEST",
            assetPath = assetPath,
            detail =
                "backend=SpriteRGBA" +
                    " target=" +
                    targetResolution.label +
                    " stages=" +
                    stages.joinToString(
                        separator = ">"
                    ) {
                        it.toString() +
                            "p"
                    } +
                    " key=" +
                    keyColor.name +
                    " yellow=" +
                    yellowTint
        )

        var playbackStarted =
            false
        val requestedStages =
            linkedSetOf<Int>()
        val advancedStages =
            linkedSetOf<Int>()

        fun rebaseFrame(
            sequence: SpriteSequence,
            partial: Boolean
        ) {
            val elapsed =
                (
                    SystemClock
                        .uptimeMillis() -
                        logicalStartedAtMs
                    ).coerceAtLeast(
                    0L
                )
            val logicalFrame =
                (
                    elapsed /
                        sequence
                            .frameDurationMs
                    ).toInt()

            currentFrame =
                if (partial) {
                    if (
                        sequence.frames
                            .isEmpty()
                    ) {
                        0
                    } else {
                        logicalFrame %
                            sequence.frames
                                .size
                    }
                } else {
                    logicalFrame
                        .coerceAtMost(
                            (
                                sequence.frames
                                    .size -
                                    1
                                ).coerceAtLeast(0)
                        )
                }

            startedAtMs =
                if (partial) {
                    SystemClock
                        .uptimeMillis() -
                        currentFrame *
                            sequence
                                .frameDurationMs
                } else {
                    logicalStartedAtMs
                }
        }

        fun acceptStage(
            sequence: SpriteSequence,
            height: Int,
            partial: Boolean
        ) {
            if (
                playGeneration !=
                    generation ||
                sequence.frames
                    .isEmpty()
            ) {
                return
            }

            if (!playbackStarted) {
                playbackStarted = true
                currentSequence =
                    sequence
                currentFrame = 0
                currentStageHeight =
                    height
                waitingForFullSequence =
                    partial
                logicalStartedAtMs =
                    SystemClock
                        .uptimeMillis()
                startedAtMs =
                    logicalStartedAtMs

                performanceActive =
                    true
                AnimationPerformanceMonitor
                    .spriteStarted(
                        logicalLayer,
                        assetPath
                    )

                MediaTrace.event(
                    source = traceSource(),
                    event =
                        if (partial) {
                            "QUICK_START"
                        } else {
                            "BANK_START"
                        },
                    assetPath =
                        assetPath,
                    detail =
                        "frames=" +
                            sequence.frames.size +
                            " resolution=" +
                            height +
                            "p target=" +
                            targetResolution.label
                )

                onStarted()

                renderFrame(
                    firstFrameCallback =
                        onFirstFrameRendered
                )
                return
            }

            if (
                height <
                    currentStageHeight
            ) {
                return
            }

            val previousHeight =
                currentStageHeight
            val qualityChanged =
                height >
                    previousHeight

            currentSequence =
                sequence
            currentStageHeight =
                height
            waitingForFullSequence =
                partial
            rebaseFrame(
                sequence,
                partial
            )

            handler.removeCallbacks(
                frameRunnable
            )

            if (qualityChanged) {
                MediaTrace.event(
                    source = traceSource(),
                    event =
                        "QUALITY_STAGE_SWAP",
                    assetPath =
                        assetPath,
                    detail =
                        "from=" +
                            previousHeight +
                            "p to=" +
                            height +
                            "p partial=" +
                            partial +
                            " frame=" +
                            currentFrame
                )
            } else if (!partial) {
                MediaTrace.event(
                    source = traceSource(),
                    event =
                        "FULL_SEQUENCE_READY",
                    assetPath =
                        assetPath,
                    detail =
                        "frames=" +
                            sequence.frames.size +
                            " resolution=" +
                            height +
                            "p"
                )
            }

            renderFrame()
        }

        fun priorityFor(
            height: Int
        ): SpriteFactoryPriority =
            when {
                height <=
                    SpriteProgressivePolicy
                        .INTERNAL_LOW_HEIGHT ->
                    SpriteFactoryPriority
                        .VISIBLE

                height <= 120 ->
                    SpriteFactoryPriority
                        .VISIBLE_BOOTSTRAP

                SpriteProgressivePolicy
                    .isSecondary(height) ->
                    SpriteFactoryPriority
                        .SECONDARY

                else ->
                    SpriteFactoryPriority
                        .VISIBLE_UPGRADE
            }

        var requestStage:
            ((Int) -> Unit)? =
            null

        fun advance(
            index: Int
        ) {
            if (
                !advancedStages.add(
                    index
                )
            ) {
                return
            }

            val next =
                index + 1

            if (next < stages.size) {
                requestStage
                    ?.invoke(next)
            }
        }

        requestStage = {
                index ->
            if (
                index !in
                    stages.indices
            ) {
                Unit
            } else {
                val height =
                    stages[index]

                if (
                    requestedStages.add(
                        height
                    )
                ) {
                    MediaTrace.event(
                        source =
                            traceSource(),
                        event =
                            "QUALITY_STAGE_REQUEST",
                        assetPath =
                            assetPath,
                        detail =
                            "resolution=" +
                                height +
                                "p target=" +
                                targetResolution
                                    .label
                    )

                    SpriteBankFactory
                        .prepare(
                            context =
                                context,
                            assetPath =
                                assetPath,
                            keyColor =
                                keyColor,
                            resolutionHeight =
                                height,
                            requestedBy =
                                logicalLayer,
                            priority =
                                priorityFor(
                                    height
                                ),
                            onQuickReady = {
                                partial ->
                                if (
                                    playGeneration ==
                                        generation
                                ) {
                                    acceptStage(
                                        sequence =
                                            partial,
                                        height =
                                            height,
                                        partial =
                                            true
                                    )
                                    advance(
                                        index
                                    )
                                }
                            }
                        ) {
                            result ->
                            if (
                                playGeneration !=
                                    generation
                            ) {
                                return@prepare
                            }

                            result.fold(
                                onSuccess = {
                                    sequence ->
                                    acceptStage(
                                        sequence =
                                            sequence,
                                        height =
                                            height,
                                        partial =
                                            false
                                    )
                                    advance(
                                        index
                                    )
                                },
                                onFailure = {
                                    throwable ->
                                    MediaTrace.event(
                                        source =
                                            traceSource(),
                                        event =
                                            "QUALITY_STAGE_ERROR",
                                        assetPath =
                                            assetPath,
                                        detail =
                                            "resolution=" +
                                                height +
                                                "p error=" +
                                                throwable
                                                    .javaClass
                                                    .simpleName
                                    )

                                    if (
                                        index + 1 <
                                            stages.size
                                    ) {
                                        advance(
                                            index
                                        )
                                    } else if (
                                        !playbackStarted
                                    ) {
                                        fail(
                                            assetPath,
                                            throwable
                                        )
                                    }
                                }
                            )
                        }
                }
            }
        }

        requestStage
            ?.invoke(0)
    }

    private fun renderFrame(
        firstFrameCallback:
            (() -> Unit)? = null
    ) {
        val sequence =
            currentSequence
                ?: return

        if (
            currentFrame >=
                sequence.frames.size
        ) {
            if (waitingForFullSequence) {
                loopPartialSequence(
                    sequence
                )
            } else {
                finishPlayback()
            }
            return
        }

        val startedNs =
            System.nanoTime()

        val bitmap =
            SpriteFrameCache.bitmapFor(
                sequence.frames[
                    currentFrame
                ]
            )

        if (bitmap == null) {
            fail(
                sequence.frames[
                    currentFrame
                ].name,
                IllegalStateException(
                    "Sprite frame decode failed"
                )
            )
            return
        }

        setImageBitmap(bitmap)

        (
            drawable as?
                BitmapDrawable
            )?.isFilterBitmap =
            true

        firstFrameCallback?.invoke()

        AnimationPerformanceMonitor
            .recordSpriteFrame(
                System.nanoTime() -
                    startedNs
            )

        if (
            currentFrame == 0 &&
            heldFirstFrame
        ) {
            return
        }

        currentFrame += 1

        if (
            currentFrame >=
                sequence.frames.size
        ) {
            if (waitingForFullSequence) {
                loopPartialSequence(
                    sequence
                )
            } else {
                finishPlayback()
            }
            return
        }

        scheduleNextFrame(sequence)
    }

    private fun loopPartialSequence(
        sequence: SpriteSequence
    ) {
        currentFrame = 0
        startedAtMs =
            SystemClock
                .uptimeMillis()

        handler.postDelayed(
            frameRunnable,
            sequence.frameDurationMs
        )
    }

    private fun scheduleNextFrame(
        sequence: SpriteSequence
    ) {
        val targetMs =
            startedAtMs +
                currentFrame *
                    sequence
                        .frameDurationMs

        val delay =
            (
                targetMs -
                    SystemClock
                        .uptimeMillis()
                ).coerceAtLeast(
                0L
            )

        handler.postDelayed(
            frameRunnable,
            delay
        )
    }

    override fun revealHeldFirstFrame():
        Boolean {
        if (!heldFirstFrame) {
            return false
        }

        heldFirstFrame = false
        currentFrame += 1

        currentSequence?.let {
            if (
                currentFrame <
                    it.frames.size
            ) {
                scheduleNextFrame(it)
            } else {
                finishPlayback()
            }
        }

        return true
    }

    override fun setMuted(
        value: Boolean
    ) {
        // Sprite playback has no decoded audio stream.
    }

    override fun setYellowTint(
        enabled: Boolean
    ) {
        yellowTint = enabled

        colorFilter =
            if (enabled) {
                ColorMatrixColorFilter(
                    ColorMatrix(
                        GomokuYellowFilterPolicy
                            .colorMatrixValues()
                    )
                )
            } else {
                null
            }
    }

    override fun setKeyColor(
        color: ChromaKeyColor
    ) {
        keyColor = color
    }

    override fun stopPlayback() {
        generation += 1
        handler.removeCallbacks(
            frameRunnable
        )

        if (performanceActive) {
            AnimationPerformanceMonitor
                .spriteStopped(
                    logicalLayer
                )
        }

        performanceActive = false
        currentSequence = null
        currentFrame = 0
        currentStageHeight = 0
        logicalStartedAtMs = 0L
        startedAtMs = 0L
        heldFirstFrame = false
        waitingForFullSequence = false
        completion = null
        error = null
        setImageBitmap(null)
    }

    override fun release() {
        stopPlayback()
    }

    private fun finishPlayback() {
        val callback = completion

        handler.removeCallbacks(
            frameRunnable
        )

        if (performanceActive) {
            AnimationPerformanceMonitor
                .spriteStopped(
                    logicalLayer
                )
        }

        performanceActive = false
        currentSequence = null
        waitingForFullSequence = false
        completion = null
        error = null
        callback?.invoke()
    }

    private fun fail(
        assetPath: String,
        throwable: Throwable
    ) {
        val callback = error

        MediaTrace.event(
            source = traceSource(),
            event = "ERROR",
            assetPath = assetPath,
            detail =
                "backend=SpriteRGBA error=" +
                    throwable
                        .javaClass
                        .simpleName +
                    ":" +
                    (
                        throwable.message
                            ?: ""
                        )
        )

        stopPlayback()

        callback?.invoke(
            throwable.message
                ?: "Sprite playback error"
        )
    }

    private fun traceSource():
        String =
        "ChromaSprite[" +
            logicalLayer +
            "]"
}

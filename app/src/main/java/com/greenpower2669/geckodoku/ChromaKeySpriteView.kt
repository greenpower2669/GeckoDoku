package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
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
    private var heldFirstFrame = false
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

        val resolution =
            RichMediaSettings
                .spriteResolutionFor(
                    context
                )

        MediaTrace.event(
            source = traceSource(),
            event = "PLAY_REQUEST",
            assetPath = assetPath,
            detail =
                "backend=SpriteRGBA" +
                    " resolution=" +
                    resolution.label +
                    " key=" +
                    keyColor.name +
                    " yellow=" +
                    yellowTint
        )

        SpriteFrameCache.prepare(
            context = context,
            assetPath = assetPath,
            keyColor = keyColor,
            resolution = resolution
        ) { result ->
            if (
                playGeneration !=
                    generation
            ) {
                return@prepare
            }

            result.fold(
                onSuccess = { sequence ->
                    currentSequence =
                        sequence
                    currentFrame = 0
                    startedAtMs =
                        SystemClock
                            .uptimeMillis()

                    performanceActive =
                        true
                    AnimationPerformanceMonitor
                        .spriteStarted(
                            logicalLayer,
                            assetPath
                        )

                    onStarted()

                    renderFrame(
                        firstFrameCallback =
                            onFirstFrameRendered
                    )
                },
                onFailure = { throwable ->
                    fail(
                        assetPath,
                        throwable
                    )
                }
            )
        }
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
            finishPlayback()
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
            finishPlayback()
            return
        }

        scheduleNextFrame(sequence)
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
        heldFirstFrame = false
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

package com.greenpower2669.geckodoku

import android.view.Choreographer
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

object AnimationPerformanceMonitor :
    Choreographer.FrameCallback {
    private const val REPORT_INTERVAL_NS =
        5_000_000_000L

    private var running = false
    private var lastFrameNs = 0L
    private var windowStartNs = 0L
    private var uiFrameCount = 0L
    private var uiFrameTotalNs = 0L
    private var uiFrameMaxNs = 0L

    private val activeSprites =
        AtomicInteger(0)
    private val peakSprites =
        AtomicInteger(0)
    private val spriteFrameCount =
        AtomicLong(0L)
    private val spriteFrameTotalNs =
        AtomicLong(0L)

    fun start() {
        if (running) return

        running = true
        lastFrameNs = 0L
        windowStartNs = 0L

        Choreographer.getInstance()
            .postFrameCallback(this)
    }

    fun stop() {
        if (!running) return

        running = false
        Choreographer.getInstance()
            .removeFrameCallback(this)
        report(finalReport = true)
    }

    override fun doFrame(
        frameTimeNanos: Long
    ) {
        if (!running) return

        if (windowStartNs == 0L) {
            windowStartNs =
                frameTimeNanos
        }

        if (lastFrameNs != 0L) {
            val delta =
                frameTimeNanos -
                    lastFrameNs

            uiFrameCount += 1L
            uiFrameTotalNs += delta

            if (delta > uiFrameMaxNs) {
                uiFrameMaxNs = delta
            }
        }

        lastFrameNs = frameTimeNanos

        if (
            frameTimeNanos -
                windowStartNs >=
                REPORT_INTERVAL_NS
        ) {
            report(finalReport = false)

            windowStartNs =
                frameTimeNanos
            uiFrameCount = 0L
            uiFrameTotalNs = 0L
            uiFrameMaxNs = 0L
            spriteFrameCount.set(0L)
            spriteFrameTotalNs.set(0L)
        }

        Choreographer.getInstance()
            .postFrameCallback(this)
    }

    fun spriteStarted(
        owner: String,
        asset: String
    ) {
        val active =
            activeSprites
                .incrementAndGet()

        while (true) {
            val peak =
                peakSprites.get()

            if (
                active <= peak ||
                peakSprites
                    .compareAndSet(
                        peak,
                        active
                    )
            ) {
                break
            }
        }

        MediaTrace.event(
            source = "SpritePerf",
            event = "SPRITE_ACTIVE",
            assetPath = asset,
            detail =
                "owner=" +
                    owner +
                    " active=" +
                    active +
                    " peak=" +
                    peakSprites.get()
        )
    }

    fun spriteStopped(
        owner: String
    ) {
        var active =
            activeSprites.get()

        while (active > 0) {
            if (
                activeSprites
                    .compareAndSet(
                        active,
                        active - 1
                    )
            ) {
                active -= 1
                break
            }

            active =
                activeSprites.get()
        }

        MediaTrace.event(
            source = "SpritePerf",
            event = "SPRITE_INACTIVE",
            detail =
                "owner=" +
                    owner +
                    " active=" +
                    active +
                    " peak=" +
                    peakSprites.get()
        )
    }

    fun recordSpriteFrame(
        renderNs: Long
    ) {
        spriteFrameCount
            .incrementAndGet()
        spriteFrameTotalNs
            .addAndGet(renderNs)
    }

    private fun report(
        finalReport: Boolean
    ) {
        val uiFrames =
            uiFrameCount

        val uiAverageNs =
            if (uiFrames > 0L) {
                uiFrameTotalNs /
                    uiFrames
            } else {
                0L
            }

        val uiFps =
            if (uiAverageNs > 0L) {
                1_000_000_000.0 /
                    uiAverageNs
            } else {
                0.0
            }

        val spriteFrames =
            spriteFrameCount.get()

        val spriteAverageNs =
            if (spriteFrames > 0L) {
                spriteFrameTotalNs
                    .get() /
                    spriteFrames
            } else {
                0L
            }

        val runtime =
            Runtime.getRuntime()

        val usedMb =
            (
                runtime.totalMemory() -
                    runtime.freeMemory()
                ) /
                (1024L * 1024L)

        MediaTrace.event(
            source = "PerfMonitor",
            event =
                if (finalReport) {
                    "PERF_FINAL"
                } else {
                    "PERF_SAMPLE"
                },
            detail =
                "uiFps=" +
                    f(uiFps) +
                    " uiAvgMs=" +
                    f(
                        uiAverageNs /
                            1_000_000.0
                    ) +
                    " uiMaxMs=" +
                    f(
                        uiFrameMaxNs /
                            1_000_000.0
                    ) +
                    " activeAnimations=" +
                    activeSprites.get() +
                    " peakAnimations=" +
                    peakSprites.get() +
                    " spriteFrames=" +
                    spriteFrames +
                    " spriteAvgDecodeMs=" +
                    f(
                        spriteAverageNs /
                            1_000_000.0
                    ) +
                    " spriteMemMb=" +
                    f(
                        SpriteFrameCache
                            .memoryBytes() /
                            (1024.0 * 1024.0)
                    ) +
                    " jvmUsedMb=" +
                    usedMb
        )
    }

    private fun f(
        value: Double
    ): String =
        String.format(
            Locale.US,
            "%.2f",
            value
        )
}

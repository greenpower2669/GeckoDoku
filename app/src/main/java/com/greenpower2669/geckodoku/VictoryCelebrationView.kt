package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class VictoryCelebrationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    var onFireworkBurst:
        ((Int, Int, Boolean) -> Unit)? =
        null

    var onCelebrationStopped:
        (() -> Unit)? =
        null

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val colors =
        intArrayOf(
            Color.rgb(255, 64, 64),
            Color.rgb(255, 162, 32),
            Color.rgb(255, 220, 48),
            Color.rgb(62, 210, 92),
            Color.rgb(55, 190, 255),
            Color.rgb(82, 96, 255),
            Color.rgb(191, 76, 255),
            Color.rgb(255, 82, 188)
        )

    private var startedAt = 0L
    private var durationMs = 2600L
    private var level = 0
    private var difficultyLabel = ""
    private var emittedBursts = 0
    private var running = false

    init {
        visibility = GONE
        isClickable = true

        setOnClickListener {
            stop()
        }
    }

    fun start(
        difficulty: GameDifficulty
    ) {
        level = difficulty.ordinal
        difficultyLabel = difficulty.label
        durationMs = 2200L + level * 320L
        startedAt = SystemClock.uptimeMillis()
        emittedBursts = 0
        running = true
        visibility = VISIBLE
        bringToFront()
        invalidate()
    }

    fun stop() {
        if (!running &&
            visibility == GONE
        ) {
            return
        }

        running = false
        visibility = GONE
        onCelebrationStopped?.invoke()
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(canvas)

        if (
            visibility != VISIBLE ||
            !running
        ) {
            return
        }

        val elapsed =
            SystemClock.uptimeMillis() -
                startedAt

        if (elapsed >= durationMs) {
            stop()
            return
        }

        emitSoundBursts(elapsed)

        paint.style = Paint.Style.FILL
        paint.color =
            Color.argb(
                88,
                7,
                18,
                28
            )

        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            paint
        )

        drawConfetti(
            canvas,
            elapsed
        )

        drawFireworks(
            canvas,
            elapsed
        )

        drawMessage(canvas)

        postInvalidateOnAnimation()
    }

    private fun emitSoundBursts(
        elapsed: Long
    ) {
        val bursts =
            2 + level

        while (
            emittedBursts < bursts &&
            elapsed >=
                emittedBursts * 180L
        ) {
            val index =
                emittedBursts

            onFireworkBurst?.invoke(
                level,
                index,
                index == bursts - 1
            )

            emittedBursts += 1
        }
    }

    private fun drawFireworks(
        canvas: Canvas,
        elapsed: Long
    ) {
        val bursts = 2 + level
        val particleCount =
            10 + level * 3

        for (burst in 0 until bursts) {
            val localElapsed =
                elapsed -
                    burst * 180L

            if (localElapsed < 0L) {
                continue
            }

            val progress =
                (localElapsed / 1250f)
                    .coerceIn(
                        0f,
                        1f
                    )

            if (progress >= 1f) {
                continue
            }

            val cx =
                width *
                    (
                        0.14f +
                            (
                                (
                                    burst * 37 +
                                        11
                                    ) % 72
                                ) / 100f
                        )

            val cy =
                height *
                    (
                        0.12f +
                            (
                                (
                                    burst * 29 +
                                        7
                                    ) % 45
                                ) / 100f
                        )

            val maxRadius =
                width *
                    (
                        0.16f +
                            level * 0.012f
                        )

            val radius =
                maxRadius *
                    (
                        1f -
                            (1f - progress) *
                            (1f - progress)
                        )

            val alpha =
                (
                    255 *
                        (1f - progress)
                    ).toInt()
                    .coerceIn(
                        0,
                        255
                    )

            for (
                particle in
                0 until particleCount
            ) {
                val angle =
                    2.0 *
                        PI *
                        particle /
                        particleCount +
                        burst * 0.19

                val wobble =
                    0.86f +
                        (
                            (
                                particle * 17 +
                                    burst * 13
                                ) % 24
                            ) / 100f

                val x =
                    cx +
                        cos(angle)
                            .toFloat() *
                        radius *
                        wobble

                val y =
                    cy +
                        sin(angle)
                            .toFloat() *
                        radius *
                        wobble +
                        progress *
                        progress *
                        dp(28f)

                val color =
                    colors[
                        (
                            particle +
                                burst * 2
                            ) %
                            colors.size
                    ]

                paint.color =
                    Color.argb(
                        alpha,
                        Color.red(color),
                        Color.green(color),
                        Color.blue(color)
                    )

                canvas.drawCircle(
                    x,
                    y,
                    dp(
                        2.2f +
                            level * 0.15f
                    ),
                    paint
                )
            }
        }
    }

    private fun drawConfetti(
        canvas: Canvas,
        elapsed: Long
    ) {
        val count =
            28 +
                level * 14

        for (i in 0 until count) {
            val x =
                width *
                    (
                        (
                            i * 47 +
                                17
                            ) % 100
                        ) / 100f

            val speed =
                0.055f +
                    (
                        (
                            i * 11
                            ) % 18
                        ) / 1000f

            val start =
                (
                    (
                        i * 83
                        ) %
                        (height + 300)
                    ) -
                    300

            val y =
                (
                    start.toFloat() +
                        elapsed.toFloat() *
                        speed
                    ) %
                    (
                        height.toFloat() +
                            dp(40f)
                        )

            val color =
                colors[
                    (
                        i * 3 +
                            level
                        ) %
                        colors.size
                ]

            paint.color =
                Color.argb(
                    220,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )

            canvas.save()

            val angle =
                (
                    (i * 31).toFloat() +
                        elapsed.toFloat() /
                        18f
                    ) % 360f

            canvas.rotate(
                angle,
                x,
                y
            )

            canvas.drawRoundRect(
                RectF(
                    x - dp(2.5f),
                    y - dp(6f),
                    x + dp(2.5f),
                    y + dp(6f)
                ),
                dp(1.5f),
                dp(1.5f),
                paint
            )

            canvas.restore()
        }
    }

    private fun drawMessage(
        canvas: Canvas
    ) {
        val boxWidth =
            width * 0.82f

        val boxHeight =
            dp(
                if (level >= 6) {
                    116f
                } else {
                    100f
                }
            )

        val left =
            (width - boxWidth) /
                2f

        val top =
            height * 0.42f

        paint.color =
            Color.argb(
                215,
                255,
                255,
                255
            )

        canvas.drawRoundRect(
            RectF(
                left,
                top,
                left + boxWidth,
                top + boxHeight
            ),
            dp(24f),
            dp(24f),
            paint
        )

        paint.textAlign =
            Paint.Align.CENTER

        paint.isFakeBoldText =
            true

        paint.textSize =
            sp(
                if (level >= 6) {
                    28f
                } else {
                    25f
                }
            )

        paint.color =
            Color.rgb(
                18,
                91,
                48
            )

        canvas.drawText(
            if (level >= 7) {
                "INFERNAL VAINCU ! 🎆"
            } else if (level >= 6) {
                "MISSION ACCOMPLIE ! 🎇"
            } else {
                "FÉLICITATIONS ! 🦎"
            },
            width / 2f,
            top + dp(44f),
            paint
        )

        paint.isFakeBoldText =
            false

        paint.textSize =
            sp(17f)

        paint.color =
            Color.rgb(
                38,
                38,
                38
            )

        canvas.drawText(
            difficultyLabel +
                " • grille terminée",
            width / 2f,
            top + dp(76f),
            paint
        )

        if (level >= 5) {
            paint.textSize =
                sp(14f)

            canvas.drawText(
                if (level >= 7) {
                    "Quel monstre de GeckoDoku ! 😈🦎"
                } else {
                    "Magnifique résolution !"
                },
                width / 2f,
                top + dp(99f),
                paint
            )
        }
    }

    private fun dp(
        value: Float
    ): Float =
        value *
            resources
                .displayMetrics
                .density

    private fun sp(
        value: Float
    ): Float =
        value *
            resources
                .displayMetrics
                .scaledDensity
}

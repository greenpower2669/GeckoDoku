package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class PlayerStatsTrendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private var events:
        List<PlayerStatEvent> =
        emptyList()

    private var difficulty:
        GameDifficulty =
        GameDifficulty.EASY

    private val paint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        )

    fun setData(
        difficulty:
            GameDifficulty,
        events:
            List<PlayerStatEvent>
    ) {
        this.difficulty =
            difficulty

        this.events =
            events.sortedBy {
                it.occurredAt
            }

        contentDescription =
            buildDescription()

        invalidate()
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        setMeasuredDimension(
            MeasureSpec.getSize(
                widthMeasureSpec
            ),
            resolveSize(
                dp(340f)
                    .toInt(),
                heightMeasureSpec
            )
        )
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(
            canvas
        )

        canvas.drawColor(
            Color.rgb(
                250,
                251,
                248
            )
        )

        val completed =
            events.filter {
                it.completed
            }

        val padding =
            dp(18f)

        val titleHeight =
            dp(32f)

        paint.style =
            Paint.Style.FILL
        paint.textAlign =
            Paint.Align.LEFT
        paint.isFakeBoldText =
            true
        paint.textSize =
            dp(16f)
        paint.color =
            Color.rgb(
                45,
                55,
                48
            )

        canvas.drawText(
            difficulty.label +
                " • évolution",
            padding,
            padding +
                dp(15f),
            paint
        )

        val usableTop =
            padding +
                titleHeight

        val usableBottom =
            height -
                padding

        val gap =
            dp(24f)

        val half =
            (
                usableBottom -
                    usableTop -
                    gap
                ) /
                2f

        val timeRect =
            RectF(
                padding,
                usableTop,
                width -
                    padding,
                usableTop +
                    half
            )

        val starRect =
            RectF(
                padding,
                timeRect.bottom +
                    gap,
                width -
                    padding,
                usableBottom
            )

        drawPanel(
            canvas,
            timeRect,
            "Temps • plus bas = plus rapide"
        )

        drawPanel(
            canvas,
            starRect,
            "Étoiles • plus haut = mieux"
        )

        if (
            completed.isEmpty()
        ) {
            drawEmpty(
                canvas,
                timeRect,
                "Aucune partie terminée"
            )

            drawEmpty(
                canvas,
                starRect,
                "Pas encore de tendance"
            )

            return
        }

        drawTimeSeries(
            canvas,
            timeRect,
            completed
        )

        drawStarSeries(
            canvas,
            starRect,
            completed
        )

        drawAbandonedMarks(
            canvas,
            timeRect
        )
    }

    private fun drawPanel(
        canvas: Canvas,
        rect: RectF,
        title: String
    ) {
        paint.style =
            Paint.Style.FILL
        paint.color =
            Color.rgb(
                244,
                247,
                243
            )

        canvas.drawRoundRect(
            rect,
            dp(10f),
            dp(10f),
            paint
        )

        paint.style =
            Paint.Style.STROKE
        paint.strokeWidth =
            dp(1.2f)
        paint.color =
            Color.rgb(
                115,
                124,
                117
            )

        canvas.drawRoundRect(
            rect,
            dp(10f),
            dp(10f),
            paint
        )

        paint.style =
            Paint.Style.FILL
        paint.textAlign =
            Paint.Align.LEFT
        paint.isFakeBoldText =
            false
        paint.textSize =
            dp(12f)
        paint.color =
            Color.rgb(
                65,
                72,
                67
            )

        canvas.drawText(
            title,
            rect.left +
                dp(8f),
            rect.top +
                dp(17f),
            paint
        )
    }

    private fun drawTimeSeries(
        canvas: Canvas,
        rect: RectF,
        completed:
            List<PlayerStatEvent>
    ) {
        val values =
            completed.map {
                it.elapsedSeconds
                    .toFloat()
            }

        val maxValue =
            max(
                1f,
                values.maxOrNull()
                    ?: 1f
            )

        drawSeries(
            canvas =
                canvas,
            rect =
                rect,
            values =
                values,
            minValue =
                0f,
            maxValue =
                maxValue,
            invert =
                true
        )
    }

    private fun drawStarSeries(
        canvas: Canvas,
        rect: RectF,
        completed:
            List<PlayerStatEvent>
    ) {
        drawSeries(
            canvas =
                canvas,
            rect =
                rect,
            values =
                completed.map {
                    it.stars
                        .toFloat()
                },
            minValue =
                1f,
            maxValue =
                5f,
            invert =
                false
        )
    }

    private fun drawSeries(
        canvas: Canvas,
        rect: RectF,
        values: List<Float>,
        minValue: Float,
        maxValue: Float,
        invert: Boolean
    ) {
        if (values.isEmpty()) {
            return
        }

        val left =
            rect.left +
                dp(12f)

        val right =
            rect.right -
                dp(12f)

        val top =
            rect.top +
                dp(28f)

        val bottom =
            rect.bottom -
                dp(12f)

        val range =
            (
                maxValue -
                    minValue
                )
                .coerceAtLeast(
                    1f
                )

        fun pointX(
            index: Int
        ): Float =
            if (
                values.size <=
                    1
            ) {
                (
                    left +
                        right
                    ) /
                    2f
            } else {
                left +
                    (
                        right -
                            left
                        ) *
                    index.toFloat() /
                    (
                        values.size -
                            1
                        )
            }

        fun pointY(
            value: Float
        ): Float {
            val normalized =
                (
                    value -
                        minValue
                    ) /
                    range

            val visual =
                if (invert) {
                    normalized
                } else {
                    1f -
                        normalized
                }

            return top +
                (
                    bottom -
                        top
                    ) *
                visual
        }

        paint.style =
            Paint.Style.STROKE
        paint.strokeWidth =
            dp(2.4f)
        paint.color =
            Color.rgb(
                58,
                126,
                79
            )

        for (
            index in
            1 until
                values.size
        ) {
            canvas.drawLine(
                pointX(
                    index -
                        1
                ),
                pointY(
                    values[
                        index -
                            1
                    ]
                ),
                pointX(
                    index
                ),
                pointY(
                    values[
                        index
                    ]
                ),
                paint
            )
        }

        paint.style =
            Paint.Style.FILL

        values.forEachIndexed {
                index,
                value ->

            canvas.drawCircle(
                pointX(
                    index
                ),
                pointY(
                    value
                ),
                dp(4.5f),
                paint
            )
        }
    }

    private fun drawAbandonedMarks(
        canvas: Canvas,
        rect: RectF
    ) {
        val abandoned =
            events.filter {
                !it.completed &&
                    it.mistakes >
                        0
            }

        if (abandoned.isEmpty()) {
            return
        }

        paint.style =
            Paint.Style.STROKE
        paint.strokeWidth =
            dp(2f)
        paint.color =
            Color.rgb(
                170,
                62,
                52
            )

        val y =
            rect.bottom -
                dp(13f)

        abandoned
            .takeLast(
                10
            )
            .forEachIndexed {
                    index,
                    _ ->

                val x =
                    rect.right -
                        dp(12f) -
                        index *
                            dp(10f)

                canvas.drawLine(
                    x -
                        dp(3f),
                    y -
                        dp(3f),
                    x +
                        dp(3f),
                    y +
                        dp(3f),
                    paint
                )

                canvas.drawLine(
                    x -
                        dp(3f),
                    y +
                        dp(3f),
                    x +
                        dp(3f),
                    y -
                        dp(3f),
                    paint
                )
            }
    }

    private fun drawEmpty(
        canvas: Canvas,
        rect: RectF,
        message: String
    ) {
        paint.style =
            Paint.Style.FILL
        paint.textAlign =
            Paint.Align.CENTER
        paint.textSize =
            dp(13f)
        paint.color =
            Color.rgb(
                95,
                101,
                96
            )

        canvas.drawText(
            message,
            rect.centerX(),
            rect.centerY() -
                (
                    paint.ascent() +
                        paint.descent()
                    ) /
                    2f,
            paint
        )
    }

    private fun buildDescription():
        String {
        val completed =
            events.filter {
                it.completed
            }

        val abandoned =
            events.count {
                !it.completed &&
                    it.mistakes >
                        0
            }

        return difficulty.label +
            ". " +
            completed.size +
            " parties terminées. " +
            abandoned +
            " parties annulées avec erreur. " +
            events.sumOf {
                it.mistakes
            } +
            " erreurs."
    }

    private fun dp(
        value: Float
    ): Float =
        value *
            resources
                .displayMetrics
                .density
}

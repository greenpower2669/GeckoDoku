package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max

class HypothesisTimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    var timelineProvider:
        () ->
            List<HypothesisTimelineEntry> =
        {
            emptyList()
        }

    var onEntrySelected:
        ((Int) -> Unit)? = null

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val hitRects =
        linkedMapOf<Int, RectF>()

    init {
        isClickable = true
        isFocusable = true
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription =
            "Frise chronologique des hypothèses"
        setPadding(
            dp(6f).toInt(),
            dp(4f).toInt(),
            dp(6f).toInt(),
            dp(4f).toInt()
        )
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        val desiredHeight =
            dp(42f).toInt()

        setMeasuredDimension(
            MeasureSpec.getSize(
                widthMeasureSpec
            ),
            resolveSize(
                desiredHeight,
                heightMeasureSpec
            )
        )
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(canvas)

        val entries =
            timelineProvider()
                .sortedBy {
                    it.order
                }

        hitRects.clear()

        paint.style =
            Paint.Style.FILL
        paint.color =
            Color.rgb(
                238,
                241,
                238
            )

        val outer =
            RectF(
                paddingLeft.toFloat(),
                paddingTop.toFloat(),
                (
                    width -
                        paddingRight
                    ).toFloat(),
                (
                    height -
                        paddingBottom
                    ).toFloat()
            )

        canvas.drawRoundRect(
            outer,
            dp(9f),
            dp(9f),
            paint
        )

        if (entries.isEmpty()) {
            paint.color =
                Color.rgb(
                    74,
                    82,
                    78
                )
            paint.textSize =
                dp(13f)
            paint.textAlign =
                Paint.Align.CENTER
            paint.isFakeBoldText =
                true

            canvas.drawText(
                "Hypothèses • aucune branche active",
                outer.centerX(),
                outer.centerY() -
                    (
                        paint.ascent() +
                            paint.descent()
                        ) /
                        2f,
                paint
            )

            paint.isFakeBoldText =
                false
            contentDescription =
                "Frise des hypothèses. Aucune hypothèse active."
            return
        }

        val gap =
            dp(4f)

        val labelWidth =
            dp(72f)

        paint.color =
            Color.rgb(
                55,
                62,
                58
            )
        paint.textSize =
            dp(12f)
        paint.textAlign =
            Paint.Align.LEFT
        paint.isFakeBoldText =
            true

        canvas.drawText(
            "Hypothèses",
            outer.left +
                dp(7f),
            outer.centerY() -
                (
                    paint.ascent() +
                        paint.descent()
                    ) /
                    2f,
            paint
        )

        val available =
            max(
                dp(36f),
                outer.width() -
                    labelWidth -
                    gap *
                        (
                            entries.size +
                                1
                            )
            )

        val itemWidth =
            available /
                entries.size

        var left =
            outer.left +
                labelWidth +
                gap

        entries.forEach {
                entry ->

            val rect =
                RectF(
                    left,
                    outer.top +
                        dp(3f),
                    left +
                        itemWidth,
                    outer.bottom -
                        dp(3f)
                )

            hitRects[entry.id] =
                RectF(rect)

            paint.style =
                Paint.Style.FILL
            paint.color =
                Color.rgb(
                    entry.color.red,
                    entry.color.green,
                    entry.color.blue
                )

            canvas.drawRoundRect(
                rect,
                dp(7f),
                dp(7f),
                paint
            )

            paint.style =
                Paint.Style.STROKE
            paint.strokeWidth =
                if (entry.active) {
                    dp(3f)
                } else {
                    dp(1.2f)
                }
            paint.color =
                Color.rgb(
                    34,
                    38,
                    36
                )

            canvas.drawRoundRect(
                rect,
                dp(7f),
                dp(7f),
                paint
            )

            if (
                entry.state ==
                    HypothesisBranchState
                        .CONTRADICTION
            ) {
                paint.style =
                    Paint.Style.STROKE
                paint.strokeWidth =
                    dp(2.4f)
                paint.color =
                    Color.rgb(
                        150,
                        20,
                        20
                    )

                canvas.drawLine(
                    rect.left +
                        dp(5f),
                    rect.bottom -
                        dp(5f),
                    rect.right -
                        dp(5f),
                    rect.top +
                        dp(5f),
                    paint
                )
            }

            left =
                rect.right +
                    gap
        }

        paint.isFakeBoldText =
            false

        contentDescription =
            "Frise des hypothèses. " +
                entries.joinToString(
                    separator = ", "
                ) {
                    it.color.label +
                        if (
                            it.state ==
                                HypothesisBranchState
                                    .CONTRADICTION
                        ) {
                            " contradiction"
                        } else {
                            ""
                        }
                } +
                ". Touchez une étape pour revenir à cette branche."
    }

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {
        if (
            event.actionMasked ==
                MotionEvent.ACTION_UP
        ) {
            val selected =
                hitRects.entries
                    .firstOrNull {
                        it.value.contains(
                            event.x,
                            event.y
                        )
                    }
                    ?.key

            if (selected != null) {
                performClick()

                onEntrySelected
                    ?.invoke(
                        selected
                    )

                return true
            }
        }

        return true
    }

    override fun performClick():
        Boolean {
        super.performClick()
        return true
    }

    private fun dp(
        value: Float
    ): Float =
        value *
            resources
                .displayMetrics
                .density
}

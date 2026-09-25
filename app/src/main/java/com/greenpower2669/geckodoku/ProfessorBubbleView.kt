package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

class ProfessorBubbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val fillPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 252, 224)
            style = Paint.Style.FILL
        }

    private val strokePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(38, 88, 55)
            style = Paint.Style.STROKE
            strokeWidth = dp(2.2f)
        }

    private val titlePaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(26, 92, 52)
            textSize = sp(15f)
            isFakeBoldText = true
        }

    private val bodyPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(28, 28, 28)
            textSize = sp(16f)
        }

    private var message =
        ""

    fun showMessage(
        text: String
    ) {
        message = text
        visibility = VISIBLE
        requestLayout()
        invalidate()
    }

    fun hideMessage() {
        message = ""
        visibility = GONE
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        val width =
            MeasureSpec.getSize(
                widthMeasureSpec
            )

        val contentWidth =
            (width - dp(30f).toInt())
                .coerceAtLeast(
                    dp(120f).toInt()
                )

        val body =
            makeLayout(
                message,
                bodyPaint,
                contentWidth
            )

        val desired =
            dp(52f).toInt() +
                body.height +
                dp(18f).toInt()

        setMeasuredDimension(
            width,
            resolveSize(
                desired.coerceAtLeast(
                    dp(92f).toInt()
                ),
                heightMeasureSpec
            )
        )
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(canvas)

        if (message.isBlank()) {
            return
        }

        val margin =
            dp(8f)

        val tail =
            dp(14f)

        val rect =
            RectF(
                margin,
                margin,
                width - margin,
                height - margin - tail
            )

        canvas.drawRoundRect(
            rect,
            dp(18f),
            dp(18f),
            fillPaint
        )

        canvas.drawRoundRect(
            rect,
            dp(18f),
            dp(18f),
            strokePaint
        )

        val tailPath =
            Path().apply {
                moveTo(
                    rect.left +
                        dp(34f),
                    rect.bottom
                )
                lineTo(
                    rect.left +
                        dp(54f),
                    rect.bottom
                )
                lineTo(
                    rect.left +
                        dp(42f),
                    rect.bottom +
                        tail
                )
                close()
            }

        canvas.drawPath(
            tailPath,
            fillPaint
        )

        canvas.drawPath(
            tailPath,
            strokePaint
        )

        canvas.drawText(
            "🦎  Prof Gecko",
            rect.left + dp(14f),
            rect.top + dp(22f),
            titlePaint
        )

        val body =
            makeLayout(
                message,
                bodyPaint,
                (
                    rect.width() -
                        dp(28f)
                    ).toInt()
            )

        canvas.save()
        canvas.translate(
            rect.left + dp(14f),
            rect.top + dp(32f)
        )
        body.draw(canvas)
        canvas.restore()
    }

    private fun makeLayout(
        text: String,
        paint: TextPaint,
        width: Int
    ): StaticLayout =
        StaticLayout.Builder
            .obtain(
                text,
                0,
                text.length,
                paint,
                width.coerceAtLeast(1)
            )
            .setAlignment(
                Layout.Alignment.ALIGN_NORMAL
            )
            .setLineSpacing(
                0f,
                1.08f
            )
            .setIncludePad(true)
            .build()

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

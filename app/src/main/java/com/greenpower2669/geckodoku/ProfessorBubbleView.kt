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
import android.view.MotionEvent
import android.view.View

class ProfessorBubbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    var onClose: (() -> Unit)? = null

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

    private val closeFillPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(38, 88, 55)
            style = Paint.Style.FILL
        }

    private val closeTextPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = sp(23f)
            isFakeBoldText = true
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

    private val closeRect =
        RectF()

    private var message =
        ""

    init {
        isClickable = true
        elevation = dp(8f)
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    fun showMessage(
        text: String
    ) {
        message = text
        visibility = VISIBLE
        contentDescription =
            "Prof Gecko. " +
                text +
                ". Fermer la bulle avec la croix."
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
            (width - dp(44f).toInt())
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
            dp(58f).toInt() +
                body.height +
                dp(18f).toInt()

        setMeasuredDimension(
            width,
            resolveSize(
                desired.coerceAtLeast(
                    dp(96f).toInt()
                ),
                heightMeasureSpec
            )
        )
    }

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {
        if (
            event.action ==
                MotionEvent.ACTION_UP &&
            closeRect.contains(
                event.x,
                event.y
            )
        ) {
            performClick()
            onClose?.invoke()
            return true
        }

        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
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

        closeRect.set(
            rect.right - dp(46f),
            rect.top + dp(5f),
            rect.right - dp(6f),
            rect.top + dp(45f)
        )

        canvas.drawCircle(
            closeRect.centerX(),
            closeRect.centerY(),
            dp(17f),
            closeFillPaint
        )

        val closeY =
            closeRect.centerY() -
                (
                    closeTextPaint.ascent() +
                        closeTextPaint.descent()
                    ) / 2f

        canvas.drawText(
            "×",
            closeRect.centerX(),
            closeY,
            closeTextPaint
        )

        canvas.drawText(
            "Prof Gecko",
            rect.left + dp(14f),
            rect.top + dp(24f),
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
            rect.top + dp(38f)
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

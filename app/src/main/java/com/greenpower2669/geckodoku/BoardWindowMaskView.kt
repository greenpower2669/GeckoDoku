package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class BoardWindowMaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    var windowProvider:
        (() -> RectF?)? = null

    var cornerRadiusProvider:
        (() -> Float)? = null

    var maskColor: Int =
        Color.rgb(
            247,
            250,
            247
        )
        set(value) {
            field = value
            paint.color = value
            invalidate()
        }

    private val paint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {
            style =
                Paint.Style.FILL
            color =
                maskColor
        }

    private val path =
        Path()

    init {
        isClickable = false
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_NO
        setWillNotDraw(false)
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(canvas)

        val window =
            windowProvider
                ?.invoke()

        if (
            window == null ||
            width <= 0 ||
            height <= 0
        ) {
            canvas.drawColor(
                maskColor
            )
            return
        }

        val hole =
            RectF(
                window.left.coerceIn(
                    0f,
                    width.toFloat()
                ),
                window.top.coerceIn(
                    0f,
                    height.toFloat()
                ),
                window.right.coerceIn(
                    0f,
                    width.toFloat()
                ),
                window.bottom.coerceIn(
                    0f,
                    height.toFloat()
                )
            )

        if (
            hole.width() <= 0f ||
            hole.height() <= 0f
        ) {
            canvas.drawColor(
                maskColor
            )
            return
        }

        path.reset()
        path.fillType =
            Path.FillType.EVEN_ODD

        path.addRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            Path.Direction.CW
        )

        val radius =
            cornerRadiusProvider
                ?.invoke()
                ?.coerceAtLeast(0f)
                ?: 0f

        if (radius > 0f) {
            path.addRoundRect(
                hole,
                radius,
                radius,
                Path.Direction.CW
            )
        } else {
            path.addRect(
                hole,
                Path.Direction.CW
            )
        }

        canvas.drawPath(
            path,
            paint
        )
    }
}

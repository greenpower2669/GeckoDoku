package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View

class SudokuStyleSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    var onPreviewStyle:
        ((SudokuVisualStyle) -> Unit)? =
        null

    var onCommitStyle:
        ((SudokuVisualStyle) -> Unit)? =
        null

    private var committedStyle =
        SudokuVisualStyle
            .CLASSIC_NUMBERS

    private var previewStyle =
        committedStyle

    private var tracking = false

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        isClickable = true
        isFocusable = true
        minimumHeight = dp(64f).toInt()
        contentDescription =
            "Style Sudoku à trois positions : classique, gecko noir et blanc, gecko couleur."
    }

    fun setCommittedStyle(
        style: SudokuVisualStyle
    ) {
        committedStyle = style
        previewStyle = style
        invalidate()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {
        when (
            event.actionMasked
        ) {
            MotionEvent.ACTION_DOWN -> {
                tracking = true
                updatePreview(
                    event.x
                )
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (tracking) {
                    updatePreview(
                        event.x
                    )
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (tracking) {
                    updatePreview(
                        event.x
                    )
                    committedStyle =
                        previewStyle
                    onCommitStyle
                        ?.invoke(
                            committedStyle
                        )
                    tracking = false
                    performClick()
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                tracking = false
                previewStyle =
                    committedStyle
                onPreviewStyle
                    ?.invoke(
                        committedStyle
                    )
                invalidate()
                return true
            }
        }

        return false
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(canvas)

        val labels =
            arrayOf(
                "123",
                "Gecko N/B",
                "Gecko couleur"
            )

        val active =
            SudokuVisualStylePolicy
                .slotForStyle(
                    previewStyle
                )

        val slotWidth =
            width / 3f

        for (slot in 0..2) {
            val centerX =
                slotWidth *
                    (
                        slot +
                            .5f
                        )

            val selected =
                slot == active

            val horizontal =
                slotWidth *
                    if (selected) {
                        .44f
                    } else {
                        .34f
                    }

            val vertical =
                height *
                    if (selected) {
                        .42f
                    } else {
                        .31f
                    }

            val rect =
                RectF(
                    centerX - horizontal,
                    height / 2f -
                        vertical,
                    centerX + horizontal,
                    height / 2f +
                        vertical
                )

            paint.style =
                Paint.Style.FILL

            paint.color =
                if (selected) {
                    Color.rgb(
                        222,
                        241,
                        224
                    )
                } else {
                    Color.rgb(
                        242,
                        244,
                        241
                    )
                }

            canvas.drawRoundRect(
                rect,
                dp(13f),
                dp(13f),
                paint
            )

            paint.style =
                Paint.Style.STROKE
            paint.strokeWidth =
                dp(
                    if (selected) {
                        2.4f
                    } else {
                        1f
                    }
                )
            paint.color =
                if (selected) {
                    Color.rgb(
                        38,
                        105,
                        58
                    )
                } else {
                    Color.rgb(
                        125,
                        132,
                        126
                    )
                }

            canvas.drawRoundRect(
                rect,
                dp(13f),
                dp(13f),
                paint
            )

            paint.style =
                Paint.Style.FILL
            paint.textAlign =
                Paint.Align.CENTER
            paint.isFakeBoldText =
                selected
            paint.textSize =
                dp(
                    if (selected) {
                        18f
                    } else {
                        12.5f
                    }
                )
            paint.color =
                Color.rgb(
                    32,
                    48,
                    36
                )

            val y =
                rect.centerY() -
                    (
                        paint.ascent() +
                            paint.descent()
                        ) / 2f

            canvas.drawText(
                labels[slot],
                centerX,
                y,
                paint
            )
        }

        paint.isFakeBoldText = false
    }

    private fun updatePreview(
        x: Float
    ) {
        val newSlot =
            (
                x /
                    width
                        .coerceAtLeast(1)
                    .toFloat() *
                    3f
                ).toInt()
                .coerceIn(
                    0,
                    2
                )

        val newStyle =
            SudokuVisualStylePolicy
                .styleForSlot(
                    newSlot
                )

        if (
            newStyle ==
                previewStyle
        ) {
            return
        }

        previewStyle = newStyle

        performHapticFeedback(
            HapticFeedbackConstants
                .CLOCK_TICK
        )

        onPreviewStyle
            ?.invoke(
                previewStyle
            )

        invalidate()
    }

    private fun dp(
        value: Float
    ): Float =
        value *
            resources
                .displayMetrics
                .density
}

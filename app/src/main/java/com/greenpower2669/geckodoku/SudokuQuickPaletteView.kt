package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SudokuQuickPaletteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    var visualStyle:
        SudokuVisualStyle =
        SudokuVisualStyle
            .CLASSIC_NUMBERS
        set(value) {
            field = value
            invalidate()
        }

    var onValueDigit:
        ((Int) -> Unit)? = null

    var onCandidateDigit:
        ((Int) -> Unit)? = null

    var onErase:
        (() -> Unit)? = null

    private var activeCandidates:
        Set<Int> = emptySet()

    private val layoutPolicy =
        SudokuPaletteLayoutPolicy()

    private val renderer =
        SudokuDigitRenderer(context)

    private val candidateVisualPolicy =
        SudokuCandidateVisualPolicy()

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        isClickable = true
        isFocusable = true
        contentDescription =
            "Palette Sudoku. Valeurs à gauche, candidats à droite, effacer en bas."
    }

    fun setActiveCandidates(
        candidates: Set<Int>
    ) {
        activeCandidates =
            candidates.filter {
                it in 1..9
            }.toSet()

        invalidate()
    }

    override fun performClick():
        Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {
        if (
            event.actionMasked !=
                MotionEvent.ACTION_UP
        ) {
            return event.actionMasked ==
                MotionEvent.ACTION_DOWN ||
                event.actionMasked ==
                    MotionEvent.ACTION_MOVE
        }

        when (
            val action =
                layoutPolicy
                    .actionAt(
                        x = event.x,
                        y = event.y,
                        width = width,
                        height = height
                    )
        ) {
            is SudokuPaletteAction
                .Value ->
                onValueDigit
                    ?.invoke(
                        action.digit
                    )

            is SudokuPaletteAction
                .Candidate ->
                onCandidateDigit
                    ?.invoke(
                        action.digit
                    )

            SudokuPaletteAction
                .Erase ->
                onErase?.invoke()

            null -> Unit
        }

        performClick()
        return true
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(canvas)

        canvas.drawColor(
            Color.rgb(
                252,
                252,
                248
            )
        )

        drawHeaders(canvas)

        for (digit in 1..9) {
            drawTile(
                canvas,
                digit,
                candidate = false
            )

            drawTile(
                canvas,
                digit,
                candidate = true
            )
        }

        drawErase(canvas)
    }

    private fun drawHeaders(
        canvas: Canvas
    ) {
        paint.style =
            Paint.Style.FILL
        paint.textAlign =
            Paint.Align.CENTER
        paint.isFakeBoldText = true
        paint.textSize =
            height * .07f
        paint.color =
            Color.rgb(
                42,
                55,
                45
            )

        canvas.drawText(
            "Valeur",
            width * .25f,
            height * .09f,
            paint
        )

        canvas.drawText(
            "Candidats",
            width * .75f,
            height * .09f,
            paint
        )

        paint.isFakeBoldText = false
    }

    private fun drawTile(
        canvas: Canvas,
        digit: Int,
        candidate: Boolean
    ) {
        val b =
            layoutPolicy.tileBounds(
                digit = digit,
                candidate =
                    candidate,
                width = width,
                height = height
            )

        val rect =
            RectF(
                b.left.toFloat(),
                b.top.toFloat(),
                b.right.toFloat(),
                b.bottom.toFloat()
            )

        val inset =
            rect.width() * .06f

        val inner =
            RectF(
                rect.left + inset,
                rect.top + inset,
                rect.right - inset,
                rect.bottom - inset
            )

        paint.style =
            Paint.Style.FILL

        paint.color =
            if (
                candidate &&
                digit in
                    activeCandidates
            ) {
                Color.rgb(
                    218,
                    240,
                    220
                )
            } else {
                Color.rgb(
                    242,
                    245,
                    241
                )
            }

        canvas.drawRoundRect(
            inner,
            inner.width() * .12f,
            inner.width() * .12f,
            paint
        )

        renderer.draw(
            canvas = canvas,
            target =
                RectF(
                    inner.left +
                        inner.width() * .12f,
                    inner.top +
                        inner.height() * .12f,
                    inner.right -
                        inner.width() * .12f,
                    inner.bottom -
                        inner.height() * .12f
                ),
            digit = digit,
            style =
                if (candidate) {
                    candidateVisualPolicy
                        .styleForCandidate(
                            visualStyle
                        )
                } else {
                    visualStyle
                },
            given = false,
            mini = candidate
        )
    }

    private fun drawErase(
        canvas: Canvas
    ) {
        val top =
            layoutPolicy
                .footerTop(height)
                .toFloat()

        paint.style =
            Paint.Style.FILL
        paint.color =
            Color.rgb(
                232,
                235,
                232
            )

        canvas.drawRect(
            0f,
            top,
            width.toFloat(),
            height.toFloat(),
            paint
        )

        paint.textAlign =
            Paint.Align.CENTER
        paint.textSize =
            (height - top) *
                .40f
        paint.color =
            Color.rgb(
                42,
                55,
                45
            )

        val centerY =
            (
                top +
                    height
                ) / 2f

        val y =
            centerY -
                (
                    paint.ascent() +
                        paint.descent()
                    ) / 2f

        canvas.drawText(
            "⌫ Effacer",
            width / 2f,
            y,
            paint
        )
    }
}

package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class SudokuDigitRenderer(
    context: Context
) {
    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val nbSheet: Bitmap? =
        loadBitmap(
            context,
            AssetMediaCatalog
                .GECKO_NUMBER_NB
        )

    private val coloredSheet: Bitmap? =
        loadBitmap(
            context,
            AssetMediaCatalog
                .GECKO_NUMBER_COLORED
        )

    fun draw(
        canvas: Canvas,
        target: RectF,
        digit: Int,
        style: SudokuVisualStyle,
        given: Boolean = false,
        mini: Boolean = false,
        alpha: Int = 255
    ) {
        require(digit in 1..9)

        if (mini) {
            drawClassic(
                canvas,
                target,
                digit,
                given = false,
                mini = true,
                alpha = alpha
            )
            return
        }

        when (style) {
            SudokuVisualStyle
                .CLASSIC_NUMBERS ->
                drawClassic(
                    canvas,
                    target,
                    digit,
                    given,
                    mini,
                    alpha
                )

            SudokuVisualStyle
                .GECKO_NB ->
                drawGecko(
                    canvas,
                    target,
                    digit,
                    nbSheet,
                    colored = false,
                    given = given,
                    mini = mini,
                    alpha = alpha
                )

            SudokuVisualStyle
                .GECKO_COLORED ->
                drawGecko(
                    canvas,
                    target,
                    digit,
                    coloredSheet,
                    colored = true,
                    given = given,
                    mini = mini,
                    alpha = alpha
                )
        }
    }

    private fun drawClassic(
        canvas: Canvas,
        target: RectF,
        digit: Int,
        given: Boolean,
        mini: Boolean,
        alpha: Int
    ) {
        paint.style =
            Paint.Style.FILL
        paint.textAlign =
            Paint.Align.CENTER
        paint.textSize =
            target.height() *
                if (mini) {
                    .66f
                } else {
                    .62f
                }
        paint.isFakeBoldText =
            given && !mini
        paint.color =
            if (mini) {
                Color.BLACK
            } else if (given) {
                Color.rgb(
                    30,
                    42,
                    34
                )
            } else {
                Color.rgb(
                    25,
                    95,
                    52
                )
            }
        paint.alpha =
            alpha.coerceIn(
                0,
                255
            )

        val y =
            target.centerY() -
                (
                    paint.ascent() +
                        paint.descent()
                    ) / 2f

        canvas.drawText(
            digit.toString(),
            target.centerX(),
            y,
            paint
        )

        paint.alpha = 255
        paint.isFakeBoldText = false
    }

    private fun drawGecko(
        canvas: Canvas,
        target: RectF,
        digit: Int,
        sheet: Bitmap?,
        colored: Boolean,
        given: Boolean,
        mini: Boolean,
        alpha: Int
    ) {
        if (sheet == null) {
            drawClassic(
                canvas,
                target,
                digit,
                given,
                mini,
                alpha
            )
            return
        }

        val source =
            SudokuNumberSheetLayout
                .sourceRect(
                    width =
                        sheet.width,
                    height =
                        sheet.height,
                    digit = digit,
                    colored =
                        colored
                )

        val inset =
            target.width() *
                if (mini) {
                    .035f
                } else {
                    .07f
                }

        val drawTarget =
            RectF(
                target.left + inset,
                target.top + inset,
                target.right - inset,
                target.bottom - inset
            )

        paint.alpha =
            alpha.coerceIn(
                0,
                255
            )

        canvas.drawBitmap(
            sheet,
            source,
            drawTarget,
            paint
        )

        paint.alpha = 255

        if (
            given &&
            !mini
        ) {
            paint.style =
                Paint.Style.STROKE
            paint.strokeWidth =
                target.width() *
                    .025f
            paint.color =
                Color.rgb(
                    55,
                    85,
                    62
                )

            canvas.drawRoundRect(
                RectF(
                    target.left +
                        target.width() *
                        .035f,
                    target.top +
                        target.height() *
                        .035f,
                    target.right -
                        target.width() *
                        .035f,
                    target.bottom -
                        target.height() *
                        .035f
                ),
                target.width() *
                    .08f,
                target.height() *
                    .08f,
                paint
            )
        }
    }

    private fun loadBitmap(
        context: Context,
        path: String
    ): Bitmap? =
        try {
            context.assets
                .open(path)
                .use {
                    BitmapFactory
                        .decodeStream(it)
                }
        } catch (_: Exception) {
            null
        }
}

package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class SudokuValueOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    lateinit var snapshotProvider:
        () -> SudokuSnapshot

    var visualStyle:
        SudokuVisualStyle =
        SudokuVisualStyle
            .CLASSIC_NUMBERS
        set(value) {
            field = value
            invalidate()
        }

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val boardRect =
        RectF()

    private var cellSize = 1f

    private val nbSheet: Bitmap? =
        loadBitmap(
            AssetMediaCatalog
                .GECKO_NUMBER_NB
        )

    private val coloredSheet: Bitmap? =
        loadBitmap(
            AssetMediaCatalog
                .GECKO_NUMBER_COLORED
        )

    init {
        isClickable = false
        isFocusable = false
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_NO
        contentDescription = null
    }

    override fun onTouchEvent(
        event:
            android.view.MotionEvent
    ): Boolean = false

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        updateBoardRect(
            w,
            h
        )
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(canvas)

        if (
            !::snapshotProvider
                .isInitialized
        ) {
            return
        }

        if (boardRect.width() <= 0f) {
            updateBoardRect(
                width,
                height
            )
        }

        val state =
            snapshotProvider()

        for (row in 0..8) {
            for (col in 0..8) {
                val cell =
                    Cell(row, col)

                val digit =
                    state.valueAt(cell)

                if (digit == 0) {
                    continue
                }

                val rect =
                    cellRect(cell)

                when (visualStyle) {
                    SudokuVisualStyle
                        .CLASSIC_NUMBERS ->
                        drawClassicDigit(
                            canvas,
                            rect,
                            digit,
                            state.isGiven(
                                cell
                            )
                        )

                    SudokuVisualStyle
                        .GECKO_NB ->
                        drawGeckoDigit(
                            canvas,
                            rect,
                            digit,
                            nbSheet,
                            colored = false,
                            given =
                                state.isGiven(
                                    cell
                                )
                        )

                    SudokuVisualStyle
                        .GECKO_COLORED ->
                        drawGeckoDigit(
                            canvas,
                            rect,
                            digit,
                            coloredSheet,
                            colored = true,
                            given =
                                state.isGiven(
                                    cell
                                )
                        )
                }
            }
        }
    }

    private fun drawClassicDigit(
        canvas: Canvas,
        rect: RectF,
        digit: Int,
        given: Boolean
    ) {
        paint.style =
            Paint.Style.FILL
        paint.textAlign =
            Paint.Align.CENTER
        paint.textSize =
            cellSize * .58f
        paint.isFakeBoldText =
            given
        paint.color =
            if (given) {
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

        val y =
            rect.centerY() -
                (
                    paint.ascent() +
                        paint.descent()
                    ) / 2f

        canvas.drawText(
            digit.toString(),
            rect.centerX(),
            y,
            paint
        )

        paint.isFakeBoldText =
            false
    }

    private fun drawGeckoDigit(
        canvas: Canvas,
        rect: RectF,
        digit: Int,
        sheet: Bitmap?,
        colored: Boolean,
        given: Boolean
    ) {
        if (sheet == null) {
            drawClassicDigit(
                canvas,
                rect,
                digit,
                given
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
            cellSize * .07f

        val target =
            RectF(
                rect.left + inset,
                rect.top + inset,
                rect.right - inset,
                rect.bottom - inset
            )

        paint.alpha =
            if (given) {
                255
            } else {
                235
            }

        canvas.drawBitmap(
            sheet,
            source,
            target,
            paint
        )

        paint.alpha = 255

        if (given) {
            paint.style =
                Paint.Style.STROKE
            paint.strokeWidth =
                cellSize * .025f
            paint.color =
                Color.rgb(
                    55,
                    85,
                    62
                )

            canvas.drawRoundRect(
                RectF(
                    rect.left +
                        cellSize * .035f,
                    rect.top +
                        cellSize * .035f,
                    rect.right -
                        cellSize * .035f,
                    rect.bottom -
                        cellSize * .035f
                ),
                cellSize * .08f,
                cellSize * .08f,
                paint
            )
        }
    }

    private fun updateBoardRect(
        w: Int,
        h: Int
    ) {
        if (w <= 0 || h <= 0) {
            return
        }

        val margin = dp(4f)
        val side =
            min(
                w - margin * 2f,
                h - margin * 2f
            ).coerceAtLeast(
                dp(80f)
            )

        val left =
            (w - side) / 2f
        val top =
            (h - side) / 2f

        boardRect.set(
            left,
            top,
            left + side,
            top + side
        )

        cellSize =
            side /
                SudokuPuzzle.SIZE
    }

    private fun cellRect(
        cell: Cell
    ): RectF {
        val left =
            boardRect.left +
                cell.col * cellSize

        val top =
            boardRect.top +
                cell.row * cellSize

        return RectF(
            left,
            top,
            left + cellSize,
            top + cellSize
        )
    }

    private fun loadBitmap(
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

    private fun dp(
        value: Float
    ): Float =
        value *
            resources
                .displayMetrics
                .density
}

object SudokuNumberSheetLayout {
    fun sourceRect(
        width: Int,
        height: Int,
        digit: Int,
        colored: Boolean
    ): Rect {
        require(digit in 0..9)
        require(width > 0)
        require(height > 0)

        if (!colored) {
            val bounds =
                NB_BOUNDS[digit]

            val padX = .010f
            val padY = .012f

            return Rect(
                (
                    width *
                        (
                            bounds[0] -
                                padX
                            )
                    ).toInt()
                    .coerceIn(
                        0,
                        width - 1
                    ),
                (
                    height *
                        (
                            bounds[1] -
                                padY
                            )
                    ).toInt()
                    .coerceIn(
                        0,
                        height - 1
                    ),
                (
                    width *
                        (
                            bounds[2] +
                                padX
                            )
                    ).toInt()
                    .coerceIn(
                        1,
                        width
                    ),
                (
                    height *
                        (
                            bounds[3] +
                                padY
                            )
                    ).toInt()
                    .coerceIn(
                        1,
                        height
                    )
            )
        }

        val col =
            digit % 5

        val row =
            digit / 5

        val cardWidth =
            width / 5f

        val usableHeight =
            height * .88f

        val rowHeight =
            usableHeight / 2f

        val left =
            col * cardWidth

        val top =
            row * rowHeight

        val horizontalInset =
            cardWidth * .05f

        val topInset =
            rowHeight * .025f

        val bottomInset =
            rowHeight * .25f

        return Rect(
            (
                left +
                    horizontalInset
                ).toInt()
                .coerceIn(
                    0,
                    width - 1
                ),
            (
                top +
                    topInset
                ).toInt()
                .coerceIn(
                    0,
                    height - 1
                ),
            (
                left +
                    cardWidth -
                    horizontalInset
                ).toInt()
                .coerceIn(
                    1,
                    width
                ),
            (
                top +
                    rowHeight -
                    bottomInset
                ).toInt()
                .coerceIn(
                    1,
                    height
                )
        )
    }

    private val NB_BOUNDS =
        arrayOf(
            floatArrayOf(.0260f, .0576f, .2012f, .4385f),
            floatArrayOf(.2441f, .0566f, .3535f, .4541f),
            floatArrayOf(.3939f, .0635f, .5677f, .4531f),
            floatArrayOf(.5990f, .0596f, .7565f, .4521f),
            floatArrayOf(.7799f, .0449f, .9564f, .4629f),
            floatArrayOf(.0299f, .5117f, .1908f, .9189f),
            floatArrayOf(.2233f, .5215f, .3874f, .9121f),
            floatArrayOf(.4043f, .5195f, .5710f, .9307f),
            floatArrayOf(.5924f, .5156f, .7682f, .9189f),
            floatArrayOf(.8125f, .5264f, .9622f, .9141f)
        )
    }
}

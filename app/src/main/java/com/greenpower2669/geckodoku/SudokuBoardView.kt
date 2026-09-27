package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

class SudokuBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    lateinit var snapshotProvider:
        () -> SudokuSnapshot

    var onCellSelected:
        ((Cell) -> Unit)? = null

    var selectedCell: Cell? = null
        private set

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val boardRect =
        RectF()

    private var cellSize = 1f

    init {
        isClickable = true
        isFocusable = true
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription =
            "Grille Sudoku 9 par 9. Touchez une case puis choisissez un chiffre."
    }

    fun setSelectedCell(
        cell: Cell?
    ) {
        selectedCell = cell
        invalidate()
    }

    fun refresh() {
        invalidate()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        updateBoardRect(w, h)
    }

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {
        if (
            event.actionMasked ==
                MotionEvent.ACTION_UP
        ) {
            val cell =
                cellAt(
                    event.x,
                    event.y
                )

            if (cell != null) {
                selectedCell = cell
                onCellSelected?.invoke(cell)
                performClick()
                invalidate()
                return true
            }
        }

        return event.actionMasked ==
            MotionEvent.ACTION_DOWN ||
            event.actionMasked ==
                MotionEvent.ACTION_MOVE
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

        drawBackgrounds(
            canvas,
            state
        )
        drawGrid(canvas)
        drawNotes(
            canvas,
            state
        )
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

    private fun drawBackgrounds(
        canvas: Canvas,
        state: SudokuSnapshot
    ) {
        for (row in 0..8) {
            for (col in 0..8) {
                val cell =
                    Cell(row, col)

                val rect =
                    cellRect(cell)

                paint.style =
                    Paint.Style.FILL

                paint.color =
                    when {
                        selectedCell == cell ->
                            Color.rgb(
                                219,
                                238,
                                224
                            )

                        state.isGiven(cell) ->
                            Color.rgb(
                                238,
                                241,
                                238
                            )

                        (
                            row / 3 +
                                col / 3
                            ) % 2 == 0 ->
                            Color.rgb(
                                252,
                                252,
                                248
                            )

                        else ->
                            Color.rgb(
                                246,
                                249,
                                246
                            )
                    }

                canvas.drawRect(
                    rect,
                    paint
                )
            }
        }
    }

    private fun drawGrid(
        canvas: Canvas
    ) {
        paint.style =
            Paint.Style.STROKE
        paint.strokeCap =
            Paint.Cap.SQUARE
        paint.color =
            Color.rgb(
                55,
                65,
                58
            )

        for (i in 0..9) {
            paint.strokeWidth =
                dp(
                    if (
                        i % 3 == 0
                    ) {
                        2.8f
                    } else {
                        0.8f
                    }
                )

            val x =
                boardRect.left +
                    i * cellSize

            val y =
                boardRect.top +
                    i * cellSize

            canvas.drawLine(
                x,
                boardRect.top,
                x,
                boardRect.bottom,
                paint
            )

            canvas.drawLine(
                boardRect.left,
                y,
                boardRect.right,
                y,
                paint
            )
        }
    }

    private fun drawNotes(
        canvas: Canvas,
        state: SudokuSnapshot
    ) {
        paint.style =
            Paint.Style.FILL
        paint.textAlign =
            Paint.Align.CENTER
        paint.color =
            Color.rgb(
                70,
                80,
                72
            )
        paint.textSize =
            cellSize * .19f
        paint.isFakeBoldText = false

        for (row in 0..8) {
            for (col in 0..8) {
                val cell =
                    Cell(row, col)

                if (
                    state.valueAt(cell) != 0
                ) {
                    continue
                }

                val notes =
                    state.notesAt(cell)

                if (notes.isEmpty()) {
                    continue
                }

                val rect =
                    cellRect(cell)

                for (digit in notes) {
                    val zero =
                        digit - 1

                    val noteRow =
                        zero / 3

                    val noteCol =
                        zero % 3

                    val x =
                        rect.left +
                            cellSize *
                            (
                                noteCol +
                                    .5f
                                ) / 3f

                    val centerY =
                        rect.top +
                            cellSize *
                            (
                                noteRow +
                                    .5f
                                ) / 3f

                    val y =
                        centerY -
                            (
                                paint.ascent() +
                                    paint.descent()
                                ) / 2f

                    canvas.drawText(
                        digit.toString(),
                        x,
                        y,
                        paint
                    )
                }
            }
        }
    }

    fun cellRectLocal(
        cell: Cell
    ): RectF =
        cellRect(cell)

    private fun cellAt(
        x: Float,
        y: Float
    ): Cell? {
        if (
            !boardRect.contains(
                x,
                y
            )
        ) {
            return null
        }

        val col =
            (
                (
                    x -
                        boardRect.left
                    ) /
                    cellSize
                ).toInt()
                .coerceIn(0, 8)

        val row =
            (
                (
                    y -
                        boardRect.top
                    ) /
                    cellSize
                ).toInt()
                .coerceIn(0, 8)

        return Cell(
            row,
            col
        )
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

    private fun dp(
        value: Float
    ): Float =
        value *
            resources
                .displayMetrics
                .density
}

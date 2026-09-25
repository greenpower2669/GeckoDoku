package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

class GeckoBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    lateinit var puzzle: Puzzle
    lateinit var snapshotProvider: () -> GameSnapshot

    var onSingleTapCell: ((Cell) -> Unit)? = null
    var onDoubleTapCell: ((Cell) -> Unit)? = null
    var onLongPressCell: ((Cell) -> Unit)? = null
    var onLongPressOutside: (() -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val boardRect = RectF()
    private var cellSize = 1f
    private val gutter = dp(76f)

    private var professorSources: Set<Cell> = emptySet()
    private var professorTargets: Set<Cell> = emptySet()
    private var professorGhosts: Set<Cell> = emptySet()
    private var professorLevel = 0

    private val regionColors = intArrayOf(
        Color.rgb(232, 248, 232),
        Color.rgb(222, 239, 255),
        Color.rgb(255, 241, 204),
        Color.rgb(244, 225, 255),
        Color.rgb(255, 222, 226),
        Color.rgb(218, 247, 242),
        Color.rgb(255, 229, 204),
        Color.rgb(225, 229, 255),
        Color.rgb(237, 246, 205),
        Color.rgb(255, 218, 242),
        Color.rgb(216, 245, 255),
        Color.rgb(239, 225, 207)
    )

    private val gestures =
        GestureDetector(
            context,
            object :
                GestureDetector.SimpleOnGestureListener() {

                override fun onDown(
                    e: MotionEvent
                ): Boolean = true

                override fun onSingleTapConfirmed(
                    e: MotionEvent
                ): Boolean {
                    val cell =
                        cellAt(e.x, e.y)
                            ?: return false

                    onSingleTapCell?.invoke(cell)
                    performClick()
                    return true
                }

                override fun onDoubleTap(
                    e: MotionEvent
                ): Boolean {
                    val cell =
                        cellAt(e.x, e.y)
                            ?: return false

                    onDoubleTapCell?.invoke(cell)
                    performClick()
                    return true
                }

                override fun onLongPress(
                    e: MotionEvent
                ) {
                    val cell =
                        cellAt(e.x, e.y)

                    if (cell != null) {
                        onLongPressCell?.invoke(cell)
                    } else {
                        onLongPressOutside?.invoke()
                    }
                }
            }
        )

    init {
        isFocusable = true
        isClickable = true
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_YES

        contentDescription =
            "Grille GeckoDoku. Clic simple pour une croix, vrai double-clic pour un gecko, appui long pour une hypothèse."
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun setPuzzleAndRefresh(
        newPuzzle: Puzzle
    ) {
        puzzle = newPuzzle
        clearProfessorHint()
        requestLayout()
        invalidate()
    }

    fun showProfessorHint(
        step: SolveStep,
        level: Int = 3
    ) {
        professorLevel =
            level.coerceIn(1, 3)

        professorSources =
            step.sourceCells

        professorTargets =
            if (professorLevel >= 3) {
                step.actionCells +
                    listOfNotNull(
                        step.hypothesisRejected
                    )
            } else {
                emptySet()
            }

        professorGhosts =
            emptySet()

        invalidate()
    }

    fun showProfessorHypothesis(
        step: SolveStep
    ) {
        professorLevel = 2
        professorSources =
            step.sourceCells
        professorTargets =
            emptySet()
        professorGhosts =
            step.sourceCells
        invalidate()
    }

    fun clearProfessorHint() {
        professorLevel = 0
        professorSources = emptySet()
        professorTargets = emptySet()
        professorGhosts = emptySet()
        invalidate()
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        val w =
            MeasureSpec.getSize(
                widthMeasureSpec
            )

        setMeasuredDimension(
            w,
            resolveSize(
                w + gutter.toInt(),
                heightMeasureSpec
            )
        )
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        updateBoardRect(w, h)
    }

    private fun updateBoardRect(
        w: Int = width,
        h: Int = height
    ) {
        if (w <= 0 || h <= 0) return

        val horizontal = dp(6f)

        val side =
            min(
                w - horizontal * 2,
                h - gutter - dp(4f)
            ).coerceAtLeast(dp(80f))

        val left =
            (w - side) / 2f

        boardRect.set(
            left,
            dp(4f),
            left + side,
            dp(4f) + side
        )

        if (::puzzle.isInitialized) {
            cellSize =
                side / puzzle.size
        }
    }

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean =
        gestures.onTouchEvent(event)

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(canvas)

        if (!::puzzle.isInitialized ||
            !::snapshotProvider.isInitialized
        ) {
            return
        }

        if (boardRect.width() <= 0f) {
            updateBoardRect()
        }

        cellSize =
            boardRect.width() /
                puzzle.size

        val state =
            snapshotProvider()

        drawRegions(canvas)
        drawGrid(canvas)
        drawMarks(canvas, state)
        drawProfessorGhosts(canvas, state)
        drawProfessorOverlay(canvas)
        drawGutterHint(canvas)

        if (state.hypotheses.values.any {
                it ==
                    HypothesisMark.ALERT_GECKO
            }
        ) {
            postInvalidateDelayed(450)
        }
    }

    private fun drawRegions(
        canvas: Canvas
    ) {
        for (r in 0 until puzzle.size) {
            for (c in 0 until puzzle.size) {
                val cell = Cell(r, c)

                paint.style =
                    Paint.Style.FILL

                paint.color =
                    regionColors[
                        puzzle.regionAt(cell) %
                            regionColors.size
                    ]

                canvas.drawRect(
                    cellRect(cell),
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

        for (i in 0..puzzle.size) {
            paint.color =
                Color.rgb(70, 70, 70)

            paint.strokeWidth =
                dp(
                    if (i == 0 ||
                        i == puzzle.size
                    ) {
                        3.0f
                    } else {
                        1.0f
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

        paint.color = Color.BLACK

        paint.strokeWidth =
            dp(
                if (puzzle.size >= 10) {
                    3.2f
                } else {
                    4.0f
                }
            )

        for (r in 0 until puzzle.size) {
            for (c in 0 until puzzle.size) {
                val here =
                    puzzle.regionAt(
                        Cell(r, c)
                    )

                val rect =
                    cellRect(
                        Cell(r, c)
                    )

                if (c + 1 < puzzle.size &&
                    here !=
                    puzzle.regionAt(
                        Cell(r, c + 1)
                    )
                ) {
                    canvas.drawLine(
                        rect.right,
                        rect.top,
                        rect.right,
                        rect.bottom,
                        paint
                    )
                }

                if (r + 1 < puzzle.size &&
                    here !=
                    puzzle.regionAt(
                        Cell(r + 1, c)
                    )
                ) {
                    canvas.drawLine(
                        rect.left,
                        rect.bottom,
                        rect.right,
                        rect.bottom,
                        paint
                    )
                }
            }
        }
    }

    private fun drawMarks(
        canvas: Canvas,
        state: GameSnapshot
    ) {
        for (r in 0 until puzzle.size) {
            for (c in 0 until puzzle.size) {
                val cell = Cell(r, c)
                val rect = cellRect(cell)

                when {
                    state.givens.contains(cell) -> {
                        drawGecko(
                            canvas,
                            rect,
                            1f,
                            false
                        )
                        drawGivenRing(
                            canvas,
                            rect
                        )
                    }

                    state.confirmed.contains(cell) ->
                        drawGecko(
                            canvas,
                            rect,
                            1f,
                            false
                        )

                    state.hypotheses[cell] ==
                        HypothesisMark.ALERT_GECKO -> {
                        val visible =
                            (
                                SystemClock
                                    .uptimeMillis() /
                                    450L
                                ) % 2L == 0L

                        drawGecko(
                            canvas,
                            rect,
                            if (visible) {
                                1f
                            } else {
                                .20f
                            },
                            true
                        )
                    }

                    state.hypotheses[cell] ==
                        HypothesisMark.GHOST_GECKO ->
                        drawGecko(
                            canvas,
                            rect,
                            .18f,
                            false
                        )

                    state.manualCrosses
                        .contains(cell) ->
                        drawCross(
                            canvas,
                            rect,
                            1f
                        )

                    state.autoCrosses
                        .contains(cell) ->
                        drawCross(
                            canvas,
                            rect,
                            .30f
                        )
                }

                state.customMarkers[cell]
                    ?.let {
                        drawCustomMarker(
                            canvas,
                            rect,
                            it
                        )
                    }
            }
        }
    }

    private fun drawProfessorGhosts(
        canvas: Canvas,
        state: GameSnapshot
    ) {
        for (cell in professorGhosts) {
            if (state.confirmed.contains(cell) ||
                state.givens.contains(cell) ||
                state.manualCrosses.contains(cell) ||
                state.autoCrosses.contains(cell)
            ) {
                continue
            }

            drawGecko(
                canvas,
                cellRect(cell),
                .24f,
                false
            )
        }
    }

    private fun drawProfessorOverlay(
        canvas: Canvas
    ) {
        if (professorLevel <= 0) {
            return
        }

        paint.style =
            Paint.Style.STROKE

        paint.strokeCap =
            Paint.Cap.ROUND

        paint.strokeWidth =
            cellSize * .065f

        paint.color =
            Color.rgb(
                184,
                118,
                0
            )

        for (cell in professorSources) {
            val rect =
                cellRect(cell)

            val inset =
                cellSize * .08f

            canvas.drawRoundRect(
                RectF(
                    rect.left + inset,
                    rect.top + inset,
                    rect.right - inset,
                    rect.bottom - inset
                ),
                cellSize * .10f,
                cellSize * .10f,
                paint
            )
        }

        if (professorLevel >= 3) {
            paint.strokeWidth =
                cellSize * .085f

            paint.color =
                Color.rgb(
                    185,
                    28,
                    28
                )

            for (cell in professorTargets) {
                val rect =
                    cellRect(cell)

                val inset =
                    cellSize * .035f

                canvas.drawRoundRect(
                    RectF(
                        rect.left + inset,
                        rect.top + inset,
                        rect.right - inset,
                        rect.bottom - inset
                    ),
                    cellSize * .08f,
                    cellSize * .08f,
                    paint
                )
            }
        }
    }

    private fun drawGivenRing(
        canvas: Canvas,
        rect: RectF
    ) {
        paint.style =
            Paint.Style.STROKE

        paint.strokeWidth =
            cellSize * .045f

        paint.color =
            Color.rgb(
                16,
                78,
                44
            )

        canvas.drawCircle(
            rect.centerX(),
            rect.centerY(),
            cellSize * .34f,
            paint
        )
    }

    private fun drawCross(
        canvas: Canvas,
        rect: RectF,
        alpha: Float
    ) {
        paint.style =
            Paint.Style.STROKE

        paint.strokeWidth =
            cellSize * .075f

        paint.strokeCap =
            Paint.Cap.ROUND

        paint.color =
            Color.argb(
                (255 * alpha)
                    .toInt(),
                45,
                45,
                45
            )

        val m =
            cellSize * .29f

        canvas.drawLine(
            rect.left + m,
            rect.top + m,
            rect.right - m,
            rect.bottom - m,
            paint
        )

        canvas.drawLine(
            rect.right - m,
            rect.top + m,
            rect.left + m,
            rect.bottom - m,
            paint
        )
    }

    private fun drawGecko(
        canvas: Canvas,
        rect: RectF,
        alpha: Float,
        alert: Boolean
    ) {
        val cx = rect.centerX()
        val cy = rect.centerY()

        val a =
            (255 * alpha)
                .toInt()
                .coerceIn(
                    0,
                    255
                )

        paint.style =
            Paint.Style.FILL

        paint.color =
            Color.argb(
                a,
                31,
                128,
                72
            )

        canvas.drawOval(
            RectF(
                cx-cellSize*.13f,
                cy-cellSize*.18f,
                cx+cellSize*.13f,
                cy+cellSize*.20f
            ),
            paint
        )

        canvas.drawCircle(
            cx,
            cy-cellSize*.21f,
            cellSize*.105f,
            paint
        )

        paint.style =
            Paint.Style.STROKE

        paint.strokeWidth =
            cellSize * .055f

        paint.strokeCap =
            Paint.Cap.ROUND

        val leg =
            cellSize * .20f

        canvas.drawLine(
            cx-cellSize*.08f,
            cy-cellSize*.05f,
            cx-leg,
            cy-cellSize*.15f,
            paint
        )

        canvas.drawLine(
            cx+cellSize*.08f,
            cy-cellSize*.05f,
            cx+leg,
            cy-cellSize*.15f,
            paint
        )

        canvas.drawLine(
            cx-cellSize*.08f,
            cy+cellSize*.10f,
            cx-leg,
            cy+cellSize*.20f,
            paint
        )

        canvas.drawLine(
            cx+cellSize*.08f,
            cy+cellSize*.10f,
            cx+leg,
            cy+cellSize*.20f,
            paint
        )

        val tail = Path()

        tail.moveTo(
            cx,
            cy + cellSize*.18f
        )

        tail.cubicTo(
            cx+cellSize*.02f,
            cy+cellSize*.32f,
            cx+cellSize*.22f,
            cy+cellSize*.26f,
            cx+cellSize*.20f,
            cy+cellSize*.36f
        )

        canvas.drawPath(
            tail,
            paint
        )

        paint.style =
            Paint.Style.FILL

        paint.color =
            Color.argb(
                a,
                255,
                255,
                255
            )

        canvas.drawCircle(
            cx-cellSize*.04f,
            cy-cellSize*.225f,
            cellSize*.025f,
            paint
        )

        canvas.drawCircle(
            cx+cellSize*.04f,
            cy-cellSize*.225f,
            cellSize*.025f,
            paint
        )

        if (alert) {
            paint.style =
                Paint.Style.STROKE

            paint.strokeWidth =
                cellSize*.045f

            paint.color =
                Color.argb(
                    a,
                    170,
                    20,
                    20
                )

            canvas.drawCircle(
                cx,
                cy,
                cellSize*.31f,
                paint
            )
        }
    }

    private fun drawCustomMarker(
        canvas: Canvas,
        rect: RectF,
        marker: CustomMarker
    ) {
        if (marker ==
            CustomMarker.RAINBOW_GECKO
        ) {
            val colors =
                intArrayOf(
                    Color.RED,
                    Color.rgb(
                        255,
                        140,
                        0
                    ),
                    Color.YELLOW,
                    Color.GREEN,
                    Color.BLUE,
                    Color.MAGENTA
                )

            paint.style =
                Paint.Style.STROKE

            paint.strokeWidth =
                cellSize*.025f

            colors.forEachIndexed {
                    i,
                    color ->

                paint.color = color

                canvas.drawCircle(
                    rect.centerX(),
                    rect.centerY(),
                    cellSize*
                        (.34f-i*.025f),
                    paint
                )
            }

            drawGecko(
                canvas,
                rect,
                .85f,
                false
            )

            return
        }

        paint.style =
            Paint.Style.FILL

        paint.textAlign =
            Paint.Align.CENTER

        paint.textSize =
            cellSize * .34f

        paint.isFakeBoldText = true

        paint.color =
            Color.rgb(
                20,
                45,
                90
            )

        val y =
            rect.top +
                cellSize*.42f -
                (
                    paint.ascent() +
                        paint.descent()
                    ) / 2f

        canvas.drawText(
            marker.symbol,
            rect.centerX(),
            y,
            paint
        )

        paint.isFakeBoldText = false
    }

    private fun drawGutterHint(
        canvas: Canvas
    ) {
        paint.style =
            Paint.Style.FILL

        paint.color =
            Color.rgb(
                55,
                55,
                55
            )

        paint.textAlign =
            Paint.Align.CENTER

        paint.textSize =
            dp(13f)

        canvas.drawText(
            "Simple = ✕   •   Double = 🦎   •   Long = hypothèse",
            width/2f,
            boardRect.bottom +
                dp(32f),
            paint
        )
    }

    private fun cellAt(
        x: Float,
        y: Float
    ): Cell? {
        if (!boardRect.contains(x, y)) {
            return null
        }

        val col =
            (
                (x-boardRect.left) /
                    cellSize
                )
                .toInt()
                .coerceIn(
                    0,
                    puzzle.size-1
                )

        val row =
            (
                (y-boardRect.top) /
                    cellSize
                )
                .toInt()
                .coerceIn(
                    0,
                    puzzle.size-1
                )

        return Cell(row, col)
    }

    private fun cellRect(
        cell: Cell
    ): RectF {
        val left =
            boardRect.left +
                cell.col*cellSize

        val top =
            boardRect.top +
                cell.row*cellSize

        return RectF(
            left,
            top,
            left+cellSize,
            top+cellSize
        )
    }

    private fun dp(v: Float): Float =
        v *
            resources
                .displayMetrics
                .density
}

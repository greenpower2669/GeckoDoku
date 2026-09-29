package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.min
import kotlin.math.sin

class SudokuValueOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    lateinit var snapshotProvider:
        () -> SudokuSnapshot

    var animateGeckoMarkers:
        Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var visualStyle:
        SudokuVisualStyle =
        SudokuVisualStyle
            .CLASSIC_NUMBERS
        set(value) {
            field = value
            invalidate()
        }

    private val renderer =
        SudokuDigitRenderer(
            context
        )

    private val candidateVisualPolicy =
        SudokuCandidateVisualPolicy()

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val geckoMarkerBitmap:
        Bitmap? =
        try {
            context.assets
                .open(
                    AssetMediaCatalog
                        .GECKO_PORTRAIT
                )
                .use {
                    BitmapFactory
                        .decodeStream(it)
                }
        } catch (_: Exception) {
            null
        }

    private val mediaSuppressedGeckos =
        linkedSetOf<Cell>()

    private val boardRect =
        RectF()

    private var cellSize = 1f

    private var professorCell:
        Cell? = null

    private var professorCandidates:
        Set<Int> = emptySet()

    private var professorFocusDigit:
        Int? = null

    private var reasoningTrace:
        SudokuReasoningTrace? = null

    private var reasoningStepIndex =
        -1

    init {
        isClickable = false
        isFocusable = false
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_NO
        contentDescription = null
    }

    fun showProfessorCandidates(
        cell: Cell,
        candidates: Set<Int>,
        focusDigit: Int?
    ) {
        professorCell = cell
        professorCandidates =
            candidates.filter {
                it in 1..9
            }.toSet()

        professorFocusDigit =
            focusDigit
                ?.takeIf {
                    it in 1..9
                }

        invalidate()
    }

    fun clearProfessorCandidates() {
        professorCell = null
        professorCandidates =
            emptySet()
        professorFocusDigit = null
        invalidate()
    }

    fun showReasoningStep(
        trace: SudokuReasoningTrace,
        stepIndex: Int
    ) {
        reasoningTrace = trace
        reasoningStepIndex =
            stepIndex.coerceIn(
                0,
                trace.steps.lastIndex
            )
        invalidate()
    }

    fun clearReasoning() {
        reasoningTrace = null
        reasoningStepIndex = -1
        invalidate()
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

    fun setMediaGeckoSuppressed(
        cell: Cell,
        suppressed: Boolean
    ) {
        if (suppressed) {
            mediaSuppressedGeckos.add(
                cell
            )
        } else {
            mediaSuppressedGeckos.remove(
                cell
            )
        }

        invalidate()
    }

    fun clearMediaGeckoSuppression() {
        if (
            mediaSuppressedGeckos
                .isEmpty()
        ) {
            return
        }

        mediaSuppressedGeckos.clear()
        invalidate()
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

        if (
            boardRect.width() <=
                0f
        ) {
            updateBoardRect(
                width,
                height
            )
        }

        val state =
            snapshotProvider()

        var animateMarker =
            false

        for (row in 0..8) {
            for (col in 0..8) {
                val cell =
                    Cell(
                        row,
                        col
                    )

                val rect =
                    cellRect(cell)

                val digit =
                    state.valueAt(cell)

                if (digit != 0) {
                    renderer.draw(
                        canvas = canvas,
                        target = rect,
                        digit = digit,
                        style = visualStyle,
                        given =
                            state.isGiven(
                                cell
                            )
                    )
                    continue
                }

                if (
                    state.hasGeckoMarker(
                        cell
                    ) &&
                    !mediaSuppressedGeckos
                        .contains(
                            cell
                        )
                ) {
                    drawGeckoMarker(
                        canvas,
                        rect,
                        cell
                    )
                    animateMarker = true
                }

                state.customMarkerAt(
                    cell
                )?.let {
                    drawCustomMarker(
                        canvas,
                        rect,
                        it
                    )
                }

                val candidates =
                    linkedSetOf<Int>()

                candidates.addAll(
                    state.notesAt(
                        cell
                    )
                )

                if (
                    professorCell ==
                        cell
                ) {
                    candidates.addAll(
                        professorCandidates
                    )
                }

                if (
                    candidates.isNotEmpty()
                ) {
                    drawCandidates(
                        canvas,
                        rect,
                        cell,
                        candidates
                    )
                }
            }
        }

        drawReasoningOverlay(
            canvas
        )

        if (
            animateMarker &&
            animateGeckoMarkers
        ) {
            postInvalidateDelayed(
                180L
            )
        }
    }

    private fun drawGeckoMarker(
        canvas: Canvas,
        rect: RectF,
        cell: Cell
    ) {
        val now =
            SystemClock
                .uptimeMillis()

        val seed =
            cell.row * 37 +
                cell.col * 19 +
                11

        val period =
            780L +
                (seed % 7) *
                95L

        val phase =
            seed *
                0.73

        val wave =
            if (animateGeckoMarkers) {
                sin(
                    now.toDouble() /
                        period.toDouble() *
                        Math.PI *
                        2.0 +
                        phase
                ).toFloat()
            } else {
                0f
            }

        val scale =
            .66f +
                .035f * wave

        val bob =
            rect.height() *
                .035f *
                wave

        val targetWidth =
            rect.width() *
                scale

        val targetHeight =
            rect.height() *
                scale

        val target =
            RectF(
                rect.centerX() -
                    targetWidth / 2f,
                rect.centerY() -
                    targetHeight / 2f +
                    bob,
                rect.centerX() +
                    targetWidth / 2f,
                rect.centerY() +
                    targetHeight / 2f +
                    bob
            )

        val sprite =
            geckoMarkerBitmap

        if (sprite != null) {
            canvas.save()

            canvas.rotate(
                wave * 3.5f,
                rect.centerX(),
                rect.centerY()
            )

            paint.alpha =
                155

            canvas.drawBitmap(
                sprite,
                null,
                target,
                paint
            )

            paint.alpha =
                255

            canvas.restore()
            return
        }

        paint.style =
            Paint.Style.FILL

        paint.color =
            Color.argb(
                150,
                38,
                128,
                72
            )

        canvas.drawOval(
            target,
            paint
        )
    }

    private fun drawReasoningOverlay(
        canvas: Canvas
    ) {
        val trace =
            reasoningTrace
                ?: return

        if (
            reasoningStepIndex !in
                trace.steps.indices
        ) {
            return
        }

        val current =
            trace.steps[
                reasoningStepIndex
            ]

        val source =
            current.sourceCell

        if (
            source != null
        ) {
            val sourceRect =
                cellRect(source)

            paint.style =
                Paint.Style.STROKE
            paint.strokeWidth =
                cellSize * .075f
            paint.color =
                Color.rgb(
                    172,
                    106,
                    0
                )

            canvas.drawRoundRect(
                RectF(
                    sourceRect.left +
                        cellSize * .06f,
                    sourceRect.top +
                        cellSize * .06f,
                    sourceRect.right -
                        cellSize * .06f,
                    sourceRect.bottom -
                        cellSize * .06f
                ),
                cellSize * .08f,
                cellSize * .08f,
                paint
            )

            when (
                current.projection
            ) {
                SudokuProjectionType
                    .ROW -> {
                    val y =
                        sourceRect.centerY()
                    canvas.drawLine(
                        boardRect.left,
                        y,
                        boardRect.right,
                        y,
                        paint
                    )
                }

                SudokuProjectionType
                    .COLUMN -> {
                    val x =
                        sourceRect.centerX()
                    canvas.drawLine(
                        x,
                        boardRect.top,
                        x,
                        boardRect.bottom,
                        paint
                    )
                }

                SudokuProjectionType
                    .BOX -> {
                    val boxRow =
                        source.row /
                            3 * 3
                    val boxCol =
                        source.col /
                            3 * 3
                    val first =
                        cellRect(
                            Cell(
                                boxRow,
                                boxCol
                            )
                        )
                    val last =
                        cellRect(
                            Cell(
                                boxRow + 2,
                                boxCol + 2
                            )
                        )

                    canvas.drawRect(
                        RectF(
                            first.left,
                            first.top,
                            last.right,
                            last.bottom
                        ),
                        paint
                    )
                }

                null -> Unit
            }
        }

        val cumulative =
            trace.steps
                .take(
                    reasoningStepIndex +
                        1
                )
                .flatMap {
                    it.eliminations
                }
                .distinctBy {
                    it.cell to
                        it.digit
                }

        paint.color =
            Color.rgb(
                170,
                32,
                32
            )
        paint.strokeWidth =
            cellSize * .065f
        paint.strokeCap =
            Paint.Cap.ROUND

        for (
            elimination in
                cumulative
        ) {
            val rect =
                cellRect(
                    elimination.cell
                )
            val inset =
                cellSize * .25f

            canvas.drawLine(
                rect.left + inset,
                rect.top + inset,
                rect.right - inset,
                rect.bottom - inset,
                paint
            )
            canvas.drawLine(
                rect.right - inset,
                rect.top + inset,
                rect.left + inset,
                rect.bottom - inset,
                paint
            )
        }

        val finalCell =
            current.finalCell

        if (
            finalCell != null
        ) {
            val rect =
                cellRect(
                    finalCell
                )
            paint.color =
                Color.rgb(
                    20,
                    118,
                    54
                )
            paint.strokeWidth =
                cellSize * .10f

            canvas.drawRoundRect(
                RectF(
                    rect.left +
                        cellSize * .04f,
                    rect.top +
                        cellSize * .04f,
                    rect.right -
                        cellSize * .04f,
                    rect.bottom -
                        cellSize * .04f
                ),
                cellSize * .10f,
                cellSize * .10f,
                paint
            )
        }
    }

    private fun drawCustomMarker(
        canvas: Canvas,
        rect: RectF,
        marker: CustomMarker
    ) {
        paint.style =
            Paint.Style.FILL
        paint.textAlign =
            Paint.Align.CENTER
        paint.isFakeBoldText = true
        paint.color =
            Color.rgb(
                20,
                45,
                90
            )
        paint.textSize =
            cellSize * .25f

        val x =
            rect.right -
                cellSize * .16f
        val y =
            rect.top +
                cellSize * .25f -
                (
                    paint.ascent() +
                        paint.descent()
                    ) / 2f

        canvas.drawText(
            marker.symbol,
            x,
            y,
            paint
        )

        paint.isFakeBoldText = false
    }

    private fun drawCandidates(
        canvas: Canvas,
        cellRect: RectF,
        cell: Cell,
        candidates: Set<Int>
    ) {
        for (
            digit in
                candidates.sorted()
        ) {
            val slot =
                SudokuCandidateLayout
                    .slot(digit)

            val slotRect =
                RectF(
                    cellRect.left +
                        slot.left *
                        cellRect.width(),
                    cellRect.top +
                        slot.top *
                        cellRect.height(),
                    cellRect.left +
                        slot.right *
                        cellRect.width(),
                    cellRect.top +
                        slot.bottom *
                        cellRect.height()
                )

            val inset =
                slotRect.width() *
                    .08f

            val target =
                RectF(
                    slotRect.left +
                        inset,
                    slotRect.top +
                        inset,
                    slotRect.right -
                        inset,
                    slotRect.bottom -
                        inset
                )

            val professorFocus =
                professorCell ==
                    cell &&
                    professorFocusDigit ==
                    digit

            if (professorFocus) {
                paint.style =
                    Paint.Style.FILL
                paint.color =
                    Color.argb(
                        52,
                        60,
                        150,
                        82
                    )

                canvas.drawRoundRect(
                    slotRect,
                    slotRect.width() *
                        .15f,
                    slotRect.height() *
                        .15f,
                    paint
                )
            }

            renderer.draw(
                canvas = canvas,
                target = target,
                digit = digit,
                style =
                    candidateVisualPolicy
                        .styleForCandidate(
                            visualStyle
                        ),
                mini = true,
                alpha =
                    if (
                        professorFocus
                    ) {
                        255
                    } else {
                        225
                    }
            )
        }
    }

    private fun updateBoardRect(
        w: Int,
        h: Int
    ) {
        if (
            w <= 0 ||
            h <= 0
        ) {
            return
        }

        val margin =
            SudokuFullWidthBoardPolicy
                .INNER_GRID_MARGIN_PX
                .toFloat()

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

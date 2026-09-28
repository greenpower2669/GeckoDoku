package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.sin

class GomokuBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    lateinit var snapshotProvider:
        () -> GomokuSnapshot

    var onPlayCell:
        ((Cell) -> Unit)? = null

    var onViewportChanged:
        ((GomokuViewport) -> Unit)? = null

    var animationsEnabled:
        Boolean = true

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val boardPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val geckoBitmap:
        Bitmap? =
        try {
            context.assets.open(
                AssetMediaCatalog
                    .GECKO_PORTRAIT
            ).use {
                BitmapFactory
                    .decodeStream(it)
            }
        } catch (_: Exception) {
            null
        }

    private val professorFilter =
        ColorMatrixColorFilter(
            ColorMatrix(
                GomokuYellowFilterPolicy
                    .colorMatrixValues()
            )
        )

    private var logicalSize =
        GomokuGameEngine.DEFAULT_SIZE

    private var viewportPolicy =
        GomokuViewportPolicy(
            logicalSize
        )

    private var viewport =
        viewportPolicy.initial()

    private val boardRect =
        RectF()

    private val gesturePolicy =
        GomokuGesturePolicy(
            dragThresholdPx =
                dp(10f)
        )

    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var panning = false
    private var scaledGesture = false

    private var animatedCell:
        Cell? = null
    private var animatedPlayer:
        GomokuPlayer? = null
    private var animationStartedAtMs =
        0L

    private val mediaSuppressedCells =
        linkedSetOf<Cell>()

    private var professorReasoning:
        GomokuReasoningTrace? = null

    private val scaleDetector =
        ScaleGestureDetector(
            context,
            object :
                ScaleGestureDetector
                    .SimpleOnScaleGestureListener() {
                override fun onScaleBegin(
                    detector:
                        ScaleGestureDetector
                ): Boolean {
                    scaledGesture = true
                    return true
                }

                override fun onScale(
                    detector:
                        ScaleGestureDetector
                ): Boolean {
                    if (
                        width <= 0 ||
                        height <= 0
                    ) {
                        return false
                    }

                    val fx =
                        (
                            detector.focusX /
                                width.toFloat()
                            ).coerceIn(
                            0f,
                            1f
                        )

                    val fy =
                        (
                            detector.focusY /
                                height.toFloat()
                            ).coerceIn(
                            0f,
                            1f
                        )

                    viewport =
                        viewportPolicy.zoom(
                            viewport =
                                viewport,
                            scaleFactor =
                                detector.scaleFactor,
                            focusXFraction =
                                fx,
                            focusYFraction =
                                fy
                        )

                    onViewportChanged
                        ?.invoke(viewport)

                    invalidate()
                    return true
                }
            }
        )

    init {
        isClickable = true
        isFocusable = true
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_YES

        contentDescription =
            "Plateau Gomoku. Touchez pour poser un Gecko, glissez pour explorer, pincez pour zoomer."
    }

    override fun performClick():
        Boolean {
        super.performClick()
        return true
    }

    fun reset(
        boardSize: Int
    ) {
        logicalSize =
            boardSize.coerceIn(
                5,
                31
            )

        viewportPolicy =
            GomokuViewportPolicy(
                logicalSize
            )

        viewport =
            viewportPolicy.initial()

        animatedCell = null
        animatedPlayer = null
        mediaSuppressedCells.clear()
        professorReasoning = null

        invalidate()
    }

    fun currentViewport():
        GomokuViewport =
        viewport

    fun centerOn(
        cell: Cell
    ) {
        viewport =
            viewportPolicy.centerOn(
                viewport,
                cell
            )

        onViewportChanged
            ?.invoke(viewport)

        invalidate()
    }

    fun showProfessorReasoning(
        reasoning:
            GomokuReasoningTrace
    ) {
        professorReasoning =
            reasoning

        if (
            cellRectLocal(
                reasoning.focusCell
            ) == null
        ) {
            centerOn(
                reasoning.focusCell
            )
        }

        invalidate()
    }

    fun clearProfessorReasoning() {
        if (
            professorReasoning ==
                null
        ) {
            return
        }

        professorReasoning =
            null
        invalidate()
    }

    fun animatePlacement(
        cell: Cell,
        player: GomokuPlayer
    ) {
        animatedCell = cell
        animatedPlayer = player
        animationStartedAtMs =
            SystemClock.uptimeMillis()

        invalidate()
    }

    fun setMediaStoneSuppressed(
        cell: Cell,
        suppressed: Boolean
    ) {
        if (suppressed) {
            mediaSuppressedCells.add(
                cell
            )
        } else {
            mediaSuppressedCells.remove(
                cell
            )
        }

        invalidate()
    }

    fun clearMediaStoneSuppression() {
        if (
            mediaSuppressedCells
                .isEmpty()
        ) {
            return
        }

        mediaSuppressedCells.clear()
        invalidate()
    }

    fun geckoRectOnScreen(
        cell: Cell
    ): RectF? {
        val local =
            cellRectLocal(cell)
                ?: return null

        val rect =
            RectF(local).apply {
                val insetAmount =
                    width() * .08f

                inset(
                    insetAmount,
                    insetAmount
                )
            }

        val location =
            IntArray(2)

        getLocationOnScreen(
            location
        )

        return rect.apply {
            offset(
                location[0].toFloat(),
                location[1].toFloat()
            )
        }
    }

    fun cellRectOnScreen(
        cell: Cell
    ): RectF? {
        val local =
            cellRectLocal(cell)
                ?: return null

        val location =
            IntArray(2)

        getLocationOnScreen(
            location
        )

        return RectF(local).apply {
            offset(
                location[0].toFloat(),
                location[1].toFloat()
            )
        }
    }

    fun cellBackgroundColor():
        Int =
        BOARD_COLOR

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

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {
        scaleDetector
            .onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                lastX = event.x
                lastY = event.y
                panning = false
                scaledGesture = false
                return true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                scaledGesture = true
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (
                    scaleDetector
                        .isInProgress ||
                    event.pointerCount >= 2
                ) {
                    scaledGesture = true
                    lastX = event.x
                    lastY = event.y
                    return true
                }

                val distance =
                    hypot(
                        event.x - downX,
                        event.y - downY
                    )

                val action =
                    gesturePolicy.actionFor(
                        pointerCount =
                            event.pointerCount,
                        distancePx =
                            distance,
                        scaleInProgress =
                            false
                    )

                if (
                    action ==
                        GomokuGestureAction
                            .PAN
                ) {
                    panning = true

                    val pixelsPerLogical =
                        (
                            boardRect.width() /
                                viewport
                                    .visibleSpan
                            ).coerceAtLeast(
                            1f
                        )

                    val dx =
                        event.x - lastX
                    val dy =
                        event.y - lastY

                    viewport =
                        viewportPolicy.pan(
                            viewport =
                                viewport,
                            deltaCols =
                                -dx /
                                    pixelsPerLogical,
                            deltaRows =
                                -dy /
                                    pixelsPerLogical
                        )

                    onViewportChanged
                        ?.invoke(viewport)

                    invalidate()
                }

                lastX = event.x
                lastY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                val distance =
                    hypot(
                        event.x - downX,
                        event.y - downY
                    )

                val action =
                    gesturePolicy.actionFor(
                        pointerCount =
                            if (
                                scaledGesture
                            ) {
                                2
                            } else {
                                1
                            },
                        distancePx =
                            distance,
                        scaleInProgress =
                            scaledGesture
                    )

                if (
                    !panning &&
                    action ==
                        GomokuGestureAction
                            .PLAY
                ) {
                    cellAt(
                        event.x,
                        event.y
                    )?.let {
                        onPlayCell
                            ?.invoke(it)
                        performClick()
                    }
                }

                panning = false
                scaledGesture = false
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                panning = false
                scaledGesture = false
                return true
            }
        }

        return true
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
            boardRect.width() <= 0f
        ) {
            updateBoardRect(
                width,
                height
            )
        }

        val snapshot =
            snapshotProvider()

        if (
            snapshot.size !=
                logicalSize
        ) {
            reset(
                snapshot.size
            )
        }

        drawBoard(canvas)
        drawGrid(canvas)
        drawGeckos(
            canvas,
            snapshot
        )
        drawProfessorReasoning(
            canvas
        )
        drawWinningLine(
            canvas,
            snapshot
        )

        if (
            animationsEnabled &&
            animatedCell != null
        ) {
            val elapsed =
                SystemClock
                    .uptimeMillis() -
                    animationStartedAtMs

            if (elapsed < 420L) {
                postInvalidateDelayed(
                    16L
                )
            } else {
                animatedCell = null
                animatedPlayer = null
            }
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

        val inset =
            dp(8f)

        boardRect.set(
            inset,
            inset,
            w.toFloat() - inset,
            h.toFloat() - inset
        )
    }

    private fun drawBoard(
        canvas: Canvas
    ) {
        boardPaint.style =
            Paint.Style.FILL
        boardPaint.color =
            BOARD_COLOR

        canvas.drawRoundRect(
            boardRect,
            dp(10f),
            dp(10f),
            boardPaint
        )
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
                77,
                58,
                35
            )
        paint.strokeWidth =
            dp(1.15f)

        val cell =
            logicalCellSize()

        val firstCol =
            floor(
                viewport.originCol
            ).toInt()
                .coerceAtLeast(0)

        val lastCol =
            ceil(
                viewport.originCol +
                    viewport.visibleSpan
            ).toInt()
                .coerceAtMost(
                    logicalSize
                )

        val firstRow =
            floor(
                viewport.originRow
            ).toInt()
                .coerceAtLeast(0)

        val lastRow =
            ceil(
                viewport.originRow +
                    viewport.visibleSpan
            ).toInt()
                .coerceAtMost(
                    logicalSize
                )

        for (
            col in
            firstCol until
                lastCol
        ) {
            val x =
                boardRect.left +
                    (
                        col +
                            .5f -
                            viewport
                                .originCol
                        ) *
                    cell

            if (
                x < boardRect.left ||
                x > boardRect.right
            ) {
                continue
            }

            canvas.drawLine(
                x,
                boardRect.top,
                x,
                boardRect.bottom,
                paint
            )
        }

        for (
            row in
            firstRow until
                lastRow
        ) {
            val y =
                boardRect.top +
                    (
                        row +
                            .5f -
                            viewport
                                .originRow
                        ) *
                    cell

            if (
                y < boardRect.top ||
                y > boardRect.bottom
            ) {
                continue
            }

            canvas.drawLine(
                boardRect.left,
                y,
                boardRect.right,
                y,
                paint
            )
        }
    }

    private fun drawGeckos(
        canvas: Canvas,
        snapshot: GomokuSnapshot
    ) {
        snapshot.stones.forEach {
                entry ->

            if (
                mediaSuppressedCells
                    .contains(
                        entry.key
                    )
            ) {
                return@forEach
            }

            val rect =
                cellRectLocal(
                    entry.key
                )
                    ?: return@forEach

            val scale =
                placementScale(
                    entry.key,
                    entry.value
                )

            drawGecko(
                canvas =
                    canvas,
                rect =
                    scaledRect(
                        rect,
                        scale
                    ),
                player =
                    entry.value
            )
        }
    }

    private fun drawProfessorReasoning(
        canvas: Canvas
    ) {
        val reasoning =
            professorReasoning
                ?: return

        val lineRects =
            reasoning.lineCells
                .mapNotNull {
                    cell ->
                    cellRectLocal(
                        cell
                    )
                        ?.let {
                            cell to
                                it
                        }
                }

        if (lineRects.size >= 2) {
            paint.style =
                Paint.Style.STROKE
            paint.strokeCap =
                Paint.Cap.ROUND
            paint.strokeWidth =
                dp(4f)
            paint.color =
                Color.rgb(
                    45,
                    105,
                    190
                )

            val first =
                lineRects.first()
                    .second

            val last =
                lineRects.last()
                    .second

            canvas.drawLine(
                first.centerX(),
                first.centerY(),
                last.centerX(),
                last.centerY(),
                paint
            )
        }

        reasoning.lineCells
            .forEach {
                cell ->
                val rect =
                    cellRectLocal(
                        cell
                    )
                        ?: return@forEach

                paint.style =
                    Paint.Style.STROKE
                paint.strokeWidth =
                    dp(2.8f)
                paint.color =
                    Color.rgb(
                        45,
                        105,
                        190
                    )

                canvas.drawOval(
                    RectF(rect).apply {
                        inset(
                            rect.width() *
                                .18f,
                            rect.height() *
                                .18f
                        )
                    },
                    paint
                )
            }

        reasoning.threatCells
            .forEach {
                cell ->
                val rect =
                    cellRectLocal(
                        cell
                    )
                        ?: return@forEach

                paint.style =
                    Paint.Style.STROKE
                paint.strokeWidth =
                    dp(4f)
                paint.color =
                    Color.rgb(
                        190,
                        45,
                        45
                    )

                canvas.drawRoundRect(
                    RectF(rect).apply {
                        inset(
                            rect.width() *
                                .16f,
                            rect.height() *
                                .16f
                        )
                    },
                    dp(7f),
                    dp(7f),
                    paint
                )
            }

        cellRectLocal(
            reasoning.focusCell
        )
            ?.let {
                rect ->

                paint.style =
                    Paint.Style.STROKE
                paint.strokeWidth =
                    dp(5f)
                paint.color =
                    Color.rgb(
                        35,
                        150,
                        65
                    )

                canvas.drawOval(
                    RectF(rect).apply {
                        inset(
                            rect.width() *
                                .08f,
                            rect.height() *
                                .08f
                        )
                    },
                    paint
                )
            }

        reasoning.projectedCells
            .forEachIndexed {
                index,
                cell ->

                val rect =
                    cellRectLocal(
                        cell
                    )
                        ?: return@forEachIndexed

                paint.style =
                    Paint.Style.FILL
                paint.color =
                    Color.rgb(
                        224,
                        145,
                        20
                    )
                paint.textAlign =
                    Paint.Align.CENTER
                paint.textSize =
                    (
                        rect.width() *
                            .34f
                        )
                        .coerceAtLeast(
                            dp(13f)
                        )
                paint.isFakeBoldText =
                    true

                canvas.drawText(
                    (index + 1)
                        .toString(),
                    rect.centerX(),
                    rect.centerY() +
                        paint.textSize *
                            .34f,
                    paint
                )

                paint.isFakeBoldText =
                    false
            }
    }

    private fun drawWinningLine(
        canvas: Canvas,
        snapshot: GomokuSnapshot
    ) {
        if (
            snapshot.winningLine
                .isEmpty()
        ) {
            return
        }

        paint.style =
            Paint.Style.STROKE
        paint.strokeWidth =
            dp(3.4f)
        paint.color =
            Color.rgb(
                170,
                35,
                35
            )

        snapshot.winningLine
            .forEach {
                cell ->
                val rect =
                    cellRectLocal(
                        cell
                    )
                        ?: return@forEach

                canvas.drawOval(
                    RectF(rect).apply {
                        inset(
                            rect.width() *
                                .08f,
                            rect.height() *
                                .08f
                        )
                    },
                    paint
                )
            }
    }

    private fun drawGecko(
        canvas: Canvas,
        rect: RectF,
        player: GomokuPlayer
    ) {
        val sprite =
            geckoBitmap

        if (sprite != null) {
            paint.alpha = 255
            paint.colorFilter =
                if (
                    player ==
                        GomokuPlayer
                            .PROFESSOR
                ) {
                    professorFilter
                } else {
                    null
                }

            val inset =
                rect.width() *
                    .08f

            canvas.drawBitmap(
                sprite,
                null,
                RectF(rect).apply {
                    inset(
                        inset,
                        inset
                    )
                },
                paint
            )

            paint.colorFilter = null
            return
        }

        paint.style =
            Paint.Style.FILL

        paint.color =
            if (
                player ==
                    GomokuPlayer.PLAYER
            ) {
                Color.rgb(
                    34,
                    139,
                    70
                )
            } else {
                Color.rgb(
                    220,
                    190,
                    35
                )
            }

        canvas.drawOval(
            RectF(rect).apply {
                inset(
                    rect.width() *
                        .14f,
                    rect.height() *
                        .14f
                )
            },
            paint
        )
    }

    private fun placementScale(
        cell: Cell,
        player: GomokuPlayer
    ): Float {
        if (
            !animationsEnabled ||
            animatedCell != cell ||
            animatedPlayer != player
        ) {
            return 1f
        }

        val elapsed =
            (
                SystemClock
                    .uptimeMillis() -
                    animationStartedAtMs
                ).coerceAtLeast(0L)

        if (elapsed >= 420L) {
            return 1f
        }

        val t =
            elapsed /
                420f

        return (
            .68f +
                .32f *
                t +
                .16f *
                sin(
                    t *
                        Math.PI *
                        2.0
                ).toFloat() *
                (1f - t)
            ).coerceIn(
            .55f,
            1.12f
        )
    }

    private fun scaledRect(
        source: RectF,
        scale: Float
    ): RectF {
        val cx =
            source.centerX()
        val cy =
            source.centerY()

        val halfW =
            source.width() *
                scale /
                2f

        val halfH =
            source.height() *
                scale /
                2f

        return RectF(
            cx - halfW,
            cy - halfH,
            cx + halfW,
            cy + halfH
        )
    }

    private fun logicalCellSize():
        Float =
        (
            boardRect.width() /
                viewport.visibleSpan
            ).coerceAtLeast(
            1f
        )

    private fun cellRectLocal(
        cell: Cell
    ): RectF? {
        if (
            cell.row !in
                0 until logicalSize ||
            cell.col !in
                0 until logicalSize
        ) {
            return null
        }

        val cellSize =
            logicalCellSize()

        val left =
            boardRect.left +
                (
                    cell.col -
                        viewport
                            .originCol
                    ) *
                cellSize

        val top =
            boardRect.top +
                (
                    cell.row -
                        viewport
                            .originRow
                    ) *
                cellSize

        val rect =
            RectF(
                left,
                top,
                left + cellSize,
                top + cellSize
            )

        if (
            rect.right <
                boardRect.left ||
            rect.left >
                boardRect.right ||
            rect.bottom <
                boardRect.top ||
            rect.top >
                boardRect.bottom
        ) {
            return null
        }

        return rect
    }

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

        val size =
            logicalCellSize()

        val col =
            floor(
                viewport.originCol +
                    (
                        x -
                            boardRect.left
                        ) /
                    size
            ).toInt()

        val row =
            floor(
                viewport.originRow +
                    (
                        y -
                            boardRect.top
                        ) /
                    size
            ).toInt()

        if (
            row !in 0 until
                logicalSize ||
            col !in 0 until
                logicalSize
        ) {
            return null
        }

        return Cell(
            row,
            col
        )
    }

    private fun dp(
        value: Float
    ): Float =
        value *
            resources
                .displayMetrics
                .density

    companion object {
        private val BOARD_COLOR =
            Color.rgb(
                232,
                202,
                145
            )
    }
}

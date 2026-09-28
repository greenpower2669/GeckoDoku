package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

class BeeGeckoBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    lateinit var snapshotProvider:
        () -> BeeGeckoSnapshot

    var onSingleTapCell:
        ((HexCoord) -> Unit)? = null

    var onDoubleTapCell:
        ((HexCoord) -> Unit)? = null

    var onLongPressCell:
        ((HexCoord) -> Unit)? = null

    var onViewportChanged:
        ((BeeGeckoCamera) -> Unit)? = null

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val hexPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val viewportPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val viewportInset =
        dp(8f)

    private val geckoBitmap:
        Bitmap? =
        loadBitmap(
            AssetMediaCatalog
                .GECKO_PORTRAIT
        )

    private val beeBitmap:
        Bitmap? =
        loadBitmap(
            AssetMediaCatalog
                .BEE_PORTRAIT
        )

    private var puzzle:
        BeeGeckoPuzzle? = null

    private var camera =
        BeeGeckoCamera()

    private var initializedCamera =
        false

    private var worldBounds =
        BeeGeckoBounds(
            0f,
            0f,
            0f,
            0f
        )

    private val baseRadius =
        dp(35f)

    private val horizontalStep =
        sqrt(3f) *
            baseRadius

    private val verticalStep =
        1.5f *
            baseRadius

    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var dragging =
        false
    private var scaledGesture =
        false
    private var longPressTriggered =
        false

    private var downCell:
        HexCoord? = null

    private var longPressRunnable:
        Runnable? = null

    private var pendingSingleTap:
        Runnable? = null

    private var lastTapCell:
        HexCoord? = null

    private var lastTapAtMs =
        0L

    private val dragThreshold =
        dp(10f)

    private val gesturePolicy =
        BeeGeckoGesturePolicy(
            dragThresholdPx =
                dragThreshold
        )

    private var professorHint:
        BeeGeckoHint? = null

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
                    cancelPendingLongPress()
                    cancelPendingSingleTap()
                    return true
                }

                override fun onScale(
                    detector:
                        ScaleGestureDetector
                ): Boolean {
                    camera =
                        BeeGeckoViewportPolicy
                            .zoom(
                                camera =
                                    camera,
                                factor =
                                    detector
                                        .scaleFactor,
                                focusX =
                                    detector
                                        .focusX,
                                focusY =
                                    detector
                                        .focusY,
                                minimumScale =
                                    minimumScale()
                            )

                    clampCamera()
                    notifyViewport()
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
            "Plateau Abeilles et Geckos. Un tap pose ou retire une croix, un double tap choisit Gecko ou Abeille, un appui long ouvre les repères. Glissez pour déplacer et pincez pour zoomer."
    }

    override fun performClick():
        Boolean {
        super.performClick()
        return true
    }

    fun setPuzzle(
        next: BeeGeckoPuzzle,
        restoredCamera:
            BeeGeckoCamera? = null
    ) {
        puzzle = next
        calculateWorldBounds(
            next
        )

        camera =
            restoredCamera
                ?: BeeGeckoCamera()

        initializedCamera =
            restoredCamera != null

        professorHint = null
        cancelPendingSingleTap()
        cancelPendingLongPress()

        if (
            width > 0 &&
            height > 0
        ) {
            ensureCamera()
        }

        invalidate()
    }

    fun currentCamera():
        BeeGeckoCamera =
        camera

    fun showProfessorHint(
        hint: BeeGeckoHint
    ) {
        professorHint = hint

        val focus =
            hint.green
                .firstOrNull()
                ?: hint.blue
                    .firstOrNull()

        if (focus != null) {
            centerOn(focus)
        }

        invalidate()
    }

    fun clearProfessorHint() {
        professorHint = null
        invalidate()
    }

    fun recenter() {
        initializedCamera = false
        ensureCamera()
        notifyViewport()
        invalidate()
    }

    fun centerOn(
        cell: HexCoord
    ) {
        val center =
            cellCenter(cell)

        val viewport =
            viewportBounds()

        camera =
            camera.copy(
                offsetX =
                    viewport.centerX -
                        center.first *
                            camera.scale,
                offsetY =
                    viewport.centerY -
                        center.second *
                            camera.scale
            )

        clampCamera()
        notifyViewport()
        invalidate()
    }

    fun cellRectOnScreen(
        cell: HexCoord
    ): RectF? {
        val puzzle =
            puzzle
                ?: return null

        if (!puzzle.contains(cell)) {
            return null
        }

        val center =
            cellCenter(cell)

        val cx =
            center.first *
                camera.scale +
                camera.offsetX

        val cy =
            center.second *
                camera.scale +
                camera.offsetY

        val radius =
            baseRadius *
                camera.scale

        val rect =
            RectF(
                cx - radius,
                cy - radius,
                cx + radius,
                cy + radius
            )

        if (
            rect.right < 0f ||
            rect.bottom < 0f ||
            rect.left > width ||
            rect.top > height
        ) {
            return null
        }

        val location =
            IntArray(2)

        getLocationOnScreen(
            location
        )

        rect.offset(
            location[0].toFloat(),
            location[1].toFloat()
        )

        val viewport =
            viewportBounds()

        if (
            rect.centerX() <
                viewport.left ||
            rect.centerX() >
                viewport.right ||
            rect.centerY() <
                viewport.top ||
            rect.centerY() >
                viewport.bottom
        ) {
            return null
        }

        return rect
    }

    fun viewportRectOnScreen():
        RectF? {
        if (
            width <= 0 ||
            height <= 0
        ) {
            return null
        }

        val viewport =
            viewportBounds()

        val location =
            IntArray(2)

        getLocationOnScreen(
            location
        )

        return RectF(
            viewport.left +
                location[0],
            viewport.top +
                location[1],
            viewport.right +
                location[0],
            viewport.bottom +
                location[1]
        )
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        super.onSizeChanged(
            w,
            h,
            oldw,
            oldh
        )

        if (
            w > 0 &&
            h > 0
        ) {
            if (initializedCamera) {
                clampCamera()
                notifyViewport()
            } else {
                ensureCamera()
            }
        }
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(canvas)

        val puzzle =
            puzzle
                ?: return

        if (
            !::snapshotProvider
                .isInitialized
        ) {
            return
        }

        ensureCamera()

        val snapshot =
            snapshotProvider()

        val viewport =
            viewportBounds()

        viewportPaint.style =
            Paint.Style.FILL
        viewportPaint.color =
            Color.rgb(
                252,
                252,
                249
            )

        canvas.drawRect(
            viewport.left,
            viewport.top,
            viewport.right,
            viewport.bottom,
            viewportPaint
        )

        canvas.save()
        canvas.clipRect(
            viewport.left,
            viewport.top,
            viewport.right,
            viewport.bottom
        )
        canvas.translate(
            camera.offsetX,
            camera.offsetY
        )
        canvas.scale(
            camera.scale,
            camera.scale
        )

        drawRegionsAndGrid(
            canvas,
            puzzle
        )

        drawPieces(
            canvas,
            snapshot
        )

        drawCrosses(
            canvas,
            snapshot
        )

        drawMarkers(
            canvas,
            snapshot
        )

        drawProfessorProjection(
            canvas
        )

        canvas.restore()

        viewportPaint.style =
            Paint.Style.STROKE
        viewportPaint.strokeWidth =
            dp(2.4f)
        viewportPaint.color =
            Color.rgb(
                48,
                48,
                46
            )

        canvas.drawRect(
            viewport.left,
            viewport.top,
            viewport.right,
            viewport.bottom,
            viewportPaint
        )
    }

    private fun drawRegionsAndGrid(
        canvas: Canvas,
        puzzle: BeeGeckoPuzzle
    ) {
        for (cell in puzzle.cells) {
            val path =
                hexPath(cell)

            hexPaint.style =
                Paint.Style.FILL

            hexPaint.color =
                GeckoBoardPalette
                    .colorFor(
                        puzzle
                            .regionAt(cell)
                    )

            canvas.drawPath(
                path,
                hexPaint
            )

            hexPaint.style =
                Paint.Style.STROKE
            hexPaint.strokeWidth =
                dp(1.05f) /
                    camera.scale
            hexPaint.color =
                Color.rgb(
                    88,
                    88,
                    82
                )

            canvas.drawPath(
                path,
                hexPaint
            )
        }

        for (cell in puzzle.cells) {
            val region =
                puzzle.regionAt(cell)

            val vertices =
                hexVertices(cell)

            val neighbors =
                cell.neighbors()

            for (
                direction in
                0 until 6
            ) {
                val neighbor =
                    neighbors[direction]

                if (
                    !puzzle.contains(
                        neighbor
                    ) ||
                    puzzle.regionAt(
                        neighbor
                    ) != region
                ) {
                    val edge =
                        edgeVertices(
                            direction
                        )

                    hexPaint.style =
                        Paint.Style.STROKE
                    hexPaint.strokeCap =
                        Paint.Cap.ROUND
                    hexPaint.strokeWidth =
                        dp(3.2f) /
                            camera.scale
                    hexPaint.color =
                        Color.rgb(
                            40,
                            40,
                            38
                        )

                    canvas.drawLine(
                        vertices[
                            edge.first
                        ].first,
                        vertices[
                            edge.first
                        ].second,
                        vertices[
                            edge.second
                        ].first,
                        vertices[
                            edge.second
                        ].second,
                        hexPaint
                    )
                }
            }
        }
    }

    private fun drawPieces(
        canvas: Canvas,
        snapshot: BeeGeckoSnapshot
    ) {
        val puzzle =
            snapshot.puzzle

        for (cell in puzzle.cells) {
            val piece =
                snapshot
                    .pieceAt(cell)
                    ?: continue

            val center =
                cellCenter(cell)

            val pieceScale =
                if (
                    piece ==
                        BeeGeckoPiece.BEE
                ) {
                    BeeGeckoVisualPolicy
                        .BEE_SCALE
                } else {
                    1f
                }

            val radius =
                baseRadius *
                    .61f *
                    pieceScale

            val target =
                RectF(
                    center.first -
                        radius,
                    center.second -
                        radius,
                    center.first +
                        radius,
                    center.second +
                        radius
                )

            val bitmap =
                if (
                    piece ==
                        BeeGeckoPiece.GECKO
                ) {
                    geckoBitmap
                } else {
                    beeBitmap
                }

            if (bitmap != null) {
                paint.alpha = 255
                paint.colorFilter = null

                canvas.drawBitmap(
                    bitmap,
                    null,
                    target,
                    paint
                )
            } else {
                paint.style =
                    Paint.Style.FILL
                paint.color =
                    if (
                        piece ==
                            BeeGeckoPiece.GECKO
                    ) {
                        Color.rgb(
                            51,
                            137,
                            72
                        )
                    } else {
                        Color.rgb(
                            243,
                            183,
                            32
                        )
                    }

                canvas.drawCircle(
                    center.first,
                    center.second,
                    radius *
                        .58f,
                    paint
                )

                paint.color =
                    Color.BLACK
                paint.textAlign =
                    Paint.Align.CENTER
                paint.textSize =
                    radius *
                        .72f
                paint.isFakeBoldText =
                    true

                canvas.drawText(
                    if (
                        piece ==
                            BeeGeckoPiece.GECKO
                    ) {
                        "G"
                    } else {
                        "A"
                    },
                    center.first,
                    center.second +
                        paint.textSize *
                            .34f,
                    paint
                )

                paint.isFakeBoldText =
                    false
            }

            if (
                snapshot.isGiven(
                    cell
                )
            ) {
                paint.style =
                    Paint.Style.STROKE
                paint.strokeWidth =
                    dp(3f) /
                        camera.scale
                paint.color =
                    Color.rgb(
                        35,
                        35,
                        35
                    )

                canvas.drawCircle(
                    center.first,
                    center.second,
                    radius *
                        .77f,
                    paint
                )
            }
        }
    }

    private fun drawCrosses(
        canvas: Canvas,
        snapshot: BeeGeckoSnapshot
    ) {
        paint.style =
            Paint.Style.STROKE
        paint.strokeCap =
            Paint.Cap.ROUND
        paint.strokeWidth =
            dp(3f) /
                camera.scale
        paint.color =
            Color.rgb(
                170,
                40,
                40
            )

        for (
            cell in
            snapshot.manualCrosses
        ) {
            val center =
                cellCenter(cell)

            val size =
                baseRadius *
                    .31f

            canvas.drawLine(
                center.first - size,
                center.second - size,
                center.first + size,
                center.second + size,
                paint
            )

            canvas.drawLine(
                center.first + size,
                center.second - size,
                center.first - size,
                center.second + size,
                paint
            )
        }
    }

    private fun drawMarkers(
        canvas: Canvas,
        snapshot: BeeGeckoSnapshot
    ) {
        paint.style =
            Paint.Style.FILL
        paint.color =
            Color.rgb(
                54,
                48,
                72
            )
        paint.textAlign =
            Paint.Align.CENTER
        paint.textSize =
            baseRadius *
                .44f
        paint.isFakeBoldText =
            true

        snapshot.markers
            .forEach {
                (cell, marker) ->
                val center =
                    cellCenter(
                        cell
                    )

                canvas.drawText(
                    marker.symbol,
                    center.first +
                        baseRadius *
                            .49f,
                    center.second -
                        baseRadius *
                            .40f,
                    paint
                )
            }

        paint.isFakeBoldText =
            false
    }

    private fun drawProfessorProjection(
        canvas: Canvas
    ) {
        val hint =
            professorHint
                ?: return

        drawHintSet(
            canvas =
                canvas,
            cells =
                hint.blue,
            color =
                Color.rgb(
                    43,
                    105,
                    190
                ),
            symbol =
                "A",
            widthDp =
                4.7f
        )

        drawHintSet(
            canvas =
                canvas,
            cells =
                hint.orange,
            color =
                Color.rgb(
                    224,
                    145,
                    20
                ),
            symbol =
                "?",
            widthDp =
                4.1f
        )

        drawHintSet(
            canvas =
                canvas,
            cells =
                hint.red,
            color =
                Color.rgb(
                    190,
                    45,
                    45
                ),
            symbol =
                "×",
            widthDp =
                4.0f
        )

        drawHintSet(
            canvas =
                canvas,
            cells =
                hint.green,
            color =
                Color.rgb(
                    35,
                    150,
                    65
                ),
            symbol =
                "✓",
            widthDp =
                5.6f
        )

        val g =
            hint.step.gecko

        val b =
            hint.step.bee

        if (
            g != null &&
            b != null &&
            BeeGeckoRules
                .areNeighbors(
                    g,
                    b
                )
        ) {
            val gc =
                cellCenter(g)
            val bc =
                cellCenter(b)

            paint.style =
                Paint.Style.STROKE
            paint.strokeCap =
                Paint.Cap.ROUND
            paint.strokeWidth =
                dp(5f) /
                    camera.scale
            paint.color =
                Color.rgb(
                    35,
                    150,
                    65
                )

            canvas.drawLine(
                gc.first,
                gc.second,
                bc.first,
                bc.second,
                paint
            )
        }
    }

    private fun drawHintSet(
        canvas: Canvas,
        cells: Set<HexCoord>,
        color: Int,
        symbol: String,
        widthDp: Float
    ) {
        for (cell in cells) {
            if (
                puzzle
                    ?.contains(cell) !=
                    true
            ) {
                continue
            }

            hexPaint.style =
                Paint.Style.STROKE
            hexPaint.strokeWidth =
                dp(widthDp) /
                    camera.scale
            hexPaint.color =
                color

            canvas.drawPath(
                hexPath(cell),
                hexPaint
            )

            val center =
                cellCenter(cell)

            paint.style =
                Paint.Style.FILL
            paint.color =
                color
            paint.textAlign =
                Paint.Align.CENTER
            paint.textSize =
                baseRadius *
                    .38f
            paint.isFakeBoldText =
                true

            canvas.drawText(
                symbol,
                center.first,
                center.second -
                    baseRadius *
                        .52f,
                paint
            )

            paint.isFakeBoldText =
                false
        }
    }

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {
        if (
            event.actionMasked ==
                MotionEvent.ACTION_DOWN &&
            !viewportContains(
                event.x,
                event.y
            )
        ) {
            return false
        }

        scaleDetector
            .onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                lastX = event.x
                lastY = event.y
                dragging = false
                scaledGesture = false
                longPressTriggered = false

                downCell =
                    screenToCell(
                        event.x,
                        event.y
                    )

                scheduleLongPress()
                return true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                scaledGesture = true
                cancelPendingLongPress()
                cancelPendingSingleTap()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (
                    scaleDetector
                        .isInProgress ||
                    event.pointerCount >= 2
                ) {
                    scaledGesture = true
                    cancelPendingLongPress()
                    cancelPendingSingleTap()
                    lastX = event.x
                    lastY = event.y
                    return true
                }

                val distance =
                    hypot(
                        event.x -
                            downX,
                        event.y -
                            downY
                    )

                val action =
                    gesturePolicy
                        .actionFor(
                            pointerCount =
                                event
                                    .pointerCount,
                            distancePx =
                                distance,
                            scaleInProgress =
                                false
                        )

                if (
                    action ==
                        BeeGeckoGestureAction
                            .PAN
                ) {
                    dragging = true
                    cancelPendingLongPress()
                    cancelPendingSingleTap()

                    camera =
                        BeeGeckoViewportPolicy
                            .pan(
                                camera =
                                    camera,
                                dx =
                                    event.x -
                                        lastX,
                                dy =
                                    event.y -
                                        lastY
                            )

                    clampCamera()
                    notifyViewport()
                    invalidate()
                }

                lastX = event.x
                lastY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                cancelPendingLongPress()

                val distance =
                    hypot(
                        event.x -
                            downX,
                        event.y -
                            downY
                    )

                val action =
                    gesturePolicy
                        .actionFor(
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
                    !dragging &&
                    !longPressTriggered &&
                    action ==
                        BeeGeckoGestureAction
                            .TAP
                ) {
                    performClick()

                    screenToCell(
                        event.x,
                        event.y
                    )
                        ?.let {
                            handleTap(it)
                        }
                }

                dragging = false
                scaledGesture = false
                downCell = null
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                cancelPendingLongPress()
                cancelPendingSingleTap()
                dragging = false
                scaledGesture = false
                downCell = null
                return true
            }
        }

        return true
    }

    private fun handleTap(
        cell: HexCoord
    ) {
        val now =
            SystemClock
                .uptimeMillis()

        if (
            lastTapCell ==
                cell &&
            now -
                lastTapAtMs <=
                DOUBLE_TAP_MS
        ) {
            cancelPendingSingleTap()
            lastTapCell = null
            lastTapAtMs = 0L

            onDoubleTapCell
                ?.invoke(cell)

            return
        }

        lastTapCell = cell
        lastTapAtMs = now

        cancelPendingSingleTap()

        val runnable =
            Runnable {
                if (
                    lastTapCell ==
                        cell
                ) {
                    lastTapCell = null
                    lastTapAtMs = 0L

                    onSingleTapCell
                        ?.invoke(cell)
                }
            }

        pendingSingleTap =
            runnable

        postDelayed(
            runnable,
            DOUBLE_TAP_MS
        )
    }

    private fun scheduleLongPress() {
        cancelPendingLongPress()

        val cell =
            downCell
                ?: return

        val runnable =
            Runnable {
                if (
                    !dragging &&
                    !scaledGesture
                ) {
                    longPressTriggered = true
                    cancelPendingSingleTap()

                    onLongPressCell
                        ?.invoke(cell)
                }
            }

        longPressRunnable =
            runnable

        postDelayed(
            runnable,
            LONG_PRESS_MS
        )
    }

    private fun cancelPendingLongPress() {
        longPressRunnable
            ?.let {
                removeCallbacks(it)
            }

        longPressRunnable = null
    }

    private fun cancelPendingSingleTap() {
        pendingSingleTap
            ?.let {
                removeCallbacks(it)
            }

        pendingSingleTap = null
    }

    private fun screenToCell(
        x: Float,
        y: Float
    ): HexCoord? {
        if (
            !viewportContains(
                x,
                y
            )
        ) {
            return null
        }

        val puzzle =
            puzzle
                ?: return null

        val worldX =
            (
                x -
                    camera.offsetX
                ) /
                camera.scale

        val worldY =
            (
                y -
                    camera.offsetY
                ) /
                camera.scale

        var best:
            HexCoord? = null

        var bestDistance =
            Float.MAX_VALUE

        for (cell in puzzle.cells) {
            val center =
                cellCenter(
                    cell
                )

            val distance =
                hypot(
                    center.first -
                        worldX,
                    center.second -
                        worldY
                )

            if (
                distance <
                    bestDistance
            ) {
                bestDistance =
                    distance
                best = cell
            }
        }

        return best
            ?.takeIf {
                bestDistance <=
                    baseRadius *
                        .94f
            }
    }

    private fun ensureCamera() {
        if (
            initializedCamera ||
            width <= 0 ||
            height <= 0 ||
            worldBounds.width <= 0f ||
            worldBounds.height <= 0f
        ) {
            return
        }

        camera =
            BeeGeckoViewportPolicy
                .centeredInViewport(
                    viewport =
                        viewportBounds(),
                    content =
                        worldBounds,
                    preferredMinScale =
                        minimumScale()
                )

        initializedCamera = true
        clampCamera()
        notifyViewport()
    }

    private fun clampCamera() {
        camera =
            BeeGeckoViewportPolicy
                .clampInViewport(
                    camera =
                        camera,
                    viewport =
                        viewportBounds(),
                    content =
                        worldBounds,
                    visibleMarginPx =
                        dp(6f)
                )
    }

    private fun minimumScale():
        Float {
        val viewport =
            viewportBounds()

        return BeeGeckoViewportPolicy
            .fitScaleInViewport(
                viewport =
                    viewport,
                content =
                    worldBounds,
                marginPx =
                    dp(6f)
            )
    }

    private fun viewportBounds():
        BeeGeckoBounds =
        BeeGeckoSquareViewportPolicy
            .bounds(
                viewWidth =
                    width.toFloat(),
                viewHeight =
                    height.toFloat(),
                insetPx =
                    viewportInset
            )

    private fun viewportContains(
        x: Float,
        y: Float
    ): Boolean {
        val viewport =
            viewportBounds()

        return x >=
            viewport.left &&
            x <=
                viewport.right &&
            y >=
                viewport.top &&
            y <=
                viewport.bottom
    }

    private fun notifyViewport() {
        onViewportChanged
            ?.invoke(camera)
    }

    private fun calculateWorldBounds(
        puzzle: BeeGeckoPuzzle
    ) {
        val centers =
            puzzle.cells
                .map {
                    cellCenter(it)
                }

        if (centers.isEmpty()) {
            worldBounds =
                BeeGeckoBounds(
                    0f,
                    0f,
                    0f,
                    0f
                )
            return
        }

        val minX =
            centers.minOf {
                it.first
            } -
                baseRadius

        val maxX =
            centers.maxOf {
                it.first
            } +
                baseRadius

        val minY =
            centers.minOf {
                it.second
            } -
                baseRadius

        val maxY =
            centers.maxOf {
                it.second
            } +
                baseRadius

        worldBounds =
            BeeGeckoBounds(
                left =
                    minX,
                top =
                    minY,
                right =
                    maxX,
                bottom =
                    maxY
            )
    }

    private fun cellCenter(
        cell: HexCoord
    ): Pair<Float, Float> {
        val x =
            horizontalStep *
                (
                    cell.q +
                        cell.r /
                            2f
                    )

        val y =
            verticalStep *
                cell.r

        return x to y
    }

    private fun hexPath(
        cell: HexCoord
    ): Path {
        val vertices =
            hexVertices(cell)

        return Path()
            .apply {
                vertices
                    .forEachIndexed {
                        index,
                        point ->

                        if (index == 0) {
                            moveTo(
                                point.first,
                                point.second
                            )
                        } else {
                            lineTo(
                                point.first,
                                point.second
                            )
                        }
                    }

                close()
            }
    }

    private fun hexVertices(
        cell: HexCoord
    ): List<Pair<Float, Float>> {
        val center =
            cellCenter(
                cell
            )

        return List(6) {
            index ->

            val angle =
                Math.toRadians(
                    60.0 *
                        index -
                        30.0
                )

            center.first +
                baseRadius *
                    cos(angle)
                        .toFloat() to
                center.second +
                    baseRadius *
                        sin(angle)
                            .toFloat()
        }
    }

    private fun edgeVertices(
        direction: Int
    ): Pair<Int, Int> =
        when (direction) {
            0 -> 0 to 1
            1 -> 5 to 0
            2 -> 4 to 5
            3 -> 3 to 4
            4 -> 2 to 3
            else -> 1 to 2
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
        } catch (
            _: Exception
        ) {
            null
        }

    private fun dp(
        value: Float
    ): Float =
        value *
            resources
                .displayMetrics
                .density

    companion object {
        private const val DOUBLE_TAP_MS =
            285L

        private const val LONG_PRESS_MS =
            520L
    }
}

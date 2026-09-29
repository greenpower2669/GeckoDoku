package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
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

    var onAxisGuideMoved:
        ((
            HexCoord,
            HexAxis,
            HexCoord?
        ) -> Unit)? =
        null

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

    private val mediaSuppressedPieces =
        linkedSetOf<HexCoord>()

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

    private var draggingAxisGuide:
        Pair<HexCoord, HexAxis>? =
        null

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

    private var victoryStartedAt =
        0L

    private var victoryRunning =
        false

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
        victoryRunning = false
        victoryStartedAt = 0L
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

    fun startVictoryAnimation() {
        victoryStartedAt =
            SystemClock
                .uptimeMillis()

        victoryRunning = true
        invalidate()
    }

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

    fun setMediaPieceSuppressed(
        cell: HexCoord,
        suppressed: Boolean
    ) {
        if (suppressed) {
            mediaSuppressedPieces.add(
                cell
            )
        } else {
            mediaSuppressedPieces.remove(
                cell
            )
        }

        invalidate()
    }

    fun clearMediaPieceSuppression() {
        if (
            mediaSuppressedPieces
                .isEmpty()
        ) {
            return
        }

        mediaSuppressedPieces.clear()
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

    fun cellBackgroundColor(
        cell: HexCoord
    ): Int {
        val board =
            puzzle
                ?: return Color.WHITE

        if (!board.contains(cell)) {
            return Color.WHITE
        }

        return GeckoBoardPalette
            .colorFor(
                board.regionAt(cell)
            )
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

        if (victoryRunning) {
            val elapsed =
                SystemClock
                    .uptimeMillis() -
                    victoryStartedAt

            if (elapsed <
                VICTORY_DURATION_MS
            ) {
                postInvalidateOnAnimation()
            } else {
                victoryRunning = false
            }
        }

        if (
            snapshot.puzzle
                .givenGeckos
                .isNotEmpty() ||
            snapshot.puzzle
                .givenBees
                .isNotEmpty()
        ) {
            postInvalidateOnAnimation()
        }
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
        // AliveMascotOverlayView owns both PNG fallback and video.
        // Do not render a second legacy Bee/Gecko representation here.
        return

        val puzzle =
            snapshot.puzzle

        for (cell in puzzle.cells) {
            val piece =
                snapshot
                    .pieceAt(cell)
                    ?: continue

            if (
                mediaSuppressedPieces
                    .contains(
                        cell
                    )
            ) {
                continue
            }

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

            val victoryScale =
                victoryScaleFor(
                    cell
                )

            val radius =
                baseRadius *
                    .61f *
                    pieceScale *
                    victoryScale

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
                drawGivenFog(
                    canvas =
                        canvas,
                    centerX =
                        center.first,
                    centerY =
                        center.second,
                    radius =
                        maxOf(
                            radius,
                            baseRadius *
                                .32f
                        ),
                    seed =
                        cell.q *
                            31 +
                            cell.r *
                                17
                )
            }
        }
    }

    private fun drawCrosses(
        canvas: Canvas,
        snapshot: BeeGeckoSnapshot
    ) {
        val states =
            if (
                snapshot.crossStates
                    .isNotEmpty()
            ) {
                snapshot.crossStates
            } else {
                snapshot.manualCrosses
                    .associateWith {
                        BeeGeckoCrossState
                            .IMPOSSIBLE
                    }
            }

        drawCrossStates(
            canvas,
            states
        )
    }

    private fun drawCrossStates(
        canvas: Canvas,
        states:
            Map<
                HexCoord,
                BeeGeckoCrossState
                >
    ) {
        states.forEach {
            (cell, state) ->
            val center =
                cellCenter(cell)

            val size =
                baseRadius *
                    .31f

            paint.style =
                Paint.Style.STROKE
            paint.strokeCap =
                Paint.Cap.ROUND
            paint.pathEffect =
                when (state) {
                    BeeGeckoCrossState
                        .HYPOTHESIS ->
                        DashPathEffect(
                            floatArrayOf(
                                baseRadius *
                                    .15f,
                                baseRadius *
                                    .09f
                            ),
                            0f
                        )

                    else ->
                        null
                }

            paint.strokeWidth =
                when (state) {
                    BeeGeckoCrossState
                        .HYPOTHESIS ->
                        dp(2.6f) /
                            camera.scale

                    BeeGeckoCrossState
                        .CONFIRMED ->
                        dp(3.5f) /
                            camera.scale

                    BeeGeckoCrossState
                        .IMPOSSIBLE ->
                        dp(4.3f) /
                            camera.scale
                }

            paint.color =
                when (state) {
                    BeeGeckoCrossState
                        .HYPOTHESIS ->
                        Color.rgb(
                            224,
                            166,
                            24
                        )

                    BeeGeckoCrossState
                        .CONFIRMED ->
                        Color.rgb(
                            35,
                            150,
                            65
                        )

                    BeeGeckoCrossState
                        .IMPOSSIBLE ->
                        Color.rgb(
                            190,
                            45,
                            45
                        )
                }

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

            paint.pathEffect = null
        }
    }

    private fun drawMarkers(
        canvas: Canvas,
        snapshot: BeeGeckoSnapshot
    ) {
        drawLogicalMarkers(
            canvas,
            snapshot.logicalMarkers
        )

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

    private fun drawLogicalMarkers(
        canvas: Canvas,
        logicalMarkers:
            Map<
                HexCoord,
                BeeGeckoLogicalMarks
                >,
        professor:
            Boolean = false
    ) {
        val drawnAxes =
            linkedSetOf<
                Pair<HexAxis, Int>
                >()

        logicalMarkers
            .forEach {
                (cell, marker) ->
                val center =
                    cellCenter(cell)

                if (
                    marker.geckoCandidate
                ) {
                    drawPieceMarker(
                        canvas =
                            canvas,
                        centerX =
                            center.first -
                                baseRadius *
                                    .27f,
                        centerY =
                            center.second -
                                baseRadius *
                                    .20f,
                        label = "G",
                        color =
                            Color.rgb(
                                32,
                                145,
                                65
                            )
                    )
                }

                if (
                    marker.beeCandidate
                ) {
                    drawPieceMarker(
                        canvas =
                            canvas,
                        centerX =
                            center.first +
                                baseRadius *
                                    .27f,
                        centerY =
                            center.second +
                                baseRadius *
                                    .20f,
                        label = "A",
                        color =
                            Color.rgb(
                                230,
                                170,
                                25
                            )
                    )
                }

                marker.excludedAxes
                    .forEach {
                        axis ->
                        val key =
                            axis to
                                cell.axisValue(
                                    axis
                                )

                        if (
                            drawnAxes.add(
                                key
                            )
                        ) {
                            drawGlobalAxisBar(
                                canvas =
                                    canvas,
                                origin =
                                    cell,
                                axis =
                                    axis,
                                professor =
                                    professor,
                                color =
                                    marker.colorFor(
                                        axis
                                    )
                            )
                        }
                    }
            }
    }

    private fun drawPieceMarker(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        label: String,
        color: Int
    ) {
        paint.style =
            Paint.Style.FILL
        paint.color =
            Color.argb(
                218,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )

        canvas.drawCircle(
            centerX,
            centerY,
            baseRadius *
                .18f,
            paint
        )

        paint.color =
            Color.WHITE
        paint.textAlign =
            Paint.Align.CENTER
        paint.textSize =
            baseRadius *
                .25f
        paint.isFakeBoldText =
            true

        canvas.drawText(
            label,
            centerX,
            centerY -
                (
                    paint.ascent() +
                        paint.descent()
                    ) /
                    2f,
            paint
        )

        paint.isFakeBoldText =
            false
    }

    private fun drawGlobalAxisBar(
        canvas: Canvas,
        origin: HexCoord,
        axis: HexAxis,
        professor: Boolean,
        color: AxisGuideColor
    ) {
        val board =
            puzzle
                ?: return

        val matching =
            board.cells
                .filter {
                    it.axisValue(
                        axis
                    ) ==
                        origin.axisValue(
                            axis
                        )
                }

        if (matching.size < 2) {
            return
        }

        val centers =
            matching
                .map {
                    cellCenter(it)
                }

        var first =
            centers.first()

        var last =
            centers.last()

        var maxDistance =
            -1f

        for (a in centers.indices) {
            for (
                b in
                a + 1 until
                    centers.size
            ) {
                val dx =
                    centers[a].first -
                        centers[b].first
                val dy =
                    centers[a].second -
                        centers[b].second
                val distance =
                    dx * dx +
                        dy * dy

                if (distance >
                    maxDistance
                ) {
                    maxDistance =
                        distance
                    first =
                        centers[a]
                    last =
                        centers[b]
                }
            }
        }

        paint.style =
            Paint.Style.STROKE
        paint.strokeCap =
            Paint.Cap.ROUND
        paint.strokeWidth =
            baseRadius *
                .38f
        paint.color =
            if (professor) {
                Color.argb(
                    72,
                    220,
                    118,
                    20
                )
            } else {
                when (color) {
                    AxisGuideColor.YELLOW ->
                        Color.argb(
                            72,
                            232,
                            178,
                            28
                        )

                    AxisGuideColor.GREEN ->
                        Color.argb(
                            68,
                            38,
                            156,
                            72
                        )

                    AxisGuideColor.RED ->
                        Color.argb(
                            70,
                            190,
                            45,
                            45
                        )
                }
            }

        canvas.drawLine(
            first.first,
            first.second,
            last.first,
            last.second,
            paint
        )
    }

    private fun findAxisGuideAtScreen(
        x: Float,
        y: Float
    ): Pair<HexCoord, HexAxis>? {
        if (
            !::snapshotProvider
                .isInitialized
        ) {
            return null
        }

        val touched =
            screenToCell(
                x,
                y
            )
                ?: return null

        return snapshotProvider()
            .logicalMarkers
            .entries
            .asSequence()
            .flatMap {
                (origin, marker) ->
                marker.excludedAxes
                    .asSequence()
                    .map {
                        axis ->
                        origin to
                            axis
                    }
            }
            .firstOrNull {
                (origin, axis) ->
                origin.axisValue(
                    axis
                ) ==
                    touched.axisValue(
                        axis
                    )
            }
    }

    private fun drawGivenFog(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        radius: Float,
        seed: Int
    ) {
        val phase =
            BeeGeckoFogPolicy
                .phase(
                    SystemClock
                        .uptimeMillis(),
                    seed
                )

        val scale =
            radius *
                2f

        GivenFogVisualPolicy
            .puffs(
                seed = seed,
                phase = phase
            )
            .forEach {
                puff ->
                paint.style =
                    Paint.Style.FILL
                paint.color =
                    Color.argb(
                        puff.alpha,
                        72,
                        78,
                        82
                    )

                val cx =
                    centerX +
                        puff.offsetX *
                            scale
                val cy =
                    centerY +
                        puff.offsetY *
                            scale

                canvas.drawOval(
                    RectF(
                        cx -
                            puff.halfWidth *
                                scale,
                        cy -
                            puff.halfHeight *
                                scale,
                        cx +
                            puff.halfWidth *
                                scale,
                        cy +
                            puff.halfHeight *
                                scale
                    ),
                    paint
                )
            }
    }

    private fun victoryScaleFor(
        cell: HexCoord
    ): Float {
        if (!victoryRunning) {
            return 1f
        }

        val elapsed =
            SystemClock
                .uptimeMillis() -
                victoryStartedAt

        val phase =
            elapsed /
                130f +
                cell.q *
                    .65f +
                cell.r *
                    .41f

        return 1f +
            sin(phase)
                .toFloat() *
                .13f
    }

    private fun drawProfessorProjection(
        canvas: Canvas
    ) {
        val hint =
            professorHint
                ?: return

        drawLogicalMarkers(
            canvas,
            hint.logicalMarkers,
            professor = true
        )

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
                "",
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

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val axisGuide =
                    findAxisGuideAtScreen(
                        event.x,
                        event.y
                    )

                if (axisGuide != null) {
                    draggingAxisGuide =
                        axisGuide
                    dragging = true
                    scaledGesture = false
                    longPressTriggered = false
                    cancelPendingLongPress()
                    cancelPendingSingleTap()

                    parent
                        ?.requestDisallowInterceptTouchEvent(
                            true
                        )

                    return true
                }

                scaleDetector
                    .onTouchEvent(event)

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
                if (
                    draggingAxisGuide !=
                        null
                ) {
                    return true
                }

                scaleDetector
                    .onTouchEvent(event)

                scaledGesture = true
                cancelPendingLongPress()
                cancelPendingSingleTap()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val activeGuide =
                    draggingAxisGuide

                if (activeGuide != null) {
                    val target =
                        screenToCell(
                            event.x,
                            event.y
                        )

                    if (
                        target != null &&
                        target.axisValue(
                            activeGuide.second
                        ) !=
                        activeGuide.first
                            .axisValue(
                                activeGuide.second
                            )
                    ) {
                        onAxisGuideMoved
                            ?.invoke(
                                activeGuide.first,
                                activeGuide.second,
                                target
                            )

                        draggingAxisGuide =
                            target to
                                activeGuide.second
                    }

                    invalidate()
                    return true
                }

                scaleDetector
                    .onTouchEvent(event)

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
                val activeGuide =
                    draggingAxisGuide

                if (activeGuide != null) {
                    val target =
                        screenToCell(
                            event.x,
                            event.y
                        )

                    if (target == null) {
                        onAxisGuideMoved
                            ?.invoke(
                                activeGuide.first,
                                activeGuide.second,
                                null
                            )
                    } else if (
                        target.axisValue(
                            activeGuide.second
                        ) !=
                        activeGuide.first
                            .axisValue(
                                activeGuide.second
                            )
                    ) {
                        onAxisGuideMoved
                            ?.invoke(
                                activeGuide.first,
                                activeGuide.second,
                                target
                            )
                    }

                    draggingAxisGuide =
                        null
                    dragging = false

                    parent
                        ?.requestDisallowInterceptTouchEvent(
                            false
                        )

                    invalidate()
                    performClick()
                    return true
                }

                scaleDetector
                    .onTouchEvent(event)

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
                if (
                    draggingAxisGuide !=
                        null
                ) {
                    draggingAxisGuide =
                        null

                    parent
                        ?.requestDisallowInterceptTouchEvent(
                            false
                        )
                }

                scaleDetector
                    .onTouchEvent(event)

                cancelPendingLongPress()
                cancelPendingSingleTap()
                dragging = false
                scaledGesture = false
                downCell = null
                return true
            }
        }

        scaleDetector
            .onTouchEvent(event)

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
    ): Pair<Float, Float> =
        BeeGeckoAxisGeometry
            .center(
                cell = cell,
                horizontalStep =
                    horizontalStep,
                verticalStep =
                    verticalStep
            )

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
        private const val VICTORY_DURATION_MS =
            3600L

        private const val DOUBLE_TAP_MS =
            285L

        private const val LONG_PRESS_MS =
            520L
    }
}

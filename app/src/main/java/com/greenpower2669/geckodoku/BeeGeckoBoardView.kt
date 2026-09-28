package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.abs
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

    var onTapPiece:
        ((HexCoord) -> Unit)? = null

    var onViewportChanged:
        ((BeeGeckoCamera) -> Unit)? = null

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val hexPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val pairPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

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
        dp(34f)

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

    private val dragThreshold =
        dp(10f)

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
                                        .focusY
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
            "Plateau Abeilles et Geckos. Touchez deux voisins opposés pour créer un couple, glissez pour explorer et pincez pour zoomer."
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
        centerOn(
            hint.source
        )
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

        camera =
            camera.copy(
                offsetX =
                    width /
                        2f -
                        center.first *
                            camera.scale,
                offsetY =
                    height /
                        2f -
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

        return rect
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
            ensureCamera()
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

        canvas.save()
        canvas.translate(
            camera.offsetX,
            camera.offsetY
        )
        canvas.scale(
            camera.scale,
            camera.scale
        )

        drawGrid(
            canvas,
            puzzle,
            snapshot
        )

        drawPairs(
            canvas,
            snapshot
        )

        drawPieces(
            canvas,
            puzzle,
            snapshot
        )

        drawProfessorProjection(
            canvas
        )

        canvas.restore()
    }

    private fun drawGrid(
        canvas: Canvas,
        puzzle: BeeGeckoPuzzle,
        snapshot: BeeGeckoSnapshot
    ) {
        for (
            r in
            0 until puzzle.rows
        ) {
            for (
                q in
                0 until puzzle.columns
            ) {
                val cell =
                    HexCoord(
                        q,
                        r
                    )

                val path =
                    hexPath(cell)

                val selected =
                    snapshot.selected ==
                        cell

                hexPaint.style =
                    Paint.Style.FILL

                hexPaint.color =
                    when {
                        selected ->
                            Color.rgb(
                                229,
                                245,
                                214
                            )

                        (
                            q +
                                r
                            ) % 2 ==
                            0 ->
                            Color.rgb(
                                251,
                                247,
                                221
                            )

                        else ->
                            Color.rgb(
                                243,
                                238,
                                206
                            )
                    }

                canvas.drawPath(
                    path,
                    hexPaint
                )

                hexPaint.style =
                    Paint.Style.STROKE
                hexPaint.strokeWidth =
                    if (selected) {
                        dp(3f) /
                            camera.scale
                    } else {
                        dp(1.2f) /
                            camera.scale
                    }

                hexPaint.color =
                    if (selected) {
                        Color.rgb(
                            52,
                            126,
                            70
                        )
                    } else {
                        Color.rgb(
                            145,
                            132,
                            92
                        )
                    }

                canvas.drawPath(
                    path,
                    hexPaint
                )
            }
        }
    }

    private fun drawPairs(
        canvas: Canvas,
        snapshot: BeeGeckoSnapshot
    ) {
        pairPaint.style =
            Paint.Style.STROKE
        pairPaint.strokeCap =
            Paint.Cap.ROUND
        pairPaint.strokeWidth =
            dp(6f) /
                camera.scale
        pairPaint.color =
            Color.rgb(
                76,
                113,
                61
            )

        snapshot.pairs.forEach {
            pair ->
            val g =
                cellCenter(
                    pair.gecko
                )

            val b =
                cellCenter(
                    pair.bee
                )

            canvas.drawLine(
                g.first,
                g.second,
                b.first,
                b.second,
                pairPaint
            )
        }
    }

    private fun drawPieces(
        canvas: Canvas,
        puzzle: BeeGeckoPuzzle,
        snapshot: BeeGeckoSnapshot
    ) {
        puzzle.pieces.forEach {
            (cell, piece) ->
            val center =
                cellCenter(
                    cell
                )

            val radius =
                baseRadius *
                    .66f

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
                paint.alpha =
                    if (
                        snapshot
                            .pairFor(cell) !=
                            null
                    ) {
                        255
                    } else {
                        235
                    }

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
                            60,
                            144,
                            83
                        )
                    } else {
                        Color.rgb(
                            247,
                            193,
                            48
                        )
                    }

                canvas.drawCircle(
                    center.first,
                    center.second,
                    radius *
                        .55f,
                    paint
                )
            }
        }
    }

    private fun drawProfessorProjection(
        canvas: Canvas
    ) {
        val hint =
            professorHint
                ?: return

        fun outline(
            cell: HexCoord,
            color: Int,
            widthDp: Float
        ) {
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
        }

        outline(
            hint.source,
            Color.rgb(
                35,
                93,
                176
            ),
            5f
        )

        hint.candidates.forEach {
            outline(
                it,
                Color.rgb(
                    229,
                    164,
                    25
                ),
                4f
            )
        }

        hint.excluded.forEach {
            outline(
                it,
                Color.rgb(
                    125,
                    125,
                    125
                ),
                2.5f
            )
        }

        hint.reserved.forEach {
            outline(
                it,
                Color.rgb(
                    151,
                    70,
                    156
                ),
                3.5f
            )
        }

        outline(
            hint.target,
            Color.rgb(
                38,
                152,
                64
            ),
            6f
        )

        val from =
            cellCenter(
                hint.source
            )

        val to =
            cellCenter(
                hint.target
            )

        pairPaint.color =
            Color.rgb(
                38,
                152,
                64
            )
        pairPaint.strokeWidth =
            dp(5f) /
                camera.scale

        canvas.drawLine(
            from.first,
            from.second,
            to.first,
            to.second,
            pairPaint
        )
    }

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {
        scaleDetector
            .onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent
                    ?.requestDisallowInterceptTouchEvent(
                        true
                    )

                downX = event.x
                downY = event.y
                lastX = event.x
                lastY = event.y
                dragging = false
                scaledGesture = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (
                    event.pointerCount >
                        1 ||
                    scaleDetector
                        .isInProgress
                ) {
                    return true
                }

                val distance =
                    hypot(
                        event.x -
                            downX,
                        event.y -
                            downY
                    )

                if (
                    distance >
                        dragThreshold
                ) {
                    dragging = true
                }

                if (dragging) {
                    camera =
                        BeeGeckoViewportPolicy
                            .pan(
                                camera,
                                event.x -
                                    lastX,
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
                parent
                    ?.requestDisallowInterceptTouchEvent(
                        false
                    )

                if (
                    !dragging &&
                    !scaledGesture
                ) {
                    performClick()

                    screenToCell(
                        event.x,
                        event.y
                    )
                        ?.let {
                            onTapPiece
                                ?.invoke(it)
                        }
                }

                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                parent
                    ?.requestDisallowInterceptTouchEvent(
                        false
                    )
                return true
            }
        }

        return true
    }

    private fun screenToCell(
        x: Float,
        y: Float
    ): HexCoord? {
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

        for (
            r in
            0 until puzzle.rows
        ) {
            for (
                q in
                0 until puzzle.columns
            ) {
                val cell =
                    HexCoord(
                        q,
                        r
                    )

                val center =
                    cellCenter(
                        cell
                    )

                val d =
                    hypot(
                        center.first -
                            worldX,
                        center.second -
                            worldY
                    )

                if (
                    d <
                        bestDistance
                ) {
                    bestDistance = d
                    best = cell
                }
            }
        }

        return best
            ?.takeIf {
                bestDistance <=
                    baseRadius *
                        .95f
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
                .centered(
                    viewWidth =
                        width.toFloat(),
                    viewHeight =
                        height.toFloat(),
                    content =
                        worldBounds
                )

        initializedCamera = true
        clampCamera()
        notifyViewport()
    }

    private fun clampCamera() {
        camera =
            BeeGeckoViewportPolicy
                .clamp(
                    camera = camera,
                    viewWidth =
                        width.toFloat(),
                    viewHeight =
                        height.toFloat(),
                    content =
                        worldBounds,
                    visibleMarginPx =
                        dp(56f)
                )
    }

    private fun notifyViewport() {
        onViewportChanged
            ?.invoke(camera)
    }

    private fun calculateWorldBounds(
        puzzle: BeeGeckoPuzzle
    ) {
        val left =
            -baseRadius

        val top =
            -baseRadius

        val last =
            cellCenter(
                HexCoord(
                    puzzle.columns - 1,
                    puzzle.rows - 1
                )
            )

        worldBounds =
            BeeGeckoBounds(
                left = left,
                top = top,
                right =
                    last.first +
                        baseRadius,
                bottom =
                    last.second +
                        baseRadius
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
        val center =
            cellCenter(
                cell
            )

        val path =
            Path()

        for (
            index in
            0 until 6
        ) {
            val angle =
                Math.toRadians(
                    (
                        60.0 *
                            index -
                            30.0
                        )
                )

            val x =
                center.first +
                    baseRadius *
                        cos(angle)
                            .toFloat()

            val y =
                center.second +
                    baseRadius *
                        sin(angle)
                            .toFloat()

            if (index == 0) {
                path.moveTo(
                    x,
                    y
                )
            } else {
                path.lineTo(
                    x,
                    y
                )
            }
        }

        path.close()
        return path
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
}

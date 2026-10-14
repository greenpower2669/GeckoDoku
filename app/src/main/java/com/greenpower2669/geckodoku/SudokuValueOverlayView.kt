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
    /** Affichages facultatifs : jamais d'effet sur les règles du Sudoku. */
    var showVisualProjections: Boolean = false
        set(value) { field = value; invalidate() }

    var showVisualPairs: Boolean = false
        set(value) { field = value; invalidate() }

    var showVisualTriplets: Boolean = false
        set(value) { field = value; invalidate() }

    var showVisualBinary: Boolean = false
        set(value) { field = value; invalidate() }

    var showVisualCandidates: Boolean = false
        set(value) { field = value; invalidate() }

    var showVisualCircles: Boolean = false
        set(value) { field = value; invalidate() }

    // Pulsation très lente, facultative, sans disparition ni flash.
    var animateVisualGroups: Boolean = false
        set(value) { field = value; invalidate() }

    var playerCircleLinks: Set<SudokuVisualLink> = emptySet()
        set(value) { field = value; invalidate() }

    // Repères purement graphiques placés manuellement par le joueur.
    var manualProjectionRefs: Set<SudokuCandidateRef> = emptySet()
        set(value) { field = value; invalidate() }

    var manualCandidateRefs: Set<SudokuCandidateRef> = emptySet()
        set(value) { field = value; invalidate() }

    var manualPairGroups: List<Set<Cell>> = emptyList()
        set(value) { field = value; invalidate() }

    var manualTripletGroups: List<Set<Cell>> = emptyList()
        set(value) { field = value; invalidate() }

    var manualPendingCells: Set<Cell> = emptySet()
        set(value) { field = value; invalidate() }


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

        val analyticalDisplay =
            showVisualCandidates ||
                showVisualProjections ||
                showVisualPairs ||
                showVisualTriplets ||
                showVisualBinary ||
                showVisualCircles

        // Les candidats de raisonnement sont calculés depuis la grille
        // confirmée. Ils restent strictement visuels et ne touchent jamais
        // aux notes personnelles enregistrées dans le moteur.
        val logicalCandidates =
            if (analyticalDisplay) {
                SudokuVisualReasoning
                    .logicalCandidates(
                        state.values
                    )
            } else {
                emptyMap()
            }

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

                val hypothesis =
                    state.hypothesisAt(
                        cell
                    )

                if (hypothesis != null) {
                    drawHypothesisAura(
                        canvas,
                        rect,
                        hypothesis
                    )
                }

                if (
                    hypothesis == null &&
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

                val personalCandidates =
                    linkedSetOf<Int>()
                        .apply {
                            addAll(
                                state.notesAt(
                                    cell
                                )
                            )

                            if (
                                professorCell ==
                                    cell
                            ) {
                                addAll(
                                    professorCandidates
                                )
                            }
                        }

                val analyticalCandidates =
                    logicalCandidates[
                        cell
                    ].orEmpty() -
                        personalCandidates

                if (
                    showVisualCandidates &&
                    analyticalCandidates
                        .isNotEmpty()
                ) {
                    drawCandidates(
                        canvas =
                            canvas,
                        cellRect =
                            rect,
                        cell =
                            cell,
                        candidates =
                            analyticalCandidates,
                        normalAlpha =
                            150
                    )
                }

                if (
                    personalCandidates
                        .isNotEmpty()
                ) {
                    drawCandidates(
                        canvas =
                            canvas,
                        cellRect =
                            rect,
                        cell =
                            cell,
                        candidates =
                            personalCandidates,
                        normalAlpha =
                            225
                    )
                }

                hypothesis
                    ?.let {
                        drawHypothesisDigit(
                            canvas,
                            rect,
                            it
                        )
                    }
            }
        }

        drawReasoningOverlay(
            canvas
        )

        // Annotations en surimpression, sans toucher aux notes de jeu.
        drawPlayerReasoningAnnotations(
            canvas =
                canvas,
            candidates =
                logicalCandidates
        )

        drawManualPlayerAnnotations(
            canvas
        )

        if (
            (
                animateVisualGroups &&
                (
                    showVisualPairs ||
                    showVisualTriplets
                )
            ) ||
            showVisualBinary
        ) {
            postInvalidateDelayed(
                180L
            )
        }

        if (
            animateMarker &&
            animateGeckoMarkers
        ) {
            postInvalidateDelayed(
                180L
            )
        }
    }

    /**
     * Candidats personnels uniquement : les suggestions temporaires de Pierre
     * ne servent pas de preuve ou de lien de projection.
     */
    private fun drawPlayerReasoningAnnotations(
        canvas: Canvas,
        candidates:
            Map<Cell, Set<Int>>
    ) {
        if (
            !showVisualProjections &&
            !showVisualPairs &&
            !showVisualTriplets &&
            !showVisualBinary &&
            !showVisualCircles
        ) {
            return
        }

        fun targetPoint(
            ref: SudokuCandidateRef
        ): Pair<Float, Float> {
            val cell =
                cellRect(
                    ref.cell
                )

            val slot =
                SudokuCandidateLayout
                    .slot(
                        ref.digit
                    )

            return (
                cell.left +
                    (
                        slot.left +
                            slot.right
                        ) *
                        cell.width() /
                        2f
                ) to
                (
                    cell.top +
                        (
                            slot.top +
                                slot.bottom
                            ) *
                            cell.height() /
                            2f
                    )
        }

        paint.style =
            Paint.Style.STROKE
        paint.strokeCap =
            Paint.Cap.ROUND
        paint.pathEffect =
            null

        if (showVisualProjections) {
            val links =
                SudokuVisualReasoning
                    .projectionSegments(
                        candidates
                    )

            paint.strokeWidth =
                (
                    cellSize *
                        .028f
                    ).coerceAtLeast(
                    dp(
                        1.0f
                    )
                )

            links.forEach {
                link ->

                val (
                    x0,
                    y0
                ) =
                    targetPoint(
                        link.first
                    )

                val (
                    x1,
                    y1
                ) =
                    targetPoint(
                        link.second
                    )

                val hue =
                    (
                        link.first.digit *
                            37
                        ) %
                        255

                // Très discret : la projection sert de repère, pas de mur.
                paint.color =
                    Color.argb(
                        72,
                        25 +
                            hue /
                                4,
                        75 +
                            hue /
                                3,
                        135
                    )

                if (
                    link.first.cell.row ==
                        link.second.cell.row
                ) {
                    // Même ligne : l'axe traverse toute la grille.
                    canvas.drawLine(
                        boardRect.left,
                        y0,
                        boardRect.right,
                        y0,
                        paint
                    )
                } else if (
                    link.first.cell.col ==
                        link.second.cell.col
                ) {
                    // Même colonne : l'axe traverse toute la grille.
                    canvas.drawLine(
                        x0,
                        boardRect.top,
                        x0,
                        boardRect.bottom,
                        paint
                    )
                }

                // Les deux extrémités logiques restent repérées avec un
                // petit anneau afin de ne pas dépendre de la couleur seule.
                paint.color =
                    Color.argb(
                        120,
                        Color.red(
                            paint.color
                        ),
                        Color.green(
                            paint.color
                        ),
                        Color.blue(
                            paint.color
                        )
                    )

                canvas.drawCircle(
                    x0,
                    y0,
                    cellSize *
                        .07f,
                    paint
                )

                canvas.drawCircle(
                    x1,
                    y1,
                    cellSize *
                        .07f,
                    paint
                )
            }
        }

        // Pas de flash rapide : la pulsation est lente, douce et ne fait
        // jamais disparaître un repère.
        for (
            (enabled, size) in
                listOf(
                    showVisualPairs to
                        2,
                    showVisualTriplets to
                        3
                )
        ) {
            if (!enabled) {
                continue
            }

            val alpha =
                if (
                    animateVisualGroups
                ) {
                    val phase =
                        (
                            SystemClock
                                .uptimeMillis() %
                                3000L
                            ).toDouble() /
                            3000.0 *
                            (
                                2.0 *
                                    Math.PI
                                )

                    (
                        170 +
                            45 *
                            sin(
                                phase
                            )
                        ).toInt()
                        .coerceIn(
                            125,
                            215
                        )
                } else {
                    195
                }

            paint.color =
                if (size == 2) {
                    Color.argb(
                        alpha,
                        22,
                        105,
                        160
                    )
                } else {
                    Color.argb(
                        alpha,
                        143,
                        64,
                        150
                    )
                }

            paint.strokeWidth =
                (
                    cellSize *
                        .055f
                    ).coerceAtLeast(
                    dp(
                        1.5f
                    )
                )

            paint.pathEffect =
                if (size == 3) {
                    android.graphics
                        .DashPathEffect(
                            floatArrayOf(
                                cellSize *
                                    .12f,
                                cellSize *
                                    .07f
                            ),
                            0f
                        )
                } else {
                    null
                }

            val highlighted =
                SudokuVisualReasoning
                    .nakedGroups(
                        candidates,
                        size
                    )
                    .flatMap {
                        it.cells
                    }
                    .toSet()

            highlighted.forEach {
                cell ->

                val rect =
                    cellRect(
                        cell
                    )

                val inset =
                    cellSize *
                        .055f

                canvas.drawRoundRect(
                    rect.left +
                        inset,
                    rect.top +
                        inset,
                    rect.right -
                        inset,
                    rect.bottom -
                        inset,
                    cellSize *
                        .12f,
                    cellSize *
                        .12f,
                    paint
                )
            }

            paint.pathEffect =
                null
        }

        if (showVisualBinary) {
            val phase =
                (
                    SystemClock
                        .uptimeMillis() %
                        2400L
                    ).toDouble() /
                    2400.0 *
                    (
                        2.0 *
                            Math.PI
                        )

            val alpha =
                (
                    185 +
                        55 *
                        sin(
                            phase
                        )
                    ).toInt()
                    .coerceIn(
                        120,
                        240
                    )

            paint.strokeWidth =
                (
                    cellSize *
                        .065f
                    ).coerceAtLeast(
                    dp(
                        1.8f
                    )
                )

            paint.pathEffect =
                null

            SudokuVisualReasoning
                .binaryGroups(
                    candidates
                )
                .forEach {
                    group ->

                    val hue =
                        (
                            group.digit *
                                41
                            ) %
                            255

                    paint.color =
                        Color.argb(
                            alpha,
                            30 +
                                hue /
                                    5,
                            95 +
                                hue /
                                    4,
                            42 +
                                hue /
                                    3
                        )

                    group.cells
                        .forEach {
                            cell ->

                            val (
                                x,
                                y
                            ) =
                                targetPoint(
                                    SudokuCandidateRef(
                                        cell =
                                            cell,
                                        digit =
                                            group.digit
                                    )
                                )

                            canvas.drawCircle(
                                x,
                                y,
                                cellSize *
                                    .125f,
                                paint
                            )

                            // Petit second anneau : lisible sans dépendre
                            // uniquement de la couleur.
                            canvas.drawCircle(
                                x,
                                y,
                                cellSize *
                                    .155f,
                                paint
                            )
                        }
                }
        }

        if (showVisualCircles) {
            paint.strokeWidth =
                (
                    cellSize *
                        .055f
                    ).coerceAtLeast(
                    dp(
                        1.4f
                    )
                )

            playerCircleLinks
                .forEachIndexed {
                    index,
                    link ->

                    if (
                        link.first.digit !in
                            candidates[
                                link.first.cell
                            ].orEmpty() ||
                        link.second.digit !in
                            candidates[
                                link.second.cell
                            ].orEmpty()
                    ) {
                        return@forEachIndexed
                    }

                    val colors =
                        intArrayOf(
                            Color.rgb(
                                15,
                                108,
                                171
                            ),
                            Color.rgb(
                                145,
                                48,
                                143
                            ),
                            Color.rgb(
                                30,
                                127,
                                75
                            ),
                            Color.rgb(
                                165,
                                97,
                                17
                            )
                        )

                    val color =
                        colors[
                            index %
                                colors.size
                            ]

                    val (
                        x0,
                        y0
                    ) =
                        targetPoint(
                            link.first
                        )

                    val (
                        x1,
                        y1
                    ) =
                        targetPoint(
                            link.second
                        )

                    paint.color =
                        Color.argb(
                            110,
                            Color.red(
                                color
                            ),
                            Color.green(
                                color
                            ),
                            Color.blue(
                                color
                            )
                        )

                    canvas.drawLine(
                        x0,
                        y0,
                        x1,
                        y1,
                        paint
                    )

                    paint.color =
                        Color.argb(
                            220,
                            Color.red(
                                color
                            ),
                            Color.green(
                                color
                            ),
                            Color.blue(
                                color
                            )
                        )

                    canvas.drawCircle(
                        x0,
                        y0,
                        cellSize *
                            .105f,
                        paint
                    )

                    canvas.drawCircle(
                        x1,
                        y1,
                        cellSize *
                            .105f,
                        paint
                    )
                }
        }

        paint.pathEffect =
            null
        paint.style =
            Paint.Style.FILL
    }

    private fun drawManualPlayerAnnotations(
        canvas: Canvas
    ) {
        if (
            manualProjectionRefs.isEmpty() &&
            manualCandidateRefs.isEmpty() &&
            manualPairGroups.isEmpty() &&
            manualTripletGroups.isEmpty() &&
            manualPendingCells.isEmpty()
        ) {
            return
        }

        fun targetRect(
            ref: SudokuCandidateRef
        ): RectF {
            val cell =
                cellRect(
                    ref.cell
                )
            val slot =
                SudokuCandidateLayout
                    .slot(
                        ref.digit
                    )

            return RectF(
                cell.left +
                    slot.left *
                        cell.width(),
                cell.top +
                    slot.top *
                        cell.height(),
                cell.left +
                    slot.right *
                        cell.width(),
                cell.top +
                    slot.bottom *
                        cell.height()
            )
        }

        fun targetPoint(
            ref: SudokuCandidateRef
        ): Pair<Float, Float> {
            val rect =
                targetRect(
                    ref
                )

            return rect.centerX() to
                rect.centerY()
        }

        fun drawManualDigit(
            ref: SudokuCandidateRef
        ) {
            val rect =
                targetRect(
                    ref
                )
            val inset =
                rect.width() *
                    .10f

            renderer.draw(
                canvas =
                    canvas,
                target =
                    RectF(
                        rect.left +
                            inset,
                        rect.top +
                            inset,
                        rect.right -
                            inset,
                        rect.bottom -
                            inset
                    ),
                digit =
                    ref.digit,
                style =
                    candidateVisualPolicy
                        .styleForCandidate(
                            visualStyle
                        ),
                mini =
                    true,
                alpha =
                    215
            )
        }

        paint.style =
            Paint.Style.STROKE
        paint.strokeCap =
            Paint.Cap.ROUND
        paint.pathEffect =
            null

        // Projection humaine : croix complète ligne + colonne depuis le
        // candidat choisi par le joueur. Aucun calcul de solution ici.
        paint.strokeWidth =
            (
                cellSize *
                    .026f
                ).coerceAtLeast(
                dp(
                    1f
                )
            )

        manualProjectionRefs
            .forEachIndexed {
                index,
                ref ->

                drawManualDigit(
                    ref
                )

                val (
                    x,
                    y
                ) =
                    targetPoint(
                        ref
                    )

                val colors =
                    intArrayOf(
                        Color.rgb(
                            35,
                            104,
                            165
                        ),
                        Color.rgb(
                            123,
                            78,
                            153
                        ),
                        Color.rgb(
                            46,
                            124,
                            83
                        )
                    )

                val color =
                    colors[
                        index %
                            colors.size
                        ]

                paint.color =
                    Color.argb(
                        66,
                        Color.red(
                            color
                        ),
                        Color.green(
                            color
                        ),
                        Color.blue(
                            color
                        )
                    )

                canvas.drawLine(
                    boardRect.left,
                    y,
                    boardRect.right,
                    y,
                    paint
                )

                canvas.drawLine(
                    x,
                    boardRect.top,
                    x,
                    boardRect.bottom,
                    paint
                )

                paint.color =
                    Color.argb(
                        190,
                        Color.red(
                            color
                        ),
                        Color.green(
                            color
                        ),
                        Color.blue(
                            color
                        )
                    )

                canvas.drawCircle(
                    x,
                    y,
                    cellSize *
                        .085f,
                    paint
                )
            }

        // Candidat manuel : simple pièce graphique autour du petit chiffre.
        paint.strokeWidth =
            (
                cellSize *
                    .05f
                ).coerceAtLeast(
                    dp(
                        1.4f
                    )
                )
        paint.color =
            Color.argb(
                205,
                33,
                105,
                82
            )

        manualCandidateRefs
            .forEach {
                ref ->

                drawManualDigit(
                    ref
                )

                val (
                    x,
                    y
                ) =
                    targetPoint(
                        ref
                    )

                canvas.drawCircle(
                    x,
                    y,
                    cellSize *
                        .11f,
                    paint
                )
            }

        fun drawCellGroups(
            groups: List<Set<Cell>>,
            triplet: Boolean
        ) {
            paint.strokeWidth =
                (
                    cellSize *
                        .052f
                    ).coerceAtLeast(
                    dp(
                        1.5f
                    )
                )

            paint.color =
                if (triplet) {
                    Color.argb(
                        190,
                        139,
                        70,
                        149
                    )
                } else {
                    Color.argb(
                        190,
                        21,
                        109,
                        162
                    )
                }

            paint.pathEffect =
                if (triplet) {
                    android.graphics
                        .DashPathEffect(
                            floatArrayOf(
                                cellSize *
                                    .12f,
                                cellSize *
                                    .07f
                            ),
                            0f
                        )
                } else {
                    null
                }

            groups.forEach {
                group ->
                group.forEach {
                    cell ->

                    val rect =
                        cellRect(
                            cell
                        )
                    val inset =
                        cellSize *
                            .07f

                    canvas.drawRoundRect(
                        rect.left +
                            inset,
                        rect.top +
                            inset,
                        rect.right -
                            inset,
                        rect.bottom -
                            inset,
                        cellSize *
                            .11f,
                        cellSize *
                            .11f,
                        paint
                    )
                }
            }

            paint.pathEffect =
                null
        }

        drawCellGroups(
            manualPairGroups,
            false
        )
        drawCellGroups(
            manualTripletGroups,
            true
        )

        if (
            manualPendingCells
                .isNotEmpty()
        ) {
            paint.strokeWidth =
                (
                    cellSize *
                        .035f
                    ).coerceAtLeast(
                    dp(
                        1.2f
                    )
                )
            paint.color =
                Color.argb(
                    150,
                    80,
                    80,
                    80
                )
            paint.pathEffect =
                android.graphics
                    .DashPathEffect(
                        floatArrayOf(
                            cellSize *
                                .09f,
                            cellSize *
                                .06f
                        ),
                        0f
                    )

            manualPendingCells
                .forEach {
                    cell ->

                    val rect =
                        cellRect(
                            cell
                        )
                    val inset =
                        cellSize *
                            .11f

                    canvas.drawRoundRect(
                        rect.left +
                            inset,
                        rect.top +
                            inset,
                        rect.right -
                            inset,
                        rect.bottom -
                            inset,
                        cellSize *
                            .09f,
                        cellSize *
                            .09f,
                        paint
                    )
                }
        }

        paint.pathEffect =
            null
        paint.style =
            Paint.Style.FILL
    }

    private fun drawHypothesisAura(
        canvas: Canvas,
        rect: RectF,
        node:
            HypothesisNode<
                SudokuHypothesisChoice
                >
    ) {
        val color =
            node.color

        paint.style =
            Paint.Style.STROKE
        paint.strokeWidth =
            cellSize *
                .065f
        paint.color =
            Color.argb(
                225,
                color.red,
                color.green,
                color.blue
            )

        canvas.drawCircle(
            rect.centerX(),
            rect.centerY(),
            cellSize *
                .42f,
            paint
        )

        paint.strokeWidth =
            cellSize *
                .12f
        paint.color =
            Color.argb(
                52,
                color.red,
                color.green,
                color.blue
            )

        canvas.drawCircle(
            rect.centerX(),
            rect.centerY(),
            cellSize *
                .42f,
            paint
        )
    }

    private fun drawHypothesisDigit(
        canvas: Canvas,
        rect: RectF,
        node:
            HypothesisNode<
                SudokuHypothesisChoice
                >
    ) {
        val target =
            RectF(
                rect.left +
                    rect.width() *
                        .20f,
                rect.top +
                    rect.height() *
                        .17f,
                rect.right -
                    rect.width() *
                        .20f,
                rect.bottom -
                    rect.height() *
                        .17f
            )

        renderer.draw(
            canvas =
                canvas,
            target =
                target,
            digit =
                node.cell.digit,
            style =
                visualStyle,
            given =
                false,
            mini =
                false,
            alpha =
                if (
                    node.state ==
                        HypothesisBranchState
                            .CONTRADICTION
                ) {
                    135
                } else {
                    215
                }
        )

        if (
            node.state ==
                HypothesisBranchState
                    .CONTRADICTION
        ) {
            paint.style =
                Paint.Style.STROKE
            paint.strokeWidth =
                cellSize *
                    .065f
            paint.strokeCap =
                Paint.Cap.ROUND
            paint.color =
                Color.rgb(
                    172,
                    28,
                    28
                )

            canvas.drawCircle(
                rect.centerX(),
                rect.centerY(),
                cellSize *
                    .31f,
                paint
            )

            canvas.drawLine(
                rect.left +
                    cellSize *
                        .22f,
                rect.bottom -
                    cellSize *
                        .22f,
                rect.right -
                    cellSize *
                        .22f,
                rect.top +
                    cellSize *
                        .22f,
                paint
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
                cellSize * .052f
            paint.color =
                Color.argb(
                    135,
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
        candidates: Set<Int>,
        normalAlpha: Int
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
                        normalAlpha
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

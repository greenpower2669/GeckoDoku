package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

data class SudokuHypothesisDigitVisual(
    val color: HypothesisColor,
    val state: HypothesisBranchState
)

class SudokuQuickPaletteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    var visualStyle:
        SudokuVisualStyle =
        SudokuVisualStyle
            .CLASSIC_NUMBERS
        set(value) {
            field = value
            invalidate()
        }

    var onValueDigit:
        ((Int) -> Unit)? = null

    var onCandidateDigit:
        ((Int) -> Unit)? = null

    var onHypothesisDigit:
        ((Int) -> Unit)? = null

    var onConfirmValue:
        ((Int) -> Unit)? = null

    var onRejectValue:
        (() -> Unit)? = null

    var onClose:
        (() -> Unit)? = null

    var onHelpPanel:
        ((SudokuPalettePanel) -> Unit)? =
        null

    var onDragDelta:
        ((Float, Float) -> Unit)? =
        null

    var onResizeDelta:
        ((Float, Float) -> Unit)? =
        null

    private var activeCandidates:
        Set<Int> =
        emptySet()

    private var hypothesisDigits:
        Map<
            Int,
            SudokuHypothesisDigitVisual
            > =
        emptyMap()

    private var nextHypothesisColor =
        HypothesisColor.YELLOW

    private var pendingValueDigit:
        Int? = null

    private var currentValueDigit:
        Int? = null

    private var helpMode =
        false

    private var dragging =
        false

    private var resizing =
        false

    private var dragMoved =
        false

    private var lastRawX =
        0f

    private var lastRawY =
        0f

    private val layoutPolicy =
        SudokuPaletteLayoutPolicy()

    private val renderer =
        SudokuDigitRenderer(
            context
        )

    private val candidateVisualPolicy =
        SudokuCandidateVisualPolicy()

    private val paint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        )

    init {
        isClickable = true
        isFocusable = true
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_YES

        contentDescription =
            "Palette Sudoku déplaçable. Quatre zones : choix, candidats, hypothèse et prévisualisation."
    }

    fun setActiveCandidates(
        candidates: Set<Int>
    ) {
        activeCandidates =
            candidates.filter {
                it in 1..9
            }
                .toSet()

        invalidate()
    }

    fun setHypothesisDigits(
        digits:
            Map<
                Int,
                SudokuHypothesisDigitVisual
                >,
        nextColor:
            HypothesisColor
    ) {
        hypothesisDigits =
            digits.filterKeys {
                it in 1..9
            }

        nextHypothesisColor =
            nextColor

        invalidate()
    }

    fun setCurrentValue(
        digit: Int?
    ) {
        currentValueDigit =
            digit
                ?.takeIf {
                    it in 1..9
                }

        invalidate()
    }

    fun setHelpMode(
        active: Boolean
    ) {
        helpMode =
            active

        contentDescription =
            if (active) {
                "Aide Sudoku. Touchez Choix, Candidats, Hypothèse ou Prévisu pour entendre l'explication du Prof."
            } else {
                "Palette Sudoku persistante et déplaçable. Quatre zones : choix, candidats, hypothèse et prévisualisation."
            }

        invalidate()
    }

    fun setPendingValue(
        digit: Int?
    ) {
        pendingValueDigit =
            digit
                ?.takeIf {
                    it in 1..9
                }

        invalidate()
    }

    override fun performClick():
        Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {
        val confirmationActive =
            pendingValueDigit !=
                null

        val headerHeight =
            layoutPolicy
                .headerHeight(
                    height
                )

        when (
            event.actionMasked
        ) {
            MotionEvent.ACTION_DOWN -> {
                if (
                    isResizeHandle(
                        event.x,
                        event.y
                    )
                ) {
                    resizing = true
                    dragMoved = false
                    lastRawX =
                        event.rawX
                    lastRawY =
                        event.rawY

                    parent
                        ?.requestDisallowInterceptTouchEvent(
                            true
                        )

                    return true
                }

                if (
                    !confirmationActive &&
                    event.y <
                        headerHeight
                ) {
                    val closeAction =
                        layoutPolicy
                            .actionAt(
                                x =
                                    event.x,
                                y =
                                    event.y,
                                width =
                                    width,
                                height =
                                    height,
                                confirmationActive =
                                    false
                            )

                    if (
                        closeAction !=
                            SudokuPaletteAction
                                .Close &&
                        closeAction !=
                            SudokuPaletteAction
                                .Help
                    ) {
                        dragging = true
                        dragMoved =
                            false
                        lastRawX =
                            event.rawX
                        lastRawY =
                            event.rawY
                        parent
                            ?.requestDisallowInterceptTouchEvent(
                                true
                            )

                        return true
                    }
                }

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (resizing) {
                    val dx =
                        event.rawX -
                            lastRawX

                    val dy =
                        event.rawY -
                            lastRawY

                    lastRawX =
                        event.rawX

                    lastRawY =
                        event.rawY

                    onResizeDelta
                        ?.invoke(
                            dx,
                            dy
                        )

                    return true
                }

                if (dragging) {
                    val dx =
                        event.rawX -
                            lastRawX

                    val dy =
                        event.rawY -
                            lastRawY

                    if (
                        abs(dx) >
                            1f ||
                        abs(dy) >
                            1f
                    ) {
                        dragMoved =
                            true
                    }

                    lastRawX =
                        event.rawX
                    lastRawY =
                        event.rawY

                    onDragDelta
                        ?.invoke(
                            dx,
                            dy
                        )

                    return true
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                stopInteraction()
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (resizing) {
                    stopInteraction()
                    performClick()
                    return true
                }

                if (dragging) {
                    val moved =
                        dragMoved

                    stopInteraction()

                    if (moved) {
                        performClick()
                        return true
                    }
                }

                if (
                    helpMode &&
                    event.y >=
                        headerHeight
                ) {
                    layoutPolicy
                        .panelAt(
                            x =
                                event.x,
                            y =
                                event.y,
                            width =
                                width,
                            height =
                                height
                        )
                        ?.let {
                            onHelpPanel
                                ?.invoke(
                                    it
                                )
                        }

                    performClick()
                    return true
                }

                when (
                    val action =
                        layoutPolicy
                            .actionAt(
                                x =
                                    event.x,
                                y =
                                    event.y,
                                width =
                                    width,
                                height =
                                    height,
                                confirmationActive =
                                    confirmationActive
                            )
                ) {
                    is SudokuPaletteAction
                        .Value ->
                        onValueDigit
                            ?.invoke(
                                action.digit
                            )

                    is SudokuPaletteAction
                        .Candidate ->
                        onCandidateDigit
                            ?.invoke(
                                action.digit
                            )

                    is SudokuPaletteAction
                        .Hypothesis ->
                        onHypothesisDigit
                            ?.invoke(
                                action.digit
                            )

                    SudokuPaletteAction
                        .ConfirmYes ->
                        pendingValueDigit
                            ?.let {
                                onConfirmValue
                                    ?.invoke(
                                        it
                                    )
                            }

                    SudokuPaletteAction
                        .ConfirmNo ->
                        onRejectValue
                            ?.invoke()

                    SudokuPaletteAction
                        .Help -> {
                        setHelpMode(
                            !helpMode
                        )
                    }

                    SudokuPaletteAction
                        .Close ->
                        onClose
                            ?.invoke()

                    null ->
                        Unit
                }

                performClick()
                return true
            }
        }

        return true
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(
            canvas
        )

        canvas.drawColor(
            Color.rgb(
                250,
                251,
                248
            )
        )

        drawHeader(
            canvas
        )

        drawPanelBackgrounds(
            canvas
        )

        for (
            digit in
            1..9
        ) {
            drawDigitTile(
                canvas =
                    canvas,
                digit =
                    digit,
                panel =
                    SudokuPalettePanel
                        .VALUE
            )

            drawDigitTile(
                canvas =
                    canvas,
                digit =
                    digit,
                panel =
                    SudokuPalettePanel
                        .CANDIDATE
            )

            drawDigitTile(
                canvas =
                    canvas,
                digit =
                    digit,
                panel =
                    SudokuPalettePanel
                        .HYPOTHESIS
            )
        }

        drawPreview(
            canvas
        )

        drawResizeHandle(
            canvas
        )
    }

    private fun drawHeader(
        canvas: Canvas
    ) {
        val header =
            layoutPolicy
                .headerHeight(
                    height
                )
                .toFloat()

        paint.style =
            Paint.Style.FILL

        paint.color =
            Color.rgb(
                57,
                74,
                62
            )

        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            header,
            paint
        )

        paint.textAlign =
            Paint.Align.LEFT
        paint.isFakeBoldText =
            true
        paint.textSize =
            header *
                .42f
        paint.color =
            Color.WHITE

        canvas.drawText(
            if (helpMode) {
                "Aide • touche une zone"
            } else {
                "Sudoku • glisser ici"
            },
            header *
                .35f,
            header /
                2f -
                (
                    paint.ascent() +
                        paint.descent()
                    ) /
                    2f,
            paint
        )

        if (
            pendingValueDigit ==
                null
        ) {
            val help =
                layoutPolicy
                    .helpBounds(
                        width,
                        height
                    )

            paint.textAlign =
                Paint.Align.CENTER
            paint.textSize =
                header *
                    .48f

            canvas.drawText(
                "?",
                (
                    help.left +
                        help.right
                    ) /
                    2f,
                (
                    help.top +
                        help.bottom
                    ) /
                    2f -
                    (
                        paint.ascent() +
                            paint.descent()
                    ) /
                    2f,
                paint
            )

            val close =
                layoutPolicy
                    .closeBounds(
                        width,
                        height
                    )

            paint.textAlign =
                Paint.Align.CENTER
            paint.textSize =
                header *
                    .58f

            canvas.drawText(
                "×",
                (
                    close.left +
                        close.right
                    ) /
                    2f,
                (
                    close.top +
                        close.bottom
                    ) /
                    2f -
                    (
                        paint.ascent() +
                            paint.descent()
                        ) /
                        2f,
                paint
            )
        }

        paint.isFakeBoldText =
            false
    }

    private fun drawPanelBackgrounds(
        canvas: Canvas
    ) {
        SudokuPalettePanel
            .entries
            .forEach {
                panel ->

                val bounds =
                    layoutPolicy
                        .panelBounds(
                            panel,
                            width,
                            height
                        )

                val rect =
                    RectF(
                        bounds.left
                            .toFloat(),
                        bounds.top
                            .toFloat(),
                        bounds.right
                            .toFloat(),
                        bounds.bottom
                            .toFloat()
                    )

                paint.style =
                    Paint.Style.FILL

                paint.color =
                    when (panel) {
                        SudokuPalettePanel
                            .VALUE ->
                            Color.rgb(
                                244,
                                248,
                                244
                            )

                        SudokuPalettePanel
                            .CANDIDATE ->
                            Color.rgb(
                                239,
                                246,
                                241
                            )

                        SudokuPalettePanel
                            .HYPOTHESIS ->
                            Color.rgb(
                                (
                                    nextHypothesisColor
                                        .red +
                                        238
                                    ) /
                                    2,
                                (
                                    nextHypothesisColor
                                        .green +
                                        238
                                    ) /
                                    2,
                                (
                                    nextHypothesisColor
                                        .blue +
                                        238
                                    ) /
                                    2
                            )

                        SudokuPalettePanel
                            .PREVIEW ->
                            Color.rgb(
                                247,
                                244,
                                238
                            )
                    }

                canvas.drawRect(
                    rect,
                    paint
                )

                paint.style =
                    Paint.Style.STROKE
                paint.strokeWidth =
                    dp(
                        1.2f
                    )
                paint.color =
                    Color.rgb(
                        105,
                        112,
                        106
                    )

                canvas.drawRect(
                    rect,
                    paint
                )

                paint.style =
                    Paint.Style.FILL
                paint.isFakeBoldText =
                    true
                paint.textAlign =
                    Paint.Align.CENTER
                paint.textSize =
                    rect.height() *
                        .095f
                paint.color =
                    Color.rgb(
                        43,
                        54,
                        46
                    )

                val title =
                    when (panel) {
                        SudokuPalettePanel
                            .VALUE ->
                            "Choix"

                        SudokuPalettePanel
                            .CANDIDATE ->
                            "Candidats"

                        SudokuPalettePanel
                            .HYPOTHESIS ->
                            "Hypothèse • " +
                                nextHypothesisColor
                                    .label

                        SudokuPalettePanel
                            .PREVIEW ->
                            "Prévisu"
                    }

                canvas.drawText(
                    title,
                    rect.centerX(),
                    rect.top +
                        rect.height() *
                            .13f -
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
    }

    private fun drawDigitTile(
        canvas: Canvas,
        digit: Int,
        panel: SudokuPalettePanel
    ) {
        val bounds =
            layoutPolicy
                .tileBounds(
                    digit =
                        digit,
                    panel =
                        panel,
                    width =
                        width,
                    height =
                        height
                )

        val rect =
            RectF(
                bounds.left
                    .toFloat(),
                bounds.top
                    .toFloat(),
                bounds.right
                    .toFloat(),
                bounds.bottom
                    .toFloat()
            )

        val inset =
            rect.width() *
                .07f

        val inner =
            RectF(
                rect.left +
                    inset,
                rect.top +
                    inset,
                rect.right -
                    inset,
                rect.bottom -
                    inset
            )

        paint.style =
            Paint.Style.FILL

        val hypothesis =
            hypothesisDigits[
                digit
                ]

        paint.color =
            when {
                panel ==
                    SudokuPalettePanel
                        .CANDIDATE &&
                    digit in
                        activeCandidates ->
                    Color.rgb(
                        205,
                        236,
                        211
                    )

                panel ==
                    SudokuPalettePanel
                        .HYPOTHESIS &&
                    hypothesis !=
                        null ->
                    Color.rgb(
                        hypothesis
                            .color
                            .red,
                        hypothesis
                            .color
                            .green,
                        hypothesis
                            .color
                            .blue
                    )

                panel ==
                    SudokuPalettePanel
                        .HYPOTHESIS ->
                    Color.argb(
                        92,
                        nextHypothesisColor
                            .red,
                        nextHypothesisColor
                            .green,
                        nextHypothesisColor
                            .blue
                    )

                else ->
                    Color.rgb(
                        242,
                        245,
                        241
                    )
            }

        canvas.drawRoundRect(
            inner,
            inner.width() *
                .12f,
            inner.width() *
                .12f,
            paint
        )

        renderer.draw(
            canvas =
                canvas,
            target =
                RectF(
                    inner.left +
                        inner.width() *
                            .10f,
                    inner.top +
                        inner.height() *
                            .10f,
                    inner.right -
                        inner.width() *
                            .10f,
                    inner.bottom -
                        inner.height() *
                            .10f
                ),
            digit =
                digit,
            style =
                if (
                    panel ==
                        SudokuPalettePanel
                            .CANDIDATE
                ) {
                    candidateVisualPolicy
                        .styleForCandidate(
                            visualStyle
                        )
                } else {
                    visualStyle
                },
            given =
                false,
            mini =
                panel !=
                    SudokuPalettePanel
                        .VALUE
        )

        if (
            panel ==
                SudokuPalettePanel
                    .HYPOTHESIS &&
            hypothesis
                ?.state ==
                HypothesisBranchState
                    .CONTRADICTION
        ) {
            paint.style =
                Paint.Style.STROKE
            paint.strokeWidth =
                inner.width() *
                    .10f
            paint.color =
                Color.rgb(
                    156,
                    28,
                    28
                )

            canvas.drawLine(
                inner.left +
                    inner.width() *
                        .14f,
                inner.bottom -
                    inner.height() *
                        .14f,
                inner.right -
                    inner.width() *
                        .14f,
                inner.top +
                    inner.height() *
                        .14f,
                paint
            )
        }
    }

    private fun drawPreview(
        canvas: Canvas
    ) {
        val bounds =
            layoutPolicy
                .panelBounds(
                    SudokuPalettePanel
                        .PREVIEW,
                    width,
                    height
                )

        val rect =
            RectF(
                bounds.left
                    .toFloat(),
                bounds.top
                    .toFloat(),
                bounds.right
                    .toFloat(),
                bounds.bottom
                    .toFloat()
            )

        val digit =
            pendingValueDigit
                ?: currentValueDigit

        if (digit != null) {
            renderer.draw(
                canvas =
                    canvas,
                target =
                    RectF(
                        rect.left +
                            rect.width() *
                                .30f,
                        rect.top +
                            rect.height() *
                                .23f,
                        rect.right -
                            rect.width() *
                                .30f,
                        rect.top +
                            rect.height() *
                                .58f
                    ),
                digit =
                    digit,
                style =
                    visualStyle,
                given =
                    false,
                mini =
                    false
            )
        }

        if (
            pendingValueDigit ==
                null
        ) {
            paint.style =
                Paint.Style.FILL
            paint.textAlign =
                Paint.Align.CENTER
            paint.textSize =
                rect.height() *
                    .085f
            paint.color =
                Color.rgb(
                    72,
                    77,
                    73
                )

            canvas.drawText(
                if (digit == null) {
                    "Choisis un nombre"
                } else {
                    "Valeur actuelle"
                },
                rect.centerX(),
                rect.bottom -
                    rect.height() *
                        .10f,
                paint
            )

            return
        }

        paint.style =
            Paint.Style.FILL
        paint.textAlign =
            Paint.Align.CENTER
        paint.isFakeBoldText =
            true
        paint.textSize =
            rect.height() *
                .09f
        paint.color =
            Color.rgb(
                54,
                58,
                55
            )

        canvas.drawText(
            "Êtes-vous sûr ?",
            rect.centerX(),
            rect.top +
                rect.height() *
                    .60f,
            paint
        )

        val yes =
            layoutPolicy
                .confirmYesBounds(
                    width,
                    height
                )

        val no =
            layoutPolicy
                .confirmNoBounds(
                    width,
                    height
                )

        drawConfirmationButton(
            canvas =
                canvas,
            box =
                yes,
            label =
                "Oui"
        )

        drawConfirmationButton(
            canvas =
                canvas,
            box =
                no,
            label =
                "Non"
        )

        paint.isFakeBoldText =
            false
    }

    private fun drawConfirmationButton(
        canvas: Canvas,
        box: PixelBox,
        label: String
    ) {
        val rect =
            RectF(
                box.left
                    .toFloat(),
                box.top
                    .toFloat(),
                box.right
                    .toFloat(),
                box.bottom
                    .toFloat()
            )

        val inset =
            dp(
                4f
            )

        val inner =
            RectF(
                rect.left +
                    inset,
                rect.top +
                    inset,
                rect.right -
                    inset,
                rect.bottom -
                    inset
            )

        paint.style =
            Paint.Style.FILL
        paint.color =
            Color.rgb(
                225,
                232,
                226
            )

        canvas.drawRoundRect(
            inner,
            dp(
                8f
            ),
            dp(
                8f
            ),
            paint
        )

        paint.textAlign =
            Paint.Align.CENTER
        paint.textSize =
            inner.height() *
                .34f
        paint.color =
            Color.rgb(
                42,
                52,
                44
            )

        canvas.drawText(
            label,
            inner.centerX(),
            inner.centerY() -
                (
                    paint.ascent() +
                        paint.descent()
                    ) /
                    2f,
            paint
        )
    }

    private fun drawResizeHandle(
        canvas: Canvas
    ) {
        val size =
            dp(
                30f
            )

        val right =
            width.toFloat() -
                dp(
                    5f
                )

        val bottom =
            height.toFloat() -
                dp(
                    5f
                )

        paint.style =
            Paint.Style.STROKE

        paint.strokeWidth =
            dp(
                2f
            )

        paint.color =
            Color.rgb(
                92,
                101,
                94
            )

        for (
            index in
            0..2
        ) {
            val offset =
                index *
                    dp(
                        7f
                    )

            canvas.drawLine(
                right -
                    size +
                    offset,
                bottom,
                right,
                bottom -
                    size +
                    offset,
                paint
            )
        }
    }

    private fun isResizeHandle(
        x: Float,
        y: Float
    ): Boolean {
        val size =
            dp(
                42f
            )

        return x >=
            width -
                size &&
            y >=
                height -
                    size
    }

    private fun stopInteraction() {
        dragging = false
        resizing = false
        dragMoved = false

        parent
            ?.requestDisallowInterceptTouchEvent(
                false
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

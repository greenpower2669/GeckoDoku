package com.greenpower2669.geckodoku

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class MainActivity : Activity() {
    private lateinit var puzzle: Puzzle
    private lateinit var engine: GameEngine

    private val fx: FxFeedback =
        ToneFxFeedback()

    private lateinit var statsStore:
        PlayerStatsStore

    private lateinit var journalStore:
        PuzzleJournalStore

    private lateinit var playerProfileStore:
        PlayerProfileStore

    private lateinit var hallOfFameStore:
        HallOfFameStore

    private lateinit var userDataBackup:
        UserDataBackup

    private val exportDataRequestCode =
        7401

    private val importDataRequestCode =
        7402

    private lateinit var board:
        GeckoBoardView

    private lateinit var sudokuBoard:
        SudokuBoardView

    private lateinit var sudokuValueOverlay:
        SudokuValueOverlayView

    private lateinit var sudokuStyleSelector:
        SudokuStyleSelectorView

    private lateinit var sudokuControlsPanel:
        LinearLayout

    private lateinit var sudokuNotesButton:
        Button

    private lateinit var sudokuEraseButton:
        Button

    private lateinit var sudokuUndoButton:
        Button

    private lateinit var sudokuRedoButton:
        Button

    private var sudokuPalettePopup:
        PopupWindow? = null

    private val sudokuPopupPlacementPolicy =
        SudokuPopupPlacementPolicy()

    private val sudokuProfessorCandidatePolicy =
        SudokuProfessorCandidatePolicy()

    private val sudokuProfessorInteractionPolicy =
        SudokuProfessorInteractionPolicy()

    private val sudokuCellTapPolicy =
        SudokuCellTapPolicy()

    private val sudokuGesturePolicy =
        SudokuGesturePolicy()

    private var sudokuReasoningGeneration =
        0

    private val sudokuFullWidthBoardPolicy =
        SudokuFullWidthBoardPolicy(
            horizontalMarginPx = 3
        )

    private lateinit var boardAnchor:
        View

    private val boardGeometryPolicy =
        GameModeBoardGeometryPolicy()

    private lateinit var status:
        TextView

    private lateinit var info:
        TextView

    private lateinit var settingsButton:
        Button

    private lateinit var quickTalkButton:
        Button

    private lateinit var sizeButton:
        Button

    private lateinit var difficultyButton:
        Button

    private lateinit var professorButton:
        Button

    private lateinit var professorButtonHost:
        FrameLayout

    private lateinit var professorPortrait:
        ImageView

    private lateinit var professorVideo:
        ChromaKeyVideoView

    private var professorVideoMode =
        ProfessorVideoMode.NONE

    private var professorActionVideoFailed =
        false

    private var professorSpeechVideoFailed =
        false

    private var professorSpeechActive =
        false

    private val professorSpeechVideoPolicy =
        ProfessorSpeechVideoPolicy()

    private val professorSpeechVideoStartPolicy =
        ProfessorSpeechVideoStartPolicy()

    private lateinit var titleView:
        TextView

    private lateinit var titleIdentityHost:
        FrameLayout

    private lateinit var titleIdentityImage:
        ImageView

    private val titleIdentityPolicy =
        TitleIdentityPolicy()

    private lateinit var screenRoot:
        FrameLayout

    private lateinit var controlsPanel:
        LinearLayout

    private lateinit var professorBubble:
        ProfessorBubbleView

    private lateinit var celebrationView:
        VictoryCelebrationView

    private lateinit var saveButton:
        Button

    private lateinit var newButton:
        Button

    private lateinit var statsButton:
        Button

    private lateinit var replayButton:
        Button

    private lateinit var journalButton:
        Button

    private lateinit var gameModePreferences:
        GameModePreferences

    private var selectedGameMode =
        GameMode.GECKODOKU

    private var sudokuPuzzle:
        SudokuPuzzle? = null

    private var sudokuEngine:
        SudokuGameEngine? = null

    private lateinit var gomokuBoard:
        GomokuBoardView

    private var gomokuEngine:
        GomokuGameEngine? = null

    private val gomokuBoardLayoutPolicy =
        GomokuBoardLayoutPolicy()

    private var gomokuProfessorThinking =
        false

    private var gomokuMatchMode =
        GomokuMatchMode.VS_PROFESSOR

    private var gomokuGeneration =
        0

    private lateinit var beeGeckoBoard:
        BeeGeckoBoardView

    private var beeGeckoPuzzle:
        BeeGeckoPuzzle? = null

    private var beeGeckoEngine:
        BeeGeckoGameEngine? = null

    private lateinit var beeGeckoSessionStore:
        BeeGeckoSessionStore

    private var beeGeckoCamera =
        BeeGeckoCamera()

    private var sudokuSelectedCell:
        Cell? = null

    private var sudokuNotesMode =
        false

    private lateinit var richMediaSettings:
        RichMediaSettings

    private lateinit var richMediaOverlay:
        RichMediaOverlayView

    private lateinit var aliveMascotOverlay:
        AliveMascotOverlayView

    private lateinit var gameAudio:
        AssetAudioPlayer

    private lateinit var professorSpeech:
        ProfessorSpeech

    private lateinit var professorLife:
        ProfessorLifeController

    private val professorQuickBubbleClosePolicy =
        ProfessorQuickBubbleClosePolicy()

    private val professorSimpleSpeechCoordinator =
        ProfessorSimpleSpeechCoordinator(
            professorQuickBubbleClosePolicy
        )

    private var professorQuickBubbleCloseRunnable:
        Runnable? = null

    private var professorPausedAtMs: Long = 0L

    private val rewardedGeckos =
        linkedSetOf<Cell>()

    private var celebrationUsesMusic =
        false

    private val celebrationAudioPolicy =
        CelebrationAudioPolicy()

    private val richMediaScheduler =
        RichMediaScheduler()

    private val introLifecyclePolicy =
        IntroLifecyclePolicy()

    private var introPhase =
        IntroPhase.DONE

    private val professorUiPolicy =
        ProfessorUiPolicy()

    private val professorIdleAnimationPolicy =
        ProfessorIdleAnimationPolicy()

    private val cellAnimationStyle =
        CellAnimationStyle()

    private val professorAmbientPolicy =
        ProfessorAmbientPolicy()

    private val professorSpeechLaunchPolicy =
        ProfessorSpeechLaunchPolicy()

    private val professorSpeechVisualPolicy =
        ProfessorSpeechVisualPolicy()

    private val settingsMenuPolicy =
        SettingsMenuPolicy()

    private val professorPortraitContinuityPolicy =
        ProfessorPortraitContinuityPolicy()

    private var professorVisualGeneration = 0
    private var professorVisualPreparing = false
    private var professorVisualPrepared = false
    private var suppressVisualForCurrentSpeech = false
    private var professorVisualTimeout:
        Runnable? = null

    private var lastBoardActionAtMs = 0L
    private var boardActionCount = 0
    private var ambientHelpOffered = false
    private var ambientSaveOffered = false
    private var nextSmallTalkAtMs = 0L
    private var nextAmbientAllowedAtMs = 0L

    private val professorAmbientRunnable =
        Runnable {
            runProfessorAmbientTick()
        }

    private val professorIdleAnimationRunnable =
        Runnable {
            runProfessorIdleAnimation()
        }

    private var selectedSize = 5

    @Volatile
    private var classicGenerationToken =
        0

    @Volatile
    private var classicGenerationActive =
        false

    @Volatile
    private var beeGenerationToken =
        0

    @Volatile
    private var beeGenerationActive =
        false

    private var selectedDifficulty =
        GameDifficulty.EASY

    private var pendingMarker:
        CustomMarker? = null

    private var eraseMarkerMode = false

    private var gameStartedAt = 0L

    private var completionRecorded = false

    private var pendingProfessorHypothesis:
        ProfessorHint? = null

    private var professorUsed = false

    private var assistancePoints =
        0

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        MediaTrace.install(this)

        AudioCapturePolicy
            .applyApplicationPolicy(
                this
            )

        AudioCapturePolicy.log(
            source = "FX",
            detail =
                "transport=TONE_GENERATOR stream=MUSIC capture=APP_POLICY"
        )

        richMediaSettings =
            RichMediaSettings(this)

        introPhase =
            if (
                savedInstanceState == null &&
                richMediaSettings.enabled
            ) {
                IntroPhase.FIRST
            } else {
                IntroPhase.DONE
            }

        gameAudio =
            AssetAudioPlayer(this)

        professorSpeech =
            ProfessorSpeech(this).apply {
                onSpeakingChanged = {
                    speaking ->

                    handleProfessorSpeakingChanged(
                        speaking
                    )
                }
            }

        professorLife =
            ProfessorLifeController
                .create(this)

        statsStore =
            PlayerStatsStore(this)

        journalStore =
            PuzzleJournalStore(this)

        playerProfileStore =
            PlayerProfileStore(this)

        hallOfFameStore =
            HallOfFameStore(this)

        userDataBackup =
            UserDataBackup(this)

        beeGeckoSessionStore =
            BeeGeckoSessionStore(this)

        fx.enabled =
            playerProfileStore
                .soundEnabled
        gameAudio.enabled =
            fx.enabled
        professorSpeech.enabled =
            fx.enabled

        gameModePreferences =
            GameModePreferences(this)

        selectedGameMode =
            gameModePreferences
                .gameMode

        gomokuMatchMode =
            gameModePreferences
                .gomokuMatchMode

        selectedSize =
            gameModePreferences
                .classicSize

        selectedDifficulty =
            gameModePreferences
                .selectedDifficulty

        createPuzzle(
            recordStart =
                selectedGameMode ==
                    GameMode.GECKODOKU
        )

        when (selectedGameMode) {
            GameMode.SUDOKU ->
                startSudokuPuzzle(
                    SudokuGenerator.generate(
                        selectedDifficulty
                    )
                )

            GameMode.GOMOKU ->
                startGomokuGame()

            GameMode.BEES_GECKOS ->
                startBeeGeckoGame(
                    restoreSaved = true
                )

            GameMode.GECKODOKU ->
                Unit
        }

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                clipChildren = false
                clipToPadding = false

                setPadding(
                    dp(8),
                    dp(8),
                    dp(8),
                    dp(8)
                )

                setBackgroundColor(
                    Color.rgb(
                        247,
                        250,
                        247
                    )
                )
            }

        titleView =
            TextView(this).apply {
                text =
                    "GeckoDoku 🦎"

                textSize = 28f

                setTextColor(
                    Color.rgb(
                        20,
                        70,
                        40
                    )
                )

                gravity =
                    Gravity.CENTER

                addOnLayoutChangeListener {
                        _,
                        _,
                        _,
                        _,
                        _,
                        _,
                        _,
                        _,
                        _ ->

                    if (
                        ::screenRoot
                            .isInitialized
                    ) {
                        positionTitleIdentity()
                    }
                }
            }

        titleIdentityImage =
            ImageView(this).apply {
                scaleType =
                    ImageView.ScaleType
                        .CENTER_CROP
                importantForAccessibility =
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO
                contentDescription = null

                try {
                    context.assets.open(
                        AssetMediaCatalog
                            .GECKO_ICON
                    ).use {
                        setImageBitmap(
                            BitmapFactory
                                .decodeStream(it)
                        )
                    }
                } catch (_: Exception) {
                    visibility =
                        View.INVISIBLE
                }
            }

        titleIdentityHost =
            FrameLayout(this).apply {
                background =
                    GradientDrawable()
                        .apply {
                            shape =
                                GradientDrawable.OVAL
                            setColor(
                                Color.rgb(
                                    244,
                                    250,
                                    235
                                )
                            )
                            setStroke(
                                dp(2),
                                Color.rgb(
                                    63,
                                    120,
                                    72
                                )
                            )
                        }

                clipToOutline = true

                addView(
                    titleIdentityImage,
                    FrameLayout.LayoutParams(
                        dp(
                            titleIdentityPolicy
                                .iconSizeDp
                        ),
                        dp(
                            titleIdentityPolicy
                                .iconSizeDp
                        )
                    ).apply {
                        gravity =
                            Gravity.CENTER
                    }
                )
            }

        info =
            TextView(this).apply {
                textSize = 15f
                setTextColor(
                    Color.DKGRAY
                )
                gravity =
                    Gravity.CENTER
                setPadding(
                    0,
                    0,
                    0,
                    dp(4)
                )
            }

        professorBubble =
            ProfessorBubbleView(this).apply {
                visibility = View.GONE
                onClose = {
                    closeProfessorBubble()
                }
            }

        status =
            TextView(this).apply {
                textSize = 17f
                setTextColor(
                    Color.BLACK
                )
                gravity =
                    Gravity.CENTER
                minHeight =
                    dp(58)
            }

        board =
            GeckoBoardView(this).apply {
                setPuzzleAndRefresh(
                    this@MainActivity
                        .puzzle
                )

                snapshotProvider = {
                    engine.snapshot()
                }

                onSingleTapCell = {
                    handleSingleTap(it)
                }

                onDoubleTapCell = {
                    showClassicLogicalPalette(
                        it
                    )
                }

                onAxisGuideMoved = {
                        from,
                        to ->

                    engine.moveAxisGuide(
                        from,
                        to
                    )

                    board.invalidate()
                }

                onLongPressCell = {
                    handleLongPress(it)
                }

                onLongPressOutside = {
                    showMarkerPalette()
                }
            }

        sudokuBoard =
            SudokuBoardView(this).apply {
                visibility =
                    View.GONE

                snapshotProvider = {
                    requireNotNull(
                        sudokuEngine
                    ).snapshot()
                }

                onCellSelected = {
                        cell ->

                    sudokuSelectedCell =
                        cell

                    setSelectedCell(
                        cell
                    )

                    val snapshot =
                        sudokuEngine
                            ?.snapshot()

                    if (
                        snapshot != null &&
                        sudokuCellTapPolicy
                            .actionFor(
                                snapshot,
                                cell
                            ) ==
                            SudokuCellTapAction
                                .TOGGLE_GECKO_MARKER
                    ) {
                        toggleSudokuGeckoMarker()
                    } else {
                        sudokuValueOverlay
                            .invalidate()

                        status.text =
                            if (
                                snapshot
                                    ?.isGiven(cell) ==
                                    true
                            ) {
                                "Ce chiffre est donné."
                            } else {
                                "Sudoku • case " +
                                    (cell.row + 1) +
                                    "," +
                                    (cell.col + 1)
                            }
                    }
                }

                onDoubleTapCell = {
                        cell ->

                    if (
                        sudokuGesturePolicy
                            .actionFor(
                                SudokuGesture
                                    .DOUBLE_TAP
                            ) ==
                            SudokuGestureAction
                                .OPEN_PERSONAL_MARKERS
                    ) {
                        showSudokuPersonalMarkerPalette(
                            cell
                        )
                    }
                }

                onLongPressCell = {
                        cell ->

                    showSudokuCellPalette(
                        cell
                    )
                }
            }

        gomokuBoard =
            GomokuBoardView(this).apply {
                visibility =
                    View.GONE

                snapshotProvider = {
                    requireNotNull(
                        gomokuEngine
                    ).snapshot()
                }

                animationsEnabled =
                    richMediaSettings.enabled

                onPlayCell = {
                        cell ->

                    handleGomokuPlayerMove(
                        cell
                    )
                }

                onViewportChanged = {
                    if (
                        selectedGameMode ==
                            GameMode.GOMOKU
                    ) {
                        refreshGomokuInfo()

                        if (
                            ::richMediaOverlay
                                .isInitialized
                        ) {
                            richMediaOverlay
                                .refreshDynamicTargets()
                        }

                        if (
                            ::aliveMascotOverlay
                                .isInitialized
                        ) {
                            aliveMascotOverlay
                                .refreshDynamicTargets()
                        }
                    }
                }
            }

        beeGeckoBoard =
            BeeGeckoBoardView(this).apply {
                visibility =
                    View.GONE

                beeGeckoPuzzle
                    ?.let {
                        setPuzzle(
                            next = it,
                            restoredCamera =
                                beeGeckoCamera
                        )
                    }

                snapshotProvider = {
                    requireNotNull(
                        beeGeckoEngine
                    ).snapshot()
                }

                onSingleTapCell = {
                        cell ->

                    handleBeeGeckoSingleTap(
                        cell
                    )
                }

                onDoubleTapCell = {
                        cell ->

                    showBeeGeckoPiecePalette(
                        cell
                    )
                }

                onLongPressCell = {
                        cell ->

                    showBeeGeckoMarkerPalette(
                        cell
                    )
                }

                onAxisGuideMoved = {
                        from,
                        axis,
                        to ->

                    beeGeckoEngine
                        ?.moveAxisMarker(
                            from,
                            axis,
                            to
                        )

                    beeGeckoBoard
                        .invalidate()

                    persistBeeGeckoSession()
                }

                onViewportChanged = {
                        camera ->

                    beeGeckoCamera =
                        camera

                    if (
                        selectedGameMode ==
                            GameMode.BEES_GECKOS
                    ) {
                        refreshBeeGeckoInfo()

                        if (
                            ::richMediaOverlay
                                .isInitialized
                        ) {
                            richMediaOverlay
                                .refreshDynamicTargets()
                        }

                        if (
                            ::aliveMascotOverlay
                                .isInitialized
                        ) {
                            aliveMascotOverlay
                                .refreshDynamicTargets()
                        }
                    }
                }
            }

        sudokuValueOverlay =
            SudokuValueOverlayView(this).apply {
                visibility =
                    View.GONE

                snapshotProvider = {
                    requireNotNull(
                        sudokuEngine
                    ).snapshot()
                }

                visualStyle =
                    gameModePreferences
                        .sudokuVisualStyle
            }

        sudokuStyleSelector =
            SudokuStyleSelectorView(this).apply {
                visibility =
                    View.GONE

                setCommittedStyle(
                    gameModePreferences
                        .sudokuVisualStyle
                )

                onPreviewStyle = {
                        style ->

                    sudokuValueOverlay
                        .visualStyle =
                        style
                }

                onCommitStyle = {
                        style ->

                    gameModePreferences
                        .sudokuVisualStyle =
                        style

                    sudokuValueOverlay
                        .visualStyle =
                        style

                    status.text =
                        when (style) {
                            SudokuVisualStyle
                                .CLASSIC_NUMBERS ->
                                "Style Sudoku : classique."

                            SudokuVisualStyle
                                .GECKO_NB ->
                                "Style Sudoku : Gecko noir et blanc."

                            SudokuVisualStyle
                                .GECKO_COLORED ->
                                "Style Sudoku : Gecko couleur."
                        }
                }
            }

        sudokuControlsPanel =
            createSudokuControlsPanel()
                .apply {
                    visibility =
                        View.GONE
                }

        boardAnchor =
            View(this).apply {
                importantForAccessibility =
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable = false
                setBackgroundColor(
                    Color.TRANSPARENT
                )

                addOnLayoutChangeListener {
                        _,
                        _,
                        _,
                        _,
                        _,
                        _,
                        _,
                        _,
                        _ ->

                    if (
                        ::screenRoot
                            .isInitialized
                    ) {
                        positionFloatingBoard()
                    }
                }
            }

        sizeButton =
            Button(this).apply {
                textSize = 15f
                minHeight = dp(46)

                setOnClickListener {
                    chooseSize()
                }
            }

        difficultyButton =
            Button(this).apply {
                textSize = 15f
                minHeight = dp(46)

                setOnClickListener {
                    chooseDifficulty()
                }
            }

        newButton =
            Button(this).apply {
                text = "↻ Nouvelle"
                textSize = 15f
                minHeight = dp(46)

                setOnClickListener {
                    when {
                        selectedGameMode ==
                            GameMode.GECKODOKU &&
                            classicGenerationActive ->
                            cancelClassicPuzzleSearch(
                                announce = true
                            )

                        selectedGameMode ==
                            GameMode.BEES_GECKOS &&
                            beeGenerationActive ->
                            cancelBeeGeckoPuzzleSearch(
                                announce = true
                            )

                        else -> {
                            createActivePuzzle()

                            if (
                                selectedGameMode !=
                                    GameMode.GECKODOKU &&
                                selectedGameMode !=
                                    GameMode.BEES_GECKOS
                            ) {
                                refreshGameUi()
                                playLevelStartMusic()
                            }
                        }
                    }
                }
            }

        statsButton =
            Button(this).apply {
                text = "Stats"
                textSize = 15f
                minHeight = dp(46)

                setOnClickListener {
                    showStats()
                }
            }

        quickTalkButton =
            Button(this).apply {
                text = "!"
                textSize = 20f
                minHeight = dp(46)
                contentDescription =
                    "Faire parler Prof Gecko"

                setOnClickListener {
                    speakQuickProfessorLine()
                }
            }

        settingsButton =
            Button(this).apply {
                text = "⚙️"
                textSize = 18f
                minHeight = dp(46)
                contentDescription =
                    "Réglages"

                setOnClickListener {
                    showSettings()
                }
            }

        replayButton =
            Button(this).apply {
                text = "↺ Rejouer"
                textSize = 14f
                minHeight = dp(46)

                setOnClickListener {
                    replayCurrentPuzzle()
                }
            }

        saveButton =
            Button(this).apply {
                text = "⭐ Sauver"
                textSize = 14f
                minHeight = dp(46)

                setOnClickListener {
                    saveCurrentPuzzle()
                }
            }

        journalButton =
            Button(this).apply {
                text = "📚 Journal"
                textSize = 14f
                minHeight = dp(46)

                setOnClickListener {
                    showJournal()
                }
            }

        professorButton =
            Button(this).apply {
                text =
                    "Prof Gecko"

                textSize = 16f
                minHeight =
                    dp(
                        professorUiPolicy
                            .buttonHostHeightDp
                    )

                gravity =
                    Gravity.CENTER_VERTICAL

                stateListAnimator = null
                elevation =
                    dp(
                        professorUiPolicy
                            .buttonElevationDp
                    ).toFloat()
                translationZ = 0f

                setPadding(
                    dp(86),
                    0,
                    dp(16),
                    0
                )

                setOnClickListener {
                    showProfessorHint()
                }

                setOnLongClickListener {
                    when (
                        selectedGameMode
                    ) {
                        GameMode.SUDOKU -> {
                            playSudokuProfessorDirect()
                            true
                        }

                        GameMode.GOMOKU -> {
                            showGomokuProfessorAdvice(
                                applyMoveForHuman =
                                    gomokuMatchMode ==
                                        GomokuMatchMode
                                            .VS_PROFESSOR,
                                deepAnalysis =
                                    true
                            )
                            true
                        }

                        GameMode.BEES_GECKOS -> {
                            showBeeGeckoProfessorHint(
                                applyStep = true
                            )
                            true
                        }

                        GameMode.GECKODOKU ->
                            false
                    }
                }
            }

        professorPortrait =
            ImageView(this).apply {
                scaleType =
                    ImageView.ScaleType
                        .FIT_CENTER

                importantForAccessibility =
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO

                contentDescription = null
                isClickable = false
                elevation =
                    dp(
                        professorUiPolicy
                            .portraitElevationDp
                    ).toFloat()

                try {
                    context.assets.open(
                        AssetMediaCatalog
                            .PROF_PORTRAIT
                    ).use {
                        setImageBitmap(
                            BitmapFactory
                                .decodeStream(it)
                        )
                    }
                } catch (_: Exception) {
                    visibility =
                        View.INVISIBLE
                }
            }

        professorVideo =
            ChromaKeyVideoView(this).apply {
                logicalLayer =
                    "PROFESSOR"
                visibility = View.INVISIBLE
                importantForAccessibility =
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable = false
                elevation =
                    dp(
                        professorUiPolicy
                            .portraitElevationDp + 4
                    ).toFloat()
            }

        professorButtonHost =
            FrameLayout(this).apply {
                clipChildren = false
                clipToPadding = false

                addView(
                    professorButton,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams
                            .MATCH_PARENT,
                        FrameLayout.LayoutParams
                            .MATCH_PARENT
                    )
                )

                if (
                    professorUiPolicy
                        .showPortraitInButton
                ) {
                    val portraitSize =
                        dp(
                            professorUiPolicy
                                .buttonHostHeightDp +
                                professorUiPolicy
                                    .buttonPortraitOverhangDp
                        )

                    addView(
                        professorPortrait,
                        FrameLayout.LayoutParams(
                            portraitSize,
                            portraitSize
                        ).apply {
                            gravity =
                                Gravity.START or
                                    Gravity.BOTTOM
                            leftMargin = dp(8)
                        }
                    )

                    professorPortrait.bringToFront()

                    if (
                        professorUiPolicy
                            .playVideoInButton
                    ) {
                        addView(
                            professorVideo,
                            FrameLayout.LayoutParams(
                                portraitSize,
                                portraitSize
                            ).apply {
                                gravity =
                                    Gravity.START or
                                        Gravity.BOTTOM
                                leftMargin = dp(8)
                            }
                        )

                        professorVideo.bringToFront()
                    }
                }
            }

        root.addView(
            titleView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        root.addView(
            info,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        root.addView(
            status,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        root.addView(
            boardAnchor,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val row1 =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL

                addView(
                    sizeButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                addView(
                    difficultyButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )
            }

        val row2 =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL

                addView(
                    newButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.3f
                    )
                )

                addView(
                    replayButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.3f
                    )
                )

                addView(
                    statsButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                addView(
                    quickTalkButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        .6f
                    )
                )

                addView(
                    settingsButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        .6f
                    )
                )
            }

        controlsPanel =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL

                addView(row1)
                addView(row2)
            }

        root.addView(
            sudokuStyleSelector,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            )
        )

        root.addView(
            sudokuControlsPanel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            controlsPanel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            professorButtonHost,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(
                    professorUiPolicy
                        .buttonHostHeightDp
                )
            )
        )

        screenRoot =
            FrameLayout(this).apply {
                clipChildren = false
                clipToPadding = false

                addView(
                    root,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )

                addView(
                    titleIdentityHost,
                    FrameLayout.LayoutParams(
                        dp(
                            titleIdentityPolicy
                                .iconSizeDp +
                                titleIdentityPolicy
                                    .frameExtraDp
                        ),
                        dp(
                            titleIdentityPolicy
                                .iconSizeDp +
                                titleIdentityPolicy
                                    .frameExtraDp
                        )
                    ).apply {
                        gravity =
                            Gravity.TOP or
                                Gravity.START
                    }
                )
            }

        screenRoot.addView(
            board,
            1,
            FrameLayout.LayoutParams(
                1,
                1
            ).apply {
                gravity =
                    Gravity.TOP or
                        Gravity.START
            }
        )

        screenRoot.addView(
            sudokuBoard,
            2,
            FrameLayout.LayoutParams(
                1,
                1
            ).apply {
                gravity =
                    Gravity.TOP or
                        Gravity.START
            }
        )

        screenRoot.addView(
            sudokuValueOverlay,
            3,
            FrameLayout.LayoutParams(
                1,
                1
            ).apply {
                gravity =
                    Gravity.TOP or
                        Gravity.START
            }
        )

        screenRoot.addView(
            gomokuBoard,
            4,
            FrameLayout.LayoutParams(
                1,
                1
            ).apply {
                gravity =
                    Gravity.TOP or
                        Gravity.START
            }
        )

        screenRoot.addView(
            beeGeckoBoard,
            5,
            FrameLayout.LayoutParams(
                1,
                1
            ).apply {
                gravity =
                    Gravity.TOP or
                        Gravity.START
            }
        )

        screenRoot.addOnLayoutChangeListener {
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _ ->

            positionFloatingBoard()
        }

        aliveMascotOverlay =
            AliveMascotOverlayView(this).apply {
                animationsEnabled =
                    richMediaSettings.enabled
                visibility =
                    View.INVISIBLE
            }

        screenRoot.addView(
            aliveMascotOverlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        richMediaOverlay =
            RichMediaOverlayView(this).apply {
                visibility = View.GONE
            }

        screenRoot.addView(
            richMediaOverlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        screenRoot.addView(
            professorBubble,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = dp(12)
                rightMargin = dp(12)
                gravity = Gravity.TOP
            }
        )

        celebrationView =
            VictoryCelebrationView(this).apply {
                visibility = View.GONE

                onFireworkBurst = {
                        level,
                        burst,
                        isLast ->

                    if (!celebrationUsesMusic) {
                        fx.celebrationBurst(
                            level,
                            burst,
                            isLast
                        )
                    }
                }

                onCelebrationStopped = {
                    fx.stopCelebration()

                    if (
                        celebrationAudioPolicy
                            .stopMusicWhenVisualCelebrationStops
                    ) {
                        gameAudio.stopMusic()
                    }

                    celebrationUsesMusic = false
                }
            }

        screenRoot.addView(
            celebrationView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(
            screenRoot
        )

        screenRoot.post {
            ensurePlantMascot()
        }

        protectFromSystemBars(root)
        applyGameModeVisibility()
        refreshGameUi()
        applyProfessorIntroVisibility()
        scheduleProfessorIdleAnimation()
        resetProfessorAmbientState()
        scheduleProfessorAmbientTick()
        startTitleIdentityAnimation()

        if (savedInstanceState == null) {
            screenRoot.post {
                positionTitleIdentity()
                playIntroIfEnabled()
            }
        }
    }

    private fun createActivePuzzle() {
        when (selectedGameMode) {
            GameMode.SUDOKU ->
                startSudokuPuzzle(
                    SudokuGenerator.generate(
                        selectedDifficulty
                    )
                )

            GameMode.GOMOKU ->
                startGomokuGame()

            GameMode.BEES_GECKOS ->
                requestBeeGeckoPuzzle(
                    recordStart = true
                )

            GameMode.GECKODOKU ->
                requestClassicPuzzle(
                    recordStart = true
                )
        }
    }

    private fun requestClassicPuzzle(
        recordStart: Boolean
    ) {
        classicGenerationToken += 1

        val token =
            classicGenerationToken
        val requestedSize =
            selectedSize
        val requestedDifficulty =
            selectedDifficulty

        classicGenerationActive = true

        if (::newButton.isInitialized) {
            newButton.text =
                "✕ Annuler"
        }

        if (::status.isInitialized) {
            status.text =
                "Recherche d'une grille " +
                    requestedDifficulty.label +
                    "…"
        }

        Thread {
            val generated =
                PuzzleGenerator.generateExact(
                    size =
                        requestedSize,
                    requested =
                        requestedDifficulty,
                    shouldCancel = {
                        token !=
                            classicGenerationToken ||
                            Thread
                                .currentThread()
                                .isInterrupted
                    },
                    onBatchCompleted = {
                        batch ->

                        if (
                            batch == 1 ||
                            batch % 2 == 0
                        ) {
                            runOnUiThread {
                                if (
                                    token ==
                                        classicGenerationToken &&
                                    classicGenerationActive &&
                                    selectedGameMode ==
                                        GameMode.GECKODOKU
                                ) {
                                    status.text =
                                        "Recherche d'une grille " +
                                            requestedDifficulty.label +
                                            "… série " +
                                            (batch + 1)
                                }
                            }
                        }
                    }
                )

            runOnUiThread {
                if (
                    token !=
                        classicGenerationToken
                ) {
                    return@runOnUiThread
                }

                classicGenerationActive =
                    false

                if (::newButton.isInitialized) {
                    newButton.text =
                        "↻ Nouvelle"
                }

                if (
                    generated == null ||
                    selectedGameMode !=
                        GameMode.GECKODOKU ||
                    selectedSize !=
                        requestedSize ||
                    selectedDifficulty !=
                        requestedDifficulty
                ) {
                    return@runOnUiThread
                }

                startPuzzle(
                    generated,
                    recordStart
                )
                refreshGameUi()

                status.text =
                    "Grille " +
                        generated.difficulty.label +
                        " trouvée • " +
                        generated.givens.size +
                        " Gecko(s) donné(s)."

                playLevelStartMusic()
            }
        }.start()
    }

    private fun cancelClassicPuzzleSearch(
        announce: Boolean
    ) {
        if (!classicGenerationActive) {
            return
        }

        classicGenerationToken += 1
        classicGenerationActive =
            false

        if (::newButton.isInitialized) {
            newButton.text =
                "↻ Nouvelle"
        }

        if (
            announce &&
            ::status.isInitialized
        ) {
            status.text =
                "Recherche de grille annulée."
        }
    }

    private fun createPuzzle(
        recordStart: Boolean
    ) {
        val generated =
            PuzzleGenerator.generate(
                selectedSize,
                selectedDifficulty
            )

        startPuzzle(
            generated,
            recordStart
        )
    }

    private fun startPuzzle(
        nextPuzzle: Puzzle,
        recordStart: Boolean
    ) {
        if (::richMediaOverlay.isInitialized) {
            richMediaOverlay.stop()

            introPhase =
                IntroPhase.DONE
            applyProfessorIntroVisibility()
        }

        if (
            ::aliveMascotOverlay
                .isInitialized
        ) {
            aliveMascotOverlay
                .stopBoardMascots()
        }

        puzzle = nextPuzzle

        selectedSize =
            puzzle.size

        selectedDifficulty =
            puzzle.difficulty

        gameModePreferences
            .classicSize =
            selectedSize
        gameModePreferences
            .selectedDifficulty =
            selectedDifficulty

        engine =
            GameEngine(puzzle)

        pendingMarker = null
        eraseMarkerMode = false
        completionRecorded = false
        professorUsed = false
        assistancePoints = 0
        rewardedGeckos.clear()

        if (::gameAudio.isInitialized) {
            gameAudio.stopAll()
        }

        if (::professorSpeech.isInitialized) {
            professorSpeech.stop(
                reason =
                    SpeechStopReason
                        .PUZZLE_RESET,
                caller =
                    "MainActivity.startPuzzle"
            )
        }

        gameStartedAt =
            SystemClock.elapsedRealtime()

        if (::professorLife.isInitialized) {
            professorLife.observe(
                ProfessorPlayerEvent
                    .GAME_STARTED,
                puzzle.difficulty
            )
        }

        resetProfessorAmbientState()
        clearProfessorSession()

        if (recordStart) {
            statsStore.recordStart(
                size = puzzle.size,
                difficulty =
                    puzzle.difficulty,
                mode =
                    GameMode.GECKODOKU
            )
        }
    }

    private fun replayCurrentPuzzle() {
        when (selectedGameMode) {
            GameMode.SUDOKU -> {
                val current =
                    sudokuPuzzle

                if (current != null) {
                    startSudokuPuzzle(
                        current
                    )

                    refreshGameUi()

                    status.text =
                        "Même Sudoku réinitialisé."
                }
            }

            GameMode.GOMOKU -> {
                startGomokuGame()
                refreshGameUi()

                status.text =
                    if (
                        gomokuMatchMode ==
                            GomokuMatchMode
                                .VS_PROFESSOR
                    ) {
                        "Nouvelle partie contre Prof Gecko. À toi de jouer vert 🦎"
                    } else {
                        "Nouvelle partie humain contre humain. Au joueur Vert."
                    }
            }

            GameMode.BEES_GECKOS -> {
                val current =
                    beeGeckoPuzzle

                if (current != null) {
                    startBeeGeckoGame(
                        puzzle = current,
                        restoreSaved = false,
                        recordStart = true
                    )
                    refreshGameUi()
                    status.text =
                        "Même grille Abeilles & Geckos réinitialisée."
                }
            }

            GameMode.GECKODOKU -> {
                startPuzzle(
                    puzzle,
                    recordStart = true
                )

                refreshGameUi()

                status.text =
                    "Même grille réinitialisée. À toi de rejouer 🦎"
            }
        }
    }

    private fun saveCurrentPuzzle() {
        journalStore.save(puzzle)

        saveButton.text =
            "★ Sauvée"

        status.text =
            "Grille sauvegardée dans le journal ⭐"

        fx.marker()
    }

    private fun loadJournalPuzzle(
        id: String
    ) {
        val saved =
            journalStore.load(id)

        if (saved == null) {
            fx.error()
            status.text =
                "Impossible de relire cette grille."
            return
        }

        selectedDifficulty =
            saved.difficulty

        startPuzzle(
            saved,
            recordStart = true
        )

        refreshGameUi()

        status.text =
            "Grille du journal chargée et remise à zéro 📚"
    }

    private fun showJournal() {
        val entries =
            journalStore.list()

        if (entries.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle(
                    "📚 Journal de grilles"
                )
                .setMessage(
                    "Le journal est vide. Utilise ⚙️ puis « Sauver la grille » pour conserver une grille."
                )
                .setPositiveButton(
                    "OK",
                    null
                )
                .show()

            return
        }

        val labels =
            entries.mapIndexed {
                    index,
                    entry ->

                val date =
                    SimpleDateFormat(
                        "dd/MM HH:mm",
                        Locale.getDefault()
                    ).format(
                        Date(entry.savedAt)
                    )

                (index + 1)
                    .toString() +
                    ". " +
                    entry.size +
                    "×" +
                    entry.size +
                    " • " +
                    entry.difficulty.label +
                    " • " +
                    date
            }
            .toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(
                "📚 Journal de grilles"
            )
            .setItems(
                labels
            ) { _, which ->
                showJournalEntryActions(
                    entries[which]
                )
            }
            .setNeutralButton(
                "Vider tout"
            ) { _, _ ->
                confirmClearJournal()
            }
            .setNegativeButton(
                "Fermer",
                null
            )
            .show()
    }

    private fun showJournalEntryActions(
        entry: JournalEntry
    ) {
        val label =
            entry.size.toString() +
                "×" +
                entry.size +
                " • " +
                entry.difficulty.label

        AlertDialog.Builder(this)
            .setTitle(label)
            .setItems(
                arrayOf(
                    "▶ Rejouer cette grille",
                    "🗑 Supprimer du journal"
                )
            ) { _, which ->
                when (which) {
                    0 ->
                        loadJournalPuzzle(
                            entry.id
                        )

                    1 ->
                        confirmDeleteJournalEntry(
                            entry
                        )
                }
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun confirmDeleteJournalEntry(
        entry: JournalEntry
    ) {
        AlertDialog.Builder(this)
            .setTitle(
                "Supprimer cette grille ?"
            )
            .setMessage(
                "La grille sera retirée du journal local."
            )
            .setPositiveButton(
                "Supprimer"
            ) { _, _ ->
                journalStore.delete(
                    entry.id
                )

                if (entry.id ==
                    puzzle.id
                ) {
                    saveButton.text =
                        "⭐ Sauver"
                }

                status.text =
                    "Grille supprimée du journal."
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun confirmClearJournal() {
        AlertDialog.Builder(this)
            .setTitle(
                "Vider tout le journal ?"
            )
            .setMessage(
                "Toutes les grilles sauvegardées seront supprimées. Les statistiques restent intactes."
            )
            .setPositiveButton(
                "Tout vider"
            ) { _, _ ->
                journalStore.clear()

                saveButton.text =
                    "⭐ Sauver"

                status.text =
                    "Journal vidé."
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun refreshGameUi() {
        applyGameModeVisibility()

        if (
            ::titleView.isInitialized
        ) {
            titleView.text =
                AppTitlePolicy
                    .titleFor(
                        selectedGameMode
                    )
        }

        if (
            selectedGameMode ==
                GameMode.SUDOKU
        ) {
            refreshSudokuUi()
            return
        }

        if (
            selectedGameMode ==
                GameMode.GOMOKU
        ) {
            refreshGomokuUi()
            return
        }

        if (
            selectedGameMode ==
                GameMode.BEES_GECKOS
        ) {
            refreshBeeGeckoUi()
            return
        }

        if (::board.isInitialized) {
            board.setPuzzleAndRefresh(
                puzzle
            )
        }

        val report =
            DifficultyIndexer.analyze(
                puzzle
            )

        if (::info.isInitialized) {
            val measured =
                report.ratedDifficulty
                    .label

            val requestedText =
                if (
                    report.ratedDifficulty ==
                    selectedDifficulty
                ) {
                    measured
                } else {
                    selectedDifficulty.label +
                        " demandé • " +
                        measured +
                        " mesuré"
                }

            val logic =
                buildString {
                    append("zones ")
                    append(
                        report.features
                            .regionLogicCount
                    )

                    if (
                        report.features
                            .xWingRequired
                    ) {
                        append(
                            " • X-Wing requis"
                        )
                    }

                    if (
                        report.features
                            .projectionRequired
                    ) {
                        append(
                            " • projection requise"
                        )
                    }

                    if (
                        report.features
                            .hypothesisCount > 0
                    ) {
                        append(
                            " • hypothèse "
                        )
                        append(
                            report.features
                                .hypothesisCount
                        )

                        if (
                            report.features
                                .hypothesisDepth > 1
                        ) {
                            append(
                                " • profondeur "
                            )
                            append(
                                report.features
                                    .hypothesisDepth
                            )
                        }
                    }

                    append(" • trace ")
                    append(
                        puzzle.solverTrace.size
                    )
                }

            info.text =
                selectedSize.toString() +
                    "×" +
                    selectedSize +
                    " • " +
                    requestedText +
                    " • " +
                    logic
        }

        if (::sizeButton.isInitialized) {
            sizeButton.text =
                "Taille " +
                    selectedSize +
                    "×" +
                    selectedSize
        }

        if (::difficultyButton.isInitialized) {
            difficultyButton.text =
                selectedDifficulty.label
        }

        if (::professorButton.isInitialized) {
            professorButton.text =
                "🧑‍🏫 Prof Gecko"
        }

        if (::saveButton.isInitialized) {
            saveButton.text =
                if (
                    journalStore.contains(
                        puzzle.id
                    )
                ) {
                    "★ Sauvée"
                } else {
                    "⭐ Sauver"
                }
        }

        if (::status.isInitialized) {
            status.text =
                if (
                    puzzle.givens.isNotEmpty()
                ) {
                    puzzle.givens.size
                        .toString() +
                        " gecko(s) donné(s). Simple = ✕, double = repères logiques."
                } else {
                    "Simple = ✕, vrai double-clic = repères logiques, appui long = hypothèse."
                }
        }
    }

    private fun handleSingleTap(
        cell: Cell
    ) {
        recordBoardAction()

        if (
            placePendingMarkerIfNeeded(
                cell
            )
        ) {
            return
        }

        clearProfessorSession()

        when (
            engine.toggleCross(cell)
        ) {
            ActionFeedback.CROSS_SET -> {
                fx.cross()
                status.text =
                    "Croix posée."
            }

            ActionFeedback.CROSS_REMOVED -> {
                fx.cross()
                status.text =
                    "Croix retirée."
            }

            ActionFeedback.CROSS_BLOCKED -> {
                fx.blocked()
                status.text =
                    "Case déjà impossible grâce à un gecko."
            }

            ActionFeedback.GECKO_PRESENT -> {
                fx.blocked()
                status.text =
                    "Un gecko est ici. Double-clic pour le retirer."
            }

            ActionFeedback.GIVEN_LOCKED -> {
                fx.blocked()
                status.text =
                    "Gecko donné : il est verrouillé."
            }

            else -> Unit
        }

        board.invalidate()
    }

    private fun showClassicLogicalPalette(
        cell: Cell
    ) {
        val labels =
            arrayOf(
                "🟢  Gecko",
                "🟡  Hypothèse",
                "🔴  Axe"
            )

        AlertDialog.Builder(this)
            .setTitle(
                "Double clic • repère logique"
            )
            .setItems(
                labels
            ) {
                    _,
                    which ->

                when (which) {
                    0 ->
                        handleDoubleTap(
                            cell
                        )

                    1 ->
                        handleLongPress(
                            cell
                        )

                    2 ->
                        showClassicAxisPalette(
                            cell
                        )
                }
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun showClassicAxisPalette(
        cell: Cell
    ) {
        val labels =
            arrayOf(
                "━  Horizontal",
                "┃  Vertical"
            )

        val guides =
            arrayOf(
                ClassicAxisGuide(
                    ClassicAxisGuideKind
                        .HORIZONTAL,
                    cell.row
                ),
                ClassicAxisGuide(
                    ClassicAxisGuideKind
                        .VERTICAL,
                    cell.col
                )
            )

        AlertDialog.Builder(this)
            .setTitle(
                "Choisir l’axe"
            )
            .setItems(
                labels
            ) {
                    _,
                    which ->

                val selected =
                    guides[which]

                showAxisGuideColorPalette {
                    color ->

                    engine.toggleAxisGuide(
                        selected.copy(
                            color =
                                color
                        )
                    )

                    fx.marker()

                    status.text =
                        "Barre d’axe " +
                            color.label.lowercase() +
                            " posée. Fais-la glisser ; hors du plateau elle disparaît."

                    board.invalidate()
                }
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun showAxisGuideColorPalette(
        onChosen:
            (AxisGuideColor) -> Unit
    ) {
        val colors =
            arrayOf(
                AxisGuideColor.YELLOW,
                AxisGuideColor.GREEN,
                AxisGuideColor.RED
            )

        val labels =
            arrayOf(
                "🟡  Jaune",
                "🟢  Vert",
                "🔴  Rouge"
            )

        AlertDialog.Builder(this)
            .setTitle(
                "Couleur de la barre"
            )
            .setItems(
                labels
            ) {
                    _,
                    which ->

                onChosen(
                    colors[which]
                )
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun handleDoubleTap(
        cell: Cell
    ) {
        val actionNow =
            SystemClock.elapsedRealtime()

        val thinkingMs =
            if (lastBoardActionAtMs > 0L) {
                (
                    actionNow -
                        lastBoardActionAtMs
                    ).coerceAtLeast(0L)
            } else {
                Long.MAX_VALUE
            }

        recordBoardAction()

        if (
            placePendingMarkerIfNeeded(
                cell
            )
        ) {
            return
        }

        clearProfessorSession()

        when (
            engine.toggleGecko(cell)
        ) {
            ActionFeedback.GECKO_CONFIRMED -> {
                if (thinkingMs >= 15_000L) {
                    professorLife.observe(
                        ProfessorPlayerEvent
                            .LONG_THINKING,
                        puzzle.difficulty
                    )
                }

                professorLife.observe(
                    ProfessorPlayerEvent
                        .CORRECT_MOVE,
                    puzzle.difficulty
                )

                handlePlayerGeckoConfirmed(
                    cell = cell,
                    completed = false
                )
            }

            ActionFeedback.GECKO_REMOVED -> {
                fx.cross()
                status.text =
                    "Gecko retiré."

                playCellAnimation(
                    RichMediaKind.GECKO_DISAPPEARANCE,
                    cell
                )
            }

            ActionFeedback.WRONG_GECKO -> {
                fx.error()
                statsStore.recordMistake()

                val reactionEvent =
                    when {
                        thinkingMs >= 15_000L -> {
                            professorLife.observe(
                                ProfessorPlayerEvent
                                    .LONG_THINKING,
                                puzzle.difficulty
                            )
                            ProfessorPlayerEvent
                                .WRONG_MOVE
                        }

                        thinkingMs <= 2_500L ->
                            ProfessorPlayerEvent
                                .RAPID_WRONG_MOVE

                        else ->
                            ProfessorPlayerEvent
                                .WRONG_MOVE
                    }

                professorLife.observe(
                    reactionEvent,
                    puzzle.difficulty
                )

                status.text =
                    "Pas ici. Une croix reste en place."

                board
                    .announceForAccessibility(
                        "Gecko incorrect."
                    )

                speakLivingProfessor(
                    event = reactionEvent,
                    origin =
                        SpeechOrigin.QUICK_TALK
                )
            }

            ActionFeedback.CROSS_BLOCKED -> {
                fx.blocked()
                status.text =
                    "Cette case est déjà exclue."
            }

            ActionFeedback.GIVEN_LOCKED -> {
                fx.blocked()
                status.text =
                    "Ce gecko est donné et ne peut pas être retiré."
            }

            ActionFeedback.COMPLETED -> {
                if (thinkingMs >= 15_000L) {
                    professorLife.observe(
                        ProfessorPlayerEvent
                            .LONG_THINKING,
                        puzzle.difficulty
                    )
                }

                professorLife.observe(
                    ProfessorPlayerEvent
                        .CORRECT_MOVE,
                    puzzle.difficulty
                )

                professorLife.observe(
                    ProfessorPlayerEvent
                        .LEVEL_COMPLETED,
                    puzzle.difficulty
                )

                handlePlayerGeckoConfirmed(
                    cell = cell,
                    completed = true
                )
            }

            else -> Unit
        }

        board.invalidate()
    }

    private fun handleLongPress(
        cell: Cell
    ) {
        recordBoardAction()
        clearProfessorSession()

        when (
            engine.longPress(cell)
        ) {
            ActionFeedback.HYPOTHESIS_CHANGED -> {
                fx.hint()

                status.text =
                    when (
                        engine.snapshot()
                            .hypotheses[cell]
                            ?: HypothesisMark.NONE
                    ) {
                        HypothesisMark.GHOST_GECKO ->
                            "Gecko hypothèse discret."

                        HypothesisMark.ALERT_GECKO ->
                            "Gecko repère fort clignotant."

                        HypothesisMark.NONE ->
                            "Hypothèse retirée."
                    }
            }

            ActionFeedback.GIVEN_LOCKED -> {
                fx.blocked()
                status.text =
                    "Gecko confirmé : hypothèse inutile ici."
            }

            ActionFeedback.CROSS_BLOCKED -> {
                fx.blocked()
                status.text =
                    "Case déjà exclue."
            }

            else -> Unit
        }

        board.invalidate()
    }

    private fun showProfessorHint() {
        if (
            selectedGameMode ==
                GameMode.GOMOKU
        ) {
            showGomokuProfessorAdvice(
                applyMoveForHuman =
                    false,
                deepAnalysis =
                    false
            )
            return
        }

        if (
            selectedGameMode ==
                GameMode.BEES_GECKOS
        ) {
            showBeeGeckoProfessorHint(
                applyStep = false
            )
            return
        }

        if (
            selectedGameMode ==
                GameMode.SUDOKU
        ) {
            showSudokuProfessorHint()
            return
        }

        professorUsed = true
        assistancePoints +=
            AssistanceKind.ADVICE.points

        if (::professorLife.isInitialized) {
            professorLife.observe(
                ProfessorPlayerEvent
                    .HINT_REQUESTED,
                puzzle.difficulty
            )
        }

        val videoHandled =
            playProfessorButtonVideo()

        if (!videoHandled) {
            animateProfessorButtonPortrait(
                professorIdleAnimationPolicy
                    .actionFor(
                        Random.nextInt()
                    )
            )
            scheduleProfessorIdleAnimation()
        }

        val pending =
            pendingProfessorHypothesis

        if (pending != null) {
            pendingProfessorHypothesis =
                null

            applyProfessorHint(
                pending,
                resolvingHypothesis = true
            )

            return
        }

        board.clearProfessorHint()

        val next =
            ProfessorGecko.nextHint(
                puzzle,
                engine.snapshot()
            )

        if (next == null) {
            fx.blocked()

            val message =
                if (
                    engine.snapshot()
                        .complete
                ) {
                    "La grille est déjà terminée, biloute 🦎"
                } else {
                    "Je ne trouve plus de déduction sûre avec l'état actuel. Vérifie tes croix et tes hypothèses."
                }

            status.text =
                "Prof Gecko"

            showProfessorBubble(
                message
            )

            board.announceForAccessibility(
                message
            )

            return
        }

        val isHypothesis =
            next.step.technique ==
                SolveTechnique.HYPOTHESIS_TEST ||
            next.step.technique ==
                SolveTechnique.DOUBLE_HYPOTHESIS

        if (isHypothesis) {
            pendingProfessorHypothesis =
                next

            board.showProfessorHypothesis(
                next.step
            )

            val message =
                next.focusText +
                    "\n\nLes deux geckos semi-transparents sont les deux possibilités. Appuie encore sur Prof Gecko : je testerai la branche qui mène à la contradiction."

            showProfessorBubble(
                message
            )

            status.text =
                "Prof Gecko • 2 possibilités"

            professorButton.text =
                "🧑‍🏫 Tester l'hypothèse"

            fx.hint()

            board.announceForAccessibility(
                message
            )

            return
        }

        applyProfessorHint(
            next,
            resolvingHypothesis = false
        )
    }

    private fun applyProfessorHint(
        hint: ProfessorHint,
        resolvingHypothesis: Boolean
    ) {
        val result =
            engine.applyProfessorStep(
                hint.step
            )

        if (
            result ==
            ActionFeedback.WRONG_GECKO
        ) {
            fx.error()

            val message =
                "Alerte : ma déduction contredit la solution unique. Je refuse de modifier la grille et je garde ce cas pour le debug."

            showProfessorBubble(
                message
            )

            status.text =
                "Prof Gecko • incohérence détectée"

            board.clearProfessorHint()

            professorButton.text =
                "🧑‍🏫 Prof Gecko"

            board.announceForAccessibility(
                message
            )

            return
        }

        board.showProfessorHint(
            hint.step,
            3
        )

        val message =
            if (resolvingHypothesis) {
                hint.explanationText +
                    "\n\n" +
                    hint.appliedText
            } else {
                hint.explanationText +
                    "\n\n" +
                    hint.appliedText
            }

        showProfessorBubble(
            message
        )

        status.text =
            "Prof Gecko • étape appliquée"

        professorButton.text =
            "🧑‍🏫 Étape suivante"

        if (hint.step.cell != null) {
            fx.gecko()
        } else {
            fx.cross()
        }

        board.invalidate()

        board.announceForAccessibility(
            message
        )

        if (
            result ==
            ActionFeedback.COMPLETED
        ) {
            completeGame(
                playCelebrationMusicImmediately =
                    true
            )
        } else if (hint.step.cell != null) {
            playCellAnimation(
                RichMediaKind.GECKO_APPEARANCE,
                hint.step.cell
            )
        }
    }

    private fun clearProfessorSession() {
        pendingProfessorHypothesis =
            null

        if (::board.isInitialized) {
            board.clearProfessorHint()
        }

        if (
            ::beeGeckoBoard
                .isInitialized
        ) {
            beeGeckoBoard
                .clearProfessorHint()
        }

        if (
            ::gomokuBoard
                .isInitialized
        ) {
            gomokuBoard
                .clearProfessorReasoning()
        }

        if (
            ::professorButton.isInitialized
        ) {
            professorButton.text =
                "🧑‍🏫 Prof Gecko"
        }

        closeProfessorBubble()
    }

    private fun showProfessorBubble(
        message: String
    ) {
        val normalizedMessage =
            ProfessorDialogTextPolicy
                .normalize(message)

        cancelProfessorQuickBubbleClose()
        professorQuickBubbleClosePolicy
            .onPedagogicalBubbleShown()

        showProfessorBubbleVisualOnly(
            normalizedMessage
        )

        speakWithProfessorVisual(
            text = normalizedMessage,
            origin =
                SpeechOrigin
                    .PROF_BUTTON
        )
    }

    private fun showProfessorBubbleVisualOnly(
        message: String
    ) {
        if (
            ::controlsPanel.isInitialized &&
            professorUiPolicy
                .keepControlsVisibleWhileBubbleOpen
        ) {
            controlsPanel.visibility =
                View.VISIBLE
        }

        professorBubble.showMessage(
            message
        )

        professorBubble.bringToFront()

        if (
            ::celebrationView.isInitialized &&
            celebrationView.visibility ==
                View.VISIBLE
        ) {
            celebrationView.bringToFront()
        }

        screenRoot.post {
            positionProfessorBubble()
        }
    }

    private fun closeProfessorBubble() {
        cancelProfessorQuickBubbleClose()
        professorQuickBubbleClosePolicy
            .invalidate()

        if (
            ::professorBubble.isInitialized
        ) {
            professorBubble.hideMessage()
        }

        if (
            ::controlsPanel.isInitialized &&
            professorUiPolicy
                .keepControlsVisibleWhileBubbleOpen
        ) {
            controlsPanel.visibility =
                View.VISIBLE
        }
    }

    private fun positionFloatingBoard() {
        if (
            !::screenRoot.isInitialized ||
            !::boardAnchor.isInitialized ||
            !::board.isInitialized ||
            screenRoot.width <= 0 ||
            screenRoot.height <= 0
        ) {
            return
        }

        if (
            !ensureBoardAnchorForMode()
        ) {
            boardAnchor.post {
                positionFloatingBoard()
            }
            return
        }

        if (
            boardAnchor.width <= 0 ||
            boardAnchor.height <= 0
        ) {
            return
        }

        val rootLocation =
            IntArray(2)

        val anchorLocation =
            IntArray(2)

        screenRoot.getLocationOnScreen(
            rootLocation
        )

        boardAnchor.getLocationOnScreen(
            anchorLocation
        )

        val anchorTop =
            anchorLocation[1] -
                rootLocation[1]

        val proposed =
            when (selectedGameMode) {
                GameMode.SUDOKU ->
                    sudokuFullWidthBoardPolicy
                        .geometry(
                            usefulWidthPx =
                                screenRoot.width,
                            topPx =
                                anchorTop
                        )

                GameMode.GOMOKU ->
                    gomokuBoardLayoutPolicy
                        .geometry(
                            usefulWidthPx =
                                screenRoot.width,
                            topPx =
                                anchorTop
                        )

                GameMode.BEES_GECKOS ->
                    gomokuBoardLayoutPolicy
                        .geometry(
                            usefulWidthPx =
                                screenRoot.width,
                            topPx =
                                anchorTop
                        )

                GameMode.GECKODOKU ->
                    BoardGeometry(
                        left = 0,
                        top =
                            anchorTop,
                        width =
                            screenRoot.width,
                        height =
                            boardAnchor.height
                    )
            }

        val geometry =
            boardGeometryPolicy.resolve(
                mode =
                    selectedGameMode,
                windowWidth =
                    screenRoot.width,
                windowHeight =
                    screenRoot.height,
                proposed =
                    proposed
            )

        val params =
            board.layoutParams
                as? FrameLayout.LayoutParams
                ?: FrameLayout.LayoutParams(
                    geometry.width,
                    geometry.height
                )

        val changed =
            params.leftMargin !=
                geometry.left ||
                params.topMargin !=
                    geometry.top ||
                params.width !=
                    geometry.width ||
                params.height !=
                    geometry.height

        if (changed) {
            params.width =
                geometry.width
            params.height =
                geometry.height
            params.leftMargin =
                geometry.left
            params.topMargin =
                geometry.top
            params.gravity =
                Gravity.TOP or
                    Gravity.START

            board.layoutParams =
                params

            MediaTrace.event(
                source = "MainActivity",
                event =
                    "BOARD_GEOMETRY_ANCHORED",
                detail =
                    "mode=" +
                        selectedGameMode +
                        " left=" +
                        geometry.left +
                        " top=" +
                        geometry.top +
                        " width=" +
                        geometry.width +
                        " height=" +
                        geometry.height +
                        " window=" +
                        screenRoot.width +
                        "x" +
                        screenRoot.height
            )
        }

        positionSudokuLayer(
            sudokuBoard,
            geometry
        )

        positionSudokuLayer(
            sudokuValueOverlay,
            geometry
        )

        if (
            ::gomokuBoard
                .isInitialized
        ) {
            positionSudokuLayer(
                gomokuBoard,
                geometry
            )
        }

        if (
            ::beeGeckoBoard
                .isInitialized
        ) {
            positionSudokuLayer(
                beeGeckoBoard,
                geometry
            )
        }

        if (
            ::aliveMascotOverlay
                .isInitialized
        ) {
            aliveMascotOverlay
                .refreshDynamicTargets()
        }
    }

    private fun ensureBoardAnchorForMode():
        Boolean {
        val params =
            boardAnchor.layoutParams
                as? LinearLayout.LayoutParams
                ?: return true

        if (
            selectedGameMode ==
                GameMode.SUDOKU ||
            selectedGameMode ==
                GameMode.GOMOKU ||
            selectedGameMode ==
                GameMode.BEES_GECKOS
        ) {
            val boardSide =
                when (selectedGameMode) {
                    GameMode.SUDOKU ->
                        sudokuFullWidthBoardPolicy
                            .geometry(
                                usefulWidthPx =
                                    screenRoot.width,
                                topPx = 0
                            )
                            .height

                    GameMode.GOMOKU ->
                        gomokuBoardLayoutPolicy
                            .geometry(
                                usefulWidthPx =
                                    screenRoot.width,
                                topPx = 0
                            )
                            .height

                    GameMode.BEES_GECKOS ->
                        gomokuBoardLayoutPolicy
                            .geometry(
                                usefulWidthPx =
                                    screenRoot.width,
                                topPx = 0
                            )
                            .height

                    GameMode.GECKODOKU ->
                        0
                }

            val changed =
                params.height !=
                    boardSide ||
                    params.weight !=
                    0f

            if (changed) {
                params.height =
                    boardSide
                params.weight = 0f
                boardAnchor.layoutParams =
                    params
            }

            return !changed
        }

        val changed =
            params.height != 0 ||
                params.weight != 1f

        if (changed) {
            params.height = 0
            params.weight = 1f
            boardAnchor.layoutParams =
                params
        }

        return !changed
    }

    private fun positionProfessorBubble() {
        if (
            professorBubble.visibility !=
                View.VISIBLE ||
            !::screenRoot.isInitialized ||
            !::professorButton.isInitialized
        ) {
            return
        }

        val availableWidth =
            (
                screenRoot.width -
                    dp(24)
                ).coerceAtLeast(
                dp(220)
            )

        professorBubble.measure(
            View.MeasureSpec.makeMeasureSpec(
                availableWidth,
                View.MeasureSpec.EXACTLY
            ),
            View.MeasureSpec.makeMeasureSpec(
                screenRoot.height,
                View.MeasureSpec.AT_MOST
            )
        )

        val rootLocation =
            IntArray(2)

        val buttonLocation =
            IntArray(2)

        screenRoot.getLocationOnScreen(
            rootLocation
        )

        professorButton.getLocationOnScreen(
            buttonLocation
        )

        val buttonTop =
            buttonLocation[1] -
                rootLocation[1]

        val top =
            (
                buttonTop -
                    professorBubble.measuredHeight -
                    dp(8)
                ).coerceAtLeast(
                dp(8)
            )

        val params =
            professorBubble.layoutParams
                as FrameLayout.LayoutParams

        if (params.topMargin != top) {
            params.topMargin = top
            params.leftMargin = dp(12)
            params.rightMargin = dp(12)
            params.gravity = Gravity.TOP
            professorBubble.layoutParams =
                params
        }
    }

    private fun mistakeCountForMode(
        mode: GameMode
    ): Int =
        when (mode) {
            GameMode.GECKODOKU ->
                engine
                    .snapshot()
                    .mistakes

            GameMode.SUDOKU ->
                sudokuEngine
                    ?.snapshot()
                    ?.mistakes
                    ?: 0

            GameMode.BEES_GECKOS ->
                beeGeckoEngine
                    ?.snapshot()
                    ?.mistakes
                    ?: 0

            GameMode.GOMOKU ->
                0
        }

    private fun recordRatedCompletionIfNeeded(
        mode: GameMode,
        size: Int,
        difficulty: GameDifficulty
    ): Int {
        val stars =
            CompletionRatingPolicy
                .starsFor(
                    assistancePoints =
                        assistancePoints,
                    mistakes =
                        mistakeCountForMode(
                            mode
                        )
                )

        if (!completionRecorded) {
            val seconds =
                (
                    SystemClock
                        .elapsedRealtime() -
                        gameStartedAt
                    ) / 1000L

            statsStore.recordComplete(
                size = size,
                difficulty = difficulty,
                elapsedSeconds = seconds,
                usedProfessor =
                    assistancePoints > 0,
                stars = stars,
                mode = mode
            )

            hallOfFameStore.add(
                HallOfFameEntry(
                    playerName =
                        playerProfileStore
                            .playerName,
                    mode = mode,
                    size = size,
                    difficulty =
                        difficulty,
                    stars = stars,
                    elapsedSeconds =
                        seconds.coerceAtLeast(
                            0L
                        ),
                    completedAt =
                        System.currentTimeMillis()
                )
            )

            completionRecorded = true
        }

        return stars
    }

    private fun completeGame(
        playCelebrationMusicImmediately: Boolean = true
    ) {
        fx.stopCelebration()

        if (::richMediaOverlay.isInitialized) {
            richMediaOverlay.stop()
        }

        if (::professorSpeech.isInitialized) {
            professorSpeech.stop(
                reason =
                    SpeechStopReason
                        .END_GAME,
                caller =
                    "MainActivity.completeGame"
            )
        }

        clearProfessorSession()

        val stars =
            recordRatedCompletionIfNeeded(
                mode =
                    GameMode.GECKODOKU,
                size =
                    puzzle.size,
                difficulty =
                    puzzle.difficulty
            )

        status.text =
            "Bravo ! Grille terminée 🦎  " +
                CompletionRatingPolicy
                    .symbols(stars)

        celebrationUsesMusic = true

        if (::board.isInitialized) {
            board.startVictoryAnimation()
        }

        if (playCelebrationMusicImmediately) {
            startCelebrationMusic()
        }

        if (
            ::celebrationView.isInitialized
        ) {
            celebrationView.start(
                puzzle.difficulty
            )
        }

        board.announceForAccessibility(
            "Bravo, grille terminée. Félicitations !"
        )
    }

    private fun placePendingMarkerIfNeeded(
        cell: Cell
    ): Boolean {
        if (
            pendingMarker == null &&
            !eraseMarkerMode
        ) {
            return false
        }

        val result =
            engine.placeCustomMarker(
                cell,
                if (eraseMarkerMode) {
                    null
                } else {
                    pendingMarker
                }
            )

        pendingMarker = null
        eraseMarkerMode = false

        fx.marker()

        status.text =
            if (
                result ==
                ActionFeedback
                    .CUSTOM_MARKER_CLEARED
            ) {
                "Repère effacé."
            } else {
                "Repère personnel posé."
            }

        board.invalidate()
        return true
    }

    private fun chooseSize() {
        val values =
            intArrayOf(
                5,
                6,
                7,
                8,
                9,
                10,
                11,
                12
            )

        val labels =
            values.map {
                it.toString() +
                    "×" +
                    it
            }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(
                "Taille de grille"
            )
            .setSingleChoiceItems(
                labels,
                values.indexOf(
                    selectedSize
                )
            ) { dialog, which ->
                selectedSize =
                    values[which]

                gameModePreferences
                    .classicSize =
                    selectedSize

                dialog.dismiss()

                requestClassicPuzzle(
                    recordStart = true
                )
            }
            .show()
    }

    private fun chooseDifficulty() {
        val values =
            GameDifficulty.entries

        val labels =
            values.map {
                it.label
            }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(
                when (selectedGameMode) {
                    GameMode.SUDOKU ->
                        "Difficulté Sudoku • nouvelle grille"

                    GameMode.GOMOKU ->
                        "Difficulté stratégique de Pierre"

                    GameMode.BEES_GECKOS ->
                        "Difficulté Abeilles & Geckos"

                    GameMode.GECKODOKU ->
                        "Difficulté logique réelle"
                }
            )
            .setSingleChoiceItems(
                labels,
                values.indexOf(
                    selectedDifficulty
                )
            ) { dialog, which ->
                selectedDifficulty =
                    values[which]

                gameModePreferences
                    .selectedDifficulty =
                    selectedDifficulty

                dialog.dismiss()

                when (selectedGameMode) {
                    GameMode.GECKODOKU ->
                        requestClassicPuzzle(
                            recordStart = true
                        )

                    GameMode.BEES_GECKOS ->
                        requestBeeGeckoPuzzle(
                            recordStart = true
                        )

                    else -> {
                        createActivePuzzle()
                        refreshGameUi()
                    }
                }
            }
            .show()
    }

    private fun showMarkerPalette() {
        val markers =
            CustomMarker.entries

        val labels =
            markers.map {
                it.symbol +
                    "  " +
                    it.label
            }.toMutableList()

        labels.add(
            "⌫  Effacer un repère"
        )

        AlertDialog.Builder(this)
            .setTitle(
                "Choisir un repère"
            )
            .setItems(
                labels.toTypedArray()
            ) { _, which ->
                if (
                    which ==
                    markers.size
                ) {
                    pendingMarker = null
                    eraseMarkerMode = true

                    status.text =
                        "Touchez une case pour effacer son repère."
                } else {
                    pendingMarker =
                        markers[which]

                    eraseMarkerMode = false

                    status.text =
                        "Touchez une case pour placer : " +
                            markers[which].label +
                            "."
                }

                fx.marker()
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun showStats() {
        val s =
            statsStore.read()

        val text =
            buildString {
                append(
                    "Joueur : "
                )
                append(
                    playerProfileStore
                        .playerName
                )
                append("\n\n")

                append(
                    "Statistiques locales uniquement\n\n"
                )

                append(
                    "Parties lancées : "
                )
                append(
                    s.gamesStarted
                )
                append("\n")

                append(
                    "Parties terminées : "
                )
                append(
                    s.gamesCompleted
                )
                append("\n")

                append(
                    "Terminées avec Prof : "
                )
                append(
                    s.assistedCompleted
                )
                append("\n")

                append(
                    "Réussite globale : "
                )
                append(
                    s.completionRate
                )
                append("%\n")

                append(
                    "Erreurs : "
                )
                append(
                    s.mistakes
                )
                append("\n")

                append(
                    "Temps moyen terminé : "
                )
                append(
                    formatSeconds(
                        s.averageSeconds
                    )
                )
                append("\n\n")

                append(
                    "Réussite et étoiles par difficulté :\n"
                )

                for (
                    d in
                    GameDifficulty.entries
                ) {
                    val ds =
                        statsStore
                            .statsForDifficulty(
                                d
                            )

                    append(d.label)
                    append(" : ")

                    if (ds.started == 0) {
                        append(
                            "— (0 partie)"
                        )
                    } else {
                        append(
                            ds.completionRate
                        )
                        append("%  •  ")
                        append(
                            ds.completed
                        )
                        append("/")
                        append(
                            ds.started
                        )

                        if (
                            ds.assistedCompleted > 0
                        ) {
                            append(
                                " • Prof "
                            )
                            append(
                                ds.assistedCompleted
                            )
                        }

                        if (
                            ds.completed > 0 &&
                            ds.bestStars > 0
                        ) {
                            append("\n    meilleur ")
                            append(
                                CompletionRatingPolicy
                                    .symbols(
                                        ds.bestStars
                                    )
                            )
                            append(
                                " • moyenne "
                            )
                            append(
                                ds.averageStars
                            )
                            append("★")
                        }
                    }

                    append("\n")
                }

                append(
                    "\nDétail par mode et difficulté :\n"
                )

                for (
                    mode in
                    GameMode.entries
                ) {
                    append("\n")
                    append(
                        gameModeLabel(mode)
                    )
                    append("\n")

                    for (
                        d in
                        GameDifficulty.entries
                    ) {
                        val ds =
                            statsStore
                                .statsForModeAndDifficulty(
                                    mode,
                                    d
                                )

                        if (
                            ds.started == 0 &&
                            ds.completed == 0
                        ) {
                            continue
                        }

                        append("  ")
                        append(d.label)
                        append(" : ")
                        append(ds.completed)
                        append("/")
                        append(ds.started)

                        if (
                            ds.completed > 0
                        ) {
                            append(" • meilleur ")
                            append(
                                CompletionRatingPolicy
                                    .symbols(
                                        ds.bestStars
                                    )
                            )
                        }

                        append("\n")
                    }
                }

                append(
                    "\nTerminées par taille :\n"
                )

                for (size in 5..12) {
                    append(size)
                    append("×")
                    append(size)
                    append(" : ")
                    append(
                        statsStore
                            .completedForSize(
                                size
                            )
                    )
                    append("\n")
                }

                append(
                    "\n5 étoiles = sans aide et sans erreur. Une erreur coûte 3 étoiles."
                )
            }

        AlertDialog.Builder(this)
            .setTitle(
                "Stats du joueur"
            )
            .setMessage(text)
            .setPositiveButton(
                "OK",
                null
            )
            .setNeutralButton(
                "🏆 Hall of Fame"
            ) {
                    _,
                    _ ->
                showHallOfFame()
            }
            .show()
    }

    private fun showHallOfFame() {
        val entries =
            hallOfFameStore.entries()

        if (entries.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle(
                    "🏆 Hall of Fame"
                )
                .setMessage(
                    "Aucune partie terminée n'est encore classée."
                )
                .setPositiveButton(
                    "OK",
                    null
                )
                .show()
            return
        }

        val dateFormat =
            SimpleDateFormat(
                "dd/MM/yy",
                Locale.getDefault()
            )

        val text =
            buildString {
                for (
                    mode in
                    GameMode.entries
                ) {
                    val modeEntries =
                        entries.filter {
                            it.mode == mode
                        }

                    if (modeEntries.isEmpty()) {
                        continue
                    }

                    if (isNotEmpty()) {
                        append("\n")
                    }

                    append(
                        gameModeLabel(mode)
                    )
                    append("\n")

                    for (
                        difficulty in
                        GameDifficulty.entries
                    ) {
                        val levelEntries =
                            modeEntries.filter {
                                it.difficulty ==
                                    difficulty
                            }

                        if (levelEntries.isEmpty()) {
                            continue
                        }

                        append(
                            "  " +
                                difficulty.label +
                                "\n"
                        )

                        levelEntries
                            .take(10)
                            .forEachIndexed {
                                    index,
                                    entry ->

                                append(
                                    "  " +
                                        (index + 1) +
                                        ". "
                                )
                                append(
                                    CompletionRatingPolicy
                                        .symbols(
                                            entry.stars
                                        )
                                )
                                append(" • ")
                                append(entry.playerName)

                                if (
                                    entry.mode !=
                                        GameMode.GOMOKU
                                ) {
                                    append(" • ")
                                    append(
                                        entry.size
                                            .toString() +
                                            "×" +
                                            entry.size
                                    )
                                }

                                append(" • ")
                                append(
                                    formatSeconds(
                                        entry.elapsedSeconds
                                    )
                                )
                                append(" • ")
                                append(
                                    dateFormat.format(
                                        Date(
                                            entry.completedAt
                                        )
                                    )
                                )
                                append("\n")
                            }
                    }
                }
            }

        val view =
            TextView(this).apply {
                this.text = text
                textSize = 17f
                setTextIsSelectable(true)
                setPadding(
                    dp(18),
                    dp(12),
                    dp(18),
                    dp(12)
                )
            }

        val scroll =
            ScrollView(this).apply {
                addView(view)
            }

        AlertDialog.Builder(this)
            .setTitle(
                "🏆 Hall of Fame"
            )
            .setView(scroll)
            .setPositiveButton(
                "Fermer",
                null
            )
            .show()
    }

    private fun editPlayerName() {
        val input =
            EditText(this).apply {
                setSingleLine(true)
                setText(
                    playerProfileStore
                        .playerName
                )
                selectAll()
                contentDescription =
                    "Nom du joueur"
            }

        AlertDialog.Builder(this)
            .setTitle(
                "Nom du joueur"
            )
            .setMessage(
                "Ce nom sera utilisé pour les nouveaux résultats du Hall of Fame."
            )
            .setView(input)
            .setPositiveButton(
                "Enregistrer"
            ) {
                    _,
                    _ ->

                playerProfileStore
                    .playerName =
                    input.text
                        ?.toString()
                        .orEmpty()

                status.text =
                    "Joueur : " +
                        playerProfileStore
                            .playerName
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun confirmClearResultHistory() {
        AlertDialog.Builder(this)
            .setTitle(
                "Vider l'historique ?"
            )
            .setMessage(
                "Les entrées du Hall of Fame seront supprimées. Les statistiques agrégées, réglages et grilles sauvegardées restent intactes."
            )
            .setPositiveButton(
                "Vider"
            ) {
                    _,
                    _ ->

                hallOfFameStore.clear()
                status.text =
                    "Historique du Hall of Fame vidé."
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun exportUserData() {
        val stamp =
            SimpleDateFormat(
                "yyyyMMdd-HHmm",
                Locale.getDefault()
            ).format(
                Date()
            )

        val intent =
            Intent(
                Intent.ACTION_CREATE_DOCUMENT
            ).apply {
                addCategory(
                    Intent.CATEGORY_OPENABLE
                )
                type =
                    "application/json"
                putExtra(
                    Intent.EXTRA_TITLE,
                    "GeckoDoku-backup-" +
                        stamp +
                        ".json"
                )
            }

        startActivityForResult(
            intent,
            exportDataRequestCode
        )
    }

    private fun importUserData() {
        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {
                addCategory(
                    Intent.CATEGORY_OPENABLE
                )
                type =
                    "application/json"
            }

        startActivityForResult(
            intent,
            importDataRequestCode
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            resultCode !=
                RESULT_OK
        ) {
            return
        }

        val uri =
            data?.data
                ?: return

        when (requestCode) {
            exportDataRequestCode -> {
                try {
                    val json =
                        userDataBackup
                            .exportJson()

                    val output =
                        contentResolver
                            .openOutputStream(
                                uri
                            )
                            ?: throw IllegalStateException(
                                "Impossible d'ouvrir le fichier de destination."
                            )

                    output.bufferedWriter(
                        Charsets.UTF_8
                    ).use {
                        writer ->
                        writer.write(json)
                    }

                    status.text =
                        "Export GeckoDoku terminé 📤"
                } catch (
                    error: Exception
                ) {
                    showDataTransferError(
                        title =
                            "Export impossible",
                        message =
                            error.message
                                ?: "Erreur d'écriture."
                    )
                }
            }

            importDataRequestCode -> {
                try {
                    val input =
                        contentResolver
                            .openInputStream(
                                uri
                            )
                            ?: throw IllegalStateException(
                                "Impossible d'ouvrir le fichier."
                            )

                    val json =
                        input.bufferedReader(
                            Charsets.UTF_8
                        ).use {
                            reader ->
                            reader.readText()
                        }

                    val result =
                        userDataBackup
                            .importJson(json)

                    if (result.success) {
                        status.text =
                            "Import terminé. GeckoDoku recharge les données…"
                        recreate()
                    } else {
                        showDataTransferError(
                            title =
                                "Import refusé",
                            message =
                                result.message
                        )
                    }
                } catch (
                    error: Exception
                ) {
                    showDataTransferError(
                        title =
                            "Import impossible",
                        message =
                            error.message
                                ?: "Fichier invalide."
                    )
                }
            }
        }
    }

    private fun showDataTransferError(
        title: String,
        message: String
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                "OK",
                null
            )
            .show()
    }

    private fun applyProfessorIntroVisibility() {
        if (
            !::professorButtonHost
                .isInitialized
        ) {
            return
        }

        val eligible =
            introLifecyclePolicy
                .professorEligible(
                    introPhase
                )

        professorButtonHost.visibility =
            if (eligible) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

        MediaTrace.event(
            source = "MainActivity",
            event =
                "PROF_INTRO_ELIGIBILITY",
            detail =
                "phase=" +
                    introPhase +
                    " eligible=" +
                    eligible
        )

        if (eligible) {
            scheduleProfessorIdleAnimation()
        } else {
            cancelProfessorIdleAnimation()
        }
    }

    private fun positionTitleIdentity() {
        if (
            !::screenRoot.isInitialized ||
            !::titleView.isInitialized ||
            !::titleIdentityHost.isInitialized ||
            titleView.width <= 0 ||
            titleView.height <= 0
        ) {
            return
        }

        val rootLocation = IntArray(2)
        val titleLocation = IntArray(2)

        screenRoot.getLocationOnScreen(
            rootLocation
        )
        titleView.getLocationOnScreen(
            titleLocation
        )

        val frameSize =
            dp(
                titleIdentityPolicy
                    .iconSizeDp +
                    titleIdentityPolicy
                        .frameExtraDp
            )

        val textWidth =
            titleView.paint
                .measureText(
                    titleView.text
                        .toString()
                )

        val left =
            (
                titleLocation[0] -
                    rootLocation[0] +
                    (
                        titleView.width -
                            textWidth
                        ) / 2f -
                    frameSize -
                    dp(
                        titleIdentityPolicy
                            .gapDp
                    )
                )
                .toInt()
                .coerceAtLeast(
                    dp(4)
                )

        val top =
            (
                titleLocation[1] -
                    rootLocation[1] +
                    (
                        titleView.height -
                            frameSize
                        ) / 2f
                )
                .toInt()
                .coerceAtLeast(0)

        val params =
            titleIdentityHost
                .layoutParams as
                FrameLayout.LayoutParams

        params.leftMargin = left
        params.topMargin = top
        params.gravity =
            Gravity.TOP or
                Gravity.START
        titleIdentityHost.layoutParams =
            params
    }

    private fun startTitleIdentityAnimation() {
        if (
            !::titleIdentityHost.isInitialized ||
            !richMediaSettings.enabled ||
            titleIdentityHost.visibility !=
                View.VISIBLE
        ) {
            return
        }

        animateTitleIdentity(
            expanded = true
        )
    }

    private fun animateTitleIdentity(
        expanded: Boolean
    ) {
        if (
            !::titleIdentityHost.isInitialized ||
            !richMediaSettings.enabled
        ) {
            return
        }

        val scale =
            if (expanded) 1.045f else 1f

        titleIdentityHost
            .animate()
            .cancel()

        titleIdentityHost
            .animate()
            .scaleX(scale)
            .scaleY(scale)
            .rotation(
                if (expanded) {
                    1.6f
                } else {
                    -1.2f
                }
            )
            .alpha(
                if (expanded) {
                    0.96f
                } else {
                    1f
                }
            )
            .setDuration(
                titleIdentityPolicy
                    .animationDurationMs
            )
            .withEndAction {
                animateTitleIdentity(
                    !expanded
                )
            }
            .start()
    }

    private fun stopTitleIdentityAnimation() {
        if (!::titleIdentityHost.isInitialized) {
            return
        }

        titleIdentityHost
            .animate()
            .cancel()

        titleIdentityHost.scaleX = 1f
        titleIdentityHost.scaleY = 1f
        titleIdentityHost.rotation = 0f
        titleIdentityHost.alpha = 1f
    }

    private fun resetProfessorAmbientState() {
        val now =
            SystemClock
                .elapsedRealtime()

        lastBoardActionAtMs = now
        boardActionCount = 0
        ambientHelpOffered = false
        ambientSaveOffered = false
        nextAmbientAllowedAtMs =
            now +
                professorAmbientPolicy
                    .minimumAmbientGapMs
        nextSmallTalkAtMs =
            now +
                professorAmbientPolicy
                    .smallTalkDelayMs(
                        Random.nextInt()
                    )
    }

    private fun recordBoardAction() {
        lastBoardActionAtMs =
            SystemClock
                .elapsedRealtime()
        boardActionCount += 1
        ambientHelpOffered = false
    }

    private fun scheduleProfessorAmbientTick() {
        if (!::screenRoot.isInitialized) {
            return
        }

        cancelProfessorAmbientTick()

        screenRoot.postDelayed(
            professorAmbientRunnable,
            professorAmbientPolicy
                .tickMs
        )
    }

    private fun cancelProfessorAmbientTick() {
        if (::screenRoot.isInitialized) {
            screenRoot.removeCallbacks(
                professorAmbientRunnable
            )
        }
    }

    private fun ambientSpeechBlocked(): Boolean {
        if (
            !fx.enabled ||
            professorSpeechActive ||
            professorSpeech.isBusy ||
            !introLifecyclePolicy
                .professorEligible(
                    introPhase
                ) ||
            !hasWindowFocus() ||
            isCurrentGameComplete() ||
            pendingProfessorHypothesis !=
                null
        ) {
            return true
        }

        if (
            ::professorBubble.isInitialized &&
            professorBubble.visibility ==
                View.VISIBLE
        ) {
            return true
        }

        return (
            ::celebrationView.isInitialized &&
                celebrationView.visibility ==
                    View.VISIBLE
            )
    }

    private fun runProfessorAmbientTick() {
        val now =
            SystemClock
                .elapsedRealtime()

        val blocked =
            ambientSpeechBlocked() ||
                now <
                    nextAmbientAllowedAtMs

        if (
            professorAmbientPolicy
                .shouldOfferHelp(
                    nowMs = now,
                    lastBoardActionAtMs =
                        lastBoardActionAtMs,
                    alreadyOffered =
                        ambientHelpOffered,
                    blocked = blocked
                )
        ) {
            ambientHelpOffered = true

            speakProfessorAmbient(
                "Vous réfléchissez depuis un moment. Si vous voulez un coup de main, appuyez sur le bouton Prof Gecko : je vous montrerai la prochaine étape."
            )

            scheduleProfessorAmbientTick()
            return
        }

        if (
            professorAmbientPolicy
                .shouldOfferSave(
                    nowMs = now,
                    gameStartedAtMs =
                        gameStartedAt,
                    boardActionCount =
                        boardActionCount,
                    alreadyOffered =
                        ambientSaveOffered,
                    alreadySaved =
                        selectedGameMode !=
                            GameMode.GECKODOKU ||
                        journalStore
                            .contains(
                                puzzle.id
                            ),
                    blocked = blocked
                )
        ) {
            ambientSaveOffered = true

            speakProfessorAmbient(
                "Cette partie commence à être longue. Si vous voulez la reprendre plus tard, vous pouvez utiliser le bouton Sauver."
            )

            scheduleProfessorAmbientTick()
            return
        }

        if (
            professorAmbientPolicy
                .canSpeakSmallTalk(
                    nowMs = now,
                    nextSmallTalkAtMs =
                        nextSmallTalkAtMs,
                    blocked = blocked
                )
        ) {
            professorLife.observe(
                ProfessorPlayerEvent.AMBIENT,
                currentDifficulty()
            )

            if (
                speakLivingProfessor(
                    event =
                        ProfessorPlayerEvent
                            .AMBIENT,
                    origin =
                        SpeechOrigin.AMBIENT
                )
            ) {
                nextAmbientAllowedAtMs =
                    now +
                        professorAmbientPolicy
                            .minimumAmbientGapMs

                nextSmallTalkAtMs =
                    now +
                        professorAmbientPolicy
                            .smallTalkDelayMs(
                                Random.nextInt()
                            )
            }
        }

        scheduleProfessorAmbientTick()
    }

    private fun speakProfessorAmbient(
        message: String
    ) {
        val accepted =
            speakSimpleProfessorBubble(
                text = message,
                origin =
                    SpeechOrigin.AMBIENT
            )

        if (!accepted) {
            return
        }

        val now =
            SystemClock
                .elapsedRealtime()

        nextAmbientAllowedAtMs =
            now +
                professorAmbientPolicy
                    .minimumAmbientGapMs

        nextSmallTalkAtMs =
            now +
                professorAmbientPolicy
                    .smallTalkDelayMs(
                        Random.nextInt()
                    )
    }

    private fun encouragement(
        remaining: Int
    ): String {
        val mistakes =
            engine.mistakes

        return when {
            mistakes == 0 &&
                remaining == 1 ->
                "Excellent, encore un seul gecko !"

            mistakes == 0 &&
                remaining <= 2 ->
                "Super, tu approches de la fin sans erreur."

            remaining <= 2 ->
                "Presque terminé : encore " +
                    remaining +
                    " geckos."

            else ->
                "Bien vu. Encore " +
                    remaining +
                    " geckos."
        }
    }

    private fun formatSeconds(
        total: Long
    ): String {
        val minutes =
            total / 60

        val seconds =
            total % 60

        return minutes.toString() +
            " min " +
            seconds +
            " s"
    }

    private fun protectFromSystemBars(
        root: LinearLayout
    ) {
        root.setOnApplyWindowInsetsListener {
                view,
                insets ->

            val base =
                dp(8)

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.R
            ) {
                val bars =
                    insets.getInsets(
                        WindowInsets.Type
                            .systemBars()
                    )

                view.setPadding(
                    base + bars.left,
                    base + bars.top,
                    base + bars.right,
                    base + bars.bottom
                )

                boardGeometryPolicy.reset()

                if (
                    ::screenRoot.isInitialized &&
                    ::boardAnchor.isInitialized
                ) {
                    view.post {
                        positionFloatingBoard()
                    }
                }
            } else {
                @Suppress(
                    "DEPRECATION"
                )

                view.setPadding(
                    base +
                        insets
                            .systemWindowInsetLeft,
                    base +
                        insets
                            .systemWindowInsetTop,
                    base +
                        insets
                            .systemWindowInsetRight,
                    base +
                        insets
                            .systemWindowInsetBottom
                )

                boardGeometryPolicy.reset()

                if (
                    ::screenRoot.isInitialized &&
                    ::boardAnchor.isInitialized
                ) {
                    view.post {
                        positionFloatingBoard()
                    }
                }
            }

            insets
        }

        root.requestApplyInsets()
    }

    private fun toggleSoundSetting() {
        fx.enabled =
            !fx.enabled

        playerProfileStore
            .soundEnabled =
            fx.enabled

        gameAudio.enabled =
            fx.enabled
        professorSpeech.enabled =
            fx.enabled

        if (::richMediaOverlay.isInitialized) {
            richMediaOverlay.setMuted(
                !fx.enabled
            )
        }

        status.text =
            if (fx.enabled) {
                "Son activé 🔊"
            } else {
                "Son désactivé 🔇"
            }
    }

    private fun toggleAnimationSetting() {
        richMediaSettings.enabled =
            !richMediaSettings.enabled

        if (
            ::aliveMascotOverlay
                .isInitialized
        ) {
            aliveMascotOverlay
                .animationsEnabled =
                richMediaSettings.enabled

            ensurePlantMascot()
        }

        MediaTrace.event(
            source = "MainActivity",
            event = "ANIM_TOGGLE_USER",
            detail =
                "enabled=" +
                    richMediaSettings.enabled
        )

        if (!richMediaSettings.enabled) {
            if (::richMediaOverlay.isInitialized) {
                richMediaOverlay.stop()
            }

            introPhase =
                IntroPhase.DONE
            applyProfessorIntroVisibility()
            stopProfessorButtonVideo()
            stopTitleIdentityAnimation()
            cancelProfessorIdleAnimation()
        } else {
            startTitleIdentityAnimation()
            scheduleProfessorIdleAnimation()
        }

        if (
            ::sudokuValueOverlay
                .isInitialized
        ) {
            sudokuValueOverlay
                .animateGeckoMarkers =
                richMediaSettings.enabled
        }

        if (
            ::gomokuBoard
                .isInitialized
        ) {
            gomokuBoard
                .animationsEnabled =
                richMediaSettings.enabled
            gomokuBoard.invalidate()
        }

        status.text =
            if (richMediaSettings.enabled) {
                "Habillage animé activé 🎬"
            } else {
                "Habillage animé désactivé."
            }
    }

    private fun createSudokuControlsPanel():
        LinearLayout {
        sudokuUndoButton =
            Button(this).apply {
                text = "↶ Annuler"
                textSize = 15f
                minHeight = dp(40)
                contentDescription =
                    "Annuler"
                setOnClickListener {
                    undoSudoku()
                }
            }

        sudokuRedoButton =
            Button(this).apply {
                text = "↷ Refaire"
                textSize = 15f
                minHeight = dp(40)
                contentDescription =
                    "Refaire"
                setOnClickListener {
                    redoSudoku()
                }
            }

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL

            addView(
                sudokuUndoButton,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                sudokuRedoButton,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )
        }
    }

    private fun showGameModeChooser() {
        val labels =
            arrayOf(
                "🦎 GeckoDoku",
                "🔢 Sudoku",
                "🟩 Gomoku contre Prof Gecko",
                "👥 Gomoku humain contre humain",
                "🐝 Abeilles & Geckos"
            )

        val checked =
            when (
                selectedGameMode
            ) {
                GameMode.GECKODOKU ->
                    0

                GameMode.SUDOKU ->
                    1

                GameMode.GOMOKU ->
                    if (
                        gomokuMatchMode ==
                            GomokuMatchMode
                                .VS_PROFESSOR
                    ) {
                        2
                    } else {
                        3
                    }

                GameMode.BEES_GECKOS ->
                    4
            }

        AlertDialog.Builder(this)
            .setTitle("Mode de jeu")
            .setSingleChoiceItems(
                labels,
                checked
            ) {
                    dialog,
                    which ->

                when (which) {
                    0 ->
                        setGameMode(
                            GameMode.GECKODOKU
                        )

                    1 ->
                        setGameMode(
                            GameMode.SUDOKU
                        )

                    2 ->
                        selectGomokuMatchMode(
                            GomokuMatchMode
                                .VS_PROFESSOR
                        )

                    3 ->
                        selectGomokuMatchMode(
                            GomokuMatchMode
                                .HUMAN_VS_HUMAN
                        )

                    4 ->
                        setGameMode(
                            GameMode
                                .BEES_GECKOS
                        )
                }

                dialog.dismiss()
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun selectGomokuMatchMode(
        matchMode: GomokuMatchMode
    ) {
        if (
            selectedGameMode ==
                GameMode.GOMOKU &&
            gomokuMatchMode ==
                matchMode
        ) {
            return
        }

        val hadGomokuEngine =
            gomokuEngine != null

        gomokuMatchMode =
            matchMode

        gameModePreferences
            .gomokuMatchMode =
            matchMode

        if (
            selectedGameMode !=
                GameMode.GOMOKU
        ) {
            setGameMode(
                GameMode.GOMOKU
            )

            if (hadGomokuEngine) {
                startGomokuGame()
            }
        } else {
            startGomokuGame()
        }

        applyGameModeVisibility()
        refreshGameUi()

        status.text =
            if (
                gomokuMatchMode ==
                    GomokuMatchMode
                        .VS_PROFESSOR
            ) {
                "Gomoku contre Prof Gecko. À toi de jouer vert 🦎"
            } else {
                "Gomoku humain contre humain. Au joueur Vert."
            }
    }

    private fun setGameMode(
        mode: GameMode
    ) {
        if (
            mode ==
                selectedGameMode
        ) {
            return
        }

        if (
            selectedGameMode ==
                GameMode.GOMOKU &&
            mode !=
                GameMode.GOMOKU
        ) {
            stopGomokuPieceMedia()
        }

        if (
            selectedGameMode ==
                GameMode.BEES_GECKOS &&
            mode !=
                GameMode.BEES_GECKOS
        ) {
            cancelBeeGeckoPuzzleSearch(
                announce = false
            )
            persistBeeGeckoSession()
        }

        if (
            selectedGameMode ==
                GameMode.GECKODOKU &&
            mode !=
                GameMode.GECKODOKU
        ) {
            cancelClassicPuzzleSearch(
                announce = false
            )
        }

        clearProfessorSession()
        cancelSudokuReasoningPresentation()
        sudokuProfessorInteractionPolicy
            .invalidate()
        dismissSudokuPalette()

        if (
            ::sudokuValueOverlay
                .isInitialized
        ) {
            sudokuValueOverlay
                .clearProfessorCandidates()
        }

        gomokuGeneration += 1
        gomokuProfessorThinking = false

        if (
            ::aliveMascotOverlay
                .isInitialized
        ) {
            aliveMascotOverlay
                .stopBoardMascots()
        }

        selectedGameMode = mode
        gameModePreferences.gameMode =
            mode

        selectedDifficulty =
            when (mode) {
                GameMode.SUDOKU ->
                    sudokuPuzzle
                        ?.difficulty
                        ?: selectedDifficulty

                GameMode.GECKODOKU ->
                    puzzle.difficulty

                GameMode.GOMOKU ->
                    selectedDifficulty

                GameMode.BEES_GECKOS ->
                    beeGeckoPuzzle
                        ?.difficulty
                        ?: selectedDifficulty
            }

        when (mode) {
            GameMode.SUDOKU ->
                if (
                    sudokuEngine ==
                        null
                ) {
                    startSudokuPuzzle(
                        SudokuGenerator
                            .generate(
                                selectedDifficulty
                            )
                    )
                }

            GameMode.GOMOKU ->
                if (
                    gomokuEngine ==
                        null
                ) {
                    startGomokuGame()
                }

            GameMode.BEES_GECKOS ->
                if (
                    beeGeckoEngine ==
                        null
                ) {
                    startBeeGeckoGame(
                        restoreSaved = true
                    )
                }

            GameMode.GECKODOKU ->
                Unit
        }

        boardGeometryPolicy
            .reset(mode)

        applyGameModeVisibility()
        refreshGameUi()

        status.text =
            when (mode) {
                GameMode.SUDOKU ->
                    "Mode Sudoku activé 🔢"

                GameMode.GOMOKU ->
                    if (
                        gomokuMatchMode ==
                            GomokuMatchMode
                                .VS_PROFESSOR
                    ) {
                        "Gomoku contre Prof Gecko. Pose ton Gecko vert 🦎"
                    } else {
                        "Gomoku humain contre humain. Au joueur Vert."
                    }

                GameMode.BEES_GECKOS ->
                    "Mode Abeilles & Geckos activé 🐝🦎"

                GameMode.GECKODOKU ->
                    "Mode GeckoDoku activé 🦎"
            }
    }

    private fun startGomokuGame(
        boardSize: Int =
            GomokuGameEngine
                .DEFAULT_SIZE
    ) {
        gomokuGeneration += 1
        gomokuProfessorThinking =
            false

        gomokuEngine =
            GomokuGameEngine(
                boardSize
            )

        if (
            ::gomokuBoard
                .isInitialized
        ) {
            gomokuBoard.reset(
                boardSize
            )
            gomokuBoard
                .animationsEnabled =
                richMediaSettings.enabled
        }

        if (
            ::richMediaOverlay
                .isInitialized
        ) {
            richMediaOverlay.stop()
        }

        if (
            ::aliveMascotOverlay
                .isInitialized
        ) {
            aliveMascotOverlay
                .stopBoardMascots()
        }

        if (
            ::professorSpeech
                .isInitialized
        ) {
            professorSpeech.stop(
                reason =
                    SpeechStopReason
                        .PUZZLE_RESET,
                caller =
                    "MainActivity.startGomokuGame"
            )
        }

        completionRecorded = false
        professorUsed = false
        assistancePoints = 0

        statsStore.recordStart(
            size = boardSize,
            difficulty =
                selectedDifficulty,
            mode =
                GameMode.GOMOKU
        )

        gameStartedAt =
            SystemClock.elapsedRealtime()

        if (
            ::professorLife
                .isInitialized
        ) {
            professorLife.observe(
                ProfessorPlayerEvent
                    .GAME_STARTED,
                selectedDifficulty
            )
        }

        resetProfessorAmbientState()
        clearProfessorSession()
    }

    private fun refreshGomokuUi() {
        val engine =
            gomokuEngine
                ?: return

        if (
            ::gomokuBoard
                .isInitialized
        ) {
            gomokuBoard
                .animationsEnabled =
                richMediaSettings.enabled
            gomokuBoard.invalidate()
        }

        titleView.text =
            AppTitlePolicy
                .titleFor(
                    GameMode.GOMOKU
                )

        refreshGomokuInfo()

        difficultyButton.text =
            selectedDifficulty.label

        val snapshot =
            engine.snapshot()

        professorButton.text =
            when {
                snapshot.gameOver ->
                    "🧑‍🏫 Partie terminée"

                gomokuProfessorThinking ->
                    "🧑‍🏫 Prof Gecko réfléchit…"

                else ->
                    "🧑‍🏫 Prof Gecko"
            }

        professorButton.isEnabled =
            GomokuMatchPolicy
                .professorCanAdvise(
                    mode =
                        gomokuMatchMode,
                    snapshot =
                        snapshot,
                    thinking =
                        gomokuProfessorThinking
                )
    }

    private fun refreshGomokuInfo() {
        if (
            !::info.isInitialized
        ) {
            return
        }

        val snapshot =
            gomokuEngine
                ?.snapshot()
                ?: return

        val span =
            if (
                ::gomokuBoard
                    .isInitialized
            ) {
                gomokuBoard
                    .currentViewport()
                    .visibleSpan
                    .toInt()
                    .coerceAtLeast(1)
            } else {
                12
            }

        val matchText =
            if (
                gomokuMatchMode ==
                    GomokuMatchMode
                        .VS_PROFESSOR
            ) {
                "Prof • " +
                    selectedDifficulty.label
            } else {
                "Humain vs humain"
            }

        info.text =
            "Gomoku " +
                snapshot.size +
                "×" +
                snapshot.size +
                " • " +
                matchText +
                " • vue ~" +
                span +
                "×" +
                span +
                " • glisser / pincer"
    }

    private fun requestBeeGeckoPuzzle(
        recordStart: Boolean
    ) {
        beeGenerationToken += 1

        val token =
            beeGenerationToken

        val requestedDifficulty =
            selectedDifficulty

        beeGenerationActive = true

        if (::newButton.isInitialized) {
            newButton.text =
                "✕ Annuler"
        }

        if (::status.isInitialized) {
            status.text =
                "Recherche d'une grille Abeilles & Geckos " +
                    requestedDifficulty.label +
                    "…"
        }

        Thread {
            val generated =
                BeeGeckoGenerator
                    .generateExact(
                        requested =
                            requestedDifficulty,
                        shouldCancel = {
                            token !=
                                beeGenerationToken ||
                                Thread
                                    .currentThread()
                                    .isInterrupted
                        },
                        onBatchCompleted = {
                            batch ->

                            if (
                                batch == 1 ||
                                batch % 2 ==
                                    0
                            ) {
                                runOnUiThread {
                                    if (
                                        token ==
                                            beeGenerationToken &&
                                        beeGenerationActive &&
                                        selectedGameMode ==
                                            GameMode
                                                .BEES_GECKOS
                                    ) {
                                        status.text =
                                            "Recherche Abeilles & Geckos " +
                                                requestedDifficulty.label +
                                                "… série " +
                                                (batch + 1)
                                    }
                                }
                            }
                        }
                    )

            runOnUiThread {
                if (
                    token !=
                        beeGenerationToken
                ) {
                    return@runOnUiThread
                }

                beeGenerationActive =
                    false

                if (::newButton.isInitialized) {
                    newButton.text =
                        "↻ Nouvelle"
                }

                if (
                    generated == null ||
                    selectedGameMode !=
                        GameMode.BEES_GECKOS ||
                    selectedDifficulty !=
                        requestedDifficulty
                ) {
                    return@runOnUiThread
                }

                startBeeGeckoGame(
                    puzzle =
                        generated,
                    restoreSaved =
                        false,
                    recordStart =
                        recordStart
                )

                refreshGameUi()

                status.text =
                    "Grille " +
                        generated
                            .difficulty
                            .label +
                        " trouvée • " +
                        generated
                            .regionCount +
                        " zones • axes " +
                        BeeGeckoAxisGeometry
                            .legend() +
                        "."

                playLevelStartMusic()
            }
        }.start()
    }

    private fun cancelBeeGeckoPuzzleSearch(
        announce: Boolean
    ) {
        if (!beeGenerationActive) {
            return
        }

        beeGenerationToken += 1
        beeGenerationActive =
            false

        if (::newButton.isInitialized) {
            newButton.text =
                "↻ Nouvelle"
        }

        if (
            announce &&
            ::status.isInitialized
        ) {
            status.text =
                "Recherche Abeilles & Geckos annulée."
        }
    }

    private fun startBeeGeckoGame(
        puzzle: BeeGeckoPuzzle? = null,
        restoreSaved: Boolean = false,
        recordStart: Boolean = true
    ) {
        val restored =
            if (
                restoreSaved &&
                puzzle == null
            ) {
                beeGeckoSessionStore
                    .load()
            } else {
                null
            }

        val nextPuzzle =
            puzzle
                ?: restored
                    ?.puzzle
                ?: BeeGeckoGenerator
                    .generate(
                        selectedDifficulty
                    )

        beeGeckoPuzzle =
            nextPuzzle

        selectedDifficulty =
            nextPuzzle.difficulty

        gameModePreferences
            .selectedDifficulty =
            selectedDifficulty

        val restoredMatches =
            restored
                ?.puzzle
                ?.id ==
                nextPuzzle.id

        beeGeckoEngine =
            BeeGeckoGameEngine(
                puzzle =
                    nextPuzzle,
                initialGeckos =
                    if (restoredMatches) {
                        restored
                            ?.confirmedGeckos
                            ?: nextPuzzle
                                .givenGeckos
                    } else {
                        nextPuzzle
                            .givenGeckos
                    },
                initialBees =
                    if (restoredMatches) {
                        restored
                            ?.confirmedBees
                            ?: nextPuzzle
                                .givenBees
                    } else {
                        nextPuzzle
                            .givenBees
                    },
                initialCrosses =
                    if (restoredMatches) {
                        restored
                            ?.crosses
                            ?: emptySet()
                    } else {
                        emptySet()
                    },
                initialCrossStates =
                    if (restoredMatches) {
                        restored
                            ?.crossStates
                            ?: emptyMap()
                    } else {
                        emptyMap()
                    },
                initialLogicalMarkers =
                    if (restoredMatches) {
                        restored
                            ?.logicalMarkers
                            ?: emptyMap()
                    } else {
                        emptyMap()
                    },
                initialMarkers =
                    if (restoredMatches) {
                        restored
                            ?.markers
                            ?: emptyMap()
                    } else {
                        emptyMap()
                    },
                initialMistakes =
                    if (restoredMatches) {
                        restored
                            ?.mistakes
                            ?: 0
                    } else {
                        0
                    }
            )

        beeGeckoCamera =
            if (restoredMatches) {
                restored
                    ?.camera
                    ?: BeeGeckoCamera()
            } else {
                BeeGeckoCamera()
            }

        assistancePoints =
            if (restoredMatches) {
                restored
                    ?.assistancePoints
                    ?: 0
            } else {
                0
            }

        professorUsed =
            assistancePoints > 0

        completionRecorded = false

        val restoredSeconds =
            if (restoredMatches) {
                restored
                    ?.elapsedSeconds
                    ?: 0L
            } else {
                0L
            }

        gameStartedAt =
            SystemClock
                .elapsedRealtime() -
                restoredSeconds *
                    1000L

        if (
            ::beeGeckoBoard
                .isInitialized
        ) {
            beeGeckoBoard.setPuzzle(
                next =
                    nextPuzzle,
                restoredCamera =
                    if (restoredMatches) {
                        beeGeckoCamera
                    } else {
                        null
                    }
            )
        }

        if (
            ::richMediaOverlay
                .isInitialized
        ) {
            richMediaOverlay.stopKind(
                RichMediaKind
                    .BEE_APPEARANCE
            )
        }

        if (
            ::aliveMascotOverlay
                .isInitialized
        ) {
            aliveMascotOverlay
                .stopBoardMascots()
        }

        if (
            ::professorSpeech
                .isInitialized
        ) {
            professorSpeech.stop(
                reason =
                    SpeechStopReason
                        .PUZZLE_RESET,
                caller =
                    "MainActivity.startBeeGeckoGame"
            )
        }

        if (
            recordStart &&
            !restoredMatches
        ) {
            statsStore.recordStart(
                size =
                    nextPuzzle
                        .regionCount,
                difficulty =
                    nextPuzzle
                        .difficulty,
                mode =
                    GameMode
                        .BEES_GECKOS
            )
        }

        if (
            ::professorLife
                .isInitialized
        ) {
            professorLife.observe(
                ProfessorPlayerEvent
                    .GAME_STARTED,
                nextPuzzle
                    .difficulty
            )
        }

        resetProfessorAmbientState()
        clearProfessorSession()
        persistBeeGeckoSession()
    }

    private fun persistBeeGeckoSession() {
        val puzzle =
            beeGeckoPuzzle
                ?: return

        val engine =
            beeGeckoEngine
                ?: return

        if (
            ::beeGeckoBoard
                .isInitialized
        ) {
            beeGeckoCamera =
                beeGeckoBoard
                    .currentCamera()
        }

        val snapshot =
            engine.snapshot()

        val elapsed =
            if (
                gameStartedAt >
                    0L
            ) {
                (
                    SystemClock
                        .elapsedRealtime() -
                        gameStartedAt
                    )
                    .coerceAtLeast(
                        0L
                    ) /
                    1000L
            } else {
                0L
            }

        beeGeckoSessionStore.save(
            BeeGeckoSession(
                puzzle = puzzle,
                confirmedGeckos =
                    snapshot
                        .confirmedGeckos,
                confirmedBees =
                    snapshot
                        .confirmedBees,
                crosses =
                    snapshot
                        .manualCrosses,
                crossStates =
                    snapshot
                        .crossStates,
                logicalMarkers =
                    snapshot
                        .logicalMarkers,
                markers =
                    snapshot
                        .markers,
                mistakes =
                    snapshot
                        .mistakes,
                camera =
                    beeGeckoCamera,
                elapsedSeconds =
                    elapsed,
                assistancePoints =
                    assistancePoints
            )
        )
    }

    private fun refreshBeeGeckoUi() {
        val snapshot =
            beeGeckoEngine
                ?.snapshot()
                ?: return

        if (
            ::beeGeckoBoard
                .isInitialized
        ) {
            beeGeckoBoard.invalidate()
        }

        titleView.text =
            AppTitlePolicy
                .titleFor(
                    GameMode
                        .BEES_GECKOS
                )

        refreshBeeGeckoInfo()

        difficultyButton.text =
            selectedDifficulty.label

        professorButton.text =
            if (
                snapshot.complete
            ) {
                "🧑‍🏫 Grille terminée"
            } else {
                "🧑‍🏫 Prof Gecko"
            }

        professorButton.isEnabled =
            !snapshot.complete
    }

    private fun refreshBeeGeckoInfo() {
        if (
            !::info.isInitialized
        ) {
            return
        }

        val snapshot =
            beeGeckoEngine
                ?.snapshot()
                ?: return

        val puzzle =
            snapshot.puzzle

        info.text =
            "Abeilles & Geckos • " +
                puzzle.regionCount +
                " zones • " +
                snapshot
                    .confirmedPairCount +
                "/" +
                puzzle.regionCount +
                " couples confirmés • " +
                puzzle.difficulty.label +
                "\nAxes : " +
                BeeGeckoAxisGeometry
                    .legend() +
                " • tap = croix • double tap = pièce"
    }

    private fun handleBeeGeckoSingleTap(
        cell: HexCoord
    ) {
        val engine =
            beeGeckoEngine
                ?: return

        recordBoardAction()
        clearProfessorSession()

        when (
            engine.toggleCross(
                cell
            )
        ) {
            BeeGeckoActionFeedback
                .CROSS_SET -> {
                fx.cross()

                val state =
                    engine
                        .snapshot()
                        .crossStates[cell]

                status.text =
                    when (state) {
                        BeeGeckoCrossState
                            .HYPOTHESIS ->
                            "Croix jaune : hypothèse."

                        BeeGeckoCrossState
                            .CONFIRMED ->
                            "Croix verte : déduction sûre."

                        BeeGeckoCrossState
                            .IMPOSSIBLE ->
                            "Croix rouge : impossible."

                        null ->
                            "Croix retirée."
                    }
            }

            BeeGeckoActionFeedback
                .CROSS_REMOVED -> {
                fx.cross()
                status.text =
                    "Croix retirée."
            }

            BeeGeckoActionFeedback
                .GIVEN_LOCKED -> {
                fx.blocked()
                status.text =
                    "Pièce donnée : elle est verrouillée."
            }

            BeeGeckoActionFeedback
                .CROSS_BLOCKED -> {
                fx.blocked()
                status.text =
                    "Cette case contient déjà une pièce."
            }

            else ->
                Unit
        }

        beeGeckoBoard.invalidate()
        persistBeeGeckoSession()
    }

    private fun showBeeGeckoPiecePalette(
        cell: HexCoord
    ) {
        val labels =
            arrayOf(
                "🟢  Gecko",
                "🟡  Abeille",
                "🔴  Axe"
            )

        AlertDialog.Builder(this)
            .setTitle(
                "Double clic • repère logique"
            )
            .setItems(
                labels
            ) {
                    _,
                    which ->

                when (which) {
                    0 ->
                        beeGeckoEngine
                            ?.let {
                                handleBeeGeckoPalettePiece(
                                    it,
                                    cell,
                                    BeeGeckoPiece
                                        .GECKO
                                )
                            }

                    1 ->
                        beeGeckoEngine
                            ?.let {
                                handleBeeGeckoPalettePiece(
                                    it,
                                    cell,
                                    BeeGeckoPiece
                                        .BEE
                                )
                            }

                    2 ->
                        showBeeGeckoAxisPalette(
                            cell
                        )
                }

                beeGeckoBoard
                    .invalidate()

                refreshBeeGeckoInfo()
                persistBeeGeckoSession()
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun showBeeGeckoAxisPalette(
        cell: HexCoord
    ) {
        val engine =
            beeGeckoEngine
                ?: return

        val axes =
            BeeGeckoAxisGeometry
                .paletteOrder
                .toTypedArray()

        val labels =
            axes
                .map {
                    BeeGeckoAxisGeometry
                        .menuLabel(it)
                }
                .toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(
                "Choisir l’axe"
            )
            .setItems(
                labels
            ) {
                    _,
                    which ->

                val selectedAxis =
                    axes[which]

                showAxisGuideColorPalette {
                    color ->

                    engine.setAxisMarker(
                        cell,
                        selectedAxis,
                        color
                    )

                    fx.marker()

                    status.text =
                        "Barre d’axe " +
                            color.label.lowercase() +
                            " posée. Fais-la glisser pour la déplacer ; sors-la du plateau pour l’effacer."

                    beeGeckoBoard
                        .invalidate()

                    persistBeeGeckoSession()
                }
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun handleBeeGeckoPalettePiece(
        engine: BeeGeckoGameEngine,
        cell: HexCoord,
        piece: BeeGeckoPiece
    ) {
        val feedback =
            engine.placePiece(
                cell,
                piece
            )

        when (feedback) {
            BeeGeckoActionFeedback
                .PIECE_CONFIRMED -> {
                fx.gecko()

                status.text =
                    if (
                        piece ==
                            BeeGeckoPiece.BEE
                    ) {
                        "Abeille confirmée."
                    } else {
                        "Gecko confirmé."
                    }

                showBeeGeckoLivingPiece(
                    cell,
                    piece
                )
            }

            BeeGeckoActionFeedback
                .COMPLETED -> {
                fx.complete()

                showBeeGeckoLivingPiece(
                    cell,
                    piece
                )

                completeBeeGeckoGame()
            }

            BeeGeckoActionFeedback
                .PIECE_REMOVED -> {
                fx.cross()
                status.text =
                    "Pièce retirée."

                hideBeeGeckoLivingPiece(
                    cell,
                    piece
                )
            }

            BeeGeckoActionFeedback
                .WRONG_PIECE -> {
                fx.error()
                statsStore.recordMistake()

                professorLife.observe(
                    ProfessorPlayerEvent
                        .WRONG_MOVE,
                    selectedDifficulty
                )

                status.text =
                    "Pas ici : erreur = −3 étoiles. Croix rouge posée."

                beeGeckoBoard
                    .announceForAccessibility(
                        "Placement incorrect. Trois étoiles de pénalité."
                    )

                speakLivingProfessor(
                    event =
                        ProfessorPlayerEvent
                            .WRONG_MOVE,
                    origin =
                        SpeechOrigin
                            .QUICK_TALK
                )
            }

            BeeGeckoActionFeedback
                .GIVEN_LOCKED -> {
                fx.blocked()
                status.text =
                    "Pièce donnée : elle est verrouillée."
            }

            BeeGeckoActionFeedback
                .CROSS_BLOCKED -> {
                fx.blocked()
                status.text =
                    "Cette case est actuellement exclue."
            }

            else ->
                Unit
        }
    }

    private fun showBeeGeckoMarkerPalette(
        cell: HexCoord
    ) {
        val engine =
            beeGeckoEngine
                ?: return

        val markers =
            CustomMarker.entries

        val labels =
            markers.map {
                it.symbol +
                    "  " +
                    it.label
            }
                .toMutableList()

        labels.add(
            "⌫  Effacer le repère"
        )

        AlertDialog.Builder(this)
            .setTitle(
                "Repère personnel • hexagone"
            )
            .setItems(
                labels.toTypedArray()
            ) {
                    _,
                    which ->

                clearProfessorSession()

                val marker =
                    if (
                        which ==
                            markers.size
                    ) {
                        null
                    } else {
                        markers[which]
                    }

                engine.setMarker(
                    cell,
                    marker
                )

                fx.marker()

                status.text =
                    if (marker == null) {
                        "Repère personnel retiré."
                    } else {
                        "Repère personnel posé : " +
                            marker.label +
                            "."
                    }

                beeGeckoBoard.invalidate()
                persistBeeGeckoSession()
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun focusNextBeeGeckoUnresolved() {
        val engine =
            beeGeckoEngine
                ?: return

        val snapshot =
            engine.snapshot()

        val targetRegion =
            (
                0 until
                    snapshot
                        .puzzle
                        .regionCount
                )
                .firstOrNull {
                    region ->
                    snapshot
                        .puzzle
                        .pairForRegion(
                            region
                        )
                        ?.let {
                            pair ->
                            pair.gecko !in
                                snapshot
                                    .confirmedGeckos ||
                                pair.bee !in
                                    snapshot
                                        .confirmedBees
                        } ==
                        true
                }

        if (targetRegion == null) {
            status.text =
                "Toutes les zones sont résolues."
            return
        }

        val target =
            snapshot
                .puzzle
                .cellsInRegion(
                    targetRegion
                )
                .firstOrNull()
                ?: return

        beeGeckoBoard
            .centerOn(target)

        beeGeckoCamera =
            beeGeckoBoard
                .currentCamera()

        persistBeeGeckoSession()

        status.text =
            "Zone " +
                (targetRegion + 1) +
                " non résolue centrée."
    }

    private fun showBeeGeckoProfessorHint(
        applyStep: Boolean
    ) {
        val engine =
            beeGeckoEngine
                ?: return

        val snapshot =
            engine.snapshot()

        if (snapshot.complete) {
            fx.blocked()
            status.text =
                "La grille est déjà terminée."
            return
        }

        val hint =
            BeeGeckoSolver
                .nextHint(
                    snapshot
                )

        if (hint == null) {
            fx.blocked()

            val message =
                "Je ne trouve plus de déduction sûre avec l'état actuel. Vérifie les croix et les pièces déjà posées."

            showProfessorBubble(
                message
            )

            status.text =
                "Prof Gecko • aucune déduction sûre"
            return
        }

        professorUsed = true
        assistancePoints +=
            if (applyStep) {
                AssistanceKind
                    .DIRECT_MOVE
                    .points
            } else {
                AssistanceKind
                    .ADVICE
                    .points
            }

        beeGeckoBoard
            .showProfessorHint(
                hint
            )

        val visualLegend =
            "\n\nBleu A : zone analysée. Orange ? : hypothèse encore possible. Rouge × : élimination. Vert ✓ : conclusion certaine."

        showProfessorBubble(
            hint.message +
                visualLegend
        )

        if (
            ::professorBubble
                .isInitialized
        ) {
            professorBubble
                .bringToFront()
            professorBubble.elevation =
                dp(24)
                    .toFloat()
        }

        status.text =
            if (applyStep) {
                "Prof Gecko • raisonnement expliqué puis appliqué"
            } else {
                "Prof Gecko • raisonnement affiché"
            }

        fx.hint()

        beeGeckoBoard
            .announceForAccessibility(
                hint.message
            )

        if (!applyStep) {
            persistBeeGeckoSession()
            return
        }

        when (
            engine.applyProfessorStep(
                hint.step
            )
        ) {
            BeeGeckoActionFeedback
                .COMPLETED -> {
                beeGeckoBoard.invalidate()
                persistBeeGeckoSession()
                completeBeeGeckoGame()
            }

            BeeGeckoActionFeedback
                .PIECE_CONFIRMED,
            BeeGeckoActionFeedback
                .CROSS_SET -> {
                beeGeckoBoard.invalidate()

                hint.step.gecko
                    ?.let {
                        showBeeGeckoLivingPiece(
                            it,
                            BeeGeckoPiece
                                .GECKO
                        )
                    }

                hint.step.bee
                    ?.let {
                        showBeeGeckoLivingPiece(
                            it,
                            BeeGeckoPiece
                                .BEE
                        )
                    }

                persistBeeGeckoSession()
                refreshBeeGeckoInfo()
            }

            else -> {
                fx.blocked()
                status.text =
                    "La déduction du Prof n'est plus applicable."
            }
        }
    }

    private fun completeBeeGeckoGame() {
        val puzzle =
            beeGeckoPuzzle
                ?: return

        val stars =
            recordRatedCompletionIfNeeded(
                mode =
                    GameMode.BEES_GECKOS,
                size =
                    puzzle.regionCount,
                difficulty =
                    puzzle.difficulty
            )

        status.text =
            "Grille terminée 🐝🦎  " +
                CompletionRatingPolicy
                    .symbols(stars)

        beeGeckoSessionStore.clear()

        celebrationUsesMusic = true
        startCelebrationMusic()

        if (
            ::beeGeckoBoard
                .isInitialized
        ) {
            beeGeckoBoard
                .startVictoryAnimation()
        }

        if (
            ::celebrationView
                .isInitialized
        ) {
            celebrationView.start(
                puzzle.difficulty
            )
        }

        if (
            ::beeGeckoBoard
                .isInitialized
        ) {
            beeGeckoBoard
                .announceForAccessibility(
                    "Grille Abeilles et Geckos terminée. " +
                        stars +
                        " étoiles."
                )
        }

        speakLivingProfessor(
            event =
                ProfessorPlayerEvent
                    .LEVEL_COMPLETED,
            origin =
                SpeechOrigin
                    .END_GAME
        )

        refreshBeeGeckoUi()
    }

    private fun screenRectToRoot(
        screenRect: RectF
    ): RectF? {
        if (
            !::screenRoot
                .isInitialized
        ) {
            return null
        }

        val rootLocation =
            IntArray(2)

        screenRoot.getLocationOnScreen(
            rootLocation
        )

        return RectF(
            screenRect
        ).apply {
            offset(
                -rootLocation[0]
                    .toFloat(),
                -rootLocation[1]
                    .toFloat()
            )
        }
    }

    private fun centeredScaledRect(
        source: RectF,
        scale: Float
    ): RectF {
        val halfWidth =
            source.width() *
                scale /
                2f
        val halfHeight =
            source.height() *
                scale /
                2f

        return RectF(
            source.centerX() -
                halfWidth,
            source.centerY() -
                halfHeight,
            source.centerX() +
                halfWidth,
            source.centerY() +
                halfHeight
        )
    }

    private fun classicGeckoAliveTarget(
        cell: Cell
    ): RectF? =
        board.cellRectOnScreen(
            cell
        )
            ?.let {
                centeredScaledRect(
                    it,
                    MascotRenderPolicy
                        .CLASSIC_GECKO_SCALE
                )
            }
            ?.let {
                screenRectToRoot(it)
            }

    private fun sudokuGeckoAliveTarget(
        cell: Cell
    ): RectF? =
        sudokuBoard
            .cellRectOnScreen(
                cell
            )
            ?.let {
                centeredScaledRect(
                    it,
                    MascotRenderPolicy
                        .SUDOKU_GECKO_SCALE
                )
            }
            ?.let {
                screenRectToRoot(it)
            }

    private fun beeGeckoAliveTarget(
        cell: HexCoord,
        piece: BeeGeckoPiece
    ): RectF? {
        if (
            !::beeGeckoBoard
                .isInitialized
        ) {
            return null
        }

        val screenRect =
            beeGeckoBoard
                .cellRectOnScreen(
                    cell
                )
                ?: return null

        val target =
            centeredScaledRect(
                screenRect,
                MascotRenderPolicy
                    .beeGeckoScale(
                        piece
                    )
            )

        val viewport =
            beeGeckoBoard
                .viewportRectOnScreen()
                ?: return null

        if (
            target.left <
                viewport.left ||
            target.top <
                viewport.top ||
            target.right >
                viewport.right ||
            target.bottom >
                viewport.bottom
        ) {
            return null
        }

        return screenRectToRoot(
            target
        )
    }

    private fun plantAliveTarget():
        RectF? {
        if (
            !::screenRoot
                .isInitialized ||
            !::boardAnchor
                .isInitialized ||
            screenRoot.width <= 0 ||
            boardAnchor.width <= 0 ||
            boardAnchor.height <= 0
        ) {
            return null
        }

        val rootLocation =
            IntArray(2)
        val anchorLocation =
            IntArray(2)

        screenRoot.getLocationOnScreen(
            rootLocation
        )
        boardAnchor.getLocationOnScreen(
            anchorLocation
        )

        val size =
            dp(
                MascotRenderPolicy
                    .PLANT_SIZE_DP
            ).toFloat()

        val margin =
            dp(
                MascotRenderPolicy
                    .PLANT_MARGIN_DP
            ).toFloat()

        val right =
            anchorLocation[0] -
                rootLocation[0] +
                boardAnchor.width -
                margin

        val bottom =
            anchorLocation[1] -
                rootLocation[1] +
                boardAnchor.height -
                margin

        return RectF(
            right - size,
            bottom - size,
            right,
            bottom
        )
    }

    private fun ensurePlantMascot() {
        if (
            !::aliveMascotOverlay
                .isInitialized
        ) {
            return
        }

        aliveMascotOverlay.show(
            kind =
                MascotKind.PLANT,
            ownerKey =
                "plant:decoration",
            targetProvider = {
                plantAliveTarget()
            },
            eligible = {
                true
            }
        )
    }

    private fun showClassicLivingGecko(
        cell: Cell
    ) {
        if (
            !::aliveMascotOverlay
                .isInitialized
        ) {
            return
        }

        aliveMascotOverlay.show(
            kind =
                MascotKind.GECKO,
            ownerKey =
                "classic:" +
                    cell.row +
                    ":" +
                    cell.col,
            targetProvider = {
                classicGeckoAliveTarget(
                    cell
                )
            },
            maskColorProvider = {
                board.cellBackgroundColor(
                    cell
                )
            },
            eligible = {
                selectedGameMode ==
                    GameMode.GECKODOKU &&
                    engine.snapshot()
                        .confirmed
                        .contains(
                            cell
                        )
            }
        )
    }

    private fun hideClassicLivingGecko(
        cell: Cell
    ) {
        if (
            !::aliveMascotOverlay
                .isInitialized
        ) {
            return
        }

        aliveMascotOverlay.disappear(
            kind =
                MascotKind.GECKO,
            ownerKey =
                "classic:" +
                    cell.row +
                    ":" +
                    cell.col,
            targetProvider = {
                classicGeckoAliveTarget(
                    cell
                )
            },
            maskColorProvider = {
                board.cellBackgroundColor(
                    cell
                )
            }
        )
    }

    private fun showSudokuLivingGecko(
        cell: Cell
    ) {
        if (
            !::aliveMascotOverlay
                .isInitialized
        ) {
            return
        }

        aliveMascotOverlay.show(
            kind =
                MascotKind.GECKO,
            ownerKey =
                "sudoku:" +
                    cell.row +
                    ":" +
                    cell.col,
            targetProvider = {
                sudokuGeckoAliveTarget(
                    cell
                )
            },
            maskColorProvider = {
                sudokuBoard
                    .cellBackgroundColor(
                        cell
                    )
            },
            eligible = {
                selectedGameMode ==
                    GameMode.SUDOKU &&
                    sudokuEngine
                        ?.snapshot()
                        ?.hasGeckoMarker(
                            cell
                        ) ==
                        true
            }
        )
    }

    private fun hideSudokuLivingGecko(
        cell: Cell
    ) {
        if (
            !::aliveMascotOverlay
                .isInitialized
        ) {
            return
        }

        aliveMascotOverlay.disappear(
            kind =
                MascotKind.GECKO,
            ownerKey =
                "sudoku:" +
                    cell.row +
                    ":" +
                    cell.col,
            targetProvider = {
                sudokuGeckoAliveTarget(
                    cell
                )
            },
            maskColorProvider = {
                sudokuBoard
                    .cellBackgroundColor(
                        cell
                    )
            }
        )
    }

    private fun showGomokuLivingGecko(
        cell: Cell,
        player: GomokuPlayer
    ) {
        if (
            !::aliveMascotOverlay
                .isInitialized
        ) {
            return
        }

        aliveMascotOverlay.show(
            kind =
                MascotKind.GECKO,
            ownerKey =
                "gomoku:" +
                    cell.row +
                    ":" +
                    cell.col,
            targetProvider = {
                gomokuOverlayTarget(
                    cell
                )
            },
            maskColorProvider = {
                gomokuBoard
                    .cellBackgroundColor()
            },
            eligible = {
                selectedGameMode ==
                    GameMode.GOMOKU &&
                    gomokuEngine
                        ?.snapshot()
                        ?.playerAt(
                            cell
                        ) ==
                        player
            },
            yellowTint =
                player ==
                    GomokuPlayer.PROFESSOR
        )
    }

    private fun showBeeGeckoLivingPiece(
        cell: HexCoord,
        piece: BeeGeckoPiece
    ) {
        if (
            !::aliveMascotOverlay
                .isInitialized
        ) {
            return
        }

        val kind =
            if (
                piece ==
                    BeeGeckoPiece.BEE
            ) {
                MascotKind.BEE
            } else {
                MascotKind.GECKO
            }

        aliveMascotOverlay.show(
            kind = kind,
            ownerKey =
                "bee:" +
                    kind.name +
                    ":" +
                    cell.q +
                    ":" +
                    cell.r,
            targetProvider = {
                beeGeckoAliveTarget(
                    cell,
                    piece
                )
            },
            maskColorProvider = {
                beeGeckoBoard
                    .cellBackgroundColor(
                        cell
                    )
            },
            eligible = {
                selectedGameMode ==
                    GameMode.BEES_GECKOS &&
                    beeGeckoEngine
                        ?.snapshot()
                        ?.pieceAt(
                            cell
                        ) ==
                        piece
            }
        )
    }

    private fun hideBeeGeckoLivingPiece(
        cell: HexCoord,
        piece: BeeGeckoPiece
    ) {
        if (
            !::aliveMascotOverlay
                .isInitialized
        ) {
            return
        }

        val kind =
            if (
                piece ==
                    BeeGeckoPiece.BEE
            ) {
                MascotKind.BEE
            } else {
                MascotKind.GECKO
            }

        aliveMascotOverlay.disappear(
            kind = kind,
            ownerKey =
                "bee:" +
                    kind.name +
                    ":" +
                    cell.q +
                    ":" +
                    cell.r,
            targetProvider = {
                beeGeckoAliveTarget(
                    cell,
                    piece
                )
            },
            maskColorProvider = {
                beeGeckoBoard
                    .cellBackgroundColor(
                        cell
                    )
            }
        )
    }

    private fun beeGeckoOverlayTarget(
        cell: HexCoord
    ): RectF? {
        if (
            !::beeGeckoBoard
                .isInitialized ||
            !::screenRoot
                .isInitialized
        ) {
            return null
        }

        val screenRect =
            beeGeckoBoard
                .cellRectOnScreen(
                    cell
                )
                ?: return null

        val viewport =
            beeGeckoBoard
                .viewportRectOnScreen()
                ?: return null

        val halfWidth =
            screenRect.width() *
                BeeGeckoVisualPolicy
                    .BEE_SCALE /
                2f

        val halfHeight =
            screenRect.height() *
                BeeGeckoVisualPolicy
                    .BEE_SCALE /
                2f

        val reduced =
            RectF(
                screenRect.centerX() -
                    halfWidth,
                screenRect.centerY() -
                    halfHeight,
                screenRect.centerX() +
                    halfWidth,
                screenRect.centerY() +
                    halfHeight
            )

        if (
            reduced.left <
                viewport.left ||
            reduced.top <
                viewport.top ||
            reduced.right >
                viewport.right ||
            reduced.bottom >
                viewport.bottom
        ) {
            return null
        }

        val rootLocation =
            IntArray(2)

        screenRoot.getLocationOnScreen(
            rootLocation
        )

        return reduced.apply {
            offset(
                -rootLocation[0]
                    .toFloat(),
                -rootLocation[1]
                    .toFloat()
            )
        }
    }

    private fun playBeeGeckoAnimation(
        cell: HexCoord
    ) {
        showBeeGeckoLivingPiece(
            cell,
            BeeGeckoPiece.BEE
        )
    }

    private fun gameModeLabel(
        mode: GameMode
    ): String =
        when (mode) {
            GameMode.GECKODOKU ->
                "GeckoDoku Classic"

            GameMode.SUDOKU ->
                "Sudoku"

            GameMode.GOMOKU ->
                "Gomoku"

            GameMode.BEES_GECKOS ->
                "Abeilles & Geckos"
        }

    private fun gomokuCampLabel(
        player: GomokuPlayer
    ): String =
        if (
            player ==
                GomokuPlayer.PLAYER
        ) {
            "Vert"
        } else {
            "Jaune"
        }

    private fun handleGomokuPlayerMove(
        cell: Cell
    ) {
        val engine =
            gomokuEngine
                ?: return

        val before =
            engine.snapshot()

        if (
            before.gameOver
        ) {
            fx.blocked()
            return
        }

        if (gomokuProfessorThinking) {
            fx.blocked()
            status.text =
                "Prof Gecko termine son analyse."
            return
        }

        if (
            gomokuMatchMode ==
                GomokuMatchMode
                    .VS_PROFESSOR &&
            before.currentPlayer !=
                GomokuPlayer.PLAYER
        ) {
            fx.blocked()
            status.text =
                "Prof Gecko réfléchit déjà à son coup."
            return
        }

        val playedBy =
            before.currentPlayer

        clearProfessorSession()
        recordBoardAction()

        when (
            val result =
                engine.play(cell)
        ) {
            GomokuMoveResult.OCCUPIED -> {
                fx.blocked()

                val phrases =
                    arrayOf(
                        "Petit problème… cette place est déjà prise.",
                        "Tu veux mettre deux Geckos au même endroit ? Ambitieux.",
                        "Cette place est prise, jeune lézard."
                    )

                showProfessorBubble(
                    phrases[
                        (
                            cell.row *
                                31 +
                                cell.col
                            ).mod(
                            phrases.size
                        )
                    ]
                )
            }

            GomokuMoveResult.PLACED -> {
                fx.gecko()

                gomokuBoard.animatePlacement(
                    cell,
                    playedBy
                )

                playGomokuPieceAnimation(
                    kind =
                        RichMediaKind
                            .GECKO_APPEARANCE,
                    cell = cell,
                    player = playedBy,
                    onFinished = {
                        maybePlayGomokuLongAction(
                            cell = cell,
                            player = playedBy
                        )
                    }
                )

                if (
                    gomokuMatchMode ==
                        GomokuMatchMode
                            .VS_PROFESSOR
                ) {
                    status.text =
                        "Prof Gecko réfléchit…"
                    playGomokuProfessorTurn()
                } else {
                    status.text =
                        "Au joueur " +
                            gomokuCampLabel(
                                engine
                                    .snapshot()
                                    .currentPlayer
                            ) +
                            "."
                }
            }

            GomokuMoveResult.WIN -> {
                fx.complete()

                gomokuBoard.animatePlacement(
                    cell,
                    playedBy
                )

                playGomokuPieceAnimation(
                    kind =
                        RichMediaKind
                            .GECKO_APPEARANCE,
                    cell = cell,
                    player = playedBy
                )

                completeGomokuGame(
                    playedBy
                )
            }

            GomokuMoveResult.DRAW -> {
                completeGomokuGame(
                    null
                )
            }

            GomokuMoveResult.OUT_OF_BOUNDS,
            GomokuMoveResult.GAME_OVER -> {
                fx.blocked()
            }
        }

        refreshGomokuUi()
    }

    private fun playGomokuProfessorTurn() {
        if (
            gomokuMatchMode !=
                GomokuMatchMode
                    .VS_PROFESSOR
        ) {
            return
        }

        val engine =
            gomokuEngine
                ?: return

        val snapshot =
            engine.snapshot()

        if (
            snapshot.gameOver ||
            snapshot.currentPlayer !=
                GomokuPlayer.PROFESSOR ||
            gomokuProfessorThinking
        ) {
            return
        }

        gomokuProfessorThinking = true
        professorUsed = true

        val generation =
            gomokuGeneration

        professorButton.isEnabled =
            false
        professorButton.text =
            "🧑‍🏫 Prof Gecko réfléchit…"
        status.text =
            "Prof Gecko réfléchit…"

        val difficulty =
            selectedDifficulty

        Thread {
            val decision =
                GomokuAi.chooseMoveFor(
                    snapshot =
                        snapshot,
                    difficulty =
                        difficulty,
                    player =
                        GomokuPlayer
                            .PROFESSOR
                )

            runOnUiThread {
                if (
                    generation !=
                        gomokuGeneration ||
                    selectedGameMode !=
                        GameMode.GOMOKU ||
                    gomokuMatchMode !=
                        GomokuMatchMode
                            .VS_PROFESSOR ||
                    gomokuEngine !==
                        engine
                ) {
                    return@runOnUiThread
                }

                gomokuProfessorThinking =
                    false

                if (decision == null) {
                    fx.blocked()
                    status.text =
                        "Prof Gecko ne trouve aucun coup disponible."
                    refreshGomokuUi()
                    return@runOnUiThread
                }

                if (
                    gomokuBoard
                        .cellRectOnScreen(
                            decision.cell
                        ) ==
                        null
                ) {
                    gomokuBoard.centerOn(
                        decision.cell
                    )
                }

                when (
                    val result =
                        engine.play(
                            decision.cell
                        )
                ) {
                    GomokuMoveResult.PLACED -> {
                        fx.gecko()

                        gomokuBoard
                            .animatePlacement(
                                decision.cell,
                                GomokuPlayer
                                    .PROFESSOR
                            )

                        playGomokuPieceAnimation(
                            kind =
                                RichMediaKind
                                    .GECKO_APPEARANCE,
                            cell =
                                decision.cell,
                            player =
                                GomokuPlayer
                                    .PROFESSOR,
                            onFinished = {
                                maybePlayGomokuLongAction(
                                    cell =
                                        decision.cell,
                                    player =
                                        GomokuPlayer
                                            .PROFESSOR
                                )
                            }
                        )

                        status.text =
                            GomokuProfessorPersona
                                .moveStatus(
                                    selectedDifficulty
                                )

                    }

                    GomokuMoveResult.WIN -> {
                        fx.complete()

                        gomokuBoard
                            .animatePlacement(
                                decision.cell,
                                GomokuPlayer
                                    .PROFESSOR
                            )

                        playGomokuPieceAnimation(
                            kind =
                                RichMediaKind
                                    .GECKO_APPEARANCE,
                            cell =
                                decision.cell,
                            player =
                                GomokuPlayer
                                    .PROFESSOR
                        )

                        completeGomokuGame(
                            winner =
                                GomokuPlayer
                                    .PROFESSOR
                        )
                    }

                    GomokuMoveResult.DRAW -> {
                        completeGomokuGame(
                            null
                        )
                    }

                    else -> {
                        fx.blocked()
                        status.text =
                            "Le coup de Prof Gecko est devenu indisponible."
                    }
                }

                refreshGomokuUi()
            }
        }.start()
    }

    private fun showGomokuProfessorAdvice(
        applyMoveForHuman: Boolean,
        deepAnalysis: Boolean
    ) {
        val engine =
            gomokuEngine
                ?: return

        val snapshot =
            engine.snapshot()

        if (
            snapshot.gameOver
        ) {
            fx.blocked()
            status.text =
                "La partie est terminée."
            return
        }

        if (gomokuProfessorThinking) {
            return
        }

        if (
            gomokuMatchMode ==
                GomokuMatchMode
                    .VS_PROFESSOR &&
            snapshot.currentPlayer !=
                GomokuPlayer.PLAYER
        ) {
            fx.blocked()
            status.text =
                "Prof Gecko est en train de jouer son propre tour."
            return
        }

        val advisedPlayer =
            snapshot.currentPlayer

        val shouldApply =
            applyMoveForHuman &&
                GomokuMatchPolicy
                    .longPressAppliesHumanMove(
                        mode =
                            gomokuMatchMode,
                        snapshot =
                            snapshot
                    )

        gomokuProfessorThinking = true
        professorUsed = true
        assistancePoints +=
            if (shouldApply) {
                AssistanceKind
                    .DIRECT_MOVE
                    .points
            } else {
                AssistanceKind
                    .ADVICE
                    .points
            }

        val generation =
            gomokuGeneration

        professorButton.isEnabled =
            false
        professorButton.text =
            "🧑‍🏫 Prof Gecko réfléchit…"
        status.text =
            if (deepAnalysis) {
                "Prof Gecko pousse l'analyse plus loin…"
            } else {
                "Prof Gecko analyse la position…"
            }

        val analysisDifficulty =
            if (
                deepAnalysis &&
                gomokuMatchMode ==
                    GomokuMatchMode
                        .HUMAN_VS_HUMAN
            ) {
                GameDifficulty.EXPERT
            } else {
                selectedDifficulty
            }

        Thread {
            val decision =
                GomokuAi.chooseMoveFor(
                    snapshot =
                        snapshot,
                    difficulty =
                        analysisDifficulty,
                    player =
                        advisedPlayer
                )

            runOnUiThread {
                if (
                    generation !=
                        gomokuGeneration ||
                    selectedGameMode !=
                        GameMode.GOMOKU ||
                    gomokuEngine !==
                        engine
                ) {
                    return@runOnUiThread
                }

                gomokuProfessorThinking =
                    false

                if (decision == null) {
                    fx.blocked()
                    status.text =
                        "Prof Gecko ne trouve pas de conseil sûr ici."
                    refreshGomokuUi()
                    return@runOnUiThread
                }

                val camp =
                    gomokuCampLabel(
                        advisedPlayer
                    )

                gomokuBoard
                    .showProfessorReasoning(
                        decision.reasoning
                    )

                val explanation =
                    buildString {
                        append(
                            "Conseil pour le camp "
                        )
                        append(camp)
                        append(
                            " :\n\n"
                        )
                        append(
                            decision.reason
                        )
                        append(
                            "\n\nIntersection conseillée : ligne "
                        )
                        append(
                            decision.cell.row +
                                1
                        )
                        append(
                            ", colonne "
                        )
                        append(
                            decision.cell.col +
                                1
                        )
                        append(".")

                        if (shouldApply) {
                            append(
                                "\n\nJe joue maintenant cette intersection pour toi."
                            )
                        }
                    }

                showProfessorBubble(
                    GomokuProfessorPersona
                        .decorateAdvice(
                            explanation,
                            selectedDifficulty
                        )
                )

                if (!shouldApply) {
                    fx.hint()
                    status.text =
                        "Prof Gecko • conseil pour le camp " +
                            camp
                    refreshGomokuUi()
                    return@runOnUiThread
                }

                when (
                    val result =
                        engine.play(
                            decision.cell
                        )
                ) {
                    GomokuMoveResult.PLACED -> {
                        fx.gecko()

                        gomokuBoard
                            .animatePlacement(
                                decision.cell,
                                advisedPlayer
                            )

                        playGomokuPieceAnimation(
                            kind =
                                RichMediaKind
                                    .GECKO_APPEARANCE,
                            cell =
                                decision.cell,
                            player =
                                advisedPlayer
                        )

                        status.text =
                            "Prof Gecko a joué ton Gecko vert. À lui maintenant."
                        playGomokuProfessorTurn()
                    }

                    GomokuMoveResult.WIN -> {
                        fx.complete()

                        gomokuBoard
                            .animatePlacement(
                                decision.cell,
                                advisedPlayer
                            )

                        playGomokuPieceAnimation(
                            kind =
                                RichMediaKind
                                    .GECKO_APPEARANCE,
                            cell =
                                decision.cell,
                            player =
                                advisedPlayer
                        )

                        completeGomokuGame(
                            advisedPlayer
                        )
                    }

                    GomokuMoveResult.DRAW ->
                        completeGomokuGame(
                            null
                        )

                    else -> {
                        fx.blocked()
                        status.text =
                            "Le conseil n'est plus applicable."
                    }
                }

                refreshGomokuUi()
            }
        }.start()
    }

    private fun completeGomokuGame(
        winner: GomokuPlayer?,
        professorReason: String? =
            null
    ) {
        val snapshot =
            gomokuEngine
                ?.snapshot()
                ?: return

        snapshot.winningLine
            .firstOrNull()
            ?.let {
                gomokuBoard
                    .centerOn(it)
            }

        gomokuBoard.invalidate()
        professorButton.isEnabled =
            false

        val humanVsHuman =
            gomokuMatchMode ==
                GomokuMatchMode
                    .HUMAN_VS_HUMAN

        val stars =
            if (
                !humanVsHuman &&
                winner ==
                    GomokuPlayer.PLAYER
            ) {
                recordRatedCompletionIfNeeded(
                    mode =
                        GameMode.GOMOKU,
                    size =
                        snapshot.size,
                    difficulty =
                        selectedDifficulty
                )
            } else {
                null
            }

        if (
            ::celebrationView
                .isInitialized &&
            (
                (
                    humanVsHuman &&
                    winner != null
                ) ||
                (
                    !humanVsHuman &&
                    winner ==
                        GomokuPlayer.PLAYER
                )
                )
        ) {
            celebrationView.start(
                selectedDifficulty
            )
        }

        val baseMessage =
            if (humanVsHuman) {
                when (winner) {
                    GomokuPlayer.PLAYER ->
                        "Les verts alignent cinq Geckos. Belle ligne !"

                    GomokuPlayer.PROFESSOR ->
                        "Les jaunes alignent cinq Geckos. Belle ligne !"

                    null ->
                        "Plateau rempli : match nul. On en refait une ?"
                }
            } else {
                when (winner) {
                    GomokuPlayer.PLAYER ->
                        "Bien joué ! Tu as aligné cinq Geckos avant moi."

                    GomokuPlayer.PROFESSOR ->
                        (
                            professorReason
                                ?.plus(
                                    "\n\n"
                                )
                                ?: ""
                            ) +
                            "Et de cinq ! Une jolie ligne de Geckos."

                    null ->
                        "Plateau rempli : match nul. On en refait une ?"
                }
            }

        val message =
            if (humanVsHuman) {
                baseMessage
            } else {
                GomokuProfessorPersona
                    .decorateResult(
                        winner = winner,
                        base = baseMessage,
                        difficulty =
                            selectedDifficulty
                    )
            }

        status.text =
            if (humanVsHuman) {
                when (winner) {
                    GomokuPlayer.PLAYER ->
                        "Victoire du joueur Vert 🦎"

                    GomokuPlayer.PROFESSOR ->
                        "Victoire du joueur Jaune 🦎"

                    null ->
                        "Match nul."
                }
            } else {
                when (winner) {
                    GomokuPlayer.PLAYER ->
                        "Victoire ! Cinq Geckos alignés 🦎" +
                            (
                                stars?.let {
                                    "  " +
                                        CompletionRatingPolicy
                                            .symbols(it)
                                }
                                    ?: ""
                                )

                    GomokuPlayer.PROFESSOR ->
                        "Prof Gecko aligne cinq Geckos."

                    null ->
                        "Match nul."
                }
            }

        gomokuBoard
            .announceForAccessibility(
                status.text
            )

        showProfessorBubble(
            message
        )
    }

    private fun startSudokuPuzzle(
        next: SudokuPuzzle
    ) {
        cancelSudokuReasoningPresentation()
        sudokuProfessorInteractionPolicy
            .invalidate()
        dismissSudokuPalette()

        if (
            ::sudokuValueOverlay
                .isInitialized
        ) {
            sudokuValueOverlay
                .clearProfessorCandidates()
        }

        sudokuPuzzle = next
        selectedDifficulty =
            next.difficulty
        gameModePreferences
            .selectedDifficulty =
            selectedDifficulty

        sudokuEngine =
            SudokuGameEngine(next)
        sudokuSelectedCell = null
        sudokuNotesMode = false
        completionRecorded = false
        professorUsed = false
        assistancePoints = 0

        statsStore.recordStart(
            size = 9,
            difficulty =
                next.difficulty,
            mode =
                GameMode.SUDOKU
        )

        if (
            ::sudokuBoard
                .isInitialized
        ) {
            sudokuBoard
                .setSelectedCell(null)
        }

        if (
            ::richMediaOverlay
                .isInitialized
        ) {
            richMediaOverlay.stop()
        }

        if (
            ::aliveMascotOverlay
                .isInitialized
        ) {
            aliveMascotOverlay
                .stopBoardMascots()
        }

        if (
            ::professorSpeech
                .isInitialized
        ) {
            professorSpeech.stop(
                reason =
                    SpeechStopReason
                        .PUZZLE_RESET,
                caller =
                    "MainActivity.startSudokuPuzzle"
            )
        }

        gameStartedAt =
            SystemClock.elapsedRealtime()

        if (
            ::professorLife
                .isInitialized
        ) {
            professorLife.observe(
                ProfessorPlayerEvent
                    .GAME_STARTED,
                next.difficulty
            )
        }

        resetProfessorAmbientState()
        clearProfessorSession()
    }

    private fun refreshSudokuUi() {
        val sudoku =
            sudokuPuzzle
                ?: return

        sudokuBoard.refresh()

        sudokuValueOverlay
            .visualStyle =
            gameModePreferences
                .sudokuVisualStyle

        sudokuValueOverlay
            .animateGeckoMarkers =
            richMediaSettings.enabled

        sudokuValueOverlay.invalidate()

        sudokuStyleSelector
            .setCommittedStyle(
                gameModePreferences
                    .sudokuVisualStyle
            )

        refreshSudokuToolLabels()

        titleView.text =
            AppTitlePolicy
                .titleFor(
                    GameMode.SUDOKU
                )

        info.text =
            "Sudoku 9×9 • " +
                sudoku.difficulty.label +
                " • " +
                sudoku.givenCount() +
                " cases données"

        difficultyButton.text =
            sudoku.difficulty.label

        professorButton.text =
            "🧑‍🏫 Prof Gecko"
        professorButton.isEnabled =
            true
    }

    private fun applyGameModeVisibility() {
        if (
            !::board.isInitialized
        ) {
            return
        }

        val classic =
            selectedGameMode ==
                GameMode.GECKODOKU

        val sudoku =
            selectedGameMode ==
                GameMode.SUDOKU

        val gomoku =
            selectedGameMode ==
                GameMode.GOMOKU

        val bees =
            selectedGameMode ==
                GameMode.BEES_GECKOS

        board.visibility =
            if (classic) {
                View.VISIBLE
            } else {
                View.GONE
            }

        if (
            ::sudokuBoard
                .isInitialized
        ) {
            sudokuBoard.visibility =
                if (sudoku) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        if (
            ::sudokuValueOverlay
                .isInitialized
        ) {
            sudokuValueOverlay.visibility =
                if (sudoku) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        if (
            ::sudokuStyleSelector
                .isInitialized
        ) {
            sudokuStyleSelector.visibility =
                if (sudoku) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        if (
            ::sudokuControlsPanel
                .isInitialized
        ) {
            sudokuControlsPanel.visibility =
                if (sudoku) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        if (
            ::gomokuBoard
                .isInitialized
        ) {
            gomokuBoard.visibility =
                if (gomoku) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        if (
            ::beeGeckoBoard
                .isInitialized
        ) {
            beeGeckoBoard.visibility =
                if (bees) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        if (
            ::sizeButton
                .isInitialized
        ) {
            sizeButton.visibility =
                if (classic) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        if (
            ::difficultyButton
                .isInitialized
        ) {
            difficultyButton.visibility =
                if (
                    sudoku ||
                    (
                        gomoku &&
                        gomokuMatchMode ==
                            GomokuMatchMode
                                .HUMAN_VS_HUMAN
                        )
                ) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
        }

        if (
            ::saveButton
                .isInitialized
        ) {
            saveButton.visibility =
                if (classic) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        if (
            ::journalButton
                .isInitialized
        ) {
            journalButton.visibility =
                if (classic) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        if (
            ::statsButton
                .isInitialized
        ) {
            statsButton.visibility =
                if (classic) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        titleView.text =
            AppTitlePolicy
                .titleFor(
                    selectedGameMode
                )

        if (
            ::screenRoot
                .isInitialized
        ) {
            screenRoot.post {
                positionFloatingBoard()
            }
        }
    }

    private fun positionSudokuLayer(
        view: View,
        geometry: BoardGeometry
    ) {
        val params =
            view.layoutParams
                as? FrameLayout.LayoutParams
                ?: FrameLayout.LayoutParams(
                    geometry.width,
                    geometry.height
                )

        params.width = geometry.width
        params.height = geometry.height
        params.leftMargin =
            geometry.left
        params.topMargin =
            geometry.top
        params.gravity =
            Gravity.TOP or
                Gravity.START

        view.layoutParams =
            params
    }

    private fun handleSudokuDigit(
        digit: Int,
        notesModeOverride:
            Boolean? = null
    ) {
        val cell =
            sudokuSelectedCell

        val engine =
            sudokuEngine

        val sudoku =
            sudokuPuzzle

        if (
            cell == null ||
            engine == null ||
            sudoku == null
        ) {
            fx.blocked()
            status.text =
                "Choisis d'abord une case."
            return
        }

        val now =
            SystemClock.elapsedRealtime()

        val thinkingMs =
            if (
                lastBoardActionAtMs >
                    0L
            ) {
                (
                    now -
                        lastBoardActionAtMs
                    ).coerceAtLeast(0L)
            } else {
                Long.MAX_VALUE
            }

        cancelSudokuReasoningPresentation()
        sudokuProfessorInteractionPolicy
            .invalidate()
        recordBoardAction()
        clearProfessorSession()
        sudokuValueOverlay
            .clearProfessorCandidates()

        val notesMode =
            notesModeOverride
                ?: sudokuNotesMode

        when (
            engine.enterDigit(
                cell,
                digit,
                notesMode =
                    notesMode
            )
        ) {
            SudokuActionFeedback
                .VALUE_SET -> {
                fx.gecko()

                if (
                    thinkingMs >=
                        15_000L
                ) {
                    professorLife.observe(
                        ProfessorPlayerEvent
                            .LONG_THINKING,
                        sudoku.difficulty
                    )
                }

                professorLife.observe(
                    ProfessorPlayerEvent
                        .CORRECT_MOVE,
                    sudoku.difficulty
                )

                status.text =
                    "Bien vu : " +
                        digit +
                        "."
            }

            SudokuActionFeedback
                .NOTE_TOGGLED -> {
                fx.marker()
                status.text =
                    "Note " +
                        digit +
                        " mise à jour."
            }

            SudokuActionFeedback
                .WRONG_VALUE -> {
                fx.error()

                val playerEvent =
                    when {
                        thinkingMs >=
                            15_000L -> {
                            professorLife.observe(
                                ProfessorPlayerEvent
                                    .LONG_THINKING,
                                sudoku.difficulty
                            )

                            ProfessorPlayerEvent
                                .WRONG_MOVE
                        }

                        thinkingMs <=
                            2_500L ->
                            ProfessorPlayerEvent
                                .RAPID_WRONG_MOVE

                        else ->
                            ProfessorPlayerEvent
                                .WRONG_MOVE
                    }

                professorLife.observe(
                    playerEvent,
                    sudoku.difficulty
                )

                status.text =
                    "Ce chiffre ne va pas ici."

                speakLivingProfessor(
                    event =
                        playerEvent,
                    origin =
                        SpeechOrigin
                            .QUICK_TALK
                )
            }

            SudokuActionFeedback
                .GIVEN_LOCKED -> {
                fx.blocked()
                status.text =
                    "Ce chiffre est donné."
            }

            SudokuActionFeedback
                .COMPLETED -> {
                fx.complete()

                professorLife.observe(
                    ProfessorPlayerEvent
                        .LEVEL_COMPLETED,
                    sudoku.difficulty
                )

                completeSudokuGame()
            }

            SudokuActionFeedback
                .NOTHING_CHANGED ->
                Unit

            else -> Unit
        }

        sudokuBoard.refresh()
        sudokuValueOverlay.invalidate()
    }

    private fun toggleSudokuGeckoMarker():
        Boolean {
        val cell =
            sudokuSelectedCell
                ?: run {
                    fx.blocked()
                    return false
                }

        val engine =
            sudokuEngine
                ?: return false

        cancelSudokuReasoningPresentation()
        sudokuProfessorInteractionPolicy
            .invalidate()

        sudokuValueOverlay
            .clearProfessorCandidates()

        return when (
            engine.toggleGeckoMarker(
                cell
            )
        ) {
            SudokuActionFeedback
                .MARKER_TOGGLED -> {
                val active =
                    engine.snapshot()
                        .hasGeckoMarker(
                            cell
                        )

                fx.marker()

                status.text =
                    if (active) {
                        "Petit gecko repère posé 🦎"
                    } else {
                        "Gecko repère retiré."
                    }

                sudokuBoard.refresh()
                sudokuValueOverlay.invalidate()

                if (active) {
                    showSudokuLivingGecko(
                        cell
                    )
                } else {
                    hideSudokuLivingGecko(
                        cell
                    )
                }

                active
            }

            SudokuActionFeedback
                .GIVEN_LOCKED -> {
                fx.blocked()
                status.text =
                    "Ce chiffre est donné."
                false
            }

            else -> {
                fx.blocked()
                false
            }
        }
    }

    private fun eraseSudokuSelection() {
        cancelSudokuReasoningPresentation()
        sudokuProfessorInteractionPolicy
            .invalidate()
        sudokuValueOverlay
            .clearProfessorCandidates()

        val cell =
            sudokuSelectedCell
                ?: run {
                    fx.blocked()
                    status.text =
                        "Choisis d'abord une case."
                    return
                }

        val result =
            sudokuEngine
                ?.erase(cell)
                ?: return

        when (result) {
            SudokuActionFeedback
                .ERASED -> {
                fx.cross()
                status.text =
                    "Case effacée."
            }

            SudokuActionFeedback
                .GIVEN_LOCKED -> {
                fx.blocked()
                status.text =
                    "Ce chiffre est donné."
            }

            else ->
                fx.blocked()
        }

        sudokuBoard.refresh()
        sudokuValueOverlay.invalidate()
    }

    private fun undoSudoku() {
        cancelSudokuReasoningPresentation()
        sudokuProfessorInteractionPolicy
            .invalidate()
        sudokuValueOverlay
            .clearProfessorCandidates()

        val result =
            sudokuEngine
                ?.undo()
                ?: return

        if (
            result ==
                SudokuActionFeedback
                    .UNDONE
        ) {
            fx.marker()
            status.text =
                "Action annulée."
            sudokuBoard.refresh()
            sudokuValueOverlay.invalidate()
        } else {
            fx.blocked()
        }
    }

    private fun redoSudoku() {
        cancelSudokuReasoningPresentation()
        sudokuProfessorInteractionPolicy
            .invalidate()
        sudokuValueOverlay
            .clearProfessorCandidates()

        val result =
            sudokuEngine
                ?.redo()
                ?: return

        if (
            result ==
                SudokuActionFeedback
                    .REDONE
        ) {
            fx.marker()
            status.text =
                "Action rétablie."
            sudokuBoard.refresh()
            sudokuValueOverlay.invalidate()
        } else {
            fx.blocked()
        }
    }

    private fun refreshSudokuToolLabels() {
        if (
            !::sudokuNotesButton
                .isInitialized
        ) {
            return
        }

        sudokuNotesButton.text =
            if (sudokuNotesMode) {
                "✏️ Notes ON"
            } else {
                "✏️ Notes"
            }
    }

    private fun showSudokuProfessorHint() {
        professorUsed = true
        assistancePoints +=
            AssistanceKind.ADVICE.points

        val sudoku =
            sudokuPuzzle
                ?: return

        val engine =
            sudokuEngine
                ?: return

        professorLife.observe(
            ProfessorPlayerEvent
                .HINT_REQUESTED,
            sudoku.difficulty
        )

        val snapshot =
            engine.snapshot()

        when (
            val decision =
                sudokuProfessorInteractionPolicy
                    .onTap(
                        sudoku,
                        snapshot
                    )
        ) {
            is SudokuProfessorDecision
                .Explain ->
                presentSudokuProfessorHint(
                    decision.hint,
                    snapshot
                )

            is SudokuProfessorDecision
                .Apply ->
                applySudokuProfessorMove(
                    decision.hint,
                    announce =
                        false
                )

            SudokuProfessorDecision
                .NoHint ->
                showSudokuNoHint()
        }
    }

    private fun playSudokuProfessorDirect() {
        professorUsed = true
        assistancePoints +=
            AssistanceKind.DIRECT_MOVE.points

        val sudoku =
            sudokuPuzzle
                ?: return

        val engine =
            sudokuEngine
                ?: return

        professorLife.observe(
            ProfessorPlayerEvent
                .HINT_REQUESTED,
            sudoku.difficulty
        )

        when (
            val decision =
                sudokuProfessorInteractionPolicy
                    .onLongPress(
                        sudoku,
                        engine.snapshot()
                    )
        ) {
            is SudokuProfessorDecision
                .Apply ->
                applySudokuProfessorMove(
                    decision.hint,
                    announce =
                        true
                )

            is SudokuProfessorDecision
                .Explain ->
                Unit

            SudokuProfessorDecision
                .NoHint ->
                showSudokuNoHint()
        }
    }

    private fun presentSudokuProfessorHint(
        hint: SudokuHint,
        snapshot: SudokuSnapshot
    ) {
        sudokuSelectedCell =
            hint.cell

        sudokuBoard
            .setSelectedCell(
                hint.cell
            )

        sudokuValueOverlay
            .showProfessorCandidates(
                cell = hint.cell,
                candidates =
                    sudokuProfessorCandidatePolicy
                        .candidatesFor(
                            snapshot.values,
                            hint.cell
                        ),
                focusDigit =
                    hint.digit
            )

        fx.hint()

        val reasoning =
            hint.reasoning

        if (
            reasoning != null &&
            reasoning.steps
                .isNotEmpty()
        ) {
            startSudokuReasoningPresentation(
                reasoning
            )
        } else {
            showProfessorBubble(
                hint.explanation
            )
        }

        status.text =
            hint.technique.label
    }

    private fun startSudokuReasoningPresentation(
        trace: SudokuReasoningTrace
    ) {
        val generation =
            ++sudokuReasoningGeneration

        playSudokuReasoningStep(
            trace = trace,
            stepIndex = 0,
            generation =
                generation
        )
    }

    private fun playSudokuReasoningStep(
        trace: SudokuReasoningTrace,
        stepIndex: Int,
        generation: Int
    ) {
        if (
            generation !=
                sudokuReasoningGeneration
        ) {
            return
        }

        val step =
            trace.steps
                .getOrNull(
                    stepIndex
                )
                ?: return

        sudokuValueOverlay
            .showReasoningStep(
                trace,
                stepIndex
            )

        showProfessorBubbleVisualOnly(
            step.narration
        )

        sudokuBoard
            .announceForAccessibility(
                step.narration
            )

        val accepted =
            speakWithProfessorVisual(
                text =
                    ProfessorDialogTextPolicy
                        .normalize(
                            step.narration
                        ),
                origin =
                    SpeechOrigin
                        .PROF_BUTTON,
                onCompletion = {
                    if (
                        generation ==
                            sudokuReasoningGeneration &&
                        stepIndex <
                            trace.steps
                                .lastIndex
                    ) {
                        screenRoot.postDelayed(
                            {
                                playSudokuReasoningStep(
                                    trace =
                                        trace,
                                    stepIndex =
                                        stepIndex +
                                            1,
                                    generation =
                                        generation
                                )
                            },
                            280L
                        )
                    }
                }
            )

        if (
            !accepted &&
            stepIndex <
                trace.steps
                    .lastIndex
        ) {
            screenRoot.postDelayed(
                {
                    playSudokuReasoningStep(
                        trace = trace,
                        stepIndex =
                            stepIndex + 1,
                        generation =
                            generation
                    )
                },
                1400L
            )
        }
    }

    private fun cancelSudokuReasoningPresentation() {
        sudokuReasoningGeneration += 1

        if (
            ::sudokuValueOverlay
                .isInitialized
        ) {
            sudokuValueOverlay
                .clearReasoning()
        }
    }

    private fun applySudokuProfessorMove(
        hint: SudokuHint,
        announce: Boolean
    ) {
        cancelSudokuReasoningPresentation()

        val engine =
            sudokuEngine
                ?: return

        sudokuSelectedCell =
            hint.cell

        sudokuBoard
            .setSelectedCell(
                hint.cell
            )

        if (announce) {
            showProfessorBubble(
                hint.explanation +
                    "\n\nJe le pose."
            )
        }

        val result =
            engine.enterDigit(
                cell = hint.cell,
                digit = hint.digit,
                notesMode = false,
                origin =
                    SudokuMoveOrigin
                        .PROFESSOR
            )

        sudokuProfessorInteractionPolicy
            .invalidate()

        sudokuValueOverlay
            .clearProfessorCandidates()

        when (result) {
            SudokuActionFeedback
                .VALUE_SET -> {
                fx.hint()
                status.text =
                    hint.digit
                        .toString() +
                        " posé"

                if (!announce) {
                    speakSimpleProfessorBubble(
                        text =
                            "Je le pose.",
                        origin =
                            SpeechOrigin
                                .PROF_BUTTON
                    )
                }
            }

            SudokuActionFeedback
                .COMPLETED -> {
                fx.complete()
                status.text =
                    "Grille terminée"
                completeSudokuGame()
            }

            else -> {
                fx.blocked()
                status.text =
                    "Étape devenue obsolète"
            }
        }

        sudokuBoard.refresh()
        sudokuValueOverlay.invalidate()
    }

    private fun showSudokuNoHint() {
        cancelSudokuReasoningPresentation()
        sudokuProfessorInteractionPolicy
            .invalidate()

        sudokuValueOverlay
            .clearProfessorCandidates()

        fx.blocked()

        showProfessorBubble(
            "Je ne vois pas encore de déduction simple sûre. Vérifie les candidats déjà posés."
        )
    }

    private fun completeSudokuGame() {
        val sudoku =
            sudokuPuzzle
                ?: return

        val stars =
            recordRatedCompletionIfNeeded(
                mode =
                    GameMode.SUDOKU,
                size = 9,
                difficulty =
                    sudoku.difficulty
            )

        status.text =
            "Bravo ! Sudoku terminé 🦎  " +
                CompletionRatingPolicy
                    .symbols(stars)

        if (
            ::celebrationView
                .isInitialized
        ) {
            celebrationView.start(
                sudoku.difficulty
            )
        }

        sudokuBoard
            .announceForAccessibility(
                "Bravo, Sudoku terminé. " +
                    stars +
                    " étoiles."
            )

        speakLivingProfessor(
            event =
                ProfessorPlayerEvent
                    .LEVEL_COMPLETED,
            origin =
                SpeechOrigin
                    .END_GAME
        )
    }

    private fun currentDifficulty():
        GameDifficulty =
        when (selectedGameMode) {
            GameMode.SUDOKU ->
                sudokuPuzzle
                    ?.difficulty
                    ?: selectedDifficulty

            GameMode.GOMOKU ->
                selectedDifficulty

            GameMode.BEES_GECKOS ->
                beeGeckoPuzzle
                    ?.difficulty
                    ?: selectedDifficulty

            GameMode.GECKODOKU ->
                puzzle.difficulty
        }

    private fun isCurrentGameComplete():
        Boolean =
        when (selectedGameMode) {
            GameMode.SUDOKU ->
                sudokuEngine
                    ?.snapshot()
                    ?.complete
                    ?: false

            GameMode.GOMOKU ->
                gomokuEngine
                    ?.snapshot()
                    ?.gameOver
                    ?: false

            GameMode.BEES_GECKOS ->
                beeGeckoEngine
                    ?.snapshot()
                    ?.complete
                    ?: false

            GameMode.GECKODOKU ->
                engine.snapshot()
                    .complete
        }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        if (
            selectedGameMode ==
                GameMode.SUDOKU
        ) {
            val digit =
                when (keyCode) {
                    KeyEvent.KEYCODE_1,
                    KeyEvent.KEYCODE_NUMPAD_1 ->
                        1

                    KeyEvent.KEYCODE_2,
                    KeyEvent.KEYCODE_NUMPAD_2 ->
                        2

                    KeyEvent.KEYCODE_3,
                    KeyEvent.KEYCODE_NUMPAD_3 ->
                        3

                    KeyEvent.KEYCODE_4,
                    KeyEvent.KEYCODE_NUMPAD_4 ->
                        4

                    KeyEvent.KEYCODE_5,
                    KeyEvent.KEYCODE_NUMPAD_5 ->
                        5

                    KeyEvent.KEYCODE_6,
                    KeyEvent.KEYCODE_NUMPAD_6 ->
                        6

                    KeyEvent.KEYCODE_7,
                    KeyEvent.KEYCODE_NUMPAD_7 ->
                        7

                    KeyEvent.KEYCODE_8,
                    KeyEvent.KEYCODE_NUMPAD_8 ->
                        8

                    KeyEvent.KEYCODE_9,
                    KeyEvent.KEYCODE_NUMPAD_9 ->
                        9

                    else -> null
                }

            if (digit != null) {
                handleSudokuDigit(digit)
                return true
            }

            when (keyCode) {
                KeyEvent.KEYCODE_DEL -> {
                    eraseSudokuSelection()
                    return true
                }

                KeyEvent.KEYCODE_N -> {
                    sudokuNotesMode =
                        !sudokuNotesMode
                    refreshSudokuToolLabels()
                    return true
                }

                KeyEvent.KEYCODE_P -> {
                    showSudokuProfessorHint()
                    return true
                }

                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    moveSudokuSelection(
                        keyCode
                    )
                    return true
                }
            }
        }

        return super.onKeyDown(
            keyCode,
            event
        )
    }

    private fun moveSudokuSelection(
        keyCode: Int
    ) {
        val current =
            sudokuSelectedCell
                ?: Cell(0, 0)

        val next =
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT ->
                    Cell(
                        current.row,
                        (
                            current.col -
                                1
                            ).coerceAtLeast(
                            0
                        )
                    )

                KeyEvent.KEYCODE_DPAD_RIGHT ->
                    Cell(
                        current.row,
                        (
                            current.col +
                                1
                            ).coerceAtMost(
                            8
                        )
                    )

                KeyEvent.KEYCODE_DPAD_UP ->
                    Cell(
                        (
                            current.row -
                                1
                            ).coerceAtLeast(
                            0
                        ),
                        current.col
                    )

                else ->
                    Cell(
                        (
                            current.row +
                                1
                            ).coerceAtMost(
                            8
                        ),
                        current.col
                    )
            }

        sudokuSelectedCell = next
        sudokuBoard
            .setSelectedCell(next)
        sudokuValueOverlay.invalidate()
    }

    private fun showSudokuPersonalMarkerPalette(
        cell: Cell
    ) {
        val engine =
            sudokuEngine
                ?: return

        val snapshot =
            engine.snapshot()

        if (
            snapshot.isGiven(cell) ||
            snapshot.valueAt(cell) != 0
        ) {
            fx.blocked()
            status.text =
                if (
                    snapshot.isGiven(
                        cell
                    )
                ) {
                    "Ce chiffre est donné."
                } else {
                    "Cette case contient déjà une valeur."
                }
            return
        }

        sudokuSelectedCell = cell
        sudokuBoard
            .setSelectedCell(
                cell
            )

        val markers =
            CustomMarker.entries

        val labels =
            markers.map {
                it.symbol +
                    "  " +
                    it.label
            }.toMutableList()

        labels.add(
            "⌫  Effacer le repère personnel"
        )

        AlertDialog.Builder(this)
            .setTitle(
                "Repère personnel Sudoku"
            )
            .setItems(
                labels.toTypedArray()
            ) {
                    _,
                    which ->

                cancelSudokuReasoningPresentation()
                sudokuProfessorInteractionPolicy
                    .invalidate()
                sudokuValueOverlay
                    .clearProfessorCandidates()

                val result =
                    if (
                        which ==
                            markers.size
                    ) {
                        engine.setCustomMarker(
                            cell,
                            null
                        )
                    } else {
                        engine.setCustomMarker(
                            cell,
                            markers[which]
                        )
                    }

                when (result) {
                    SudokuActionFeedback
                        .PERSONAL_MARKER_SET -> {
                        fx.marker()
                        status.text =
                            "Repère personnel posé."
                    }

                    SudokuActionFeedback
                        .PERSONAL_MARKER_CLEARED -> {
                        fx.marker()
                        status.text =
                            "Repère personnel retiré."
                    }

                    else ->
                        fx.blocked()
                }

                sudokuBoard.refresh()
                sudokuValueOverlay.invalidate()
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun showSudokuCellPalette(
        cell: Cell
    ) {
        val engine =
            sudokuEngine
                ?: return

        if (
            engine.snapshot()
                .isGiven(cell)
        ) {
            fx.blocked()
            status.text =
                "Ce chiffre est donné."
            return
        }

        if (
            !::screenRoot.isInitialized ||
            screenRoot.width <= 0 ||
            screenRoot.height <= 0
        ) {
            return
        }

        dismissSudokuPalette()

        sudokuSelectedCell = cell
        sudokuBoard
            .setSelectedCell(cell)

        val palette =
            SudokuQuickPaletteView(
                this
            ).apply {
                visualStyle =
                    gameModePreferences
                        .sudokuVisualStyle

                setActiveCandidates(
                    engine.snapshot()
                        .notesAt(cell)
                )

                setGeckoMarkerActive(
                    engine.snapshot()
                        .hasGeckoMarker(
                            cell
                        )
                )

                onValueDigit = {
                        digit ->

                    dismissSudokuPalette()
                    handleSudokuDigit(
                        digit,
                        notesModeOverride =
                            false
                    )
                }

                onCandidateDigit = {
                        digit ->

                    handleSudokuDigit(
                        digit,
                        notesModeOverride =
                            true
                    )

                    setActiveCandidates(
                        engine.snapshot()
                            .notesAt(cell)
                    )
                }

                onGeckoMarker = {
                    setGeckoMarkerActive(
                        toggleSudokuGeckoMarker()
                    )
                }

                onErase = {
                    dismissSudokuPalette()
                    eraseSudokuSelection()
                }
            }

        val popupWidth =
            minOf(
                (
                    screenRoot.width -
                        dp(24)
                    ).coerceAtLeast(
                    dp(220)
                ),
                dp(320)
            )

        val popupHeight =
            minOf(
                (
                    screenRoot.height -
                        dp(24)
                    ).coerceAtLeast(
                    dp(180)
                ),
                dp(230)
            )

        val popup =
            PopupWindow(
                palette,
                popupWidth,
                popupHeight,
                true
            ).apply {
                isOutsideTouchable = true
                setBackgroundDrawable(
                    ColorDrawable(
                        Color.WHITE
                    )
                )
                elevation =
                    dp(10).toFloat()

                setOnDismissListener {
                    if (
                        sudokuPalettePopup ===
                            this
                    ) {
                        sudokuPalettePopup =
                            null
                    }
                }
            }

        val rootLocation =
            IntArray(2)

        val boardLocation =
            IntArray(2)

        screenRoot.getLocationOnScreen(
            rootLocation
        )

        sudokuBoard
            .getLocationOnScreen(
                boardLocation
            )

        val cellRect =
            sudokuBoard
                .cellRectLocal(cell)

        val anchor =
            PixelBox(
                left =
                    boardLocation[0] -
                        rootLocation[0] +
                        cellRect.left
                            .toInt(),
                top =
                    boardLocation[1] -
                        rootLocation[1] +
                        cellRect.top
                            .toInt(),
                right =
                    boardLocation[0] -
                        rootLocation[0] +
                        cellRect.right
                            .toInt(),
                bottom =
                    boardLocation[1] -
                        rootLocation[1] +
                        cellRect.bottom
                            .toInt()
            )

        val position =
            sudokuPopupPlacementPolicy
                .place(
                    screenWidth =
                        screenRoot.width,
                    screenHeight =
                        screenRoot.height,
                    anchor = anchor,
                    popupWidth =
                        popupWidth,
                    popupHeight =
                        popupHeight,
                    margin =
                        dp(8)
                )

        sudokuPalettePopup =
            popup

        popup.showAtLocation(
            screenRoot,
            Gravity.TOP or
                Gravity.START,
            position.x,
            position.y
        )

        status.text =
            "Sudoku • saisie rapide case " +
                (cell.row + 1) +
                "," +
                (cell.col + 1)
    }

    private fun dismissSudokuPalette() {
        val popup =
            sudokuPalettePopup

        sudokuPalettePopup = null

        if (
            popup != null &&
            popup.isShowing
        ) {
            popup.dismiss()
        }
    }

    private fun showSettings() {
        val entries =
            settingsMenuPolicy
                .entriesFor(
                    selectedGameMode
                )
                .filterNot {
                    entry ->
                    selectedGameMode ==
                        GameMode.GOMOKU &&
                        gomokuMatchMode ==
                            GomokuMatchMode
                                .HUMAN_VS_HUMAN &&
                        entry ==
                            SettingsEntry
                                .DIFFICULTY
                }

        val labels =
            entries.map {
                entry ->

                when (entry) {
                    SettingsEntry.GAME_MODE ->
                        when (selectedGameMode) {
                            GameMode.SUDOKU ->
                                "🎮 Mode : Sudoku"

                            GameMode.GOMOKU ->
                                if (
                                    gomokuMatchMode ==
                                        GomokuMatchMode
                                            .VS_PROFESSOR
                                ) {
                                    "🎮 Mode : Gomoku contre Prof Gecko"
                                } else {
                                    "🎮 Mode : Gomoku humain contre humain"
                                }

                            GameMode.BEES_GECKOS ->
                                "🎮 Mode : Abeilles & Geckos"

                            GameMode.GECKODOKU ->
                                "🎮 Mode : GeckoDoku"
                        }

                    SettingsEntry.DIFFICULTY ->
                        "🎯 Difficulté : " +
                            currentDifficulty()
                                .label

                    SettingsEntry.STATS ->
                        "📊 Statistiques"

                    SettingsEntry.RECENTER ->
                        "🎯 Recentrer la carte"

                    SettingsEntry.NEXT_UNRESOLVED ->
                        "🔎 Prochaine zone non résolue"

                    SettingsEntry.SAVE_GRID ->
                        if (
                            selectedGameMode ==
                                GameMode.GECKODOKU &&
                            journalStore.contains(
                                puzzle.id
                            )
                        ) {
                            "★ Grille sauvegardée"
                        } else {
                            "⭐ Sauver la grille"
                        }

                    SettingsEntry.JOURNAL ->
                        "📚 Journal de grilles"

                    SettingsEntry.PLAYER_NAME ->
                        "👤 Joueur : " +
                            playerProfileStore
                                .playerName

                    SettingsEntry.HALL_OF_FAME ->
                        "🏆 Hall of Fame"

                    SettingsEntry.CLEAR_HISTORY ->
                        "🗑 Vider l'historique"

                    SettingsEntry.EXPORT_DATA ->
                        "📤 Exporter mes données"

                    SettingsEntry.IMPORT_DATA ->
                        "📥 Importer mes données"

                    SettingsEntry.SOUND ->
                        if (fx.enabled) {
                            "🔊 Son : ON"
                        } else {
                            "🔇 Son : OFF"
                        }

                    SettingsEntry.ANIMATIONS ->
                        if (richMediaSettings.enabled) {
                            "🎬 Animations : ON"
                        } else {
                            "🎬 Animations : OFF"
                        }

                    SettingsEntry.MEDIA_LOG ->
                        "📋 Journal vidéo"
                }
            }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("⚙️ Réglages")
            .setItems(labels) {
                    dialog,
                    which ->

                when (entries[which]) {
                    SettingsEntry.GAME_MODE -> {
                        dialog.dismiss()
                        showGameModeChooser()
                    }

                    SettingsEntry.DIFFICULTY -> {
                        dialog.dismiss()
                        chooseDifficulty()
                    }

                    SettingsEntry.STATS -> {
                        dialog.dismiss()
                        showStats()
                    }

                    SettingsEntry.RECENTER -> {
                        dialog.dismiss()

                        if (
                            selectedGameMode ==
                                GameMode.BEES_GECKOS &&
                            ::beeGeckoBoard
                                .isInitialized
                        ) {
                            beeGeckoBoard.recenter()
                            beeGeckoCamera =
                                beeGeckoBoard
                                    .currentCamera()
                            persistBeeGeckoSession()
                            status.text =
                                "Carte Abeilles & Geckos recentrée."
                        }
                    }

                    SettingsEntry.NEXT_UNRESOLVED -> {
                        dialog.dismiss()
                        focusNextBeeGeckoUnresolved()
                    }

                    SettingsEntry.SAVE_GRID -> {
                        dialog.dismiss()
                        saveCurrentPuzzle()
                    }

                    SettingsEntry.JOURNAL -> {
                        dialog.dismiss()
                        showJournal()
                    }

                    SettingsEntry.PLAYER_NAME -> {
                        dialog.dismiss()
                        editPlayerName()
                    }

                    SettingsEntry.HALL_OF_FAME -> {
                        dialog.dismiss()
                        showHallOfFame()
                    }

                    SettingsEntry.CLEAR_HISTORY -> {
                        dialog.dismiss()
                        confirmClearResultHistory()
                    }

                    SettingsEntry.EXPORT_DATA -> {
                        dialog.dismiss()
                        exportUserData()
                    }

                    SettingsEntry.IMPORT_DATA -> {
                        dialog.dismiss()
                        importUserData()
                    }

                    SettingsEntry.SOUND -> {
                        toggleSoundSetting()
                        dialog.dismiss()
                        showSettings()
                    }

                    SettingsEntry.ANIMATIONS -> {
                        toggleAnimationSetting()
                        dialog.dismiss()
                        showSettings()
                    }

                    SettingsEntry.MEDIA_LOG -> {
                        dialog.dismiss()
                        showMediaLog()
                    }
                }
            }
            .setNegativeButton(
                "Fermer",
                null
            )
            .show()
    }

    private fun showMediaLog() {
        val text =
            MediaTrace.readPersistent()
                .ifBlank {
                    "Aucun événement vidéo enregistré."
                }

        val logView =
            TextView(this).apply {
                this.text = text
                textSize = 15f
                setTextIsSelectable(true)
                setPadding(
                    dp(16),
                    dp(12),
                    dp(16),
                    dp(12)
                )
            }

        val scroll =
            ScrollView(this).apply {
                addView(logView)
            }

        AlertDialog.Builder(this)
            .setTitle(
                "Journal vidéo • " +
                    MediaTrace
                        .persistentFileName()
            )
            .setView(scroll)
            .setPositiveButton(
                "Fermer",
                null
            )
            .setNeutralButton(
                "Copier"
            ) {
                    _,
                    _ ->

                val clipboard =
                    getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as ClipboardManager

                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        "GeckoDoku journal vidéo",
                        text
                    )
                )

                status.text =
                    "Journal vidéo copié."
            }
            .setNegativeButton(
                "Vider"
            ) {
                    _,
                    _ ->

                confirmClearMediaLog()
            }
            .show()
    }

    private fun confirmClearMediaLog() {
        AlertDialog.Builder(this)
            .setTitle(
                "Vider le journal vidéo ?"
            )
            .setMessage(
                "Les traces vidéo enregistrées sur ce téléphone seront effacées."
            )
            .setPositiveButton(
                "Vider"
            ) {
                    _,
                    _ ->

                MediaTrace.clearPersistent()
                status.text =
                    "Journal vidéo vidé."
            }
            .setNegativeButton(
                "Annuler",
                null
            )
            .show()
    }

    private fun playIntroIfEnabled() {
        if (
            !richMediaSettings.enabled ||
            !::richMediaOverlay.isInitialized ||
            richMediaOverlay
                .hasActiveKind(
                    RichMediaKind.INTRO
                )
        ) {
            if (!richMediaSettings.enabled) {
                introPhase =
                    IntroPhase.DONE
                applyProfessorIntroVisibility()
            }
            return
        }

        introPhase =
            IntroPhase.FIRST
        applyProfessorIntroVisibility()
        playIntroStep(0)
    }

    private fun playIntroStep(
        index: Int
    ) {
        val phase =
            when (index) {
                0 ->
                    IntroPhase.FIRST

                1 ->
                    IntroPhase.SECOND

                else ->
                    IntroPhase.DONE
            }

        MediaTrace.event(
            source = "MainActivity",
            event = "INTRO_STEP_REQUEST",
            assetPath =
                IntroSequencePolicy
                    .assets
                    .getOrNull(index),
            detail =
                "index=" +
                    index +
                    " phase=" +
                    phase +
                    " introActive=" +
                    (
                        ::richMediaOverlay
                            .isInitialized &&
                            richMediaOverlay
                                .hasActiveKind(
                                    RichMediaKind
                                        .INTRO
                                )
                        )
        )

        if (phase == IntroPhase.DONE) {
            introPhase =
                IntroPhase.DONE
            applyProfessorIntroVisibility()
            return
        }

        if (
            !richMediaSettings.enabled ||
            index !in
                IntroSequencePolicy
                    .assets.indices ||
            richMediaOverlay
                .hasActiveKind(
                    RichMediaKind.INTRO
                )
        ) {
            return
        }

        introPhase =
            phase
        applyProfessorIntroVisibility()

        val accepted =
            richMediaOverlay.play(
                kind =
                    RichMediaKind.INTRO,
                assetPath =
                    IntroSequencePolicy
                        .assets[index],
                muted =
                    introLifecyclePolicy
                        .mustMuteIntro(
                            phase = phase,
                            fxEnabled =
                                fx.enabled
                        ),
                target = null,
                titleText = "GeckoDoku",
                skippable = true,
                onFinished = {
                    MediaTrace.event(
                        source =
                            "MainActivity",
                        event =
                            "INTRO_STEP_FINISHED",
                        assetPath =
                            IntroSequencePolicy
                                .assets[index],
                        detail =
                            "index=" +
                                index +
                                " phase=" +
                                phase
                    )

                    introPhase =
                        introLifecyclePolicy
                            .onNaturalCompletion(
                                phase
                            )

                    applyProfessorIntroVisibility()

                    if (
                        introPhase !=
                        IntroPhase.DONE
                    ) {
                        playIntroStep(
                            index + 1
                        )
                    }
                },
                onSkipped = {
                    MediaTrace.event(
                        source =
                            "MainActivity",
                        event =
                            "INTRO_SEQUENCE_SKIPPED",
                        assetPath =
                            IntroSequencePolicy
                                .assets[index],
                        detail =
                            "index=" +
                                index +
                                " phase=" +
                                phase
                    )

                    introPhase =
                        introLifecyclePolicy
                            .onSkip(
                                phase
                            )

                    applyProfessorIntroVisibility()
                }
            )

        MediaTrace.event(
            source = "MainActivity",
            event =
                if (accepted) {
                    "INTRO_STEP_ACCEPTED"
                } else {
                    "INTRO_STEP_REJECTED"
                },
            assetPath =
                IntroSequencePolicy
                    .assets[index],
            detail =
                "index=" +
                    index +
                    " phase=" +
                    phase
        )
    }

    private fun playCellAnimation(
        kind: RichMediaKind,
        cell: Cell,
        onFinished: (() -> Unit)? = null
    ) {
        when (kind) {
            RichMediaKind
                .GECKO_APPEARANCE -> {
                showClassicLivingGecko(
                    cell
                )
                return
            }

            RichMediaKind
                .GECKO_DISAPPEARANCE -> {
                hideClassicLivingGecko(
                    cell
                )
                return
            }

            else -> Unit
        }

        playSharedGeckoCellAnimation(
            kind = kind,
            screenRect =
                board.cellRectOnScreen(
                    cell
                ),
            maskColor =
                board.cellBackgroundColor(
                    cell
                ),
            onFinished =
                onFinished
        )
    }

    private fun gomokuOverlayTarget(
        cell: Cell
    ): RectF? {
        if (
            !::gomokuBoard.isInitialized ||
            !::screenRoot.isInitialized
        ) {
            return null
        }

        val screenRect =
            gomokuBoard
                .geckoRectOnScreen(
                    cell
                )
                ?: return null

        val rootLocation =
            IntArray(2)

        screenRoot.getLocationOnScreen(
            rootLocation
        )

        return RectF(screenRect).apply {
            offset(
                -rootLocation[0].toFloat(),
                -rootLocation[1].toFloat()
            )
        }
    }

    private fun playGomokuPieceAnimation(
        kind: RichMediaKind,
        cell: Cell,
        player: GomokuPlayer,
        onFinished: (() -> Unit)? = null
    ) {
        if (
            kind ==
                RichMediaKind
                    .GECKO_APPEARANCE
        ) {
            showGomokuLivingGecko(
                cell,
                player
            )
            return
        }

        val celebrationVisible =
            ::celebrationView.isInitialized &&
                celebrationView.visibility ==
                    View.VISIBLE

        if (
            !richMediaSettings.enabled ||
            !::richMediaOverlay.isInitialized ||
            celebrationVisible
        ) {
            onFinished?.invoke()
            return
        }

        val asset =
            GeckoCellAnimationAssetPolicy
                .assetFor(kind)
                ?: run {
                    onFinished?.invoke()
                    return
                }

        val initialTarget =
            gomokuOverlayTarget(
                cell
            )
                ?: run {
                    onFinished?.invoke()
                    return
                }

        var suppressed =
            false

        fun restoreStaticStone() {
            if (
                suppressed &&
                ::gomokuBoard.isInitialized
            ) {
                gomokuBoard
                    .setMediaStoneSuppressed(
                        cell,
                        false
                    )
            }
            suppressed = false
        }

        val accepted =
            richMediaOverlay.play(
                kind = kind,
                assetPath = asset,
                muted =
                    GeckoMediaAudioPolicy
                        .mustMute(kind),
                target =
                    initialTarget,
                targetProvider = {
                    gomokuOverlayTarget(
                        cell
                    )
                },
                titleText = null,
                skippable =
                    kind ==
                        RichMediaKind
                            .GECKO_LONG_ACTION,
                maskTarget = null,
                yellowTint =
                    player ==
                        GomokuPlayer
                            .PROFESSOR,
                onFirstFrameVisible = {
                    if (
                        ::gomokuBoard.isInitialized
                    ) {
                        suppressed = true
                        gomokuBoard
                            .setMediaStoneSuppressed(
                                cell,
                                true
                            )
                    }
                },
                onFinished = {
                    restoreStaticStone()
                    onFinished
                        ?.invoke()
                }
            )

        if (!accepted) {
            restoreStaticStone()
            onFinished?.invoke()
        }
    }

    private fun maybePlayGomokuLongAction(
        cell: Cell,
        player: GomokuPlayer
    ) {
        if (
            !richMediaSettings.enabled ||
            !::richMediaOverlay.isInitialized ||
            (
                ::celebrationView.isInitialized &&
                    celebrationView.visibility ==
                        View.VISIBLE
                )
        ) {
            return
        }

        val eligible =
            gomokuEngine
                ?.snapshot()
                ?.gameOver ==
                false

        val shouldPlay =
            richMediaScheduler
                .shouldPlayLongAction(
                    nowMs =
                        SystemClock
                            .elapsedRealtime(),
                    randomValue =
                        Random.nextInt(100),
                    eligible =
                        eligible,
                    busy =
                        richMediaOverlay
                            .isBusy
                )

        if (!shouldPlay) {
            return
        }

        playGomokuPieceAnimation(
            kind =
                RichMediaKind
                    .GECKO_LONG_ACTION,
            cell = cell,
            player = player
        )
    }

    private fun stopGomokuPieceMedia() {
        if (
            ::aliveMascotOverlay
                .isInitialized
        ) {
            aliveMascotOverlay.stop(
                MascotKind.GECKO
            )
        }

        if (
            ::richMediaOverlay
                .isInitialized
        ) {
            richMediaOverlay.stopKind(
                RichMediaKind
                    .GECKO_APPEARANCE
            )
            richMediaOverlay.stopKind(
                RichMediaKind
                    .GECKO_LONG_ACTION
            )
        }

        if (
            ::gomokuBoard
                .isInitialized
        ) {
            gomokuBoard
                .clearMediaStoneSuppression()
        }
    }

    private fun playSharedGeckoCellAnimation(
        kind: RichMediaKind,
        screenRect: RectF,
        maskColor: Int,
        onFinished: (() -> Unit)? = null
    ) {
        val celebrationVisible =
            ::celebrationView.isInitialized &&
                celebrationView.visibility ==
                    View.VISIBLE

        if (
            !richMediaSettings.enabled ||
            !::richMediaOverlay.isInitialized ||
            celebrationVisible
        ) {
            onFinished?.invoke()
            return
        }

        val asset =
            GeckoCellAnimationAssetPolicy
                .assetFor(kind)
                ?: run {
                    onFinished?.invoke()
                    return
                }

        val rootLocation =
            IntArray(2)

        screenRoot.getLocationOnScreen(
            rootLocation
        )

        val target =
            RectF(screenRect).apply {
                offset(
                    -rootLocation[0].toFloat(),
                    -rootLocation[1].toFloat()
                )
            }

        val maskTarget =
            RectF(target).apply {
                val insetAmount =
                    width() *
                        cellAnimationStyle
                            .maskInsetFraction
                inset(
                    insetAmount,
                    insetAmount
                )
            }

        val accepted =
            richMediaOverlay.play(
                kind = kind,
                assetPath = asset,
                muted =
                    GeckoMediaAudioPolicy
                        .mustMute(kind),
                target = target,
                titleText = null,
                skippable =
                    kind ==
                        RichMediaKind
                            .GECKO_LONG_ACTION,
                maskTarget =
                    maskTarget,
                maskColor =
                    maskColor,
                onFinished =
                    onFinished
            )

        if (!accepted) {
            onFinished?.invoke()
        }
    }

    private fun maybePlayGeckoLongAction(
        cell: Cell
    ) {
        maybePlaySharedGeckoLongAction(
            screenRect =
                board.cellRectOnScreen(
                    cell
                ),
            maskColor =
                board.cellBackgroundColor(
                    cell
                ),
            eligible =
                !engine.snapshot()
                    .complete
        )
    }

    private fun maybePlaySharedGeckoLongAction(
        screenRect: RectF,
        maskColor: Int,
        eligible: Boolean
    ) {
        if (
            !richMediaSettings.enabled ||
            !::richMediaOverlay.isInitialized ||
            (
                ::celebrationView.isInitialized &&
                    celebrationView.visibility ==
                        View.VISIBLE
                ) ||
            !eligible
        ) {
            return
        }

        val shouldPlay =
            richMediaScheduler
                .shouldPlayLongAction(
                    nowMs =
                        SystemClock
                            .elapsedRealtime(),
                    randomValue =
                        Random.nextInt(100),
                    eligible = true,
                    busy =
                        richMediaOverlay
                            .isBusy
                )

        if (!shouldPlay) {
            return
        }

        playSharedGeckoCellAnimation(
            kind =
                RichMediaKind
                    .GECKO_LONG_ACTION,
            screenRect =
                screenRect,
            maskColor =
                maskColor
        )
    }

    private fun animateProfessorButtonPortrait(
        action: ProfessorAnimationAction
    ) {
        if (
            !::professorPortrait.isInitialized ||
            !professorUiPolicy
                .animatePortraitOnInteraction ||
            professorPortrait.visibility !=
                View.VISIBLE
        ) {
            return
        }

        professorPortrait.animate().cancel()
        professorPortrait.bringToFront()
        professorPortrait.elevation =
            dp(
                professorUiPolicy
                    .portraitElevationDp
            ).toFloat()

        professorPortrait.scaleX = 1f
        professorPortrait.scaleY = 1f
        professorPortrait.translationY = 0f
        professorPortrait.rotation = 0f

        when (action) {
            ProfessorAnimationAction.BOUNCE -> {
                professorPortrait.animate()
                    .scaleX(1.08f)
                    .scaleY(1.08f)
                    .translationY(
                        -dp(5).toFloat()
                    )
                    .setDuration(150L)
                    .withEndAction {
                        resetProfessorPortrait(
                            190L
                        )
                    }
                    .start()
            }

            ProfessorAnimationAction.TILT -> {
                professorPortrait.animate()
                    .rotation(8f)
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(170L)
                    .withEndAction {
                        professorPortrait
                            .animate()
                            .rotation(-6f)
                            .setDuration(150L)
                            .withEndAction {
                                resetProfessorPortrait(
                                    170L
                                )
                            }
                            .start()
                    }
                    .start()
            }

            ProfessorAnimationAction.NOD -> {
                professorPortrait.animate()
                    .translationY(
                        dp(3).toFloat()
                    )
                    .scaleY(0.96f)
                    .setDuration(120L)
                    .withEndAction {
                        professorPortrait
                            .animate()
                            .translationY(
                                -dp(4).toFloat()
                            )
                            .scaleY(1.04f)
                            .setDuration(130L)
                            .withEndAction {
                                resetProfessorPortrait(
                                    170L
                                )
                            }
                            .start()
                    }
                    .start()
            }
        }
    }

    private fun resetProfessorPortrait(
        durationMs: Long
    ) {
        professorPortrait.animate()
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .rotation(0f)
            .setDuration(durationMs)
            .start()
    }

    private fun runProfessorIdleAnimation() {
        val eligible =
            richMediaSettings.enabled &&
                ::professorPortrait.isInitialized &&
                professorButtonHost.isShown &&
                !(
                    ::celebrationView.isInitialized &&
                        celebrationView.visibility ==
                            View.VISIBLE
                    )

        if (!eligible) {
            scheduleProfessorIdleAnimation()
            return
        }

        val videoHandled =
            playProfessorButtonVideo()

        if (!videoHandled) {
            animateProfessorButtonPortrait(
                professorIdleAnimationPolicy
                    .actionFor(
                        Random.nextInt()
                    )
            )
            scheduleProfessorIdleAnimation()
        }
    }

    private fun speakQuickProfessorLine() {
        professorLife.observe(
            ProfessorPlayerEvent.AMBIENT,
            currentDifficulty()
        )

        speakLivingProfessor(
            event =
                ProfessorPlayerEvent.AMBIENT,
            origin =
                SpeechOrigin.QUICK_TALK
        )
    }

    private fun speakLivingProfessor(
        event: ProfessorPlayerEvent,
        origin: SpeechOrigin,
        onCompletion:
            (() -> Unit)? = null
    ): Boolean {
        if (
            !professorSpeech.canAccept(
                origin
            )
        ) {
            return false
        }

        val selection =
            professorLife.choose(
                event = event,
                difficulty =
                    currentDifficulty()
            )
                ?: return false

        val spokenText =
            if (
                selectedGameMode ==
                    GameMode.GOMOKU
            ) {
                GomokuProfessorPersona
                    .livingLine(
                        event = event,
                        difficulty =
                            currentDifficulty()
                    )
            } else {
                selection.phrase.text
            }

        return speakSimpleProfessorBubble(
            text =
                spokenText,
            origin = origin,
            onCompletion =
                onCompletion
        )
    }

    private fun speakSimpleProfessorBubble(
        text: String,
        origin: SpeechOrigin,
        onCompletion:
            (() -> Unit)? = null
    ): Boolean {
        val normalizedText =
            ProfessorDialogTextPolicy
                .normalize(text)

        val plan =
            professorSimpleSpeechCoordinator
                .begin(
                    text = normalizedText,
                    origin = origin,
                    canAccept =
                        professorSpeech
                            .canAccept(
                                origin
                            )
                )
                ?: return false

        cancelProfessorQuickBubbleClose()

        showProfessorBubbleVisualOnly(
            plan.bubbleText
        )

        status.text =
            plan.statusText

        return speakWithProfessorVisual(
            text =
                plan.speechText,
            origin =
                plan.origin,
            onCompletion = {
                onCompletion?.invoke()

                scheduleProfessorSimpleBubbleClose(
                    plan.token
                )
            },
            onRejected = {
                if (
                    professorSimpleSpeechCoordinator
                        .shouldCloseAfterRejection(
                            plan.token
                        )
                ) {
                    MediaTrace.event(
                        source =
                            "MainActivity",
                        event =
                            "PROF_SIMPLE_BUBBLE_REJECTED",
                        detail =
                            "origin=" +
                                plan.origin +
                                " token=" +
                                plan.token
                    )

                    closeProfessorBubble()
                }
            }
        )
    }

    private fun scheduleProfessorSimpleBubbleClose(
        token: Long
    ) {
        val schedule =
            professorSimpleSpeechCoordinator
                .onSpeechCompleted(
                    token
                )
                ?: return

        if (
            !::screenRoot.isInitialized
        ) {
            return
        }

        val runnable =
            Runnable {
                if (
                    professorSimpleSpeechCoordinator
                        .canClose(
                            schedule.token
                        )
                ) {
                    MediaTrace.event(
                        source =
                            "MainActivity",
                        event =
                            "PROF_QUICK_BUBBLE_CLOSE",
                        detail =
                            "token=" +
                                schedule.token
                    )

                    closeProfessorBubble()
                }
            }

        professorQuickBubbleCloseRunnable =
            runnable

        MediaTrace.event(
            source =
                "MainActivity",
            event =
                "PROF_QUICK_BUBBLE_CLOSE_SCHEDULED",
            detail =
                "delayMs=" +
                    schedule.delayMs +
                    " token=" +
                    schedule.token
        )

        screenRoot.postDelayed(
            runnable,
            schedule.delayMs
        )
    }

    private fun cancelProfessorQuickBubbleClose() {
        val runnable =
            professorQuickBubbleCloseRunnable

        if (
            runnable != null &&
            ::screenRoot.isInitialized
        ) {
            screenRoot.removeCallbacks(
                runnable
            )
        }

        professorQuickBubbleCloseRunnable =
            null
    }

    private fun speakWithProfessorVisual(
        text: String,
        origin: SpeechOrigin,
        onCompletion:
            (() -> Unit)? = null,
        onRejected:
            (() -> Unit)? = null
    ): Boolean {
        if (
            !professorSpeech.canAccept(
                origin
            )
        ) {
            onRejected?.invoke()
            return false
        }

        if (
            !professorSpeechVisualPolicy
                .shouldAnimate(origin)
        ) {
            val accepted =
                professorSpeech.speak(
                    text = text,
                    origin = origin,
                    onCompletion =
                        onCompletion
                )

            if (!accepted) {
                onRejected?.invoke()
            }

            return accepted
        }

        prepareProfessorSpeakingVisual {
                visualReady ->

            suppressVisualForCurrentSpeech =
                !visualReady

            val accepted =
                professorSpeech.speak(
                    text = text,
                    origin = origin,
                    onCompletion =
                        onCompletion
                )

            if (!accepted) {
                suppressVisualForCurrentSpeech =
                    false
                stopProfessorSpeechVideo()
                onRejected?.invoke()
            }
        }

        return true
    }

    private fun prepareProfessorSpeakingVisual(
        onReady: (Boolean) -> Unit
    ) {
        val eligible =
            richMediaSettings.enabled &&
                professorUiPolicy
                    .playVideoInButton &&
                ::professorVideo.isInitialized &&
                introLifecyclePolicy
                    .professorEligible(
                        introPhase
                    ) &&
                !(
                    ::celebrationView
                        .isInitialized &&
                        celebrationView
                            .visibility ==
                            View.VISIBLE
                    )

        if (!eligible) {
            onReady(false)
            return
        }

        cancelProfessorIdleAnimation()
        cancelProfessorVisualTimeout()

        val generation =
            ++professorVisualGeneration

        var readyDelivered = false

        fun deliver(ready: Boolean) {
            if (
                readyDelivered ||
                generation !=
                    professorVisualGeneration
            ) {
                return
            }

            readyDelivered = true
            cancelProfessorVisualTimeout()
            onReady(ready)
        }

        professorPortraitContinuityPolicy
            .onPrepareStarted()
        applyProfessorPortraitContinuity()
        professorVideo.alpha = 0f
        professorVideo.stopPlayback()

        professorVideoMode =
            ProfessorVideoMode.SPEECH
        professorVideo.alpha = 0f
        professorVideo.visibility =
            View.VISIBLE
        professorVideo.bringToFront()

        professorVisualPreparing = true
        professorVisualPrepared = false

        professorVideo.play(
            assetPath =
                AssetMediaCatalog
                    .PROF_SPEECH,
            muted = true,
            holdOnFirstFrame = true,
            onStarted = {
                professorSpeechVideoFailed =
                    false
            },
            onFirstFrameRendered = {
                if (
                    generation !=
                    professorVisualGeneration
                ) {
                    return@play
                }

                professorVisualPreparing =
                    false
                professorVisualPrepared =
                    true
                professorPortraitContinuityPolicy
                    .onFirstFrameHeld()
                applyProfessorPortraitContinuity()

                MediaTrace.event(
                    source = "MainActivity",
                    event =
                        "PROF_SPEECH_VIDEO_PREROLL_READY",
                    assetPath =
                        AssetMediaCatalog
                            .PROF_SPEECH
                )

                deliver(true)
            },
            onCompletion = {
                if (
                    generation ==
                    professorVisualGeneration &&
                    professorSpeechActive
                ) {
                    startProfessorSpeechVideo()
                }
            },
            onError = {
                    message ->

                professorSpeechVideoFailed =
                    true

                MediaTrace.event(
                    source = "MainActivity",
                    event =
                        "PROF_SPEECH_VIDEO_FAILED",
                    assetPath =
                        AssetMediaCatalog
                            .PROF_SPEECH,
                    detail =
                        "phase=preroll voiceContinues=true message=" +
                            message
                )

                professorVisualPreparing =
                    false
                professorVisualPrepared =
                    false
                professorPortraitContinuityPolicy
                    .onVideoError()
                restoreProfessorPngOnly()
                deliver(false)
            }
        )

        val timeout =
            Runnable {
                if (
                    generation !=
                        professorVisualGeneration ||
                    readyDelivered
                ) {
                    return@Runnable
                }

                MediaTrace.event(
                    source = "MainActivity",
                    event =
                        "PROF_SPEECH_VIDEO_PREROLL_TIMEOUT",
                    assetPath =
                        AssetMediaCatalog
                            .PROF_SPEECH,
                    detail =
                        "timeoutMs=" +
                            professorSpeechLaunchPolicy
                                .visualPrepareTimeoutMs +
                            " voiceContinues=true"
                )

                professorVisualPreparing =
                    false
                professorVisualPrepared =
                    false
                professorPortraitContinuityPolicy
                    .onTimeout()
                professorVideo.stopPlayback()
                restoreProfessorPngOnly()
                deliver(false)
            }

        professorVisualTimeout = timeout

        professorVideo.postDelayed(
            timeout,
            professorSpeechLaunchPolicy
                .visualPrepareTimeoutMs
        )
    }

    private fun revealPreparedProfessorSpeakingVisual() {
        if (!professorVisualPrepared) {
            return
        }

        professorVisualPrepared = false
        professorVisualPreparing = false

        val revealed =
            professorVideo
                .revealHeldFirstFrame()

        if (revealed) {
            professorPortraitContinuityPolicy
                .onRevealSucceeded()
            applyProfessorPortraitContinuity()

            MediaTrace.event(
                source = "MainActivity",
                event =
                    "PROF_SPEECH_SYNC_REVEAL",
                assetPath =
                    AssetMediaCatalog
                        .PROF_SPEECH
            )
        } else {
            professorPortraitContinuityPolicy
                .onRevealFailed()
            restoreProfessorPngOnly()
        }
    }

    private fun applyProfessorPortraitContinuity() {
        if (!::professorPortrait.isInitialized) {
            return
        }

        professorPortrait
            .animate()
            .cancel()

        professorPortrait.visibility =
            if (
                professorPortraitContinuityPolicy
                    .portraitVisible
            ) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

        if (
            professorPortraitContinuityPolicy
                .portraitVisible
        ) {
            professorPortrait.bringToFront()
        }
    }

    private fun restoreProfessorPngOnly() {
        professorPortraitContinuityPolicy
            .onRevealFailed()

        professorVideoMode =
            ProfessorVideoMode.NONE

        if (::professorVideo.isInitialized) {
            professorVideo.alpha = 0f
            professorVideo.visibility =
                View.INVISIBLE
        }

        if (::professorPortrait.isInitialized) {
            professorPortrait.visibility =
                View.VISIBLE
            professorPortrait.bringToFront()
        }
    }

    private fun cancelProfessorVisualTimeout() {
        val timeout =
            professorVisualTimeout

        if (
            timeout != null &&
            ::professorVideo.isInitialized
        ) {
            professorVideo.removeCallbacks(
                timeout
            )
        }

        professorVisualTimeout = null
    }

    private fun playProfessorButtonVideo(): Boolean {
        if (
            !introLifecyclePolicy
                .professorEligible(
                    introPhase
                )
        ) {
            return false
        }

        MediaTrace.event(
            source = "MainActivity",
            event = "PROF_ACTION_REQUEST",
            assetPath =
                AssetMediaCatalog
                    .PROF_LONG_ACTIONS,
            detail =
                "mode=" +
                    professorVideoMode +
                    " speechActive=" +
                    professorSpeechActive
        )

        if (
            !richMediaSettings.enabled ||
            !professorUiPolicy.playVideoInButton ||
            !::professorVideo.isInitialized ||
            professorActionVideoFailed
        ) {
            return false
        }

        if (
            professorVideoMode !=
            ProfessorVideoMode.NONE
        ) {
            return true
        }

        if (
            ::celebrationView.isInitialized &&
            celebrationView.visibility ==
                View.VISIBLE
        ) {
            return false
        }

        cancelProfessorIdleAnimation()

        professorVideoMode =
            ProfessorVideoMode.ACTION
        professorVideo.alpha = 0f
        professorVideo.visibility =
            View.VISIBLE
        professorVideo.bringToFront()
        professorVideo.elevation =
            dp(
                professorUiPolicy
                    .portraitElevationDp + 4
            ).toFloat()

        professorVideo.play(
            assetPath =
                AssetMediaCatalog
                    .PROF_LONG_ACTIONS,
            muted =
                professorUiPolicy
                    .muteProfVideoEmbeddedAudio,
            onFirstFrameRendered = {
                if (
                    professorVideoMode ==
                    ProfessorVideoMode.ACTION
                ) {
                    professorPortrait
                        .animate()
                        .cancel()
                    professorPortrait.visibility =
                        View.INVISIBLE
                }
            },
            onCompletion = {
                finishProfessorActionVideo(
                    failed = false
                )
            },
            onError = {
                finishProfessorActionVideo(
                    failed = true
                )
            }
        )

        return true
    }

    private fun finishProfessorActionVideo(
        failed: Boolean
    ) {
        if (
            professorVideoMode !=
            ProfessorVideoMode.ACTION
        ) {
            return
        }

        professorVideoMode =
            ProfessorVideoMode.NONE

        if (failed) {
            professorActionVideoFailed =
                true
        }

        professorVideo.stopPlayback()
        professorVideo.visibility =
            View.INVISIBLE
        professorPortrait.visibility =
            View.VISIBLE
        professorPortrait.bringToFront()

        if (failed) {
            animateProfessorButtonPortrait(
                professorIdleAnimationPolicy
                    .actionFor(
                        Random.nextInt()
                    )
            )
        }

        scheduleProfessorIdleAnimation()
    }

    private fun handleProfessorSpeakingChanged(
        speaking: Boolean
    ) {
        MediaTrace.event(
            source = "MainActivity",
            event = "PROF_SPEECH_STATE",
            assetPath =
                AssetMediaCatalog
                    .PROF_SPEECH,
            detail =
                "speaking=" +
                    speaking +
                    " previousMode=" +
                    professorVideoMode
        )

        professorSpeechActive =
            speaking

        if (speaking) {
            val now =
                SystemClock
                    .elapsedRealtime()

            nextSmallTalkAtMs =
                now +
                    professorAmbientPolicy
                        .smallTalkDelayMs(
                            Random.nextInt()
                        )
            nextAmbientAllowedAtMs =
                now +
                    professorAmbientPolicy
                        .minimumAmbientGapMs

            if (professorVisualPrepared) {
                revealPreparedProfessorSpeakingVisual()
            } else if (
                !suppressVisualForCurrentSpeech
            ) {
                when (
                    professorSpeechVideoPolicy
                        .onSpeechStarted(
                            professorVideoMode
                        )
                ) {
                    ProfessorSpeechVideoCommand
                        .START_SPEECH_FROM_ZERO,
                    ProfessorSpeechVideoCommand
                        .RESTART_SPEECH_FROM_ZERO ->
                        startProfessorSpeechVideo()

                    ProfessorSpeechVideoCommand
                        .KEEP_PLAYING ->
                        Unit

                    ProfessorSpeechVideoCommand
                        .STOP_SPEECH ->
                        stopProfessorSpeechVideo()
                }
            }
        } else {
            if (
                professorVisualPreparing ||
                professorVisualPrepared
            ) {
                return
            }

            suppressVisualForCurrentSpeech =
                false

            if (
                professorSpeechVideoPolicy
                    .onSpeechEnded(
                        professorVideoMode
                    ) ==
                ProfessorSpeechVideoCommand
                    .STOP_SPEECH
            ) {
                stopProfessorSpeechVideo()
            }
        }
    }

    private fun startProfessorSpeechVideo() {
        MediaTrace.event(
            source = "MainActivity",
            event = "PROF_SPEECH_VIDEO_REQUEST",
            assetPath =
                AssetMediaCatalog
                    .PROF_SPEECH,
            detail =
                "mode=" +
                    professorVideoMode +
                    " speechActive=" +
                    professorSpeechActive +
                    " previousFailed=" +
                    professorSpeechVideoFailed
        )

        if (
            !professorSpeechVideoStartPolicy
                .canStart(
                    animationsEnabled =
                        richMediaSettings.enabled,
                    playVideoInButton =
                        professorUiPolicy
                            .playVideoInButton,
                    viewReady =
                        ::professorVideo.isInitialized,
                    speechActive =
                        professorSpeechActive,
                    previousAttemptFailed =
                        professorSpeechVideoFailed
                )
        ) {
            if (
                professorVideoMode ==
                ProfessorVideoMode.ACTION
            ) {
                stopProfessorButtonVideo()
            }
            return
        }

        cancelProfessorIdleAnimation()

        professorPortraitContinuityPolicy
            .onPrepareStarted()
        applyProfessorPortraitContinuity()
        professorVideo.alpha = 0f
        professorVideo.stopPlayback()

        professorVideoMode =
            ProfessorVideoMode.SPEECH
        professorVideo.alpha = 0f
        professorVideo.visibility =
            View.VISIBLE
        professorVideo.bringToFront()
        professorVideo.elevation =
            dp(
                professorUiPolicy
                    .portraitElevationDp + 4
            ).toFloat()

        professorVideo.play(
            assetPath =
                AssetMediaCatalog
                    .PROF_SPEECH,
            muted = true,
            onStarted = {
                professorSpeechVideoFailed =
                    false

                MediaTrace.event(
                    source = "MainActivity",
                    event =
                        "PROF_SPEECH_VIDEO_STARTED",
                    assetPath =
                        AssetMediaCatalog
                            .PROF_SPEECH,
                    detail =
                        "retryRecovered=true"
                )

            },
            onFirstFrameRendered = {
                if (
                    professorVideoMode ==
                    ProfessorVideoMode.SPEECH &&
                    professorSpeechActive
                ) {
                    professorPortraitContinuityPolicy
                        .onRevealSucceeded()
                    applyProfessorPortraitContinuity()
                }
            },
            onCompletion = {
                when (
                    professorSpeechVideoPolicy
                        .onSpeechClipCompleted(
                            professorSpeechActive
                        )
                ) {
                    ProfessorSpeechVideoCommand
                        .RESTART_SPEECH_FROM_ZERO ->
                        startProfessorSpeechVideo()

                    ProfessorSpeechVideoCommand
                        .STOP_SPEECH ->
                        stopProfessorSpeechVideo()

                    else -> Unit
                }
            },
            onError = {
                professorSpeechVideoFailed =
                    true

                MediaTrace.event(
                    source = "MainActivity",
                    event =
                        "PROF_SPEECH_VIDEO_FAILED",
                    assetPath =
                        AssetMediaCatalog
                            .PROF_SPEECH,
                    detail =
                        "willRetryNextSpeech=true"
                )

                stopProfessorSpeechVideo()
            }
        )
    }

    private fun stopProfessorSpeechVideo() {
        if (
            professorVideoMode !=
            ProfessorVideoMode.SPEECH
        ) {
            return
        }

        professorVideoMode =
            ProfessorVideoMode.NONE
        professorVisualGeneration += 1
        professorVisualPreparing = false
        professorVisualPrepared = false
        cancelProfessorVisualTimeout()
        professorVideo.stopPlayback()
        professorVideo.alpha = 0f
        professorVideo.visibility =
            View.INVISIBLE
        professorPortrait.visibility =
            View.VISIBLE
        professorPortrait.bringToFront()

        if (!professorSpeechActive) {
            scheduleProfessorIdleAnimation()
        }
    }

    private fun stopProfessorButtonVideo() {
        MediaTrace.event(
            source = "MainActivity",
            event = "PROF_VIDEO_STOP_ALL",
            detail =
                "previousMode=" +
                    professorVideoMode +
                    " speechActive=" +
                    professorSpeechActive
        )

        professorVideoMode =
            ProfessorVideoMode.NONE

        if (::professorVideo.isInitialized) {
            professorVideo.stopPlayback()
            professorVideo.visibility =
                View.INVISIBLE
        }

        if (::professorPortrait.isInitialized) {
            professorPortrait.visibility =
                View.VISIBLE
            professorPortrait.bringToFront()
        }
    }

    private fun scheduleProfessorIdleAnimation() {
        if (!::professorPortrait.isInitialized) {
            return
        }

        cancelProfessorIdleAnimation()

        if (
            !richMediaSettings.enabled ||
            !introLifecyclePolicy
                .professorEligible(
                    introPhase
                )
        ) {
            return
        }

        val delay =
            professorIdleAnimationPolicy
                .delayMs(
                    Random.nextInt()
                )

        professorPortrait.postDelayed(
            professorIdleAnimationRunnable,
            delay
        )
    }

    private fun cancelProfessorIdleAnimation() {
        if (::professorPortrait.isInitialized) {
            professorPortrait.removeCallbacks(
                professorIdleAnimationRunnable
            )
        }
    }

    private fun handlePlayerGeckoConfirmed(
        cell: Cell,
        completed: Boolean
    ) {
        val remaining =
            puzzle.size -
                engine.snapshot()
                    .confirmed.size

        status.text =
            if (completed) {
                "Dernier gecko trouvé !"
            } else {
                encouragement(remaining)
            }

        board.announceForAccessibility(
            "Gecko confirmé. " +
                remaining +
                " restant."
        )

        val newlyRewarded =
            rewardedGeckos.add(cell)

        val voiceStarted =
            if (newlyRewarded) {
                playEncouragement(
                    remaining = remaining,
                    onFinished = {
                        if (completed) {
                            startCelebrationMusic()
                        }
                    }
                )
            } else {
                false
            }

        if (!voiceStarted) {
            fx.gecko()
        }

        if (completed) {
            completeGame(
                playCelebrationMusicImmediately =
                    !voiceStarted
            )
            return
        }

        playCellAnimation(
            kind =
                RichMediaKind
                    .GECKO_APPEARANCE,
            cell = cell,
            onFinished = {
                maybePlayGeckoLongAction(
                    cell
                )
            }
        )
    }

    private fun playEncouragement(
        remaining: Int,
        onFinished: (() -> Unit)? = null
    ): Boolean {
        if (!fx.enabled) {
            return false
        }

        val event =
            when {
                remaining <= 0 ->
                    ProfessorPlayerEvent
                        .LEVEL_COMPLETED

                professorLife
                    .context
                    .successStreak >= 3 ->
                    ProfessorPlayerEvent
                        .STREAK_CONTINUED

                else ->
                    ProfessorPlayerEvent
                        .CORRECT_MOVE
            }

        return speakLivingProfessor(
            event = event,
            origin =
                SpeechOrigin.ENCOURAGEMENT,
            onCompletion =
                onFinished
        )
    }

    private fun playLevelStartMusic() {
        if (!fx.enabled) {
            return
        }

        val started =
            gameAudio.playMusic(
                assetPath =
                    AssetAudioCatalog
                        .LEVEL_START,
                onCompletion = {
                    announcePlayerStats()
                }
            )

        if (!started) {
            announcePlayerStats()
        }
    }

    private fun announcePlayerStats() {
        if (
            !fx.enabled ||
            !::professorSpeech.isInitialized
        ) {
            return
        }

        val narration =
            PlayerStatsNarration.build(
                statsStore.read()
            )

        speakSimpleProfessorBubble(
            text = narration,
            origin =
                SpeechOrigin.STATS
        )
    }

    private fun startCelebrationMusic() {
        if (fx.enabled) {
            gameAudio.playMusic(
                AssetAudioCatalog.CELEBRATION
            )
        }
    }

    override fun onResume() {
        super.onResume()

        val now =
            SystemClock.elapsedRealtime()

        val awayMs =
            if (professorPausedAtMs > 0L) {
                now - professorPausedAtMs
            } else {
                0L
            }

        professorPausedAtMs = 0L

        if (
            awayMs >= 60_000L &&
            ::professorLife.isInitialized &&
            ::screenRoot.isInitialized
        ) {
            professorLife.observe(
                ProfessorPlayerEvent
                    .RETURN_AFTER_PAUSE,
                puzzle.difficulty
            )

            screenRoot.postDelayed(
                {
                    speakLivingProfessor(
                        event =
                            ProfessorPlayerEvent
                                .RETURN_AFTER_PAUSE,
                        origin =
                            SpeechOrigin.QUICK_TALK
                    )
                },
                350L
            )
        }

        scheduleProfessorIdleAnimation()
        scheduleProfessorAmbientTick()
        startTitleIdentityAnimation()

        if (
            ::screenRoot.isInitialized
        ) {
            screenRoot.post {
                positionTitleIdentity()
                ensurePlantMascot()
            }
        }
    }

    override fun onPause() {
        if (
            selectedGameMode ==
                GameMode.BEES_GECKOS
        ) {
            persistBeeGeckoSession()
        }

        dismissSudokuPalette()

        professorPausedAtMs =
            SystemClock.elapsedRealtime()

        cancelProfessorQuickBubbleClose()
        professorQuickBubbleClosePolicy
            .invalidate()

        cancelProfessorIdleAnimation()
        cancelProfessorAmbientTick()
        stopTitleIdentityAnimation()
        stopProfessorButtonVideo()

        if (
            ::aliveMascotOverlay
                .isInitialized
        ) {
            aliveMascotOverlay
                .stopAll()
        }

        if (::richMediaOverlay.isInitialized) {
            richMediaOverlay.stop()
        }

        introPhase =
            IntroPhase.DONE
        applyProfessorIntroVisibility()

        if (::gameAudio.isInitialized) {
            gameAudio.stopAll()
        }

        if (::professorSpeech.isInitialized) {
            professorSpeech.stop(
                reason =
                    SpeechStopReason
                        .LIFECYCLE_PAUSE,
                caller =
                    "MainActivity.onPause"
            )
        }

        super.onPause()
    }

    override fun onDestroy() {
        cancelClassicPuzzleSearch(
            announce = false
        )
        cancelProfessorQuickBubbleClose()
        professorQuickBubbleClosePolicy
            .invalidate()

        cancelProfessorIdleAnimation()
        cancelProfessorAmbientTick()
        stopTitleIdentityAnimation()
        stopProfessorButtonVideo()

        if (::professorVideo.isInitialized) {
            professorVideo.release()
        }

        if (::richMediaOverlay.isInitialized) {
            richMediaOverlay.release()
        }

        if (
            ::aliveMascotOverlay
                .isInitialized
        ) {
            aliveMascotOverlay
                .release()
        }

        if (::gameAudio.isInitialized) {
            gameAudio.release()
        }

        if (::professorSpeech.isInitialized) {
            professorSpeech.release()
        }

        fx.release()
        super.onDestroy()
    }

    private fun dp(
        value: Int
    ): Int =
        (
            value *
                resources
                    .displayMetrics
                    .density
            ).toInt()
}

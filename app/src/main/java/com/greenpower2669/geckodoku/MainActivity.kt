package com.greenpower2669.geckodoku

import android.app.Activity
import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
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

    private lateinit var board:
        GeckoBoardView

    private lateinit var status:
        TextView

    private lateinit var info:
        TextView

    private lateinit var soundButton:
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

    private var professorVideoPlaying =
        false

    private var professorVideoFailed =
        false

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

    private lateinit var animationButton:
        Button

    private lateinit var richMediaSettings:
        RichMediaSettings

    private lateinit var richMediaOverlay:
        RichMediaOverlayView

    private lateinit var gameAudio:
        AssetAudioPlayer

    private lateinit var professorSpeech:
        ProfessorSpeech

    private val encouragementSelector =
        EncouragementSelector()

    private val rewardedGeckos =
        linkedSetOf<Cell>()

    private var celebrationUsesMusic =
        false

    private val celebrationAudioPolicy =
        CelebrationAudioPolicy()

    private val richMediaScheduler =
        RichMediaScheduler()

    private val professorUiPolicy =
        ProfessorUiPolicy()

    private val professorIdleAnimationPolicy =
        ProfessorIdleAnimationPolicy()

    private val cellAnimationStyle =
        CellAnimationStyle()

    private val professorIdleAnimationRunnable =
        Runnable {
            runProfessorIdleAnimation()
        }

    private var selectedSize = 5

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

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        richMediaSettings =
            RichMediaSettings(this)

        gameAudio =
            AssetAudioPlayer(this)

        professorSpeech =
            ProfessorSpeech(this)

        statsStore =
            PlayerStatsStore(this)

        journalStore =
            PuzzleJournalStore(this)

        createPuzzle(
            recordStart = true
        )

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

        val title =
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
                    handleDoubleTap(it)
                }

                onLongPressCell = {
                    handleLongPress(it)
                }

                onLongPressOutside = {
                    showMarkerPalette()
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

        val newButton =
            Button(this).apply {
                text = "↻ Nouvelle"
                textSize = 15f
                minHeight = dp(46)

                setOnClickListener {
                    createPuzzle(
                        recordStart = true
                    )
                    refreshGameUi()
                    playLevelStartMusic()
                }
            }

        val statsButton =
            Button(this).apply {
                text = "Stats"
                textSize = 15f
                minHeight = dp(46)

                setOnClickListener {
                    showStats()
                }
            }

        soundButton =
            Button(this).apply {
                text = "🔊 FX"
                textSize = 15f
                minHeight = dp(46)

                setOnClickListener {
                    fx.enabled =
                        !fx.enabled

                    text =
                        if (fx.enabled) {
                            "🔊 FX"
                        } else {
                            "🔇 FX"
                        }

                    gameAudio.enabled = fx.enabled
                    professorSpeech.enabled = fx.enabled

                    if (::richMediaOverlay.isInitialized) {
                        richMediaOverlay.setMuted(
                            !fx.enabled
                        )
                    }
                }
            }

        animationButton =
            Button(this).apply {
                textSize = 13f
                minHeight = dp(46)

                setOnClickListener {
                    richMediaSettings.enabled =
                        !richMediaSettings.enabled

                    updateAnimationButton()

                    if (!richMediaSettings.enabled) {
                        if (::richMediaOverlay.isInitialized) {
                            richMediaOverlay.stop()
                        }
                        stopProfessorButtonVideo()
                    }

                    if (richMediaSettings.enabled) {
                        scheduleProfessorIdleAnimation()
                    } else {
                        cancelProfessorIdleAnimation()
                    }

                    status.text =
                        if (richMediaSettings.enabled) {
                            "Habillage animé activé 🎬"
                        } else {
                            "Habillage animé désactivé."
                        }
                }
            }

        val replayButton =
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

        val journalButton =
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
            title,
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
            board,
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
                        1f
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
                    soundButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                addView(
                    animationButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )
            }

        val row3 =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL

                addView(
                    replayButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                addView(
                    saveButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                addView(
                    journalButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )
            }

        controlsPanel =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL

                addView(row1)
                addView(row2)
                addView(row3)
            }

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
            }

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

        protectFromSystemBars(root)
        refreshGameUi()
        updateAnimationButton()
        scheduleProfessorIdleAnimation()

        if (savedInstanceState == null) {
            screenRoot.post {
                playIntroIfEnabled()
            }
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
        }

        puzzle = nextPuzzle

        selectedSize =
            puzzle.size

        engine =
            GameEngine(puzzle)

        pendingMarker = null
        eraseMarkerMode = false
        completionRecorded = false
        professorUsed = false
        rewardedGeckos.clear()
        encouragementSelector.reset()

        if (::gameAudio.isInitialized) {
            gameAudio.stopAll()
        }

        if (::professorSpeech.isInitialized) {
            professorSpeech.stop()
        }

        gameStartedAt =
            SystemClock.elapsedRealtime()

        clearProfessorSession()

        if (recordStart) {
            statsStore.recordStart(
                puzzle.size,
                puzzle.difficulty
            )
        }
    }

    private fun replayCurrentPuzzle() {
        startPuzzle(
            puzzle,
            recordStart = true
        )

        refreshGameUi()

        status.text =
            "Même grille réinitialisée. À toi de rejouer 🦎"
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
                    "Le journal est vide. Utilise ⭐ Sauver pour conserver une grille."
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
                        " gecko(s) donné(s). Simple = ✕, double = 🦎."
                } else {
                    "Simple = ✕, vrai double-clic = 🦎, appui long = hypothèse."
                }
        }
    }

    private fun handleSingleTap(
        cell: Cell
    ) {
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

    private fun handleDoubleTap(
        cell: Cell
    ) {
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

                status.text =
                    "Pas ici. Une croix reste en place."

                board
                    .announceForAccessibility(
                        "Gecko incorrect."
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

            ActionFeedback.COMPLETED ->
                handlePlayerGeckoConfirmed(
                    cell = cell,
                    completed = true
                )

            else -> Unit
        }

        board.invalidate()
    }

    private fun handleLongPress(
        cell: Cell
    ) {
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
        professorUsed = true

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

        professorSpeech.speak(
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
        if (
            ::professorBubble.isInitialized
        ) {
            professorBubble.hideMessage()
        }

        if (::professorSpeech.isInitialized) {
            professorSpeech.stop()
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

    private fun completeGame(
        playCelebrationMusicImmediately: Boolean = true
    ) {
        fx.stopCelebration()

        if (::richMediaOverlay.isInitialized) {
            richMediaOverlay.stop()
        }

        clearProfessorSession()

        if (!completionRecorded) {
            val seconds =
                (
                    SystemClock
                        .elapsedRealtime() -
                        gameStartedAt
                    ) / 1000L

            statsStore.recordComplete(
                puzzle.size,
                puzzle.difficulty,
                seconds,
                usedProfessor =
                    professorUsed
            )

            completionRecorded = true
        }

        status.text =
            "Bravo ! Grille terminée 🦎"

        celebrationUsesMusic = true

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

                dialog.dismiss()

                createPuzzle(
                    recordStart = true
                )

                refreshGameUi()
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
                "Difficulté logique réelle"
            )
            .setSingleChoiceItems(
                labels,
                values.indexOf(
                    selectedDifficulty
                )
            ) { dialog, which ->
                selectedDifficulty =
                    values[which]

                dialog.dismiss()

                createPuzzle(
                    recordStart = true
                )

                refreshGameUi()
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
                    "Réussite par difficulté :\n"
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
                    }

                    append("\n")
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
                    "\nTout reste sur ce téléphone."
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
            .show()
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
            }

            insets
        }

        root.requestApplyInsets()
    }

    private fun updateAnimationButton() {
        if (!::animationButton.isInitialized) {
            return
        }

        animationButton.text =
            if (richMediaSettings.enabled) {
                "🎬 Anim. ON"
            } else {
                "🎬 Anim. OFF"
            }
    }

    private fun playIntroIfEnabled() {
        if (!richMediaSettings.enabled ||
            !::richMediaOverlay.isInitialized ||
            richMediaOverlay.isBusy
        ) {
            return
        }

        richMediaOverlay.play(
            kind = RichMediaKind.INTRO,
            assetPath = AssetMediaCatalog.GECKO_INTRO,
            muted = !fx.enabled,
            target = null,
            titleText = "GeckoDoku",
            skippable = true
        )
    }

    private fun playCellAnimation(
        kind: RichMediaKind,
        cell: Cell,
        onFinished: (() -> Unit)? = null
    ) {
        if (!richMediaSettings.enabled ||
            !::richMediaOverlay.isInitialized ||
            richMediaOverlay.isBusy ||
            (::celebrationView.isInitialized &&
                celebrationView.visibility == View.VISIBLE)
        ) {
            return
        }

        val screenRect =
            board.cellRectOnScreen(cell)

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

        val asset =
            when (kind) {
                RichMediaKind.GECKO_APPEARANCE ->
                    AssetMediaCatalog.GECKO_APPEARANCE

                RichMediaKind.GECKO_DISAPPEARANCE ->
                    AssetMediaCatalog.GECKO_DISAPPEARANCE

                else -> return
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

        richMediaOverlay.play(
            kind = kind,
            assetPath = asset,
            muted = !fx.enabled,
            target = target,
            titleText = null,
            skippable = false,
            maskTarget = maskTarget,
            maskColor =
                board.cellBackgroundColor(
                    cell
                ),
            onFinished = onFinished
        )
    }

    private fun maybePlayGeckoLongAction(
        cell: Cell
    ) {
        if (!richMediaSettings.enabled ||
            !::richMediaOverlay.isInitialized ||
            richMediaOverlay.isBusy ||
            pendingProfessorHypothesis != null ||
            (::professorBubble.isInitialized &&
                professorBubble.visibility == View.VISIBLE) ||
            (::celebrationView.isInitialized &&
                celebrationView.visibility == View.VISIBLE) ||
            engine.snapshot().complete
        ) {
            return
        }

        val shouldPlay =
            richMediaScheduler
                .shouldPlayLongAction(
                    nowMs =
                        SystemClock.elapsedRealtime(),
                    randomValue =
                        Random.nextInt(100),
                    eligible = true,
                    busy = false
                )

        if (!shouldPlay) {
            return
        }

        val screenRect =
            board.cellRectOnScreen(cell)
        val rootLocation = IntArray(2)
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

        richMediaOverlay.play(
            kind = RichMediaKind.GECKO_LONG_ACTION,
            assetPath = AssetMediaCatalog.GECKO_LONG_ACTIONS,
            muted = !fx.enabled,
            target = target,
            titleText = null,
            skippable = true,
            maskTarget = maskTarget,
            maskColor =
                board.cellBackgroundColor(
                    cell
                )
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

    private fun playProfessorButtonVideo(): Boolean {
        if (
            !richMediaSettings.enabled ||
            !professorUiPolicy.playVideoInButton ||
            !::professorVideo.isInitialized ||
            professorVideoFailed
        ) {
            return false
        }

        if (professorVideoPlaying) {
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

        professorVideoPlaying = true
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
            onStarted = {
                professorPortrait
                    .animate()
                    .cancel()
                professorPortrait.visibility =
                    View.INVISIBLE
            },
            onCompletion = {
                finishProfessorButtonVideo(
                    failed = false
                )
            },
            onError = {
                finishProfessorButtonVideo(
                    failed = true
                )
            }
        )

        return true
    }

    private fun finishProfessorButtonVideo(
        failed: Boolean
    ) {
        professorVideoPlaying = false

        if (failed) {
            professorVideoFailed = true
        }

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

        if (
            failed &&
            ::professorPortrait.isInitialized
        ) {
            animateProfessorButtonPortrait(
                professorIdleAnimationPolicy
                    .actionFor(
                        Random.nextInt()
                    )
            )
        }

        scheduleProfessorIdleAnimation()
    }

    private fun stopProfessorButtonVideo() {
        professorVideoPlaying = false

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

        if (!richMediaSettings.enabled) {
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

        val index =
            encouragementSelector.choose(
                remaining = remaining,
                randomValue =
                    Random.nextInt(
                        Int.MAX_VALUE
                    )
            )

        val segment =
            AssetAudioCatalog
                .ENCOURAGEMENTS[index]

        return gameAudio
            .playVoiceSegment(
                assetPath =
                    AssetAudioCatalog
                        .ENCOURAGEMENT_MASTER,
                startMs = segment.startMs,
                endMs = segment.endMs,
                onCompletion = onFinished
            )
    }

    private fun playLevelStartMusic() {
        if (fx.enabled) {
            gameAudio.playMusic(
                AssetAudioCatalog.LEVEL_START
            )
        }
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
        scheduleProfessorIdleAnimation()
    }

    override fun onPause() {
        cancelProfessorIdleAnimation()
        stopProfessorButtonVideo()

        if (::richMediaOverlay.isInitialized) {
            richMediaOverlay.stop()
        }

        if (::gameAudio.isInitialized) {
            gameAudio.stopAll()
        }

        if (::professorSpeech.isInitialized) {
            professorSpeech.stop()
        }

        super.onPause()
    }

    override fun onDestroy() {
        cancelProfessorIdleAnimation()
        stopProfessorButtonVideo()

        if (::professorVideo.isInitialized) {
            professorVideo.release()
        }

        if (::richMediaOverlay.isInitialized) {
            richMediaOverlay.release()
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

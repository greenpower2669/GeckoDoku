package com.greenpower2669.geckodoku

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private lateinit var professorBubble:
        ProfessorBubbleView

    private lateinit var celebrationView:
        VictoryCelebrationView

    private lateinit var saveButton:
        Button

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
                    "🧑‍🏫 Prof Gecko"

                textSize = 16f
                minHeight = dp(50)

                setOnClickListener {
                    showProfessorHint()
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
            professorBubble,
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

        root.addView(row1)
        root.addView(row2)
        root.addView(row3)

        root.addView(
            professorButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)

        celebrationView =
            VictoryCelebrationView(this)

        addContentView(
            celebrationView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        celebrationView.visibility =
            View.GONE

        protectFromSystemBars(root)
        refreshGameUi()
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
        puzzle = nextPuzzle

        selectedSize =
            puzzle.size

        engine =
            GameEngine(puzzle)

        pendingMarker = null
        eraseMarkerMode = false
        completionRecorded = false
        professorUsed = false

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
                fx.gecko()

                val remaining =
                    puzzle.size -
                        engine.snapshot()
                            .confirmed.size

                status.text =
                    encouragement(
                        remaining
                    )

                board
                    .announceForAccessibility(
                        "Gecko confirmé. " +
                            remaining +
                            " restant."
                    )
            }

            ActionFeedback.GECKO_REMOVED -> {
                fx.cross()
                status.text =
                    "Gecko retiré."
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
                completeGame()

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

            professorBubble
                .showMessage(message)

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

            professorBubble
                .showMessage(message)

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

            professorBubble
                .showMessage(message)

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

        professorBubble
            .showMessage(message)

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
            completeGame()
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

        if (
            ::professorBubble.isInitialized
        ) {
            professorBubble.hideMessage()
        }
    }

    private fun completeGame() {
        fx.complete()
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

    override fun onDestroy() {
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

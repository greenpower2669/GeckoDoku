package com.greenpower2669.geckodoku

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var puzzle: Puzzle
    private lateinit var engine: GameEngine
    private val fx: FxFeedback = ToneFxFeedback()
    private lateinit var statsStore: PlayerStatsStore

    private lateinit var board: GeckoBoardView
    private lateinit var status: TextView
    private lateinit var info: TextView
    private lateinit var soundButton: Button
    private lateinit var sizeButton: Button
    private lateinit var difficultyButton: Button

    private var selectedSize = 5
    private var selectedDifficulty = GameDifficulty.EASY
    private var pendingMarker: CustomMarker? = null
    private var eraseMarkerMode = false
    private var gameStartedAt = 0L
    private var completionRecorded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statsStore = PlayerStatsStore(this)
        createPuzzle(recordStart = true)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.rgb(247, 250, 247))
        }

        val title = TextView(this).apply {
            text = "GeckoDoku 🦎"
            textSize = 28f
            setTextColor(Color.rgb(20, 70, 40))
            gravity = Gravity.CENTER
        }

        info = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(4))
        }

        status = TextView(this).apply {
            textSize = 17f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            minHeight = dp(52)
        }

        board = GeckoBoardView(this).apply {
            setPuzzleAndRefresh(this@MainActivity.puzzle)
            snapshotProvider = { engine.snapshot() }
            onSingleTapCell = { handleSingleTap(it) }
            onDoubleTapCell = { handleDoubleTap(it) }
            onLongPressCell = { handleLongPress(it) }
            onLongPressOutside = { showMarkerPalette() }
        }

        sizeButton = Button(this).apply {
            textSize = 15f
            minHeight = dp(46)
            setOnClickListener { chooseSize() }
        }

        difficultyButton = Button(this).apply {
            textSize = 15f
            minHeight = dp(46)
            setOnClickListener { chooseDifficulty() }
        }

        val newButton = Button(this).apply {
            text = "↻ Nouvelle"
            textSize = 15f
            minHeight = dp(46)
            setOnClickListener {
                createPuzzle(recordStart = true)
                refreshGameUi()
            }
        }

        val statsButton = Button(this).apply {
            text = "Stats"
            textSize = 15f
            minHeight = dp(46)
            setOnClickListener { showStats() }
        }

        soundButton = Button(this).apply {
            text = "🔊 FX"
            textSize = 15f
            minHeight = dp(46)
            setOnClickListener {
                fx.enabled = !fx.enabled
                text = if (fx.enabled) "🔊 FX" else "🔇 FX"
            }
        }

        root.addView(title, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(info, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(status, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(board, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(sizeButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(difficultyButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(newButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(statsButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(soundButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        root.addView(row1)
        root.addView(row2)
        setContentView(root)
        refreshGameUi()
    }

    private fun createPuzzle(recordStart: Boolean) {
        puzzle = PuzzleGenerator.generate(selectedSize, selectedDifficulty)
        engine = GameEngine(puzzle)
        pendingMarker = null
        eraseMarkerMode = false
        completionRecorded = false
        gameStartedAt = SystemClock.elapsedRealtime()

        if (recordStart) {
            statsStore.recordStart(selectedSize, puzzle.difficulty)
        }
    }

    private fun refreshGameUi() {
        if (::board.isInitialized) board.setPuzzleAndRefresh(puzzle)
        val report = DifficultyIndexer.analyze(puzzle)

        if (::info.isInitialized) {
            val measured = report.ratedDifficulty.label
            val requestedText = if (report.ratedDifficulty == selectedDifficulty) {
                measured
            } else {
                selectedDifficulty.label + " demandé • " + measured + " mesuré"
            }

            val logic = buildString {
                append("zones ").append(report.features.regionLogicCount)
                if (report.features.xWingRequired) append(" • X-Wing requis")
                if (report.features.projectionRequired) append(" • projection requise")
            }

            info.text = selectedSize.toString() + "×" + selectedSize +
                " • " + requestedText +
                " • " + logic
        }

        if (::sizeButton.isInitialized) {
            sizeButton.text = "Taille " + selectedSize + "×" + selectedSize
        }

        if (::difficultyButton.isInitialized) {
            difficultyButton.text = selectedDifficulty.label
        }

        if (::status.isInitialized) {
            status.text = if (puzzle.givens.isNotEmpty()) {
                puzzle.givens.size.toString() +
                    " gecko(s) donné(s). Simple = ✕, double = 🦎."
            } else {
                "Simple = ✕, vrai double-clic = 🦎, appui long = hypothèse."
            }
        }
    }

    private fun handleSingleTap(cell: Cell) {
        if (placePendingMarkerIfNeeded(cell)) return

        when (engine.toggleCross(cell)) {
            ActionFeedback.CROSS_SET -> {
                fx.cross()
                status.text = "Croix posée."
            }
            ActionFeedback.CROSS_REMOVED -> {
                fx.cross()
                status.text = "Croix retirée."
            }
            ActionFeedback.CROSS_BLOCKED -> {
                fx.blocked()
                status.text = "Case déjà impossible grâce à un gecko."
            }
            ActionFeedback.GECKO_PRESENT -> {
                fx.blocked()
                status.text = "Un gecko est ici. Double-clic pour le retirer."
            }
            ActionFeedback.GIVEN_LOCKED -> {
                fx.blocked()
                status.text = "Gecko donné : il est verrouillé."
            }
            else -> Unit
        }
        board.invalidate()
    }

    private fun handleDoubleTap(cell: Cell) {
        if (placePendingMarkerIfNeeded(cell)) return

        when (engine.toggleGecko(cell)) {
            ActionFeedback.GECKO_CONFIRMED -> {
                fx.gecko()
                val remaining = puzzle.size - engine.snapshot().confirmed.size
                status.text = encouragement(remaining)
                board.announceForAccessibility("Gecko confirmé. " + remaining + " restant.")
            }
            ActionFeedback.GECKO_REMOVED -> {
                fx.cross()
                status.text = "Gecko retiré."
            }
            ActionFeedback.WRONG_GECKO -> {
                fx.error()
                statsStore.recordMistake()
                status.text = "Pas ici. Une croix reste en place."
                board.announceForAccessibility("Gecko incorrect.")
            }
            ActionFeedback.CROSS_BLOCKED -> {
                fx.blocked()
                status.text = "Cette case est déjà exclue."
            }
            ActionFeedback.GIVEN_LOCKED -> {
                fx.blocked()
                status.text = "Ce gecko est donné et ne peut pas être retiré."
            }
            ActionFeedback.COMPLETED -> completeGame()
            else -> Unit
        }
        board.invalidate()
    }

    private fun handleLongPress(cell: Cell) {
        when (engine.longPress(cell)) {
            ActionFeedback.HYPOTHESIS_CHANGED -> {
                fx.hint()
                status.text = when (engine.snapshot().hypotheses[cell] ?: HypothesisMark.NONE) {
                    HypothesisMark.GHOST_GECKO -> "Gecko hypothèse discret."
                    HypothesisMark.ALERT_GECKO -> "Gecko repère fort clignotant."
                    HypothesisMark.NONE -> "Hypothèse retirée."
                }
            }
            ActionFeedback.GIVEN_LOCKED -> {
                fx.blocked()
                status.text = "Gecko confirmé : hypothèse inutile ici."
            }
            ActionFeedback.CROSS_BLOCKED -> {
                fx.blocked()
                status.text = "Case déjà exclue."
            }
            else -> Unit
        }
        board.invalidate()
    }

    private fun completeGame() {
        fx.complete()
        if (!completionRecorded) {
            val seconds = (SystemClock.elapsedRealtime() - gameStartedAt) / 1000L
            statsStore.recordComplete(selectedSize, puzzle.difficulty, seconds)
            completionRecorded = true
        }
        status.text = "Bravo ! Grille terminée 🦎"
        board.announceForAccessibility("Bravo, grille terminée.")
    }

    private fun placePendingMarkerIfNeeded(cell: Cell): Boolean {
        if (pendingMarker == null && !eraseMarkerMode) return false

        val result = engine.placeCustomMarker(
            cell,
            if (eraseMarkerMode) null else pendingMarker
        )

        pendingMarker = null
        eraseMarkerMode = false
        fx.marker()

        status.text = if (result == ActionFeedback.CUSTOM_MARKER_CLEARED) {
            "Repère effacé."
        } else {
            "Repère personnel posé."
        }

        board.invalidate()
        return true
    }

    private fun chooseSize() {
        val values = intArrayOf(5, 6, 7, 8, 9, 10, 11, 12)
        val labels = values.map { it.toString() + "×" + it }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Taille de grille")
            .setSingleChoiceItems(labels, values.indexOf(selectedSize)) { dialog, which ->
                selectedSize = values[which]
                dialog.dismiss()
                createPuzzle(recordStart = true)
                refreshGameUi()
            }
            .show()
    }

    private fun chooseDifficulty() {
        val values = GameDifficulty.entries
        val labels = values.map { it.label }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Difficulté logique réelle")
            .setSingleChoiceItems(labels, values.indexOf(selectedDifficulty)) { dialog, which ->
                selectedDifficulty = values[which]
                dialog.dismiss()
                createPuzzle(recordStart = true)
                refreshGameUi()
            }
            .show()
    }

    private fun showMarkerPalette() {
        val markers = CustomMarker.entries
        val labels = markers.map { it.symbol + "  " + it.label }.toMutableList()
        labels.add("⌫  Effacer un repère")

        AlertDialog.Builder(this)
            .setTitle("Choisir un repère")
            .setItems(labels.toTypedArray()) { _, which ->
                if (which == markers.size) {
                    pendingMarker = null
                    eraseMarkerMode = true
                    status.text = "Touchez une case pour effacer son repère."
                } else {
                    pendingMarker = markers[which]
                    eraseMarkerMode = false
                    status.text = "Touchez une case pour placer : " + markers[which].label + "."
                }
                fx.marker()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showStats() {
        val s = statsStore.read()
        val text = buildString {
            append("Statistiques locales uniquement\n\n")
            append("Parties lancées : ").append(s.gamesStarted).append("\n")
            append("Parties terminées : ").append(s.gamesCompleted).append("\n")
            append("Réussite : ").append(s.completionRate).append("%\n")
            append("Erreurs : ").append(s.mistakes).append("\n")
            append("Temps moyen terminé : ").append(formatSeconds(s.averageSeconds)).append("\n\n")
            append("Terminées par taille :\n")
            for (size in 5..12) {
                append(size).append("×").append(size).append(" : ")
                    .append(statsStore.completedForSize(size)).append("\n")
            }
            append("\nTerminées par difficulté mesurée :\n")
            for (d in GameDifficulty.entries) {
                append(d.label).append(" : ")
                    .append(statsStore.completedForDifficulty(d)).append("\n")
            }
            append("\nTout reste sur ce téléphone.")
        }

        AlertDialog.Builder(this)
            .setTitle("Stats du joueur")
            .setMessage(text)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun encouragement(remaining: Int): String {
        val mistakes = engine.mistakes
        return when {
            mistakes == 0 && remaining == 1 -> "Excellent, encore un seul gecko !"
            mistakes == 0 && remaining <= 2 -> "Super, tu approches de la fin sans erreur."
            remaining <= 2 -> "Presque terminé : encore " + remaining + " geckos."
            else -> "Bien vu. Encore " + remaining + " geckos."
        }
    }

    private fun formatSeconds(total: Long): String {
        val minutes = total / 60
        val seconds = total % 60
        return minutes.toString() + " min " + seconds + " s"
    }

    override fun onDestroy() {
        fx.release()
        super.onDestroy()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}

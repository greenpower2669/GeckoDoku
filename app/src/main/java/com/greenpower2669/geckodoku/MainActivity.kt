package com.greenpower2669.geckodoku

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private val puzzle = Puzzle.demo5x5()
    private val engine = GameEngine(puzzle)
    private val fx: FxFeedback = ToneFxFeedback()

    private lateinit var board: GeckoBoardView
    private lateinit var status: TextView
    private lateinit var soundButton: Button
    private var pendingMarker: CustomMarker? = null
    private var eraseMarkerMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.rgb(247, 250, 247))
        }

        val title = TextView(this).apply {
            text = "GeckoDoku 🦎"
            textSize = 30f
            setTextColor(Color.rgb(20, 70, 40))
            gravity = Gravity.CENTER
        }

        val difficulty = DifficultyIndexer.analyze(puzzle)
        val info = TextView(this).apply {
            text = "Démo 5×5 • ${difficulty.label} • index ${difficulty.index}/100"
            textSize = 17f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(6))
        }

        status = TextView(this).apply {
            text = "1 tap = ✕ • 2e tap = gecko • appui long = hypothèse"
            textSize = 18f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            minHeight = dp(54)
        }

        board = GeckoBoardView(this).apply {
            puzzle = this@MainActivity.puzzle
            snapshotProvider = { engine.snapshot() }
            onTapCell = { handleTap(it) }
            onLongPressCell = { handleLongPress(it) }
            onLongPressOutside = { showMarkerPalette() }
        }

        soundButton = Button(this).apply {
            text = "🔊 FX : ON"
            textSize = 18f
            minHeight = dp(52)
            setOnClickListener {
                fx.enabled = !fx.enabled
                text = if (fx.enabled) "🔊 FX : ON" else "🔇 FX : OFF"
            }
        }

        val markerButton = Button(this).apply {
            text = "Repères"
            textSize = 18f
            minHeight = dp(52)
            setOnClickListener { showMarkerPalette() }
        }

        root.addView(title, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(info, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(status, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(board, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            addView(soundButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(markerButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
        root.addView(controls)
        setContentView(root)
    }

    private fun handleTap(cell: Cell) {
        if (pendingMarker != null || eraseMarkerMode) {
            val result = engine.placeCustomMarker(cell, if (eraseMarkerMode) null else pendingMarker)
            pendingMarker = null
            eraseMarkerMode = false
            fx.marker()
            status.text = if (result == ActionFeedback.CUSTOM_MARKER_CLEARED) "Repère effacé." else "Repère personnel posé."
            board.invalidate()
            return
        }

        when (engine.tap(cell)) {
            ActionFeedback.CROSS_SET -> {
                fx.cross()
                status.text = "Croix posée. Retouche la case pour proposer un gecko."
            }
            ActionFeedback.CROSS_BLOCKED -> {
                fx.blocked()
                status.text = "Cette case est déjà exclue par un gecko confirmé."
            }
            ActionFeedback.GECKO_CONFIRMED -> {
                fx.gecko()
                val remaining = puzzle.size - engine.snapshot().confirmed.size
                status.text = encouragement(remaining)
                board.announceForAccessibility("Gecko confirmé. $remaining gecko restant.")
            }
            ActionFeedback.GECKO_REMOVED -> {
                fx.cross()
                status.text = "Gecko retiré."
            }
            ActionFeedback.WRONG_GECKO -> {
                fx.error()
                status.text = "Pas ici. Erreur ${engine.mistakes}. La croix reste en place."
                board.announceForAccessibility("Gecko incorrect.")
            }
            ActionFeedback.COMPLETED -> {
                fx.complete()
                status.text = "Bravo ! Grille terminée 🦎"
                board.announceForAccessibility("Bravo, grille terminée.")
            }
            else -> Unit
        }
        board.invalidate()
    }

    private fun handleLongPress(cell: Cell) {
        engine.longPress(cell)
        fx.hint()
        status.text = when (engine.snapshot().hypotheses[cell] ?: HypothesisMark.NONE) {
            HypothesisMark.GHOST_GECKO -> "Gecko hypothèse discret."
            HypothesisMark.ALERT_GECKO -> "Gecko repère fort clignotant."
            HypothesisMark.NONE -> "Hypothèse retirée."
        }
        board.invalidate()
    }

    private fun showMarkerPalette() {
        val markers = CustomMarker.entries
        val labels = markers.map { "${it.symbol}  ${it.label}" }.toMutableList()
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
                    status.text = "Touchez une case pour placer : ${markers[which].label}."
                }
                fx.marker()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun encouragement(remaining: Int): String {
        val mistakes = engine.mistakes
        return when {
            mistakes == 0 && remaining == 1 -> "Excellent, encore un seul gecko !"
            mistakes == 0 && remaining <= 2 -> "Super, tu approches de la fin sans erreur."
            remaining <= 2 -> "Presque terminé : encore $remaining geckos."
            else -> "Bien vu. Encore $remaining geckos."
        }
    }

    override fun onDestroy() {
        fx.release()
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

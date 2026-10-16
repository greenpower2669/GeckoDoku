package com.greenpower2669.geckodoku

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression guard for Android AlertDialog: setMessage hides setItems. */
class InfernalWorkshopDialogVisibilityTest {
    private fun activitySource(): String {
        val relative = "src/main/java/com/greenpower2669/geckodoku/MainActivity.kt"
        val file = sequenceOf(
            File(relative), File("app/$relative")
        ).firstOrNull { it.isFile }
            ?: error("MainActivity.kt not found in Gradle project root")
        return file.readText()
    }

    private fun section(source: String, first: String, next: String): String {
        val from = source.indexOf("    private fun $first")
        require(from >= 0) { "Missing workshop dialog: $first" }
        val until = source.indexOf("    private fun $next", from + 1)
        require(until > from) { "Missing next workshop function: $next" }
        return source.substring(from, until)
    }

    @Test
    fun startStopAndArchiveCommandsMustNotBeHiddenByStatusMessage() {
        val menu = section(
            activitySource(),
            "showInfernalWorkshop()",
            "showInfernalWorkshopLiveCounters()"
        )
        assertTrue(menu.contains("DÉMARRER / REPRENDRE la recherche continue"))
        assertTrue(menu.contains("ARRÊTER la recherche continue"))
        assertTrue(menu.contains(".setItems(labels)"))
        assertFalse(menu.contains(".setMessage("))
    }

    @Test
    fun liveCountersAreInASeparateMessageOnlyDialog() {
        val live = section(
            activitySource(),
            "showInfernalWorkshopLiveCounters()",
            "workshopLiveCounters()"
        )
        assertTrue(live.contains(".setMessage(workshopLiveCounters())"))
        assertFalse(live.contains(".setItems("))
        assertTrue(live.contains("handler.postDelayed(ticker, 1_500L)"))
    }

    @Test
    fun sizeButtonsRemainVisible() {
        val sizes = section(
            activitySource(),
            "showInfernalWorkshopStats()",
            "showInfernalWorkshopSize("
        )
        assertTrue(sizes.contains(".setItems(labels)"))
        assertFalse(sizes.contains(".setMessage("))
    }

    @Test
    fun testApproveRejectButtonsRemainVisible() {
        val candidate = section(
            activitySource(),
            "showInfernalWorkshopEntry(",
            "showSettings()"
        )
        val listIndex = candidate.indexOf(".setItems(actions)")
        assertTrue(listIndex >= 0)
        assertFalse(candidate.substring(0, listIndex).contains(".setMessage("))
        assertTrue(candidate.contains(".setNeutralButton(\"Détails\")"))
    }
}

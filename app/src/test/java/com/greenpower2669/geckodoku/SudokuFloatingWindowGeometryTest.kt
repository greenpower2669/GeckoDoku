package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuFloatingWindowGeometryTest {
    @Test
    fun compactWindowUsesBoardWidthAndSitsDirectlyAboveIt() {
        val geometry =
            SudokuFloatingWindowGeometryPolicy
                .compactAboveBoard(
                    rootWidth =
                        1080,
                    rootHeight =
                        2200,
                    boardLeft =
                        20,
                    boardTop =
                        520,
                    boardWidth =
                        1040,
                    preferredHeight =
                        160,
                    minimumWidth =
                        500,
                    margin =
                        8,
                    gap =
                        6
                )

        assertEquals(
            20,
            geometry.x
        )
        assertEquals(
            1040,
            geometry.width
        )
        assertEquals(
            354,
            geometry.y
        )
        assertEquals(
            160,
            geometry.height
        )
    }

    @Test
    fun restoredWindowIsClampedInsideCurrentScreen() {
        val geometry =
            SudokuFloatingWindowGeometryPolicy
                .clamp(
                    geometry =
                        SudokuFloatingWindowGeometry(
                            x =
                                900,
                            y =
                                2000,
                            width =
                                700,
                            height =
                                300
                        ),
                    rootWidth =
                        1080,
                    rootHeight =
                        2200,
                    minimumWidth =
                        500,
                    minimumHeight =
                        120,
                    margin =
                        8
                )

        assertTrue(
            geometry.x +
                geometry.width <=
                1072
        )
        assertTrue(
            geometry.y +
                geometry.height <=
                2192
        )
    }
    @Test
    fun edgeToEdgeWindowRespectsStatusAndSamsungNavigationBars() {
        val safe = SudokuFloatingWindowGeometryPolicy.safeBounds(
            rootX = 0, rootY = 0,
            rootWidth = 1080, rootHeight = 2400,
            windowWidth = 1080, windowHeight = 2400,
            insetLeft = 0, insetTop = 75, insetRight = 0,
            insetBottom = 120
        )
        assertEquals(75, safe.top)
        assertEquals(2280, safe.bottom)
        val dragged = SudokuFloatingWindowGeometryPolicy.clampToBounds(
            SudokuFloatingWindowGeometry(100, 2200, 600, 520),
            safe, minimumWidth = 250, minimumHeight = 180, margin = 8
        )
        assertEquals(1752, dragged.y)
        assertTrue(dragged.y + dragged.height <= 2272)
    }

    @Test
    fun alreadyInsetRootDoesNotSubtractNavigationBarTwice() {
        val safe = SudokuFloatingWindowGeometryPolicy.safeBounds(
            rootX = 0, rootY = 75,
            rootWidth = 1080, rootHeight = 2205,
            windowWidth = 1080, windowHeight = 2400,
            insetLeft = 0, insetTop = 75, insetRight = 0,
            insetBottom = 120
        )
        assertEquals(75, safe.top)
        assertEquals(2280, safe.bottom)
    }

    @Test
    fun rotationShrinksAndClampsPreviouslyOversizedFloatingWindow() {
        val rotated = SudokuFloatingWindowBounds(40, 100, 640, 800)
        val fixed = SudokuFloatingWindowGeometryPolicy.clampToBounds(
            SudokuFloatingWindowGeometry(-20, 1400, 900, 850),
            rotated, minimumWidth = 280, minimumHeight = 200, margin = 8
        )
        assertEquals(584, fixed.width)
        assertEquals(684, fixed.height)
        assertEquals(48, fixed.x)
        assertEquals(108, fixed.y)
    }

    @Test
    fun keypadIsBottomCenteredAboveNavigationNotBelowIt() {
        val safe = SudokuFloatingWindowBounds(0, 75, 1080, 2280)
        val keypad = SudokuFloatingWindowGeometryPolicy.bottomCentered(
            safe, width = 600, height = 520,
            minWidth = 300, minHeight = 260,
            margin = 8, bottomGap = 18
        )
        assertEquals(240, keypad.x)
        assertEquals(1742, keypad.y)
        assertEquals(18, safe.bottom - keypad.y - keypad.height)
    }

    @Test
    fun extremelySmallTemporaryLayoutDoesNotCrashClamping() {
        val bounds = SudokuFloatingWindowBounds(10, 20, 20, 30)
        val result = SudokuFloatingWindowGeometryPolicy.clampToBounds(
            SudokuFloatingWindowGeometry(500, 500, 900, 900),
            bounds, minimumWidth = 300, minimumHeight = 300, margin = 8
        )
        assertEquals(1, result.width)
        assertEquals(1, result.height)
    }

    @Test
    fun sudokuToolsWaitForIntroAndVisibleBoard() {
        fun allowed(
            introDone: Boolean = true,
            introVideo: Boolean = false,
            curtain: Boolean = false,
            visible: Boolean = true,
            measured: Boolean = true,
            dismissed: Boolean = false,
            sudoku: Boolean = true
        ) = SudokuFloatingStartupPolicy.canOpen(
            sudokuMode = sudoku,
            introFinished = introDone,
            introVideoActive = introVideo,
            launchCurtainVisible = curtain,
            boardVisible = visible,
            boardMeasured = measured,
            dismissed = dismissed
        )
        assertTrue(allowed())
        assertTrue(!allowed(introDone = false))
        assertTrue(!allowed(introVideo = true))
        assertTrue(!allowed(curtain = true))
        assertTrue(!allowed(visible = false))
        assertTrue(!allowed(measured = false))
        assertTrue(!allowed(dismissed = true))
        assertTrue(!allowed(sudoku = false))
    }

    @Test
    fun keyboardDockNeverCoversBottomRowInPortrait() {
        val safe = SudokuFloatingWindowBounds(0, 60, 1080, 2390)
        val dock = SudokuFloatingWindowGeometryPolicy.dockBelowBoard(
            safe, boardBottom = 1710, gap = 12
        )
        val popup = SudokuFloatingWindowGeometryPolicy.bottomCentered(
            dock,
            width = 900,
            height = 800,
            minWidth = 280,
            minHeight = 160,
            margin = 8,
            bottomGap = 16
        )
        assertEquals(1722, dock.top)
        assertTrue(popup.y >= 1710 + 12)
        assertTrue(popup.y + popup.height <= 2390 - 8)
        assertTrue(popup.height < 800) // Dynamic shrink, not board overlap.
    }

    @Test
    fun keyboardDragAndResizeStayInsideItsDock() {
        val safe = SudokuFloatingWindowBounds(0, 60, 1080, 2390)
        val dock = SudokuFloatingWindowGeometryPolicy.dockBelowBoard(
            safe, boardBottom = 1730, gap = 12
        )
        val dragged = SudokuFloatingWindowGeometryPolicy.clampToBounds(
            SudokuFloatingWindowGeometry(0, 0, 850, 750),
            dock, minimumWidth = 280, minimumHeight = 160, margin = 8
        )
        assertTrue(dragged.y >= dock.top + 8)
        assertTrue(dragged.y + dragged.height <= dock.bottom - 8)
        val resized = SudokuFloatingWindowGeometryPolicy.bottomCentered(
            dock, 850, 1300, 280, 160, 8, 16
        )
        assertTrue(resized.y >= dock.top + 8)
        assertTrue(resized.y + resized.height <= dock.bottom - 8)
    }

    @Test
    fun keyboardDockChangesWithMeasuredBoardBottom() {
        val safe = SudokuFloatingWindowBounds(0, 50, 1080, 2370)
        val before = SudokuFloatingWindowGeometryPolicy.dockBelowBoard(
            safe, boardBottom = 1710, gap = 8
        )
        val after = SudokuFloatingWindowGeometryPolicy.dockBelowBoard(
            safe, boardBottom = 1810, gap = 8
        )
        assertEquals(100, after.top - before.top)
    }

    @Test
    fun samsungScreenshotPortraitOpensCompactAtRightBelowCompleteGrid() {
        // Screen-space geometry approximates Fab's second screenshot.
        val safe = SudokuFloatingWindowBounds(0, 45, 709, 1460)
        val dock = SudokuFloatingWindowGeometryPolicy.dockBelowBoard(
            safe, boardBottom = 1005, gap = 8
        )
        val opening = SudokuFloatingWindowGeometryPolicy.compactKeypadBelowBoard(
            dock,
            preferredWidth = 584, preferredHeight = 416,
            minimumWidth = 440, minimumHeight = 320,
            margin = 8, bottomGap = 16
        )
        assertEquals(117, opening.x)
        assertEquals(584, opening.width)
        assertEquals(416, opening.height)
        assertTrue(opening.y >= 1013)
        assertTrue(opening.y + opening.height <= 1460 - 8)
    }

    @Test
    fun physicalPopupMustUpdateEvenIfStoredGeometryMatchesDock() {
        val stored = SudokuFloatingWindowGeometry(
            x = 117, y = 1028, width = 584, height = 416
        )
        val dock = SudokuFloatingWindowBounds(0, 1013, 709, 1460)
        val fitted = SudokuFloatingWindowGeometryPolicy.clampToBounds(
            stored, dock, 440, 320, 8
        )
        assertEquals(stored, fitted)
        assertTrue(
            SudokuFloatingWindowGeometryPolicy.needsPhysicalUpdate(
                stored, fitted, actualWidth = 584, actualHeight = 598
            )
        )
        assertTrue(
            !SudokuFloatingWindowGeometryPolicy.needsPhysicalUpdate(
                stored, fitted, actualWidth = 584, actualHeight = 416
            )
        )
    }

    @Test
    fun reusedKeyboardPreservesValidUserPlacementButClampsOverlap() {
        val dock = SudokuFloatingWindowBounds(0, 1013, 709, 1460)
        val userPlacement = SudokuFloatingWindowGeometry(
            x = 110, y = 1030, width = 580, height = 410
        )
        assertEquals(
            userPlacement,
            SudokuFloatingWindowGeometryPolicy.clampToBounds(
                userPlacement, dock, 440, 320, 8
            )
        )
        val oldOversized = SudokuFloatingWindowGeometry(
            x = 50, y = 840, width = 610, height = 600
        )
        val corrected = SudokuFloatingWindowGeometryPolicy.clampToBounds(
            oldOversized, dock, 440, 320, 8
        )
        assertTrue(corrected.y >= dock.top + 8)
        assertTrue(corrected.y + corrected.height <= dock.bottom - 8)
        assertTrue(corrected.height < oldOversized.height)
    }

}

package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuPaletteLayoutPolicyTest {
    @Test
    fun fourPanelsSelectValueCandidateAndHypothesisDigits() {
        val policy =
            SudokuPaletteLayoutPolicy()

        val value =
            policy.tileBounds(
                digit = 1,
                panel =
                    SudokuPalettePanel
                        .VALUE,
                width = 360,
                height = 380
            )

        val candidate =
            policy.tileBounds(
                digit = 1,
                panel =
                    SudokuPalettePanel
                        .CANDIDATE,
                width = 360,
                height = 380
            )

        val hypothesis =
            policy.tileBounds(
                digit = 1,
                panel =
                    SudokuPalettePanel
                        .HYPOTHESIS,
                width = 360,
                height = 380
            )

        assertEquals(
            SudokuPaletteAction
                .Value(1),
            policy.actionAt(
                x =
                    (
                        value.left +
                            value.right
                        ) /
                        2f,
                y =
                    (
                        value.top +
                            value.bottom
                        ) /
                        2f,
                width = 360,
                height = 380
            )
        )

        assertEquals(
            SudokuPaletteAction
                .Candidate(1),
            policy.actionAt(
                x =
                    (
                        candidate.left +
                            candidate.right
                        ) /
                        2f,
                y =
                    (
                        candidate.top +
                            candidate.bottom
                        ) /
                        2f,
                width = 360,
                height = 380
            )
        )

        assertEquals(
            SudokuPaletteAction
                .Hypothesis(1),
            policy.actionAt(
                x =
                    (
                        hypothesis.left +
                            hypothesis.right
                        ) /
                        2f,
                y =
                    (
                        hypothesis.top +
                            hypothesis.bottom
                        ) /
                        2f,
                width = 360,
                height = 380
            )
        )
    }

    @Test
    fun confirmationModeOnlyOffersYesAndNoAndHidesCloseAction() {
        val policy =
            SudokuPaletteLayoutPolicy()

        val yes =
            policy.confirmYesBounds(
                360,
                380
            )

        val no =
            policy.confirmNoBounds(
                360,
                380
            )

        val close =
            policy.closeBounds(
                360,
                380
            )

        assertEquals(
            SudokuPaletteAction
                .ConfirmYes,
            policy.actionAt(
                x =
                    (
                        yes.left +
                            yes.right
                        ) /
                        2f,
                y =
                    (
                        yes.top +
                            yes.bottom
                        ) /
                        2f,
                width = 360,
                height = 380,
                confirmationActive =
                    true
            )
        )

        assertEquals(
            SudokuPaletteAction
                .ConfirmNo,
            policy.actionAt(
                x =
                    (
                        no.left +
                            no.right
                        ) /
                        2f,
                y =
                    (
                        no.top +
                            no.bottom
                        ) /
                        2f,
                width = 360,
                height = 380,
                confirmationActive =
                    true
            )
        )

        assertNull(
            policy.actionAt(
                x =
                    (
                        close.left +
                            close.right
                        ) /
                        2f,
                y =
                    (
                        close.top +
                            close.bottom
                        ) /
                        2f,
                width = 360,
                height = 380,
                confirmationActive =
                    true
            )
        )
    }

    @Test
    fun closeLivesInsideHeaderWhenNoConfirmationIsActive() {
        val policy =
            SudokuPaletteLayoutPolicy()

        val close =
            policy.closeBounds(
                360,
                380
            )

        assertEquals(
            SudokuPaletteAction
                .Close,
            policy.actionAt(
                x =
                    (
                        close.left +
                            close.right
                        ) /
                        2f,
                y =
                    (
                        close.top +
                            close.bottom
                        ) /
                        2f,
                width = 360,
                height = 380
            )
        )
    }

    @Test
    fun popupPlacementAlwaysStaysInsideScreenAndCanFlipAroundAnchor() {
        val policy =
            SudokuPopupPlacementPolicy()

        val nearRight =
            policy.place(
                screenWidth = 720,
                screenHeight = 1500,
                anchor = PixelBox(
                    left = 620,
                    top = 600,
                    right = 690,
                    bottom = 670
                ),
                popupWidth = 560,
                popupHeight = 440,
                margin = 16
            )

        assertTrue(
            nearRight.x >=
                16
        )

        assertTrue(
            nearRight.y >=
                16
        )

        assertTrue(
            nearRight.x +
                560 <=
                720 -
                    16
        )

        assertTrue(
            nearRight.y +
                440 <=
                1500 -
                    16
        )

        assertTrue(
            nearRight.x <
                620
        )
    }

    @Test
    fun helpButtonLivesBesideCloseAndIsDisabledDuringConfirmation() {
        val policy =
            SudokuPaletteLayoutPolicy()

        val help =
            policy.helpBounds(
                360,
                380
            )

        assertEquals(
            SudokuPaletteAction
                .Help,
            policy.actionAt(
                x =
                    (
                        help.left +
                            help.right
                        ) /
                        2f,
                y =
                    (
                        help.top +
                            help.bottom
                        ) /
                        2f,
                width = 360,
                height = 380
            )
        )

        assertNull(
            policy.actionAt(
                x =
                    (
                        help.left +
                            help.right
                        ) /
                        2f,
                y =
                    (
                        help.top +
                            help.bottom
                        ) /
                        2f,
                width = 360,
                height = 380,
                confirmationActive =
                    true
            )
        )
    }

    @Test
    fun panelsCanBeIdentifiedForInteractiveHelp() {
        val policy =
            SudokuPaletteLayoutPolicy()

        for (
            panel in
            SudokuPalettePanel.entries
        ) {
            val bounds =
                policy.panelBounds(
                    panel,
                    360,
                    380
                )

            assertEquals(
                panel,
                policy.panelAt(
                    x =
                        (
                            bounds.left +
                                bounds.right
                            ) /
                            2f,
                    y =
                        (
                            bounds.top +
                                bounds.bottom
                            ) /
                            2f,
                    width = 360,
                    height = 380
                )
            )
        }
    }
}

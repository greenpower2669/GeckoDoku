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
}

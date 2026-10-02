package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HypothesisBranchTraceTest {
    @Test
    fun crossesInheritActiveHypothesisColor() {
        val trace =
            HypothesisBranchTrace<String>()

        val first =
            trace.startHypothesis(
                "A"
            )

        assertEquals(
            HypothesisColor.YELLOW,
            first.color
        )

        assertEquals(
            HypothesisCrossChange.ADDED,
            trace.toggleCross(
                "x1"
            )
        )

        val second =
            trace.startHypothesis(
                "B"
            )

        assertEquals(
            HypothesisColor.GREEN,
            second.color
        )

        assertEquals(
            HypothesisCrossChange.ADDED,
            trace.toggleCross(
                "x2"
            )
        )

        val snapshot =
            trace.snapshot()

        assertEquals(
            HypothesisColor.YELLOW,
            snapshot.colorForCross(
                "x1"
            )
        )

        assertEquals(
            HypothesisColor.GREEN,
            snapshot.colorForCross(
                "x2"
            )
        )
    }

    @Test
    fun removingParentPrunesEveryDescendantAndOwnedCross() {
        val trace =
            HypothesisBranchTrace<String>()

        trace.startHypothesis(
            "A"
        )
        trace.toggleCross(
            "x1"
        )
        trace.startHypothesis(
            "B"
        )
        trace.toggleCross(
            "x2"
        )
        trace.startHypothesis(
            "C"
        )
        trace.toggleCross(
            "x3"
        )

        val prune =
            trace.removeBranch(
                "A"
            )

        assertEquals(
            setOf(
                "A",
                "B",
                "C"
            ),
            prune.hypothesisCells
        )

        assertEquals(
            setOf(
                "x1",
                "x2",
                "x3"
            ),
            prune.crossCells
        )

        assertTrue(
            trace.snapshot()
                .nodes
                .isEmpty()
        )

        assertTrue(
            trace.snapshot()
                .crossOwners
                .isEmpty()
        )
    }

    @Test
    fun contradictionPropagatesToChildHypotheses() {
        val trace =
            HypothesisBranchTrace<String>()

        trace.startHypothesis(
            "A"
        )
        trace.startHypothesis(
            "B"
        )
        trace.startHypothesis(
            "C"
        )

        assertTrue(
            trace.markContradiction(
                "A"
            )
        )

        assertTrue(
            trace.snapshot()
                .nodes
                .all {
                    it.state ==
                        HypothesisBranchState
                            .CONTRADICTION
                }
        )

        assertEquals(
            HypothesisCrossChange.BLOCKED,
            trace.toggleCross(
                "x"
            )
        )
    }

    @Test
    fun rewindKeepsParentMarksAndRemovesOnlyChildren() {
        val trace =
            HypothesisBranchTrace<String>()

        val parent =
            trace.startHypothesis(
                "A"
            )
        trace.toggleCross(
            "parent-cross"
        )
        trace.startHypothesis(
            "B"
        )
        trace.toggleCross(
            "child-cross"
        )

        val prune =
            trace.rewindTo(
                parent.id
            )

        assertEquals(
            setOf("B"),
            prune
                ?.hypothesisCells
        )

        assertEquals(
            setOf(
                "child-cross"
            ),
            prune
                ?.crossCells
        )

        val snapshot =
            trace.snapshot()

        assertEquals(
            parent.id,
            snapshot.activeId
        )

        assertEquals(
            HypothesisColor.YELLOW,
            snapshot.colorForCross(
                "parent-cross"
            )
        )

        assertNull(
            snapshot.colorForCross(
                "child-cross"
            )
        )

        assertFalse(
            snapshot.nodes
                .any {
                    it.cell ==
                        "B"
                }
        )
    }
}

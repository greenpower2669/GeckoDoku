package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EncouragementSelectorTest {
    @Test
    fun nearEndPhraseIsExcludedFarFromFinish() {
        val selector = EncouragementSelector()
        val eligible = selector.eligibleIndices(remaining = 5)
        assertFalse(eligible.contains(7))
        assertTrue(eligible.contains(0))
    }

    @Test
    fun nearEndPhraseIsEligibleForOneOrTwoRemaining() {
        val selector = EncouragementSelector()
        assertTrue(selector.eligibleIndices(remaining = 2).contains(7))
        assertTrue(selector.eligibleIndices(remaining = 1).contains(7))
    }

    @Test
    fun selectionDoesNotRepeatImmediatelyWhenAlternativesExist() {
        val selector = EncouragementSelector()
        val first = selector.choose(remaining = 5, randomValue = 0)
        val second = selector.choose(remaining = 5, randomValue = 0)
        assertNotEquals(first, second)
    }

    @Test
    fun selectorMapsStableCatalogIndices() {
        val selector = EncouragementSelector()
        assertEquals(13, selector.totalCount)
        assertEquals(0, selector.choose(remaining = 5, randomValue = 0))
    }
}

package com.greenpower2669.geckodoku

import org.junit.Assert.assertEquals
import org.junit.Test

class EncouragementSourcePolicyTest {
    @Test
    fun policyKeepsRecordedAndAddsPierre() {
        val policy = EncouragementSourcePolicy()

        assertEquals(
            EncouragementSource.RECORDED,
            policy.choose(0)
        )
        assertEquals(
            EncouragementSource.PIERRE,
            policy.choose(1)
        )
    }

    @Test
    fun pierreHasAdditionalPhrases() {
        val phrases =
            PierreEncouragements.phrases

        assert(phrases.size >= 8)
        assert(
            phrases.none {
                it.isBlank()
            }
        )
    }
}

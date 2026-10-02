package com.greenpower2669.geckodoku

import kotlin.random.Random

enum class PhraseCategory {
    GENERAL, SUCCESS, ERROR, STREAK, HESITATION,
    ABSURD, SELF, SMART, FINISH, RETURN,
    RARE, FAB, TAQUIN
}

enum class PhraseRarity {
    NORMAL, SEMI_RARE, RARE
}

enum class ProfessorMood {
    NEUTRAL, PROUD, IMPRESSED, TAQUIN,
    PEDAGOGICAL, ENCOURAGING, CURIOUS
}

data class ProfessorPhrase(
    val id: String,
    val text: String,
    val category: PhraseCategory,
    val rarity: PhraseRarity = PhraseRarity.NORMAL,
    val moods: Set<ProfessorMood> = emptySet()
)

interface ProfessorClock {
    fun nowMs(): Long
}

object SystemProfessorClock : ProfessorClock {
    override fun nowMs(): Long =
        System.currentTimeMillis()
}

interface ProfessorRandom {
    fun nextInt(bound: Int): Int
}

object KotlinProfessorRandom : ProfessorRandom {
    override fun nextInt(bound: Int): Int =
        Random.nextInt(bound)
}

package com.greenpower2669.geckodoku

import java.util.Random

/**
 * Construct puzzle clues in the FORWARD direction.
 *
 * The valid full solution is hidden; the player sees no Gecko at first.
 * Each stage adds exactly one genuine clue. Never remove an existing
 * Gecko and never invent a location outside the secret solution.
 *
 * The unique-solution boundary is evaluated by PuzzleGenerator, not
 * by the sequence itself (which is deliberately independent of Sudoku).
 */
object WorkshopForwardPath {
    fun stages(solution: Set<Cell>, random: Random): List<Set<Cell>> {
        val order = solution.shuffled(random)
        val placed = linkedSetOf<Cell>()
        val stages = ArrayList<Set<Cell>>(order.size + 1)
        stages.add(emptySet())
        for (cell in order) {
            check(placed.add(cell)) { "Duplicate Gecko in complete solution" }
            stages.add(placed.toSet())
        }
        return stages
    }
}

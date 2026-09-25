package com.greenpower2669.geckodoku

import java.util.Random

object PuzzleGenerator {
    private val permutationCache = mutableMapOf<Int, List<IntArray>>()

    fun generate(
        size: Int,
        difficulty: GameDifficulty,
        seed: Long = System.nanoTime()
    ): Puzzle {
        require(size in 5..8)
        val random = Random(seed)
        val permutations = validPermutations(size)

        repeat(180) { attempt ->
            val solution = permutations[random.nextInt(permutations.size)]
            val regions = growRegions(size, solution, random)
            if (countSolutions(size, regions, permutations, 2) == 1) {
                val base = Puzzle(
                    id = "g-" + size + "-" + difficulty.name.lowercase() + "-" + seed + "-" + attempt,
                    size = size,
                    regions = regions,
                    solutionCols = solution.copyOf(),
                    difficulty = difficulty,
                    seed = seed
                )
                val givens = HumanSolver.selectGivens(base, difficulty, seed + attempt * 31L)
                val prepared = base.copy(givens = givens)
                if (HumanSolver.analyze(prepared, givens, difficulty.allowedTechnique()).solved) return prepared
            }
        }

        return fallback(size, difficulty, seed)
    }

    private fun validPermutations(size: Int): List<IntArray> =
        permutationCache.getOrPut(size) {
            val result = mutableListOf<IntArray>()
            val current = IntArray(size) { -1 }
            val used = BooleanArray(size)

            fun build(row: Int) {
                if (row == size) {
                    result.add(current.copyOf())
                    return
                }
                for (col in 0 until size) {
                    if (used[col]) continue
                    if (row > 0 && kotlin.math.abs(current[row - 1] - col) <= 1) continue
                    used[col] = true
                    current[row] = col
                    build(row + 1)
                    used[col] = false
                }
            }
            build(0)
            result
        }

    private fun growRegions(size: Int, solution: IntArray, random: Random): IntArray {
        val regions = IntArray(size * size) { -1 }
        val unassigned = mutableSetOf<Int>()
        for (i in regions.indices) unassigned.add(i)

        for (r in 0 until size) {
            val idx = r * size + solution[r]
            regions[idx] = r
            unassigned.remove(idx)
        }

        while (unassigned.isNotEmpty()) {
            val expandable = mutableListOf<Pair<Int, List<Int>>>()
            for (idx in unassigned) {
                val r = idx / size
                val c = idx % size
                val neighborRegions = linkedSetOf<Int>()
                val neighbors = arrayOf(r - 1 to c, r + 1 to c, r to c - 1, r to c + 1)
                for ((rr, cc) in neighbors) {
                    if (rr !in 0 until size || cc !in 0 until size) continue
                    val region = regions[rr * size + cc]
                    if (region >= 0) neighborRegions.add(region)
                }
                if (neighborRegions.isNotEmpty()) expandable.add(idx to neighborRegions.toList())
            }
            if (expandable.isEmpty()) break
            val (idx, choices) = expandable[random.nextInt(expandable.size)]
            regions[idx] = choices[random.nextInt(choices.size)]
            unassigned.remove(idx)
        }

        return regions
    }

    private fun countSolutions(
        size: Int,
        regions: IntArray,
        permutations: List<IntArray>,
        limit: Int
    ): Int {
        var count = 0
        for (perm in permutations) {
            val seen = BooleanArray(size)
            var valid = true
            for (r in 0 until size) {
                val region = regions[r * size + perm[r]]
                if (region !in 0 until size || seen[region]) {
                    valid = false
                    break
                }
                seen[region] = true
            }
            if (valid) {
                count++
                if (count >= limit) return count
            }
        }
        return count
    }

    private fun fallback(size: Int, difficulty: GameDifficulty, seed: Long): Puzzle {
        val pair = when (size) {
            5 -> intArrayOf(0,2,4,1,3) to intArrayOf(
                0,1,1,1,1,
                1,1,1,1,2,
                1,1,1,1,2,
                1,3,3,4,4,
                3,3,3,4,4
            )
            6 -> intArrayOf(0,2,4,1,3,5) to intArrayOf(
                0,3,1,2,2,2,
                3,3,1,2,2,2,
                3,3,1,2,2,2,
                3,3,2,2,2,2,
                3,3,2,4,2,5,
                3,3,3,3,5,5
            )
            7 -> intArrayOf(0,2,4,1,5,3,6) to intArrayOf(
                0,1,1,1,1,1,1,
                1,1,1,1,1,1,1,
                3,1,3,2,2,2,4,
                3,3,3,3,2,2,4,
                3,3,5,5,5,4,4,
                3,3,3,5,5,5,4,
                3,3,5,5,5,5,6
            )
            else -> intArrayOf(0,2,4,1,5,7,3,6) to intArrayOf(
                0,0,2,2,2,2,2,4,
                1,1,1,1,2,2,2,4,
                3,3,2,2,2,4,4,4,
                3,3,3,4,4,4,4,4,
                3,6,3,6,6,4,5,5,
                3,6,6,6,6,5,5,5,
                6,6,6,6,6,6,7,5,
                6,6,6,6,6,6,7,7
            )
        }

        val base = Puzzle(
            id = "fallback-" + size + "-" + difficulty.name.lowercase() + "-" + seed,
            size = size,
            regions = pair.second,
            solutionCols = pair.first,
            difficulty = difficulty,
            seed = seed
        )
        return base.copy(givens = HumanSolver.selectGivens(base, difficulty, seed))
    }
}

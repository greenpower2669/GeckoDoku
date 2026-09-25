package com.greenpower2669.geckodoku

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class JournalEntry(
    val id: String,
    val size: Int,
    val difficulty: GameDifficulty,
    val savedAt: Long
)

class PuzzleJournalStore(context: Context) {
    private val prefs =
        context.getSharedPreferences(
            "geckodoku_grid_journal",
            Context.MODE_PRIVATE
        )

    private val key = "entries_v1"

    fun save(puzzle: Puzzle) {
        val entries = readObjects().toMutableList()
        entries.removeAll { it.optString("id") == puzzle.id }

        val obj = JSONObject().apply {
            put("id", puzzle.id)
            put("size", puzzle.size)
            put("difficulty", puzzle.difficulty.name)
            put("seed", puzzle.seed)
            put("savedAt", System.currentTimeMillis())
            put("regions", intArrayToJson(puzzle.regions))
            put("solutionCols", intArrayToJson(puzzle.solutionCols))
            put("givens", cellsToJson(puzzle.givens))
        }

        entries.add(0, obj)
        writeObjects(entries)
    }

    fun list(): List<JournalEntry> =
        readObjects().mapNotNull { obj ->
            val id = obj.optString("id")
            val size = obj.optInt("size", 0)
            val difficultyName = obj.optString("difficulty")
            val savedAt = obj.optLong("savedAt", 0L)

            val difficulty =
                GameDifficulty.entries
                    .firstOrNull { it.name == difficultyName }
                    ?: return@mapNotNull null

            if (id.isBlank() || size !in 5..12) {
                return@mapNotNull null
            }

            JournalEntry(
                id = id,
                size = size,
                difficulty = difficulty,
                savedAt = savedAt
            )
        }

    fun load(id: String): Puzzle? {
        val obj =
            readObjects()
                .firstOrNull {
                    it.optString("id") == id
                }
                ?: return null

        val size =
            obj.optInt("size", 0)

        if (size !in 5..12) {
            return null
        }

        val difficulty =
            GameDifficulty.entries
                .firstOrNull {
                    it.name ==
                        obj.optString("difficulty")
                }
                ?: GameDifficulty.EASY

        val regions =
            jsonToIntArray(
                obj.optJSONArray("regions")
                    ?: return null
            )

        val solutionCols =
            jsonToIntArray(
                obj.optJSONArray("solutionCols")
                    ?: return null
            )

        if (regions.size != size * size ||
            solutionCols.size != size
        ) {
            return null
        }

        val givens =
            jsonToCells(
                obj.optJSONArray("givens")
                    ?: JSONArray()
            )

        val base = Puzzle(
            id = obj.optString("id"),
            size = size,
            regions = regions,
            solutionCols = solutionCols,
            givens = givens,
            difficulty = difficulty,
            seed = obj.optLong("seed", 0L)
        )

        val trace =
            HumanSolver.analyze(
                puzzle = base,
                givens = base.givens,
                rules = SolverRules.FULL
            ).steps

        return base.copy(
            solverTrace = trace
        )
    }

    fun delete(id: String) {
        val kept =
            readObjects()
                .filterNot {
                    it.optString("id") == id
                }

        writeObjects(kept)
    }

    fun clear() {
        prefs.edit()
            .remove(key)
            .apply()
    }

    fun contains(id: String): Boolean =
        readObjects().any {
            it.optString("id") == id
        }

    private fun readObjects(): List<JSONObject> {
        val raw =
            prefs.getString(
                key,
                "[]"
            ) ?: "[]"

        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    array.optJSONObject(i)
                        ?.let { add(it) }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun writeObjects(
        entries: List<JSONObject>
    ) {
        val array = JSONArray()
        entries.forEach {
            array.put(it)
        }

        prefs.edit()
            .putString(
                key,
                array.toString()
            )
            .apply()
    }

    private fun intArrayToJson(
        values: IntArray
    ): JSONArray {
        val array = JSONArray()
        values.forEach {
            array.put(it)
        }
        return array
    }

    private fun jsonToIntArray(
        array: JSONArray
    ): IntArray =
        IntArray(array.length()) {
            index ->
            array.optInt(index)
        }

    private fun cellsToJson(
        cells: Set<Cell>
    ): JSONArray {
        val array = JSONArray()

        cells.sortedWith(
            compareBy<Cell> { it.row }
                .thenBy { it.col }
        ).forEach { cell ->
            array.put(
                JSONObject().apply {
                    put("row", cell.row)
                    put("col", cell.col)
                }
            )
        }

        return array
    }

    private fun jsonToCells(
        array: JSONArray
    ): Set<Cell> {
        val result =
            linkedSetOf<Cell>()

        for (i in 0 until array.length()) {
            val obj =
                array.optJSONObject(i)
                    ?: continue

            val row =
                obj.optInt("row", -1)

            val col =
                obj.optInt("col", -1)

            if (row >= 0 && col >= 0) {
                result.add(
                    Cell(row, col)
                )
            }
        }

        return result
    }
}

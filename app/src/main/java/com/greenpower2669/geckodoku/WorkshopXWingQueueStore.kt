package com.greenpower2669.geckodoku

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/**
 * Layer 1 archives every distinct, unique grid where a non-empty Gecko
 * X-Wing elimination was observed. Layer 2 reviews a pending record without
 * changing the player's manually validated Infernal library.
 *
 * Records survive process death, manual STOP, thermal pauses and app restarts.
 */
data class WorkshopXWingProspect(
    val id: String,
    val puzzle: Puzzle,
    val sourceCells: Set<Cell>,
    val eliminated: Set<Cell>,
    val axis: String,
    val detectedAt: Long,
    val status: WorkshopXWingStatus = WorkshopXWingStatus.PENDING,
    // Independent, optional laboratory outcomes: old v7 records are intact.
    val laboratory: Map<WorkshopComparisonLane, WorkshopComparisonAttempt> = emptyMap()
)

data class WorkshopComparisonAttempt(
    val verdict: WorkshopComparisonVerdict,
    val depth: Int,
    val hypotheses: Int,
    val xWings: Int
)

data class WorkshopComparisonStats(
    val total: Int,
    val pending: Int,
    val matched: Int,
    val rejected: Int
)

data class WorkshopXWingStats(
    val detected: Int,
    val pending: Int,
    val promising: Int,
    val explored: Int,
    val comparisons: Map<WorkshopComparisonLane, WorkshopComparisonStats> = emptyMap()
)

class WorkshopXWingQueueStore(context: Context) {
    companion object {
        private val lock = Any()
        private const val SCHEMA = 1
    }

    private val folder = File(
        context.applicationContext.filesDir,
        "infernal_workshop/xwing_detected_v7"
    ).apply { mkdirs() }

    fun enqueue(puzzle: Puzzle, evidence: SolveStep): Boolean = synchronized(lock) {
        require(WorkshopXWingTwoLayer.isProspect(evidence))
        require(puzzle.givens.all { it in puzzle.solutionCells() })
        val id = fingerprint(puzzle)
        val destination = path(id)
        if (destination.exists()) return@synchronized false
        write(WorkshopXWingProspect(
            id, puzzle, evidence.sourceCells, evidence.eliminated,
            evidence.axis ?: "Gecko X-Wing",
            System.currentTimeMillis()
        ))
        true
    }

    fun nextUntried(lane: WorkshopComparisonLane): WorkshopXWingProspect? =
        synchronized(lock) {
            records().asSequence()
                .filter { lane !in it.laboratory }
                .minWithOrNull(
                    compareBy<WorkshopXWingProspect> { it.detectedAt }.thenBy { it.id }
                )
        }

    fun completeExperiment(
        id: String, lane: WorkshopComparisonLane,
        result: WorkshopComparisonResult
    ): Boolean = synchronized(lock) {
        val old = read(id) ?: return@synchronized false
        if (lane in old.laboratory) return@synchronized false
        val attempt = WorkshopComparisonAttempt(
            verdict = if (result.match) {
                WorkshopComparisonVerdict.MATCH
            } else {
                WorkshopComparisonVerdict.NO_MATCH
            },
            depth = result.depth,
            hypotheses = result.hypothesisCount,
            xWings = result.xWingCount
        )
        write(old.copy(laboratory = old.laboratory + (lane to attempt)))
        true
    }

    fun comparisonStats(): Map<WorkshopComparisonLane, WorkshopComparisonStats> =
        stats().comparisons

    fun matching(lane: WorkshopComparisonLane, size: Int? = null): List<WorkshopXWingProspect> =
        synchronized(lock) {
            records().filter { prospect ->
                (size == null || prospect.puzzle.size == size) &&
                    prospect.laboratory[lane]?.verdict == WorkshopComparisonVerdict.MATCH
            }.sortedByDescending { it.detectedAt }
        }

    fun nextPending(): WorkshopXWingProspect? = synchronized(lock) {
        records().filter { it.status == WorkshopXWingStatus.PENDING }
            .minWithOrNull(
                compareBy<WorkshopXWingProspect> { it.detectedAt }.thenBy { it.id }
            )
    }

    fun complete(id: String, proofAccepted: Boolean): Boolean = synchronized(lock) {
        val current = read(id) ?: return@synchronized false
        if (current.status != WorkshopXWingStatus.PENDING) return@synchronized false
        write(current.copy(status = WorkshopXWingTwoLayer.afterProof(
            current.status, interrupted = false, accepted = proofAccepted
        )))
        true
    }

    fun stats(): WorkshopXWingStats = synchronized(lock) {
        val all = records()
        WorkshopXWingStats(
            detected = all.size,
            pending = all.count { it.status == WorkshopXWingStatus.PENDING },
            promising = all.count { it.status == WorkshopXWingStatus.PROMISING },
            explored = all.count { it.status == WorkshopXWingStatus.EXPLORED },
            comparisons = WorkshopComparisonLane.entries.associateWith { lane ->
                val seen = all.mapNotNull { it.laboratory[lane] }
                WorkshopComparisonStats(
                    total = all.size,
                    pending = all.size - seen.size,
                    matched = seen.count { it.verdict == WorkshopComparisonVerdict.MATCH },
                    rejected = seen.count { it.verdict == WorkshopComparisonVerdict.NO_MATCH }
                )
            }
        )
    }

    private fun records(): List<WorkshopXWingProspect> =
        folder.listFiles()?.asSequence()
            ?.filter { it.name.matches(Regex("[a-f0-9]{64}\\.json")) }
            ?.mapNotNull { read(it.name.removeSuffix(".json")) }
            ?.toList() ?: emptyList()

    private fun path(id: String): File {
        require(id.matches(Regex("[a-f0-9]{64}")))
        return File(folder, "$id.json")
    }

    private fun read(id: String): WorkshopXWingProspect? = try {
        val f = path(id)
        if (!f.exists()) null else {
            val obj = AtomicFile(f).openRead().bufferedReader().use {
                JSONObject(it.readText())
            }
            require(obj.getInt("schema") == SCHEMA)
            val n = obj.getInt("size")
            require(n in InfernalWorkshopPolicy.sizes)
            val regions = parseInts(obj.getJSONArray("regions"))
            val solution = parseInts(obj.getJSONArray("solutionCols"))
            require(regions.size == n * n && solution.size == n)
            val givens = parseCells(obj.getJSONArray("givens"), n)
            val puzzle = Puzzle(
                id = obj.getString("puzzleId"),
                size = n,
                regions = regions,
                solutionCols = solution,
                givens = givens,
                difficulty = GameDifficulty.INFERNAL,
                seed = obj.getLong("seed")
            )
            require(fingerprint(puzzle) == id)
            require(givens.all { it in puzzle.solutionCells() })
            WorkshopXWingProspect(
                id, puzzle,
                parseCells(obj.getJSONArray("sourceCells"), n),
                parseCells(obj.getJSONArray("eliminated"), n),
                obj.optString("axis", "Gecko X-Wing").take(160),
                obj.getLong("detectedAt"),
                WorkshopXWingStatus.entries.firstOrNull {
                    it.name == obj.optString("status")
                } ?: WorkshopXWingStatus.PENDING,
                laboratory = parseLaboratory(obj.optJSONObject("laboratory"))
            )
        }
    } catch (_: Exception) {
        null
    }

    private fun write(item: WorkshopXWingProspect) {
        val puzzle = item.puzzle
        val json = JSONObject().apply {
            put("schema", SCHEMA)
            put("id", item.id)
            put("puzzleId", puzzle.id)
            put("size", puzzle.size)
            put("regions", ints(puzzle.regions))
            put("solutionCols", ints(puzzle.solutionCols))
            put("givens", cells(puzzle.givens, puzzle.size))
            put("seed", puzzle.seed)
            put("sourceCells", cells(item.sourceCells, puzzle.size))
            put("eliminated", cells(item.eliminated, puzzle.size))
            put("axis", item.axis)
            put("detectedAt", item.detectedAt)
            put("status", item.status.name)
            put("laboratory", JSONObject().also { labs ->
                for ((lane, assessment) in item.laboratory) {
                    labs.put(lane.name, JSONObject().apply {
                        put("verdict", assessment.verdict.name)
                        put("depth", assessment.depth)
                        put("hypotheses", assessment.hypotheses)
                        put("xWings", assessment.xWings)
                    })
                }
            })
        }
        val atom = AtomicFile(path(item.id))
        val stream = atom.startWrite()
        try {
            stream.write(json.toString().toByteArray(Charsets.UTF_8))
            atom.finishWrite(stream)
        } catch (e: Exception) {
            atom.failWrite(stream)
            throw e
        }
    }

    private fun parseLaboratory(json: JSONObject?): Map<WorkshopComparisonLane, WorkshopComparisonAttempt> {
        if (json == null) return emptyMap()
        val result = mutableMapOf<WorkshopComparisonLane, WorkshopComparisonAttempt>()
        for (lane in WorkshopComparisonLane.entries) {
            val entry = json.optJSONObject(lane.name) ?: continue
            val verdict = WorkshopComparisonVerdict.entries.firstOrNull {
                it.name == entry.optString("verdict")
            } ?: continue
            val depth = entry.optInt("depth", -1)
            val hypotheses = entry.optInt("hypotheses", -1)
            val xWings = entry.optInt("xWings", -1)
            if (depth < 0 || hypotheses < 0 || xWings < 0) continue
            result[lane] = WorkshopComparisonAttempt(
                verdict, depth, hypotheses, xWings
            )
        }
        return result
    }

    private fun fingerprint(puzzle: Puzzle): String {
        val key = puzzle.size.toString() + "|" +
            puzzle.regions.joinToString(",") + "|" +
            puzzle.givens.map { it.index(puzzle.size) }.sorted().joinToString(",")
        return MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun ints(arr: IntArray) = JSONArray().also { j ->
        arr.forEach { j.put(it) }
    }
    private fun cells(arr: Set<Cell>, size: Int) = JSONArray().also { j ->
        arr.sortedWith(compareBy<Cell> { it.row }.thenBy { it.col })
            .forEach { j.put(it.index(size)) }
    }

    private fun parseInts(arr: JSONArray) = IntArray(arr.length()) { arr.getInt(it) }

    private fun parseCells(arr: JSONArray, size: Int): Set<Cell> {
        val cells = linkedSetOf<Cell>()
        for (i in 0 until arr.length()) {
            val index = arr.getInt(i)
            require(index >= 0)
            val cell = Cell(index / size, index % size)
            require(cell.row in 0 until size && cell.col in 0 until size)
            require(cells.add(cell))
        }
        return cells
    }
}

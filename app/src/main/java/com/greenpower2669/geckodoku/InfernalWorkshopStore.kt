package com.greenpower2669.geckodoku

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

enum class WorkshopStatus {
    CANDIDATE, TESTING, VALIDATED, REJECTED
}

data class WorkshopEntry(
    val id: String,
    val puzzle: Puzzle,
    val xWingCount: Int,
    val nestedDepth: Int,
    val hypothesisCount: Int,
    val createdAt: Long,
    val status: WorkshopStatus = WorkshopStatus.CANDIDATE,
    val notes: String = "",
    val xWingExclusionModelComplete: Boolean = false,
    val shallowExclusionModelComplete: Boolean = false
) {
    // The player has approved this H2/H3/H4 candidate after a test.
    // Depth is always displayed as recorded; H2 is never shown as H3.
    val eligibleForInfernal: Boolean
        get() = status == WorkshopStatus.VALIDATED &&
            InfernalWorkshopPolicy.qualifies(
                puzzle.size, nestedDepth, xWingCount
            )
}

class InfernalWorkshopStore(context: Context) {
    companion object {
        private val lock = Any()
        private const val FORMAT = 1
        private const val MAX_IMPORT_ENTRIES = 5000
        private const val MAX_RECORD_BYTES = 256 * 1024
    }

    private val directory = File(
        context.applicationContext.filesDir, "infernal_workshop/records"
    ).apply { mkdirs() }

    fun list(): List<WorkshopEntry> = synchronized(lock) {
        directory.listFiles()?.asSequence()
            ?.filter { it.isFile && it.name.matches(Regex("[a-f0-9]{64}\\.json")) }
            ?.mapNotNull { file ->
                try { fromJson(JSONObject(file.readText(Charsets.UTF_8))) }
                catch (_: Exception) { null }
            }
            ?.sortedByDescending { it.createdAt }
            ?.toList() ?: emptyList()
    }

    fun add(proof: WorkshopCandidateProof): Boolean = synchronized(lock) {
        val puzzle = proof.puzzle
        if (!InfernalWorkshopPolicy.qualifies(
                puzzle.size, proof.hypothesisDepth, proof.xWingCount
            ) || !PuzzleGenerator.hasExactlyOneSolution(puzzle)
        ) return@synchronized false
        val id = fingerprint(puzzle)
        val file = path(id)
        if (file.exists()) return@synchronized false
        persist(WorkshopEntry(
            id, puzzle, proof.xWingCount,
            proof.hypothesisDepth, proof.hypothesisCount,
            System.currentTimeMillis(),
            xWingExclusionModelComplete =
                proof.xWingExclusionModelComplete,
            shallowExclusionModelComplete =
                proof.shallowExclusionModelComplete
        ))
        true
    }

    fun update(id: String, status: WorkshopStatus, notes: String): Boolean =
        synchronized(lock) {
            val old = load(id) ?: return@synchronized false
            persist(old.copy(status = status, notes = notes.take(4000)))
            true
        }

    fun load(id: String): WorkshopEntry? = synchronized(lock) {
        if (!id.matches(Regex("[a-f0-9]{64}"))) return@synchronized null
        try {
            val f = path(id)
            if (!f.exists()) return@synchronized null
            AtomicFile(f).openRead().bufferedReader(Charsets.UTF_8).use {
                val item = fromJson(JSONObject(it.readText()))
                if (item.id == id) item else null
            }
        } catch (_: Exception) { null }
    }

    fun forInfernal(size: Int): List<WorkshopEntry> =
        list().filter { it.puzzle.size == size && it.eligibleForInfernal }

    fun exportZip(output: OutputStream) {
        // Solutions are included for independent audit. Do NOT publish publicly.
        val snapshot = list()
        ZipOutputStream(output.buffered()).use { zip ->
            val manifest = JSONObject().apply {
                put("schema", FORMAT)
                put("kind", "geckodoku-classic-infernal-workshop")
                put("exportedAt", System.currentTimeMillis())
                put("count", snapshot.size)
                put("containsSolutions", true)
                put("warning", "Private archive: solutions and proof traces")
            }
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(manifest.toString(2).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            snapshot.forEach { item ->
                zip.putNextEntry(ZipEntry("grids/" + item.id + ".json"))
                zip.write(toJson(item).toString(2).toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
    }

    fun importZip(input: InputStream): Int {
        var imported = 0
        var entries = 0
        ZipInputStream(input.buffered()).use { zip ->
            while (true) {
                val e = zip.nextEntry ?: break
                entries++
                require(entries <= MAX_IMPORT_ENTRIES) { "Trop de fichiers dans l'archive." }
                if (!e.isDirectory &&
                    e.name.matches(Regex("grids/[a-f0-9]{64}\\.json"))
                ) {
                    val data = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var bytes = 0
                    while (true) {
                        val n = zip.read(buffer)
                        if (n < 0) break
                        bytes += n
                        require(bytes <= MAX_RECORD_BYTES) {
                            "Fichier de grille trop volumineux."
                        }
                        data.write(buffer, 0, n)
                    }
                    val record = fromJson(
                        JSONObject(data.toString(Charsets.UTF_8.name()))
                    )
                    // Imported statuses are untrusted: ALWAYS review again.
                    synchronized(lock) {
                        if (!path(record.id).exists() &&
                            PuzzleGenerator.hasExactlyOneSolution(record.puzzle)
                        ) {
                            persist(record.copy(
                                status = WorkshopStatus.CANDIDATE,
                                notes = "Import à revérifier : " + record.notes.take(3900)
                            ))
                            imported++
                        }
                    }
                }
                zip.closeEntry()
            }
        }
        return imported
    }

    private fun path(id: String): File = File(directory, "$id.json")

    private fun persist(entry: WorkshopEntry) {
        val file = AtomicFile(path(entry.id))
        val stream = file.startWrite()
        try {
            stream.write(toJson(entry).toString().toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
        } catch (e: Exception) {
            file.failWrite(stream)
            throw e
        }
    }

    private fun toJson(entry: WorkshopEntry): JSONObject {
        val puzzle = entry.puzzle
        return JSONObject().apply {
            put("schema", FORMAT)
            put("id", entry.id)
            put("createdAt", entry.createdAt)
            put("status", entry.status.name)
            put("notes", entry.notes)
            put("xWingCount", entry.xWingCount)
            put("xWingExclusionModelComplete", entry.xWingExclusionModelComplete)
            put("shallowExclusionModelComplete", entry.shallowExclusionModelComplete)
            put("nestedDepth", entry.nestedDepth)
            put("hypothesisCount", entry.hypothesisCount)
            put("size", puzzle.size)
            put("seed", puzzle.seed)
            put("regions", ints(puzzle.regions))
            put("solutionCols", ints(puzzle.solutionCols))
            put("givens", cells(puzzle.givens))
            put("trace", JSONArray().also { array ->
                puzzle.solverTrace.forEach { step ->
                    array.put(JSONObject().apply {
                        put("technique", step.technique.name)
                        put("cell", step.cell?.let { cell(it) })
                        put("eliminated", cells(step.eliminated))
                        put("sourceCells", cells(step.sourceCells))
                        put("sourceRegions", JSONArray(step.sourceRegions.sorted()))
                        put("axis", step.axis)
                        put("beforeConfirmed", cells(step.beforeConfirmed))
                        put("beforeExcluded", cells(step.beforeExcluded))
                        put("hypothesisRejected", step.hypothesisRejected?.let { cell(it) })
                        put("hypothesisDepth", step.hypothesisDepth)
                    })
                }
            })
        }
    }

    private fun fromJson(obj: JSONObject): WorkshopEntry {
        require(obj.getInt("schema") == FORMAT)
        val size = obj.getInt("size")
        require(size in 5..12)
        val regions = parseInts(obj.getJSONArray("regions"))
        val cols = parseInts(obj.getJSONArray("solutionCols"))
        require(regions.size == size * size && cols.size == size)
        val givens = parseCells(obj.getJSONArray("givens"), size)
        val trace = mutableListOf<SolveStep>()
        val steps = obj.optJSONArray("trace") ?: JSONArray()
        require(steps.length() <= 2000)
        for (i in 0 until steps.length()) {
            val s = steps.getJSONObject(i)
            val name = s.getString("technique")
            val technique = SolveTechnique.entries.first { it.name == name }
            trace.add(SolveStep(
                technique = technique,
                cell = parseCell(s.optJSONObject("cell"), size),
                eliminated = parseCells(s.optJSONArray("eliminated") ?: JSONArray(), size),
                sourceCells = parseCells(s.optJSONArray("sourceCells") ?: JSONArray(), size),
                sourceRegions = parseInts(s.optJSONArray("sourceRegions") ?: JSONArray()).toSet(),
                axis = s.optString("axis", ""),
                beforeConfirmed = parseCells(s.optJSONArray("beforeConfirmed") ?: JSONArray(), size),
                beforeExcluded = parseCells(s.optJSONArray("beforeExcluded") ?: JSONArray(), size),
                hypothesisRejected = parseCell(s.optJSONObject("hypothesisRejected"), size),
                hypothesisDepth = s.optInt("hypothesisDepth", 0)
            ))
        }
        val puzzle = Puzzle(
            id = "workshop-" + obj.getString("id"),
            size = size, regions = regions, solutionCols = cols,
            givens = givens, difficulty = GameDifficulty.INFERNAL,
            seed = obj.getLong("seed"), solverTrace = trace
        )
        val id = obj.getString("id")
        require(fingerprint(puzzle) == id)
        val wingCount = obj.getInt("xWingCount")
        val depth = obj.getInt("nestedDepth")
        require(InfernalWorkshopPolicy.qualifies(size, depth, wingCount))
        require(trace.count {
            it.technique == SolveTechnique.GECKO_X_WING
        } == wingCount)
        require(trace.any { it.hypothesisDepth >= 2 })
        return WorkshopEntry(
            id = id, puzzle = puzzle,
            xWingCount = wingCount, nestedDepth = depth,
            hypothesisCount = obj.getInt("hypothesisCount"),
            createdAt = obj.getLong("createdAt"),
            status = WorkshopStatus.entries.firstOrNull {
                it.name == obj.optString("status")
            } ?: WorkshopStatus.CANDIDATE,
            notes = obj.optString("notes", "").take(4000),
            xWingExclusionModelComplete =
                obj.optBoolean("xWingExclusionModelComplete", false),
            shallowExclusionModelComplete =
                obj.optBoolean("shallowExclusionModelComplete", false)
        )
    }

    private fun fingerprint(puzzle: Puzzle): String {
        val key = puzzle.size.toString() + "|" +
            puzzle.regions.joinToString(",") + "|" +
            puzzle.givens.map { it.index(puzzle.size) }.sorted().joinToString(",")
        return MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun ints(a: IntArray): JSONArray =
        JSONArray().also { j -> a.forEach { j.put(it) } }

    private fun cells(a: Set<Cell>): JSONArray =
        JSONArray().also { j ->
            a.sortedWith(compareBy<Cell> { it.row }.thenBy { it.col })
                .forEach { j.put(cell(it)) }
        }

    private fun cell(c: Cell): JSONObject = JSONObject().apply {
        put("row", c.row)
        put("col", c.col)
    }

    private fun parseInts(j: JSONArray): IntArray =
        IntArray(j.length()) { j.getInt(it) }

    private fun parseCell(j: JSONObject?, n: Int): Cell? {
        if (j == null) return null
        val row = j.getInt("row")
        val col = j.getInt("col")
        require(row in 0 until n && col in 0 until n)
        return Cell(row, col)
    }

    private fun parseCells(j: JSONArray, n: Int): Set<Cell> =
        (0 until j.length()).map {
            parseCell(j.getJSONObject(it), n)!!
        }.toSet()
}

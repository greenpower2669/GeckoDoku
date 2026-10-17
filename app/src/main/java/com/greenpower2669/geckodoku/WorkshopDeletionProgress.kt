package com.greenpower2669.geckodoku

/**
 * Durable boundary between Gecko removal attempts. A canceled proof must NOT
 * advance nextRemoval: the exact pending puzzle is retried on resume.
 */
data class WorkshopDeletionProgress(
    val size: Int,
    val seed: Long,
    val attempt: Int,
    val nextRemoval: Int,
    val kept: Set<Cell>
) {
    fun encode(): String = listOf(
        "1", size, seed, attempt, nextRemoval,
        kept.map { it.index(size) }.sorted().joinToString(",").ifEmpty { "-" }
    ).joinToString("|")

    companion object {
        fun start(
            size: Int,
            seed: Long,
            attempt: Int,
            solution: Set<Cell>
        ): WorkshopDeletionProgress {
            require(size in InfernalWorkshopPolicy.sizes)
            require(attempt >= 0)
            require(solution.size == size)
            require(solution.all { it.row in 0 until size && it.col in 0 until size })
            return WorkshopDeletionProgress(size, seed, attempt, 0, solution)
        }

        fun afterTest(
            before: WorkshopDeletionProgress,
            removed: Cell,
            unique: Boolean
        ): WorkshopDeletionProgress {
            require(before.nextRemoval < before.size)
            require(removed in before.kept)
            return before.copy(
                nextRemoval = before.nextRemoval + 1,
                kept = if (unique) before.kept - removed else before.kept
            )
        }

        fun decode(encoded: String): WorkshopDeletionProgress? = try {
            val fields = encoded.split("|")
            require(fields.size == 6 && fields[0] == "1")
            val size = fields[1].toInt()
            require(size in InfernalWorkshopPolicy.sizes)
            val seed = fields[2].toLong()
            val attempt = fields[3].toInt()
            val offset = fields[4].toInt()
            require(attempt in 0..12 && offset in 0..size)
            val indexes = if (fields[5] == "-") emptyList() else {
                fields[5].split(",").map { it.toInt() }
            }
            require(indexes.distinct().size == indexes.size)
            require(indexes.all { it in 0 until size * size })
            val kept = indexes.map { Cell(it / size, it % size) }.toSet()
            require(kept.size <= size)
            WorkshopDeletionProgress(size, seed, attempt, offset, kept)
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: IndexOutOfBoundsException) {
            null
        }
    }
}

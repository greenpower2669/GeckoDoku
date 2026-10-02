package com.greenpower2669.geckodoku

object PierreSmallTalk {
    val lines: List<String>
        get() =
            ProfessorPhraseCatalog
                .legacy
                .map { it.text }
}

class PierreSmallTalkSelector {
    fun chooseIndex(
        randomValue: Int,
        previousIndex: Int?
    ): Int {
        val size = PierreSmallTalk.lines.size
        require(size > 0)

        var candidate =
            Math.floorMod(
                randomValue,
                size
            )

        if (
            size > 1 &&
            candidate == previousIndex
        ) {
            candidate =
                (candidate + 1) % size
        }

        return candidate
    }
}

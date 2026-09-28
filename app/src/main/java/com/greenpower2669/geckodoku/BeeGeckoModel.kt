package com.greenpower2669.geckodoku

import kotlin.math.max
import kotlin.random.Random

data class HexCoord(
    val q: Int,
    val r: Int
) {
    fun neighbors(): List<HexCoord> =
        HEX_DIRECTIONS.map {
            (dq, dr) ->
            HexCoord(q + dq, r + dr)
        }

    companion object {
        private val HEX_DIRECTIONS =
            listOf(
                1 to 0,
                1 to -1,
                0 to -1,
                -1 to 0,
                -1 to 1,
                0 to 1
            )
    }
}

enum class BeeGeckoPiece {
    GECKO,
    BEE
}

data class BeeGeckoPair(
    val gecko: HexCoord,
    val bee: HexCoord
)

data class BeeGeckoPuzzle(
    val id: String,
    val columns: Int,
    val rows: Int,
    val pieces: Map<HexCoord, BeeGeckoPiece>,
    val difficulty: GameDifficulty
) {
    val geckos: Set<HexCoord>
        get() =
            pieces
                .filterValues {
                    it == BeeGeckoPiece.GECKO
                }
                .keys

    val bees: Set<HexCoord>
        get() =
            pieces
                .filterValues {
                    it == BeeGeckoPiece.BEE
                }
                .keys

    fun contains(
        cell: HexCoord
    ): Boolean =
        cell.q in 0 until columns &&
            cell.r in 0 until rows

    fun pieceAt(
        cell: HexCoord
    ): BeeGeckoPiece? =
        pieces[cell]
}

data class BeeGeckoSnapshot(
    val puzzle: BeeGeckoPuzzle,
    val pairs: Set<BeeGeckoPair>,
    val selected: HexCoord?
) {
    val complete: Boolean
        get() =
            BeeGeckoRules.isComplete(
                puzzle,
                pairs
            )

    fun pairFor(
        cell: HexCoord
    ): BeeGeckoPair? =
        pairs.firstOrNull {
            it.gecko == cell ||
                it.bee == cell
        }
}

enum class BeeGeckoTapResult {
    SELECTED,
    DESELECTED,
    PAIRED,
    UNPAIRED,
    RESERVED,
    NOT_NEIGHBORS,
    EMPTY_CELL,
    COMPLETED
}

object BeeGeckoRules {
    fun getHexNeighbors(
        cell: HexCoord,
        puzzle: BeeGeckoPuzzle
    ): List<HexCoord> =
        cell.neighbors()
            .filter {
                puzzle.contains(it)
            }

    fun areNeighbors(
        first: HexCoord,
        second: HexCoord
    ): Boolean =
        second in first.neighbors()

    fun candidateOpposites(
        puzzle: BeeGeckoPuzzle,
        cell: HexCoord
    ): List<HexCoord> {
        val piece =
            puzzle.pieceAt(cell)
                ?: return emptyList()

        val wanted =
            if (
                piece ==
                    BeeGeckoPiece.GECKO
            ) {
                BeeGeckoPiece.BEE
            } else {
                BeeGeckoPiece.GECKO
            }

        return getHexNeighbors(
            cell,
            puzzle
        )
            .filter {
                puzzle.pieceAt(it) ==
                    wanted
            }
            .sortedWith(
                compareBy<HexCoord> {
                    it.r
                }.thenBy {
                    it.q
                }
            )
    }

    fun normalizePair(
        puzzle: BeeGeckoPuzzle,
        first: HexCoord,
        second: HexCoord
    ): BeeGeckoPair? {
        if (
            !areNeighbors(
                first,
                second
            )
        ) {
            return null
        }

        return when {
            puzzle.pieceAt(first) ==
                BeeGeckoPiece.GECKO &&
                puzzle.pieceAt(second) ==
                    BeeGeckoPiece.BEE ->
                BeeGeckoPair(
                    gecko = first,
                    bee = second
                )

            puzzle.pieceAt(first) ==
                BeeGeckoPiece.BEE &&
                puzzle.pieceAt(second) ==
                    BeeGeckoPiece.GECKO ->
                BeeGeckoPair(
                    gecko = second,
                    bee = first
                )

            else -> null
        }
    }

    fun validatePairs(
        puzzle: BeeGeckoPuzzle,
        pairs: Set<BeeGeckoPair>
    ): Boolean {
        val usedGeckos =
            linkedSetOf<HexCoord>()
        val usedBees =
            linkedSetOf<HexCoord>()

        for (pair in pairs) {
            if (
                puzzle.pieceAt(
                    pair.gecko
                ) !=
                BeeGeckoPiece.GECKO ||
                puzzle.pieceAt(
                    pair.bee
                ) !=
                BeeGeckoPiece.BEE ||
                !areNeighbors(
                    pair.gecko,
                    pair.bee
                ) ||
                !usedGeckos.add(
                    pair.gecko
                ) ||
                !usedBees.add(
                    pair.bee
                )
            ) {
                return false
            }
        }

        return true
    }

    fun isComplete(
        puzzle: BeeGeckoPuzzle,
        pairs: Set<BeeGeckoPair>
    ): Boolean =
        validatePairs(
            puzzle,
            pairs
        ) &&
            pairs.size ==
                puzzle.geckos.size &&
            pairs.size ==
                puzzle.bees.size &&
            puzzle.geckos.all {
                gecko ->
                pairs.count {
                    it.gecko == gecko
                } == 1
            } &&
            puzzle.bees.all {
                bee ->
                pairs.count {
                    it.bee == bee
                } == 1
            }
}

class BeeGeckoGameEngine(
    private val puzzle: BeeGeckoPuzzle,
    initialPairs:
        Set<BeeGeckoPair> =
        emptySet(),
    initialSelected:
        HexCoord? = null
) {
    private val pairs =
        linkedSetOf<BeeGeckoPair>()
            .apply {
                if (
                    BeeGeckoRules
                        .validatePairs(
                            puzzle,
                            initialPairs
                        )
                ) {
                    addAll(initialPairs)
                }
            }

    private var selected:
        HexCoord? =
        initialSelected
            ?.takeIf {
                puzzle.pieceAt(it) !=
                    null
            }

    fun snapshot():
        BeeGeckoSnapshot =
        BeeGeckoSnapshot(
            puzzle = puzzle,
            pairs = pairs.toSet(),
            selected = selected
        )

    fun tap(
        cell: HexCoord
    ): BeeGeckoTapResult {
        val piece =
            puzzle.pieceAt(cell)

        if (piece == null) {
            selected = null
            return BeeGeckoTapResult
                .EMPTY_CELL
        }

        val current =
            selected

        if (current == null) {
            selected = cell
            return BeeGeckoTapResult
                .SELECTED
        }

        if (current == cell) {
            selected = null
            return BeeGeckoTapResult
                .DESELECTED
        }

        val pair =
            BeeGeckoRules
                .normalizePair(
                    puzzle,
                    current,
                    cell
                )

        if (pair == null) {
            selected = cell

            return if (
                BeeGeckoRules
                    .areNeighbors(
                        current,
                        cell
                    )
            ) {
                BeeGeckoTapResult
                    .SELECTED
            } else {
                BeeGeckoTapResult
                    .NOT_NEIGHBORS
            }
        }

        val currentPair =
            pairs.firstOrNull {
                it.gecko ==
                    pair.gecko ||
                    it.bee ==
                        pair.bee
            }

        if (currentPair != null) {
            if (currentPair == pair) {
                pairs.remove(
                    currentPair
                )
                selected = null
                return BeeGeckoTapResult
                    .UNPAIRED
            }

            selected = cell
            return BeeGeckoTapResult
                .RESERVED
        }

        pairs.add(pair)
        selected = null

        return if (
            BeeGeckoRules.isComplete(
                puzzle,
                pairs
            )
        ) {
            BeeGeckoTapResult
                .COMPLETED
        } else {
            BeeGeckoTapResult
                .PAIRED
        }
    }

    fun applyPair(
        pair: BeeGeckoPair
    ): BeeGeckoTapResult {
        if (
            BeeGeckoRules
                .normalizePair(
                    puzzle,
                    pair.gecko,
                    pair.bee
                ) !=
                pair
        ) {
            return BeeGeckoTapResult
                .NOT_NEIGHBORS
        }

        if (
            pairs.any {
                it.gecko ==
                    pair.gecko ||
                    it.bee ==
                        pair.bee
            }
        ) {
            return BeeGeckoTapResult
                .RESERVED
        }

        pairs.add(pair)
        selected = null

        return if (
            BeeGeckoRules.isComplete(
                puzzle,
                pairs
            )
        ) {
            BeeGeckoTapResult
                .COMPLETED
        } else {
            BeeGeckoTapResult
                .PAIRED
        }
    }
}

data class BeeGeckoHint(
    val source: HexCoord,
    val target: HexCoord,
    val candidates: List<HexCoord>,
    val excluded: List<HexCoord>,
    val reserved: List<HexCoord>,
    val message: String,
    val pair: BeeGeckoPair
)

object BeeGeckoSolver {
    fun nextHint(
        snapshot: BeeGeckoSnapshot
    ): BeeGeckoHint? {
        if (snapshot.complete) {
            return null
        }

        val puzzle =
            snapshot.puzzle

        val used =
            snapshot.pairs
                .flatMap {
                    listOf(
                        it.gecko,
                        it.bee
                    )
                }
                .toSet()

        val pieces =
            puzzle.pieces.keys
                .filter {
                    it !in used
                }
                .sortedWith(
                    compareBy<HexCoord> {
                        it.r
                    }.thenBy {
                        it.q
                    }
                )

        for (source in pieces) {
            val allCandidates =
                BeeGeckoRules
                    .candidateOpposites(
                        puzzle,
                        source
                    )

            val available =
                allCandidates.filter {
                    it !in used
                }

            if (available.size == 1) {
                return buildHint(
                    snapshot =
                        snapshot,
                    source =
                        source,
                    target =
                        available.single(),
                    candidates =
                        available,
                    branchExplanation =
                        null
                )
            }
        }

        for (source in pieces) {
            val available =
                BeeGeckoRules
                    .candidateOpposites(
                        puzzle,
                        source
                    )
                    .filter {
                        it !in used
                    }

            if (available.size <= 1) {
                continue
            }

            val viable =
                available.filter {
                    candidate ->
                    val pair =
                        BeeGeckoRules
                            .normalizePair(
                                puzzle,
                                source,
                                candidate
                            )
                            ?: return@filter false

                    countSolutions(
                        puzzle,
                        snapshot.pairs +
                            pair,
                        limit = 1
                    ) > 0
                }

            if (viable.size == 1) {
                val target =
                    viable.single()

                return buildHint(
                    snapshot =
                        snapshot,
                    source =
                        source,
                    target =
                        target,
                    candidates =
                        available,
                    branchExplanation =
                        "J'ai projeté les autres associations : elles conduisent à une pièce sans partenaire disponible."
                )
            }
        }

        val solution =
            solveOne(
                puzzle,
                snapshot.pairs
            )
                ?: return null

        val nextPair =
            solution.firstOrNull {
                it !in snapshot.pairs
            }
                ?: return null

        return buildHint(
            snapshot =
                snapshot,
            source =
                nextPair.gecko,
            target =
                nextPair.bee,
            candidates =
                BeeGeckoRules
                    .candidateOpposites(
                        puzzle,
                        nextPair.gecko
                    )
                    .filter {
                        it !in used
                    },
            branchExplanation =
                "Je projette les associations restantes : cette liaison appartient à la seule solution complète encore possible."
        )
    }

    fun countSolutions(
        puzzle: BeeGeckoPuzzle,
        forcedPairs:
            Set<BeeGeckoPair> =
            emptySet(),
        limit: Int = 2
    ): Int {
        if (
            !BeeGeckoRules
                .validatePairs(
                    puzzle,
                    forcedPairs
                )
        ) {
            return 0
        }

        val usedGeckos =
            forcedPairs
                .mapTo(
                    linkedSetOf()
                ) {
                    it.gecko
                }

        val usedBees =
            forcedPairs
                .mapTo(
                    linkedSetOf()
                ) {
                    it.bee
                }

        var count = 0

        fun search() {
            if (count >= limit) {
                return
            }

            if (
                usedGeckos.size ==
                    puzzle.geckos.size
            ) {
                if (
                    usedBees.size ==
                        puzzle.bees.size
                ) {
                    count += 1
                }
                return
            }

            val next =
                puzzle.geckos
                    .asSequence()
                    .filter {
                        it !in usedGeckos
                    }
                    .map {
                        gecko ->
                        gecko to
                            BeeGeckoRules
                                .candidateOpposites(
                                    puzzle,
                                    gecko
                                )
                                .filter {
                                    it !in usedBees
                                }
                    }
                    .minByOrNull {
                        it.second.size
                    }
                    ?: return

            if (next.second.isEmpty()) {
                return
            }

            val gecko =
                next.first

            usedGeckos.add(gecko)

            for (bee in next.second) {
                usedBees.add(bee)
                search()
                usedBees.remove(bee)

                if (count >= limit) {
                    break
                }
            }

            usedGeckos.remove(gecko)
        }

        search()
        return count
    }

    fun solveOne(
        puzzle: BeeGeckoPuzzle,
        forcedPairs:
            Set<BeeGeckoPair> =
            emptySet()
    ): Set<BeeGeckoPair>? {
        if (
            !BeeGeckoRules
                .validatePairs(
                    puzzle,
                    forcedPairs
                )
        ) {
            return null
        }

        val result =
            linkedSetOf<BeeGeckoPair>()
                .apply {
                    addAll(
                        forcedPairs
                    )
                }

        val usedGeckos =
            result
                .mapTo(
                    linkedSetOf()
                ) {
                    it.gecko
                }

        val usedBees =
            result
                .mapTo(
                    linkedSetOf()
                ) {
                    it.bee
                }

        fun search(): Boolean {
            if (
                usedGeckos.size ==
                    puzzle.geckos.size
            ) {
                return usedBees.size ==
                    puzzle.bees.size
            }

            val next =
                puzzle.geckos
                    .asSequence()
                    .filter {
                        it !in usedGeckos
                    }
                    .map {
                        gecko ->
                        gecko to
                            BeeGeckoRules
                                .candidateOpposites(
                                    puzzle,
                                    gecko
                                )
                                .filter {
                                    it !in usedBees
                                }
                    }
                    .minByOrNull {
                        it.second.size
                    }
                    ?: return false

            val gecko =
                next.first

            if (next.second.isEmpty()) {
                return false
            }

            usedGeckos.add(gecko)

            for (bee in next.second) {
                val pair =
                    BeeGeckoPair(
                        gecko,
                        bee
                    )

                usedBees.add(bee)
                result.add(pair)

                if (search()) {
                    return true
                }

                result.remove(pair)
                usedBees.remove(bee)
            }

            usedGeckos.remove(gecko)
            return false
        }

        return if (search()) {
            result.toSet()
        } else {
            null
        }
    }

    private fun buildHint(
        snapshot: BeeGeckoSnapshot,
        source: HexCoord,
        target: HexCoord,
        candidates: List<HexCoord>,
        branchExplanation: String?
    ): BeeGeckoHint {
        val puzzle =
            snapshot.puzzle

        val allNeighbors =
            BeeGeckoRules
                .getHexNeighbors(
                    source,
                    puzzle
                )

        val pairedCells =
            snapshot.pairs
                .flatMap {
                    listOf(
                        it.gecko,
                        it.bee
                    )
                }
                .toSet()

        val allOpposites =
            BeeGeckoRules
                .candidateOpposites(
                    puzzle,
                    source
                )

        val reserved =
            allOpposites.filter {
                it in pairedCells
            }

        val excluded =
            allNeighbors.filter {
                it !in candidates &&
                    it !in reserved
            }

        val sourceLabel =
            if (
                puzzle.pieceAt(source) ==
                    BeeGeckoPiece.GECKO
            ) {
                "Ce Gecko"
            } else {
                "Cette Abeille"
            }

        val targetLabel =
            if (
                puzzle.pieceAt(target) ==
                    BeeGeckoPiece.BEE
            ) {
                "Abeille"
            } else {
                "Gecko"
            }

        val message =
            buildString {
                append(sourceLabel)
                append(
                    " doit appartenir à exactement un couple. "
                )
                append(
                    "Je regarde ses six directions hexagonales : "
                )
                append(
                    candidates.size
                )
                append(
                    if (
                        candidates.size ==
                            1
                    ) {
                        " partenaire encore disponible"
                    } else {
                        " partenaires encore possibles"
                    }
                )
                append(".")

                if (reserved.isNotEmpty()) {
                    append(
                        " "
                    )
                    append(
                        reserved.size
                    )
                    append(
                        " possibilité(s) sont déjà réservées par un autre couple."
                    )
                }

                if (excluded.isNotEmpty()) {
                    append(
                        " "
                    )
                    append(
                        excluded.size
                    )
                    append(
                        " direction(s) ne contiennent pas de partenaire compatible."
                    )
                }

                if (
                    branchExplanation !=
                        null
                ) {
                    append(
                        " "
                    )
                    append(
                        branchExplanation
                    )
                }

                append(
                    " Donc le couple forcé relie "
                )
                append(
                    sourceLabel
                        .lowercase()
                )
                append(
                    if (
                        targetLabel ==
                            "Abeille"
                    ) {
                        " à l'abeille mise en évidence."
                    } else {
                        " au Gecko mis en évidence."
                    }
                )
            }

        val pair =
            requireNotNull(
                BeeGeckoRules
                    .normalizePair(
                        puzzle,
                        source,
                        target
                    )
            )

        return BeeGeckoHint(
            source = source,
            target = target,
            candidates =
                candidates,
            excluded = excluded,
            reserved = reserved,
            message = message,
            pair = pair
        )
    }
}

data class BeeGeckoDifficultyProfile(
    val columns: Int,
    val rows: Int,
    val pairCount: Int,
    val maxChainPairs: Int
)

object BeeGeckoGenerator {
    fun profile(
        difficulty:
            GameDifficulty
    ): BeeGeckoDifficultyProfile =
        when (difficulty) {
            GameDifficulty.DISCOVERY ->
                BeeGeckoDifficultyProfile(
                    columns = 10,
                    rows = 8,
                    pairCount = 4,
                    maxChainPairs = 4
                )

            GameDifficulty.EASY ->
                BeeGeckoDifficultyProfile(
                    columns = 12,
                    rows = 9,
                    pairCount = 7,
                    maxChainPairs = 6
                )

            GameDifficulty.THINKING ->
                BeeGeckoDifficultyProfile(
                    columns = 14,
                    rows = 11,
                    pairCount = 12,
                    maxChainPairs = 7
                )

            GameDifficulty.HARD ->
                BeeGeckoDifficultyProfile(
                    columns = 16,
                    rows = 12,
                    pairCount = 18,
                    maxChainPairs = 8
                )

            GameDifficulty.EXPERT ->
                BeeGeckoDifficultyProfile(
                    columns = 18,
                    rows = 14,
                    pairCount = 24,
                    maxChainPairs = 9
                )

            GameDifficulty.DEMENTIAL ->
                BeeGeckoDifficultyProfile(
                    columns = 20,
                    rows = 15,
                    pairCount = 30,
                    maxChainPairs = 10
                )

            GameDifficulty.MISSION_IMPOSSIBLE ->
                BeeGeckoDifficultyProfile(
                    columns = 22,
                    rows = 17,
                    pairCount = 36,
                    maxChainPairs = 11
                )

            GameDifficulty.INFERNAL ->
                BeeGeckoDifficultyProfile(
                    columns = 24,
                    rows = 18,
                    pairCount = 42,
                    maxChainPairs = 11
                )
        }

    fun generate(
        difficulty:
            GameDifficulty,
        seed: Int =
            Random.nextInt()
    ): BeeGeckoPuzzle {
        val profile =
            profile(difficulty)

        val pieces =
            linkedMapOf<
                HexCoord,
                BeeGeckoPiece
                >()

        var remaining =
            profile.pairCount

        var row = 1
        var chainIndex = 0

        while (
            remaining > 0 &&
            row <
                profile.rows - 1
        ) {
            val capacity =
                max(
                    1,
                    minOf(
                        profile.maxChainPairs,
                        (profile.columns - 2) /
                            2,
                        remaining
                    )
                )

            val count =
                capacity

            val reverse =
                (
                    seed +
                        chainIndex *
                            31
                    ).and(1) != 0

            val startQ =
                if (reverse) {
                    profile.columns -
                        2 * count
                } else {
                    1
                }

            for (
                index in
                0 until count
            ) {
                val baseQ =
                    startQ +
                        index * 2

                val geckoQ =
                    if (reverse) {
                        baseQ + 1
                    } else {
                        baseQ
                    }

                val beeQ =
                    if (reverse) {
                        baseQ
                    } else {
                        baseQ + 1
                    }

                pieces[
                    HexCoord(
                        geckoQ,
                        row
                    )
                ] =
                    BeeGeckoPiece
                        .GECKO

                pieces[
                    HexCoord(
                        beeQ,
                        row
                    )
                ] =
                    BeeGeckoPiece
                        .BEE
            }

            remaining -= count
            row += 3
            chainIndex += 1
        }

        require(
            remaining == 0
        ) {
            "BeeGecko profile does not fit board."
        }

        val puzzle =
            BeeGeckoPuzzle(
                id =
                    "bee-" +
                        difficulty.name +
                        "-" +
                        seed,
                columns =
                    profile.columns,
                rows =
                    profile.rows,
                pieces =
                    pieces.toMap(),
                difficulty =
                    difficulty
            )

        require(
            puzzle.geckos.size ==
                profile.pairCount &&
                puzzle.bees.size ==
                    profile.pairCount
        )

        require(
            BeeGeckoSolver
                .countSolutions(
                    puzzle,
                    limit = 2
                ) == 1
        ) {
            "Generated BeeGecko puzzle must have one matching."
        }

        return puzzle
    }
}

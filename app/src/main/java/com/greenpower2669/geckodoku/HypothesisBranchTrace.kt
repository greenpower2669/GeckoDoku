package com.greenpower2669.geckodoku

enum class HypothesisColor(
    val label: String,
    val red: Int,
    val green: Int,
    val blue: Int
) {
    YELLOW("Jaune", 236, 184, 35),
    GREEN("Vert", 46, 166, 84),
    RED("Rouge", 210, 62, 62),
    PURPLE("Violet", 147, 82, 196),
    BLUE("Bleu", 55, 126, 210),
    ORANGE("Orange", 239, 132, 40);

    companion object {
        val palette:
            List<HypothesisColor> =
            listOf(
                YELLOW,
                GREEN,
                RED,
                PURPLE,
                BLUE,
                ORANGE
            )
    }
}

enum class HypothesisBranchState {
    ACTIVE,
    CONTRADICTION
}

enum class HypothesisCrossChange {
    ADDED,
    REMOVED,
    NO_ACTIVE,
    BLOCKED
}

data class HypothesisNode<T>(
    val id: Int,
    val cell: T,
    val parentId: Int?,
    val depth: Int,
    val color: HypothesisColor,
    val state: HypothesisBranchState,
    val order: Int
)

data class HypothesisTimelineEntry(
    val id: Int,
    val parentId: Int?,
    val depth: Int,
    val color: HypothesisColor,
    val state: HypothesisBranchState,
    val order: Int,
    val active: Boolean
)

data class HypothesisTraceSnapshot<T>(
    val nodes:
        List<HypothesisNode<T>> =
        emptyList(),
    val crossOwners:
        Map<T, Int> =
        emptyMap(),
    val activeId:
        Int? = null
) {
    fun nodeAt(
        cell: T
    ): HypothesisNode<T>? =
        nodes.firstOrNull {
            it.cell == cell
        }

    fun nodeById(
        id: Int
    ): HypothesisNode<T>? =
        nodes.firstOrNull {
            it.id == id
        }

    fun colorForHypothesis(
        cell: T
    ): HypothesisColor? =
        nodeAt(cell)
            ?.color

    fun colorForCross(
        cell: T
    ): HypothesisColor? {
        val owner =
            crossOwners[cell]
                ?: return null

        return nodeById(owner)
            ?.color
    }

    val timeline:
        List<HypothesisTimelineEntry>
        get() =
            nodes
                .sortedBy {
                    it.order
                }
                .map {
                    HypothesisTimelineEntry(
                        id = it.id,
                        parentId =
                            it.parentId,
                        depth =
                            it.depth,
                        color =
                            it.color,
                        state =
                            it.state,
                        order =
                            it.order,
                        active =
                            it.id ==
                                activeId
                    )
                }

    companion object {
        fun <T> empty():
            HypothesisTraceSnapshot<T> =
            HypothesisTraceSnapshot()
    }
}

data class HypothesisPrune<T>(
    val hypothesisCells: Set<T>,
    val crossCells: Set<T>,
    val activeId: Int?
)

class HypothesisBranchTrace<T>(
    initial:
        HypothesisTraceSnapshot<T> =
        HypothesisTraceSnapshot.empty()
) {
    private val nodes =
        linkedMapOf<
            Int,
            HypothesisNode<T>
            >()

    private val cellToId =
        linkedMapOf<T, Int>()

    private val crossOwners =
        linkedMapOf<T, Int>()

    private var nextId =
        1

    private var nextOrder =
        1

    var activeId:
        Int? = null
        private set

    init {
        initial.nodes
            .sortedBy {
                it.order
            }
            .forEach {
                node ->

                if (
                    node.id > 0 &&
                    node.id !in nodes &&
                    node.cell !in cellToId
                ) {
                    nodes[node.id] =
                        node

                    cellToId[node.cell] =
                        node.id
                }
            }

        initial.crossOwners
            .forEach {
                (cell, ownerId) ->

                if (
                    ownerId in nodes &&
                    cell !in cellToId
                ) {
                    crossOwners[cell] =
                        ownerId
                }
            }

        activeId =
            initial.activeId
                ?.takeIf {
                    it in nodes
                }

        if (activeId == null) {
            activeId =
                nodes.values
                    .lastOrNull {
                        it.state ==
                            HypothesisBranchState
                                .ACTIVE
                    }
                    ?.id
        }

        nextId =
            (
                nodes.keys
                    .maxOrNull()
                    ?: 0
                ) +
                1

        nextOrder =
            (
                nodes.values
                    .maxOfOrNull {
                        it.order
                    }
                    ?: 0
                ) +
                1
    }

    fun snapshot():
        HypothesisTraceSnapshot<T> =
        HypothesisTraceSnapshot(
            nodes =
                nodes.values
                    .sortedBy {
                        it.order
                    },
            crossOwners =
                crossOwners.toMap(),
            activeId =
                activeId
        )

    fun nodeAt(
        cell: T
    ): HypothesisNode<T>? =
        cellToId[cell]
            ?.let {
                nodes[it]
            }

    fun startHypothesis(
        cell: T
    ): HypothesisNode<T> {
        nodeAt(cell)
            ?.let {
                activeId = it.id
                return it
            }

        val current =
            activeId
                ?.let {
                    nodes[it]
                }

        if (
            current?.state ==
                HypothesisBranchState
                    .CONTRADICTION
        ) {
            removeBranch(
                current.cell
            )
        }

        val parentId =
            activeId
                ?.takeIf {
                    nodes[it]
                        ?.state ==
                        HypothesisBranchState
                            .ACTIVE
                }

        val parent =
            parentId
                ?.let {
                    nodes[it]
                }

        val node =
            HypothesisNode(
                id =
                    nextId++,
                cell =
                    cell,
                parentId =
                    parentId,
                depth =
                    (parent?.depth ?: -1) +
                        1,
                color =
                    allocateColor(),
                state =
                    HypothesisBranchState
                        .ACTIVE,
                order =
                    nextOrder++
            )

        nodes[node.id] =
            node

        cellToId[cell] =
            node.id

        crossOwners.remove(cell)

        activeId =
            node.id

        return node
    }

    fun toggleCross(
        cell: T
    ): HypothesisCrossChange {
        val owner =
            crossOwners[cell]

        if (owner != null) {
            crossOwners.remove(cell)

            return HypothesisCrossChange
                .REMOVED
        }

        val active =
            activeId
                ?.let {
                    nodes[it]
                }
                ?: return HypothesisCrossChange
                    .NO_ACTIVE

        if (
            active.state !=
                HypothesisBranchState
                    .ACTIVE ||
            cell in cellToId
        ) {
            return HypothesisCrossChange
                .BLOCKED
        }

        crossOwners[cell] =
            active.id

        return HypothesisCrossChange
            .ADDED
    }

    fun removeCross(
        cell: T
    ): Boolean =
        crossOwners.remove(cell) !=
            null

    fun markContradiction(
        cell: T
    ): Boolean {
        val id =
            cellToId[cell]
                ?: return false

        val affected =
            branchIds(
                id,
                includeRoot = true
            )

        affected.forEach {
            nodeId ->

            val current =
                nodes[nodeId]
                    ?: return@forEach

            nodes[nodeId] =
                current.copy(
                    state =
                        HypothesisBranchState
                            .CONTRADICTION
                )
        }

        activeId =
            id

        return true
    }

    fun removeBranch(
        cell: T
    ): HypothesisPrune<T> {
        val id =
            cellToId[cell]
                ?: return HypothesisPrune(
                    emptySet(),
                    emptySet(),
                    activeId
                )

        return removeBranchById(id)
    }

    fun rewindTo(
        id: Int
    ): HypothesisPrune<T>? {
        if (id !in nodes) {
            return null
        }

        val descendants =
            branchIds(
                id,
                includeRoot = false
            )

        val hypothesisCells =
            descendants
                .mapNotNull {
                    nodes[it]
                        ?.cell
                }
                .toSet()

        val crossCells =
            crossOwners
                .filterValues {
                    it in descendants
                }
                .keys
                .toSet()

        crossCells.forEach {
            crossOwners.remove(it)
        }

        descendants.forEach {
            childId ->

            nodes.remove(childId)
                ?.let {
                    cellToId.remove(
                        it.cell
                    )
                }
        }

        activeId =
            id

        return HypothesisPrune(
            hypothesisCells =
                hypothesisCells,
            crossCells =
                crossCells,
            activeId =
                activeId
        )
    }

    private fun removeBranchById(
        id: Int
    ): HypothesisPrune<T> {
        val root =
            nodes[id]
                ?: return HypothesisPrune(
                    emptySet(),
                    emptySet(),
                    activeId
                )

        val affected =
            branchIds(
                id,
                includeRoot = true
            )

        val hypothesisCells =
            affected
                .mapNotNull {
                    nodes[it]
                        ?.cell
                }
                .toSet()

        val crossCells =
            crossOwners
                .filterValues {
                    it in affected
                }
                .keys
                .toSet()

        crossCells.forEach {
            crossOwners.remove(it)
        }

        affected.forEach {
            nodeId ->

            nodes.remove(nodeId)
                ?.let {
                    cellToId.remove(
                        it.cell
                    )
                }
        }

        activeId =
            root.parentId
                ?.takeIf {
                    it in nodes
                }

        return HypothesisPrune(
            hypothesisCells =
                hypothesisCells,
            crossCells =
                crossCells,
            activeId =
                activeId
        )
    }

    private fun branchIds(
        rootId: Int,
        includeRoot: Boolean
    ): Set<Int> {
        val result =
            linkedSetOf<Int>()

        if (includeRoot) {
            result.add(rootId)
        }

        var frontier =
            setOf(rootId)

        while (frontier.isNotEmpty()) {
            val children =
                nodes.values
                    .filter {
                        node ->

                        node.parentId
                            ?.let {
                                it in frontier
                            } ==
                            true
                    }
                    .map {
                        it.id
                    }
                    .filter {
                        it !in result
                    }
                    .toSet()

            if (children.isEmpty()) {
                break
            }

            result.addAll(
                children
            )

            frontier =
                children
        }

        return result
    }

    private fun allocateColor():
        HypothesisColor {
        val used =
            nodes.values
                .map {
                    it.color
                }
                .toSet()

        return HypothesisColor
            .palette
            .firstOrNull {
                it !in used
            }
            ?: HypothesisColor
                .palette[
                    (
                        nextOrder -
                            1
                        ).mod(
                        HypothesisColor
                            .palette
                            .size
                    )
                ]
    }
}

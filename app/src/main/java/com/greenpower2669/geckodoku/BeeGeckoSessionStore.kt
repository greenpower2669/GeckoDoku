package com.greenpower2669.geckodoku

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class BeeGeckoSession(
    val puzzle: BeeGeckoPuzzle,
    val confirmedGeckos:
        Set<HexCoord>,
    val confirmedBees:
        Set<HexCoord>,
    val crosses:
        Set<HexCoord>,
    val crossStates:
        Map<
            HexCoord,
            BeeGeckoCrossState
            >,
    val logicalMarkers:
        Map<
            HexCoord,
            BeeGeckoLogicalMarks
            >,
    val markers:
        Map<HexCoord, CustomMarker>,
    val mistakes: Int,
    val camera: BeeGeckoCamera,
    val elapsedSeconds: Long,
    val assistancePoints: Int,
    val hypothesisTrace:
        HypothesisTraceSnapshot<HexCoord> =
        HypothesisTraceSnapshot
            .empty()
)

class BeeGeckoSessionStore(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    fun save(
        session: BeeGeckoSession
    ) {
        val root =
            JSONObject()
                .put(
                    "schema",
                    SCHEMA
                )
                .put(
                    "id",
                    session.puzzle.id
                )
                .put(
                    "radius",
                    session.puzzle.radius
                )
                .put(
                    "difficulty",
                    session.puzzle
                        .difficulty
                        .name
                )
                .put(
                    "seed",
                    session.puzzle.seed
                )
                .put(
                    "elapsedSeconds",
                    session.elapsedSeconds
                )
                .put(
                    "assistancePoints",
                    session.assistancePoints
                )
                .put(
                    "mistakes",
                    session.mistakes
                )
                .put(
                    "camera",
                    JSONObject()
                        .put(
                            "scale",
                            session.camera.scale
                        )
                        .put(
                            "offsetX",
                            session.camera.offsetX
                        )
                        .put(
                            "offsetY",
                            session.camera.offsetY
                        )
                )

        root.put(
            "regions",
            encodeRegions(
                session.puzzle.regions
            )
        )
        root.put(
            "solutionGeckos",
            encodeCells(
                session.puzzle
                    .solutionGeckos
            )
        )
        root.put(
            "solutionBees",
            encodeCells(
                session.puzzle
                    .solutionBees
            )
        )
        root.put(
            "givenGeckos",
            encodeCells(
                session.puzzle
                    .givenGeckos
            )
        )
        root.put(
            "givenBees",
            encodeCells(
                session.puzzle
                    .givenBees
            )
        )
        root.put(
            "solutionPairs",
            encodePairs(
                session.puzzle
                    .solutionPairs
            )
        )
        root.put(
            "confirmedGeckos",
            encodeCells(
                session.confirmedGeckos
            )
        )
        root.put(
            "confirmedBees",
            encodeCells(
                session.confirmedBees
            )
        )
        root.put(
            "crosses",
            encodeCells(
                session.crosses
            )
        )

        root.put(
            "crossStates",
            encodeCrossStates(
                session.crossStates
            )
        )

        root.put(
            "logicalMarkers",
            encodeLogicalMarkers(
                session.logicalMarkers
            )
        )

        root.put(
            "hypothesisTrace",
            encodeHypothesisTrace(
                session.hypothesisTrace
            )
        )

        val markers =
            JSONArray()

        session.markers
            .toSortedMap(
                compareBy<HexCoord> {
                    it.r
                }.thenBy {
                    it.q
                }
            )
            .forEach {
                (cell, marker) ->
                markers.put(
                    JSONObject()
                        .put("q", cell.q)
                        .put("r", cell.r)
                        .put(
                            "marker",
                            marker.name
                        )
                )
            }

        root.put(
            "markers",
            markers
        )

        prefs.edit()
            .putString(
                KEY_SESSION,
                root.toString()
            )
            .apply()
    }

    fun load():
        BeeGeckoSession? {
        val raw =
            prefs.getString(
                KEY_SESSION,
                null
            )
                ?: return null

        return try {
            val root =
                JSONObject(raw)

            val schema =
                root.optInt(
                    "schema",
                    -1
                )

            if (
                schema !in
                    2..SCHEMA
            ) {
                return null
            }

            val regions =
                decodeRegions(
                    root.getJSONArray(
                        "regions"
                    )
                )

            val pairs =
                decodePairs(
                    root.getJSONArray(
                        "solutionPairs"
                    )
                )

            val puzzle =
                BeeGeckoPuzzle(
                    id =
                        root.getString(
                            "id"
                        ),
                    radius =
                        root.getInt(
                            "radius"
                        ),
                    regions =
                        regions,
                    solutionGeckos =
                        decodeCells(
                            root.getJSONArray(
                                "solutionGeckos"
                            )
                        ),
                    solutionBees =
                        decodeCells(
                            root.getJSONArray(
                                "solutionBees"
                            )
                        ),
                    solutionPairs =
                        pairs,
                    givenGeckos =
                        decodeCells(
                            root.getJSONArray(
                                "givenGeckos"
                            )
                        ),
                    givenBees =
                        decodeCells(
                            root.getJSONArray(
                                "givenBees"
                            )
                        ),
                    difficulty =
                        enumValueOf<
                            GameDifficulty
                            >(
                            root.getString(
                                "difficulty"
                            )
                        ),
                    seed =
                        root.optLong(
                            "seed",
                            0L
                        )
                )

            val markers =
                linkedMapOf<
                    HexCoord,
                    CustomMarker
                    >()

            val markerArray =
                root.optJSONArray(
                    "markers"
                )
                    ?: JSONArray()

            for (
                index in
                0 until markerArray.length()
            ) {
                val item =
                    markerArray
                        .getJSONObject(
                            index
                        )

                val cell =
                    HexCoord(
                        item.getInt("q"),
                        item.getInt("r")
                    )

                if (puzzle.contains(cell)) {
                    markers[cell] =
                        enumValueOf<
                            CustomMarker
                            >(
                            item.getString(
                                "marker"
                            )
                        )
                }
            }

            val cameraJson =
                root.optJSONObject(
                    "camera"
                )

            val camera =
                if (cameraJson == null) {
                    BeeGeckoCamera()
                } else {
                    BeeGeckoCamera(
                        scale =
                            cameraJson
                                .optDouble(
                                    "scale",
                                    1.0
                                )
                                .toFloat(),
                        offsetX =
                            cameraJson
                                .optDouble(
                                    "offsetX",
                                    0.0
                                )
                                .toFloat(),
                        offsetY =
                            cameraJson
                                .optDouble(
                                    "offsetY",
                                    0.0
                                )
                                .toFloat()
                    )
                }

            BeeGeckoSession(
                puzzle = puzzle,
                confirmedGeckos =
                    decodeCells(
                        root.optJSONArray(
                            "confirmedGeckos"
                        )
                            ?: JSONArray()
                    ),
                confirmedBees =
                    decodeCells(
                        root.optJSONArray(
                            "confirmedBees"
                        )
                            ?: JSONArray()
                    ),
                crosses =
                    decodeCells(
                        root.optJSONArray(
                            "crosses"
                        )
                            ?: JSONArray()
                    ),
                crossStates =
                    decodeCrossStates(
                        root.optJSONArray(
                            "crossStates"
                        )
                            ?: JSONArray()
                    ),
                logicalMarkers =
                    decodeLogicalMarkers(
                        root.optJSONArray(
                            "logicalMarkers"
                        )
                            ?: JSONArray()
                    ),
                markers =
                    markers.toMap(),
                mistakes =
                    root.optInt(
                        "mistakes",
                        0
                    )
                        .coerceAtLeast(0),
                camera =
                    camera,
                elapsedSeconds =
                    root.optLong(
                        "elapsedSeconds",
                        0L
                    )
                        .coerceAtLeast(0L),
                assistancePoints =
                    root.optInt(
                        "assistancePoints",
                        0
                    )
                        .coerceAtLeast(0),
                hypothesisTrace =
                    decodeHypothesisTrace(
                        root.optJSONObject(
                            "hypothesisTrace"
                        ),
                        puzzle
                    )
            )
        } catch (
            _: Exception
        ) {
            null
        }
    }

    fun clear() {
        prefs.edit()
            .remove(
                KEY_SESSION
            )
            .apply()
    }

    private fun encodeCells(
        cells: Collection<HexCoord>
    ): JSONArray {
        val array =
            JSONArray()

        cells
            .sortedWith(
                compareBy<HexCoord> {
                    it.r
                }.thenBy {
                    it.q
                }
            )
            .forEach {
                array.put(
                    JSONObject()
                        .put("q", it.q)
                        .put("r", it.r)
                )
            }

        return array
    }

    private fun decodeCells(
        source: JSONArray
    ): Set<HexCoord> =
        buildSet {
            for (
                index in
                0 until source.length()
            ) {
                val item =
                    source.getJSONObject(
                        index
                    )

                add(
                    HexCoord(
                        item.getInt("q"),
                        item.getInt("r")
                    )
                )
            }
        }

    private fun encodeCrossStates(
        states:
            Map<
                HexCoord,
                BeeGeckoCrossState
                >
    ): JSONArray {
        val array =
            JSONArray()

        states.forEach {
            (cell, state) ->
            array.put(
                JSONObject()
                    .put("q", cell.q)
                    .put("r", cell.r)
                    .put(
                        "state",
                        state.name
                    )
            )
        }

        return array
    }

    private fun decodeCrossStates(
        source: JSONArray
    ): Map<
        HexCoord,
        BeeGeckoCrossState
        > =
        buildMap {
            for (
                index in
                0 until source.length()
            ) {
                val item =
                    source
                        .getJSONObject(index)

                put(
                    HexCoord(
                        item.getInt("q"),
                        item.getInt("r")
                    ),
                    enumValueOf<
                        BeeGeckoCrossState
                        >(
                        item.getString(
                            "state"
                        )
                    )
                )
            }
        }

    private fun encodeLogicalMarkers(
        markers:
            Map<
                HexCoord,
                BeeGeckoLogicalMarks
                >
    ): JSONArray {
        val array =
            JSONArray()

        markers.forEach {
            (cell, marker) ->
            val axes =
                JSONArray()

            marker.excludedAxes
                .forEach {
                    axis ->
                    axes.put(
                        JSONObject()
                            .put(
                                "axis",
                                axis.name
                            )
                            .put(
                                "color",
                                marker
                                    .colorFor(axis)
                                    .name
                            )
                    )
                }

            array.put(
                JSONObject()
                    .put("q", cell.q)
                    .put("r", cell.r)
                    .put(
                        "gecko",
                        marker
                            .geckoCandidate
                    )
                    .put(
                        "bee",
                        marker
                            .beeCandidate
                    )
                    .put(
                        "axes",
                        axes
                    )
            )
        }

        return array
    }

    private fun decodeLogicalMarkers(
        source: JSONArray
    ): Map<
        HexCoord,
        BeeGeckoLogicalMarks
        > =
        buildMap {
            for (
                index in
                0 until source.length()
            ) {
                val item =
                    source
                        .getJSONObject(index)

                val axesJson =
                    item.optJSONArray(
                        "axes"
                    )
                        ?: JSONArray()

                val axes =
                    linkedSetOf<HexAxis>()

                val axisColors =
                    linkedMapOf<
                        HexAxis,
                        AxisGuideColor
                        >()

                for (
                    axisIndex in
                    0 until axesJson.length()
                ) {
                    val raw =
                        axesJson.get(
                            axisIndex
                        )

                    if (
                        raw is JSONObject
                    ) {
                        val axis =
                            enumValueOf<
                                HexAxis
                                >(
                                raw.getString(
                                    "axis"
                                )
                            )

                        axes.add(axis)

                        axisColors[axis] =
                            runCatching {
                                enumValueOf<
                                    AxisGuideColor
                                    >(
                                    raw.optString(
                                        "color",
                                        AxisGuideColor
                                            .RED
                                            .name
                                    )
                                )
                            }
                                .getOrDefault(
                                    AxisGuideColor
                                        .RED
                                )
                    } else {
                        val axis =
                            enumValueOf<
                                HexAxis
                                >(
                                axesJson
                                    .getString(
                                        axisIndex
                                    )
                            )

                        axes.add(axis)
                        axisColors[axis] =
                            AxisGuideColor.RED
                    }
                }

                val marker =
                    BeeGeckoLogicalMarks(
                        geckoCandidate =
                            item.optBoolean(
                                "gecko",
                                false
                            ),
                        beeCandidate =
                            item.optBoolean(
                                "bee",
                                false
                            ),
                        excludedAxes =
                            axes,
                        axisColors =
                            axisColors
                    )

                if (!marker.isEmpty) {
                    put(
                        HexCoord(
                            item.getInt("q"),
                            item.getInt("r")
                        ),
                        marker
                    )
                }
            }
        }

    private fun encodeHypothesisTrace(
        trace:
            HypothesisTraceSnapshot<
                HexCoord
                >
    ): JSONObject {
        val nodes =
            JSONArray()

        trace.nodes
            .sortedBy {
                it.order
            }
            .forEach {
                node ->

                nodes.put(
                    JSONObject()
                        .put(
                            "id",
                            node.id
                        )
                        .put(
                            "parentId",
                            node.parentId
                                ?: JSONObject.NULL
                        )
                        .put(
                            "q",
                            node.cell.q
                        )
                        .put(
                            "r",
                            node.cell.r
                        )
                        .put(
                            "depth",
                            node.depth
                        )
                        .put(
                            "color",
                            node.color.name
                        )
                        .put(
                            "state",
                            node.state.name
                        )
                        .put(
                            "order",
                            node.order
                        )
                )
            }

        val crosses =
            JSONArray()

        trace.crossOwners
            .forEach {
                (cell, ownerId) ->

                crosses.put(
                    JSONObject()
                        .put(
                            "q",
                            cell.q
                        )
                        .put(
                            "r",
                            cell.r
                        )
                        .put(
                            "ownerId",
                            ownerId
                        )
                )
            }

        return JSONObject()
            .put(
                "activeId",
                trace.activeId
                    ?: JSONObject.NULL
            )
            .put(
                "nodes",
                nodes
            )
            .put(
                "crosses",
                crosses
            )
    }

    private fun decodeHypothesisTrace(
        source: JSONObject?,
        puzzle: BeeGeckoPuzzle
    ): HypothesisTraceSnapshot<
        HexCoord
        > {
        if (source == null) {
            return HypothesisTraceSnapshot
                .empty()
        }

        val nodesJson =
            source.optJSONArray(
                "nodes"
            )
                ?: JSONArray()

        val nodes =
            mutableListOf<
                HypothesisNode<
                    HexCoord
                    >
                >()

        for (
            index in
            0 until nodesJson.length()
        ) {
            val item =
                nodesJson
                    .getJSONObject(
                        index
                    )

            val cell =
                HexCoord(
                    item.getInt("q"),
                    item.getInt("r")
                )

            if (!puzzle.contains(cell)) {
                continue
            }

            nodes.add(
                HypothesisNode(
                    id =
                        item.getInt(
                            "id"
                        ),
                    cell =
                        cell,
                    parentId =
                        if (
                            item.isNull(
                                "parentId"
                            )
                        ) {
                            null
                        } else {
                            item.optInt(
                                "parentId"
                            )
                                .takeIf {
                                    it > 0
                                }
                        },
                    depth =
                        item.optInt(
                            "depth",
                            0
                        ),
                    color =
                        enumValueOf<
                            HypothesisColor
                            >(
                            item.getString(
                                "color"
                            )
                        ),
                    state =
                        enumValueOf<
                            HypothesisBranchState
                            >(
                            item.getString(
                                "state"
                            )
                        ),
                    order =
                        item.optInt(
                            "order",
                            index +
                                1
                        )
                )
            )
        }

        val validIds =
            nodes.map {
                it.id
            }
                .toSet()

        val crossesJson =
            source.optJSONArray(
                "crosses"
            )
                ?: JSONArray()

        val crosses =
            linkedMapOf<
                HexCoord,
                Int
                >()

        for (
            index in
            0 until crossesJson.length()
        ) {
            val item =
                crossesJson
                    .getJSONObject(
                        index
                    )

            val cell =
                HexCoord(
                    item.getInt("q"),
                    item.getInt("r")
                )

            val ownerId =
                item.getInt(
                    "ownerId"
                )

            if (
                puzzle.contains(cell) &&
                ownerId in validIds
            ) {
                crosses[cell] =
                    ownerId
            }
        }

        return HypothesisTraceSnapshot(
            nodes =
                nodes,
            crossOwners =
                crosses,
            activeId =
                if (
                    source.isNull(
                        "activeId"
                    )
                ) {
                    null
                } else {
                    source
                        .optInt(
                            "activeId",
                            -1
                        )
                        .takeIf {
                            it in validIds
                        }
                }
        )
    }

    private fun encodeRegions(
        regions:
            Map<HexCoord, Int>
    ): JSONArray {
        val array =
            JSONArray()

        regions
            .toSortedMap(
                compareBy<HexCoord> {
                    it.r
                }.thenBy {
                    it.q
                }
            )
            .forEach {
                (cell, region) ->
                array.put(
                    JSONObject()
                        .put("q", cell.q)
                        .put("r", cell.r)
                        .put(
                            "region",
                            region
                        )
                )
            }

        return array
    }

    private fun decodeRegions(
        source: JSONArray
    ): Map<HexCoord, Int> =
        buildMap {
            for (
                index in
                0 until source.length()
            ) {
                val item =
                    source.getJSONObject(
                        index
                    )

                put(
                    HexCoord(
                        item.getInt("q"),
                        item.getInt("r")
                    ),
                    item.getInt(
                        "region"
                    )
                )
            }
        }

    private fun encodePairs(
        pairs:
            List<BeeGeckoPair>
    ): JSONArray {
        val array =
            JSONArray()

        pairs
            .sortedBy {
                it.region
            }
            .forEach {
                pair ->
                array.put(
                    JSONObject()
                        .put(
                            "gq",
                            pair.gecko.q
                        )
                        .put(
                            "gr",
                            pair.gecko.r
                        )
                        .put(
                            "bq",
                            pair.bee.q
                        )
                        .put(
                            "br",
                            pair.bee.r
                        )
                        .put(
                            "region",
                            pair.region
                        )
                )
            }

        return array
    }

    private fun decodePairs(
        source: JSONArray
    ): List<BeeGeckoPair> =
        buildList {
            for (
                index in
                0 until source.length()
            ) {
                val item =
                    source.getJSONObject(
                        index
                    )

                add(
                    BeeGeckoPair(
                        gecko =
                            HexCoord(
                                item.getInt("gq"),
                                item.getInt("gr")
                            ),
                        bee =
                            HexCoord(
                                item.getInt("bq"),
                                item.getInt("br")
                            ),
                        region =
                            item.getInt(
                                "region"
                            )
                    )
                )
            }
        }

    companion object {
        const val PREFERENCES_NAME =
            "geckodoku_bee_gecko_session_v2"

        private const val SCHEMA =
            5

        private const val KEY_SESSION =
            "active_session"
    }
}

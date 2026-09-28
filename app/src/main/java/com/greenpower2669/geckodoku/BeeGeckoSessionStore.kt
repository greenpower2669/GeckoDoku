package com.greenpower2669.geckodoku

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class BeeGeckoSession(
    val puzzle: BeeGeckoPuzzle,
    val pairs: Set<BeeGeckoPair>,
    val camera: BeeGeckoCamera,
    val elapsedSeconds: Long,
    val assistancePoints: Int
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
                    1
                )
                .put(
                    "id",
                    session.puzzle.id
                )
                .put(
                    "columns",
                    session.puzzle.columns
                )
                .put(
                    "rows",
                    session.puzzle.rows
                )
                .put(
                    "difficulty",
                    session.puzzle
                        .difficulty
                        .name
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

        val pieces =
            JSONArray()

        session.puzzle.pieces
            .toSortedMap(
                compareBy<HexCoord> {
                    it.r
                }.thenBy {
                    it.q
                }
            )
            .forEach {
                (cell, piece) ->
                pieces.put(
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
                            "piece",
                            piece.name
                        )
                )
            }

        root.put(
            "pieces",
            pieces
        )

        val pairs =
            JSONArray()

        session.pairs.forEach {
            pair ->
            pairs.put(
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
            )
        }

        root.put(
            "pairs",
            pairs
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

            if (
                root.optInt(
                    "schema",
                    -1
                ) != 1
            ) {
                return null
            }

            val pieces =
                linkedMapOf<
                    HexCoord,
                    BeeGeckoPiece
                    >()

            val pieceArray =
                root.getJSONArray(
                    "pieces"
                )

            for (
                i in
                0 until pieceArray.length()
            ) {
                val item =
                    pieceArray
                        .getJSONObject(i)

                val cell =
                    HexCoord(
                        item.getInt("q"),
                        item.getInt("r")
                    )

                pieces[cell] =
                    enumValueOf<
                        BeeGeckoPiece
                        >(
                        item.getString(
                            "piece"
                        )
                    )
            }

            val puzzle =
                BeeGeckoPuzzle(
                    id =
                        root.getString(
                            "id"
                        ),
                    columns =
                        root.getInt(
                            "columns"
                        ),
                    rows =
                        root.getInt(
                            "rows"
                        ),
                    pieces =
                        pieces.toMap(),
                    difficulty =
                        enumValueOf<
                            GameDifficulty
                            >(
                            root.getString(
                                "difficulty"
                            )
                        )
                )

            val pairs =
                linkedSetOf<
                    BeeGeckoPair
                    >()

            val pairArray =
                root.optJSONArray(
                    "pairs"
                )
                    ?: JSONArray()

            for (
                i in
                0 until pairArray.length()
            ) {
                val item =
                    pairArray
                        .getJSONObject(i)

                pairs.add(
                    BeeGeckoPair(
                        gecko =
                            HexCoord(
                                item.getInt(
                                    "gq"
                                ),
                                item.getInt(
                                    "gr"
                                )
                            ),
                        bee =
                            HexCoord(
                                item.getInt(
                                    "bq"
                                ),
                                item.getInt(
                                    "br"
                                )
                            )
                    )
                )
            }

            if (
                !BeeGeckoRules
                    .validatePairs(
                        puzzle,
                        pairs
                    )
            ) {
                return null
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
                pairs = pairs,
                camera = camera,
                elapsedSeconds =
                    root.optLong(
                        "elapsedSeconds",
                        0L
                    )
                        .coerceAtLeast(
                            0L
                        ),
                assistancePoints =
                    root.optInt(
                        "assistancePoints",
                        0
                    )
                        .coerceAtLeast(
                            0
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

    companion object {
        const val PREFERENCES_NAME =
            "geckodoku_bee_gecko_session_v1"

        private const val KEY_SESSION =
            "active_session"
    }
}

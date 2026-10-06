package com.greenpower2669.geckodoku

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import org.json.JSONObject

data class HallHttpRequest(
    val method: String,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)

data class HallHttpResponse(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, String> = emptyMap()
)

interface HallHttpTransport {
    @Throws(IOException::class)
    fun execute(request: HallHttpRequest): HallHttpResponse
}

class UrlConnectionHallHttpTransport(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 15_000
) : HallHttpTransport {
    override fun execute(request: HallHttpRequest): HallHttpResponse {
        val connection = URL(request.url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = request.method
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.doInput = true
            connection.instanceFollowRedirects = true
            request.headers.forEach { (name, value) ->
                connection.setRequestProperty(name, value)
            }
            request.body?.let { body ->
                val bytes = body.toByteArray(StandardCharsets.UTF_8)
                connection.doOutput = true
                connection.setFixedLengthStreamingMode(bytes.size)
                connection.outputStream.use {
                    it.write(bytes)
                    it.flush()
                }
            }

            val status = connection.responseCode
            val stream = if (status >= 400) connection.errorStream else connection.inputStream
            val responseBody = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() } ?: ""
            val headers = buildMap<String, String> {
                connection.headerFields.forEach { (name, values) ->
                    if (name != null && !values.isNullOrEmpty()) {
                        put(name, values.first())
                    }
                }
            }
            return HallHttpResponse(status, responseBody, headers)
        } finally {
            connection.disconnect()
        }
    }
}

sealed interface ScorePostResult {
    data class Accepted(
        val duplicate: Boolean,
        val scoreId: String,
        val runId: String,
        val sequence: Long
    ) : ScorePostResult

    data class Retry(
        val httpStatus: Int?,
        val retryAfterSeconds: Long?,
        val errorCode: String?
    ) : ScorePostResult

    data class Blocked(
        val httpStatus: Int?,
        val errorCode: String
    ) : ScorePostResult
}

data class HallSyncEntry(
    val scoreId: String,
    val runId: String,
    val sequence: Long,
    val receivedAt: Long,
    val normalizedJson: String
)

data class HallSyncPage(
    val entries: List<HallSyncEntry>,
    val nextCursor: String,
    val hasMore: Boolean,
    val highWatermark: String?,
    val serverTime: Long?
)

sealed interface SyncPageResult {
    data class Success(val page: HallSyncPage) : SyncPageResult
    data class Retry(
        val httpStatus: Int?,
        val retryAfterSeconds: Long?,
        val errorCode: String?
    ) : SyncPageResult
    data class Blocked(
        val httpStatus: Int?,
        val errorCode: String
    ) : SyncPageResult
}

class GeckoDokuHallApiClient(
    private val transport: HallHttpTransport,
    private val baseUrl: String = HALL_BASE_URL
) {
    fun postScore(payloadJson: String): ScorePostResult {
        val response = try {
            transport.execute(
                HallHttpRequest(
                    method = "POST",
                    url = endpoint(SCORE_PATH),
                    headers = mapOf(
                        "Content-Type" to "application/json; charset=utf-8"
                    ),
                    body = payloadJson
                )
            )
        } catch (_: IOException) {
            return ScorePostResult.Retry(null, null, "NETWORK_ERROR")
        }

        if (response.statusCode == 200 || response.statusCode == 201) {
            return parseAcceptedResponse(response)
        }

        val errorCode = parseErrorCode(response.body)
        return when (response.statusCode) {
            429 -> ScorePostResult.Retry(
                429,
                retryAfterSeconds(response.headers),
                errorCode ?: "RATE_LIMITED"
            )
            400, 409, 413, 415 -> ScorePostResult.Blocked(
                response.statusCode,
                errorCode ?: "HTTP_${response.statusCode}"
            )
            in 500..599 -> ScorePostResult.Retry(
                response.statusCode,
                null,
                errorCode
            )
            else -> ScorePostResult.Blocked(
                response.statusCode,
                errorCode ?: "HTTP_${response.statusCode}"
            )
        }
    }

    fun fetchSync(cursor: String, limit: Int = 100): SyncPageResult {
        val safeLimit = limit.coerceIn(1, 100)
        val encodedCursor = URLEncoder.encode(
            cursor,
            StandardCharsets.UTF_8.name()
        )
        val response = try {
            transport.execute(
                HallHttpRequest(
                    method = "GET",
                    url = endpoint("$SYNC_PATH?cursor=$encodedCursor&limit=$safeLimit")
                )
            )
        } catch (_: IOException) {
            return SyncPageResult.Retry(null, null, "NETWORK_ERROR")
        }

        if (response.statusCode == 200) {
            return parseSyncResponse(response.body)
        }

        val errorCode = parseErrorCode(response.body)
        return when (response.statusCode) {
            429 -> SyncPageResult.Retry(
                429,
                retryAfterSeconds(response.headers),
                errorCode ?: "RATE_LIMITED"
            )
            in 500..599 -> SyncPageResult.Retry(
                response.statusCode,
                null,
                errorCode
            )
            else -> SyncPageResult.Blocked(
                response.statusCode,
                errorCode ?: "HTTP_${response.statusCode}"
            )
        }
    }

    private fun parseAcceptedResponse(response: HallHttpResponse): ScorePostResult {
        return try {
            val root = JSONObject(response.body)
            if (!root.optBoolean("accepted", false)) {
                ScorePostResult.Blocked(
                    response.statusCode,
                    parseErrorCode(root) ?: "REJECTED_ACK"
                )
            } else {
                val scoreId = root.optString("scoreId")
                val runId = root.optString("runId")
                if (scoreId.isBlank() || runId.isBlank() || !root.has("sequence")) {
                    ScorePostResult.Blocked(
                        response.statusCode,
                        "INVALID_ACK"
                    )
                } else {
                    ScorePostResult.Accepted(
                        duplicate = root.optBoolean("duplicate", false),
                        scoreId = scoreId,
                        runId = runId,
                        sequence = root.getLong("sequence")
                    )
                }
            }
        } catch (_: Exception) {
            ScorePostResult.Blocked(response.statusCode, "INVALID_ACK")
        }
    }

    private fun parseSyncResponse(raw: String): SyncPageResult {
        return try {
            val root = JSONObject(raw)
            val array = root.getJSONArray("entries")
            val entries = buildList {
                for (index in 0 until array.length()) {
                    val obj = array.getJSONObject(index)
                    add(
                        HallSyncEntry(
                            scoreId = obj.getString("scoreId"),
                            runId = obj.getString("runId"),
                            sequence = obj.getLong("sequence"),
                            receivedAt = obj.getLong("receivedAt"),
                            normalizedJson = obj.toString()
                        )
                    )
                }
            }
            SyncPageResult.Success(
                HallSyncPage(
                    entries = entries,
                    nextCursor = root.getString("nextCursor"),
                    hasMore = root.getBoolean("hasMore"),
                    highWatermark = root.optString("highWatermark")
                        .takeIf { it.isNotEmpty() },
                    serverTime = if (root.has("serverTime") && !root.isNull("serverTime")) {
                        root.getLong("serverTime")
                    } else {
                        null
                    }
                )
            )
        } catch (_: Exception) {
            SyncPageResult.Blocked(200, "INVALID_SYNC")
        }
    }

    private fun endpoint(path: String): String =
        baseUrl.trimEnd('/') + path

    private fun retryAfterSeconds(headers: Map<String, String>): Long? =
        headers.entries.firstOrNull {
            it.key.equals("Retry-After", ignoreCase = true)
        }?.value?.trim()?.toLongOrNull()

    private fun parseErrorCode(raw: String): String? =
        try {
            parseErrorCode(JSONObject(raw))
        } catch (_: Exception) {
            null
        }

    private fun parseErrorCode(root: JSONObject): String? =
        root.optJSONObject("error")
            ?.optString("code")
            ?.takeIf { it.isNotBlank() }

    companion object {
        const val HALL_BASE_URL =
            "https://fab-hall-of-fame.gnrationsia.chatgpt.site"
        const val SCORE_PATH =
            "/api/v1/games/geckodoku/scores"
        const val SYNC_PATH =
            "/api/v1/games/geckodoku/sync"
    }
}

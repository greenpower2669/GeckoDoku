package com.greenpower2669.geckodoku

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeckoDokuHallApiClientTest {
    private class FakeTransport(
        private val response: HallHttpResponse? = null,
        private val failure: IOException? = null
    ) : HallHttpTransport {
        var lastRequest: HallHttpRequest? = null

        override fun execute(
            request: HallHttpRequest
        ): HallHttpResponse {
            lastRequest = request
            failure?.let { throw it }
            return requireNotNull(response)
        }
    }

    private fun client(
        response: HallHttpResponse
    ): Pair<GeckoDokuHallApiClient, FakeTransport> {
        val transport = FakeTransport(response)
        return GeckoDokuHallApiClient(transport) to transport
    }

    @Test
    fun http201AcceptedIsConfirmed() {
        val (client, transport) =
            client(
                HallHttpResponse(
                    201,
                    "{\"accepted\":true,\"duplicate\":false,\"scoreId\":\"s1\",\"runId\":\"r1\",\"sequence\":12}"
                )
            )

        val result = client.postScore("{\"runId\":\"r1\"}")

        assertTrue(result is ScorePostResult.Accepted)
        result as ScorePostResult.Accepted
        assertFalse(result.duplicate)
        assertEquals("s1", result.scoreId)
        assertEquals("r1", result.runId)
        assertEquals(12L, result.sequence)
        assertEquals("POST", transport.lastRequest!!.method)
        assertTrue(transport.lastRequest!!.url.endsWith("/api/v1/games/geckodoku/scores"))
        assertEquals("application/json; charset=utf-8", transport.lastRequest!!.headers["Content-Type"])
        assertFalse(transport.lastRequest!!.headers.keys.any { it.equals("Authorization", true) })
    }

    @Test
    fun http200DuplicateAcceptedIsConfirmed() {
        val (client, _) =
            client(
                HallHttpResponse(
                    200,
                    "{\"accepted\":true,\"duplicate\":true,\"scoreId\":\"s1\",\"runId\":\"r1\",\"sequence\":12}"
                )
            )

        val result = client.postScore("{}")
        assertTrue(result is ScorePostResult.Accepted)
        assertTrue((result as ScorePostResult.Accepted).duplicate)
    }

    @Test
    fun acceptedFalseNeverConfirmsPendingScore() {
        val (client, _) =
            client(
                HallHttpResponse(
                    201,
                    "{\"accepted\":false,\"error\":{\"code\":\"INVALID_SCORE\"}}"
                )
            )

        val result = client.postScore("{}")
        assertTrue(result is ScorePostResult.Blocked)
        assertEquals("INVALID_SCORE", (result as ScorePostResult.Blocked).errorCode)
    }

    @Test
    fun invalidAckJsonNeverConfirmsPendingScore() {
        val (client, _) =
            client(HallHttpResponse(201, "not-json"))

        val result = client.postScore("{}")
        assertTrue(result is ScorePostResult.Blocked)
        assertEquals("INVALID_ACK", (result as ScorePostResult.Blocked).errorCode)
    }

    @Test
    fun permanentProtocolErrorsAreBlocked() {
        listOf(400, 409, 413, 415).forEach { status ->
            val (client, _) =
                client(
                    HallHttpResponse(
                        status,
                        "{\"accepted\":false,\"error\":{\"code\":\"E$status\"}}"
                    )
                )

            val result = client.postScore("{}")
            assertTrue("status $status", result is ScorePostResult.Blocked)
            result as ScorePostResult.Blocked
            assertEquals(status, result.httpStatus)
            assertEquals("E$status", result.errorCode)
        }
    }

    @Test
    fun rateLimitReadsRetryAfter() {
        val (client, _) =
            client(
                HallHttpResponse(
                    429,
                    "{\"accepted\":false,\"error\":{\"code\":\"RATE_LIMITED\"}}",
                    mapOf("Retry-After" to "60")
                )
            )

        val result = client.postScore("{}")
        assertTrue(result is ScorePostResult.Retry)
        result as ScorePostResult.Retry
        assertEquals(429, result.httpStatus)
        assertEquals(60L, result.retryAfterSeconds)
        assertEquals("RATE_LIMITED", result.errorCode)
    }

    @Test
    fun serviceAndOtherServerErrorsRetry() {
        listOf(500, 502, 503).forEach { status ->
            val (client, _) =
                client(HallHttpResponse(status, "{}"))
            val result = client.postScore("{}")
            assertTrue("status $status", result is ScorePostResult.Retry)
            assertEquals(status, (result as ScorePostResult.Retry).httpStatus)
        }
    }

    @Test
    fun ioFailureRetriesWithoutHttpStatus() {
        val transport =
            FakeTransport(
                failure = IOException("offline")
            )
        val client = GeckoDokuHallApiClient(transport)

        val result = client.postScore("{}")
        assertTrue(result is ScorePostResult.Retry)
        assertEquals(null, (result as ScorePostResult.Retry).httpStatus)
        assertEquals("NETWORK_ERROR", result.errorCode)
    }

    @Test
    fun syncParsesEntriesAndOpaqueCursor() {
        val (client, transport) =
            client(
                HallHttpResponse(
                    200,
                    "{\"schemaVersion\":1,\"gameId\":\"geckodoku\",\"entries\":[{\"scoreId\":\"s1\",\"runId\":\"r1\",\"sequence\":12,\"receivedAt\":100,\"stars\":5}],\"nextCursor\":\"opaque:12\",\"hasMore\":true,\"highWatermark\":\"20\",\"serverTime\":200}"
                )
            )

        val result = client.fetchSync("cursor /?", 100)
        assertTrue(result is SyncPageResult.Success)
        val page = (result as SyncPageResult.Success).page
        assertEquals("opaque:12", page.nextCursor)
        assertTrue(page.hasMore)
        assertEquals(1, page.entries.size)
        assertEquals("s1", page.entries.single().scoreId)
        assertEquals("r1", page.entries.single().runId)
        assertEquals(12L, page.entries.single().sequence)
        assertTrue(transport.lastRequest!!.url.contains("limit=100"))
        assertFalse(transport.lastRequest!!.url.contains("cursor /?"))
    }
}

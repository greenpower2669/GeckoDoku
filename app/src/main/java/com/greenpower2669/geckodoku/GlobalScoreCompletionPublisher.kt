package com.greenpower2669.geckodoku

import java.util.UUID

class GlobalScoreCompletionPublisher(
    private val pendingStore: PendingScoreStore,
    private val triggerSync: () -> Unit,
    private val runIdFactory: () -> String = {
        UUID.randomUUID().toString()
    },
    private val clock: () -> Long = {
        System.currentTimeMillis()
    }
) {
    fun publish(
        playerName: String,
        mode: GameMode,
        size: Int,
        difficulty: GameDifficulty,
        stars: Int,
        elapsedSeconds: Long,
        mistakes: Int,
        assistancePoints: Int,
        appVersion: String,
        puzzleId: String? = null,
        seed: Long? = null,
        details: GlobalScoreModeDetails,
        metadata: Map<String, Any?> = emptyMap(),
        completedAt: Long = clock()
    ): String? {
        val runId = runIdFactory()

        val common =
            GlobalScoreCommon(
                runId = runId,
                playerName = playerName,
                mode = mode,
                difficulty = difficulty,
                size = size,
                stars = stars,
                elapsedSeconds =
                    elapsedSeconds.coerceAtLeast(0L),
                mistakes =
                    mistakes.coerceAtLeast(0),
                assistancePoints =
                    assistancePoints.coerceAtLeast(0),
                usedProfessor =
                    assistancePoints > 0,
                completedAt = completedAt,
                appVersion = appVersion,
                puzzleId = puzzleId,
                seed = seed,
                metadata = metadata
            )

        val payload =
            GlobalScorePayloadFactory.create(
                common = common,
                details = details
            )

        val frozenJson =
            GlobalScorePayloadCodec.encode(
                payload
            )

        val persisted =
            pendingStore.enqueue(
                runId = runId,
                payloadJson = frozenJson,
                now = completedAt
            )

        if (!persisted) {
            return null
        }

        triggerSync()
        return runId
    }
}

package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class SpriteBankState {
    PRESENT,
    MISSING,
    BUILDING,
    INVALID
}

enum class SpriteResolutionBankState {
    EMPTY,
    IN_PROGRESS,
    COMPLETE,
    INVALID
}

data class SpriteResolutionBankSummary(
    val resolutionHeight: Int,
    val expectedAssets: Int,
    val readyAssets: Int,
    val expectedFrames: Int,
    val generatedFrames: Int,
    val invalidAssets: Int,
    val sizeBytes: Long,
    val percentage: Double,
    val state: SpriteResolutionBankState
)

data class SpriteExportPreparationProgress(
    val generatedFrames: Int,
    val expectedFrames: Int,
    val completeBanks: Int,
    val totalBanks: Int,
    val currentResolutionHeight: Int,
    val percentage: Double
) {
    val complete: Boolean
        get() =
            totalBanks > 0 &&
                completeBanks == totalBanks &&
                generatedFrames >= expectedFrames
}

private enum class SessionInitializationResult {
    READY,
    BUILD_REQUIRED
}

enum class SpriteFactoryPriority(
    val value: Int
) {
    VISIBLE(1),
    VISIBLE_BOOTSTRAP(2),
    VISIBLE_UPGRADE(3),
    CATALOG_LOW(4),
    CATALOG_120(5),
    CATALOG_240(6),
    SECONDARY(7)
}

object SpriteBankFactory {
    const val FORMAT_VERSION = 2
    const val GENERATOR_VERSION =
        "rgba-bank-v2"
    const val FPS = 12
    const val FRAME_DURATION_MS =
        1000L / FPS
    const val QUICK_START_FRAMES = 4
    const val WORKER_COUNT = 3

    private const val ROOT_DIR =
        "sprite-banks-v2"
    private const val MANIFEST_FILE =
        "manifest.json"
    private const val BUILDING_FILE =
        "building.json"

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private val taskSequence =
        AtomicLong(0L)

    private val executor =
        ThreadPoolExecutor(
            WORKER_COUNT,
            WORKER_COUNT,
            0L,
            TimeUnit.MILLISECONDS,
            PriorityBlockingQueue()
        )

    private val lock = Any()
    private val metricsLock = Any()

    private val readyMemory =
        linkedMapOf<
            String,
            SpriteSequence
        >()

    private val sessions =
        linkedMapOf<
            String,
            BuildSession
        >()

    private val stateByRequest =
        linkedMapOf<
            String,
            SpriteBankState
        >()

    private val started =
        AtomicBoolean(false)

    @Volatile
    private var stableBarrierReady =
        false

    private val activeWorkers =
        AtomicInteger(0)
    private val workersPeak =
        AtomicInteger(0)
    private val diskHits =
        AtomicLong(0L)
    private val memoryHits =
        AtomicLong(0L)
    private val apkHits =
        AtomicLong(0L)
    private val generations =
        AtomicLong(0L)
    private val coalesced =
        AtomicLong(0L)
    private val invalidBanks =
        AtomicLong(0L)
    private val catalogMisses =
        AtomicLong(0L)
    private val stableCoalesced =
        AtomicLong(0L)
    private val peakRamMb =
        AtomicLong(0L)

    private var factoryStartedAtMs = 0L
    private var stableCompletedAtMs = -1L
    private var stableReady = 0
    private var stableExpected = 0
    private var reportLogged = false

    private val stageCompletedAtMs =
        mutableMapOf<Int, Long>()

    private val generationMsByHeight =
        mutableMapOf<Int, Long>()

    private var uiMinFps =
        Double.POSITIVE_INFINITY
    private var uiFpsTotal = 0.0
    private var uiFpsSamples = 0L
    private var firstQuickReadyAtMs = -1L
    private var firstAnimationVisibleAtMs = -1L
    private val workerBusyMs =
        linkedMapOf<String, Long>()

    private val expectedFrameCountLock =
        Any()

    private val expectedFrameCountByAsset =
        mutableMapOf<String, Int>()

    private data class BankMetadata(
        val frameCount: Int,
        val width: Int,
        val height: Int,
        val extension: String
    )

    private data class BuildSession(
        val context: Context,
        val requestKey: String,
        val assetPath: String,
        val keyColor: ChromaKeyColor,
        val resolutionHeight: Int,
        val requestedBy: String,
        @Volatile var priority: Int,
        val completeCallbacks:
            MutableList<
                (Result<SpriteSequence>) -> Unit
            > =
            mutableListOf(),
        val quickCallbacks:
            MutableList<
                (SpriteSequence) -> Unit
            > =
            mutableListOf(),
        @Volatile var bankKey: String? = null,
        @Volatile var assetSha256: String? = null,
        @Volatile var dir: File? = null,
        @Volatile var metadata: BankMetadata? = null,
        @Volatile var nextFrame: Int = 0,
        @Volatile var quickSequence:
            SpriteSequence? = null,
        @Volatile var generationStartedAtMs:
            Long = 0L,
        @Volatile var generationCounted:
            Boolean = false,
        @Volatile var scheduleEpoch:
            Long = 0L,
        @Volatile var terminal:
            Boolean = false
    )

    private class FactoryTask(
        val priority: Int,
        val order: Long,
        val action: () -> Unit
    ) :
        Runnable,
        Comparable<FactoryTask> {

        override fun compareTo(
            other: FactoryTask
        ): Int {
            val byPriority =
                priority.compareTo(
                    other.priority
                )

            return if (byPriority != 0) {
                byPriority
            } else {
                order.compareTo(
                    other.order
                )
            }
        }

        override fun run() {
            action()
        }
    }

    fun startCatalogPreparation(
        context: Context
    ) {
        if (!started.compareAndSet(false, true)) {
            return
        }

        val appContext =
            context.applicationContext

        factoryStartedAtMs =
            SystemClock.elapsedRealtime()

        val stableSpecs =
            listOf(
                StableFramePolicy
                    .specFor(MascotKind.GECKO),
                StableFramePolicy
                    .specFor(MascotKind.BEE)
            )
                .filterNotNull()
                .distinct()

        stableExpected =
            stableSpecs.size

        MediaTrace.event(
            source = "SpriteBankFactory",
            event = "SPRITE_FACTORY_START",
            detail =
                "workers=" +
                    WORKER_COUNT +
                    " catalogAssets=" +
                    SpriteCatalog.entries.size +
                    " stableExpected=" +
                    stableExpected
        )

        if (stableSpecs.isEmpty()) {
            stableCompletedAtMs = 0L
            releaseStableBarrier(
                appContext
            )
            return
        }

        var remaining =
            stableSpecs.size

        stableSpecs.forEach { spec ->
            SpriteFrameCache
                .requestStableFrame(
                    context = appContext,
                    assetPath =
                        spec.assetPath,
                    keyColor =
                        spec.keyColor,
                    resolution =
                        SpriteResolution.P240
                ) { result ->
                    if (result.isSuccess) {
                        synchronized(metricsLock) {
                            stableReady += 1
                        }
                    }

                    remaining -= 1

                    if (remaining == 0) {
                        stableCompletedAtMs =
                            SystemClock
                                .elapsedRealtime() -
                                factoryStartedAtMs

                        MediaTrace.event(
                            source =
                                "SpriteBankFactory",
                            event =
                                "SPRITE_FACTORY_STABLE_DONE",
                            detail =
                                "ready=" +
                                    stableReady +
                                    "/" +
                                    stableExpected +
                                    " elapsedMs=" +
                                    stableCompletedAtMs
                        )

                        releaseStableBarrier(
                            appContext
                        )
                    }
                }
        }
    }

    fun prepare(
        context: Context,
        assetPath: String,
        keyColor: ChromaKeyColor,
        resolutionHeight: Int,
        requestedBy: String,
        priority: SpriteFactoryPriority,
        onQuickReady:
            ((SpriteSequence) -> Unit)? = null,
        callback:
            (Result<SpriteSequence>) -> Unit
    ) {
        val appContext =
            context.applicationContext

        startCatalogPreparation(
            appContext
        )

        if (
            !SpriteCatalog.contains(
                assetPath,
                keyColor
            )
        ) {
            catalogMisses.incrementAndGet()

            MediaTrace.event(
                source =
                    "SpriteBankFactory",
                event =
                    "SPRITE_CATALOG_MISS",
                assetPath =
                    assetPath,
                detail =
                    "resolution=" +
                        resolutionHeight +
                        "p requestedBy=" +
                        requestedBy +
                        " key=" +
                        keyColor.name
            )
        }

        val requestKey =
            requestKey(
                assetPath,
                keyColor,
                resolutionHeight
            )

        val memory =
            synchronized(lock) {
                readyMemory[requestKey]
            }

        if (memory != null) {
            memoryHits.incrementAndGet()

            MediaTrace.event(
                source =
                    "SpriteBankFactory",
                event =
                    "SPRITE_BANK_MEMORY_HIT",
                assetPath =
                    assetPath,
                detail =
                    "resolution=" +
                        resolutionHeight +
                        "p requestedBy=" +
                        requestedBy
            )

            mainHandler.post {
                callback(
                    Result.success(memory)
                )
            }
            return
        }

        var newSession:
            BuildSession? = null
        var sessionToBoost:
            BuildSession? = null
        var quickForLateJoin:
            SpriteSequence? = null

        synchronized(lock) {
            val existing =
                sessions[requestKey]

            if (existing != null) {
                val previousPriority =
                    existing.priority
                existing.priority =
                    min(
                        existing.priority,
                        priority.value
                    )

                if (
                    existing.priority <
                        previousPriority
                ) {
                    sessionToBoost =
                        existing
                }

                existing.completeCallbacks
                    .add(callback)

                if (onQuickReady != null) {
                    quickForLateJoin =
                        existing.quickSequence

                    if (quickForLateJoin == null) {
                        existing.quickCallbacks
                            .add(onQuickReady)
                    }
                }

                coalesced.incrementAndGet()

                MediaTrace.event(
                    source =
                        "SpriteBankFactory",
                    event =
                        "SPRITE_BANK_COALESCED",
                    assetPath =
                        assetPath,
                    detail =
                        "resolution=" +
                            resolutionHeight +
                            "p requestedBy=" +
                            requestedBy
                )
            } else {
                val session =
                    BuildSession(
                        context = appContext,
                        requestKey = requestKey,
                        assetPath = assetPath,
                        keyColor = keyColor,
                        resolutionHeight =
                            resolutionHeight,
                        requestedBy =
                            requestedBy,
                        priority =
                            priority.value
                    )

                session.completeCallbacks
                    .add(callback)

                if (onQuickReady != null) {
                    session.quickCallbacks
                        .add(onQuickReady)
                }

                sessions[requestKey] =
                    session
                stateByRequest[requestKey] =
                    SpriteBankState.MISSING
                newSession = session
            }
        }

        if (
            quickForLateJoin != null &&
            onQuickReady != null
        ) {
            val quick =
                quickForLateJoin

            mainHandler.post {
                if (quick != null) {
                    onQuickReady(quick)
                }
            }
        }

        sessionToBoost?.let {
            if (stableBarrierReady) {
                enqueueSession(it)
            }
        }

        newSession?.let {
            if (stableBarrierReady) {
                enqueueSession(it)
            }
        }
    }

    fun recordStableCoalesced() {
        stableCoalesced.incrementAndGet()
    }

    fun recordAnimationVisible(
        assetPath: String,
        resolutionHeight: Int
    ) {
        synchronized(metricsLock) {
            if (
                firstAnimationVisibleAtMs <
                    0L &&
                factoryStartedAtMs >
                    0L
            ) {
                firstAnimationVisibleAtMs =
                    SystemClock
                        .elapsedRealtime() -
                        factoryStartedAtMs

                MediaTrace.event(
                    source =
                        "SpriteBankFactory",
                    event =
                        "SPRITE_FACTORY_FIRST_VISIBLE",
                    assetPath =
                        assetPath,
                    detail =
                        "resolution=" +
                            resolutionHeight +
                            "p elapsedMs=" +
                            firstAnimationVisibleAtMs
                )
            }
        }
    }

    fun recordUiFps(
        fps: Double
    ) {
        if (fps <= 0.0) return

        synchronized(metricsLock) {
            uiMinFps =
                min(uiMinFps, fps)
            uiFpsTotal += fps
            uiFpsSamples += 1L
        }
    }

    fun reportJson(
        context: Context
    ): JSONObject {
        val appContext =
            context.applicationContext

        val counts =
            synchronized(lock) {
                mapOf(
                    60 to
                        readyCountForHeight(60),
                    120 to
                        readyCountForHeight(120),
                    240 to
                        readyCountForHeight(240)
                )
            }

        val expected =
            SpriteCatalog.entries.size

        val missing =
            JSONArray()

        synchronized(lock) {
            SpriteCatalog.entries
                .forEach { entry ->
                    if (
                        stateByRequest[
                            requestKey(
                                entry.assetPath,
                                entry.keyColor,
                                240
                            )
                        ] !=
                            SpriteBankState.PRESENT
                    ) {
                        missing.put(
                            entry.assetPath
                        )
                    }
                }
        }

        val uiValues =
            synchronized(metricsLock) {
                Pair(
                    if (
                        uiMinFps
                            .isFinite()
                    ) {
                        uiMinFps
                    } else {
                        0.0
                    },
                    if (uiFpsSamples > 0L) {
                        uiFpsTotal /
                            uiFpsSamples
                    } else {
                        0.0
                    }
                )
            }

        val root =
            rootDir(appContext)

        return JSONObject()
            .apply {
                put(
                    "formatVersion",
                    FORMAT_VERSION
                )
                put(
                    "generatorVersion",
                    GENERATOR_VERSION
                )
                put(
                    "stableFramesReady",
                    stableReady
                )
                put(
                    "stableFramesExpected",
                    stableExpected
                )
                put(
                    "lowResolutionReady",
                    counts[60] ?: 0
                )
                put(
                    "p120Ready",
                    counts[120] ?: 0
                )
                put(
                    "p240Ready",
                    counts[240] ?: 0
                )
                put(
                    "assetsExpected",
                    expected
                )
                put(
                    "assetsMissing240",
                    missing
                )
                put(
                    "generationTimeStableMs",
                    stableCompletedAtMs
                )
                put(
                    "timeToLowCompleteMs",
                    stageCompletedAtMs[60]
                        ?: -1L
                )
                put(
                    "timeTo120CompleteMs",
                    stageCompletedAtMs[120]
                        ?: -1L
                )
                put(
                    "timeTo240CompleteMs",
                    stageCompletedAtMs[240]
                        ?: -1L
                )
                put(
                    "generationTimeLowMs",
                    generationMsByHeight[60]
                        ?: 0L
                )
                put(
                    "generationTime120Ms",
                    generationMsByHeight[120]
                        ?: 0L
                )
                put(
                    "generationTime240Ms",
                    generationMsByHeight[240]
                        ?: 0L
                )
                put(
                    "firstQuickReadyMs",
                    firstQuickReadyAtMs
                )
                put(
                    "firstAnimationVisibleMs",
                    firstAnimationVisibleAtMs
                )
                put(
                    "totalGenerationMs",
                    if (
                        factoryStartedAtMs > 0L
                    ) {
                        SystemClock
                            .elapsedRealtime() -
                            factoryStartedAtMs
                    } else {
                        0L
                    }
                )
                put(
                    "diskSizeMb",
                    bytesRecursive(root) /
                        (1024.0 * 1024.0)
                )
                put(
                    "peakRamMb",
                    peakRamMb.get()
                )
                put(
                    "workersPeak",
                    workersPeak.get()
                )
                put(
                    "uiMinFps",
                    uiValues.first
                )
                put(
                    "uiAvgFps",
                    uiValues.second
                )
                put(
                    "diskHits",
                    diskHits.get()
                )
                put(
                    "memoryHits",
                    memoryHits.get()
                )
                put(
                    "apkHits",
                    apkHits.get()
                )
                put(
                    "generations",
                    generations.get()
                )
                put(
                    "coalescedRequests",
                    coalesced.get()
                )
                put(
                    "stableCoalescedRequests",
                    stableCoalesced.get()
                )
                put(
                    "invalidBanks",
                    invalidBanks.get()
                )
                put(
                    "catalogMisses",
                    catalogMisses.get()
                )
                put(
                    "workerBusyMs",
                    JSONObject().apply {
                        synchronized(metricsLock) {
                            workerBusyMs
                                .forEach {
                                    (name, value) ->
                                    put(name, value)
                                }
                        }
                    }
                )
            }
    }

    fun globalIndexJson(
        context: Context
    ): JSONObject {
        val banks =
            JSONArray()

        validatedBankDirectories(
            context.applicationContext
        ).forEach { dir ->
            val manifest =
                runCatching {
                    JSONObject(
                        File(
                            dir,
                            MANIFEST_FILE
                        ).readText()
                    )
                }.getOrNull()
                    ?: return@forEach

            banks.put(
                JSONObject()
                    .apply {
                        put(
                            "bankKey",
                            dir.name
                        )
                        put(
                            "asset",
                            manifest
                                .optString(
                                    "assetPath"
                                )
                        )
                        put(
                            "resolution",
                            manifest
                                .optInt(
                                    "resolutionHeight"
                                )
                        )
                        put(
                            "assetSha256",
                            manifest
                                .optString(
                                    "assetSha256"
                                )
                        )
                        put(
                            "bankSha256",
                            manifest
                                .optString(
                                    "bankSha256"
                                )
                        )
                    }
            )
        }

        return JSONObject()
            .apply {
                put(
                    "formatVersion",
                    FORMAT_VERSION
                )
                put(
                    "generatorVersion",
                    GENERATOR_VERSION
                )
                put(
                    "generatedAt",
                    System.currentTimeMillis()
                )
                put(
                    "banks",
                    banks
                )
                put(
                    "report",
                    reportJson(context)
                )
                put(
                    "resolutionBanks",
                    JSONArray().apply {
                        resolutionBankSummaries(
                            context
                        ).forEach {
                            summary ->
                            put(
                                JSONObject()
                                    .apply {
                                        put(
                                            "resolutionHeight",
                                            summary
                                                .resolutionHeight
                                        )
                                        put(
                                            "percentage",
                                            summary
                                                .percentage
                                        )
                                        put(
                                            "sizeBytes",
                                            summary
                                                .sizeBytes
                                        )
                                        put(
                                            "state",
                                            summary
                                                .state
                                                .name
                                        )
                                    }
                            )
                        }
                    }
                )
            }
    }

    fun exportBankHeights():
        List<Int> =
        listOf(
            60,
            120,
            240,
            480
        )

    fun exportPreparationProgress(
        context: Context
    ): SpriteExportPreparationProgress {
        val summaries =
            exportBankHeights()
                .map {
                    height ->
                    resolutionBankSummary(
                        context,
                        height
                    )
                }

        val expectedFrames =
            summaries.sumOf {
                it.expectedFrames
            }
        val generatedFrames =
            summaries.sumOf {
                it.generatedFrames
            }
        val completeBanks =
            summaries.count {
                it.state ==
                    SpriteResolutionBankState
                        .COMPLETE
            }
        val current =
            summaries.firstOrNull {
                it.state !=
                    SpriteResolutionBankState
                        .COMPLETE
            }?.resolutionHeight
                ?: 480
        val percentage =
            if (expectedFrames > 0) {
                (
                    generatedFrames
                        .toDouble() *
                        100.0 /
                        expectedFrames
                            .toDouble()
                    )
                    .coerceIn(
                        0.0,
                        100.0
                    )
            } else {
                0.0
            }

        return SpriteExportPreparationProgress(
            generatedFrames =
                generatedFrames,
            expectedFrames =
                expectedFrames,
            completeBanks =
                completeBanks,
            totalBanks =
                summaries.size,
            currentResolutionHeight =
                current,
            percentage =
                percentage
        )
    }

    fun prepareExportThrough480(
        context: Context,
        callback:
            (Result<Unit>) -> Unit
    ) {
        val appContext =
            context.applicationContext
        val jobs =
            exportBankHeights()
                .flatMap {
                    height ->
                    SpriteCatalog.entries.map {
                        entry ->
                        Pair(
                            height,
                            entry
                        )
                    }
                }

        if (jobs.isEmpty()) {
            mainHandler.post {
                callback(
                    Result.success(Unit)
                )
            }
            return
        }

        val remaining =
            AtomicInteger(
                jobs.size
            )
        val failed =
            AtomicInteger(0)

        MediaTrace.event(
            source =
                "SpriteBankFactory",
            event =
                "SPRITE_EXPORT_PREPARE_START",
            detail =
                "banks=" +
                    exportBankHeights()
                        .joinToString(
                            ">"
                        ) {
                            it.toString() +
                                "p"
                        } +
                    " assets=" +
                    SpriteCatalog.entries
                        .size +
                    " jobs=" +
                    jobs.size
        )

        jobs.forEach {
                (height, entry) ->
            prepare(
                context =
                    appContext,
                assetPath =
                    entry.assetPath,
                keyColor =
                    entry.keyColor,
                resolutionHeight =
                    height,
                requestedBy =
                    "EXPORT_480",
                priority =
                    SpriteFactoryPriority
                        .VISIBLE_UPGRADE,
                callback = {
                    result ->
                    if (result.isFailure) {
                        failed.incrementAndGet()
                    }

                    if (
                        remaining
                            .decrementAndGet() ==
                            0
                    ) {
                        val failures =
                            failed.get()

                        MediaTrace.event(
                            source =
                                "SpriteBankFactory",
                            event =
                                if (
                                    failures ==
                                        0
                                ) {
                                    "SPRITE_EXPORT_PREPARE_COMPLETE"
                                } else {
                                    "SPRITE_EXPORT_PREPARE_ERROR"
                                },
                            detail =
                                "failures=" +
                                    failures +
                                    " jobs=" +
                                    jobs.size
                        )

                        if (failures == 0) {
                            callback(
                                Result.success(
                                    Unit
                                )
                            )
                        } else {
                            callback(
                                Result.failure(
                                    IllegalStateException(
                                        failures
                                            .toString() +
                                            " banque(s) n'ont pas pu être préparées."
                                    )
                                )
                            )
                        }
                    }
                }
            )
        }
    }

    fun validatedBankDirectories(
        context: Context,
        resolutionHeight: Int? = null
    ): List<File> {
        val appContext =
            context.applicationContext

        val expectedKeys =
            resolutionHeight?.let {
                height ->
                currentBankKeys(
                    appContext,
                    height
                )
            }

        return rootDir(appContext)
            .listFiles()
            .orEmpty()
            .filter {
                dir ->
                if (!dir.isDirectory) {
                    false
                } else if (
                    expectedKeys != null &&
                    dir.name !in expectedKeys
                ) {
                    false
                } else {
                    runCatching {
                        val manifest =
                            JSONObject(
                                File(
                                    dir,
                                    MANIFEST_FILE
                                ).readText()
                            )

                        manifest.optString(
                            "status"
                        ) == "READY" &&
                            manifest.optInt(
                                "formatVersion",
                                -1
                            ) ==
                            FORMAT_VERSION &&
                            manifest.optString(
                                "generatorVersion"
                            ) ==
                            GENERATOR_VERSION &&
                            (
                                resolutionHeight ==
                                    null ||
                                    manifest.optInt(
                                        "resolutionHeight",
                                        -1
                                    ) ==
                                    resolutionHeight
                                )
                    }.getOrDefault(false)
                }
            }
            .sortedBy {
                it.name
            }
    }

    fun resolutionBankDirectories(
        context: Context,
        resolutionHeight: Int
    ): List<File> {
        val appContext =
            context.applicationContext
        val keys =
            currentBankKeys(
                appContext,
                resolutionHeight
            )

        return rootDir(appContext)
            .listFiles()
            .orEmpty()
            .filter {
                it.isDirectory &&
                    it.name in keys
            }
            .sortedBy {
                it.name
            }
    }

    fun knownResolutionBankHeights():
        List<Int> =
        (
            listOf(
                SpriteProgressivePolicy
                    .INTERNAL_LOW_HEIGHT
            ) +
                SpriteResolution
                    .values()
                    .map {
                        it.heightPx
                    }
            )
            .distinct()
            .sorted()

    fun resolutionBankSummaries(
        context: Context
    ): List<SpriteResolutionBankSummary> =
        knownResolutionBankHeights()
            .map {
                resolutionBankSummary(
                    context,
                    it
                )
            }

    fun resolutionBankSummary(
        context: Context,
        resolutionHeight: Int
    ): SpriteResolutionBankSummary {
        val appContext =
            context.applicationContext
        var expectedFrames = 0
        var generatedFrames = 0
        var readyAssets = 0
        var invalidAssets = 0
        var sizeBytes = 0L

        SpriteCatalog.entries
            .forEach {
                entry ->
                val expected =
                    expectedFrameCount(
                        appContext,
                        entry.assetPath
                    )

                expectedFrames +=
                    expected

                val assetSha =
                    sourceHash(
                        appContext,
                        entry.assetPath
                    )
                val key =
                    bankKey(
                        entry.assetPath,
                        entry.keyColor,
                        resolutionHeight,
                        assetSha
                    )
                val dir =
                    File(
                        rootDir(appContext),
                        key
                    )

                if (!dir.isDirectory) {
                    return@forEach
                }

                sizeBytes +=
                    bytesRecursive(dir)

                val session =
                    BuildSession(
                        context =
                            appContext,
                        requestKey =
                            requestKey(
                                entry.assetPath,
                                entry.keyColor,
                                resolutionHeight
                            ),
                        assetPath =
                            entry.assetPath,
                        keyColor =
                            entry.keyColor,
                        resolutionHeight =
                            resolutionHeight,
                        requestedBy =
                            "BANK_SUMMARY",
                        priority =
                            SpriteFactoryPriority
                                .SECONDARY
                                .value
                    ).apply {
                        assetSha256 =
                            assetSha
                    }

                val ready =
                    readReadyBank(
                        session,
                        dir
                    )

                if (ready != null) {
                    readyAssets += 1
                    generatedFrames +=
                        ready.frames.size
                } else {
                    val partial =
                        partialFrameCount(
                            entry =
                                entry,
                            assetSha =
                                assetSha,
                            resolutionHeight =
                                resolutionHeight,
                            dir =
                                dir
                        )

                    generatedFrames +=
                        partial

                    if (
                        File(
                            dir,
                            MANIFEST_FILE
                        ).isFile
                    ) {
                        invalidAssets += 1
                    }
                }
            }

        val percentage =
            if (expectedFrames > 0) {
                (
                    generatedFrames
                        .toDouble() *
                        100.0 /
                        expectedFrames
                            .toDouble()
                    )
                    .coerceIn(
                        0.0,
                        100.0
                    )
            } else {
                0.0
            }

        val state =
            when {
                invalidAssets > 0 ->
                    SpriteResolutionBankState
                        .INVALID

                readyAssets ==
                    SpriteCatalog.entries.size &&
                    generatedFrames >=
                    expectedFrames ->
                    SpriteResolutionBankState
                        .COMPLETE

                generatedFrames > 0 ->
                    SpriteResolutionBankState
                        .IN_PROGRESS

                else ->
                    SpriteResolutionBankState
                        .EMPTY
            }

        return SpriteResolutionBankSummary(
            resolutionHeight =
                resolutionHeight,
            expectedAssets =
                SpriteCatalog.entries.size,
            readyAssets =
                readyAssets,
            expectedFrames =
                expectedFrames,
            generatedFrames =
                generatedFrames,
            invalidAssets =
                invalidAssets,
            sizeBytes =
                sizeBytes,
            percentage =
                percentage,
            state =
                state
        )
    }

    fun resolutionBankManifestJson(
        context: Context,
        resolutionHeight: Int
    ): JSONObject {
        val appContext =
            context.applicationContext
        val summary =
            resolutionBankSummary(
                appContext,
                resolutionHeight
            )
        val assets =
            JSONArray()

        SpriteCatalog.entries
            .forEach {
                entry ->
                val assetSha =
                    sourceHash(
                        appContext,
                        entry.assetPath
                    )
                val key =
                    bankKey(
                        entry.assetPath,
                        entry.keyColor,
                        resolutionHeight,
                        assetSha
                    )
                val dir =
                    File(
                        rootDir(appContext),
                        key
                    )
                val manifest =
                    runCatching {
                        JSONObject(
                            File(
                                dir,
                                MANIFEST_FILE
                            ).readText()
                        )
                    }.getOrNull()
                val building =
                    runCatching {
                        JSONObject(
                            File(
                                dir,
                                BUILDING_FILE
                            ).readText()
                        )
                    }.getOrNull()
                val expected =
                    expectedFrameCount(
                        appContext,
                        entry.assetPath
                    )
                val generated =
                    when {
                        manifest != null &&
                            manifest.optString(
                                "status"
                            ) ==
                            "READY" ->
                            manifest.optInt(
                                "frameCount",
                                0
                            )

                        building != null ->
                            partialFrameCount(
                                entry =
                                    entry,
                                assetSha =
                                    assetSha,
                                resolutionHeight =
                                    resolutionHeight,
                                dir =
                                    dir
                            )

                        else ->
                            0
                    }

                assets.put(
                    JSONObject()
                        .apply {
                            put(
                                "assetPath",
                                entry.assetPath
                            )
                            put(
                                "keyColor",
                                entry.keyColor.name
                            )
                            put(
                                "bankKey",
                                key
                            )
                            put(
                                "expectedFrames",
                                expected
                            )
                            put(
                                "generatedFrames",
                                generated
                            )
                            put(
                                "status",
                                when {
                                    manifest !=
                                        null &&
                                        manifest.optString(
                                            "status"
                                        ) ==
                                        "READY" ->
                                        "READY"

                                    building !=
                                        null ->
                                        "BUILDING"

                                    else ->
                                        "MISSING"
                                }
                            )
                            put(
                                "bankSha256",
                                manifest
                                    ?.optString(
                                        "bankSha256"
                                    )
                                    ?: ""
                            )
                        }
                )
            }

        return JSONObject()
            .apply {
                put(
                    "status",
                    summary.state.name
                )
                put(
                    "formatVersion",
                    FORMAT_VERSION
                )
                put(
                    "generatorVersion",
                    GENERATOR_VERSION
                )
                put(
                    "resolutionHeight",
                    resolutionHeight
                )
                put(
                    "fps",
                    FPS
                )
                put(
                    "expectedAssets",
                    summary.expectedAssets
                )
                put(
                    "readyAssets",
                    summary.readyAssets
                )
                put(
                    "expectedFrames",
                    summary.expectedFrames
                )
                put(
                    "generatedFrames",
                    summary.generatedFrames
                )
                put(
                    "percentage",
                    summary.percentage
                )
                put(
                    "sizeBytes",
                    summary.sizeBytes
                )
                put(
                    "invalidAssets",
                    summary.invalidAssets
                )
                put(
                    "generatedAt",
                    System.currentTimeMillis()
                )
                put(
                    "assets",
                    assets
                )
            }
    }

    fun stableRoot(
        context: Context
    ): File =
        File(
            context.applicationContext
                .filesDir,
            "stable-frames"
        )

    private fun releaseStableBarrier(
        context: Context
    ) {
        stableBarrierReady =
            true

        val waiting =
            synchronized(lock) {
                sessions.values
                    .toList()
            }

        waiting.forEach {
            enqueueSession(it)
        }

        enqueueCatalog(context)
    }

    private fun enqueueCatalog(
        context: Context
    ) {
        val target =
            RichMediaSettings
                .spriteResolutionFor(
                    context
                )

        val stages =
            SpriteProgressivePolicy
                .stagesFor(target)
                .map {
                    height ->
                    height to
                        catalogPriorityFor(
                            height
                        )
                }

        MediaTrace.event(
            source =
                "SpriteBankFactory",
            event =
                "SPRITE_FACTORY_CATALOG_PLAN",
            detail =
                "target=" +
                    target.label +
                    " stages=" +
                    stages.joinToString(
                        ">"
                    ) {
                        it.first
                            .toString() +
                            "p"
                    }
        )

        stages.forEach {
                (height, priority) ->
            SpriteCatalog.entries
                .forEach { entry ->
                    prepare(
                        context = context,
                        assetPath =
                            entry.assetPath,
                        keyColor =
                            entry.keyColor,
                        resolutionHeight =
                            height,
                        requestedBy =
                            "CATALOG",
                        priority =
                            priority,
                        callback = {
                            result ->
                            if (
                                result.isFailure
                            ) {
                                MediaTrace.event(
                                    source =
                                        "SpriteBankFactory",
                                    event =
                                        "SPRITE_CATALOG_ERROR",
                                    assetPath =
                                        entry.assetPath,
                                    detail =
                                        "resolution=" +
                                            height +
                                            "p error=" +
                                            (
                                                result
                                                    .exceptionOrNull()
                                                    ?.javaClass
                                                    ?.simpleName
                                                    ?: "unknown"
                                                )
                                )
                            }
                        }
                    )
                }
        }
    }

    private fun catalogPriorityFor(
        height: Int
    ): SpriteFactoryPriority =
        when (height) {
            60 ->
                SpriteFactoryPriority
                    .CATALOG_LOW

            120 ->
                SpriteFactoryPriority
                    .CATALOG_120

            240 ->
                SpriteFactoryPriority
                    .CATALOG_240

            else ->
                SpriteFactoryPriority
                    .SECONDARY
        }

    private fun enqueueSession(
        session: BuildSession
    ) {
        val epoch =
            synchronized(session) {
                session.scheduleEpoch +=
                    1L
                session.scheduleEpoch
            }

        enqueueTask(
            session.priority
        ) {
            if (
                epoch !=
                    session.scheduleEpoch
            ) {
                return@enqueueTask
            }

            resolveOrBuildChunk(
                session
            )
        }
    }

    private fun enqueueTask(
        priority: Int,
        action: () -> Unit
    ) {
        val task =
            FactoryTask(
                priority =
                    priority,
                order =
                    taskSequence
                        .incrementAndGet()
            ) {
                val workerStartedAt =
                    SystemClock
                        .elapsedRealtime()
                val active =
                    activeWorkers
                        .incrementAndGet()

                updatePeak(
                    workersPeak,
                    active
                )

                runCatching {
                    Process.setThreadPriority(
                        Process
                            .THREAD_PRIORITY_BACKGROUND
                    )
                }

                try {
                    action()
                } finally {
                    val elapsed =
                        SystemClock
                            .elapsedRealtime() -
                            workerStartedAt

                    synchronized(metricsLock) {
                        val name =
                            Thread
                                .currentThread()
                                .name

                        workerBusyMs[name] =
                            (
                                workerBusyMs[
                                    name
                                ] ?: 0L
                                ) +
                                elapsed
                    }

                    activeWorkers
                        .decrementAndGet()
                    sampleRam()
                }
            }

        executor.execute(task)
    }

    private fun resolveOrBuildChunk(
        session: BuildSession
    ) {
        try {
            val initialization =
                synchronized(session) {
                    if (session.terminal) {
                        SessionInitializationResult
                            .READY
                    } else if (
                        session.metadata ==
                            null
                    ) {
                        initializeSession(
                            session
                        )
                    } else {
                        SessionInitializationResult
                            .BUILD_REQUIRED
                    }
                }

            if (
                initialization ==
                    SessionInitializationResult
                        .READY ||
                session.terminal
            ) {
                return
            }

            val metadata =
                requireNotNull(
                    session.metadata
                )
            val dir =
                requireNotNull(
                    session.dir
                )

            if (
                session.nextFrame >=
                    metadata.frameCount
            ) {
                finalizeSession(
                    session
                )
                return
            }

            if (
                !session
                    .generationCounted
            ) {
                session.generationCounted =
                    true
                session.generationStartedAtMs =
                    SystemClock
                        .elapsedRealtime()
                generations
                    .incrementAndGet()
            }

            synchronized(lock) {
                stateByRequest[
                    session.requestKey
                ] =
                    SpriteBankState.BUILDING
            }

            val retriever =
                MediaMetadataRetriever()

            try {
                session.context
                    .assets
                    .openFd(
                        session.assetPath
                    )
                    .use { afd ->
                        retriever.setDataSource(
                            afd.fileDescriptor,
                            afd.startOffset,
                            afd.length
                        )
                    }

                val end =
                    min(
                        metadata.frameCount,
                        session.nextFrame +
                            QUICK_START_FRAMES
                    )

                for (
                    index in
                        session.nextFrame until end
                ) {
                    val timeUs =
                        index.toLong() *
                            1_000_000L /
                            FPS

                    val source =
                        SpriteFrameCache
                            .frameAt(
                                retriever =
                                    retriever,
                                timeUs =
                                    timeUs,
                                width =
                                    metadata.width,
                                height =
                                    metadata.height
                            )
                            ?: continue

                    val transparent =
                        SpriteFrameCache
                            .applyChromaKey(
                                source,
                                session.keyColor
                            )

                    if (
                        transparent !==
                            source
                    ) {
                        source.recycle()
                    }

                    val finalFile =
                        frameFile(
                            dir,
                            index,
                            metadata.extension
                        )
                    val tempFile =
                        File(
                            finalFile
                                .absolutePath +
                                ".tmp"
                        )

                    writeBitmap(
                        transparent,
                        tempFile
                    )
                    transparent.recycle()

                    if (
                        finalFile.exists()
                    ) {
                        finalFile.delete()
                    }

                    check(
                        tempFile.renameTo(
                            finalFile
                        )
                    ) {
                        "Unable to publish sprite frame"
                    }

                    session.nextFrame =
                        index + 1

                    maybeDeliverQuick(
                        session
                    )
                }
            } finally {
                retriever.release()
            }

            if (
                session.nextFrame >=
                    metadata.frameCount
            ) {
                finalizeSession(
                    session
                )
            } else {
                enqueueSession(
                    session
                )
            }
        } catch (
            error: Throwable
        ) {
            failSession(
                session,
                error
            )
        }
    }

    private fun initializeSession(
        session: BuildSession
    ): SessionInitializationResult {
        val assetSha =
            sourceHash(
                session.context,
                session.assetPath
            )

        val bankKey =
            bankKey(
                session.assetPath,
                session.keyColor,
                session.resolutionHeight,
                assetSha
            )

        val dir =
            File(
                rootDir(
                    session.context
                ),
                bankKey
            )

        session.assetSha256 =
            assetSha
        session.bankKey =
            bankKey
        session.dir =
            dir

        readEmbeddedReady(
            session,
            bankKey,
            dir
        )?.let {
            apkHits.incrementAndGet()
            completeSession(
                session,
                it,
                hit = "APK"
            )
            return SessionInitializationResult
                .READY
        }

        val local =
            readReadyBank(
                session,
                dir
            )

        if (local != null) {
            diskHits.incrementAndGet()
            completeSession(
                session,
                local,
                hit = "DISK"
            )
            return SessionInitializationResult
                .READY
        }

        val manifest =
            File(
                dir,
                MANIFEST_FILE
            )

        if (manifest.isFile) {
            invalidBanks
                .incrementAndGet()

            synchronized(lock) {
                stateByRequest[
                    session.requestKey
                ] =
                    SpriteBankState.INVALID
            }

            MediaTrace.event(
                source =
                    "SpriteBankFactory",
                event =
                    "SPRITE_BANK_INVALID",
                assetPath =
                    session.assetPath,
                detail =
                    "resolution=" +
                        session.resolutionHeight +
                        "p bankKey=" +
                        bankKey
            )

            clearDirectory(dir)
        }

        val metadata =
            metadataFor(
                session
            )

        session.metadata =
            metadata

        val building =
            File(
                dir,
                BUILDING_FILE
            )

        val canResume =
            building.isFile &&
                runCatching {
                    val json =
                        JSONObject(
                            building.readText()
                        )

                    json.optInt(
                        "formatVersion"
                    ) ==
                        FORMAT_VERSION &&
                        json.optString(
                            "generatorVersion"
                        ) ==
                        GENERATOR_VERSION &&
                        json.optString(
                            "assetPath"
                        ) ==
                        session.assetPath &&
                        json.optString(
                            "assetSha256"
                        ) ==
                        assetSha &&
                        json.optString(
                            "keyColor"
                        ) ==
                        session.keyColor.name &&
                        json.optInt(
                            "resolutionHeight"
                        ) ==
                        session.resolutionHeight &&
                        json.optInt(
                            "frameCount"
                        ) ==
                        metadata.frameCount &&
                        json.optInt(
                            "width"
                        ) ==
                        metadata.width &&
                        json.optInt(
                            "height"
                        ) ==
                        metadata.height &&
                        json.optString(
                            "extension"
                        ) ==
                        metadata.extension
                }.getOrDefault(false)

        if (!canResume) {
            clearDirectory(
                dir
            )
            dir.mkdirs()

            building.writeText(
                buildingJson(
                    session,
                    metadata
                ).toString(2)
            )
        }

        session.nextFrame =
            contiguousFrameCount(
                dir,
                metadata
            )

        synchronized(lock) {
            stateByRequest[
                session.requestKey
            ] =
                SpriteBankState.BUILDING
        }

        if (
            session.nextFrame > 0
        ) {
            MediaTrace.event(
                source =
                    "SpriteBankFactory",
                event =
                    "SPRITE_BANK_RESUME",
                assetPath =
                    session.assetPath,
                detail =
                    "resolution=" +
                        session.resolutionHeight +
                        "p frames=" +
                        session.nextFrame +
                        "/" +
                        metadata.frameCount
            )
        } else {
            MediaTrace.event(
                source =
                    "SpriteBankFactory",
                event =
                    "SPRITE_BANK_BUILD_START",
                assetPath =
                    session.assetPath,
                detail =
                    "resolution=" +
                        session.resolutionHeight +
                        "p frames=" +
                        metadata.frameCount +
                        " requestedBy=" +
                        session.requestedBy
            )
        }

        maybeDeliverQuick(
            session
        )

        return SessionInitializationResult
            .BUILD_REQUIRED
    }

    private fun maybeDeliverQuick(
        session: BuildSession
    ) {
        if (
            session.quickSequence !=
                null
        ) {
            return
        }

        val metadata =
            session.metadata
                ?: return
        val dir =
            session.dir
                ?: return

        val threshold =
            min(
                QUICK_START_FRAMES,
                metadata.frameCount
            )

        if (
            session.nextFrame <
                threshold
        ) {
            return
        }

        val frames =
            (0 until session.nextFrame)
                .map {
                    index ->
                    frameFile(
                        dir,
                        index,
                        metadata.extension
                    )
                }
                .takeWhile {
                    it.isFile
                }

        if (
            frames.size <
                threshold
        ) {
            return
        }

        val partial =
            SpriteSequence(
                frames = frames,
                frameDurationMs =
                    FRAME_DURATION_MS,
                width =
                    metadata.width,
                height =
                    metadata.height
            )

        session.quickSequence =
            partial

        val callbacks =
            synchronized(lock) {
                session.quickCallbacks
                    .toList()
                    .also {
                        session
                            .quickCallbacks
                            .clear()
                    }
            }

        synchronized(metricsLock) {
            if (
                firstQuickReadyAtMs <
                    0L &&
                factoryStartedAtMs >
                    0L
            ) {
                firstQuickReadyAtMs =
                    SystemClock
                        .elapsedRealtime() -
                        factoryStartedAtMs
            }
        }

        MediaTrace.event(
            source =
                "SpriteBankFactory",
            event =
                "SPRITE_QUICK_READY",
            assetPath =
                session.assetPath,
            detail =
                "frames=" +
                    partial.frames.size +
                    "/" +
                    metadata.frameCount +
                    " resolution=" +
                    session.resolutionHeight +
                    "p workerActive=" +
                    activeWorkers.get()
        )

        if (callbacks.isNotEmpty()) {
            mainHandler.post {
                callbacks.forEach {
                    it(partial)
                }
            }
        }
    }

    private fun finalizeSession(
        session: BuildSession
    ) {
        val metadata =
            requireNotNull(
                session.metadata
            )
        val dir =
            requireNotNull(
                session.dir
            )

        val frames =
            (0 until metadata.frameCount)
                .map {
                    index ->
                    frameFile(
                        dir,
                        index,
                        metadata.extension
                    )
                }

        check(
            frames.all {
                it.isFile &&
                    it.length() > 0L
            }
        ) {
            "Sprite bank incomplete"
        }

        val bankDigest =
            digestFiles(frames)

        val generationElapsedMs =
            if (
                session.generationStartedAtMs >
                    0L
            ) {
                SystemClock
                    .elapsedRealtime() -
                    session
                        .generationStartedAtMs
            } else {
                0L
            }

        val manifest =
            JSONObject()
                .apply {
                    put(
                        "status",
                        "READY"
                    )
                    put(
                        "formatVersion",
                        FORMAT_VERSION
                    )
                    put(
                        "generatorVersion",
                        GENERATOR_VERSION
                    )
                    put(
                        "assetPath",
                        session.assetPath
                    )
                    put(
                        "assetSha256",
                        session.assetSha256
                    )
                    put(
                        "keyColor",
                        session.keyColor.name
                    )
                    put(
                        "resolutionHeight",
                        session.resolutionHeight
                    )
                    put(
                        "frameCount",
                        metadata.frameCount
                    )
                    put(
                        "fps",
                        FPS
                    )
                    put(
                        "frameDurationMs",
                        FRAME_DURATION_MS
                    )
                    put(
                        "width",
                        metadata.width
                    )
                    put(
                        "height",
                        metadata.height
                    )
                    put(
                        "extension",
                        metadata.extension
                    )
                    put(
                        "frameFormat",
                        if (
                            metadata.extension ==
                                "webp"
                        ) {
                            "WEBP_LOSSLESS"
                        } else {
                            "PNG"
                        }
                    )
                    put(
                        "keyThreshold",
                        AssetMediaCatalog
                            .KEY_THRESHOLD
                    )
                    put(
                        "keySoftness",
                        AssetMediaCatalog
                            .KEY_SOFTNESS
                    )
                    put(
                        "keyDespill",
                        AssetMediaCatalog
                            .KEY_DESPILL
                    )
                    put(
                        "bankSha256",
                        bankDigest
                    )
                    put(
                        "generationMs",
                        generationElapsedMs
                    )
                    put(
                        "completedAt",
                        System
                            .currentTimeMillis()
                    )
                }

        val temp =
            File(
                dir,
                MANIFEST_FILE +
                    ".tmp"
            )
        val final =
            File(
                dir,
                MANIFEST_FILE
            )

        temp.writeText(
            manifest.toString(2)
        )

        if (final.exists()) {
            final.delete()
        }

        check(
            temp.renameTo(final)
        ) {
            "Unable to publish sprite manifest"
        }

        File(
            dir,
            BUILDING_FILE
        ).delete()

        val sequence =
            SpriteSequence(
                frames = frames,
                frameDurationMs =
                    FRAME_DURATION_MS,
                width =
                    metadata.width,
                height =
                    metadata.height
            )

        if (
            session.generationStartedAtMs >
                0L
        ) {
            synchronized(metricsLock) {
                generationMsByHeight[
                    session
                        .resolutionHeight
                ] =
                    (
                        generationMsByHeight[
                            session
                                .resolutionHeight
                        ] ?: 0L
                        ) +
                        generationElapsedMs
            }
        }

        MediaTrace.event(
            source =
                "SpriteBankFactory",
            event =
                "SPRITE_BANK_READY",
            assetPath =
                session.assetPath,
            detail =
                "resolution=" +
                    session.resolutionHeight +
                    "p frames=" +
                    frames.size +
                    " bankKey=" +
                    session.bankKey
        )

        completeSession(
            session,
            sequence,
            hit = null
        )
    }

    private fun completeSession(
        session: BuildSession,
        sequence: SpriteSequence,
        hit: String?
    ) {
        val callbacks =
            synchronized(lock) {
                session.terminal =
                    true
                readyMemory[
                    session.requestKey
                ] =
                    sequence
                stateByRequest[
                    session.requestKey
                ] =
                    SpriteBankState.PRESENT
                sessions.remove(
                    session.requestKey
                )
                session.completeCallbacks
                    .toList()
            }

        if (hit != null) {
            MediaTrace.event(
                source =
                    "SpriteBankFactory",
                event =
                    "SPRITE_BANK_" +
                        hit +
                        "_HIT",
                assetPath =
                    session.assetPath,
                detail =
                    "resolution=" +
                        session.resolutionHeight +
                        "p requestedBy=" +
                        session.requestedBy
            )
        }

        mainHandler.post {
            callbacks.forEach {
                it(
                    Result.success(
                        sequence
                    )
                )
            }
        }

        runCatching {
            updateStageCompletion(
                session.resolutionHeight
            )

            maybeLogFinalReport(
                session.context
            )
        }.onFailure {
            error ->
            MediaTrace.event(
                source =
                    "SpriteBankFactory",
                event =
                    "SPRITE_BANK_POST_COMPLETE_WARNING",
                assetPath =
                    session.assetPath,
                detail =
                    "resolution=" +
                        session.resolutionHeight +
                        "p error=" +
                        error.javaClass
                            .simpleName +
                        ":" +
                        (
                            error.message
                                ?: ""
                            )
            )
        }
    }

    private fun failSession(
        session: BuildSession,
        error: Throwable
    ) {
        if (session.terminal) {
            return
        }

        val callbacks =
            synchronized(lock) {
                if (session.terminal) {
                    return
                }

                session.terminal =
                    true
                sessions.remove(
                    session.requestKey
                )
                stateByRequest[
                    session.requestKey
                ] =
                    SpriteBankState.INVALID
                session.completeCallbacks
                    .toList()
            }

        MediaTrace.event(
            source =
                "SpriteBankFactory",
            event =
                "SPRITE_BANK_ERROR",
            assetPath =
                session.assetPath,
            detail =
                "resolution=" +
                    session.resolutionHeight +
                    "p requestedBy=" +
                    session.requestedBy +
                    " error=" +
                    error.javaClass
                        .simpleName +
                    ":" +
                    (
                        error.message
                            ?: ""
                        )
        )

        mainHandler.post {
            callbacks.forEach {
                it(
                    Result.failure(
                        error
                    )
                )
            }
        }
    }

    private fun readReadyBank(
        session: BuildSession,
        dir: File
    ): SpriteSequence? {
        val manifestFile =
            File(
                dir,
                MANIFEST_FILE
            )

        if (!manifestFile.isFile) {
            return null
        }

        val json =
            runCatching {
                JSONObject(
                    manifestFile
                        .readText()
                )
            }.getOrNull()
                ?: return null

        if (
            !validManifest(
                session,
                json
            )
        ) {
            return null
        }

        val count =
            json.optInt(
                "frameCount",
                -1
            )
        val extension =
            json.optString(
                "extension"
            )
        val width =
            json.optInt(
                "width",
                -1
            )
        val height =
            json.optInt(
                "height",
                -1
            )
        val duration =
            json.optLong(
                "frameDurationMs",
                -1L
            )

        if (
            count <= 0 ||
            width <= 0 ||
            height <= 0 ||
            duration <= 0L ||
            extension.isBlank()
        ) {
            return null
        }

        val frames =
            (0 until count)
                .map {
                    index ->
                    frameFile(
                        dir,
                        index,
                        extension
                    )
                }

        if (
            frames.any {
                !it.isFile ||
                    it.length() <= 0L
            }
        ) {
            return null
        }

        return SpriteSequence(
            frames = frames,
            frameDurationMs =
                duration,
            width = width,
            height = height
        )
    }

    private fun readEmbeddedReady(
        session: BuildSession,
        bankKey: String,
        localDir: File
    ): SpriteSequence? {
        val bases =
            listOf(
                "sprites/banks/" +
                    session.resolutionHeight +
                    "p/" +
                    bankKey,
                "sprites/" +
                    bankKey
            )

        var selectedBase:
            String? = null
        var manifestText:
            String? = null

        for (candidate in bases) {
            val value =
                runCatching {
                    session.context
                        .assets
                        .open(
                            candidate +
                                "/" +
                                MANIFEST_FILE
                        )
                        .bufferedReader()
                        .use {
                            it.readText()
                        }
                }.getOrNull()

            if (value != null) {
                selectedBase =
                    candidate
                manifestText =
                    value
                break
            }
        }

        val base =
            selectedBase
                ?: return null
        val readyManifestText =
            manifestText
                ?: return null

        val manifest =
            runCatching {
                JSONObject(
                    readyManifestText
                )
            }.getOrNull()
                ?: return null

        if (
            !validManifest(
                session,
                manifest
            )
        ) {
            return null
        }

        readReadyBank(
            session,
            localDir
        )?.let {
            return it
        }

        val count =
            manifest.optInt(
                "frameCount",
                -1
            )
        val extension =
            manifest.optString(
                "extension"
            )

        if (
            count <= 0 ||
            extension.isBlank()
        ) {
            return null
        }

        clearDirectory(
            localDir
        )
        localDir.mkdirs()

        try {
            for (
                index in 0 until count
            ) {
                val name =
                    frameName(
                        index,
                        extension
                    )

                session.context
                    .assets
                    .open(
                        base +
                            "/" +
                            name
                    )
                    .use {
                        input ->
                        FileOutputStream(
                            File(
                                localDir,
                                name
                            )
                        ).use {
                            output ->
                            input.copyTo(
                                output
                            )
                        }
                    }
            }

            File(
                localDir,
                MANIFEST_FILE
            ).writeText(
                readyManifestText
            )
        } catch (
            error: Throwable
        ) {
            clearDirectory(
                localDir
            )
            return null
        }

        return readReadyBank(
            session,
            localDir
        )
    }

    private fun validManifest(
        session: BuildSession,
        json: JSONObject
    ): Boolean =
        json.optString(
            "status"
        ) == "READY" &&
            json.optInt(
                "formatVersion",
                -1
            ) ==
                FORMAT_VERSION &&
            json.optString(
                "generatorVersion"
            ) ==
                GENERATOR_VERSION &&
            json.optString(
                "assetPath"
            ) ==
                session.assetPath &&
            json.optString(
                "assetSha256"
            ) ==
                session.assetSha256 &&
            json.optString(
                "keyColor"
            ) ==
                session.keyColor.name &&
            json.optInt(
                "resolutionHeight",
                -1
            ) ==
                session.resolutionHeight &&
            json.optInt(
                "fps",
                -1
            ) ==
                FPS &&
            kotlin.math.abs(
                json.optDouble(
                    "keyThreshold",
                    Double.NaN
                ) -
                    AssetMediaCatalog
                        .KEY_THRESHOLD
            ) <
                0.0001 &&
            kotlin.math.abs(
                json.optDouble(
                    "keySoftness",
                    Double.NaN
                ) -
                    AssetMediaCatalog
                        .KEY_SOFTNESS
            ) <
                0.0001 &&
            kotlin.math.abs(
                json.optDouble(
                    "keyDespill",
                    Double.NaN
                ) -
                    AssetMediaCatalog
                        .KEY_DESPILL
            ) <
                0.0001

    private fun metadataFor(
        session: BuildSession
    ): BankMetadata {
        val retriever =
            MediaMetadataRetriever()

        try {
            session.context
                .assets
                .openFd(
                    session.assetPath
                )
                .use { afd ->
                    retriever.setDataSource(
                        afd.fileDescriptor,
                        afd.startOffset,
                        afd.length
                    )
                }

            val durationMs =
                retriever.extractMetadata(
                    MediaMetadataRetriever
                        .METADATA_KEY_DURATION
                )
                    ?.toLongOrNull()
                    ?.coerceAtLeast(1L)
                    ?: 1L

            val sourceWidth =
                retriever.extractMetadata(
                    MediaMetadataRetriever
                        .METADATA_KEY_VIDEO_WIDTH
                )
                    ?.toIntOrNull()
                    ?.coerceAtLeast(1)
                    ?: 854

            val sourceHeight =
                retriever.extractMetadata(
                    MediaMetadataRetriever
                        .METADATA_KEY_VIDEO_HEIGHT
                )
                    ?.toIntOrNull()
                    ?.coerceAtLeast(1)
                    ?: 480

            val targetHeight =
                min(
                    session
                        .resolutionHeight,
                    sourceHeight
                ).coerceAtLeast(1)

            val targetWidth =
                (
                    sourceWidth.toDouble() *
                        targetHeight.toDouble() /
                        sourceHeight.toDouble()
                    )
                    .roundToInt()
                    .coerceAtLeast(1)

            val frameCount =
                max(
                    1,
                    (
                        durationMs *
                            FPS /
                            1000L
                        ).toInt()
                )

            return BankMetadata(
                frameCount =
                    frameCount,
                width =
                    targetWidth,
                height =
                    targetHeight,
                extension =
                    if (
                        Build.VERSION.SDK_INT >=
                            Build.VERSION_CODES.R
                    ) {
                        "webp"
                    } else {
                        "png"
                    }
            )
        } finally {
            retriever.release()
        }
    }

    private fun buildingJson(
        session: BuildSession,
        metadata: BankMetadata
    ): JSONObject =
        JSONObject()
            .apply {
                put(
                    "status",
                    "BUILDING"
                )
                put(
                    "formatVersion",
                    FORMAT_VERSION
                )
                put(
                    "generatorVersion",
                    GENERATOR_VERSION
                )
                put(
                    "assetPath",
                    session.assetPath
                )
                put(
                    "assetSha256",
                    session.assetSha256
                )
                put(
                    "keyColor",
                    session.keyColor.name
                )
                put(
                    "resolutionHeight",
                    session.resolutionHeight
                )
                put(
                    "frameCount",
                    metadata.frameCount
                )
                put(
                    "fps",
                    FPS
                )
                put(
                    "width",
                    metadata.width
                )
                put(
                    "height",
                    metadata.height
                )
                put(
                    "extension",
                    metadata.extension
                )
            }

    private fun contiguousFrameCount(
        dir: File,
        metadata: BankMetadata
    ): Int {
        var count = 0

        while (
            count <
                metadata.frameCount
        ) {
            val frame =
                frameFile(
                    dir,
                    count,
                    metadata.extension
                )

            if (
                !frame.isFile ||
                frame.length() <= 0L
            ) {
                break
            }

            count += 1
        }

        return count
    }

    private fun sourceHash(
        context: Context,
        assetPath: String
    ): String {
        val length =
            runCatching {
                context.assets
                    .openFd(
                        assetPath
                    )
                    .use {
                        it.length
                    }
            }.getOrDefault(-1L)

        val updateToken =
            runCatching {
                context.packageManager
                    .getPackageInfo(
                        context.packageName,
                        0
                    )
                    .lastUpdateTime
            }.getOrDefault(0L)

        val prefs =
            context.getSharedPreferences(
                "sprite_asset_hashes_v2",
                Context.MODE_PRIVATE
            )

        val key =
            updateToken
                .toString() +
                "|" +
                length +
                "|" +
                assetPath

        prefs.getString(
            key,
            null
        )?.let {
            return it
        }

        val digest =
            MessageDigest
                .getInstance(
                    "SHA-256"
                )

        context.assets
            .open(assetPath)
            .use {
                input ->
                val buffer =
                    ByteArray(
                        64 * 1024
                    )

                while (true) {
                    val read =
                        input.read(
                            buffer
                        )

                    if (read <= 0) {
                        break
                    }

                    digest.update(
                        buffer,
                        0,
                        read
                    )
                }
            }

        val hash =
            digest.digest()
                .toHex()

        prefs.edit()
            .putString(
                key,
                hash
            )
            .apply()

        return hash
    }

    private fun bankKey(
        assetPath: String,
        keyColor: ChromaKeyColor,
        resolutionHeight: Int,
        assetSha256: String
    ): String {
        val raw =
            GENERATOR_VERSION +
                "|" +
                assetPath +
                "|" +
                assetSha256 +
                "|" +
                keyColor.name +
                "|" +
                resolutionHeight +
                "|" +
                AssetMediaCatalog
                    .KEY_THRESHOLD +
                "|" +
                AssetMediaCatalog
                    .KEY_SOFTNESS +
                "|" +
                AssetMediaCatalog
                    .KEY_DESPILL

        val digest =
            MessageDigest
                .getInstance(
                    "SHA-256"
                )
                .digest(
                    raw.toByteArray(
                        Charsets.UTF_8
                    )
                )
                .toHex()

        return digest
            .take(24) +
            "-" +
            resolutionHeight
    }

    private fun requestKey(
        assetPath: String,
        keyColor: ChromaKeyColor,
        resolutionHeight: Int
    ): String =
        assetPath +
            "|" +
            keyColor.name +
            "|" +
            resolutionHeight

    private fun frameFile(
        dir: File,
        index: Int,
        extension: String
    ): File =
        File(
            dir,
            frameName(
                index,
                extension
            )
        )

    private fun frameName(
        index: Int,
        extension: String
    ): String =
        "frame-" +
            index
                .toString()
                .padStart(
                    5,
                    '0'
                ) +
            "." +
            extension

    private fun writeBitmap(
        bitmap: Bitmap,
        file: File
    ) {
        FileOutputStream(file)
            .use {
                stream ->
                val format =
                    if (
                        Build.VERSION.SDK_INT >=
                            Build.VERSION_CODES.R
                    ) {
                        Bitmap
                            .CompressFormat
                            .WEBP_LOSSLESS
                    } else {
                        Bitmap
                            .CompressFormat
                            .PNG
                    }

                check(
                    bitmap.compress(
                        format,
                        100,
                        stream
                    )
                )
            }
    }

    private fun digestFiles(
        files: List<File>
    ): String {
        val digest =
            MessageDigest
                .getInstance(
                    "SHA-256"
                )
        val buffer =
            ByteArray(
                64 * 1024
            )

        files.forEach {
            file ->
            file.inputStream()
                .use {
                    input ->
                    while (true) {
                        val read =
                            input.read(
                                buffer
                            )

                        if (read <= 0) {
                            break
                        }

                        digest.update(
                            buffer,
                            0,
                            read
                        )
                    }
                }
        }

        return digest.digest()
            .toHex()
    }

    private fun updateStageCompletion(
        height: Int
    ) {
        if (
            height !in
                listOf(
                    60,
                    120,
                    240
                )
        ) {
            return
        }

        synchronized(lock) {
            if (
                readyCountForHeight(
                    height
                ) ==
                    SpriteCatalog.entries
                        .size &&
                stageCompletedAtMs[
                    height
                ] == null
            ) {
                stageCompletedAtMs[
                    height
                ] =
                    if (
                        factoryStartedAtMs > 0L
                    ) {
                        SystemClock
                            .elapsedRealtime() -
                            factoryStartedAtMs
                    } else {
                        0L
                    }

                MediaTrace.event(
                    source =
                        "SpriteBankFactory",
                    event =
                        "SPRITE_FACTORY_STAGE_COMPLETE",
                    detail =
                        "resolution=" +
                            height +
                            "p elapsedMs=" +
                            stageCompletedAtMs[
                                height
                            ]
                )
            }
        }
    }

    private fun readyCountForHeight(
        height: Int
    ): Int =
        SpriteCatalog.entries
            .count {
                entry ->
                stateByRequest[
                    requestKey(
                        entry.assetPath,
                        entry.keyColor,
                        height
                    )
                ] ==
                    SpriteBankState.PRESENT
            }

    private fun maybeLogFinalReport(
        context: Context
    ) {
        synchronized(lock) {
            if (
                reportLogged ||
                readyCountForHeight(
                    240
                ) !=
                    SpriteCatalog.entries
                        .size
            ) {
                return
            }

            reportLogged = true
        }

        val report =
            reportJson(
                context
            )

        MediaTrace.event(
            source =
                "SpriteBankFactory",
            event =
                "SPRITE_FACTORY_REPORT",
            detail =
                "stableFrames=" +
                    report.optInt(
                        "stableFramesReady"
                    ) +
                    "/" +
                    report.optInt(
                        "stableFramesExpected"
                    ) +
                    " lowResolution=" +
                    report.optInt(
                        "lowResolutionReady"
                    ) +
                    "/" +
                    SpriteCatalog.entries.size +
                    " p120=" +
                    report.optInt(
                        "p120Ready"
                    ) +
                    "/" +
                    SpriteCatalog.entries.size +
                    " p240=" +
                    report.optInt(
                        "p240Ready"
                    ) +
                    "/" +
                    SpriteCatalog.entries.size +
                    " diskSizeMb=" +
                    String.format(
                        Locale.US,
                        "%.2f",
                        report.optDouble(
                            "diskSizeMb"
                        )
                    ) +
                    " workersPeak=" +
                    report.optInt(
                        "workersPeak"
                    ) +
                    " uiMinFps=" +
                    String.format(
                        Locale.US,
                        "%.2f",
                        report.optDouble(
                            "uiMinFps"
                        )
                    )
        )
    }

    private fun sampleRam() {
        val runtime =
            Runtime.getRuntime()
        val used =
            (
                runtime.totalMemory() -
                    runtime.freeMemory()
                ) /
                (1024L * 1024L)

        updatePeak(
            peakRamMb,
            used.toInt()
        )
    }

    private fun updatePeak(
        target: AtomicInteger,
        value: Int
    ) {
        while (true) {
            val previous =
                target.get()

            if (
                value <= previous ||
                target.compareAndSet(
                    previous,
                    value
                )
            ) {
                return
            }
        }
    }

    private fun updatePeak(
        target: AtomicLong,
        value: Int
    ) {
        while (true) {
            val previous =
                target.get()

            if (
                value.toLong() <=
                    previous ||
                target.compareAndSet(
                    previous,
                    value.toLong()
                )
            ) {
                return
            }
        }
    }

    private fun rootDir(
        context: Context
    ): File =
        File(
            context.filesDir,
            ROOT_DIR
        ).apply {
            mkdirs()
        }

    private fun currentBankKeys(
        context: Context,
        resolutionHeight: Int
    ): Set<String> =
        SpriteCatalog.entries
            .map {
                entry ->
                val assetSha =
                    sourceHash(
                        context,
                        entry.assetPath
                    )

                bankKey(
                    entry.assetPath,
                    entry.keyColor,
                    resolutionHeight,
                    assetSha
                )
            }
            .toSet()

    private fun expectedFrameCount(
        context: Context,
        assetPath: String
    ): Int {
        synchronized(
            expectedFrameCountLock
        ) {
            expectedFrameCountByAsset[
                assetPath
            ]?.let {
                return it
            }
        }

        val retriever =
            MediaMetadataRetriever()

        val count =
            try {
                context.assets
                    .openFd(
                        assetPath
                    )
                    .use {
                        afd ->
                        retriever.setDataSource(
                            afd.fileDescriptor,
                            afd.startOffset,
                            afd.length
                        )
                    }

                val durationMs =
                    retriever
                        .extractMetadata(
                            MediaMetadataRetriever
                                .METADATA_KEY_DURATION
                        )
                        ?.toLongOrNull()
                        ?.coerceAtLeast(1L)
                        ?: 1L

                max(
                    1,
                    (
                        durationMs *
                            FPS /
                            1000L
                        ).toInt()
                )
            } finally {
                retriever.release()
            }

        synchronized(
            expectedFrameCountLock
        ) {
            expectedFrameCountByAsset[
                assetPath
            ] =
                count
        }

        return count
    }

    private fun partialFrameCount(
        entry: SpriteCatalogEntry,
        assetSha: String,
        resolutionHeight: Int,
        dir: File
    ): Int {
        val building =
            File(
                dir,
                BUILDING_FILE
            )

        if (!building.isFile) {
            return 0
        }

        val json =
            runCatching {
                JSONObject(
                    building.readText()
                )
            }.getOrNull()
                ?: return 0

        if (
            json.optInt(
                "formatVersion",
                -1
            ) !=
                FORMAT_VERSION ||
            json.optString(
                "generatorVersion"
            ) !=
                GENERATOR_VERSION ||
            json.optString(
                "assetPath"
            ) !=
                entry.assetPath ||
            json.optString(
                "assetSha256"
            ) !=
                assetSha ||
            json.optString(
                "keyColor"
            ) !=
                entry.keyColor.name ||
            json.optInt(
                "resolutionHeight",
                -1
            ) !=
                resolutionHeight
        ) {
            return 0
        }

        val expected =
            json.optInt(
                "frameCount",
                0
            )
        val extension =
            json.optString(
                "extension"
            )

        if (
            expected <= 0 ||
            extension.isBlank()
        ) {
            return 0
        }

        var count = 0

        while (count < expected) {
            val frame =
                frameFile(
                    dir,
                    count,
                    extension
                )

            if (
                !frame.isFile ||
                frame.length() <= 0L
            ) {
                break
            }

            count += 1
        }

        return count
    }

    private fun clearDirectory(
        dir: File
    ) {
        if (dir.exists()) {
            dir.deleteRecursively()
        }

        dir.mkdirs()
    }

    private fun bytesRecursive(
        file: File
    ): Long {
        if (!file.exists()) {
            return 0L
        }

        if (file.isFile) {
            return file.length()
        }

        return file.listFiles()
            .orEmpty()
            .sumOf {
                bytesRecursive(it)
            }
    }

    private fun ByteArray.toHex():
        String =
        joinToString("") {
            "%02x".format(it)
        }
}

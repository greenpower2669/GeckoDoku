package com.greenpower2669.geckodoku

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Build
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView

class ChromaKeyTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextureView(context, attrs),
    ChromaKeyPlayback {

    private data class PlaybackRequest(
        val assetPath: String,
        val onCompletion: () -> Unit,
        val onError: (String) -> Unit,
        val onStarted: () -> Unit,
        val generation: Long,
        val revealOnFirstFrame: Boolean,
        val holdOnFirstFrame: Boolean,
        val onFirstFrameRendered: () -> Unit
    )

    override var logicalLayer: String =
        "UNSPECIFIED"

    private var outputSurfaceTexture:
        SurfaceTexture? = null

    private var player:
        MediaPlayer? = null

    private var pendingPlayback:
        PlaybackRequest? = null

    private var activeRequest:
        PlaybackRequest? = null

    private var activeAssetPath:
        String? = null

    private var muted = false

    private var audioTrackIndices:
        List<Int> =
        emptyList()

    private var activeGeneration =
        0L

    private var surfaceUpdateSerial =
        0L

    private var firstFrameBaselineSerial =
        0L

    private var playerStarted =
        false

    private var renderingStarted =
        false

    private var firstFrameDelivered =
        false

    private var firstFrameHeld =
        false

    private var videoWidth =
        1

    private var videoHeight =
        1

    private var greenKeyStrength =
        ChromaKeyColor.BLUE.greenStrength

    private var yellowTintStrength =
        0f

    private val runtimeShader:
        RuntimeShader? =
        try {
            RuntimeShader(
                AGSL_CHROMA_SHADER
            ).also {
                shader ->
                shader.setFloatUniform(
                    "uThreshold",
                    AssetMediaCatalog.KEY_THRESHOLD
                )
                shader.setFloatUniform(
                    "uSoftness",
                    AssetMediaCatalog.KEY_SOFTNESS
                )
                shader.setFloatUniform(
                    "uDespill",
                    AssetMediaCatalog.KEY_DESPILL
                )
                shader.setFloatUniform(
                    "uYellowTint",
                    yellowTintStrength
                )
                shader.setFloatUniform(
                    "uGreenKeyStrength",
                    greenKeyStrength
                )
            }
        } catch (
            error: Throwable
        ) {
            MediaTrace.event(
                source = traceSource(),
                event =
                    "TEXTURE_SHADER_INIT_ERROR",
                detail =
                    error.message
                        ?: error.javaClass.simpleName
            )
            null
        }

    init {
        isOpaque = false
        alpha = 0f
        isClickable = false
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_NO

        runtimeShader
            ?.let {
                shader ->
                setRenderEffect(
                    RenderEffect
                        .createRuntimeShaderEffect(
                            shader,
                            "content"
                        )
                )
            }

        surfaceTextureListener =
            object :
                SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    outputSurfaceTexture =
                        surface

                    MediaTrace.event(
                        source = traceSource(),
                        event =
                            "TEXTURE_SURFACE_AVAILABLE",
                        detail =
                            "width=" +
                                width +
                                " height=" +
                                height
                    )

                    updateVideoTransform()
                    startPendingPlayback()
                }

                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    updateVideoTransform()
                }

                override fun onSurfaceTextureDestroyed(
                    surface: SurfaceTexture
                ): Boolean {
                    if (
                        outputSurfaceTexture ===
                        surface
                    ) {
                        outputSurfaceTexture =
                            null
                    }

                    stopPlayback()

                    MediaTrace.event(
                        source = traceSource(),
                        event =
                            "TEXTURE_SURFACE_DESTROYED"
                    )

                    return true
                }

                override fun onSurfaceTextureUpdated(
                    surface: SurfaceTexture
                ) {
                    surfaceUpdateSerial += 1L

                    maybeRevealFreshFrame()
                }
            }

        MediaTrace.event(
            source = traceSource(),
            event =
                "TEXTURE_BACKEND_READY",
            detail =
                "runtimeShader=" +
                    (runtimeShader != null)
        )
    }

    override fun play(
        assetPath: String,
        muted: Boolean,
        onCompletion: () -> Unit,
        onError: (String) -> Unit,
        onStarted: () -> Unit,
        revealOnFirstFrame: Boolean,
        holdOnFirstFrame: Boolean,
        onFirstFrameRendered: () -> Unit
    ) {
        MediaTrace.event(
            source = traceSource(),
            event = "PLAY_REQUEST",
            assetPath = assetPath,
            detail =
                "backend=TextureView muted=" +
                    muted +
                    " hadPlayer=" +
                    (player != null) +
                    " hadPending=" +
                    (pendingPlayback != null)
        )

        stopPlayback()

        val shader =
            runtimeShader

        if (shader == null) {
            onError(
                "RuntimeShader unavailable for " +
                    assetPath
            )
            return
        }

        activeGeneration += 1L

        this.muted = muted
        firstFrameBaselineSerial =
            surfaceUpdateSerial
        playerStarted = false
        renderingStarted = false
        firstFrameDelivered = false
        firstFrameHeld = false

        alpha =
            if (revealOnFirstFrame) {
                0f
            } else {
                1f
            }

        val request =
            PlaybackRequest(
                assetPath = assetPath,
                onCompletion =
                    onCompletion,
                onError = onError,
                onStarted = onStarted,
                generation =
                    activeGeneration,
                revealOnFirstFrame =
                    revealOnFirstFrame,
                holdOnFirstFrame =
                    holdOnFirstFrame,
                onFirstFrameRendered =
                    onFirstFrameRendered
            )

        pendingPlayback = request

        if (revealOnFirstFrame) {
            MediaTrace.event(
                source = traceSource(),
                event =
                    "VIDEO_VISIBILITY_ARMED",
                assetPath = assetPath,
                detail =
                    "backend=TextureView alpha=0 baseline=" +
                        firstFrameBaselineSerial
            )
        }

        startPendingPlayback()
    }

    override fun revealHeldFirstFrame():
        Boolean {
        if (!firstFrameHeld) {
            return false
        }

        val current =
            player
                ?: return false

        return try {
            firstFrameHeld = false
            alpha = 1f
            current.start()

            MediaTrace.event(
                source = traceSource(),
                event =
                    "VIDEO_VISIBLE",
                assetPath =
                    activeAssetPath,
                detail =
                    "backend=TextureView heldFirstFrame=true alpha=1"
            )
            true
        } catch (
            error: Throwable
        ) {
            false
        }
    }

    override fun setMuted(
        value: Boolean
    ) {
        muted = value

        player?.let {
            current ->
            current.setVolume(
                if (value) 0f else 1f,
                if (value) 0f else 1f
            )
            applyAudioTrackPolicy(
                current
            )
        }
    }

    override fun setYellowTint(
        enabled: Boolean
    ) {
        yellowTintStrength =
            if (enabled) {
                1f
            } else {
                0f
            }

        runtimeShader
            ?.setFloatUniform(
                "uYellowTint",
                yellowTintStrength
            )

        invalidate()
    }

    override fun setKeyColor(
        color: ChromaKeyColor
    ) {
        greenKeyStrength =
            color.greenStrength

        runtimeShader
            ?.setFloatUniform(
                "uGreenKeyStrength",
                greenKeyStrength
            )

        invalidate()
    }

    override fun stopPlayback() {
        val pendingAsset =
            pendingPlayback
                ?.assetPath

        if (
            player != null ||
            pendingAsset != null ||
            activeAssetPath != null
        ) {
            MediaTrace.event(
                source = traceSource(),
                event = "STOP",
                assetPath =
                    activeAssetPath
                        ?: pendingAsset,
                detail =
                    "backend=TextureView player=" +
                        (player != null) +
                        " pending=" +
                        (pendingAsset != null)
            )
        }

        pendingPlayback = null
        activeRequest = null
        playerStarted = false
        renderingStarted = false
        firstFrameDelivered = false
        firstFrameHeld = false
        alpha = 0f

        val current =
            player
        player = null

        if (current != null) {
            try {
                current.setOnCompletionListener(
                    null
                )
                current.setOnErrorListener(
                    null
                )
                current.setOnInfoListener(
                    null
                )
                current.stop()
            } catch (
                _: Throwable
            ) {
                // The player may already be stopped/released.
            }

            current.release()

            AudioCapturePolicy.log(
                source = "VIDEO",
                detail =
                    "STOP_RELEASE backend=TextureView"
            )
        }

        activeAssetPath = null
        audioTrackIndices =
            emptyList()
    }

    override fun release() {
        MediaTrace.event(
            source = traceSource(),
            event = "RELEASE",
            assetPath =
                activeAssetPath,
            detail =
                "backend=TextureView"
        )

        stopPlayback()
    }

    override fun onSizeChanged(
        width: Int,
        height: Int,
        oldWidth: Int,
        oldHeight: Int
    ) {
        super.onSizeChanged(
            width,
            height,
            oldWidth,
            oldHeight
        )
        updateVideoTransform()
    }

    private fun startPendingPlayback() {
        val request =
            pendingPlayback
                ?: return

        val texture =
            outputSurfaceTexture
                ?: return

        if (
            request.generation !=
            activeGeneration
        ) {
            pendingPlayback = null
            return
        }

        pendingPlayback = null
        activeRequest = request

        try {
            val mediaPlayer =
                MediaPlayer()

            mediaPlayer.setAudioAttributes(
                AudioCapturePolicy
                    .videoAttributes()
            )

            player = mediaPlayer

            val descriptor =
                context.assets.openFd(
                    request.assetPath
                )

            descriptor.use {
                mediaPlayer.setDataSource(
                    it.fileDescriptor,
                    it.startOffset,
                    it.length
                )
            }

            val surface =
                Surface(texture)

            mediaPlayer.setSurface(
                surface
            )
            surface.release()

            mediaPlayer
                .setOnVideoSizeChangedListener {
                        _,
                        width,
                        height ->

                    videoWidth =
                        width.coerceAtLeast(1)
                    videoHeight =
                        height.coerceAtLeast(1)

                    updateVideoTransform()
                }

            mediaPlayer
                .setOnPreparedListener {
                    prepared ->

                    if (
                        player !== prepared ||
                        request.generation !=
                        activeGeneration
                    ) {
                        return@setOnPreparedListener
                    }

                    activeAssetPath =
                        request.assetPath

                    audioTrackIndices =
                        prepared.trackInfo
                            .mapIndexedNotNull {
                                index,
                                info ->

                                if (
                                    info.trackType ==
                                    MediaPlayer.TrackInfo
                                        .MEDIA_TRACK_TYPE_AUDIO
                                ) {
                                    index
                                } else {
                                    null
                                }
                            }

                    applyAudioTrackPolicy(
                        prepared
                    )

                    prepared.setVolume(
                        if (muted) 0f else 1f,
                        if (muted) 0f else 1f
                    )

                    AudioCapturePolicy.log(
                        source = "VIDEO",
                        detail =
                            "START asset=" +
                                request.assetPath +
                                " usage=MEDIA content=MOVIE capture=ALLOW_ALL muted=" +
                                muted +
                                " audioTracks=" +
                                audioTrackIndices.size +
                                " backend=TextureView"
                    )

                    MediaTrace.event(
                        source = traceSource(),
                        event = "START",
                        assetPath =
                            request.assetPath,
                        detail =
                            "backend=TextureView muted=" +
                                muted
                    )

                    playerStarted = true

                    try {
                        prepared.start()
                        request.onStarted()
                    } catch (
                        error: Throwable
                    ) {
                        playerStarted = false
                        failRequest(
                            request,
                            error
                        )
                    }
                }

            mediaPlayer
                .setOnInfoListener {
                        current,
                        what,
                        extra ->

                    if (
                        player === current &&
                        request.generation ==
                        activeGeneration &&
                        what ==
                        MediaPlayer
                            .MEDIA_INFO_VIDEO_RENDERING_START
                    ) {
                        renderingStarted = true

                        MediaTrace.event(
                            source = traceSource(),
                            event =
                                "VIDEO_RENDERING_START_SIGNAL",
                            assetPath =
                                request.assetPath,
                            detail =
                                "backend=TextureView extra=" +
                                    extra +
                                    " generation=" +
                                    request.generation
                        )

                        maybeRevealFreshFrame()
                    }

                    false
                }

            mediaPlayer
                .setOnCompletionListener {
                    completed ->

                    if (
                        player !== completed ||
                        request.generation !=
                        activeGeneration
                    ) {
                        return@setOnCompletionListener
                    }

                    MediaTrace.event(
                        source = traceSource(),
                        event = "COMPLETE",
                        assetPath =
                            request.assetPath,
                        detail =
                            "backend=TextureView"
                    )

                    player = null
                    activeRequest = null
                    activeAssetPath = null
                    playerStarted = false
                    renderingStarted = false
                    firstFrameHeld = false

                    completed.release()
                    request.onCompletion()
                }

            mediaPlayer
                .setOnErrorListener {
                        failed,
                        what,
                        extra ->

                    if (
                        player === failed
                    ) {
                        player = null
                    }

                    failed.release()

                    MediaTrace.event(
                        source = traceSource(),
                        event = "ERROR",
                        assetPath =
                            request.assetPath,
                        detail =
                            "backend=TextureView what=" +
                                what +
                                " extra=" +
                                extra
                    )

                    activeRequest = null
                    activeAssetPath = null
                    playerStarted = false
                    renderingStarted = false

                    request.onError(
                        "MediaPlayer error " +
                            what +
                            "/" +
                            extra +
                            " for " +
                            request.assetPath
                    )

                    true
                }

            mediaPlayer.prepareAsync()
        } catch (
            error: Throwable
        ) {
            failRequest(
                request,
                error
            )
        }
    }

    private fun maybeRevealFreshFrame() {
        val request =
            activeRequest
                ?: return

        if (
            firstFrameDelivered ||
            !playerStarted ||
            !renderingStarted ||
            surfaceUpdateSerial <=
            firstFrameBaselineSerial ||
            request.generation !=
            activeGeneration
        ) {
            return
        }

        firstFrameDelivered = true

        MediaTrace.event(
            source = traceSource(),
            event =
                "VIDEO_FIRST_FRAME",
            assetPath =
                request.assetPath,
            detail =
                "backend=TextureView surfaceUpdated=true serial=" +
                    surfaceUpdateSerial +
                    " baseline=" +
                    firstFrameBaselineSerial +
                    " hold=" +
                    request.holdOnFirstFrame
        )

        if (
            request.holdOnFirstFrame
        ) {
            try {
                player?.pause()
                firstFrameHeld = true
            } catch (
                _: Throwable
            ) {
                firstFrameHeld = false
            }

            MediaTrace.event(
                source = traceSource(),
                event =
                    "VIDEO_FIRST_FRAME_HELD",
                assetPath =
                    request.assetPath,
                detail =
                    "backend=TextureView alpha=0"
            )
        } else if (
            request.revealOnFirstFrame
        ) {
            alpha = 1f

            MediaTrace.event(
                source = traceSource(),
                event =
                    "VIDEO_VISIBLE",
                assetPath =
                    request.assetPath,
                detail =
                    "backend=TextureView alpha=1"
            )
        }

        request
            .onFirstFrameRendered
            .invoke()
    }

    private fun failRequest(
        request: PlaybackRequest,
        error: Throwable
    ) {
        if (
            request.generation !=
            activeGeneration
        ) {
            return
        }

        val current =
            player
        player = null

        try {
            current?.release()
        } catch (
            _: Throwable
        ) {
            // Ignore release failures.
        }

        pendingPlayback = null
        activeRequest = null
        activeAssetPath = null
        playerStarted = false
        renderingStarted = false
        firstFrameHeld = false

        MediaTrace.event(
            source = traceSource(),
            event = "EXCEPTION",
            assetPath =
                request.assetPath,
            detail =
                "backend=TextureView " +
                    (
                        error.message
                            ?: error.javaClass
                                .simpleName
                        )
        )

        request.onError(
            "Unable to play " +
                request.assetPath +
                ": " +
                (
                    error.message
                        ?: "unknown error"
                    )
        )
    }

    private fun applyAudioTrackPolicy(
        mediaPlayer: MediaPlayer
    ) {
        val tracks =
            if (
                audioTrackIndices
                    .isNotEmpty()
            ) {
                audioTrackIndices
            } else {
                try {
                    mediaPlayer
                        .trackInfo
                        .mapIndexedNotNull {
                            index,
                            info ->

                            if (
                                info.trackType ==
                                MediaPlayer.TrackInfo
                                    .MEDIA_TRACK_TYPE_AUDIO
                            ) {
                                index
                            } else {
                                null
                            }
                        }
                } catch (
                    _: Throwable
                ) {
                    return
                }
            }

        if (muted) {
            tracks.forEach {
                index ->
                try {
                    mediaPlayer
                        .deselectTrack(
                            index
                        )
                } catch (
                    _: Throwable
                ) {
                    // Some devices keep the default audio selection.
                }
            }

            AudioCapturePolicy.log(
                source = "VIDEO",
                detail =
                    "MUTE_DECODED_AUDIO tracks=" +
                        tracks.size +
                        " backend=TextureView"
            )
        } else {
            tracks
                .firstOrNull()
                ?.let {
                    index ->
                    try {
                        mediaPlayer
                            .selectTrack(
                                index
                            )
                    } catch (
                        _: Throwable
                    ) {
                        // Default MediaPlayer selection remains active.
                    }
                }
        }
    }

    private fun updateVideoTransform() {
        if (
            width <= 0 ||
            height <= 0 ||
            videoWidth <= 0 ||
            videoHeight <= 0
        ) {
            return
        }

        val viewAspect =
            width.toFloat() /
                height.toFloat()

        val videoAspect =
            videoWidth.toFloat() /
                videoHeight.toFloat()

        val scaleX: Float
        val scaleY: Float

        if (
            videoAspect >
            viewAspect
        ) {
            scaleX = 1f
            scaleY =
                viewAspect /
                    videoAspect
        } else {
            scaleX =
                videoAspect /
                    viewAspect
            scaleY = 1f
        }

        val matrix =
            Matrix()

        matrix.setScale(
            scaleX,
            scaleY,
            width / 2f,
            height / 2f
        )

        setTransform(
            matrix
        )
    }

    private fun traceSource():
        String =
        "ChromaTexture@" +
            Integer.toHexString(
                System.identityHashCode(
                    this
                )
            ) +
            "[" +
            logicalLayer +
            "]"

    companion object {
        @SuppressLint("NewApi")
        private const val AGSL_CHROMA_SHADER =
            """
            uniform shader content;
            uniform float uThreshold;
            uniform float uSoftness;
            uniform float uDespill;
            uniform float uYellowTint;
            uniform float uGreenKeyStrength;

            half4 main(float2 coord) {
                float4 color = float4(content.eval(coord));
                float blueMax = max(color.r, color.g);
                float greenMax = max(color.r, color.b);
                float blueDominance = color.b - blueMax;
                float greenDominanceKey = color.g - greenMax;
                float dominance = mix(
                    blueDominance,
                    greenDominanceKey,
                    uGreenKeyStrength
                );
                float keyChannel = mix(
                    color.b,
                    color.g,
                    uGreenKeyStrength
                );
                float chromaKey = smoothstep(
                    uThreshold,
                    uThreshold + uSoftness,
                    dominance
                );
                float brightness = smoothstep(
                    0.18,
                    0.42,
                    keyChannel
                );
                float key = clamp(
                    chromaKey * brightness,
                    0.0,
                    1.0
                );
                float3 clean = color.rgb;
                float neutralBlue =
                    blueMax + 0.04;
                float neutralGreen =
                    greenMax + 0.04;
                clean.b = mix(
                    clean.b,
                    min(
                        clean.b,
                        neutralBlue
                    ),
                    key *
                        uDespill *
                        (
                            1.0 -
                            uGreenKeyStrength
                        )
                );
                clean.g = mix(
                    clean.g,
                    min(
                        clean.g,
                        neutralGreen
                    ),
                    key *
                        uDespill *
                        uGreenKeyStrength
                );
                float greenDominance =
                    max(
                        clean.g -
                            max(
                                clean.r,
                                clean.b
                            ),
                        0.0
                    );
                float greenMask =
                    smoothstep(
                        0.04,
                        0.34,
                        greenDominance
                    ) *
                    uYellowTint;
                float3 yellowized =
                    float3(
                        max(
                            clean.r,
                            clean.g *
                                0.95
                        ),
                        clean.g,
                        clean.b *
                            0.25
                    );
                clean = mix(
                    clean,
                    yellowized,
                    greenMask
                );
                float alpha =
                    color.a *
                    (
                        1.0 -
                        key
                    );
                return half4(
                    clean * alpha,
                    alpha
                );
            }
            """
    }
}

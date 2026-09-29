package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

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

    private var inputSurfaceTexture:
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

    private var firstFrameDelivered =
        false

    private var firstFrameHeld =
        false

    private val glPipeline =
        TextureGlPipeline(
            onInputSurfaceReady = {
                    texture ->
                post {
                    inputSurfaceTexture =
                        texture
                    startPendingPlayback()
                }
            },
            onFirstFrameRendered = {
                    generation ->
                post {
                    handleFirstFrameRendered(
                        generation
                    )
                }
            },
            onError = {
                    message ->
                post {
                    MediaTrace.event(
                        source = traceSource(),
                        event =
                            "TEXTURE_GL_ERROR",
                        detail = message
                    )

                    val request =
                        activeRequest
                            ?: pendingPlayback

                    pendingPlayback = null
                    activeRequest = null

                    request
                        ?.onError
                        ?.invoke(message)
                }
            }
        )

    init {
        isOpaque = false
        alpha = 0f
        isClickable = false
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_NO

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

                    glPipeline.attachOutput(
                        surface,
                        width,
                        height
                    )
                }

                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    glPipeline.resize(
                        width,
                        height
                    )
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

                    inputSurfaceTexture =
                        null

                    stopPlayback()
                    glPipeline.detachOutput()

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
                    // Rendering is driven by the dedicated GL thread.
                }
            }

        MediaTrace.event(
            source = traceSource(),
            event =
                "TEXTURE_BACKEND_READY",
            detail =
                "backend=TextureView+OpenGL"
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
                "backend=TextureView+OpenGL muted=" +
                    muted +
                    " hadPlayer=" +
                    (player != null) +
                    " hadPending=" +
                    (pendingPlayback != null)
        )

        stopPlayback()

        activeGeneration += 1L
        this.muted = muted
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

        glPipeline.beginPlayback(
            generation =
                activeGeneration
        )

        if (revealOnFirstFrame) {
            MediaTrace.event(
                source = traceSource(),
                event =
                    "VIDEO_VISIBILITY_ARMED",
                assetPath = assetPath,
                detail =
                    "backend=TextureView+OpenGL alpha=0 generation=" +
                        activeGeneration
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
                    "backend=TextureView+OpenGL heldFirstFrame=true alpha=1"
            )
            true
        } catch (
            _: Throwable
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
        glPipeline.setYellowTint(
            enabled
        )
    }

    override fun setKeyColor(
        color: ChromaKeyColor
    ) {
        glPipeline.setKeyColor(
            color
        )
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
                    "backend=TextureView+OpenGL player=" +
                        (player != null) +
                        " pending=" +
                        (pendingAsset != null)
            )
        }

        pendingPlayback = null
        activeRequest = null
        firstFrameDelivered = false
        firstFrameHeld = false
        alpha = 0f

        glPipeline.cancelFirstFrame(
            activeGeneration
        )

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
                // The player may already be stopped or released.
            }

            current.release()

            AudioCapturePolicy.log(
                source = "VIDEO",
                detail =
                    "STOP_RELEASE backend=TextureView+OpenGL"
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
                "backend=TextureView+OpenGL"
        )

        stopPlayback()
        inputSurfaceTexture = null
        glPipeline.release()
    }

    private fun startPendingPlayback() {
        val request =
            pendingPlayback
                ?: return

        val texture =
            inputSurfaceTexture
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

            val inputSurface =
                Surface(texture)

            mediaPlayer.setSurface(
                inputSurface
            )
            inputSurface.release()

            mediaPlayer
                .setOnVideoSizeChangedListener {
                        _,
                        width,
                        height ->

                    glPipeline.setVideoSize(
                        width,
                        height
                    )
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
                                " backend=TextureView+OpenGL"
                    )

                    MediaTrace.event(
                        source = traceSource(),
                        event = "START",
                        assetPath =
                            request.assetPath,
                        detail =
                            "backend=TextureView+OpenGL muted=" +
                                muted
                    )

                    try {
                        prepared.start()
                        request.onStarted()
                    } catch (
                        error: Throwable
                    ) {
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
                        glPipeline.armFirstFrame(
                            request.generation
                        )

                        MediaTrace.event(
                            source = traceSource(),
                            event =
                                "VIDEO_RENDERING_START_SIGNAL",
                            assetPath =
                                request.assetPath,
                            detail =
                                "backend=TextureView+OpenGL extra=" +
                                    extra +
                                    " generation=" +
                                    request.generation
                        )
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
                            "backend=TextureView+OpenGL"
                    )

                    player = null
                    activeRequest = null
                    activeAssetPath = null
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
                            "backend=TextureView+OpenGL what=" +
                                what +
                                " extra=" +
                                extra
                    )

                    activeRequest = null
                    activeAssetPath = null

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

    private fun handleFirstFrameRendered(
        generation: Long
    ) {
        val request =
            activeRequest
                ?: return

        if (
            generation !=
            activeGeneration ||
            generation !=
            request.generation ||
            firstFrameDelivered
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
                "backend=TextureView+OpenGL glFrameDrawn=true hold=" +
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
                    "backend=TextureView+OpenGL alpha=0"
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
                    "backend=TextureView+OpenGL alpha=1"
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
        firstFrameHeld = false

        MediaTrace.event(
            source = traceSource(),
            event = "EXCEPTION",
            assetPath =
                request.assetPath,
            detail =
                "backend=TextureView+OpenGL " +
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
                        " backend=TextureView+OpenGL"
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

    private fun traceSource():
        String =
        "ChromaTextureGL@" +
            Integer.toHexString(
                System.identityHashCode(
                    this
                )
            ) +
            "[" +
            logicalLayer +
            "]"

    private class TextureGlPipeline(
        private val onInputSurfaceReady:
            (SurfaceTexture) -> Unit,
        private val onFirstFrameRendered:
            (Long) -> Unit,
        private val onError:
            (String) -> Unit
    ) {
        private val thread =
            HandlerThread(
                "GeckoDokuTextureGL"
            ).apply {
                start()
            }

        private val handler =
            Handler(
                thread.looper
            )

        private var released = false

        private var eglDisplay:
            EGLDisplay =
            EGL14.EGL_NO_DISPLAY

        private var eglContext:
            EGLContext =
            EGL14.EGL_NO_CONTEXT

        private var eglSurface:
            EGLSurface =
            EGL14.EGL_NO_SURFACE

        private var eglConfig:
            EGLConfig? = null

        private var outputSurface:
            SurfaceTexture? = null

        private var inputTextureId = 0

        private var inputTexture:
            SurfaceTexture? = null

        private var program = 0

        private var width = 1
        private var height = 1

        private var videoWidth = 1
        private var videoHeight = 1

        private var greenKeyStrength =
            ChromaKeyColor.BLUE
                .greenStrength

        private var yellowTintStrength =
            0f

        private var producedSerial =
            0L

        private var firstFrameGeneration =
            0L

        private var firstFrameBaseline =
            0L

        private var firstFrameArmed =
            false

        private val textureMatrix =
            FloatArray(16)

        private val vertexBuffer:
            FloatBuffer =
            ByteBuffer
                .allocateDirect(
                    8 * 4
                )
                .order(
                    ByteOrder.nativeOrder()
                )
                .asFloatBuffer()

        private val textureBuffer:
            FloatBuffer =
            ByteBuffer
                .allocateDirect(
                    8 * 4
                )
                .order(
                    ByteOrder.nativeOrder()
                )
                .asFloatBuffer()
                .apply {
                    put(
                        MediaRenderGeometry
                            .textureCoordinates()
                    )
                    position(0)
                }

        fun attachOutput(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            handler.post {
                if (released) {
                    return@post
                }

                try {
                    destroyEgl()
                    outputSurface = surface
                    this.width =
                        width.coerceAtLeast(1)
                    this.height =
                        height.coerceAtLeast(1)

                    createEgl(
                        surface
                    )
                    createGlObjects()
                    clearOutput()

                    inputTexture
                        ?.let(
                            onInputSurfaceReady
                        )
                } catch (
                    error: Throwable
                ) {
                    onError(
                        "TextureView GL init failed: " +
                            (
                                error.message
                                    ?: error.javaClass
                                        .simpleName
                                )
                    )
                }
            }
        }

        fun detachOutput() {
            handler.post {
                if (released) {
                    return@post
                }

                destroyEgl()
                outputSurface = null
            }
        }

        fun resize(
            width: Int,
            height: Int
        ) {
            handler.post {
                this.width =
                    width.coerceAtLeast(1)
                this.height =
                    height.coerceAtLeast(1)
                updateVertexBuffer()
                renderCurrentFrame()
            }
        }

        fun setVideoSize(
            width: Int,
            height: Int
        ) {
            handler.post {
                videoWidth =
                    width.coerceAtLeast(1)
                videoHeight =
                    height.coerceAtLeast(1)
                updateVertexBuffer()
                renderCurrentFrame()
            }
        }

        fun setYellowTint(
            enabled: Boolean
        ) {
            handler.post {
                yellowTintStrength =
                    if (enabled) {
                        1f
                    } else {
                        0f
                    }

                renderCurrentFrame()
            }
        }

        fun setKeyColor(
            color: ChromaKeyColor
        ) {
            handler.post {
                greenKeyStrength =
                    color.greenStrength

                renderCurrentFrame()
            }
        }

        fun beginPlayback(
            generation: Long
        ) {
            handler.post {
                firstFrameGeneration =
                    generation
                firstFrameArmed =
                    false
                firstFrameBaseline =
                    producedSerial

                clearOutput()
            }
        }

        fun armFirstFrame(
            generation: Long
        ) {
            handler.post {
                if (
                    generation !=
                    firstFrameGeneration
                ) {
                    return@post
                }

                firstFrameBaseline =
                    producedSerial
                firstFrameArmed =
                    true
            }
        }

        fun cancelFirstFrame(
            generation: Long
        ) {
            handler.post {
                if (
                    generation ==
                    firstFrameGeneration
                ) {
                    firstFrameArmed =
                        false
                }
            }
        }

        fun release() {
            handler.post {
                if (released) {
                    return@post
                }

                released = true
                destroyEgl()
                thread.quitSafely()
            }
        }

        private fun createEgl(
            surface: SurfaceTexture
        ) {
            eglDisplay =
                EGL14.eglGetDisplay(
                    EGL14.EGL_DEFAULT_DISPLAY
                )

            if (
                eglDisplay ==
                EGL14.EGL_NO_DISPLAY
            ) {
                error(
                    "eglGetDisplay failed"
                )
            }

            val version =
                IntArray(2)

            if (
                !EGL14.eglInitialize(
                    eglDisplay,
                    version,
                    0,
                    version,
                    1
                )
            ) {
                error(
                    "eglInitialize failed"
                )
            }

            val configAttributes =
                intArrayOf(
                    EGL14.EGL_RED_SIZE,
                    8,
                    EGL14.EGL_GREEN_SIZE,
                    8,
                    EGL14.EGL_BLUE_SIZE,
                    8,
                    EGL14.EGL_ALPHA_SIZE,
                    8,
                    EGL14.EGL_RENDERABLE_TYPE,
                    EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_SURFACE_TYPE,
                    EGL14.EGL_WINDOW_BIT,
                    EGL14.EGL_NONE
                )

            val configs =
                arrayOfNulls<EGLConfig>(
                    1
                )

            val count =
                IntArray(1)

            if (
                !EGL14.eglChooseConfig(
                    eglDisplay,
                    configAttributes,
                    0,
                    configs,
                    0,
                    1,
                    count,
                    0
                ) ||
                count[0] <= 0
            ) {
                error(
                    "eglChooseConfig failed"
                )
            }

            eglConfig =
                configs[0]
                    ?: error(
                        "No EGLConfig"
                    )

            val contextAttributes =
                intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION,
                    2,
                    EGL14.EGL_NONE
                )

            eglContext =
                EGL14.eglCreateContext(
                    eglDisplay,
                    eglConfig,
                    EGL14.EGL_NO_CONTEXT,
                    contextAttributes,
                    0
                )

            if (
                eglContext ==
                EGL14.EGL_NO_CONTEXT
            ) {
                error(
                    "eglCreateContext failed"
                )
            }

            val surfaceAttributes =
                intArrayOf(
                    EGL14.EGL_NONE
                )

            eglSurface =
                EGL14.eglCreateWindowSurface(
                    eglDisplay,
                    eglConfig,
                    surface,
                    surfaceAttributes,
                    0
                )

            if (
                eglSurface ==
                EGL14.EGL_NO_SURFACE
            ) {
                error(
                    "eglCreateWindowSurface failed"
                )
            }

            if (
                !EGL14.eglMakeCurrent(
                    eglDisplay,
                    eglSurface,
                    eglSurface,
                    eglContext
                )
            ) {
                error(
                    "eglMakeCurrent failed"
                )
            }

            EGLExt.eglPresentationTimeANDROID(
                eglDisplay,
                eglSurface,
                0L
            )
        }

        private fun createGlObjects() {
            program =
                createProgram(
                    VERTEX_SHADER,
                    FRAGMENT_SHADER
                )

            if (program == 0) {
                error(
                    "TextureView chroma shader failed"
                )
            }

            inputTextureId =
                createExternalTexture()

            if (
                inputTextureId == 0
            ) {
                error(
                    "TextureView external texture failed"
                )
            }

            val texture =
                SurfaceTexture(
                    inputTextureId
                )

            texture.setOnFrameAvailableListener(
                {
                    producedSerial += 1L
                    renderFreshFrame()
                },
                handler
            )

            inputTexture = texture

            GLES20.glClearColor(
                0f,
                0f,
                0f,
                0f
            )
            GLES20.glDisable(
                GLES20.GL_DEPTH_TEST
            )
            GLES20.glEnable(
                GLES20.GL_BLEND
            )
            GLES20.glBlendFunc(
                GLES20.GL_SRC_ALPHA,
                GLES20.GL_ONE_MINUS_SRC_ALPHA
            )

            updateVertexBuffer()
        }

        private fun renderFreshFrame() {
            if (!makeCurrent()) {
                return
            }

            val texture =
                inputTexture
                    ?: return

            try {
                texture.updateTexImage()
                texture.getTransformMatrix(
                    textureMatrix
                )
            } catch (
                _: Throwable
            ) {
                return
            }

            drawFrame()

            if (
                EGL14.eglSwapBuffers(
                    eglDisplay,
                    eglSurface
                )
            ) {
                if (
                    firstFrameArmed &&
                    producedSerial >
                    firstFrameBaseline
                ) {
                    firstFrameArmed =
                        false

                    onFirstFrameRendered(
                        firstFrameGeneration
                    )
                }
            }
        }

        private fun renderCurrentFrame() {
            if (
                inputTexture == null ||
                program == 0 ||
                !makeCurrent()
            ) {
                return
            }

            drawFrame()
            EGL14.eglSwapBuffers(
                eglDisplay,
                eglSurface
            )
        }

        private fun drawFrame() {
            GLES20.glViewport(
                0,
                0,
                width,
                height
            )

            GLES20.glClear(
                GLES20.GL_COLOR_BUFFER_BIT
            )

            GLES20.glUseProgram(
                program
            )

            val positionHandle =
                GLES20.glGetAttribLocation(
                    program,
                    "aPosition"
                )

            val textureHandle =
                GLES20.glGetAttribLocation(
                    program,
                    "aTexCoord"
                )

            val matrixHandle =
                GLES20.glGetUniformLocation(
                    program,
                    "uTexMatrix"
                )

            val thresholdHandle =
                GLES20.glGetUniformLocation(
                    program,
                    "uThreshold"
                )

            val softnessHandle =
                GLES20.glGetUniformLocation(
                    program,
                    "uSoftness"
                )

            val despillHandle =
                GLES20.glGetUniformLocation(
                    program,
                    "uDespill"
                )

            val yellowTintHandle =
                GLES20.glGetUniformLocation(
                    program,
                    "uYellowTint"
                )

            val greenKeyHandle =
                GLES20.glGetUniformLocation(
                    program,
                    "uGreenKeyStrength"
                )

            GLES20.glActiveTexture(
                GLES20.GL_TEXTURE0
            )

            GLES20.glBindTexture(
                GLES11Ext
                    .GL_TEXTURE_EXTERNAL_OES,
                inputTextureId
            )

            vertexBuffer.position(0)
            textureBuffer.position(0)

            GLES20.glEnableVertexAttribArray(
                positionHandle
            )
            GLES20.glVertexAttribPointer(
                positionHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                0,
                vertexBuffer
            )

            GLES20.glEnableVertexAttribArray(
                textureHandle
            )
            GLES20.glVertexAttribPointer(
                textureHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                0,
                textureBuffer
            )

            GLES20.glUniformMatrix4fv(
                matrixHandle,
                1,
                false,
                textureMatrix,
                0
            )

            GLES20.glUniform1f(
                thresholdHandle,
                AssetMediaCatalog
                    .KEY_THRESHOLD
            )

            GLES20.glUniform1f(
                softnessHandle,
                AssetMediaCatalog
                    .KEY_SOFTNESS
            )

            GLES20.glUniform1f(
                despillHandle,
                AssetMediaCatalog
                    .KEY_DESPILL
            )

            GLES20.glUniform1f(
                yellowTintHandle,
                yellowTintStrength
            )

            GLES20.glUniform1f(
                greenKeyHandle,
                greenKeyStrength
            )

            GLES20.glDrawArrays(
                GLES20.GL_TRIANGLE_STRIP,
                0,
                4
            )

            GLES20.glDisableVertexAttribArray(
                positionHandle
            )
            GLES20.glDisableVertexAttribArray(
                textureHandle
            )

            GLES20.glBindTexture(
                GLES11Ext
                    .GL_TEXTURE_EXTERNAL_OES,
                0
            )
        }

        private fun clearOutput() {
            if (!makeCurrent()) {
                return
            }

            GLES20.glViewport(
                0,
                0,
                width,
                height
            )
            GLES20.glClearColor(
                0f,
                0f,
                0f,
                0f
            )
            GLES20.glClear(
                GLES20.GL_COLOR_BUFFER_BIT
            )

            EGL14.eglSwapBuffers(
                eglDisplay,
                eglSurface
            )
        }

        private fun updateVertexBuffer() {
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

            vertexBuffer.clear()
            vertexBuffer.put(
                floatArrayOf(
                    -scaleX,
                    -scaleY,
                    scaleX,
                    -scaleY,
                    -scaleX,
                    scaleY,
                    scaleX,
                    scaleY
                )
            )
            vertexBuffer.position(0)
        }

        private fun makeCurrent():
            Boolean {
            if (
                eglDisplay ==
                EGL14.EGL_NO_DISPLAY ||
                eglContext ==
                EGL14.EGL_NO_CONTEXT ||
                eglSurface ==
                EGL14.EGL_NO_SURFACE
            ) {
                return false
            }

            return EGL14.eglMakeCurrent(
                eglDisplay,
                eglSurface,
                eglSurface,
                eglContext
            )
        }

        private fun destroyEgl() {
            if (
                eglDisplay !=
                EGL14.EGL_NO_DISPLAY
            ) {
                try {
                    EGL14.eglMakeCurrent(
                        eglDisplay,
                        EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_CONTEXT
                    )
                } catch (
                    _: Throwable
                ) {
                    // Ignore teardown failures.
                }
            }

            try {
                inputTexture
                    ?.setOnFrameAvailableListener(
                        null
                    )
                inputTexture
                    ?.release()
            } catch (
                _: Throwable
            ) {
                // Ignore release failures.
            }

            inputTexture = null

            if (
                inputTextureId != 0 &&
                eglDisplay !=
                EGL14.EGL_NO_DISPLAY
            ) {
                try {
                    makeCurrent()
                    GLES20.glDeleteTextures(
                        1,
                        intArrayOf(
                            inputTextureId
                        ),
                        0
                    )
                } catch (
                    _: Throwable
                ) {
                    // Ignore GL teardown failures.
                }
            }

            inputTextureId = 0

            if (
                program != 0 &&
                eglDisplay !=
                EGL14.EGL_NO_DISPLAY
            ) {
                try {
                    makeCurrent()
                    GLES20.glDeleteProgram(
                        program
                    )
                } catch (
                    _: Throwable
                ) {
                    // Ignore GL teardown failures.
                }
            }

            program = 0

            if (
                eglDisplay !=
                EGL14.EGL_NO_DISPLAY &&
                eglSurface !=
                EGL14.EGL_NO_SURFACE
            ) {
                EGL14.eglDestroySurface(
                    eglDisplay,
                    eglSurface
                )
            }

            if (
                eglDisplay !=
                EGL14.EGL_NO_DISPLAY &&
                eglContext !=
                EGL14.EGL_NO_CONTEXT
            ) {
                EGL14.eglDestroyContext(
                    eglDisplay,
                    eglContext
                )
            }

            if (
                eglDisplay !=
                EGL14.EGL_NO_DISPLAY
            ) {
                EGL14.eglTerminate(
                    eglDisplay
                )
            }

            eglDisplay =
                EGL14.EGL_NO_DISPLAY
            eglContext =
                EGL14.EGL_NO_CONTEXT
            eglSurface =
                EGL14.EGL_NO_SURFACE
            eglConfig = null
        }

        private fun createExternalTexture():
            Int {
            val textures =
                IntArray(1)

            GLES20.glGenTextures(
                1,
                textures,
                0
            )

            val texture =
                textures[0]

            GLES20.glBindTexture(
                GLES11Ext
                    .GL_TEXTURE_EXTERNAL_OES,
                texture
            )

            GLES20.glTexParameteri(
                GLES11Ext
                    .GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )

            GLES20.glTexParameteri(
                GLES11Ext
                    .GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )

            GLES20.glTexParameteri(
                GLES11Ext
                    .GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )

            GLES20.glTexParameteri(
                GLES11Ext
                    .GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )

            return texture
        }

        private fun createProgram(
            vertexSource: String,
            fragmentSource: String
        ): Int {
            val vertex =
                compileShader(
                    GLES20.GL_VERTEX_SHADER,
                    vertexSource
                )

            if (vertex == 0) {
                return 0
            }

            val fragment =
                compileShader(
                    GLES20.GL_FRAGMENT_SHADER,
                    fragmentSource
                )

            if (fragment == 0) {
                GLES20.glDeleteShader(
                    vertex
                )
                return 0
            }

            val result =
                GLES20.glCreateProgram()

            GLES20.glAttachShader(
                result,
                vertex
            )
            GLES20.glAttachShader(
                result,
                fragment
            )
            GLES20.glLinkProgram(
                result
            )

            val linked =
                IntArray(1)

            GLES20.glGetProgramiv(
                result,
                GLES20.GL_LINK_STATUS,
                linked,
                0
            )

            GLES20.glDeleteShader(
                vertex
            )
            GLES20.glDeleteShader(
                fragment
            )

            if (linked[0] == 0) {
                Log.e(
                    TAG,
                    "Program link failed: " +
                        GLES20.glGetProgramInfoLog(
                            result
                        )
                )
                GLES20.glDeleteProgram(
                    result
                )
                return 0
            }

            return result
        }

        private fun compileShader(
            type: Int,
            source: String
        ): Int {
            val shader =
                GLES20.glCreateShader(
                    type
                )

            GLES20.glShaderSource(
                shader,
                source
            )
            GLES20.glCompileShader(
                shader
            )

            val compiled =
                IntArray(1)

            GLES20.glGetShaderiv(
                shader,
                GLES20.GL_COMPILE_STATUS,
                compiled,
                0
            )

            if (compiled[0] == 0) {
                Log.e(
                    TAG,
                    "Shader compile failed: " +
                        GLES20.glGetShaderInfoLog(
                            shader
                        )
                )
                GLES20.glDeleteShader(
                    shader
                )
                return 0
            }

            return shader
        }

        companion object {
            private const val TAG =
                "GeckoDokuTextureGL"

            private const val VERTEX_SHADER =
                "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTexCoord;\n" +
                    "uniform mat4 uTexMatrix;\n" +
                    "varying vec2 vTexCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = aPosition;\n" +
                    "  vTexCoord = (uTexMatrix * aTexCoord).xy;\n" +
                    "}\n"

            private const val FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "uniform float uThreshold;\n" +
                    "uniform float uSoftness;\n" +
                    "uniform float uDespill;\n" +
                    "uniform float uYellowTint;\n" +
                    "uniform float uGreenKeyStrength;\n" +
                    "varying vec2 vTexCoord;\n" +
                    "void main() {\n" +
                    "  vec4 color = texture2D(sTexture, vTexCoord);\n" +
                    "  float blueMax = max(color.r, color.g);\n" +
                    "  float greenMax = max(color.r, color.b);\n" +
                    "  float blueDominance = color.b - blueMax;\n" +
                    "  float greenDominanceKey = color.g - greenMax;\n" +
                    "  float dominance = mix(blueDominance, greenDominanceKey, uGreenKeyStrength);\n" +
                    "  float keyChannel = mix(color.b, color.g, uGreenKeyStrength);\n" +
                    "  float chromaKey = smoothstep(uThreshold, uThreshold + uSoftness, dominance);\n" +
                    "  float brightness = smoothstep(0.18, 0.42, keyChannel);\n" +
                    "  float key = clamp(chromaKey * brightness, 0.0, 1.0);\n" +
                    "  vec3 clean = color.rgb;\n" +
                    "  float neutralBlue = blueMax + 0.04;\n" +
                    "  float neutralGreen = greenMax + 0.04;\n" +
                    "  clean.b = mix(clean.b, min(clean.b, neutralBlue), key * uDespill * (1.0 - uGreenKeyStrength));\n" +
                    "  clean.g = mix(clean.g, min(clean.g, neutralGreen), key * uDespill * uGreenKeyStrength);\n" +
                    "  float greenDominance = max(clean.g - max(clean.r, clean.b), 0.0);\n" +
                    "  float greenMask = smoothstep(0.04, 0.34, greenDominance) * uYellowTint;\n" +
                    "  vec3 yellowized = vec3(max(clean.r, clean.g * 0.95), clean.g, clean.b * 0.25);\n" +
                    "  clean = mix(clean, yellowized, greenMask);\n" +
                    "  float alpha = color.a * (1.0 - key);\n" +
                    "  gl_FragColor = vec4(clean, alpha);\n" +
                    "}\n"
        }
    }
}

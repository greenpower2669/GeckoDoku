package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ChromaKeyVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {
    private val chromaRenderer =
        ChromaRenderer(
            requestFrame = {
                requestRender()
            },
            surfaceReady = {
                surfaceTexture ->
                post {
                    mediaSurfaceTexture =
                        surfaceTexture
                    startPendingPlayback()
                }
            },
            firstFrameRendered = {
                post {
                    handleFirstFrameRendered()
                }
            },
            rendererError = {
                message ->
                post {
                    rendererFailure = message
                    pendingPlayback?.onError
                        ?.invoke(message)
                    pendingPlayback = null
                }
            }
        )

    private var mediaSurfaceTexture:
        SurfaceTexture? = null

    private var player:
        MediaPlayer? = null

    private var pendingPlayback:
        PlaybackRequest? = null

    private var rendererFailure:
        String? = null

    private var muted = false

    private var activeAssetPath:
        String? = null

    private val firstFrameGate =
        FirstFrameVisibilityGate()

    private var revealOnFirstFrame =
        false

    private var firstFrameCallback:
        (() -> Unit)? = null

    private var firstFrameAssetPath:
        String? = null

    var logicalLayer: String =
        "UNSPECIFIED"

    private val layerPolicy =
        VideoSurfaceLayerPolicy()

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(
            8,
            8,
            8,
            8,
            16,
            0
        )
        holder.setFormat(
            PixelFormat.TRANSLUCENT
        )
        if (
            layerPolicy.useZOrderOnTop
        ) {
            setZOrderOnTop(true)
        }

        if (
            layerPolicy.useMediaOverlay
        ) {
            setZOrderMediaOverlay(true)
        }

        MediaTrace.event(
            source = traceSource(),
            event = "SURFACE_POLICY",
            detail =
                "onTop=" +
                    layerPolicy.useZOrderOnTop +
                    " mediaOverlay=" +
                    layerPolicy.useMediaOverlay +
                    " pixelFormat=TRANSLUCENT alphaBits=8"
        )

        setRenderer(chromaRenderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        preserveEGLContextOnPause = true
        isClickable = false
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    fun play(
        assetPath: String,
        muted: Boolean,
        onCompletion: () -> Unit,
        onError: (String) -> Unit,
        onStarted: () -> Unit = {},
        revealOnFirstFrame: Boolean = true,
        onFirstFrameRendered: () -> Unit = {}
    ) {
        MediaTrace.event(
            source =
                traceSource(),
            event = "PLAY_REQUEST",
            assetPath = assetPath,
            detail =
                "muted=" +
                    muted +
                    " hadPlayer=" +
                    (player != null) +
                    " hadPending=" +
                    (pendingPlayback != null)
        )

        stopPlayback()

        this.muted = muted
        this.revealOnFirstFrame =
            revealOnFirstFrame
        firstFrameCallback =
            onFirstFrameRendered
        firstFrameAssetPath =
            assetPath

        if (revealOnFirstFrame) {
            firstFrameGate.arm()
            alpha = 0f

            MediaTrace.event(
                source = traceSource(),
                event =
                    "VIDEO_VISIBILITY_ARMED",
                assetPath = assetPath,
                detail = "alpha=0"
            )

            queueEvent {
                chromaRenderer
                    .armFirstFrameNotification()
            }
        } else {
            firstFrameGate.reset()
            alpha = 1f

            queueEvent {
                chromaRenderer
                    .cancelFirstFrameNotification()
            }
        }

        val failure = rendererFailure

        if (failure != null) {
            onError(failure)
            return
        }

        pendingPlayback =
            PlaybackRequest(
                assetPath = assetPath,
                onCompletion =
                    onCompletion,
                onError = onError,
                onStarted = onStarted
            )

        startPendingPlayback()
    }

    fun setMuted(
        value: Boolean
    ) {
        MediaTrace.event(
            source =
                traceSource(),
            event = "SET_MUTED",
            assetPath =
                activeAssetPath,
            detail =
                "muted=" +
                    value
        )

        muted = value

        player?.setVolume(
            if (value) 0f else 1f,
            if (value) 0f else 1f
        )
    }

    fun stopPlayback() {
        abortFirstFrameReveal(
            "stop"
        )

        val pendingAsset =
            pendingPlayback
                ?.assetPath

        if (
            player != null ||
            pendingAsset != null ||
            activeAssetPath != null
        ) {
            MediaTrace.event(
                source =
                    traceSource(),
                event = "STOP",
                assetPath =
                    activeAssetPath
                        ?: pendingAsset,
                detail =
                    "player=" +
                        (player != null) +
                        " pending=" +
                        (pendingAsset != null)
            )
        }

        pendingPlayback = null

        val current = player
        player = null

        if (current != null) {
            try {
                current.setOnCompletionListener(null)
                current.setOnErrorListener(null)
                current.stop()
            } catch (_: Exception) {
                // Player can already be idle or released.
            }

            current.release()
        }

        activeAssetPath = null
    }

    fun release() {
        MediaTrace.event(
            source =
                traceSource(),
            event = "RELEASE",
            assetPath =
                activeAssetPath
        )

        stopPlayback()
        queueEvent {
            chromaRenderer.releaseSurfaceTexture()
        }
        mediaSurfaceTexture = null
    }

    private fun startPendingPlayback() {
        val request =
            pendingPlayback ?: return

        val texture =
            mediaSurfaceTexture ?: return

        pendingPlayback = null

        try {
            val mediaPlayer =
                MediaPlayer()

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

            mediaPlayer.setSurface(surface)
            surface.release()

            mediaPlayer.setOnVideoSizeChangedListener {
                    _,
                    width,
                    height ->

                queueEvent {
                    chromaRenderer.setVideoSize(
                        width,
                        height
                    )
                }
                requestRender()
            }

            mediaPlayer.setOnPreparedListener {
                activeAssetPath =
                    request.assetPath
                firstFrameGate.onPrepared()

                MediaTrace.event(
                    source =
                        traceSource(),
                    event = "START",
                    assetPath =
                        request.assetPath,
                    detail =
                        "muted=" +
                            muted
                )

                it.setVolume(
                    if (muted) 0f else 1f,
                    if (muted) 0f else 1f
                )
                it.start()
                firstFrameGate.onStarted()
                request.onStarted()
            }

            mediaPlayer.setOnInfoListener {
                    _,
                    what,
                    extra ->

                if (
                    what ==
                    MediaPlayer
                        .MEDIA_INFO_VIDEO_RENDERING_START
                ) {
                    MediaTrace.event(
                        source =
                            traceSource(),
                        event =
                            "VIDEO_RENDERING_START_SIGNAL",
                        assetPath =
                            request.assetPath,
                        detail =
                            "extra=" +
                                extra
                    )
                }

                false
            }

            mediaPlayer.setOnCompletionListener {
                if (player === it) {
                    MediaTrace.event(
                        source =
                            "ChromaKey@" +
                                Integer.toHexString(
                                    System.identityHashCode(
                                        this
                                    )
                                ),
                        event = "COMPLETE",
                        assetPath =
                            request.assetPath
                    )

                    player = null
                    activeAssetPath = null
                    abortFirstFrameReveal(
                        "complete_before_first_frame"
                    )
                    it.release()
                    request.onCompletion()
                }
            }

            mediaPlayer.setOnErrorListener {
                    failedPlayer,
                    what,
                    extra ->

                if (player === failedPlayer) {
                    player = null
                }

                failedPlayer.release()

                MediaTrace.event(
                    source =
                        traceSource(),
                    event = "ERROR",
                    assetPath =
                        request.assetPath,
                    detail =
                        "what=" +
                            what +
                            " extra=" +
                            extra
                )

                activeAssetPath = null
                abortFirstFrameReveal(
                    "media_error_before_first_frame"
                )

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
        } catch (error: Exception) {
            player?.release()
            player = null
            activeAssetPath = null
            abortFirstFrameReveal(
                "exception_before_first_frame"
            )

            MediaTrace.event(
                source =
                    traceSource(),
                event = "EXCEPTION",
                assetPath =
                    request.assetPath,
                detail =
                    error.message
            )

            request.onError(
                "Unable to play " +
                    request.assetPath +
                    ": " +
                    (error.message ?: "unknown error")
            )
        }
    }

    private fun handleFirstFrameRendered() {
        if (
            !revealOnFirstFrame ||
            !firstFrameGate.isArmed
        ) {
            return
        }

        firstFrameGate
            .onFirstFrameRendered()

        if (!firstFrameGate.isVisible) {
            return
        }

        val asset =
            firstFrameAssetPath
                ?: activeAssetPath

        MediaTrace.event(
            source = traceSource(),
            event = "VIDEO_FIRST_FRAME",
            assetPath = asset,
            detail = "glFrameDrawn=true"
        )

        alpha = 1f

        MediaTrace.event(
            source = traceSource(),
            event = "VIDEO_VISIBLE",
            assetPath = asset,
            detail = "alpha=1"
        )

        val callback =
            firstFrameCallback

        firstFrameCallback = null
        firstFrameAssetPath = null
        callback?.invoke()
    }

    private fun abortFirstFrameReveal(
        reason: String
    ) {
        if (firstFrameGate.isArmed) {
            firstFrameGate.abort()

            MediaTrace.event(
                source = traceSource(),
                event =
                    "VIDEO_ABORT_BEFORE_FIRST_FRAME",
                assetPath =
                    firstFrameAssetPath
                        ?: activeAssetPath,
                detail =
                    "reason=" +
                        reason
            )
        }

        firstFrameCallback = null
        firstFrameAssetPath = null

        queueEvent {
            chromaRenderer
                .cancelFirstFrameNotification()
        }
    }

    private fun traceSource(): String =
        "ChromaKey@" +
            Integer.toHexString(
                System.identityHashCode(
                    this
                )
            ) +
            "[" +
            logicalLayer +
            "]"

    private data class PlaybackRequest(
        val assetPath: String,
        val onCompletion: () -> Unit,
        val onError: (String) -> Unit,
        val onStarted: () -> Unit
    )

    private class ChromaRenderer(
        private val requestFrame: () -> Unit,
        private val surfaceReady:
            (SurfaceTexture) -> Unit,
        private val firstFrameRendered:
            () -> Unit,
        private val rendererError:
            (String) -> Unit
    ) : Renderer {
        private var program = 0
        private var externalTexture = 0
        private var surfaceTexture:
            SurfaceTexture? = null

        @Volatile
        private var frameAvailable = false

        private var firstFrameNotificationArmed =
            false

        private var viewWidth = 1
        private var viewHeight = 1
        private var videoWidth = 1
        private var videoHeight = 1

        private val textureMatrix =
            FloatArray(16)

        private val vertexBuffer:
            FloatBuffer =
            ByteBuffer
                .allocateDirect(8 * 4)
                .order(
                    ByteOrder.nativeOrder()
                )
                .asFloatBuffer()

        private val textureBuffer:
            FloatBuffer =
            ByteBuffer
                .allocateDirect(8 * 4)
                .order(
                    ByteOrder.nativeOrder()
                )
                .asFloatBuffer()
                .apply {
                    put(
                        MediaRenderGeometry.textureCoordinates()
                    )
                    position(0)
                }

        override fun onSurfaceCreated(
            gl: GL10?,
            config: EGLConfig?
        ) {
            program =
                createProgram(
                    VERTEX_SHADER,
                    FRAGMENT_SHADER
                )

            if (program == 0) {
                rendererError(
                    "Keycolor shader could not be created"
                )
                return
            }

            externalTexture =
                createExternalTexture()

            if (externalTexture == 0) {
                rendererError(
                    "External video texture unavailable"
                )
                return
            }

            val texture =
                SurfaceTexture(
                    externalTexture
                )

            texture.setOnFrameAvailableListener {
                frameAvailable = true
                requestFrame()
            }

            surfaceTexture = texture

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
            surfaceReady(texture)
        }

        override fun onSurfaceChanged(
            gl: GL10?,
            width: Int,
            height: Int
        ) {
            viewWidth =
                width.coerceAtLeast(1)
            viewHeight =
                height.coerceAtLeast(1)

            GLES20.glViewport(
                0,
                0,
                viewWidth,
                viewHeight
            )

            updateVertexBuffer()
        }

        override fun onDrawFrame(
            gl: GL10?
        ) {
            GLES20.glClear(
                GLES20.GL_COLOR_BUFFER_BIT
            )

            if (program == 0 ||
                externalTexture == 0
            ) {
                return
            }

            val texture =
                surfaceTexture ?: return

            var consumedFreshFrame =
                false

            if (frameAvailable) {
                try {
                    texture.updateTexImage()
                    texture.getTransformMatrix(
                        textureMatrix
                    )
                    frameAvailable = false
                    consumedFreshFrame = true
                } catch (_: Exception) {
                    return
                }
            }

            GLES20.glUseProgram(program)

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

            GLES20.glActiveTexture(
                GLES20.GL_TEXTURE0
            )
            GLES20.glBindTexture(
                GLES11Ext
                    .GL_TEXTURE_EXTERNAL_OES,
                externalTexture
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

            if (
                consumedFreshFrame &&
                firstFrameNotificationArmed
            ) {
                firstFrameNotificationArmed =
                    false
                firstFrameRendered()
            }
        }

        fun armFirstFrameNotification() {
            firstFrameNotificationArmed =
                true
        }

        fun cancelFirstFrameNotification() {
            firstFrameNotificationArmed =
                false
        }

        fun setVideoSize(
            width: Int,
            height: Int
        ) {
            videoWidth =
                width.coerceAtLeast(1)
            videoHeight =
                height.coerceAtLeast(1)
            updateVertexBuffer()
        }

        fun releaseSurfaceTexture() {
            surfaceTexture?.release()
            surfaceTexture = null
        }

        private fun updateVertexBuffer() {
            val viewAspect =
                viewWidth.toFloat() /
                    viewHeight.toFloat()

            val videoAspect =
                videoWidth.toFloat() /
                    videoHeight.toFloat()

            val scaleX: Float
            val scaleY: Float

            if (videoAspect > viewAspect) {
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
                    -scaleX, -scaleY,
                    scaleX, -scaleY,
                    -scaleX, scaleY,
                    scaleX, scaleY
                )
            )
            vertexBuffer.position(0)
        }

        private fun createExternalTexture(): Int {
            val textures =
                IntArray(1)

            GLES20.glGenTextures(
                1,
                textures,
                0
            )

            val texture = textures[0]

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
                GLES20.glDeleteShader(vertex)
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
            GLES20.glLinkProgram(result)

            val linked =
                IntArray(1)

            GLES20.glGetProgramiv(
                result,
                GLES20.GL_LINK_STATUS,
                linked,
                0
            )

            GLES20.glDeleteShader(vertex)
            GLES20.glDeleteShader(fragment)

            if (linked[0] == 0) {
                Log.e(
                    TAG,
                    "Program link failed: " +
                        GLES20.glGetProgramInfoLog(
                            result
                        )
                )
                GLES20.glDeleteProgram(result)
                return 0
            }

            return result
        }

        private fun compileShader(
            type: Int,
            source: String
        ): Int {
            val shader =
                GLES20.glCreateShader(type)

            GLES20.glShaderSource(
                shader,
                source
            )
            GLES20.glCompileShader(shader)

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
                GLES20.glDeleteShader(shader)
                return 0
            }

            return shader
        }

        companion object {
            private const val TAG =
                "GeckoDokuKeycolor"

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
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "  vec4 color = texture2D(sTexture, vTexCoord);\n" +
                "  float maxRG = max(color.r, color.g);\n" +
                "  float dominance = color.b - maxRG;\n" +
                "  float blueKey = smoothstep(uThreshold, uThreshold + uSoftness, dominance);\n" +
                "  float brightness = smoothstep(0.18, 0.42, color.b);\n" +
                "  float key = clamp(blueKey * brightness, 0.0, 1.0);\n" +
                "  vec3 clean = color.rgb;\n" +
                "  float neutralBlue = maxRG + 0.04;\n" +
                "  clean.b = mix(clean.b, min(clean.b, neutralBlue), key * uDespill);\n" +
                "  float alpha = color.a * (1.0 - key);\n" +
                "  gl_FragColor = vec4(clean, alpha);\n" +
                "}\n"
        }
    }
}

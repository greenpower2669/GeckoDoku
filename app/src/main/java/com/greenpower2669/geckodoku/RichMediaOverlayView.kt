package com.greenpower2669.geckodoku

import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView

class RichMediaOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private data class ActiveSession(
        val id: String,
        val kind: RichMediaKind,
        val assetPath: String,
        val videoView:
            ChromaKeyVideoView,
        val maskView:
            View?,
        val titleText:
            String?,
        val skippable:
            Boolean,
        val onFinished:
            (() -> Unit)?,
        val onSkipped:
            (() -> Unit)?
    )

    private val sessions =
        linkedMapOf<String, ActiveSession>()

    private val playbackRegistry =
        RichMediaPlaybackRegistry()

    private var nextSessionId =
        0L

    private val titleView =
        TextView(context).apply {
            textSize = 36f
            gravity = Gravity.CENTER
            setTextColor(
                Color.rgb(246, 255, 231)
            )
            setShadowLayer(
                8f,
                0f,
                3f,
                Color.rgb(18, 80, 44)
            )
            importantForAccessibility =
                IMPORTANT_FOR_ACCESSIBILITY_NO
            visibility = GONE
        }

    private val closeButton =
        Button(context).apply {
            text = "×"
            textSize = 24f
            contentDescription =
                "Passer l'animation"
            minWidth = dp(56)
            minHeight = dp(56)
            visibility = GONE
            setOnClickListener {
                skipCurrentSession()
            }
        }

    val isBusy: Boolean
        get() = sessions.isNotEmpty()

    val activeCount: Int
        get() = sessions.size

    init {
        setBackgroundColor(
            Color.TRANSPARENT
        )
        visibility = GONE
        isClickable = false
        clipChildren = false
        clipToPadding = false

        addView(
            titleView,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                dp(92)
            ).apply {
                gravity = Gravity.TOP
                topMargin = dp(22)
                leftMargin = dp(76)
                rightMargin = dp(76)
            }
        )

        addView(
            closeButton,
            LayoutParams(
                dp(64),
                dp(64)
            ).apply {
                gravity =
                    Gravity.TOP or
                        Gravity.END
                topMargin = dp(12)
                rightMargin = dp(12)
            }
        )
    }

    fun hasActiveKind(
        kind: RichMediaKind
    ): Boolean =
        sessions.values.any {
            it.kind == kind
        }

    fun play(
        kind: RichMediaKind,
        assetPath: String,
        muted: Boolean,
        target: RectF?,
        titleText: String?,
        skippable: Boolean,
        maskTarget: RectF? = null,
        maskColor: Int = Color.WHITE,
        onFinished: (() -> Unit)? = null,
        onSkipped: (() -> Unit)? = null
    ): Boolean {
        val effectiveMuted =
            muted ||
                GeckoMediaAudioPolicy
                    .mustMute(kind)

        if (
            kind == RichMediaKind.INTRO &&
            hasActiveKind(
                RichMediaKind.INTRO
            )
        ) {
            MediaTrace.event(
                source = "Overlay",
                event =
                    "PLAY_REJECT_INTRO_ACTIVE",
                assetPath = assetPath,
                detail =
                    "activeCount=" +
                        activeCount
            )
            return false
        }

        val sessionId =
            "media-" +
                (++nextSessionId)

        val maskView =
            if (maskTarget != null) {
                View(context).apply {
                    setBackgroundColor(
                        maskColor
                    )
                    importantForAccessibility =
                        IMPORTANT_FOR_ACCESSIBILITY_NO
                    applyBounds(
                        this,
                        maskTarget
                    )
                }
            } else {
                null
            }

        val videoView =
            ChromaKeyVideoView(context)
                .apply {
                    logicalLayer =
                        "OVERLAY:" +
                            kind +
                            ":" +
                            sessionId
                    importantForAccessibility =
                        IMPORTANT_FOR_ACCESSIBILITY_NO
                    isClickable = false
                    applyBounds(
                        this,
                        target
                    )
                }

        val session =
            ActiveSession(
                id = sessionId,
                kind = kind,
                assetPath = assetPath,
                videoView = videoView,
                maskView = maskView,
                titleText = titleText,
                skippable = skippable,
                onFinished = onFinished,
                onSkipped = onSkipped
            )

        MediaTrace.event(
            source = "Overlay",
            event = "PLAY_ACCEPT",
            assetPath = assetPath,
            detail =
                "id=" +
                    sessionId +
                    " kind=" +
                    kind +
                    " requestedMuted=" +
                    muted +
                    " effectiveMuted=" +
                    effectiveMuted +
                    " activeBefore=" +
                    sessions.size
        )

        maskView?.let {
            addView(it)
        }

        addView(
            videoView
        )

        sessions[sessionId] =
            session

        playbackRegistry.started(
            id = sessionId,
            kind = kind
        )

        visibility = VISIBLE
        refreshChrome()

        videoView.play(
            assetPath = assetPath,
            muted = effectiveMuted,
            onStarted = {
                MediaTrace.event(
                    source = "Overlay",
                    event = "SESSION_STARTED",
                    assetPath =
                        assetPath,
                    detail =
                        "id=" +
                            sessionId +
                            " kind=" +
                            kind +
                            " activeCount=" +
                            activeCount
                )
            },
            onCompletion = {
                finishSession(
                    sessionId = sessionId,
                    invokeCompletion = true,
                    invokeSkipped = false
                )
            },
            onError = {
                message ->

                Log.w(
                    TAG,
                    message
                )

                finishSession(
                    sessionId = sessionId,
                    invokeCompletion = true,
                    invokeSkipped = false
                )
            }
        )

        return true
    }

    fun setMuted(
        value: Boolean
    ) {
        sessions.values.forEach {
            session ->

            val forcedMute =
                GeckoMediaAudioPolicy
                    .mustMute(
                        session.kind
                    )

            session.videoView
                .setMuted(
                    value ||
                        forcedMute
                )
        }
    }

    fun stop() {
        MediaTrace.event(
            source = "Overlay",
            event = "STOP_ALL_REQUEST",
            detail =
                "activeCount=" +
                    activeCount
        )

        sessions.keys
            .toList()
            .forEach {
                sessionId ->

                finishSession(
                    sessionId =
                        sessionId,
                    invokeCompletion =
                        false,
                    invokeSkipped =
                        false
                )
            }
    }

    fun stopKind(
        kind: RichMediaKind
    ) {
        sessions.values
            .filter {
                it.kind == kind
            }
            .map {
                it.id
            }
            .forEach {
                finishSession(
                    sessionId = it,
                    invokeCompletion = false,
                    invokeSkipped = false
                )
            }
    }

    fun release() {
        MediaTrace.event(
            source = "Overlay",
            event = "RELEASE_ALL",
            detail =
                "activeCount=" +
                    activeCount
        )

        stop()
        playbackRegistry.clear()
        visibility = GONE
    }

    private fun skipCurrentSession() {
        val session =
            sessions.values
                .lastOrNull {
                    it.skippable &&
                        it.kind ==
                            RichMediaKind.INTRO
                }
                ?: sessions.values
                    .lastOrNull {
                        it.skippable
                    }
                ?: return

        MediaTrace.event(
            source = "Overlay",
            event = "SKIP_REQUEST",
            assetPath =
                session.assetPath,
            detail =
                "id=" +
                    session.id +
                    " kind=" +
                    session.kind
        )

        finishSession(
            sessionId =
                session.id,
            invokeCompletion =
                false,
            invokeSkipped =
                true
        )
    }

    private fun finishSession(
        sessionId: String,
        invokeCompletion: Boolean,
        invokeSkipped: Boolean
    ) {
        val session =
            sessions.remove(
                sessionId
            ) ?: return

        playbackRegistry.completed(
            sessionId
        )

        MediaTrace.event(
            source = "Overlay",
            event =
                when {
                    invokeCompletion ->
                        "SESSION_COMPLETE"

                    invokeSkipped ->
                        "SESSION_SKIPPED"

                    else ->
                        "SESSION_STOPPED"
                },
            assetPath =
                session.assetPath,
            detail =
                "id=" +
                    session.id +
                    " kind=" +
                    session.kind +
                    " remaining=" +
                    sessions.size
        )

        session.videoView
            .release()

        removeView(
            session.videoView
        )

        session.maskView
            ?.let {
                removeView(it)
            }

        refreshChrome()

        when {
            invokeCompletion ->
                session.onFinished
                    ?.invoke()

            invokeSkipped ->
                session.onSkipped
                    ?.invoke()
        }
    }

    private fun refreshChrome() {
        val hasIntro =
            hasActiveKind(
                RichMediaKind.INTRO
            )

        setBackgroundColor(
            if (hasIntro) {
                Color.BLACK
            } else {
                Color.TRANSPARENT
            }
        )

        val titled =
            sessions.values
                .lastOrNull {
                    !it.titleText
                        .isNullOrBlank()
                }

        titleView.text =
            titled?.titleText ?: ""

        titleView.visibility =
            if (titled == null) {
                GONE
            } else {
                VISIBLE
            }

        val skippable =
            sessions.values
                .lastOrNull {
                    it.skippable
                }

        closeButton.visibility =
            if (skippable == null) {
                GONE
            } else {
                VISIBLE
            }

        contentDescription =
            if (skippable == null) {
                null
            } else {
                "Animation en cours. Passer l'animation avec la croix."
            }

        titleView.bringToFront()
        closeButton.bringToFront()

        visibility =
            if (sessions.isEmpty()) {
                GONE
            } else {
                VISIBLE
            }
    }

    private fun applyBounds(
        view: View,
        target: RectF?
    ) {
        val params =
            if (target == null) {
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            } else {
                LayoutParams(
                    target.width()
                        .toInt()
                        .coerceAtLeast(1),
                    target.height()
                        .toInt()
                        .coerceAtLeast(1)
                ).apply {
                    leftMargin =
                        target.left
                            .toInt()
                    topMargin =
                        target.top
                            .toInt()
                }
            }

        view.layoutParams =
            params
    }

    private fun dp(
        value: Int
    ): Int =
        (
            value *
                resources
                    .displayMetrics
                    .density
            ).toInt()

    companion object {
        private const val TAG =
            "GeckoDokuRichMedia"
    }
}

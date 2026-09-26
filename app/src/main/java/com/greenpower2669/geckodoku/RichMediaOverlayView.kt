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
    private val maskView =
        View(context).apply {
            visibility = GONE
            importantForAccessibility =
                IMPORTANT_FOR_ACCESSIBILITY_NO
        }

    private val videoView =
        ChromaKeyVideoView(context)

    private val titleView =
        TextView(context).apply {
            textSize = 36f
            gravity = Gravity.CENTER
            setTextColor(
                Color.rgb(246, 255, 231)
            )
            setShadowLayer(
                8f, 0f, 3f,
                Color.rgb(18, 80, 44)
            )
            importantForAccessibility =
                IMPORTANT_FOR_ACCESSIBILITY_NO
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
            setOnClickListener { stop() }
        }

    private var activeKind:
        RichMediaKind? = null

    private var activeAssetPath:
        String? = null

    private var activeCompletion:
        (() -> Unit)? = null

    val isBusy: Boolean
        get() = activeKind != null

    init {
        setBackgroundColor(Color.TRANSPARENT)
        visibility = GONE
        isClickable = false

        addView(
            maskView,
            LayoutParams(1, 1)
        )
        addView(
            videoView,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )
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
                    Gravity.TOP or Gravity.END
                topMargin = dp(12)
                rightMargin = dp(12)
            }
        )
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
        onFinished: (() -> Unit)? = null
    ): Boolean {
        val effectiveMuted =
            muted ||
                GeckoMediaAudioPolicy
                    .mustMute(kind)

        if (isBusy) {
            MediaTrace.event(
                source = "Overlay",
                event = "PLAY_REJECT_BUSY",
                assetPath = assetPath,
                detail =
                    "requestedKind=" +
                        kind +
                        " activeKind=" +
                        activeKind +
                        " activeAsset=" +
                        activeAssetPath
            )
            return false
        }

        MediaTrace.event(
            source = "Overlay",
            event = "PLAY_ACCEPT",
            assetPath = assetPath,
            detail =
                "kind=" +
                    kind +
                    " requestedMuted=" +
                    muted +
                    " effectiveMuted=" +
                    effectiveMuted +
                    " skippable=" +
                    skippable
        )

        activeKind = kind
        activeAssetPath = assetPath
        activeCompletion = onFinished

        setBackgroundColor(
            if (kind == RichMediaKind.INTRO) {
                Color.BLACK
            } else {
                Color.TRANSPARENT
            }
        )

        titleView.text = titleText ?: ""
        titleView.visibility =
            if (titleText.isNullOrBlank()) {
                GONE
            } else {
                VISIBLE
            }
        closeButton.visibility =
            if (skippable) VISIBLE else GONE

        contentDescription =
            if (skippable) {
                "Animation en cours. Passer l'animation avec la croix."
            } else {
                null
            }

        if (maskTarget != null) {
            maskView.setBackgroundColor(maskColor)
            applyBounds(maskView, maskTarget)
            maskView.visibility = VISIBLE
        } else {
            maskView.visibility = GONE
        }

        applyBounds(videoView, target)
        visibility = VISIBLE

        videoView.play(
            assetPath = assetPath,
            muted =
                effectiveMuted,
            onCompletion = {
                finishActive(true)
            },
            onError = { message ->
                Log.w(TAG, message)
                finishActive(true)
            }
        )
        return true
    }

    fun setMuted(value: Boolean) {
        val forcedMute =
            activeKind?.let {
                GeckoMediaAudioPolicy
                    .mustMute(it)
            } ?: false

        videoView.setMuted(
            value || forcedMute
        )
    }

    fun stop() {
        MediaTrace.event(
            source = "Overlay",
            event = "STOP_REQUEST",
            assetPath =
                activeAssetPath,
            detail =
                "activeKind=" +
                    activeKind
        )

        finishActive(false)
    }

    fun release() {
        MediaTrace.event(
            source = "Overlay",
            event = "RELEASE",
            assetPath =
                activeAssetPath,
            detail =
                "activeKind=" +
                    activeKind
        )

        activeCompletion = null
        activeKind = null
        activeAssetPath = null
        videoView.release()
        maskView.visibility = GONE
        setBackgroundColor(Color.TRANSPARENT)
        visibility = GONE
    }

    private fun finishActive(
        invokeCompletion: Boolean
    ) {
        if (!isBusy) return
        val completion = activeCompletion
        val finishedKind = activeKind
        val finishedAsset =
            activeAssetPath

        MediaTrace.event(
            source = "Overlay",
            event =
                if (invokeCompletion) {
                    "FINISH_COMPLETE"
                } else {
                    "FINISH_STOPPED"
                },
            assetPath =
                finishedAsset,
            detail =
                "kind=" +
                    finishedKind
        )

        activeCompletion = null
        activeKind = null
        activeAssetPath = null
        videoView.stopPlayback()
        titleView.text = ""
        titleView.visibility = GONE
        closeButton.visibility = GONE
        maskView.visibility = GONE
        setBackgroundColor(Color.TRANSPARENT)
        visibility = GONE
        if (invokeCompletion) {
            completion?.invoke()
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
                        target.left.toInt()
                    topMargin =
                        target.top.toInt()
                }
            }
        view.layoutParams = params
    }

    private fun dp(value: Int): Int =
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

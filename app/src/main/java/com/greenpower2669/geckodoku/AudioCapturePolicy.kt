package com.greenpower2669.geckodoku

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.util.Log

object AudioCapturePolicy {
    private const val TAG =
        "GeckoDokuAudio"

    fun applyApplicationPolicy(
        context: Context
    ) {
        val sdk =
            Build.VERSION.SDK_INT

        if (
            sdk >=
                Build.VERSION_CODES.Q
        ) {
            try {
                val manager =
                    context.getSystemService(
                        Context.AUDIO_SERVICE
                    ) as AudioManager

                manager.setAllowedCapturePolicy(
                    AudioAttributes
                        .ALLOW_CAPTURE_BY_ALL
                )

                log(
                    source =
                        "APP",
                    detail =
                        "sdk=" +
                            sdk +
                            " capture=ALLOW_ALL"
                )
            } catch (
                error: Throwable
            ) {
                log(
                    source =
                        "APP",
                    detail =
                        "sdk=" +
                            sdk +
                            " capture=ERROR " +
                            error
                                .javaClass
                                .simpleName
                )
            }
        } else {
            log(
                source =
                    "APP",
                detail =
                    "sdk=" +
                        sdk +
                        " capture=LEGACY"
            )
        }
    }

    fun speechAttributes():
        AudioAttributes =
        buildAttributes(
            usage =
                AudioAttributes
                    .USAGE_MEDIA,
            contentType =
                AudioAttributes
                    .CONTENT_TYPE_SPEECH
        )

    fun musicAttributes():
        AudioAttributes =
        buildAttributes(
            usage =
                AudioAttributes
                    .USAGE_MEDIA,
            contentType =
                AudioAttributes
                    .CONTENT_TYPE_MUSIC
        )

    fun gameAttributes():
        AudioAttributes =
        buildAttributes(
            usage =
                AudioAttributes
                    .USAGE_GAME,
            contentType =
                AudioAttributes
                    .CONTENT_TYPE_SONIFICATION
        )

    fun videoAttributes():
        AudioAttributes =
        buildAttributes(
            usage =
                AudioAttributes
                    .USAGE_MEDIA,
            contentType =
                AudioAttributes
                    .CONTENT_TYPE_MOVIE
        )

    fun log(
        source: String,
        detail: String
    ) {
        Log.i(
            TAG,
            "[AUDIO] source=" +
                source +
                " " +
                detail
        )

        MediaTrace.event(
            source =
                "AudioCapturePolicy",
            event =
                "AUDIO",
            detail =
                "source=" +
                    source +
                    " " +
                    detail
        )
    }

    private fun buildAttributes(
        usage: Int,
        contentType: Int
    ): AudioAttributes {
        val builder =
            AudioAttributes
                .Builder()
                .setUsage(
                    usage
                )
                .setContentType(
                    contentType
                )

        if (
            Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
        ) {
            builder.setAllowedCapturePolicy(
                AudioAttributes
                    .ALLOW_CAPTURE_BY_ALL
            )
        }

        return builder.build()
    }
}

package com.greenpower2669.geckodoku

import android.content.Context

class RichMediaSettings(
    context: Context
) {
    private val preferences =
        context.getSharedPreferences(
            "geckodoku_rich_media",
            Context.MODE_PRIVATE
        )

    init {
        MediaTrace.event(
            source = "RichMediaSettings",
            event = "LOAD_ENABLED",
            detail =
                "value=" +
                    preferences.getBoolean(
                        "animations_enabled",
                        true
                    ) +
                    " persisted=" +
                    preferences.contains(
                        "animations_enabled"
                    )
        )
    }

    var enabled: Boolean
        get() =
            preferences.getBoolean(
                "animations_enabled",
                true
            )
        set(value) {
            val previous =
                preferences.getBoolean(
                    "animations_enabled",
                    true
                )

            MediaTrace.event(
                source = "RichMediaSettings",
                event = "WRITE_ENABLED",
                detail =
                    "previous=" +
                        previous +
                        " new=" +
                        value
            )

            preferences.edit()
                .putBoolean(
                    "animations_enabled",
                    value
                )
                .apply()
        }
}

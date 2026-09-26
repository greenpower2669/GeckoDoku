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

    var enabled: Boolean
        get() =
            preferences.getBoolean(
                "animations_enabled",
                true
            )
        set(value) {
            preferences.edit()
                .putBoolean(
                    "animations_enabled",
                    value
                )
                .apply()
        }
}

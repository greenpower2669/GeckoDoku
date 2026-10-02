package com.greenpower2669.geckodoku

import android.content.Context

enum class SpriteResolution(
    val heightPx: Int,
    val label: String
) {
    P240(240, "240p");

    companion object {
        fun fromHeight(value: Int): SpriteResolution =
            values().firstOrNull { it.heightPx == value } ?: P240
    }
}

class RichMediaSettings(
    context: Context
) {
    private val preferences =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    init {
        MediaTrace.event(
            source = "RichMediaSettings",
            event = "LOAD_ENABLED",
            detail =
                "value=" +
                    preferences.getBoolean(KEY_ANIMATIONS_ENABLED, true) +
                    " persisted=" +
                    preferences.contains(KEY_ANIMATIONS_ENABLED) +
                    " spriteResolution=" +
                    spriteResolution.label +
                    " gomokuLimit3PerTeam=" +
                    limitGomokuAnimationsPerTeam
        )
    }

    var enabled: Boolean
        get() =
            preferences.getBoolean(
                KEY_ANIMATIONS_ENABLED,
                true
            )
        set(value) {
            val previous =
                preferences.getBoolean(
                    KEY_ANIMATIONS_ENABLED,
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
                    KEY_ANIMATIONS_ENABLED,
                    value
                )
                .apply()
        }

    var spriteResolution: SpriteResolution
        get() =
            SpriteResolution.P240
        set(value) {
            MediaTrace.event(
                source = "RichMediaSettings",
                event = "WRITE_SPRITE_RESOLUTION_IGNORED",
                detail =
                    "fixed=240p requested=" +
                        value.label
            )
        }

    var limitGomokuAnimationsPerTeam: Boolean
        get() =
            preferences.getBoolean(
                KEY_GOMOKU_LIMIT_3,
                false
            )
        set(value) {
            val previous =
                limitGomokuAnimationsPerTeam

            preferences.edit()
                .putBoolean(
                    KEY_GOMOKU_LIMIT_3,
                    value
                )
                .apply()

            MediaTrace.event(
                source = "RichMediaSettings",
                event = "WRITE_GOMOKU_LIMIT_3",
                detail =
                    "previous=" +
                        previous +
                        " new=" +
                        value
            )
        }

    companion object {
        private const val PREFS_NAME =
            "geckodoku_rich_media"
        private const val KEY_ANIMATIONS_ENABLED =
            "animations_enabled"
        private const val KEY_SPRITE_RESOLUTION =
            "sprite_resolution_height"
        private const val KEY_GOMOKU_LIMIT_3 =
            "gomoku_limit_animations_3_per_team"

        fun spriteResolutionFor(
            context: Context
        ): SpriteResolution =
            SpriteResolution.P240
    }
}

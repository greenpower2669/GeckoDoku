package com.greenpower2669.geckodoku

import android.content.Context

enum class SpriteResolution(
    val heightPx: Int,
    val label: String
) {
    P240(240, "240p"),
    P360(360, "360p"),
    P480(480, "480p");

    companion object {
        fun fromHeight(value: Int): SpriteResolution =
            values().firstOrNull { it.heightPx == value } ?: P480
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
            SpriteResolution.fromHeight(
                preferences.getInt(
                    KEY_SPRITE_RESOLUTION,
                    SpriteResolution.P480.heightPx
                )
            )
        set(value) {
            val previous = spriteResolution

            preferences.edit()
                .putInt(
                    KEY_SPRITE_RESOLUTION,
                    value.heightPx
                )
                .apply()

            MediaTrace.event(
                source = "RichMediaSettings",
                event = "WRITE_SPRITE_RESOLUTION",
                detail =
                    "previous=" +
                        previous.label +
                        " new=" +
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
        ): SpriteResolution {
            val preferences =
                context.getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )

            return SpriteResolution.fromHeight(
                preferences.getInt(
                    KEY_SPRITE_RESOLUTION,
                    SpriteResolution.P480.heightPx
                )
            )
        }
    }
}

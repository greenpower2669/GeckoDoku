package com.greenpower2669.geckodoku

import android.content.Context

class PlayerProfileStore(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    var playerName: String
        get() =
            prefs.getString(
                KEY_PLAYER_NAME,
                DEFAULT_PLAYER_NAME
            )
                ?.trim()
                ?.takeIf {
                    it.isNotEmpty()
                }
                ?: DEFAULT_PLAYER_NAME
        set(value) {
            val normalized =
                value.trim()
                    .take(32)
                    .ifBlank {
                        DEFAULT_PLAYER_NAME
                    }

            prefs.edit()
                .putString(
                    KEY_PLAYER_NAME,
                    normalized
                )
                .apply()
        }

    var soundEnabled: Boolean
        get() =
            prefs.getBoolean(
                KEY_SOUND_ENABLED,
                true
            )
        set(value) {
            prefs.edit()
                .putBoolean(
                    KEY_SOUND_ENABLED,
                    value
                )
                .apply()
        }

    companion object {
        const val PREFERENCES_NAME =
            "geckodoku_player_profile"

        const val DEFAULT_PLAYER_NAME =
            "GeckoTétu"

        private const val KEY_PLAYER_NAME =
            "player_name"

        private const val KEY_SOUND_ENABLED =
            "sound_enabled"
    }
}

package com.greenpower2669.geckodoku

import android.content.Context
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

/** L'identité du profil est locale et distincte de son pseudonyme public. */
data class GeckoPlayerProfile(
    val id: String,
    val name: String
)

class PlayerProfileStore(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    /** Les anciennes parties appartiennent au profil historique, sans migration destructrice. */
    val activeProfileId: String
        get() {
            val candidate =
                prefs.getString(KEY_ACTIVE_PROFILE, LEGACY_PROFILE_ID)
                    ?: LEGACY_PROFILE_ID
            return profiles().firstOrNull { it.id == candidate }
                ?.id ?: LEGACY_PROFILE_ID
        }

    fun profiles(): List<GeckoPlayerProfile> {
        val historical = GeckoPlayerProfile(
            LEGACY_PROFILE_ID,
            historicalName()
        )
        val extra =
            runCatching {
                val array = JSONArray(
                    prefs.getString(KEY_OTHER_PROFILES, "[]") ?: "[]"
                )
                buildList {
                    for (i in 0 until array.length()) {
                        val obj = array.optJSONObject(i) ?: continue
                        val id = obj.optString("id")
                        val name = obj.optString("name").trim().take(32)
                        if (
                            id.matches(Regex("[a-f0-9-]{36}")) &&
                            name.isNotBlank() &&
                            none { it.id == id }
                        ) {
                            add(GeckoPlayerProfile(id, name))
                        }
                    }
                }
            }.getOrDefault(emptyList())
        return listOf(historical) + extra
    }

    fun createProfile(name: String): GeckoPlayerProfile {
        val normalized = normalize(name)
        val profile = GeckoPlayerProfile(UUID.randomUUID().toString(), normalized)
        writeExtraProfiles(profiles().drop(1) + profile)
        prefs.edit().putString(KEY_ACTIVE_PROFILE, profile.id).apply()
        return profile
    }

    fun activateProfile(id: String): Boolean {
        if (profiles().none { it.id == id }) return false
        prefs.edit().putString(KEY_ACTIVE_PROFILE, id).apply()
        return true
    }

    var playerName: String
        get() = profiles()
            .firstOrNull { it.id == activeProfileId }
            ?.name ?: DEFAULT_PLAYER_NAME
        set(value) {
            val normalized = normalize(value)
            if (activeProfileId == LEGACY_PROFILE_ID) {
                prefs.edit().putString(KEY_PLAYER_NAME, normalized).apply()
            } else {
                val selectedId = activeProfileId
                writeExtraProfiles(
                    profiles().drop(1).map {
                        if (it.id == selectedId) it.copy(name = normalized)
                        else it
                    }
                )
            }
        }

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()
        }

    private fun historicalName(): String =
        prefs.getString(KEY_PLAYER_NAME, DEFAULT_PLAYER_NAME)
            ?.trim()
            ?.take(32)
            ?.ifBlank { DEFAULT_PLAYER_NAME }
            ?: DEFAULT_PLAYER_NAME

    private fun normalize(raw: String): String =
        raw.trim().take(32).ifBlank { DEFAULT_PLAYER_NAME }

    private fun writeExtraProfiles(values: List<GeckoPlayerProfile>) {
        val array = JSONArray()
        values.forEach {
            array.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
            })
        }
        prefs.edit().putString(KEY_OTHER_PROFILES, array.toString()).apply()
    }

    companion object {
        const val PREFERENCES_NAME = "geckodoku_player_profile"
        const val DEFAULT_PLAYER_NAME = "GeckoTétu"
        const val LEGACY_PROFILE_ID = "legacy"
        private const val KEY_PLAYER_NAME = "player_name"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_OTHER_PROFILES = "additional_profiles_v1"
        private const val KEY_ACTIVE_PROFILE = "active_profile_id_v1"
    }
}

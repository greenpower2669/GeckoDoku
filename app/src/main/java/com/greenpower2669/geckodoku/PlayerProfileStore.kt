package com.greenpower2669.geckodoku

import android.content.Context
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

/**
 * L'identité de stockage locale reste distincte du pseudonyme public.
 * progressionId est l'identité opaque stable utilisée pour relier les
 * résultats d'un même joueur sans jamais grouper par pseudo.
 */
data class GeckoPlayerProfile(
    val id: String,
    val name: String,
    val progressionId: String = id
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
                prefs.getString(
                    KEY_ACTIVE_PROFILE,
                    LEGACY_PROFILE_ID
                )
                    ?: LEGACY_PROFILE_ID

            return profiles()
                .firstOrNull {
                    it.id ==
                        candidate
                }
                ?.id
                ?: LEGACY_PROFILE_ID
        }

    /**
     * Identité opaque publiable pour grouper UNE progression.
     * Elle n'est ni un compte, ni un identifiant matériel, ni un secret.
     */
    val activeProgressionId: String
        get() =
            profiles()
                .firstOrNull {
                    it.id ==
                        activeProfileId
                }
                ?.progressionId
                ?: historicalProgressionId()

    fun profiles(): List<GeckoPlayerProfile> {
        val historical =
            GeckoPlayerProfile(
                id =
                    LEGACY_PROFILE_ID,
                name =
                    historicalName(),
                progressionId =
                    historicalProgressionId()
            )

        val extra =
            runCatching {
                val array =
                    JSONArray(
                        prefs.getString(
                            KEY_OTHER_PROFILES,
                            "[]"
                        )
                            ?: "[]"
                    )

                buildList<
                    GeckoPlayerProfile
                    > {
                    for (
                        i in
                            0 until
                                array.length()
                    ) {
                        val obj =
                            array.optJSONObject(
                                i
                            )
                                ?: continue

                        val id =
                            obj.optString(
                                "id"
                            )

                        val name =
                            obj.optString(
                                "name"
                            )
                                .trim()
                                .take(
                                    32
                                )

                        if (
                            !isUuid(
                                id
                            ) ||
                            name.isBlank() ||
                            any {
                                it.id ==
                                    id
                            }
                        ) {
                            continue
                        }

                        // Migration additive : les anciens profils UUID
                        // utilisent leur id local déjà aléatoire comme
                        // progressionId tant qu'aucun champ dédié n'existe.
                        val progressionId =
                            obj.optString(
                                "progressionId"
                            )
                                .takeIf {
                                    isUuid(
                                        it
                                    )
                                }
                                ?: id

                        add(
                            GeckoPlayerProfile(
                                id =
                                    id,
                                name =
                                    name,
                                progressionId =
                                    progressionId
                            )
                        )
                    }
                }
            }
                .getOrDefault(
                    emptyList()
                )

        return listOf(
            historical
        ) +
            extra
    }

    fun createProfile(
        name: String
    ): GeckoPlayerProfile {
        val normalized =
            normalize(
                name
            )

        val id =
            UUID.randomUUID()
                .toString()

        val profile =
            GeckoPlayerProfile(
                id =
                    id,
                name =
                    normalized,
                progressionId =
                    id
            )

        writeExtraProfiles(
            profiles()
                .drop(
                    1
                ) +
                profile
        )

        prefs.edit()
            .putString(
                KEY_ACTIVE_PROFILE,
                profile.id
            )
            .apply()

        return profile
    }

    fun activateProfile(
        id: String
    ): Boolean {
        if (
            profiles().none {
                it.id ==
                    id
            }
        ) {
            return false
        }

        prefs.edit()
            .putString(
                KEY_ACTIVE_PROFILE,
                id
            )
            .apply()

        return true
    }

    var playerName: String
        get() =
            profiles()
                .firstOrNull {
                    it.id ==
                        activeProfileId
                }
                ?.name
                ?: DEFAULT_PLAYER_NAME
        set(value) {
            val normalized =
                normalize(
                    value
                )

            if (
                activeProfileId ==
                    LEGACY_PROFILE_ID
            ) {
                prefs.edit()
                    .putString(
                        KEY_PLAYER_NAME,
                        normalized
                    )
                    .apply()
            } else {
                val selectedId =
                    activeProfileId

                writeExtraProfiles(
                    profiles()
                        .drop(
                            1
                        )
                        .map {
                            if (
                                it.id ==
                                    selectedId
                            ) {
                                it.copy(
                                    name =
                                        normalized
                                )
                            } else {
                                it
                            }
                        }
                )
            }
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

    private fun historicalName(): String =
        prefs.getString(
            KEY_PLAYER_NAME,
            DEFAULT_PLAYER_NAME
        )
            ?.trim()
            ?.take(
                32
            )
            ?.ifBlank {
                DEFAULT_PLAYER_NAME
            }
            ?: DEFAULT_PLAYER_NAME

    private fun historicalProgressionId(): String {
        val existing =
            prefs.getString(
                KEY_LEGACY_PROGRESSION_ID,
                null
            )

        if (
            existing !=
                null &&
            isUuid(
                existing
            )
        ) {
            return existing
        }

        val generated =
            UUID.randomUUID()
                .toString()

        // commit() ici : l'identité doit être stable avant tout score publié.
        prefs.edit()
            .putString(
                KEY_LEGACY_PROGRESSION_ID,
                generated
            )
            .commit()

        return generated
    }

    private fun normalize(
        raw: String
    ): String =
        raw.trim()
            .take(
                32
            )
            .ifBlank {
                DEFAULT_PLAYER_NAME
            }

    private fun writeExtraProfiles(
        values: List<GeckoPlayerProfile>
    ) {
        val array =
            JSONArray()

        values.forEach {
            profile ->

            array.put(
                JSONObject().apply {
                    put(
                        "id",
                        profile.id
                    )
                    put(
                        "name",
                        profile.name
                    )
                    put(
                        "progressionId",
                        profile.progressionId
                    )
                }
            )
        }

        prefs.edit()
            .putString(
                KEY_OTHER_PROFILES,
                array.toString()
            )
            .apply()
    }

    private fun isUuid(
        value: String
    ): Boolean =
        runCatching {
            UUID.fromString(
                value
            )
        }.isSuccess

    companion object {
        const val PREFERENCES_NAME =
            "geckodoku_player_profile"
        const val DEFAULT_PLAYER_NAME =
            "GeckoTétu"
        const val LEGACY_PROFILE_ID =
            "legacy"

        private const val KEY_PLAYER_NAME =
            "player_name"
        private const val KEY_SOUND_ENABLED =
            "sound_enabled"
        private const val KEY_OTHER_PROFILES =
            "additional_profiles_v1"
        private const val KEY_ACTIVE_PROFILE =
            "active_profile_id_v1"
        private const val KEY_LEGACY_PROGRESSION_ID =
            "legacy_progression_id_v1"
    }
}

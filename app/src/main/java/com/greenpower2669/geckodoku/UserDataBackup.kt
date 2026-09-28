package com.greenpower2669.geckodoku

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class BackupImportResult(
    val success: Boolean,
    val message: String
)

class UserDataBackup(
    context: Context
) {
    private val appContext =
        context.applicationContext

    fun exportJson(): String {
        val preferences =
            JSONObject()

        for (
            name in
            preferenceNames
        ) {
            val source =
                appContext
                    .getSharedPreferences(
                        name,
                        Context.MODE_PRIVATE
                    )

            val encoded =
                JSONObject()

            source.all
                .toSortedMap()
                .forEach {
                    (key, value) ->
                    encodeValue(value)
                        ?.let {
                            encoded.put(
                                key,
                                it
                            )
                        }
                }

            preferences.put(
                name,
                encoded
            )
        }

        return JSONObject()
            .apply {
                put(
                    "application",
                    APPLICATION_ID
                )
                put(
                    "schemaVersion",
                    SCHEMA_VERSION
                )
                put(
                    "exportedAt",
                    System.currentTimeMillis()
                )
                put(
                    "preferences",
                    preferences
                )
            }
            .toString(2)
    }

    fun importJson(
        raw: String
    ): BackupImportResult {
        val root =
            try {
                JSONObject(raw)
            } catch (
                _: Exception
            ) {
                return BackupImportResult(
                    false,
                    "Le fichier n'est pas un export GeckoDoku valide."
                )
            }

        if (
            root.optString(
                "application"
            ) !=
                APPLICATION_ID
        ) {
            return BackupImportResult(
                false,
                "Ce fichier ne provient pas de GeckoDoku."
            )
        }

        val version =
            root.optInt(
                "schemaVersion",
                -1
            )

        if (
            version !=
                SCHEMA_VERSION
        ) {
            return BackupImportResult(
                false,
                "Version de sauvegarde non prise en charge : " +
                    version +
                    "."
            )
        }

        val container =
            root.optJSONObject(
                "preferences"
            )
                ?: return BackupImportResult(
                    false,
                    "La section des données persistantes est absente."
                )

        val decoded =
            linkedMapOf<
                String,
                Map<String, Any>
                >()

        try {
            for (
                name in
                preferenceNames
            ) {
                decoded[name] =
                    decodePreference(
                        container
                            .optJSONObject(
                                name
                            )
                            ?: JSONObject()
                    )
            }
        } catch (
            error: Exception
        ) {
            return BackupImportResult(
                false,
                error.message
                    ?: "Données de sauvegarde invalides."
            )
        }

        val snapshots =
            preferenceNames
                .associateWith {
                    name ->
                    snapshot(
                        appContext
                            .getSharedPreferences(
                                name,
                                Context.MODE_PRIVATE
                            )
                    )
                }

        for (
            name in
            preferenceNames
        ) {
            val prefs =
                appContext
                    .getSharedPreferences(
                        name,
                        Context.MODE_PRIVATE
                    )

            if (
                !writeAll(
                    prefs,
                    decoded[name]
                        ?: emptyMap()
                )
            ) {
                snapshots
                    .forEach {
                        (snapshotName, values) ->
                        writeAll(
                            appContext
                                .getSharedPreferences(
                                    snapshotName,
                                    Context.MODE_PRIVATE
                                ),
                            values
                        )
                    }

                return BackupImportResult(
                    false,
                    "L'import n'a pas pu être appliqué. Les données précédentes ont été restaurées."
                )
            }
        }

        return BackupImportResult(
            true,
            "Import terminé."
        )
    }

    private fun encodeValue(
        value: Any?
    ): JSONObject? {
        val encoded =
            JSONObject()

        when (value) {
            is String -> {
                encoded.put("type", "string")
                encoded.put("value", value)
            }

            is Int -> {
                encoded.put("type", "int")
                encoded.put("value", value)
            }

            is Long -> {
                encoded.put("type", "long")
                encoded.put("value", value)
            }

            is Float -> {
                encoded.put("type", "float")
                encoded.put(
                    "value",
                    value.toDouble()
                )
            }

            is Boolean -> {
                encoded.put("type", "boolean")
                encoded.put("value", value)
            }

            is Set<*> -> {
                val values =
                    value
                        .filterIsInstance<
                            String
                            >()
                        .sorted()

                if (
                    values.size !=
                        value.size
                ) {
                    return null
                }

                val array =
                    JSONArray()

                values.forEach {
                    array.put(it)
                }

                encoded.put(
                    "type",
                    "string_set"
                )
                encoded.put(
                    "value",
                    array
                )
            }

            else ->
                return null
        }

        return encoded
    }

    private fun decodePreference(
        source: JSONObject
    ): Map<String, Any> {
        val result =
            linkedMapOf<
                String,
                Any
                >()

        val keys =
            source.keys()

        while (
            keys.hasNext()
        ) {
            val key =
                keys.next()

            if (key.isBlank()) {
                throw IllegalArgumentException(
                    "Une clé de sauvegarde est vide."
                )
            }

            val encoded =
                source.optJSONObject(
                    key
                )
                    ?: throw IllegalArgumentException(
                        "Valeur invalide pour " +
                            key +
                            "."
                    )

            val value: Any =
                when (
                    val type =
                        encoded.optString(
                            "type"
                        )
                ) {
                    "string" ->
                        encoded.getString(
                            "value"
                        )

                    "int" ->
                        encoded.getInt(
                            "value"
                        )

                    "long" ->
                        encoded.getLong(
                            "value"
                        )

                    "float" ->
                        encoded.getDouble(
                            "value"
                        )
                            .toFloat()

                    "boolean" ->
                        encoded.getBoolean(
                            "value"
                        )

                    "string_set" -> {
                        val array =
                            encoded
                                .optJSONArray(
                                    "value"
                                )
                                ?: throw IllegalArgumentException(
                                    "Ensemble invalide pour " +
                                        key +
                                        "."
                                )

                        buildSet {
                            for (
                                index in
                                0 until array.length()
                            ) {
                                add(
                                    array.getString(
                                        index
                                    )
                                )
                            }
                        }
                    }

                    else ->
                        throw IllegalArgumentException(
                            "Type de donnée inconnu : " +
                                type +
                                "."
                        )
                }

            result[key] =
                value
        }

        return result
    }

    private fun snapshot(
        prefs: SharedPreferences
    ): Map<String, Any> =
        prefs.all
            .mapNotNull {
                (key, value) ->
                when (value) {
                    is String,
                    is Int,
                    is Long,
                    is Float,
                    is Boolean ->
                        key to value

                    is Set<*> -> {
                        val copy =
                            value
                                .filterIsInstance<
                                    String
                                    >()
                                .toSet()

                        if (
                            copy.size ==
                                value.size
                        ) {
                            key to copy
                        } else {
                            null
                        }
                    }

                    else ->
                        null
                }
            }
            .toMap()

    private fun writeAll(
        prefs: SharedPreferences,
        values: Map<String, Any>
    ): Boolean {
        val editor =
            prefs.edit()
                .clear()

        values.forEach {
            (key, value) ->
            when (value) {
                is String ->
                    editor.putString(
                        key,
                        value
                    )

                is Int ->
                    editor.putInt(
                        key,
                        value
                    )

                is Long ->
                    editor.putLong(
                        key,
                        value
                    )

                is Float ->
                    editor.putFloat(
                        key,
                        value
                    )

                is Boolean ->
                    editor.putBoolean(
                        key,
                        value
                    )

                is Set<*> ->
                    editor.putStringSet(
                        key,
                        value
                            .filterIsInstance<
                                String
                                >()
                            .toSet()
                    )
            }
        }

        return editor.commit()
    }

    companion object {
        const val SCHEMA_VERSION =
            1

        private const val APPLICATION_ID =
            "GeckoDoku"

        val preferenceNames =
            listOf(
                "geckodoku_local_stats",
                "geckodoku_grid_journal",
                "geckodoku_game_mode_v1",
                "geckodoku_rich_media",
                "geckodoku_professor_phrase_history",
                PlayerProfileStore
                    .PREFERENCES_NAME,
                HallOfFameStore
                    .PREFERENCES_NAME,
                BeeGeckoSessionStore
                    .PREFERENCES_NAME
            )
    }
}

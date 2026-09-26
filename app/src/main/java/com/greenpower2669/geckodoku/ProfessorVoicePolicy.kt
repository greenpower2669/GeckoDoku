package com.greenpower2669.geckodoku

class ProfessorVoicePolicy {
    val fallbackPitch: Float = 0.78f
    val masculineVoicePitch: Float = 0.94f
    val speechRate: Float = 0.93f

    fun looksMasculine(
        descriptor: String
    ): Boolean {
        val value =
            descriptor
                .lowercase()
                .replace('é', 'e')
                .replace('è', 'e')
                .replace('ê', 'e')

        if (
            value.contains("female") ||
            value.contains("feminin") ||
            value.contains("femme")
        ) {
            return false
        }

        return value.contains("male") ||
            value.contains("masculin") ||
            value.contains("homme")
    }
}

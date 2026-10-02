package com.greenpower2669.geckodoku

object ProfessorVideoRecoveryPolicy {
    fun requiresViewRecreation(
        message: String
    ): Boolean =
        message.startsWith(
            "Keycolor shader could not be created"
        ) ||
            message.startsWith(
                "External video texture unavailable"
            )
}

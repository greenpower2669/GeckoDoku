package com.greenpower2669.geckodoku

class FreshFrameSerialGate {
    private var generation = -1L
    private var baselineSerial = -1L
    private var accepted = false

    fun arm(
        generation: Long,
        currentProducedSerial: Long
    ) {
        this.generation = generation
        baselineSerial =
            currentProducedSerial
        accepted = false
    }

    fun accept(
        generation: Long,
        consumedSerial: Long
    ): Boolean {
        if (
            accepted ||
            generation != this.generation ||
            consumedSerial <= baselineSerial
        ) {
            return false
        }

        accepted = true
        return true
    }

    fun cancel() {
        generation = -1L
        baselineSerial = -1L
        accepted = false
    }
}

package com.greenpower2669.geckodoku

/**
 * Canonical rule since GECKO-054:
 * every visible mascot owns its own video surface and autonomous cycle.
 * There is no shared pool, concurrency cap or global mascot scheduler.
 */
object MascotActivityPolicy {
    const val PER_PRESENCE_VIDEO =
        true
}

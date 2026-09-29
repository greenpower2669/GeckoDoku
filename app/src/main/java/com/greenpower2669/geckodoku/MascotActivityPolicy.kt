package com.greenpower2669.geckodoku

object MascotActivityPolicy {
    const val GECKO_CONCURRENT = 3
    const val BEE_CONCURRENT = 2
    const val PLANT_CONCURRENT = 1

    fun capacity(
        kind: MascotKind
    ): Int =
        when (kind) {
            MascotKind.GECKO ->
                GECKO_CONCURRENT
            MascotKind.BEE ->
                BEE_CONCURRENT
            MascotKind.PLANT ->
                PLANT_CONCURRENT
        }
}

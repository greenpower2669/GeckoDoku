package com.greenpower2669.geckodoku

object GeckoMediaAudioPolicy {
    fun mustMute(
        kind: RichMediaKind
    ): Boolean =
        when (kind) {
            RichMediaKind.GECKO_APPEARANCE,
            RichMediaKind.GECKO_DISAPPEARANCE,
            RichMediaKind.GECKO_LONG_ACTION,
            RichMediaKind.BEE_APPEARANCE -> true

            RichMediaKind.INTRO,
            RichMediaKind.PROF_LONG_ACTION -> false
        }
}

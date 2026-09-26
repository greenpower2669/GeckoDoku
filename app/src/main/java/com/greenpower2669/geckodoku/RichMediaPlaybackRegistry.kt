package com.greenpower2669.geckodoku

class RichMediaPlaybackRegistry {
    private val active =
        linkedMapOf<String, RichMediaKind>()

    fun started(
        id: String,
        kind: RichMediaKind
    ) {
        active[id] = kind
    }

    fun completed(
        id: String
    ) {
        active.remove(id)
    }

    fun isActive(
        id: String
    ): Boolean =
        active.containsKey(id)

    fun activeIds(): Set<String> =
        active.keys.toSet()

    fun activeKinds(): Set<RichMediaKind> =
        active.values.toSet()

    fun clear() {
        active.clear()
    }

    val size: Int
        get() = active.size
}

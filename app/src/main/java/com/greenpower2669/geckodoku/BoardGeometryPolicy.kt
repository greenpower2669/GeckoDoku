package com.greenpower2669.geckodoku

data class BoardGeometry(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int
)

class BoardGeometryPolicy {
    private var windowWidth =
        -1

    private var windowHeight =
        -1

    private var frozen:
        BoardGeometry? = null

    fun resolve(
        windowWidth: Int,
        windowHeight: Int,
        proposed: BoardGeometry
    ): BoardGeometry {
        val windowChanged =
            this.windowWidth != windowWidth ||
                this.windowHeight != windowHeight

        if (
            frozen == null ||
            windowChanged
        ) {
            this.windowWidth = windowWidth
            this.windowHeight = windowHeight
            frozen = proposed
        }

        return frozen
            ?: proposed
    }

    fun reset() {
        windowWidth = -1
        windowHeight = -1
        frozen = null
    }
}

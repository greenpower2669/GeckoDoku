package com.greenpower2669.geckodoku

enum class ClassicAxisGuideKind {
    HORIZONTAL,
    VERTICAL
}

data class ClassicAxisGuide(
    val kind: ClassicAxisGuideKind,
    val index: Int
)

object ClassicProfessorAxisGuidePolicy {
    fun forStep(
        step: SolveStep
    ): Set<ClassicAxisGuide> {
        if (
            step.technique ==
                SolveTechnique
                    .GECKO_X_WING
        ) {
            val rows =
                step.sourceCells
                    .map {
                        it.row
                    }
                    .toSet()

            val cols =
                step.sourceCells
                    .map {
                        it.col
                    }
                    .toSet()

            if (
                rows.size == 2 &&
                cols.size == 2
            ) {
                val eliminatesOnCols =
                    step.eliminated
                        .any {
                            it.col in cols &&
                                it.row !in rows
                        }

                if (eliminatesOnCols) {
                    return cols
                        .map {
                            ClassicAxisGuide(
                                ClassicAxisGuideKind
                                    .VERTICAL,
                                it
                            )
                        }
                        .toSet()
                }

                val eliminatesOnRows =
                    step.eliminated
                        .any {
                            it.row in rows &&
                                it.col !in cols
                        }

                if (eliminatesOnRows) {
                    return rows
                        .map {
                            ClassicAxisGuide(
                                ClassicAxisGuideKind
                                    .HORIZONTAL,
                                it
                            )
                        }
                        .toSet()
                }
            }
        }

        val axis =
            step.axis
                ?.lowercase()
                ?: return emptySet()

        val number =
            Regex("(\\d+)")
                .find(axis)
                ?.groupValues
                ?.getOrNull(1)
                ?.toIntOrNull()
                ?.minus(1)
                ?: return emptySet()

        return when {
            axis.startsWith(
                "ligne"
            ) ->
                setOf(
                    ClassicAxisGuide(
                        ClassicAxisGuideKind
                            .HORIZONTAL,
                        number
                    )
                )

            axis.startsWith(
                "colonne"
            ) ->
                setOf(
                    ClassicAxisGuide(
                        ClassicAxisGuideKind
                            .VERTICAL,
                        number
                    )
                )

            else ->
                emptySet()
        }
    }
}

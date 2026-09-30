package com.greenpower2669.geckodoku

import kotlin.random.Random

object GomokuLivingSelectionPolicy {
    const val MAX_ANIMATED_PER_TEAM = 3

    fun select(
        stones: Map<Cell, GomokuPlayer>,
        previous: Set<Cell>,
        redistribute: Boolean,
        randomValue: Int,
        limitPerTeam: Boolean = true
    ): LinkedHashSet<Cell> {
        if (!limitPerTeam) {
            return LinkedHashSet(
                stones.keys
            )
        }

        val selected =
            linkedSetOf<Cell>()

        GomokuPlayer.values()
            .forEachIndexed {
                    index,
                    player ->

                val candidates =
                    stones
                        .filterValues {
                            it == player
                        }
                        .keys
                        .toList()

                val previousForTeam =
                    previous
                        .filter {
                            stones[it] == player
                        }
                        .take(
                            MAX_ANIMATED_PER_TEAM
                        )

                selected.addAll(
                    selectTeam(
                        candidates = candidates,
                        previous =
                            previousForTeam,
                        redistribute =
                            redistribute,
                        seed =
                            randomValue +
                                index *
                                104729
                    )
                )
            }

        return selected
    }

    private fun selectTeam(
        candidates: List<Cell>,
        previous: List<Cell>,
        redistribute: Boolean,
        seed: Int
    ): List<Cell> {
        val limit =
            minOf(
                MAX_ANIMATED_PER_TEAM,
                candidates.size
            )

        if (limit == 0) {
            return emptyList()
        }

        val candidateSet =
            candidates.toSet()

        val retained =
            previous
                .filter {
                    it in candidateSet
                }
                .distinct()
                .take(limit)

        if (!redistribute) {
            if (retained.size == limit) {
                return retained
            }

            val missing =
                limit - retained.size

            return retained +
                candidates
                    .filter {
                        it !in retained
                    }
                    .shuffled(
                        Random(seed)
                    )
                    .take(missing)
        }

        val shuffled =
            candidates
                .shuffled(
                    Random(seed)
                )

        val next =
            shuffled
                .take(limit)
                .toMutableList()

        if (
            candidates.size > limit &&
            retained.size == limit &&
            next.toSet() ==
                retained.toSet()
        ) {
            val replacement =
                shuffled.firstOrNull {
                    it !in next
                } ?: candidates.first {
                    it !in next
                }

            next[next.lastIndex] =
                replacement
        }

        return next
    }
}

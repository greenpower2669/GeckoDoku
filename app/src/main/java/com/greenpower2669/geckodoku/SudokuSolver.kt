package com.greenpower2669.geckodoku

object SudokuSolver {
    private const val ALL_DIGITS_MASK =
        0x3FE

    fun isCompleteValidGrid(
        values: IntArray
    ): Boolean {
        if (
            values.size !=
                SudokuPuzzle.CELL_COUNT ||
            values.any {
                it !in 1..9
            }
        ) {
            return false
        }

        for (i in 0..8) {
            var rowMask = 0
            var colMask = 0

            for (j in 0..8) {
                rowMask =
                    rowMask or
                        (
                            1 shl
                                values[
                                    i * 9 + j
                                ]
                            )

                colMask =
                    colMask or
                        (
                            1 shl
                                values[
                                    j * 9 + i
                                ]
                            )
            }

            if (
                rowMask !=
                    ALL_DIGITS_MASK ||
                colMask !=
                    ALL_DIGITS_MASK
            ) {
                return false
            }
        }

        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                var mask = 0

                for (dr in 0..2) {
                    for (dc in 0..2) {
                        val index =
                            (
                                boxRow * 3 +
                                    dr
                                ) * 9 +
                                boxCol * 3 +
                                dc

                        mask =
                            mask or
                                (
                                    1 shl
                                        values[index]
                                    )
                    }
                }

                if (
                    mask !=
                        ALL_DIGITS_MASK
                ) {
                    return false
                }
            }
        }

        return true
    }

    fun countSolutions(
        givens: IntArray,
        limit: Int = 2
    ): Int {
        require(
            givens.size ==
                SudokuPuzzle.CELL_COUNT
        )
        require(limit >= 1)

        val board =
            givens.copyOf()

        if (!isPartialValid(board)) {
            return 0
        }

        return solveCount(
            board,
            limit
        )
    }

    fun candidateMask(
        values: IntArray,
        index: Int
    ): Int {
        require(
            values.size ==
                SudokuPuzzle.CELL_COUNT
        )
        require(
            index in
                values.indices
        )

        if (values[index] != 0) {
            return 0
        }

        val row = index / 9
        val col = index % 9
        var used = 0

        for (i in 0..8) {
            val rowValue =
                values[
                    row * 9 + i
                ]

            if (rowValue != 0) {
                used =
                    used or
                        (1 shl rowValue)
            }

            val colValue =
                values[
                    i * 9 + col
                ]

            if (colValue != 0) {
                used =
                    used or
                        (1 shl colValue)
            }
        }

        val boxRow =
            (row / 3) * 3
        val boxCol =
            (col / 3) * 3

        for (dr in 0..2) {
            for (dc in 0..2) {
                val value =
                    values[
                        (boxRow + dr) *
                            9 +
                            boxCol +
                            dc
                    ]

                if (value != 0) {
                    used =
                        used or
                            (1 shl value)
                }
            }
        }

        return ALL_DIGITS_MASK and
            used.inv()
    }

    fun digitsFromMask(
        mask: Int
    ): List<Int> =
        (1..9).filter {
            mask and
                (1 shl it) !=
                0
        }

    private fun solveCount(
        board: IntArray,
        limit: Int
    ): Int {
        var bestIndex = -1
        var bestMask = 0
        var bestCount = 10

        for (i in board.indices) {
            if (board[i] != 0) {
                continue
            }

            val mask =
                candidateMask(
                    board,
                    i
                )

            val count =
                Integer.bitCount(
                    mask
                )

            if (count == 0) {
                return 0
            }

            if (count < bestCount) {
                bestIndex = i
                bestMask = mask
                bestCount = count

                if (count == 1) {
                    break
                }
            }
        }

        if (bestIndex < 0) {
            return 1
        }

        var total = 0

        for (digit in 1..9) {
            if (
                bestMask and
                    (1 shl digit) ==
                    0
            ) {
                continue
            }

            board[bestIndex] = digit

            total +=
                solveCount(
                    board,
                    limit - total
                )

            board[bestIndex] = 0

            if (total >= limit) {
                return total
            }
        }

        return total
    }

    private fun isPartialValid(
        values: IntArray
    ): Boolean {
        if (
            values.any {
                it !in 0..9
            }
        ) {
            return false
        }

        fun noDuplicate(
            indices: IntArray
        ): Boolean {
            var mask = 0

            for (index in indices) {
                val value =
                    values[index]

                if (value == 0) {
                    continue
                }

                val bit =
                    1 shl value

                if (
                    mask and bit !=
                        0
                ) {
                    return false
                }

                mask =
                    mask or bit
            }

            return true
        }

        for (i in 0..8) {
            if (
                !noDuplicate(
                    IntArray(9) {
                        i * 9 + it
                    }
                ) ||
                !noDuplicate(
                    IntArray(9) {
                        it * 9 + i
                    }
                )
            ) {
                return false
            }
        }

        for (br in 0..2) {
            for (bc in 0..2) {
                val indices =
                    IntArray(9) {
                        val dr = it / 3
                        val dc = it % 3

                        (
                            br * 3 +
                                dr
                            ) * 9 +
                            bc * 3 +
                            dc
                    }

                if (!noDuplicate(indices)) {
                    return false
                }
            }
        }

        return true
    }
}

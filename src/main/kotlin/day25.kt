private infix fun Long.pow(exponent: Int): Long {
    if (exponent < 0) return 0L
    else if (exponent == 0) return 1L
    else if (exponent == 1) return this

    val half = exponent / 2
    val r = pow(half)
    return if (half + half == exponent) r * r else r * r * this
}

private fun Long.Companion.fromSnafu(snafu: String) = snafu.foldIndexed(0L) { i, acc, c ->
    acc + (5L pow (snafu.lastIndex - i)) * when (c) {
        '-' -> -1
        '=' -> -2
        else -> c.digitToInt()
    }
}

private fun Long.toSnafu(): String {
    val digits = ("0" + this.toString(5)).toCharArray()
    for (i in digits.lastIndex downTo 1) when (digits[i]) {
        '3' -> {
            digits[i] = '='
            ++digits[i - 1]
        }
        '4' -> {
            digits[i] = '-'
            ++digits[i - 1]
        }
        '5' -> {
            digits[i] = '0'
            ++digits[i - 1]
        }
    }
    return digits.joinToString("").removePrefix("0")
}

fun day25(data: PuzzleData) = puzzle(data) { input ->
    println(input.map(Long.Companion::fromSnafu).sum().toSnafu())
}

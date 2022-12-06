fun day6(data: PuzzleData) = puzzle(data) { lines ->
    val input = lines[0]

    fun solve(distinct: Int): Int {
        val chars = HashMap<Char, Int>()

        for (i in 0 until distinct) chars.merge(input[i], 1, Int::plus)

        var i = distinct
        while (i < input.length) {
            if (chars.none { (_, count) -> count > 1 }) return i

            chars.merge(input[i], 1, Int::plus)
            chars.compute(input[i - distinct]) { _, count -> count!! - 1 }
            ++i
        }
        return input.length
    }

    // Part 1
    println(solve(4))
    // Part 2
    println(solve(14))
}
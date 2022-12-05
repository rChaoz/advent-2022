fun day1(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    val elves = buildList {
        var list = ArrayList<Int>()
        for (line in input) {
            if (line.isBlank()) {
                add(list)
                list = ArrayList()
            } else list.add(line.toInt())
        }
        add(list)
    }
    val totals = elves.map(List<Int>::sum)
    println(totals.max())
    // Part 2
    println(totals.sorted().takeLast(3).sum())
}
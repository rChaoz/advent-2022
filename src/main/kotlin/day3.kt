fun day3(data: PuzzleData) = puzzle(data) { input ->
    // Part 1

    // Create priority map
    val priorityMap = buildMap {
        for (char in 'a'..'z') this[char] = char - 'a' + 1
        for (char in 'A'..'Z') this[char] = char - 'A' + 27
    }
    // Split each string in half
    val rucksacks = input.map { line -> (line.length / 2).let { Triple(line.substring(0, it).toSet(), line.substring(it).toSet(), line.toSet()) } }
    // Find the common character for each pair
    val commonChar = rucksacks.map {
        val (s1, s2) = it
        for (char in s1) if (char in s2) return@map char
        throw Exception("should never happen")
    }
    // Result
    println(commonChar.sumOf { priorityMap[it]!! })

    // Part 2
    var i = 0
    var sum = 0
    while (i < rucksacks.size) {
        for (char in rucksacks[i].third) if (char in rucksacks[i + 1].third && char in rucksacks[i + 2].third) sum += priorityMap[char]!!
        i += 3
    }
    println(sum)
}
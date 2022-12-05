fun day4(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    val sectionPairs = input.map { line -> line.split(',').map { segment -> segment.split('-').map(String::toInt).let { it[0]..it[1] } }.let {
        it[0] to it[1]
    } }
    println(sectionPairs.count { (s1, s2) -> (s1.first <= s2.first && s1.last >= s2.last) || (s2.first <= s1.first && s2.last >= s1.last) })
    // Part 2
    println(sectionPairs.count { (s1, s2) -> s1.first in s2 || s1.last in s2 || s2.first in s1 || s2.last in s1 })
}
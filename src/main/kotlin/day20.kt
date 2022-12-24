import java.util.LinkedList

fun day20(data: PuzzleData) = puzzle(data) { input ->
    val nums = input.mapIndexed { i, num -> num.toLong() to i }

    fun mix(l: CircularMutableList<Pair<Long, Int>>): CircularMutableList<Pair<Long, Int>> {
        for ((_, originalIndex) in nums) {
            var num = 0L
            val pos = l.indexOfFirst { it.second == originalIndex.apply { num = it.first } }
            l.removeAt(pos)
            l.add(((pos + num) % l.size).toInt(), num to originalIndex)
        }
        return l
    }

    fun result(l: CircularMutableList<Pair<Long, Int>>) {
        val zero = l.indexOfFirst { it.first == 0L }
        println(l.slice(listOf(zero + 1000, zero + 2000, zero + 3000)).sumOf(Pair<Long, Int>::first))
    }

    // Part 1
    result(mix(CircularMutableList(LinkedList(nums))))

    // Part 2
    result(CircularMutableList(LinkedList(nums.map { it.first * 811589153L to it.second })).also { list -> repeat(10) { mix(list) } })
}
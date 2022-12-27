import java.util.PriorityQueue

private data class Blizzard(val position: Point, val direction: Point) {
    fun next() = copy(position = (position + direction).let {
        when {
            it.x == 0 -> it.copy(x = mapSize.x - 2)
            it.x == mapSize.x - 1 -> it.copy(x = 1)

            it.y == 0 -> it.copy(y = mapSize.y - 2)
            it.y == mapSize.y - 1 -> it.copy(y = 1)

            else -> it
        }
    })
}

private class BlizzardMap(val blizzards: List<Blizzard>) {
    val map = MutableMatrix(mapSize) { true }.also { for (blizzard in blizzards ) it[blizzard.position] = false }

    fun isClear(position: Point) = map[position]

    fun next() = BlizzardMap(blizzards.map(Blizzard::next))
}

private data class Position(val point: Point, val minute: Int)

private lateinit var mapSize: Dimension

fun day24(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    mapSize = Dimension(input[0].length, input.size)
    val xRange = 1..mapSize.x - 2
    val yRange = 1..mapSize.y - 2

    val initialBlizzards = buildList {
        for ((i, line) in input.withIndex()) for ((j, char) in line.withIndex()) (when (char) {
            '>' -> Point.RIGHT
            'v' -> Point.DOWN
            '<' -> Point.LEFT
            '^' -> Point.UP
            else -> null
        })?.let { add(Blizzard(Point(j, i), it)) }
    }
    val maps = mutableListOf(BlizzardMap(initialBlizzards))

    fun map(minute: Int): BlizzardMap = if (maps.size > minute) maps[minute] else map(minute - 1).next().also { maps += it }

    val directions = Point.DIRECTIONS + Point()
    val upperPosition = Point(input[0].indexOf('.'), 0)
    val lowerPosition = Point(input.last().indexOf('.'), input.lastIndex)

    fun travel(reverse: Boolean = false, startingMinute: Int = 1): Int {
        val startPosition = if (reverse) lowerPosition else upperPosition
        val endPosition = if (reverse) upperPosition else lowerPosition

        val processed = HashSet<Position>()
        val q = PriorityQueue<Position>(Comparator.comparingInt { it.point.distanceTo(endPosition) + it.minute }).also { it.add(Position(startPosition, startingMinute)) }
        while (q.isNotEmpty()) {
            val (pos, minute) = q.remove()
            val map = map(minute)
            for (nextPos in directions.map { it + pos }) {
                if (nextPos == endPosition) return minute
                if (nextPos != startPosition && (nextPos.x !in xRange || nextPos.y !in yRange || !map.isClear(nextPos))) continue
                val p = Position(nextPos, minute + 1)
                if (p !in processed) {
                    q += p
                    processed += p
                }
            }
        }
        throw Exception("impossible")
    }

    val minute = travel()
    println(minute)
    // Part 2
    println(travel(false, travel(true, minute + 1) + 1))
}

private class Cell(val height: Int, val position: Point) {
    var steps = -1
}

fun day12(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    var start = Point()
    var end = Point()

    val map = input.mapIndexed { y, line ->
        line.mapIndexed { x, char ->
            when (char) {
                'S' -> {
                    start = Point(x, y)
                    Cell('a'.code, Point(x, y))
                }

                'E' -> {
                    end = Point(x, y)
                    Cell('z'.code, Point(x, y))
                }

                else -> Cell(char.code, Point(x, y))
            }
        }
    }

    fun dfs(point: Point, steps: Int = 0) {
        val p = map[point]
        if (p.steps <= steps && p.steps != -1) return
        else p.steps = steps
        for (direction in Point.DIRECTIONS) {
            val newPoint = point + direction
            if (newPoint.x !in map[0].indices || newPoint.y !in map.indices) continue
            if (map[newPoint].height <= p.height + 1) dfs(newPoint, steps + 1)
        }
    }

    dfs(start)
    println(map[end].steps)

    // Part 2
    println(map.minOf { row ->
        row.minOf second@{ cell ->
            if (cell.height != 'a'.code) return@second Int.MAX_VALUE
            // Reset before doing dfs again
            map.forEach { r -> r.forEach { it.steps = -1 } }
            dfs(cell.position)
            map[end].steps.takeIf { it != -1 } ?: Int.MAX_VALUE
        }
    })
}

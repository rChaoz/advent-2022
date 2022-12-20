private class CavePoint(val position: Point, var type: Type = Type.AIR) {
    val solid get() = type.solid

    enum class Type(val solid: Boolean, val char: Char) {
        AIR(false, '.'), ROCK(true, '#'), SAND(true, 'o');
    }

    override fun toString() = type.char.toString()
}

fun day14(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    val cave = Matrix(Dimension(1000, 200)) { CavePoint(it) }

    // Create rock structures
    for (line in input) {
        val points = line.split(" -> ").map(Point::parse)
        var current = points[0]
        cave[current].type = CavePoint.Type.ROCK
        for (target in points.slice(1..points.lastIndex)) {
            val direction = current.directionTo(target)
            while (current != target) {
                current += direction
                cave[current].type = CavePoint.Type.ROCK
            }
        }
    }

    // Simulate sand
    var count = -1 // because we increment once for the first sand to fall
    val spawn = Point(500, 0)
    outer@while (true) {
        var sand = spawn
        ++count
        while (sand.y < 199) sand += when {
            !cave[sand + Point.DOWN].solid -> Point.DOWN
            !cave[sand + Point.UP_LEFT].solid -> Point.UP_LEFT
            !cave[sand + Point.UP_RIGHT].solid -> Point.UP_RIGHT
            else -> {
                cave[sand].type = CavePoint.Type.SAND
                continue@outer
            }
        }
        break@outer
    }
    println(count)

    // Part 2

    // Create floor
    val floorY = cave.flatten().mapNotNull { if (it.type == CavePoint.Type.ROCK) it.position.y else null }.max() + 2
    for (x in 0 until 1000) cave[floorY][x].type = CavePoint.Type.ROCK

    // Simulate sand
    while (cave[spawn].type == CavePoint.Type.AIR) {
        var sand = spawn
        ++count
        while (sand.y < 199) sand += when {
            !cave[sand + Point.DOWN].solid -> Point.DOWN
            !cave[sand + Point.UP_LEFT].solid -> Point.UP_LEFT
            !cave[sand + Point.UP_RIGHT].solid -> Point.UP_RIGHT
            else -> {
                cave[sand].type = CavePoint.Type.SAND
                break
            }
        }
    }

    println(count)
}

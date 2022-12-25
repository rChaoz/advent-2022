fun day23(data: PuzzleData) = puzzle(data) { input ->
    class Elf(var position: Point, var proposedLocation: Point? = null)

    // Part 1
    val elfMap = input.map { line -> line.map { it == '#' } }

    // 0 marks empty area, -1 means there is an elf there
    // n means that n elves are proposing to go there
    val map = MutableMatrix(Dimension(500, 500)) { 0 }
    val elves = buildList {
        val offset = (map.dimension - elfMap.dimension) / 2
        elfMap.forEachCellIndexed { pos, isElf ->
            if (isElf) {
                map[pos + offset] = -1
                add(Elf(pos + offset))
            }
        }
    }

    val proposedDirections = CircularList(
        listOf(
            listOf(Point.UP_LEFT, Point.UP, Point.UP_RIGHT),
            listOf(Point.DOWN_LEFT, Point.DOWN, Point.DOWN_RIGHT),
            listOf(Point.UP_LEFT, Point.LEFT, Point.DOWN_LEFT),
            listOf(Point.UP_RIGHT, Point.RIGHT, Point.DOWN_RIGHT),
        )
    )

    fun doRound(round: Int) {
        // Propose directions
        for (elf in elves) {
            elf.proposedLocation = null
            if (Point.ALL_DIRECTIONS.none { map[elf.position + it] == -1 }) continue
            for (direction in round until round + 4) {
                val dir = proposedDirections[direction]
                if (dir.all { map[elf.position + it] != -1 }) {
                    elf.proposedLocation = (elf.position + dir[1]).also { ++map[it] }
                    break
                }
            }
        }
        // Elves move
        for (elf in elves) {
            val proposed = elf.proposedLocation ?: continue
            if (map[proposed] != 1) map[proposed] = 0
            else {
                map[proposed] = -1
                map[elf.position] = 0
                elf.position = proposed
            }
        }
    }

    repeat(10, ::doRound)

    val (topLeft, bottomRight) = elves.let {
        var minX = it[0].position.x
        var minY = it[0].position.y
        var maxX = it[0].position.x
        var maxY = it[0].position.y
        for (elf in it) {
            if (elf.position.x < minX) minX = elf.position.x
            if (elf.position.y < minY) minY = elf.position.y
            if (elf.position.x > maxX) maxX = elf.position.x
            if (elf.position.y > maxY) maxY = elf.position.y
        }
        Point(minX, minY) to Point(maxX, maxY)
    }

    println((bottomRight.x - topLeft.x + 1) * (bottomRight.y - topLeft.y + 1) - elves.size)

    // Part 2
    var elfPositions = elves.map(Elf::position)
    doRound(10)
    var newPositions = elves.map(Elf::position)
    var round = 11
    while (elfPositions != newPositions) {
        elfPositions = newPositions
        doRound(round++)
        newPositions = elves.map(Elf::position)
    }
    println(round)
}

private class FallingRock(val points: Array<Point>) {
    constructor(base: List<Point>, shift: Point) : this(base.map { it + shift }.toTypedArray())

    val highestPoint get() = points.maxOf { it.y }

    fun canMoveDown(map: Matrix<Boolean>) = points.none { map[it - Point.DOWN] }

    fun moveDown() = points.forEachIndexed { i, _ -> points[i] -= Point.DOWN }

    fun moveRight(map: Matrix<Boolean>) {
        if (points.none { map[it + Point.RIGHT] }) for (i in points.indices) points[i] += Point.RIGHT
    }

    fun moveLeft(map: Matrix<Boolean>) {
        if (points.none { map[it + Point.LEFT] }) for (i in points.indices) points[i] += Point.LEFT
    }

    companion object {
        val ROCK_HORIZONTAL = listOf(Point(3, 0), Point(4, 0), Point(5, 0), Point(6, 0))
        val ROCK_PLUS = listOf(Point(3, 1), Point(4, 1), Point(5, 1), Point(4, 0), Point(4, 2))
        val ROCK_L = listOf(Point(3, 0), Point(4, 0), Point(5, 0), Point(5, 1), Point(5, 2))
        val ROCK_LINE = listOf(Point(3, 0), Point(3, 1), Point(3, 2), Point(3, 3))
        val ROCK_SQUARE = listOf(Point(3, 0), Point(3, 1), Point(4, 0), Point(4, 1))

        val ROCKS = listOf(ROCK_HORIZONTAL, ROCK_PLUS, ROCK_L, ROCK_LINE, ROCK_SQUARE)
        var rock = 0

        fun next(yLevel: Int): FallingRock = FallingRock(ROCKS[rock++], Point(0, yLevel)).also { if (rock >= ROCKS.size) rock = 0 }
    }
}

fun day17(data: PuzzleData) = puzzle(data) { input ->
    val jets = input[0].map {
        when (it) {
            '>' -> 1
            '<' -> -1
            else -> throw Exception("Unknown character - '$it'")
        }
    }

    // Simulation
    val map = MutableMatrix(Dimension(9, 50000)) { it.x == 0 || it.x == 8 || it.y == 0 }
    var highestPoint = 0

    var currentJet = 0

    fun nextJet() = jets[currentJet++].also { if (currentJet >= jets.size) currentJet = 0 }

    repeat(2022) {
        val rock = FallingRock.next(highestPoint + 4)
        while (true) {
            if (nextJet() == 1) rock.moveRight(map) else rock.moveLeft(map)
            if (rock.canMoveDown(map)) rock.moveDown() else break
        }
        rock.points.forEach { map[it] = true }
        if (rock.highestPoint > highestPoint) highestPoint = rock.highestPoint
    }
    println(highestPoint)

    // Part 2
    data class RepetitionData(val projection: String, val jetValue: Int, val rockNum: Long, val highestPoint: Int)

    val repetitionData = mutableListOf<RepetitionData>()

    val maxRocks = 1000000000000L
    var rockNum = 2022L
    var extraHeight = 0L

    fun nextJet(justStartedFalling: Boolean, rockType: Int) = jets[currentJet++].also {
        if (currentJet >= jets.size) currentJet = 0
        if (extraHeight == 0L && map[highestPoint][4] && justStartedFalling && rockType == 0) {
            val proj = IntArray(7)
            repeat(7) {
                var h = highestPoint
                while (h >= 0) if (map[h][it + 1]) {
                    proj[it] = highestPoint - h
                    break
                } else --h
            }
            val projection = proj.joinToString("") { it.toString(Character.MAX_RADIX) }

            val lastTimeThisExactPositionHappened = repetitionData.find { it.projection == projection && it.jetValue == currentJet }
            lastTimeThisExactPositionHappened?.let {
                val deltaH = highestPoint - it.highestPoint
                val deltaRocks = rockNum - it.rockNum
                val remainingRocks = maxRocks - rockNum

                val numFits = remainingRocks / deltaRocks
                extraHeight += deltaH * numFits
                rockNum += deltaRocks * numFits
            }
            repetitionData.add(RepetitionData(projection, currentJet, rockNum, highestPoint))
        }
    }

    // Continue the simulation
    while (rockNum < maxRocks) {
        val rock = FallingRock.next(highestPoint + 4)
        var justStartedFalling = true
        while (true) {
            if (nextJet(justStartedFalling, FallingRock.rock - 1) == 1) rock.moveRight(map) else rock.moveLeft(map)
            if (rock.canMoveDown(map)) rock.moveDown() else break
            justStartedFalling = false
        }
        rock.points.forEach { map[it] = true }
        if (rock.highestPoint > highestPoint) highestPoint = rock.highestPoint
        ++rockNum
    }

    println(highestPoint + extraHeight)
}

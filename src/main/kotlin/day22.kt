private data class MapSquare(val type: Type, val position: Point) {
    var wrap = arrayOfNulls<Point>(4)

    constructor(position: Point, type: Char) : this(Type.forChar(type), position)

    override fun toString() = type.char.toString()

    enum class Type(val char: Char) {
        ABSENT(' '), WALL('#'), GROUND('.');

        companion object {
            fun forChar(char: Char) = values().find { it.char == char }!!
        }
    }
}

private sealed class MovementInstruction

private data class MoveForward(val amount: Int) : MovementInstruction()

private data class Rotate(val direction: Direction) : MovementInstruction() {
    enum class Direction { CLOCKWISE, COUNTER_CLOCKWISE }
}

fun day22(data: PuzzleData) = puzzle(data) { rawInput ->
    // Part 1
    val cubeSize = rawInput[0].toInt()
    val rawMap = rawInput.slice(1..rawInput.lastIndex - 2).let {
        val maxLength = it.maxOf(String::length)
        it.map { line -> line.padEnd(maxLength).toList() }
    }
    val map = (" " * (rawMap.dimension.x + 2)).toList().let { emptyLine ->
        buildList {
            add(emptyLine)
            addAll(rawMap.map { listOf(' ') + it + listOf(' ') })
            add(emptyLine)
        }
    }.mapCellsIndexed(::MapSquare)

    // Generate wrap coordinates
    val directions = listOf(Point.RIGHT, Point.DOWN, Point.LEFT, Point.UP)

    fun generateWrapCoordinates(direction: Int) {
        val startingPoints = when (direction) {
            3 -> List(map[0].size) { index -> Point(index, map.lastIndex) } // Up
            0 -> List(map.size) { index -> Point(0, index) } // Right
            1 -> List(map[0].size) { index -> Point(index, 0) } // Bottom
            2 -> List(map.size) { index -> Point(map[0].lastIndex, index) } // Left
            else -> throw Exception("Invalid direction (must be 0-3): $direction")
        }
        var wrap: Point? = null
        var wall = false
        for (start in startingPoints) {
            var p = start
            while (p in map) {
                val square = map[p]
                when (square.type) {
                    MapSquare.Type.ABSENT -> if (wrap != null) {
                        if (!wall) square.wrap[direction] = wrap
                        wrap = null
                    }

                    else -> if (wrap == null) {
                        wall = square.type == MapSquare.Type.WALL
                        wrap = p
                    }
                }
                p += directions[direction]
            }
        }
    }
    for (direction in directions.indices) generateWrapCoordinates(direction)

    // Read movement instructions
    val movement = buildList {
        var num = 0
        for (char in rawInput.last()) when {
            char.isDigit() -> num = num * 10 + char.digitToInt()
            char == 'L' -> {
                if (num != 0) add(MoveForward(num))
                num = 0
                add(Rotate(Rotate.Direction.COUNTER_CLOCKWISE))
            }

            char == 'R' -> {
                if (num != 0) add(MoveForward(num))
                num = 0
                add(Rotate(Rotate.Direction.CLOCKWISE))
            }

            else -> throw Exception("Unknown character: '$char'")
        }
        if (num != 0) add(MoveForward(num))
    }

    val startingPoint = map[1].first { it.type != MapSquare.Type.ABSENT }.position

    fun rotate(direction: Int, rotation: Rotate.Direction): Int {
        var d = direction
        if (rotation == Rotate.Direction.CLOCKWISE) ++d
        else --d
        if (d < 0) d = 3
        else if (d > 3) d = 0
        return d
    }

    var pos = startingPoint
    var direction = 0
    for (move in movement) {
        when (move) {
            is Rotate -> direction = rotate(direction, move.direction)
            is MoveForward -> {
                val dirP = directions[direction]
                for (i in 0 until move.amount) {
                    val nextSquare = map[pos + dirP]
                    pos = when (nextSquare.type) {
                        MapSquare.Type.ABSENT -> if (nextSquare.wrap[direction] != null) nextSquare.wrap[direction]!! else break
                        MapSquare.Type.WALL -> break
                        MapSquare.Type.GROUND -> nextSquare.position
                    }
                }
            }
        }
    }
    println(pos.y * 1000 + pos.x * 4 + direction)

    // Part 2

    /* Cube - top view (top face (1) is the face in the 'middle' of the input)

    (6 is behind)

        2
       ---
    3 |1/6| 4
       ---
        5
    */

    /* Input file view (there are no spaces between rows/columns in actual file)
    (these are read to 'fs' variable, fs[0] = 000...)

    000  111  222
    000  111  222

    333  444  555
    333  444  555

    666  777  888
    666  777  888

    Input faces correspond to cube faces like so:
    - 444 -> 1
    - 111 -> 2
    - 333 -> 3
    - 555 -> 4
    - 777 -> 5

    Any one of these (0-8) may be absent (exactly 5 are present). We map the ones listed above to their cube faces using a map.
    A face either is composed of all spaces or all '.'/'#' - it's enough to check just 1 character -> face[0][0] != ' ' is enough.

    Always map faces as if 111, 333, 444, 555 and 777 were provided. For example, if 000, 111, 444, 555 and 777 are provided, like so:
    000  111
    000  111

         444  555
         444  555

         777
         777

    000 maps to cube face 3 like 333 would, but rotated 90 degrees counter-clockwise, For every face that is not 111/333/444/555/777,
    we rotate it before mapping to reverse this effect.
    */

    var face = -1 // the top-left-most face relative to the input map

    val calcResult = { d: Int, p: Point -> (p.y + 1) * 1000 + (p.x + 1) * 4 + d }
    val facesResultFunctions = HashMap<Int, (Int, Point) -> Int>() // also create functions to revert the created mapping

    val faces = buildMap {
        @Suppress("UnnecessaryVariable") val l = cubeSize
        val fs = List(9) { rawMap.subMatrix(Point(it % 3 * l, it / 3 * l), Point((it % 3 + 1) * l - 1, (it / 3 + 1) * l - 1)) }

        // Map middle (cube face 1, input face 444) face directly
        this[1] = fs[4].mapCells { it == '#' }
        facesResultFunctions[1] = { direction, pos -> calcResult(direction, pos + Point(l, l)) }

        // Check top face
        // We check if face 111 is present (maps to face 2 in the cube diagram)
        if (fs[1][0][0] != ' ') { // fs[x] refers to xxx in input file
            this[2] = fs[1].mapCells { it == '#' } // this[x] refers to face x on the cube diagram
            facesResultFunctions[2] = { direction, pos -> calcResult(direction, pos + Point(l, 0)) }
            // If it is, the left/right adjacent faces (000 and 222) correspond to 3 and 4 on the cube, respectively
            if (fs[0][0][0] != ' ') {
                this[3] = fs[0].rotateCounterClockwise().mapCells { it == '#' } // rotate it to reverse the effect described above
                facesResultFunctions[3] = { direction, pos ->
                    calcResult(rotate(direction, Rotate.Direction.COUNTER_CLOCKWISE), Point(pos.y, l - 1 - pos.x))
                }
                face = 3
                pos = Point(0, l - 1)
                direction = 3
            }
            if (face == -1) {
                face = 2
                pos = Point(0, 0)
                direction = 0
            }
            if (fs[2][0][0] != ' ') {
                this[4] = fs[2].rotateClockwise().mapCells { it == '#' }
                facesResultFunctions[4] = { direction, pos ->
                    calcResult(rotate(direction, Rotate.Direction.COUNTER_CLOCKWISE), Point(l - 1 - pos.y, pos.x) + Point(l * 2, 0))
                }
            }
        }
        // Check left face
        if (fs[3][0][0] != ' ') {
            this[3] = fs[3].mapCells { it == '#' }
            facesResultFunctions[3] = { direction, pos -> calcResult(direction, pos + Point(0, l)) }
            if (fs[0][0][0] != ' ') {
                this[2] = fs[0].rotateClockwise().mapCells { it == '#' }
                facesResultFunctions[2] = { direction, pos ->
                    calcResult(rotate(direction, Rotate.Direction.COUNTER_CLOCKWISE), Point(l - 1 - pos.y, pos.x))
                }
                if (face == -1) {
                    face = 2
                    pos = Point(l - 1, 0)
                    direction = 1
                }
            }
            if (fs[6][0][0] != ' ') {
                this[5] = fs[6].rotateCounterClockwise().mapCells { it == '#' }
                facesResultFunctions[5] = { direction, pos ->
                    calcResult(rotate(direction, Rotate.Direction.CLOCKWISE), Point(pos.y, l - 1 - pos.x) + Point(0, l * 2))
                }
            }
        }
        // Check right face
        if (fs[5][0][0] != ' ') {
            this[4] = fs[5].mapCells { it == '#' }
            facesResultFunctions[4] = { direction, pos -> calcResult(direction, pos + Point(l * 2, l)) }
            if (fs[2][0][0] != ' ') {
                this[2] = fs[2].rotateCounterClockwise().mapCells { it == '#' }
                facesResultFunctions[2] = { direction, pos ->
                    calcResult(rotate(direction, Rotate.Direction.CLOCKWISE), Point(pos.y, l - 1 - pos.x) + Point(l * 2, 0))
                }
                if (face == -1) {
                    face = 2
                    pos = Point(0, l - 1)
                    direction = 3
                }
            }
            if (fs[8][0][0] != ' ') {
                this[5] = fs[8].rotateClockwise().mapCells { it == '#' }
                facesResultFunctions[5] = { direction, pos ->
                    calcResult(rotate(direction, Rotate.Direction.COUNTER_CLOCKWISE), Point(l - 1 - pos.y, pos.x) + Point(l * 2, l * 2))
                }
            }
        }
        // Check bottom face
        if (fs[7][0][0] != ' ') {
            this[5] = fs[7].mapCells { it == '#' }
            facesResultFunctions[5] = { direction, pos -> calcResult(direction, pos + Point(l, l * 2)) }
            if (fs[6][0][0] != ' ') {
                this[3] = fs[6].rotateClockwise().mapCells { it == '#' }
                facesResultFunctions[3] = { direction, pos ->
                    calcResult(rotate(direction, Rotate.Direction.COUNTER_CLOCKWISE), Point(l - 1 - pos.y, pos.x) + Point(0, l * 2))
                }
            }
            if (fs[8][0][0] != ' ') {
                this[4] = fs[8].rotateCounterClockwise().mapCells { it == '#' }
                facesResultFunctions[4] = { direction, pos ->
                    calcResult(rotate(direction, Rotate.Direction.CLOCKWISE), Point(pos.y, l - 1 - pos.x) + Point(l * 2, l * 2))
                }
            }
        }

        // The extra face (not present in 'fs') is always the backside (6)
        // We will align this face to match as if we flipped the cube such that the 2 face stays up, the 5 face stays down,
        // but the 6 face is now facing us

        // Check right of 222
        if (rawMap.dimension.x > l * 3) {
            // Face is to the right of 222, 555 or 888
            if (rawMap[0][l * 3] != ' ') {
                this[6] = rawMap.subMatrix(Point(l * 3, 0), Point(l * 4 - 1, l - 1)).rotateClockwise().mapCells { it == '#' }
                facesResultFunctions[6] = { direction, pos ->
                    calcResult(rotate(direction, Rotate.Direction.COUNTER_CLOCKWISE), Point(l - 1 - pos.y, pos.x) + Point(l * 3, 0))
                }
            }
            else if (rawMap[l][l * 3] != ' ') {
                this[6] = rawMap.subMatrix(Point(l * 3, l), Point(l * 4 - 1, l * 2 - 1)).mapCells { it == '#' }
                facesResultFunctions[6] = { direction, pos -> calcResult(direction, pos + Point(l * 3, l)) }
            }
            else if (rawMap[l * 2][l * 3] != ' ') {
                this[6] = rawMap.subMatrix(Point(l * 3, l * 2), Point(l * 4 - 1, l * 3 - 1)).rotateCounterClockwise().mapCells { it == '#' }
                facesResultFunctions[6] = { direction, pos ->
                    calcResult(rotate(direction, Rotate.Direction.CLOCKWISE), Point(pos.y, l - 1 - pos.x) + Point(l * 3, l * 2))
                }
            } else throw Exception("Cannot find cube face 6")
        } else if (rawMap.dimension.y > cubeSize * 3) {
            // Face is below 666, 777 or 888
            if (rawMap[l * 3][0] != ' ') {
                this[6] = rawMap.subMatrix(Point(0, l * 3), Point(l - 1, l * 4 - 1)).rotateClockwise().mapCells { it == '#' }
                facesResultFunctions[6] = { direction, pos ->
                    calcResult(rotate(direction, Rotate.Direction.COUNTER_CLOCKWISE), Point(l - 1 - pos.y, pos.x) + Point(0, l * 3))
                }
            } else if (rawMap[l * 3][l] != ' ') {
                this[6] = rawMap.subMatrix(Point(l, l * 3), Point(l * 2 - 1, l * 4 - 1)).rotateCounterClockwise().rotateCounterClockwise().mapCells { it == '#' }
                facesResultFunctions[6] = { direction, pos ->
                    calcResult((direction + 2) % 4, Point(l - 1 - pos.x, l - 1 - pos.y) + Point(l, l * 3))
                }
            } else if (rawMap[l * 3][l * 2] != ' ') {
                this[6] = rawMap.subMatrix(Point(l * 2, l * 3), Point(l * 3 - 1, l * 4 - 1)).rotateCounterClockwise().mapCells { it == '#' }
                facesResultFunctions[6] = { direction, pos ->
                    calcResult(rotate(direction, Rotate.Direction.CLOCKWISE), Point(pos.y, l - 1 - pos.x) + Point(l * 2, l * 3))
                }
            } else throw Exception("Cannot find cube face 6")
        } else throw Exception("Cannot find cube face 6")
    }

    val posRange = 0 until cubeSize

    for (move in movement) {
        when (move) {
            is Rotate -> direction = rotate(direction, move.direction)
            is MoveForward -> {
                for (i in 1..move.amount) {
                    val dirP = directions[direction]
                    val next = pos + dirP
                    pos = if (next.x in posRange && next.y in posRange) {
                        if (faces[face]!![next]) break
                        else next
                    } else {
                        val (nextFace, nextPos, newDirection) = when (face) {
                            1 -> when { // Front face
                                next.x < 0 -> Triple(3, Point(cubeSize - 1, next.y), 2) // wrap left
                                next.x >= cubeSize -> Triple(4, Point(0, next.y), 0) // warp right
                                next.y < 0 -> Triple(2, Point(next.x, cubeSize - 1), 3) // warp up
                                next.y >= cubeSize -> Triple(5, Point(next.x, 0), 1) // wrap down
                                else -> throw Exception() // wrap down
                            }
                            3 -> when { // Left face
                                next.x < 0 -> Triple(6, Point(cubeSize - 1, next.y), 2) // wrap left
                                next.x >= cubeSize -> Triple(1, Point(0, next.y), 0) // warp right
                                next.y < 0 -> Triple(2, Point(0, next.x), 0) // warp up
                                next.y >= cubeSize -> Triple(5, Point(0, cubeSize - 1 - next.x), 0) // wrap down
                                else -> throw Exception()
                            }
                            4 -> when { // Right face
                                next.x < 0 -> Triple(1, Point(cubeSize - 1, next.y), 2) // wrap left
                                next.x >= cubeSize -> Triple(6, Point(0, next.y), 0) // warp right
                                next.y < 0 -> Triple(2, Point(cubeSize - 1, cubeSize - 1 - next.x), 2) // warp up
                                next.y >= cubeSize -> Triple(5, Point(cubeSize - 1, next.x), 2) // wrap down
                                else -> throw Exception()
                            }
                            6 -> when { // Back face
                                next.x < 0 -> Triple(4, Point(cubeSize - 1, next.y), 2) // wrap left
                                next.x >= cubeSize -> Triple(3, Point(0, next.y), 0) // warp right
                                next.y < 0 -> Triple(2, Point(cubeSize - 1 - next.x, 0), 1) // warp up
                                next.y >= cubeSize -> Triple(5, Point(cubeSize - 1 - next.x, cubeSize - 1), 3) // wrap down
                                else -> throw Exception()
                            }
                            2 -> when { // Upper face
                                next.x < 0 -> Triple(3, Point(next.y, 0), 1) // wrap left
                                next.x >= cubeSize -> Triple(4, Point(cubeSize - 1 - next.y, 0), 1) // warp right
                                next.y < 0 -> Triple(6, Point(cubeSize - 1 - next.x, 0), 1) // warp up
                                next.y >= cubeSize -> Triple(1, Point(next.x, 0), 1) // wrap down
                                else -> throw Exception()
                            }
                            5 -> when { // Lower face
                                next.x < 0 -> Triple(3, Point(cubeSize - 1 - next.y, cubeSize - 1), 3) // wrap left
                                next.x >= cubeSize -> Triple(4, Point(next.y, cubeSize - 1), 3) // warp right
                                next.y < 0 -> Triple(1, Point(next.x, cubeSize - 1), 3) // warp up
                                next.y >= cubeSize -> Triple(6, Point(cubeSize - 1 - next.x, cubeSize - 1), 3) // wrap down
                                else -> throw Exception()
                            }
                            else -> throw Exception()
                        }
                        if (faces[nextFace]!![nextPos]) break
                        else {
                            face = nextFace
                            direction = newDirection
                            nextPos
                        }
                    }
                }
            }
        }
    }

    println(facesResultFunctions[face]!!(direction, pos))
}

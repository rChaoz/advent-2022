import kotlin.math.abs
import kotlin.math.sign

private val directions = mapOf(
    'U' to Point(0, 1),
    'R' to Point(1, 0),
    'D' to Point(0, -1),
    'L' to Point(-1, 0),
)

private data class Motion(val direction: Point, val amount: Int) {
    constructor(string: String) : this(directions[string[0]]!!, string.substring(2).toInt())
}

private data class Point(val x: Int = 0, val y: Int = 0) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
}

fun day9(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    var h = Point()
    var t = Point()
    val positions = HashSet<Point>().apply { add(t) }

    val motions = input.map(::Motion)
    for (motion in motions) repeat(motion.amount) {
        h += motion.direction
        val xDelta = h.x - t.x
        val yDelta = h.y - t.y
        if (abs(xDelta) > 1 || abs(yDelta) > 1) t += Point(xDelta.sign, yDelta.sign)
        positions.add(t)
    }

    println(positions.size)

    // Part 2
    positions.clear()
    positions.add(Point())
    val points = Array(10) { Point() } // 0 is Head, 9 is Tail
    for (motion in motions) repeat(motion.amount) {
        points[0] += motion.direction
        for (i in 1..9) {
            val xDelta = points[i - 1].x - points[i].x
            val yDelta = points[i - 1].y - points[i].y
            if (abs(xDelta) > 1 || abs(yDelta) > 1) points[i] += Point(xDelta.sign, yDelta.sign)
        }
        positions.add(points[9])
    }
    kotlin.io.println(points.joinToString())

    println(positions.size)
}
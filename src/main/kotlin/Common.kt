import java.util.Objects
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.min
import kotlin.math.max

// Point

data class Point(val x: Int = 0, val y: Int = 0) {
    /**
     * Manhattan distance from origin (0,0)
     */
    val radius = abs(x) + abs(y)

    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)

    infix fun directionTo(other: Point) = Point((other.x - x).sign, (other.y - y).sign)

    infix fun distanceTo(other: Point) = (this - other).radius

    companion object {
        val UP = Point(0, -1)
        val RIGHT = Point(1, 0)
        val DOWN = Point(0, 1)
        val LEFT = Point(-1, 0)

        val UP_LEFT = DOWN + LEFT
        val UP_RIGHT = DOWN + RIGHT
        val DOWN_RIGHT = UP + RIGHT
        val DOWN_LEFT = UP + LEFT

        val DIRECTIONS = arrayOf(UP, RIGHT, DOWN, LEFT)

        fun parse(string: String): Point {
            val (x, y) = string.split(',').map(String::toInt)
            return Point(x, y)
        }
    }
}

typealias Dimension = Point

// Matrix / 2D Lists

typealias Matrix<T> = List<List<T>>

operator fun <T> List<List<T>>.get(at: Point) = this[at.y][at.x]

fun <T> Matrix<T>.areaToString(topLeft: Point = Point(0, 0), bottomRight: Point = Point(this[0].lastIndex, this.lastIndex),
                               separator: String = "", lineSeparator: String = "\n",
                               withLineNum: Boolean = false,
                               toStringFunc: (T) -> String = Objects::toString) = buildString {
    for (y in topLeft.y..bottomRight.y) {
        if (withLineNum) append(y - topLeft.y + 1).append('\t')

        append(toStringFunc(this@areaToString[y][topLeft.x]))
        for (x in topLeft.x + 1..bottomRight.x) {
            append(separator)
            append(toStringFunc(this@areaToString[y][x]))
        }
        if (y != bottomRight.y) append(lineSeparator)
    }
}

@Suppress("FunctionName")
fun <T> Matrix(size: Dimension, init: (position: Point) -> T) = List(size.y) { y -> List(size.x) { x -> init(Point(x, y)) } }

// Ranges

infix fun IntRange.intersectsOrTouches(other: IntRange) = this.last + 1 >= other.first && this.first <= other.last + 1

operator fun IntRange.plus(other: IntRange) = min(this.first, other.first)..max(this.last, other.last)

operator fun <T> Set<T>.get(index: T) = contains(index)

operator fun <T> MutableSet<T>.set(index: T, value: Boolean) {
    if (value) add(index) else remove(index)
}
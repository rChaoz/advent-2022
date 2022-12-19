data class Point(val x: Int = 0, val y: Int = 0) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    companion object {
        val UP = Point(0, 1)
        val RIGHT = Point(1, 0)
        val DOWN = Point(0, -1)
        val LEFT = Point(-1, 0)
        val DIRECTIONS = arrayOf(UP, RIGHT, DOWN, LEFT)
    }
}

operator fun <T> List<List<T>>.get(at: Point) = this[at.y][at.x]
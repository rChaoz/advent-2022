import java.util.TreeSet
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Sensor(val position: Point, val closestBeacon: Point) {
    val radius = position.distanceTo(closestBeacon)
}

fun day15(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    val (rowY, maxCoords) = Regex("""y=(\d+), maxCoords=(\d+)""").matchEntire(input[0])!!.groupValues.let { it.slice(1..it.lastIndex) }.map(String::toInt)
    val sensorRegex = Regex("""Sensor at x=(-?\d+), y=(-?\d+): closest beacon is at x=(-?\d+), y=(-?\d+)""")

    var minX = 0
    var maxX = 0
    var topLeft = Point(0, 0)
    var bottomRight = Point(0, 0)

    val sensors = input.slice(1..input.lastIndex).map { line ->
        val (x1, y1, x2, y2) = sensorRegex.matchEntire(line)!!.groupValues.let { it.slice(1..it.lastIndex) }.map(String::toInt)
        Sensor(Point(x1, y1), Point(x2, y2)).also {
            val sx1 = x1 - it.radius + abs(y1 - rowY)
            val sx2 = x1 + it.radius - abs(y1 - rowY)
            if (sx1 < minX) minX = sx1
            if (sx2 > maxX) maxX = sx2

            if (x1 - it.radius < topLeft.x) topLeft = topLeft.copy(x = x1 - it.radius)
            if (x1 + it.radius > bottomRight.x) bottomRight = bottomRight.copy(x = x1 + it.radius)
            if (y1 - it.radius < topLeft.y) topLeft = topLeft.copy(y = y1 - it.radius)
            if (y1 + it.radius > bottomRight.y) bottomRight = bottomRight.copy(y = y1 + it.radius)
        }
    }

    var count = 0
    outer@ for (i in minX..maxX) {
        val p = Point(i, rowY)
        for (sensor in sensors) {
            if (p == sensor.closestBeacon) continue@outer
            else if (p.distanceTo(sensor.position) <= sensor.radius) {
                ++count
                continue@outer
            }
        }
    }
    println(count)

    // Part 2

    for (y in max(topLeft.y, 0)..min(bottomRight.y, maxCoords)) {
        val ranges = TreeSet<IntRange> { s1, s2 -> s1.first.compareTo(s2.first) }
        for (sensor in sensors) {
            val yDelta = abs(y - sensor.position.y)
            if (yDelta > sensor.radius) continue
            var range = max(sensor.position.x - sensor.radius + yDelta, 0)..min(sensor.position.x + sensor.radius - yDelta, maxCoords)
            outer@ while (true) {
                for (r in ranges) if (r intersectsOrTouches range) {
                    ranges.remove(r)
                    range += r
                    continue@outer
                }
                break
            }
            ranges.add(range)
        }
        if (ranges.isNotEmpty() && ranges.first() != 0..maxCoords) println((ranges.first().last + 1) * 4000000L + y)
    }
}

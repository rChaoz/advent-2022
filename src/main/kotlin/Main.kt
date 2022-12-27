import java.io.File
import java.io.PrintWriter

val days = listOf(::day1, ::day2, ::day3, ::day4, ::day5, ::day6, ::day7, ::day8, ::day9, ::day10, ::day11, ::day12,
    ::day13, ::day14, ::day15, ::day16, ::day17, ::day18, ::day19, ::day20, ::day21, ::day22, ::day23, ::day24)
fun dataFolder(day: Int) = File("data/day$day")

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Need exactly 1 argument - integer 1-25 or 'all'")
        return
    }

    if (args.isEmpty()) {
        runDay(days.size)
        return
    } else if (args[0] == "all") {
        for (day in 1..days.size) {
            println("Executing: day $day")
            runDay(day)
        }
        return
    }

    val day = args[0].toIntOrNull()
    if (day !in 1..25) println("Argument must be integer 1-25 or 'all'")
    else runDay(day as Int) // always Int if it passes in 1..25 check
}

private fun runDay(day: Int) {
    // Input & output files
    val folder = dataFolder(day)
    val input = File(folder, "input.txt")
    val output = File(folder, "output.txt")
    // Get day function
    val func = days[day - 1]
    // Execute day
    func(PuzzleData(input, output))
}

data class PuzzleData(val input: File, val output: File)

fun puzzle(data: PuzzleData, lambda: PrintWriter.(List<String>) -> Unit) = data.output.printWriter().use { it.lambda(data.input.readLines()) }
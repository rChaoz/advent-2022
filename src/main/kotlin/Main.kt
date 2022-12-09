import java.io.File
import java.io.PrintWriter

val days = arrayOf(::day1, ::day2, ::day3, ::day4, ::day5, ::day6, ::day7, ::day8, ::day9)
fun dataFolder(day: Int) = File("data/day$day")

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Need exactly 1 argument - integer 1-25")
        return
    }
    val day = (if (args.isNotEmpty()) args[0].toIntOrNull() else null) ?: days.size // automatically do last day if no argument
    if (day !in 1..25) {
        println("Argument must be integer 1-25")
        return
    }

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
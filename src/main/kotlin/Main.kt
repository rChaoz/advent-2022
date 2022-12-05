import java.io.File
import java.io.PrintWriter

fun main(args: Array<String>) {
    if (args.isEmpty() || args.size > 1) {
        println("Need exactly 1 argument - integer 1-25")
        return
    }
    val day = args[0].toIntOrNull()
    if (day == null || day !in 1..25) {
        println("Argument must be integer 1-25")
        return
    }

    // Input & output files
    val folder = "data/day$day"
    val input = File("$folder/input.txt")
    val output = File("$folder/output.txt")
    // Get day function
    val days = arrayOf(::day1, ::day2, ::day3)
    val func = days[day - 1]
    // Execute day
    func(PuzzleData(input, output))
}

data class PuzzleData(val input: File, val output: File)

fun puzzle(data: PuzzleData, lambda: PrintWriter.(List<String>) -> Unit) = data.output.printWriter().use { it.lambda(data.input.readLines()) }
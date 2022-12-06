import java.util.Stack

fun day5(data: PuzzleData) = puzzle(data) { input ->
    // Part 1

    // Find line with column numbers
    val colsRegex = Regex("""( \d+ {2})+ \d {0,2}""")
    val colLine = input.indexOfFirst { it.matches(colsRegex) }
    val colCount = input[colLine].split(' ').findLast { it.toIntOrNull() != null }!!.toInt()
    // Init columns
    val columns = List(colCount) { Stack<Char>() }
    for (lineNum in colLine - 1 downTo 0) {
        val line = input[lineNum]

        var i = 1
        var col = 0
        while (i < line.length) {
            val char = line[i]
            if (char != ' ') columns[col].push(char)
            ++col
            i += 4
        }
    }
    // Copy (for part 2)
    val copy = columns.map { Stack<Char>().apply { addAll(it) } }

    // Do actions
    fun doActions(columns: List<Stack<Char>>, multiple: Boolean) {
        val actionRegex = Regex("""move (\d+) from (\d+) to (\d+)""")
        for (line in input.slice(colLine + 2 until input.size)) {
            val (amount, from, to) = actionRegex.matchEntire(line)!!.groupValues.slice(1..3).map(String::toInt)
            if (multiple) {
                columns[to - 1].addAll(columns[from - 1].takeLast(amount))
                columns[from - 1].setSize(columns[from - 1].size - amount)
            } else repeat(amount) { columns[to - 1].push(columns[from - 1].pop()) }
        }
    }
    doActions(columns, false)

    // Result
    println(columns.joinToString("", transform = { it.peek().toString() }))

    // Part 2
    doActions(copy, true)
    println(copy.joinToString("", transform = { it.peek().toString() }))
}
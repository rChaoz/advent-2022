@file:Suppress("SpellCheckingInspection")

private sealed class MonkeyData {
    abstract val number: Long
    abstract val dependsOnHumn: Boolean
    abstract fun getHumnFor(value: Long): Long
}

private open class MonkeyNumber(override val number: Long) : MonkeyData() {
    override val dependsOnHumn = false
    override fun getHumnFor(value: Long): Long = throw Exception("This aint humn")
}

private class MonkeyHumn(number: Long) : MonkeyNumber(number) {
    override val dependsOnHumn = true
    override fun getHumnFor(value: Long) = value
}

private class MonkeyOperation(val monkey1Name: String, val monkey2Name: String, val operation: String) : MonkeyData() {
    val monkey1 by lazy { monkeys[monkey1Name]!! }
    val monkey2 by lazy { monkeys[monkey2Name]!! }

    private val operationFun: (Long, Long) -> Long = when (operation) {
        "+" -> Long::plus
        "-" -> Long::minus
        "*" -> Long::times
        "/" -> Long::div
        else -> throw Exception("Unknown operation: $operation")
    }

    override val number by lazy { operationFun(monkey1.number, monkey2.number) }
    override val dependsOnHumn by lazy { monkey1.dependsOnHumn || monkey2.dependsOnHumn }

    override fun getHumnFor(value: Long): Long {
        val (num, monkey) = if (monkey1.dependsOnHumn) monkey2.number to monkey1 else monkey1.number to monkey2
        return when (operation) {
            "+" -> monkey.getHumnFor(value - num)
            "*" -> monkey.getHumnFor(value / num)
            "-" -> monkey.getHumnFor(if (monkey === monkey1) value + num else num - value)
            "/" -> monkey.getHumnFor(if (monkey === monkey1) value * num else num / value)
            else -> throw Exception("Unknown operation: $operation")
        }
    }
}

private val monkeys = HashMap<String, MonkeyData>()

fun day21(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    val monkeyRegex = Regex("""([a-z]{4}): (\d+|[a-z]{4} [+\-*/] [a-z]{4})""")
    val opRegex = Regex("""([a-z]{4}) ([+\-*/]) ([a-z]{4})""")
    for (line in input) {
        val (monkey, opOrNum) = monkeyRegex.matchEntire(line)!!.destructured
        val num = opOrNum.toLongOrNull()
        monkeys[monkey] = if (num != null) {
            if (monkey == "humn") MonkeyHumn(num)
            else MonkeyNumber(num)
        } else {
            val (monkey1, op, monkey2) = opRegex.matchEntire(opOrNum)!!.destructured
            MonkeyOperation(monkey1, monkey2, op)
        }

    }
    println(monkeys["root"]!!.number)

    // Part 2
    val root = monkeys["root"] as MonkeyOperation
    val (num, monkey) = if (root.monkey1.dependsOnHumn) root.monkey2.number to root.monkey1 else root.monkey1.number to root.monkey2
    println(monkey.getHumnFor(num))
}

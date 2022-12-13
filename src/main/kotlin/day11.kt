class Shenanigans(private val shouldDivide: Boolean) {
    val monkeys = mutableListOf<Monkey>()
    private var maxMultiplier = Int.MAX_VALUE

    inner class Monkey(val operation: (item: Long) -> Long, val monkeyTarget: (item: Long) -> Int, startingItems: List<Long>) {
        var totalInspections = 0L
        private val items = ArrayDeque(startingItems)

        fun doRound() {
            repeat(items.size) { items.add(operation(items.removeFirst()).let { if (shouldDivide) it / 3 else it % maxMultiplier }) }
            while (items.isNotEmpty()) {
                ++totalInspections
                items.removeFirst().let { monkeys[monkeyTarget(it)].items.add(it) }
            }
        }
    }

    fun doRound() = monkeys.forEach(Monkey::doRound)

    fun init(lines: List<String>) {
        // Regexes
        //val monkeyNumR = Regex("""Monkey (\d+):""")
        val startingItemsR = Regex(""" *Starting items: (.*)$""")
        val operationR = Regex(""" *Operation: new = (.*)$""")
        val testDivisibleR = Regex(""" *Test: divisible by (\d+)""")
        val ifR = Regex(""" *If (?:true|false): throw to monkey (\d+)""")

        var i = 0
        var maxMult = 1
        while (i < lines.size) {
            /*val monkeyNum = monkeyNumR.matchEntire(lines[i++])!!.groupValues[1].toInt()*/ ++i
            val startingItems = startingItemsR.matchEntire(lines[i++])!!.groupValues[1].split(", ").map(String::toLong)
            val (opS) = operationR.matchEntire(lines[i++])!!.destructured
            val operation = opS.replace(" ", "").let { op ->
                fun processAdditions(str: String): (item: Long) -> Long {
                    val nums = str.split('+')
                    val olds = nums.count { it == "old" }
                    val sum = nums.mapNotNull(String::toIntOrNull).sum()
                    return { item: Long -> item * olds + sum }
                }

                val muls = op.split('*').map(::processAdditions);
                { item: Long -> muls.map { it(item) }.fold(1, Long::times) }
            }
            val testNum = testDivisibleR.matchEntire(lines[i++])!!.groupValues[1].toInt().also { maxMult *= it }
            val trueMonkey = ifR.matchEntire(lines[i++])!!.groupValues[1].toInt()
            val falseMonkey = ifR.matchEntire(lines[i++])!!.groupValues[1].toInt()
            val test = { item: Long -> if (item % testNum == 0L) trueMonkey else falseMonkey }
            monkeys.add(Monkey(operation, test, startingItems))
            ++i
        }
        maxMultiplier = maxMult
    }
}

fun day11(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    val game = Shenanigans(true).apply { init(input) }
    repeat(20) { game.doRound() }
    println(game.monkeys.map(Shenanigans.Monkey::totalInspections).sorted().takeLast(2).let { it[0] * it[1] })
    // Part 2
    val newGame = Shenanigans(false).apply { init(input) }
    repeat(10_000) { newGame.doRound() }
    println(newGame.monkeys.map(Shenanigans.Monkey::totalInspections).sorted().takeLast(2).let { it[0] * it[1] })
}

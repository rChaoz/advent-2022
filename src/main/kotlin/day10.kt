import java.lang.StringBuilder

private class Computer(private val log: ((cycle: Int, x: Int) -> Unit)?) {
    private var instructions: List<Instruction>? = null
    private var cycle = 0
    private var x = 1

    fun execute() {
        var counter = 0
        val ins = instructions ?: throw NullPointerException()
        while (counter < ins.size) {
            val i = ins[counter]
            ++cycle
            ++i.cycle
            log?.let { it(cycle, x) }
            if (i.execute()) ++counter
        }
    }

    abstract inner class Instruction {
        var cycle = 0
        abstract fun execute(): Boolean
    }

    inner class NoOp : Instruction() {
        override fun execute() = true
    }

    inner class AddX(private val x: Int) : Instruction() {
        override fun execute(): Boolean {
            if (cycle == 2) {
                this@Computer.x += this.x
                return true
            }
            return false
        }
    }

    companion object {
        fun fromLines(lines: List<String>, log: ((cycle: Int, x: Int) -> Unit)? = null) = Computer(log).apply {
            instructions = lines.map {
                val line = it.split(' ')
                when (line[0]) {
                    "noop" -> NoOp()
                    "addx" -> AddX(line[1].toInt())
                    else -> throw Exception("Invalid instruction")
                }
            }
        }
    }
}

fun day10(data: PuzzleData) = puzzle(data) { input ->
    // Part 1 & 2
    var sum = 0
    val screen = Array(6) { StringBuilder() }

    Computer.fromLines(input) { cycle, x ->
        if (cycle >= 20 && (cycle - 20) % 40 == 0) sum += cycle * x
        val pixel = cycle - 1
        screen[pixel / 40].append(if (pixel % 40 in x-1..x+1) '#' else '.')
    }.execute()
    println(sum)
    println(screen.joinToString("\n"))
}
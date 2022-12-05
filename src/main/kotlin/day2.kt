private enum class Choice(private val c1: Char, private val c2: Char, val score: Int, private val beatsI: Int, private val losesI: Int) {
    ROCK('A', 'X', 1, 2, 1), PAPER('B', 'Y', 2, 0, 2), SCISSORS('C', 'Z', 3, 1, 0);

    val beats by lazy { values()[beatsI] }
    val loses by lazy { values()[losesI] }
    val draws by lazy { values()[intArrayOf(0, 1, 2).find{ it != beatsI && it != losesI }!!] }

    fun fight(other: Choice) = when {
        beats == other -> Result.WIN
        loses == other -> Result.LOSE
        else -> Result.DRAW
    }

    companion object {
        fun fromChar(char: Char) = values().find { it.c1 == char || it.c2 == char }!!
    }
}

private enum class Result(val score: Int, val char: Char) {
    WIN(6, 'Z'), DRAW(3, 'Y'), LOSE(0, 'X');

    companion object {
        fun fromChar(char: Char) = values().find { it.char == char }!!
    }
}

private class Round(private val opponent: Choice, yourChoice: Choice, requiredResult: Result) {

    constructor(c1: Char, c2: Char) : this(Choice.fromChar(c1), Choice.fromChar(c2), Result.fromChar(c2))

    private fun result(you: Choice) = you.fight(opponent)

    val score1 = result(yourChoice).score + yourChoice.score
    val score2 = when (requiredResult) {
        Result.WIN -> 6 + opponent.loses.score
        Result.LOSE -> opponent.beats.score
        else -> 3 + opponent.draws.score
    }
}

fun day2(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    val rounds = input.map { Round(it[0], it[2]) }
    val score = rounds.map(Round::score1).sum()
    println(score)
    // Part 2
    println(rounds.map(Round::score2).sum())
}
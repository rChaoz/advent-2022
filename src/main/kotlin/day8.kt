import kotlin.math.abs

private class Tree(val height: Int) {
    var visible = false
    private val scenic = HashMap<Pair<Int, Int>, Int>()

    val scenicScore get() = scenic.values.reduce(Int::times)

    fun updateScenic(direction: Pair<Int, Int>, score: Int) = scenic[direction].let {
        if (it == null || it < score) scenic[direction] = score
    }
}

fun day8(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    val trees = input.map { line -> line.map { Tree(it.digitToInt()) } }
    val directions = arrayOf(0 to 1, 1 to 0, 0 to -1, -1 to 0)

    fun circle(lambda: (x: Int, y: Int) -> Unit) {
        for (x in 0..trees[0].lastIndex) lambda(x, 0)
        for (y in 1..trees.lastIndex) lambda(trees[0].lastIndex, y)
        for (x in trees[0].lastIndex - 1 downTo 0) lambda(x, trees.lastIndex)
        for (y in trees.lastIndex - 1 downTo 1) lambda(0, y)
    }

    fun checkTrees(startX: Int, startY: Int, direction: Pair<Int, Int>) {
        var prevHeight = trees[startY][startX].let { it.visible = true; it.height }
        var x = startX + direction.first
        var y = startY + direction.second

        while (x in trees[0].indices && y in trees.indices) {
            if (trees[y][x].height > prevHeight) {
                prevHeight = trees[y][x].height
                trees[y][x].visible = true
            }

            x += direction.first
            y += direction.second
        }
    }

    circle { x, y -> directions.forEach { checkTrees(x, y, it) } }
    println(trees.sumOf { it.count(Tree::visible) })

    // Part 2

    fun checkTreesAgain(startX: Int, startY: Int, direction: Pair<Int, Int>) {
        val treeHeights = HashMap<Int, Int>()
        for (i in 0..9) treeHeights[i] = if (direction.first == 0) startY else startX
        trees[startY][startX].updateScenic(direction, 0)

        var x = startX + direction.first
        var y = startY + direction.second

        while (x in trees[0].indices && y in trees.indices) {
            val h = trees[y][x].height
            val index = treeHeights[h]!!
            trees[y][x].updateScenic(direction, if (direction.first == 0) abs(y - index) else abs(x - index))
            for (i in 0..h) treeHeights[i] = if (direction.first == 0) y else x

            x += direction.first
            y += direction.second
        }
    }
    circle { x, y -> directions.forEach { checkTreesAgain(x, y, it) } }

    println(trees.maxOf { it.maxOf(Tree::scenicScore) })
}
fun day18(data: PuzzleData) = puzzle(data) { input ->
    // Part 1

    val maxCoordinate = 21
    val cubeMap = MutableList(maxCoordinate + 1) { MutableList(maxCoordinate + 1) { MutableList(maxCoordinate + 1) { 0 } } }
    val directions = listOf(
        Triple(1, 0, 0), Triple(-1, 0, 0),
        Triple(0, 1, 0), Triple(0, -1, 0),
        Triple(0, 0, 1), Triple(0, 0, -1),
    )

    var faceCount = 0

    for (line in input) {
        val (x, y, z) = line.split(',').map(String::toInt)
        var faces = 6
        for (d in directions)
            if (x + d.first >= 0 && y + d.second >= 0 && z + d.third >= 0
                && x + d.first <= maxCoordinate && y + d.second <= maxCoordinate && z + d.third <= maxCoordinate
                && cubeMap[x + d.first][y + d.second][z + d.third] == 1
            ) faces -= 2
        cubeMap[x][y][z] = 1
        faceCount += faces
    }
    println(faceCount)

    // Part 2
    var airFaces = 0

    fun dfs(x: Int, y: Int, z: Int): Boolean {
        if (x == 0 || x == maxCoordinate || y == 0 || y == maxCoordinate || z == 0 || z == maxCoordinate) return true
        cubeMap[x][y][z] = 2

        airFaces += 6
        for (d in directions) when (cubeMap[x + d.first][y + d.second][z + d.third]) {
            3 -> return true
            2 -> airFaces -= 2
        }
        for (d in directions) if (cubeMap[x + d.first][y + d.second][z + d.third] == 0 && dfs(x + d.first, y + d.second, z + d.third)) return true
        return false
    }

    for (x in 0..maxCoordinate) for (y in 0..maxCoordinate) for (z in 0..maxCoordinate) {
        if (cubeMap[x][y][z] != 0) continue
        airFaces = 0
        if (dfs(x, y, z)) {
            val q = ArrayDeque<Triple<Int, Int, Int>>()
            q.add(Triple(x, y, z))

            while (q.isNotEmpty()) {
                val (px, py, pz) = q.removeFirst()
                for (d in directions) {
                    if (px + d.first >= 0 && py + d.second >= 0 && pz + d.third >= 0
                        && px + d.first <= maxCoordinate && py + d.second <= maxCoordinate && pz + d.third <= maxCoordinate
                        && cubeMap[px + d.first][py + d.second][pz + d.third] != 1 && cubeMap[px + d.first][py + d.second][pz + d.third] != 3
                    ) {
                        cubeMap[px + d.first][py + d.second][pz + d.third] = 3
                        q.add(Triple(px + d.first, py + d.second, pz + d.third))
                    }
                }
            }
        }
        else faceCount -= airFaces
    }
    println(faceCount)
}

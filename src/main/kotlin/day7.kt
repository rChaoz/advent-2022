private sealed class Item(private val _parent: Folder?) {
    val parent get() = _parent!!
}

private class File(parent: Folder, val size: Int) : Item(parent) {
}

private class Folder(parent: Folder?, val children: MutableMap<String, Item> = HashMap()) :
    Item(parent), MutableMap<String, Item> by children {

    private var computedSize = 0
    val folderSize get() = computedSize

    fun createFile(name: String, size: Int) {
        children.putIfAbsent(name, File(this, size))
    }

    fun createFolder(name: String) {
        children.putIfAbsent(name, Folder(this))
    }

    fun computeSizes(): Int {
        computedSize = children.values.sumOf {
            when (it) {
                is File -> it.size
                is Folder -> it.computeSizes()
            }
        }
        return computedSize
    }
}

fun day7(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    var nextLine = 0
    val root = Folder(null)
    var currentFolder = root

    fun cd(arg: String) {
        currentFolder = when (arg) {
            ".." -> currentFolder.parent
            "/" -> root
            else -> currentFolder[arg] as Folder
        }
    }

    fun ls() {
        while (nextLine < input.size && input[nextLine][0] != '$') {
            val (sizeS, name) = input[nextLine].split(' ')
            if (sizeS == "dir") currentFolder.createFolder(name)
            else currentFolder.createFile(name, sizeS.toInt())
            ++nextLine
        }
    }

    while (nextLine < input.size) {
        val line = input[nextLine++]
        if (line[0] != '$') throw Exception("Expected command on line $nextLine")
        val command = line.substring(2).split(' ')
        when (command[0]) {
            "cd" -> cd(command[1])
            "ls" -> ls()
        }
    }

    root.computeSizes()
    var sizeSum = 0

    fun findDirs(dir: Folder) {
        if (dir.folderSize <= 100000) sizeSum += dir.folderSize
        for (child in dir.values) if (child is Folder) findDirs(child)
    }

    findDirs(root)
    println(sizeSum)

    // Part 2
    val requiredSpace = 30000000 - (70000000 - root.folderSize)
    if (requiredSpace <= 0) throw Exception("No folder needs to be deleted")
    var smallestDir = root

    fun findSmallestDir(dir: Folder) {
        if (dir.folderSize < smallestDir.folderSize && dir.folderSize >= requiredSpace) smallestDir = dir
        for (child in dir.values) if (child is Folder) findSmallestDir(child)
    }

    findSmallestDir(root)
    println(smallestDir.folderSize)
}
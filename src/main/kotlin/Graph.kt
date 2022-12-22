data class Edge<T, C : Number>(val from: T, val to: T, val cost: C)

data class Graph<T, C : Number>(val vertices: Set<T>, val edges: List<Edge<T, C>>, val directed: Boolean, private val zero: C, private val plus: (C, C) -> C) {
    val adjLists: Map<T, List<Edge<T, C>>>
    private val edgeIds: Map<T, Int>
    private val adjMatrix: Matrix<C?>

    fun getCost(from: T, to: T) = adjMatrix[edgeIds[from]!!][edgeIds[to]!!]

    fun hasEdge(from: T, to: T) = getCost(from, to) != null

    fun bfs(startingPoint: T, onVisit: (T, C) -> Unit) {
        val q = ArrayDeque<Pair<T, C>>()
        val vis = HashSet<T>()
        q.add(startingPoint to zero)
        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            onVisit(v.first, v.second)
            for (to in adjLists[v.first]!!) {
                if (vis[to.to]) continue
                vis[to.to] = true
                q.add(to.to to plus(v.second, to.cost))
            }
        }
    }

    init {
        // Create matrix
        val matrix = ArrayList<ArrayList<C?>>()
        for (i in vertices.indices) {
            val row = ArrayList<C?>()
            matrix.add(row)
            for (j in vertices.indices) row.add(null)
        }
        // Init adjacency lists & matrix
        adjLists = buildMap<T, MutableList<Edge<T, C>>> outer@{
            edgeIds = buildMap {
                for ((id, vertex) in vertices.withIndex()) {
                    this@outer[vertex] = ArrayList()
                    this[vertex] = id
                }
            }
            for (edge in edges) {
                this[edge.from]!!.add(edge)
                matrix[edgeIds[edge.from]!!][edgeIds[edge.to]!!] = edge.cost
                if (!directed) {
                    this[edge.to]!!.add(Edge(edge.to, edge.from, edge.cost))
                    matrix[edgeIds[edge.to]!!][edgeIds[edge.from]!!] = edge.cost
                }
            }
            adjMatrix = matrix
        }
    }
}

class GraphBuilder<T, C : Number>(private val directed: Boolean = true, private val zero: C, private val sum: (C, C) -> C) {
    private val vertices = HashSet<T>()
    private val edges = ArrayList<Edge<T, C>>()

    fun addEdge(from: T, to: T, cost: C): GraphBuilder<T, C> {
        vertices.add(from)
        vertices.add(to)
        edges.add(Edge(from, to, cost))
        return this
    }

    fun build() = Graph(vertices, edges, directed, zero, sum)
}

@Suppress("UNCHECKED_CAST")
inline fun <T, reified C : Number> buildGraph(directed: Boolean = true, builderAction: GraphBuilder<T, C>.() -> Unit): Graph<T, C> {
    val (zero, plus) = when (C::class) {
        Short::class -> 0.toShort() to { a: Short, b: Short -> a + b }
        Int::class -> 0 to { a: Int, b: Int -> a + b }
        Long::class -> 0L to { a: Long, b: Long -> a + b }
        Float::class -> 0f to { a: Float, b: Float -> a + b }
        Double::class -> 0.0 to { a: Double, b: Double -> a + b }
        Char::class -> 0.toChar() to { a: Char, b: Char -> a + b.code }
        Byte::class -> 0.toByte() to { a: Byte, b: Byte -> a + b }
        else -> throw Exception("Unknown number type: " + C::class)
    } as Pair<C, (C, C) -> C>
    return GraphBuilder<T, C>(directed, zero, plus).run {
        builderAction()
        build()
    }
}
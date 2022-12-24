import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

private data class Blueprint(val id: Int, val oreRobotCost: Int, val clayRobotCost: Int, val obsidianRobotCost: Pair<Int, Int>, val geodeRobotCost: Pair<Int, Int>)

private data class Resources(
    var ore: Int = 0, var clay: Int = 0, var obsidian: Int = 0, var geodes: Int = 0,
    var oreRobots: Int = 1, var clayRobots: Int = 0, var obsidianRobots: Int = 0, var geodeRobots: Int = 0,
) {

    fun collectResources() {
        ore += oreRobots
        clay += clayRobots
        obsidian += obsidianRobots
        geodes += geodeRobots
    }

    fun tryCreatingOreRobot(oreCost: Int) =
        if (ore >= oreCost) copy(ore = ore - oreCost - 1, oreRobots = oreRobots + 1)
        else null

    fun tryCreatingClayRobot(oreCost: Int) =
        if (ore >= oreCost) copy(ore = ore - oreCost, clay = clay - 1, clayRobots = clayRobots + 1)
        else null

    fun tryCreatingObsidianRobot(oreCost: Int, clayCost: Int) =
        if (ore >= oreCost && clay >= clayCost)
            copy(ore = ore - oreCost, clay = clay - clayCost, obsidian = obsidian - 1, obsidianRobots = obsidianRobots + 1)
        else null

    fun tryCreatingGeodeRobot(oreCost: Int, obsidianCost: Int) =
        if (ore >= oreCost && obsidian >= obsidianCost)
            copy(ore = ore - oreCost, obsidian = obsidian - obsidianCost, geodes = geodes - 1, geodeRobots = geodeRobots + 1)
        else null
}

fun day19(data: PuzzleData) = puzzle(data) { input ->
    val blueprintRegex = Regex(
        """Blueprint (\d+): Each ore robot costs (\d+) ore\. Each clay robot costs (\d+) ore\. """ +
                """Each obsidian robot costs (\d+) ore and (\d+) clay\. Each geode robot costs (\d+) ore and (\d+) obsidian\."""
    )
    val blueprints = input.map { line ->
        val (id, oreCost, clayCost, obsCost1, obsCost2, geodeCost1, geodeCost2) = blueprintRegex.matchEntire(line)!!.destructured
        Blueprint(id.toInt(), oreCost.toInt(), clayCost.toInt(), obsCost1.toInt() to obsCost2.toInt(), geodeCost1.toInt() to geodeCost2.toInt())
    }

    var maxMinutes = 24
    val qualityScore = AtomicInteger()
    val multiplicationScore = AtomicInteger()

    class BacktrackingThread(private val bp: Blueprint) : Thread() {
        var maxGeodes = 0

        val maxOreRobots = maxOf(bp.clayRobotCost, bp.obsidianRobotCost.first, bp.geodeRobotCost.first)
        val maxClayRobots = bp.obsidianRobotCost.second
        val maxObsidianRobots = bp.geodeRobotCost.second

        override fun run() {
            backtrack(1, Resources())
            qualityScore.getAndAdd(maxGeodes * bp.id)

            var old = multiplicationScore.get()
            while (!multiplicationScore.compareAndSet(old, old * maxGeodes)) old = multiplicationScore.get()
        }

        fun backtrack(minute: Int, r: Resources) {
            if (r.oreRobots > maxOreRobots || r.clayRobots > maxClayRobots || r.obsidianRobots > maxObsidianRobots) return

            val geode = r.tryCreatingGeodeRobot(bp.geodeRobotCost.first, bp.geodeRobotCost.second)
            val obsidian = r.tryCreatingObsidianRobot(bp.obsidianRobotCost.first, bp.obsidianRobotCost.second)
            val clay = r.tryCreatingClayRobot(bp.clayRobotCost)

            val variants = if (geode != null) listOf(geode)
            else if (obsidian != null && r.ore >= bp.obsidianRobotCost.first + bp.geodeRobotCost.first - r.oreRobots) listOf(obsidian)
            else if (clay != null && r.ore >= bp.clayRobotCost + bp.obsidianRobotCost.first + bp.geodeRobotCost.first - r.oreRobots) listOf(clay)
            else buildList {
                add(r)
                r.tryCreatingOreRobot(bp.oreRobotCost)?.let { add(it) }
                if (clay != null) add(clay)
                if (obsidian != null) add(obsidian)
            }

            variants.forEach(Resources::collectResources)
            for (v in variants) {
                if (minute < maxMinutes) backtrack(minute + 1, v)
                else if (v.geodes > maxGeodes) maxGeodes = v.geodes
            }
        }
    }

    val threads = buildList {
        for (bp in blueprints) add(BacktrackingThread(bp).apply { start() })
    }
    for (thread in threads) thread.join()
    println(qualityScore.get())
    flush()

    // Part 2
    maxMinutes = 32
    multiplicationScore.set(1)

    val threads2 = buildList {
        for (bp in blueprints.slice(0..min(2, blueprints.lastIndex))) add(BacktrackingThread(bp).apply { start() })
    }
    for (thread in threads2) thread.join()

    println(multiplicationScore.get())
}

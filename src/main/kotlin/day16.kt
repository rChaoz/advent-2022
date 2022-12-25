import java.lang.Exception
import kotlin.math.min

private data class ValveData(val flow: Int, var open: Boolean = false) // val open is used by greedy solution

@Suppress("ConstantConditionIf")
fun day16(puzzleData: PuzzleData) = puzzle(puzzleData) { input ->
    // Part 1

    val valveRegex = Regex("""Valve ([A-Z]{2}) has flow rate=(\d+); tunnels? leads? to valves? ((?:[A-Z]{2}(?:, )?)*)""")
    val valveData = HashMap<String, ValveData>()
    val graph = buildGraph {
        for (line in input) {
            val (valve, flowRateS, tunnelsS) = valveRegex.matchEntire(line)!!.destructured
            val flowRate = flowRateS.toInt()
            val tunnels = tunnelsS.split(", ")

            valveData[valve] = ValveData(flowRate)
            for (tunnel in tunnels) addEdge(valve, tunnel, 1)
        }
    }

    data class PickedValve(val valve: String, val pressure: Int, val newMinute: Int)

    var totalMinutes = 30
    // GREEDY solution, bad
    if (false) run {
        // At every minute, find the best closed valve
        var minute = 0
        var totalPressure = 0
        var location = "AA"

        while (minute < totalMinutes) {
            var bestValve = PickedValve("??", 0, 0)
            graph.bfs(location) { valve, distance ->
                // Skip open valves & valves we can't reach in time
                val newMinute = minute + distance + 1
                val data = valveData[valve]!!
                if (data.open || newMinute > totalMinutes) return@bfs
                // Calculate how much pressure this valve would release if opened now
                val pressure = (totalMinutes - newMinute) * data.flow
                if (pressure > bestValve.pressure) bestValve = PickedValve(valve, pressure, newMinute)
            }
            // Open the valve (if there is none, stop)
            if (bestValve.valve == "??") break
            minute = bestValve.newMinute
            totalPressure += bestValve.pressure
            valveData[bestValve.valve]!!.open = true
            location = bestValve.valve
        }

        println(totalPressure)
    }

    // Backtracking solution, MID

    var maxPressure = 0
    fun backtrack(minute: Int, location: String, pressureSoFar: Int, openedValves: Set<String>) {
        // Try and open a new valve
        graph.bfs(location) { valve, distance ->
            val newMinute = minute + distance + 1
            val flow = valveData[valve]!!.flow
            if (valve in openedValves || newMinute > totalMinutes || flow == 0) return@bfs
            // Calculate pressure
            val newPressure = pressureSoFar + (totalMinutes - newMinute) * flow
            if (newPressure > maxPressure) maxPressure = newPressure
            // Recursive call
            if (newMinute < totalMinutes) backtrack(newMinute, valve, newPressure, openedValves + valve)
        }
    }
    backtrack(0, "AA", 0, HashSet())
    println(maxPressure)

    // Part 2 (slow but works)
    maxPressure = 0
    totalMinutes = 26
    fun doubleBacktrack(minute: Int, location: String, elephantLocation: String, timeFinish: Int, elephantTimeFinish: Int, pressureSoFar: Int, openedValves: Set<String>) {
        val myValves = ArrayList<PickedValve>()
        val elephantValves = ArrayList<PickedValve>()

        var valves = myValves
        val bfsFunc = { valve: String, distance: Int ->
            val newMinute = minute + distance + 1
            val flow = valveData[valve]!!.flow
            // Save valve
            if (valve !in openedValves && newMinute <= totalMinutes && flow != 0 && newMinute < totalMinutes)
                valves.add(PickedValve(valve, (totalMinutes - newMinute) * flow, newMinute))
        }

        if (timeFinish == minute) {
            // I pick a valve to open
            graph.bfs(location, bfsFunc)
        }
        if (elephantTimeFinish == minute) {
            // The elephant picks a valve to open
            valves = elephantValves
            graph.bfs(elephantLocation, bfsFunc)
        }

        if (timeFinish == minute && elephantTimeFinish == minute) {
            // We each pick a valve
            for (myValve in myValves) for (elephantValve in elephantValves) {
                if (myValve.valve == elephantValve.valve) continue
                val newPressure = pressureSoFar + myValve.pressure + elephantValve.pressure
                if (newPressure > maxPressure) maxPressure = newPressure
                doubleBacktrack(min(myValve.newMinute, elephantValve.newMinute), myValve.valve, elephantValve.valve,
                    myValve.newMinute, elephantValve.newMinute, newPressure, openedValves + arrayOf(myValve.valve, elephantValve.valve))
            }
        } else if (timeFinish == minute) {
            // I pick a new valve
            for (valve in myValves) {
                val newPressure = pressureSoFar + valve.pressure
                if (newPressure > maxPressure) maxPressure = newPressure
                doubleBacktrack(min(valve.newMinute, elephantTimeFinish), valve.valve, elephantLocation,
                    valve.newMinute, elephantTimeFinish, newPressure, openedValves + valve.valve)
            }
        } else if (elephantTimeFinish == minute) {
            // The elephant picks a new valve
            for (valve in elephantValves) {
                val newPressure = pressureSoFar + valve.pressure
                if (newPressure > maxPressure) maxPressure = newPressure
                doubleBacktrack(min(valve.newMinute, timeFinish), location, valve.valve,
                    timeFinish, valve.newMinute, newPressure, openedValves + valve.valve)
            }
        } else throw Exception("????????")
    }
    doubleBacktrack(0, "AA", "AA", 0, 0, 0, HashSet())

    println(maxPressure)
}

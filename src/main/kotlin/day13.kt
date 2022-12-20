import kotlin.math.min
import kotlin.math.sign

sealed class Element : Comparable<Element> {
    private fun compareLists(a: ElementList, b: ElementList): Int {
        for (i in 0..min(a.lastIndex, b.lastIndex)) a[i].compareTo(b[i]).takeIf { it != 0 }?.let { return it }
        return (a.size - b.size).sign
    }

    override fun compareTo(other: Element) = when (this) {
        is ElementList -> compareLists(this, when (other) {
            is ElementList -> other
            is ElementNumber -> other.listValue
        })
        is ElementNumber -> when (other) {
            is ElementList -> compareLists(listValue, other)
            is ElementNumber -> value.compareTo(other.value)
        }
    }

    companion object {
        private fun parseInternal(str: String, index: Int): Pair<Element, Int> {
            // Check if element is number
            if (str[index].isDigit()) {
                val end = str.indexOfAny(charArrayOf(',', ']'), index).let { if (it != -1) it else str.length }
                val elem = ElementNumber(str.substring(index, end).toInt())
                return elem to if (end < str.length && str[end] == ',') end + 1 else end
            }
            // Else it must be list, skip the opening bracket
            var i = index + 1
            return ElementList(buildList {
                while (i < str.length && str[i] != ']') {
                    val (newElem, newI) = parseInternal(str, i)
                    add(newElem)
                    i = newI
                }
            }) to i + 2
        }

        fun parse(input: String): Element = parseInternal(input, 0).first
    }
}

data class ElementList(private val values: List<Element>) : Element(), List<Element> by values {
    constructor(value: Element) : this(listOf(value))

    override fun toString() = values.toString()
}

data class ElementNumber(val value: Int) : Element() {
    val listValue = ElementList(this)

    override fun toString() = value.toString()
}

fun day13(data: PuzzleData) = puzzle(data) { input ->
    // Part 1
    val elementPairs = buildList {
        var i = 0
        while (i < input.size) {
            add(Element.parse(input[i]) to Element.parse(input[i + 1]))
            i += 3
        }
    }

    var sum = 0
    elementPairs.forEachIndexed { index, pair -> if (pair.first < pair.second) sum += index + 1 }
    println(sum)

    // Part 2
    val separators = arrayOf(Element.parse("[[2]]"), Element.parse("[[6]]"))
    val elements = (input.filterNot { it.isBlank() }.map(Element::parse) + separators).sorted()
    println(separators.map { elements.indexOf(it) + 1 }.reduce(Int::times))
}

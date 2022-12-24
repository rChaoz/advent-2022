/**
 * @constructor
 * Note that the Circular list is backed by the parameter list, any changes done in it will be reflected in this one and vice-versa.
 */
open class CircularList<T>(private val base: List<T>) : List<T> {
    override val size get() = base.size

    protected fun baseIndex(index: Int) = (index % size).let { if (it < 0) it + size else it }

    override fun get(index: Int) = base[baseIndex(index)]

    override fun isEmpty() = base.isEmpty()

    override fun iterator() = base.iterator()

    override fun listIterator() = base.listIterator()

    override fun listIterator(index: Int) = base.listIterator(baseIndex(index))

    override fun subList(fromIndex: Int, toIndex: Int) = CircularList(base.subList(baseIndex(fromIndex), baseIndex(toIndex)))

    override fun lastIndexOf(element: T) = base.lastIndexOf(element)

    override fun indexOf(element: T) = base.indexOf(element)

    override fun containsAll(elements: Collection<T>) = base.containsAll(elements)

    override fun contains(element: T) = base.contains(element)
}

/**
 * @constructor
 * Note that the Circular list is backed by the parameter list, any changes done in it will be reflected in this one and vice-versa.
 */
class CircularMutableList<T>(private val base: MutableList<T>) : CircularList<T>(base), MutableList<T> {
    override fun add(element: T) = base.add(element)

    override fun add(index: Int, element: T) {
        if (index == size) base.add(element) else base.add(baseIndex(index), element)
    }

    override fun addAll(index: Int, elements: Collection<T>) = if (index == size) base.addAll(elements) else base.addAll(baseIndex(index), elements)

    override fun addAll(elements: Collection<T>) = base.addAll(elements)

    override fun clear() = base.clear()

    override fun iterator() = base.iterator()

    override fun listIterator() = base.listIterator()

    override fun listIterator(index: Int) = base.listIterator(baseIndex(index))

    override fun removeAt(index: Int) = base.removeAt(baseIndex(index))

    override fun subList(fromIndex: Int, toIndex: Int) = CircularMutableList(base.subList(baseIndex(fromIndex), baseIndex(toIndex)))

    override fun set(index: Int, element: T) = base.set(baseIndex(index), element)

    override fun retainAll(elements: Collection<T>) = base.retainAll(elements)

    override fun removeAll(elements: Collection<T>) = base.removeAll(elements)

    override fun remove(element: T) = base.remove(element)
}
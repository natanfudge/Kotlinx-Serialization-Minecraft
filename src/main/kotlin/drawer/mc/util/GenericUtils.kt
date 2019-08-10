package drawer.mc.util

import kotlin.math.max
import kotlin.math.sqrt

infix fun IntRange.by(range: IntRange): List<Pair<Int, Int>> = this.flatMap { x -> range.map { y -> Pair(x, y) } }
fun IntRange.squared() = this by this

operator fun Pair<Int, Int>.rangeTo(that: Pair<Int, Int>) = object : Iterable<Pair<Int, Int>> {
    override fun iterator() = object : Iterator<Pair<Int, Int>> {
        var i = this@rangeTo.first
        var j = this@rangeTo.second

        val m = that.first - i
        val n = that.second - j

        override fun hasNext() = i <= m && j <= n

        override fun next(): Pair<Int, Int> {
            val res = i to j

            if (j == n) {
                j = 0
                i++
            }
            else {
                j++
            }

            return res
        }
    }
}

fun Double.isWholeNumber() = this.toInt().toDouble() == this

fun max(num1: Int, num2: Int, num3: Int): Int = max(max(num1, num2), num3)

infix fun Pair<Int, Int>.until(that: Pair<Int, Int>) = this..Pair(that.first - 1, that.second - 1)
data class Point(val x : Int, val y: Int, val z :Int)

fun cubeSized(size :Int) : List<Point>{
    return (0 until size).flatMap{ x -> (0 until size).flatMap {y-> (0 until size).map {z->
        Point(
            x,
            y,
            z
        )
    } } }
}

/**
 * Appends all elements yielded from results of [transform] function being invoked on each element of original collection, to the given [destination].
 */
inline fun <T, R> Iterable<T>.flatMapIndexed(transform: (Int, T) -> Iterable<R>): List<R> {
    val destination = ArrayList<R>()
    var index = 0
    for (item in this)
        destination.addAll(transform(index++, item))

    return destination
}

/**
 * Appends all elements yielded from results of [transform] function being invoked on each element of original collection, to the given [destination].
 */
inline fun <T, R> Iterable<T>.mapIndexed(transform: (Int, T) -> R): List<R> {
    val destination = ArrayList<R>()
    var index = 0
    for (item in this)
        destination.add(transform(index++, item))

    return destination
}

//TODO: turn this off in production
const val assertionsEnabled = true

inline fun assert(message: String = "Assertion failure", test: () -> Boolean) {
    if (assertionsEnabled && !test()) throw AssertionError(message)
}

inline fun Int.squared() = this * this
inline fun Double.squared() = this * this
inline fun sqrt(num: Int) : Double = sqrt(num.toDouble())

//inline fun<T> min(comparable: Comparable<T>)

val Int.d get() = this.toDouble()
val Long.d get() = this.toDouble()
val Int.f get() = this.toFloat()
val Float.d get() = this.toDouble()

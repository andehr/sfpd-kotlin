import com.google.common.collect.Ordering

/**
 * Created by Andrew D. Robertson on 26/12/2020.
 */

/**************************
 * Ordering by selector
 **************************/

// Static functions for passing iterables
fun <T> greatestOf(items : Iterable<T>, n: Int, selector: (T?) -> Comparable<*>?): List<T> =
    orderingFrom(selector).greatestOf(items, n)
fun <T> leastOf(items : Iterable<T>, n: Int, selector: (T?) -> Comparable<*>?): List<T> =
    orderingFrom(selector).leastOf(items, n)

// Extension functions for iterables
fun <T> Iterable<T>.greatest(n: Int, selector: (T?) -> Comparable<*>?): List<T> =
    greatestOf(this, n, selector)
fun <T> Iterable<T>.least(n: Int, selector: (T?) -> Comparable<*>?): List<T> =
    leastOf(this, n, selector)

fun <T> orderingFrom(selector: (T?) -> Comparable<*>?) =
    object : Ordering<T>() {
        override fun compare(a: T?, b: T?): Int =
            compareValuesBy(a, b, selector)
    }

/***************************
 * Natural Ordering
 ***************************/

// Static functions for passing iterables of comparables
fun <T : Comparable<T>> greatestOf(items: Iterable<T>, n: Int): List<T> =
    ordering<T>().greatestOf(items, n)
fun <T : Comparable<T>> leastOf(items: Iterable<T>, n: Int): List<T> =
    ordering<T>().leastOf(items, n)

// Extension functions for iterables of comparables
fun <T: Comparable<T>> Iterable<T>.greatest(n: Int): List<T> =
    greatestOf(this, n)
fun <T: Comparable<T>> Iterable<T>.least(n: Int): List<T> =
    leastOf(this, n)

fun <T : Comparable<T>> ordering() =
    object : Ordering<T>() {
        override fun compare(a: T?, b: T?): Int = compareValues(a, b)
    }
import com.squareup.moshi.JsonClass
/**
 * Represents a frequency distribution over strings.
 * The counts can be fractional.
 * The smoothing parameter is a double that is added to all counts when queried (so you
 * could avoid zero counts for example).
 *
 * Created by Andrew D. Robertson on 24/12/2020.
 */
@JsonClass(generateAdapter = true)
class Counter(val smoothing: Double = 0.0,
              val counts: MutableMap<String, Double> = mutableMapOf()) {

    val vocabSize: Int
        get() = counts.size

    val vocab: Set<String>
        get() = counts.keys

    var total: Double = counts.values.sum()
        private set

    val totalSmoothed: Double
        get() = total + (vocabSize * smoothing)


    /**
     * Increment the count for a string by an amount (1 if not specified)
     */
    fun inc(feature: String, increment: Double = 1.0) {
        counts.merge(feature, increment) { existing, new -> existing + new } ?.plus(smoothing)
        total += increment
    }


    /**
     * Get a map of the strings to their counts, sorted most frequent first (pre-smoothed counts).
     */
    fun mostFrequent(): Map<String, Double> =
        counts.entries.sortedByDescending{ it.value }.associate{ it.toPair() }

    /**
     * Return a new counter with only those strings which have a count equal to
     * or above the specified threshold (threshold is compared to the pre-smoothed counts).
     */
    fun filtered(threshold: Double) =
        Counter(smoothing, counts.filterValues{ it < threshold }.toMutableMap())

    /**
     * Return a new counter with all the counts of this counter plus another specified counter.
     */
    fun merged(counter : Counter) =
        Counter(smoothing, (counts.keys + counter.counts.keys).associateWith { this[it] + counter[it] }.toMutableMap())

    /**
     * Iterator over all strings and their counts (post-smoothed).
     * Usage in loop:
     *   for ((feature, count) in counter) { ... }
     */
    operator fun iterator() = object : AbstractIterator<Pair<String, Double>>() {
        val wrapped = counts.iterator()
        override fun computeNext() {
            if (wrapped.hasNext()){
                with (wrapped.next()){
                    setNext(Pair(key, value + smoothing))
                }
            } else done()
        }
    }

    /**
     * Get the probability for a string, post-smoothing, being sure to normalise
     * the total count using the smoothing.
     */
    fun probability(feature: String): Double =
        this[feature] / totalSmoothed

    /**
     * Get post-smoothing count for a string.
     */
    operator fun get(key: String): Double =
        (counts[key] ?: 0.0) + smoothing


    /**
     * Functions for building Counter objects from other types.
     */
    companion object Factory {

        /**
         * Build a counter given an iterable of strings and optionally a smoothing setting.
         */
        fun fromIterable(features : Iterable<String>, smoothing: Double = 0.0) =
            Counter(smoothing).apply {
                features.forEach { inc(it) }
            }
    }

}
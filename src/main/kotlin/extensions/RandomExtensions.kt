import cc.mallet.util.Randoms
import kotlin.random.Random

val randoms: Randoms = Randoms()

fun DoubleArray.setRandomSumTo1(smoothing: Double = 0.00001) {
    var sum = 0.0
    for (i in indices){
        this[i] = randoms.nextGamma(1.0,1.0) + smoothing
        sum += this[i]
    }
    this /= sum
}

fun randomSumTo1(size: Int, smoothing: Double = 0.00001): DoubleArray {
    var sum = 0.0
    val arr = DoubleArray(size)
    for (i in arr.indices){
        with(randoms.nextGamma(1.0, 1.0) + smoothing) {
            arr[i] = this
            sum += this
        }
    }
    arr /= sum
    return arr
}

fun <T> List<T>.randomElement(): T =
    this[Random.nextInt(size)]

fun <T> List<T>.randomPair(): Pair<T, T> =
    randomElement().let { it to randomExcluding(it) }

fun <T> List<T>.randomExcluding(excluding: T): T {
    val idx = Random.nextInt(size - 1)
    return if (this[idx] === excluding) last() else this[idx]
}
import cc.mallet.util.Randoms

val randoms: Randoms = Randoms()

fun DoubleArray.randomSumTo1(smoothing: Double = 0.00001): DoubleArray {
    var sum = 0.0
    for (i in indices){
        this[i] = randoms.nextGamma(1.0,1.0) + smoothing
        sum += this[i]
    }
    for (i in indices){
        this[i] /= sum
    }
    return this
}

fun main() {

    val a = DoubleArray(3)
    a.randomSumTo1()

    print(a)
}
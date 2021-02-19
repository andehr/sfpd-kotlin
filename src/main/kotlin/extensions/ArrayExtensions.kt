
operator fun DoubleArray.divAssign(n: Double) {
    for (i in indices){
        this[i] /= n
    }
}

operator fun DoubleArray.divAssign(arr: DoubleArray) {
    ensureSameSize(this, arr)
    for (i in indices){
        this[i] /= arr[i]
    }
}

operator fun DoubleArray.plusAssign(n: Double) {
    for (i in indices){
        this[i] += n
    }
}

operator fun DoubleArray.minusAssign(n: Double) {
    for (i in indices){
        this[i] -= n
    }
}

operator fun DoubleArray.timesAssign(n: Double) {
    for (i in indices){
        this[i] *= n
    }
}

operator fun DoubleArray.timesAssign(arr: DoubleArray) {
    ensureSameSize(this, arr)
    for (i in indices){
        this[i] *= arr[i]
    }
}


fun DoubleArray.uniformSumTo1() =
    fill(1.0 / size)

private fun ensureSameSize(a: DoubleArray, b: DoubleArray) {
    if (a.size != b.size) throw IllegalArgumentException("Array sizes do not match");
}
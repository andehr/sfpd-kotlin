
operator fun DoubleArray.divAssign(arr: DoubleArray) {
    requireSameSize(this, arr)
    for (i in indices){
        this[i] /= arr[i]
    }
}

operator fun DoubleArray.timesAssign(arr: DoubleArray) {
    requireSameSize(this, arr)
    for (i in indices){
        this[i] *= arr[i]
    }
}

operator fun DoubleArray.divAssign(n: Double) =
    mapInPlace { it / n }

operator fun DoubleArray.plusAssign(n: Double) =
    mapInPlace { it + n }

operator fun DoubleArray.minusAssign(n: Double) =
    mapInPlace { it - n }

operator fun DoubleArray.timesAssign(n: Double) =
    mapInPlace { it * n }

fun DoubleArray.mapInPlace(transform: (Double) -> Double) {
    for (i in indices){
        this[i] = transform(this[i])
    }
}

fun DoubleArray.mapIndexedInPlace(transform: (Int, Double) -> Double) {
    for (i in indices){
        this[i] = transform(i, this[i])
    }
}

fun DoubleArray.uniformSumTo1() =
    fill(1.0 / size)

private fun requireSameSize(a: DoubleArray, b: DoubleArray) =
    require(a.size != b.size) {"Array sizes do not match ($a.size != $b.size"}
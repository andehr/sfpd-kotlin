import java.lang.Math.ulp
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max

/**
 * Created by Andrew D. Robertson on 27/12/2020.
 */

/**
 * Equality for doubles, using a threshold based on the unit of least precision.
 * See https://levelup.gitconnected.com/double-equality-in-kotlin-f99392cba0e4
 *
 * Usage: a eq b
 */
infix fun Double.eq(other: Double) =
    abs(this - other) < max(ulp(this), ulp(other)) * 2

/**
 * Greater than or equal to, using the double equality method
 */
infix fun Double.ge(other: Double) =
    this > other || this.eq(other)

fun lnSafe(d: Double): Double =
    if (d == 0.0) 0.0 else ln(d)
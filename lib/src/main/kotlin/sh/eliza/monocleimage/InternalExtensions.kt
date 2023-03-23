package sh.eliza.monocleimage

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.ulp

internal fun Double.approximatelyEquals(other: Double) =
  abs(this - other) < kotlin.math.max(ulp, other.ulp) * 2

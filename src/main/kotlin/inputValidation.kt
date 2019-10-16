package net.ylophones.fotilo

fun checkWithinRange(message: String, valueToCheck: Int, lowerBound: Int, upperBound: Int) {
    if (valueToCheck < lowerBound || valueToCheck > upperBound) {
        val error = "$message - value $valueToCheck must be within $lowerBound to $upperBound"
        throw IllegalArgumentException(error)
    }
}

package jerre.math

internal fun List<String>.sublistOrNull(start: Int, end: Int? = null): List<String>? {
    if (start == end || start >= size) return null
    return subList(start, end ?: size)
}
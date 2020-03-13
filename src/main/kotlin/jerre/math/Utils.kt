package jerre.math

internal fun <T> List<T>.sublistOrNull(start: Int, end: Int? = null): List<T>? {
    if (start == end || start >= size) return null
    return subList(start, end ?: size)
}
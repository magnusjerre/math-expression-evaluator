package jerre.math

import java.lang.Exception

enum class BinaryOperator(
        val str: String,
        private val precedenceOrder: Int    // Lower value -> higher precedence
) {
    MULTIPLY("*", 1),
    DIVIDE("/", 1),
    PLUS("+", 2),
    MINUS("-", 2);

    /**
     * If comparing two binary operators with the same precedence order, the [this]-binary-operator is considered
     * as having a higher precedence than the [other] binary operator.
     */
    fun hasPrecedenceOver(other: BinaryOperator): Boolean = precedenceOrder <= other.precedenceOrder

    companion object {
        fun fromString(str: String): BinaryOperator =
                values().find { it.str == str } ?: throw IllegalArgumentException("Not a valid operator")
    }
}

fun String.toOperator(): BinaryOperator = BinaryOperator.fromString(this)
fun String.hasPrecedenceOver(other: String): Boolean = toOperator().hasPrecedenceOver(other.toOperator())
fun String.isBinaryOperator(): Boolean = try {
    toOperator().let { true }
} catch (e: Exception) {
    false
}

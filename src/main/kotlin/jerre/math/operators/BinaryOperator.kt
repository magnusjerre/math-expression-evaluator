package jerre.math.operators

import java.lang.Exception

enum class BinaryOperator(
        val str: String,
        override val precedenceOrder: Int    // Lower value -> higher precedence
) : OperatorPrecedence {
    POWER("^", 0),
    MULTIPLY("*", 1),
    DIVIDE("/", 1),
    PLUS("+", 2),
    MINUS("-", 2);

    companion object {
        fun fromString(str: String): BinaryOperator =
                values().find { it.str == str } ?: throw IllegalArgumentException("Not a valid binary operator")
    }
}

fun String.toBinaryOperator(): BinaryOperator = BinaryOperator.fromString(this)
fun String.isBinaryOperator(): Boolean = try {
    toBinaryOperator().let { true }
} catch (e: Exception) {
    false
}

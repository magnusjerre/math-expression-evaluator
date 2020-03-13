package jerre.math.operators

import java.lang.Exception

enum class BinaryOperator(
        val str: String,
        val regex: Regex,
        override val precedenceOrder: Int    // Lower value -> higher precedence
) : OperatorPrecedence {
    POWER("^", """^\s*\^\s*""".toRegex(),0),
    MULTIPLY("*", """^\s*\*\s*""".toRegex(), 1),
    DIVIDE("/", """^\s*/\s*""".toRegex(), 1),
    PLUS("+", """^\s*\+\s*""".toRegex(), 2),
    MINUS("-", """^\s*-\s*""".toRegex(), 2);

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

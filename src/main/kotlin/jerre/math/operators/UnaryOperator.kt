package jerre.math.operators

import java.lang.Exception

enum class UnaryOperator(
        val str: String,
        val regex: Regex,
        override val precedenceOrder: Int    // Lower value -> hight precedence
) : OperatorPrecedence {
    ABS("abs", """^\s*abs\s*""".toRegex(), -1),
    SQRT("sqrt", """^\s*sqrt\s*""".toRegex(), -1);

    companion object {
        fun fromString(str: String): UnaryOperator = values().find { it.str == str } ?: throw IllegalArgumentException("Not a valid unary operator")
    }
}

fun String.toUnaryOperator(): UnaryOperator = UnaryOperator.fromString(this)
fun String.isUnaryOperator(): Boolean = try {
    toUnaryOperator().let { true }
} catch (e: Exception) {
    false
}
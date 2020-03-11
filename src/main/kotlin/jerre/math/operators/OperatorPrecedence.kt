package jerre.math.operators

interface OperatorPrecedence {
    val precedenceOrder: Int

    /**
     * If comparing two binary operators with the same precedence order, the this-binary-operator is considered
     * as having a higher precedence than the [other] binary operator.
     */
    fun hasPrecedenceOver(other: OperatorPrecedence): Boolean = precedenceOrder <= other.precedenceOrder
}

fun String.isOperator(): Boolean = isBinaryOperator() || isUnaryOperator()
fun String.toPrecedence(): OperatorPrecedence = when {
    isBinaryOperator() -> toBinaryOperator()
    isUnaryOperator() -> toUnaryOperator()
    else -> throw IllegalArgumentException("Not a precedence string, got: $this")
}
fun String.hasPrecedenceOver(other: String): Boolean = when {
    isOperator() && other.isOperator() -> toPrecedence().hasPrecedenceOver(other.toPrecedence())
    else -> throw IllegalArgumentException("Both must strings must be operators but got: $this, and $other")
}
package jerre.math

interface Computable {
    fun compute(): Double
}

data class ValueNode(
        var number: Double,
        var index: Int = 0
) : Computable {
    override fun compute(): Double = number
}

data class BinaryOperatorNode(
        var left: Computable,
        var right: Computable,
        var operator: BinaryOperator
) : Computable {
    override fun compute(): Double = when (operator) {
        BinaryOperator.PLUS -> left.compute() + right.compute()
        BinaryOperator.MINUS -> left.compute() - right.compute()
        BinaryOperator.DIVIDE -> left.compute() / right.compute()
        BinaryOperator.MULTIPLY -> left.compute() * right.compute()
    }
}
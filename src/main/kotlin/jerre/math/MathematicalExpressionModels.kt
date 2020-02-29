package jerre.math

interface MathematicalExpression {
    /**
     * [valueMap] If null, the original expression-number-value for the given index will be used,
     * otherwise the input will be used
     */
    fun compute(valueMap: Map<Int, Double>? = null): Double
}

data class ValueExpression(
        val number: Double,
        val index: Int = 0
) : MathematicalExpression {
    override fun compute(valueMap: Map<Int, Double>?): Double = valueMap?.get(index) ?: number
}

data class BinaryOperatorExpression(
        val left: MathematicalExpression,
        val right: MathematicalExpression,
        val operator: BinaryOperator
) : MathematicalExpression {
    override fun compute(valueMap: Map<Int, Double>?): Double = when (operator) {
        BinaryOperator.PLUS -> left.compute(valueMap) + right.compute(valueMap)
        BinaryOperator.MINUS -> left.compute(valueMap) - right.compute(valueMap)
        BinaryOperator.DIVIDE -> left.compute(valueMap) / right.compute(valueMap)
        BinaryOperator.MULTIPLY -> left.compute(valueMap) * right.compute(valueMap)
    }
}
package jerre.math

import jerre.math.operators.BinaryOperator
import jerre.math.operators.UnaryOperator
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

interface MathematicalExpression {
    /**
     * [valueMap] If null, the original expression-number-value for the given index will be used,
     * otherwise the input will be used.
     * Will prefer name-mapping over index-mapping
     */
    fun compute(valueMap: Map<Mapping, Double>? = null): Double
}

data class Mapping(val name: String? = null, val index: Int? = null)

data class ValueExpression(
        val number: Double? = null,
        val name: String? = null,
        val index: Int = 0
) : MathematicalExpression {
    private val nameMapping: Mapping = Mapping(name)
    private val indexMapping: Mapping = Mapping(index = index)
    override fun compute(valueMap: Map<Mapping, Double>?): Double =
            valueMap?.get(nameMapping) ?: (
                    valueMap?.get(indexMapping) ?: (
                            number ?: throw IllegalArgumentException("We don't have a value to compute with")
                            )
                    )
}

data class BinaryOperatorExpression(
        val left: MathematicalExpression,
        val right: MathematicalExpression,
        val operator: BinaryOperator
) : MathematicalExpression {
    override fun compute(valueMap: Map<Mapping, Double>?): Double = when (operator) {
        BinaryOperator.POWER -> left.compute(valueMap).pow(right.compute(valueMap))
        BinaryOperator.PLUS -> left.compute(valueMap) + right.compute(valueMap)
        BinaryOperator.MINUS -> left.compute(valueMap) - right.compute(valueMap)
        BinaryOperator.DIVIDE -> left.compute(valueMap) / right.compute(valueMap)
        BinaryOperator.MULTIPLY -> left.compute(valueMap) * right.compute(valueMap)
    }
}


data class UnaryOperatorExpression(
        val operand: MathematicalExpression,
        val operator: UnaryOperator
): MathematicalExpression {
    override fun compute(valueMap: Map<Mapping, Double>?): Double = when (operator){
        UnaryOperator.ABS -> abs(operand.compute(valueMap))
        UnaryOperator.SQRT -> sqrt(operand.compute(valueMap))
    }
}
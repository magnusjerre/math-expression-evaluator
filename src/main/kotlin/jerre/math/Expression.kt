package jerre.math

fun String.toMathematicalExpression(): MathematicalExpression = tokenize().buildMathematicalExpressionTree()

internal fun List<String>.buildMathematicalExpressionTree(): MathematicalExpression {
    val indexValuedTokens = this.replaceValuesWithIndexes()
    val expressionTreeWithIndexAsValues = indexValuedTokens.buildMathematicalExpressionTreeBasedOnValuesOnly()   // Contains only the indexes, not the original values
    return expressionTreeWithIndexAsValues.copyWithOriginalMathematicalExpressionValues(originalValueTokens = this, indexValueTokens = indexValuedTokens)
}

private fun List<String>.buildMathematicalExpressionTreeBasedOnValuesOnly(): MathematicalExpression {
    if (isEmpty()) return ValueExpression()
    if (size == 1) return ValueExpression(number = first().toDouble())

    val firstToken = first()
    val secondToken = this[1]

    when {
        firstToken.isUnaryOperator() -> {
            val unaryOperandResult =  first().toUnaryOperator().buildUnaryOperand(sublistOrNull(1)!!)
            return unaryOperandResult.restOfTokens?.first()?.toBinaryOperator()?.buildBinary(
                    leftHand = unaryOperandResult.operand,
                    restOfTokens = unaryOperandResult.restOfTokens.sublistOrNull(1)!!
            )?.operand ?: unaryOperandResult.operand
        }
        secondToken.isBinaryOperator() -> {
            return secondToken.toBinaryOperator().buildBinary(
                    leftHand = subList(0, 1).buildMathematicalExpressionTreeBasedOnValuesOnly(),
                    restOfTokens = subList(2, size)).operand
        }
        firstToken.isGroupOpenToken() -> {
            val leftGroupOperand = buildGroup()
            return leftGroupOperand.restOfTokens?.first()?.toBinaryOperator()?.buildBinary(
                    leftHand = leftGroupOperand.operand,
                    restOfTokens = leftGroupOperand.restOfTokens.sublistOrNull(1)!!
            )?.operand ?: leftGroupOperand.operand
        }
        else -> {
            throw IllegalArgumentException("Woah, unexpected token! Got $firstToken")
        }
    }
}

private data class OperandResult(
        val operand: MathematicalExpression,
        val restOfTokens: List<String>?
)

private fun String.isValue(): Boolean = isNumber() || isVariable()
private fun String.toValueExpression(): MathematicalExpression = when {
    this.isNumber() -> ValueExpression(number = this.toDouble())
    this.isVariable() -> ValueExpression(name = this)
    else -> throw IllegalArgumentException("Expected a number of variable, but got: $this")
}

private fun UnaryOperator.buildUnaryOperand(restOfTokens: List<String>): OperandResult {
    if (restOfTokens.isEmpty()) throw IllegalArgumentException("Expected at least one token after the following unary operator: $this")
    if (restOfTokens.size == 1) return OperandResult(
            operand = UnaryOperatorExpression(
                    operator = this,
                    operand = restOfTokens.buildValue().operand
            ),
            restOfTokens = null
    )

    val firstToken = restOfTokens.first()
    if (firstToken.isValue()) {
        return OperandResult(
                operand = UnaryOperatorExpression(
                        operator = this,
                        operand = firstToken.toValueExpression()
                ),
                restOfTokens = restOfTokens.sublistOrNull(1)
        )
    }

    if (firstToken.isUnaryOperator()) {
        val subOperatorExpression = firstToken.toUnaryOperator().buildUnaryOperand(restOfTokens.sublistOrNull(1)!!)  // We know there are at least two elements
        return OperandResult(
                operand = UnaryOperatorExpression(
                        operator = this,
                        operand = subOperatorExpression.operand
                ),
                restOfTokens = subOperatorExpression.restOfTokens
        )
    }

    if (firstToken.isGroupOpenToken()) {
        val group = restOfTokens.buildGroup()
        return OperandResult(
                operand = UnaryOperatorExpression(
                        operator = this,
                        operand = group.operand
                ),
                restOfTokens = group.restOfTokens
        )
    }

    throw IllegalArgumentException("Unexpected token after ")
}

// Assumes the first element is "("
private fun List<String>.buildGroup(): OperandResult {
    val groupCloseIndex = this.indexOfMatchingGroupClose()
    return OperandResult(
            operand = sublistOrNull(1, groupCloseIndex)!!.buildMathematicalExpressionTreeBasedOnValuesOnly(),
            restOfTokens = sublistOrNull(groupCloseIndex + 1)
    )
}

private fun List<String>.buildValue(): OperandResult = OperandResult(
        operand = first().toValueExpression(),
        restOfTokens = sublistOrNull(1)
)

private fun BinaryOperator.buildBinary(leftHand: MathematicalExpression, restOfTokens: List<String>): OperandResult {
    if (restOfTokens.isEmpty()) throw IllegalArgumentException("Expected at least one token after the following unary operator: $this")
    if (restOfTokens.size == 1) {
        if (restOfTokens.first().isValue()) {
            return OperandResult(
                    operand = BinaryOperatorExpression(
                            left = leftHand,
                            operator = this,
                            right = restOfTokens.first().toValueExpression()
                    ),
                    restOfTokens = null
            )
        } else {
            throw IllegalArgumentException("Expected a value")
        }
    }

    val firstToken = restOfTokens.first()
    val rightOperandResult: OperandResult = when {
        firstToken.isValue() -> {
            restOfTokens.buildValue()
        }
        firstToken.isGroupOpenToken() -> {
            restOfTokens.buildGroup()
        }
        firstToken.isUnaryOperator() -> {
            firstToken.toUnaryOperator().buildUnaryOperand(restOfTokens.sublistOrNull(1)!!)
        }
        else -> {
            throw IllegalArgumentException("Expected one of number, variable, group or unary operator, but got $firstToken")
        }
    }

    if (rightOperandResult.restOfTokens == null) {
        return OperandResult(
                operand = BinaryOperatorExpression(
                        left = leftHand,
                        operator = this,
                        right = rightOperandResult.operand
                ),
                restOfTokens = null
        )
    }
    if (rightOperandResult.restOfTokens.first().isBinaryOperator()) {
        val nextOperator = rightOperandResult.restOfTokens.first().toBinaryOperator()
        return if (this.hasPrecedenceOver(nextOperator)) {
            val higherPrecedenceOperation = BinaryOperatorExpression(
                    left = leftHand,
                    operator = this,
                    right = rightOperandResult.operand
            )
            nextOperator.buildBinary(higherPrecedenceOperation, rightOperandResult.restOfTokens.sublistOrNull(1)!!)
        } else {
            val lowerPrecedenceOperation = nextOperator.buildBinary(rightOperandResult.operand, rightOperandResult.restOfTokens.sublistOrNull(1)!!)
            OperandResult(
                    operand = BinaryOperatorExpression(
                            left = leftHand,
                            operator = this,
                            right = lowerPrecedenceOperation.operand
                    ),
                    restOfTokens = lowerPrecedenceOperation.restOfTokens
            )
        }
    }

    throw IllegalArgumentException("Eh...")
}

private fun MathematicalExpression.copyWithOriginalMathematicalExpressionValues(
        originalValueTokens: List<String>,
        indexValueTokens: List<String>
): MathematicalExpression {
    when (this) {
        is ValueExpression -> {
            val indexInt = number?.toInt()
            val tokenIndex = indexValueTokens.indexOf("$indexInt")
            val token = originalValueTokens[tokenIndex]
            return ValueExpression(
                    number = token.toDoubleOrNull(),
                    name = if (token.isVariable()) token else null,
                    index = indexInt ?: -1  // Should never be null
            )
        }
        is BinaryOperatorExpression -> {
            return BinaryOperatorExpression(
                    left = left.copyWithOriginalMathematicalExpressionValues(originalValueTokens, indexValueTokens),
                    right = right.copyWithOriginalMathematicalExpressionValues(originalValueTokens, indexValueTokens),
                    operator = operator
            )
        }
        is UnaryOperatorExpression -> {
            return UnaryOperatorExpression(
                    operand = operand.copyWithOriginalMathematicalExpressionValues(originalValueTokens, indexValueTokens),
                    operator = operator
            )
        }
        else -> {
            throw IllegalArgumentException("Hm, this is unexpected")
        }
    }
}


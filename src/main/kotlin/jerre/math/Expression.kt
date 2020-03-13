package jerre.math

import jerre.math.operators.*

fun String.toMathematicalExpression(): MathematicalExpression = extractTokens().buildMathematicalExpressionTree()

private fun List<Token>.buildMathematicalExpressionTree(): MathematicalExpression {
    if (isEmpty()) return ValueExpression()
    if (size == 1) return ValueExpression(number = first().str.toDouble())

    val firstToken = first()

    val partialOperandResult = when(firstToken.type) {
        TokenType.UNARY_OPERATOR -> firstToken.str.toUnaryOperator().buildUnaryOperationPartialResult(sublistOrNull(1)!!)
        TokenType.VALUE, TokenType.VARIABLE -> buildValuePartialResult()
        TokenType.GROUP_OPEN -> buildGroupPartialResult()
        else -> throw IllegalArgumentException("Woah, unexpected token! Got $firstToken")
    }

    return partialOperandResult.restOfTokens?.first()?.str?.toBinaryOperator()?.buildBinaryOperationPartialResult(
            leftHand = partialOperandResult.operand,
            restOfTokens = partialOperandResult.restOfTokens.sublistOrNull(1)!!
    )?.operand ?: partialOperandResult.operand
}

private data class PartialResult(
        val operand: MathematicalExpression,
        val restOfTokens: List<Token>?
) {
    val allTokensConsumed: Boolean = restOfTokens == null || restOfTokens.isEmpty()
}

private fun List<Token>.nextPartialResultFromFirstToken(): PartialResult = when(first().type) {
    TokenType.VARIABLE, TokenType.VALUE -> buildValuePartialResult()
    TokenType.UNARY_OPERATOR -> first().str.toUnaryOperator().buildUnaryOperationPartialResult(sublistOrNull(1)!!)
    TokenType.GROUP_OPEN -> buildGroupPartialResult()
    else -> throw IllegalArgumentException("Unexpected token after ${first()}")
}

private fun UnaryOperator.buildUnaryOperationPartialResult(restOfTokens: List<Token>): PartialResult {
    val partialResult: PartialResult = restOfTokens.nextPartialResultFromFirstToken()

    return PartialResult(
            operand = UnaryOperatorExpression(
                    operator = this,
                    operand = partialResult.operand
            ),
            restOfTokens = partialResult.restOfTokens
    )
}

private fun List<Token>.buildGroupPartialResult(): PartialResult {
    val groupCloseIndex = this.indexOfMatchingGroupClose()
    return PartialResult(
            operand = sublistOrNull(1, groupCloseIndex)!!.buildMathematicalExpressionTree(),
            restOfTokens = sublistOrNull(groupCloseIndex + 1)
    )
}

private fun List<Token>.indexOfMatchingGroupClose(): Int {
    require(isNotEmpty())

    var nUnMatchedGroupOpen = 1
    for (i in 1 until size) {
        if (this[i].type == TokenType.GROUP_CLOSE) {
            nUnMatchedGroupOpen--
            if (nUnMatchedGroupOpen == 0) {
                return i
            }
        } else if (this[i].type == TokenType.GROUP_OPEN) {
            nUnMatchedGroupOpen++
        }
    }
    return -1
}

private fun List<Token>.buildValuePartialResult(): PartialResult = PartialResult(
        operand = first().let {
            when (it.type) {
                TokenType.VALUE -> ValueExpression(number = it.str.toDouble(), name = it.indexForValueOrVariable!!.toString(), index = it.indexForValueOrVariable)
                TokenType.VARIABLE -> ValueExpression(number = null, name = it.str, index = it.indexForValueOrVariable!!)
                else -> throw IllegalArgumentException("Expected a number of variable, but got: $this")
            }
        },
        restOfTokens = sublistOrNull(1)
)

private fun BinaryOperator.buildBinaryOperationPartialResult(leftHand: MathematicalExpression, restOfTokens: List<Token>): PartialResult {
    val rightOperandResult: PartialResult = restOfTokens.nextPartialResultFromFirstToken()

    if (rightOperandResult.allTokensConsumed) {
        return PartialResult(
                operand = BinaryOperatorExpression(
                        left = leftHand,
                        operator = this,
                        right = rightOperandResult.operand
                ),
                restOfTokens = null
        )
    }

    /**
     * Do a look-ahead to determine operator precedence, eg. 2 * 3 + 4 or 2 + 3 * 4
     * We always calculate the higher precedence first, then return the result of the lower precedence operation.
     * Higher precedence: first compute the left hand side (2 * 3), then return the right hand side (left) + 4
     * Lower precedence, first compute the right hand side (3 * 4), then return the left hand side 2 + (right)
     */
    val nextBinaryOperator = rightOperandResult.restOfTokens!!.first().str.toBinaryOperator()
    return if (this.hasPrecedenceOver(nextBinaryOperator)) {
        val higherPrecedenceOperation = BinaryOperatorExpression(
                left = leftHand,
                operator = this,
                right = rightOperandResult.operand
        )
        nextBinaryOperator.buildBinaryOperationPartialResult(higherPrecedenceOperation, rightOperandResult.restOfTokens.sublistOrNull(1)!!)
    } else {
        val lowerPrecedenceOperation = nextBinaryOperator.buildBinaryOperationPartialResult(rightOperandResult.operand, rightOperandResult.restOfTokens.sublistOrNull(1)!!)
        PartialResult(
                operand = BinaryOperatorExpression(
                        left = leftHand,
                        operator = this,
                        right = lowerPrecedenceOperation.operand
                ),
                restOfTokens = lowerPrecedenceOperation.restOfTokens
        )
    }
}

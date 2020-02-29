package jerre.math

fun String.toMathematicalExpression(): MathematicalExpression = tokenize().buildMathematicalExpressionTree()

internal fun List<String>.buildMathematicalExpressionTree(): MathematicalExpression {
    val indexValuedTokens = this.replaceValuesWithIndexes()
    val expressionTreeWithIndexAsValues = indexValuedTokens.buildMathematicalExpressionTreeBasedOnValuesOnly()   // Contains only the indexes, not the original values
    return expressionTreeWithIndexAsValues.copyWithOriginalMathematicalExpressionValues(originalValueTokens =  this, indexValueTokens = indexValuedTokens)
}

private fun List<String>.buildMathematicalExpressionTreeBasedOnValuesOnly(): MathematicalExpression {
    if (isEmpty()) return ValueExpression(number = 0.0)
    if (size == 1) return ValueExpression(number = first().toDouble())
    if (size == 2) throw IllegalArgumentException("eh what? $this")

    val current = first()
    var currentOperator = this[1]
    val leftHand: MathematicalExpression?
    val rightHand: MathematicalExpression?
    var rightHandList = subList(2, size)    // Start at 2 so we avoid including the oprator
    if (current.isGroupOpenToken()) {
        val closeGroupIndex = this.subList(0, size).indexOfMatchingGroupClose()
        if (closeGroupIndex == size - 1) {
            return subList(1, closeGroupIndex).buildMathematicalExpressionTreeBasedOnValuesOnly()
        } else {
            leftHand = subList(1, closeGroupIndex).buildMathematicalExpressionTreeBasedOnValuesOnly()
            currentOperator = this[closeGroupIndex + 1]
            rightHandList = subList(closeGroupIndex + 2, size)
        }
    } else if (current.isNumber()) {
        leftHand = ValueExpression(number = current.toDouble())
    } else {
        throw IllegalArgumentException("hmmm")
    }

    // Do a look-ahead to check for any precedence overrides
    val nextOperator = rightHandList.nextOperator()    // Tror det strengt tatt kun er mulig med en operator
    if (nextOperator == null || nextOperator.hasPrecedenceOver(currentOperator)) {
        rightHand = rightHandList.buildMathematicalExpressionTreeBasedOnValuesOnly()
        return BinaryOperatorExpression(
                left = leftHand,
                right = rightHand,
                operator = currentOperator.toOperator()
        )
    } else {
        return BinaryOperatorExpression(
                left = BinaryOperatorExpression(
                        left = leftHand,
                        right = ValueExpression(number = rightHandList.first().toDouble()),
                        operator = currentOperator.toOperator()
                ),
                right = rightHandList.subList(2, rightHandList.size).buildMathematicalExpressionTreeBasedOnValuesOnly(),
                operator = nextOperator.toOperator()
        )
    }
}

private fun List<String>.nextOperator(): String? {
    if (isEmpty()) return null

    // Not sure if this will ever happen
    if (size == 1) {
        return when {
            first().isBinaryOperator() -> first()
            else -> null
        }
    }

    // Not sure if this will ever happen
    if (first().isGroupOpenToken()) return null

    // Happens when rightHandList follows a groupClose token, (1 + 2) * 3 -> rightHandlList = ["*", "3"]
    if (first().isBinaryOperator()) return first()

    // Happens in the normal case, 1 + 2 + 3 -> rightHandList = ["2", "+", "3"]
    if (this[1].isBinaryOperator()) return this[1]
    return null
}

private fun MathematicalExpression.copyWithOriginalMathematicalExpressionValues(
        originalValueTokens: List<String>,
        indexValueTokens: List<String>
): MathematicalExpression {
    when (this) {
        is ValueExpression -> {
            val indexInt = number.toInt()
            val tokenIndex = indexValueTokens.indexOf("$indexInt")
            return ValueExpression(
                    number = originalValueTokens[tokenIndex].toDouble(),
                    index = indexInt
            )
        }
        is BinaryOperatorExpression -> {
            return BinaryOperatorExpression(
                    left = left.copyWithOriginalMathematicalExpressionValues(originalValueTokens, indexValueTokens),
                    right = right.copyWithOriginalMathematicalExpressionValues(originalValueTokens, indexValueTokens),
                    operator = operator
            )
        }
        else -> {
            throw IllegalArgumentException("Hm, this is unexpected")
        }
    }
}


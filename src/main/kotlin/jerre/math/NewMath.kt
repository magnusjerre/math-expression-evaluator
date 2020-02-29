package jerre.math

fun Computable.recompute(values: Map<Int, Double>): Double {
    values.forEach {
        val traverseResult = traverseAndReplaceValue(it)
        if (!traverseResult) {
            throw IllegalArgumentException("Couldn't find index ${it.key}")
        }
    }
    return compute()
}

// Boolean indicates that the value has been replaced / handled
private fun Computable.traverseAndReplaceValue(value: Map.Entry<Int, Double>): Boolean =
    when (this) {
        is ValueNode -> {
            if (index == value.key) {
                number = value.value
                true
            } else {
                false
            }
        }
        is BinaryOperatorNode -> {
            if (left.traverseAndReplaceValue(value)) true
            else right.traverseAndReplaceValue(value)
        }
        else -> {
            true
        }
    }

fun List<String>.buildComputableTree(): Computable {
    val indexValuedTokens = this.replaceValuesWithIndexes()
    val computableTreeWithIndexAsValues = indexValuedTokens.buildComputableTreeBasedOnValuesOnly()   // Contains only the indexes, not the original values
    computableTreeWithIndexAsValues.setCorrectValueNodeInformation(originalValueTokens = this, indexValueTokens = indexValuedTokens)
    return computableTreeWithIndexAsValues
}

/**
 * Populate the [Computable] with the original values and generated indexes
 * [originalValueTokens] The tokens based on the original input-string, eg, "1 + (3 + 5) * 4"
 * [indexValueTokens] The indexed version of the tokenized input string, eg. "0 + (1 + 2) * 3"
 */
fun Computable.setCorrectValueNodeInformation(originalValueTokens: List<String>, indexValueTokens: List<String>) {
    when (this) {
        is ValueNode -> {
            val indexInt = number.toInt()
            val tokenIndex = indexValueTokens.indexOf("$indexInt")
            number = originalValueTokens[tokenIndex].toDouble()
            index = indexInt
        }
        is BinaryOperatorNode -> {
            left.setCorrectValueNodeInformation(originalValueTokens, indexValueTokens)
            right.setCorrectValueNodeInformation(originalValueTokens, indexValueTokens)
        }
        else -> {
            println("huh?")
        }
    }
}

internal fun List<String>.buildComputableTreeBasedOnValuesOnly(): Computable {
    if (isEmpty()) return ValueNode(number = 0.0)
    if (size == 1) return ValueNode(number = first().toDouble())
    if (size == 2) throw IllegalArgumentException("eh what? $this")

    val current = first()
    var currentOperator = this[1]
    val leftHand: Computable?
    val rightHand: Computable?
    var rightHandList = subList(2, size)    // Start at 2 so we avoid including the oprator
    if (current.isGroupOpenToken()) {
        val closeGroupIndex = this.subList(0, size).indexOfMatchingGroupClose()
        if (closeGroupIndex == size - 1) {
            return subList(1, closeGroupIndex).buildComputableTreeBasedOnValuesOnly()
        } else {
            leftHand = subList(1, closeGroupIndex).buildComputableTreeBasedOnValuesOnly()
            currentOperator = this[closeGroupIndex + 1]
            rightHandList = subList(closeGroupIndex + 2, size)
        }
    } else if (current.isNumber()) {
        leftHand = ValueNode(number = current.toDouble())
    } else {
        throw IllegalArgumentException("hmmm")
    }

    // Do a look-ahead to check for any precedence overrides
    val nextOperator = rightHandList.nextOperator()    // Tror det strengt tatt kun er mulig med en operator
    if (nextOperator == null || nextOperator.hasPrecedenceOver(currentOperator)) {
        rightHand = rightHandList.buildComputableTreeBasedOnValuesOnly()
        return BinaryOperatorNode(
                left = leftHand,
                right = rightHand,
                operator = currentOperator.toOperator()
        )
    } else {
        return BinaryOperatorNode(
                left = BinaryOperatorNode(
                        left = leftHand,
                        right = ValueNode(number = rightHandList.first().toDouble()),
                        operator = currentOperator.toOperator()
                ),
                right = rightHandList.subList(2, rightHandList.size).buildComputableTreeBasedOnValuesOnly(),
                operator = nextOperator.toOperator()
        )
    }
}

private fun List<String>.nextOperator(): String? {
    if (isEmpty()) return null
    if (size == 1) {
        return when {
            first().isBinaryOperator() -> first()
            else -> null
        }
    }
    if (first().isGroupOpenToken()) return null
    if (first().isBinaryOperator()) return first()
    if (this[1].isBinaryOperator()) return this[1]
    return null
}


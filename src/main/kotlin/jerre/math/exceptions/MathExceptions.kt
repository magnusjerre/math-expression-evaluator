package jerre.math.exceptions

import jerre.math.LegalToken
import jerre.math.Token

sealed class MathException(message: String?): RuntimeException(message)

class UnexpectedTokenException(
        val currentToken: Token?,
        val indexOfNextToken: Int,
        val entireString: String
) : MathException(
        "Unexpected token at $indexOfNextToken. Expected on of ${currentToken?.type?.legalTokens()?.asString()}, but got ${entireString.substring(indexOfNextToken)}"
)

class ExpressionBuilderException(message: String?): MathException(message)

private fun List<LegalToken>.asString(): String = joinToString(prefix = "[", postfix = "]") { it.prettyToken }
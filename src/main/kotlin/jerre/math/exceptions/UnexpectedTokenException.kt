package jerre.math.exceptions

import jerre.math.LegalToken
import jerre.math.Token

class UnexpectedTokenException(
        val currentToken: Token?,
        val indexOfNextToken: Int,
        val entireString: String
) : RuntimeException(
        "Unexpected token at $indexOfNextToken. Expected on of ${currentToken?.type?.legalTokens()?.asString()}, but got ${entireString.substring(indexOfNextToken)}"
)

private fun List<LegalToken>.asString(): String = joinToString(prefix = "[", postfix = "]") { it.prettyToken }
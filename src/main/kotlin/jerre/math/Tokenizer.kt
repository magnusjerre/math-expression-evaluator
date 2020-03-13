package jerre.math

import jerre.math.exceptions.UnexpectedTokenException

@Throws(UnexpectedTokenException::class)
internal fun String.extractTokens(): List<Token> {
    var nUnmatchedGroupOpeningOperators = 0

    var currentTokenResult: TokenizationResult = extractNextToken(TokenType.LEGAL_INITIAL_TOKENS)
            ?: throw UnexpectedTokenException(currentToken = null, indexOfNextToken = 0, entireString = this)
    var valueVariableIndex = 0
    var currentToken = currentTokenResult.toToken(if (currentTokenResult.isValueOrVariable()) valueVariableIndex++ else null)

    if (currentTokenResult.tokenType == TokenType.GROUP_OPEN) {
        nUnmatchedGroupOpeningOperators++
    }

    val output = mutableListOf(currentToken)
    var nextTokenIndexStart = currentTokenResult.tokenMatch.length

    while (nextTokenIndexStart < length) {
        val nextToken = substring(nextTokenIndexStart).extractNextToken(currentTokenResult.tokenType.legalTokens())
                ?: throw UnexpectedTokenException(currentToken = currentToken, indexOfNextToken = nextTokenIndexStart, entireString = this)

        if (nextToken.tokenType == TokenType.GROUP_OPEN) {
            nUnmatchedGroupOpeningOperators++
        }

        if (nextToken.tokenType == TokenType.GROUP_CLOSE) {
            nUnmatchedGroupOpeningOperators--
            if (nUnmatchedGroupOpeningOperators < 0) {
                throw UnexpectedTokenException(currentToken = currentToken, indexOfNextToken = nextTokenIndexStart, entireString = this)
            }
        }
        if (nextToken.tokenType == TokenType.TERMINATION) {
            nextTokenIndexStart += 1
        } else {
            currentToken = nextToken.toToken(if (nextToken.isValueOrVariable()) valueVariableIndex++ else null)
            output.add(currentToken)
            nextTokenIndexStart += nextToken.tokenMatch.length
            currentTokenResult = nextToken
        }
    }

    return output
}

private fun String.extractNextToken(legalTokens: List<LegalToken>): TokenizationResult? {
    for (legalToken in legalTokens) {
        val result: MatchResult? = legalToken.regex.find(this)
        if (result != null) return TokenizationResult(result.value, legalToken.type)
    }
    return null
}

private data class TokenizationResult(
        val tokenMatch: String,
        val tokenType: TokenType
) {
    val result: String = tokenMatch.trim()
    fun toToken(valueVariableIndex: Int?): Token = Token(result,  valueVariableIndex, tokenType)
    fun isValueOrVariable(): Boolean = tokenType == TokenType.VARIABLE || tokenType == TokenType.VALUE
}

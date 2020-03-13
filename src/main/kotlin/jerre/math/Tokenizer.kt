package jerre.math

import jerre.math.exceptions.UnexpectedTokenException

@Throws(UnexpectedTokenException::class)
internal fun String.extractTokens(): List<Token> {
    var nUnmatchedGroupOpeningOperators = 0

    var currentToken: TokenizationResult = extractNextToken(TokenType.LEGAL_INITIAL_TOKENS)
            ?: throw UnexpectedTokenException(currentToken = null, indexOfNextToken = 0, entireString = this)

    if (currentToken.tokenType == TokenType.GROUP_OPEN) {
        nUnmatchedGroupOpeningOperators++
    }
    val output = mutableListOf(currentToken.toToken())
    var nextTokenIndexStart = currentToken.tokenMatch.length

    while (nextTokenIndexStart < length) {
        val nextToken = substring(nextTokenIndexStart).extractNextToken(currentToken.tokenType.legalTokens())
                ?: throw UnexpectedTokenException(currentToken = currentToken.toToken(), indexOfNextToken = nextTokenIndexStart, entireString = this)

        if (nextToken.tokenType == TokenType.GROUP_OPEN) {
            nUnmatchedGroupOpeningOperators++
        }

        if (nextToken.tokenType == TokenType.GROUP_CLOSE) {
            nUnmatchedGroupOpeningOperators--
            if (nUnmatchedGroupOpeningOperators < 0) {
                throw UnexpectedTokenException(currentToken = currentToken.toToken(), indexOfNextToken = nextTokenIndexStart, entireString = this)
            }
        }
        if (nextToken.tokenType == TokenType.TERMINATION) {
            nextTokenIndexStart += 1
        } else {
            output.add(nextToken.toToken())
            nextTokenIndexStart += nextToken.tokenMatch.length
            currentToken = nextToken
        }
    }

    return output
}

internal fun List<Token>.replaceTokenValuesWithIndexes(): List<Token> {
    var valueIndex = 0
    return map {
        if (it.type == TokenType.VALUE || it.type == TokenType.VARIABLE) {
            it.copy(str = "${valueIndex++}")
        } else {
            it
        }
    }
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
    fun toToken(): Token = Token(result, tokenType)
}

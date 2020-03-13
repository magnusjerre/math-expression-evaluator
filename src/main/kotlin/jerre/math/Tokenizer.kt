package jerre.math

import jerre.math.exceptions.UnexpectedTokenException
import jerre.math.operators.BinaryOperator
import jerre.math.operators.UnaryOperator

internal val numberRegex = """^\s*(-?\d+(?:\.\d+)?)""".toRegex()
internal val variableRegex = """^\s*(\w[\w\d]*)""".toRegex()
private val terminationToken = LegalToken("", "^$".toRegex(),  TokenType.TERMINATION)
private val groupOpenToken = LegalToken("(", """^\s*\(\s*""".toRegex(), TokenType.GROUP_OPEN)
private val groupCloseToken = LegalToken(")", """^\s*\)\s*""".toRegex(), TokenType.GROUP_CLOSE)
private val binaryOperatorTokens = BinaryOperator.values().map { LegalToken(it.str, it.regex, TokenType.BINARY_OPERATOR) }
private val unaryOperatorTokens = UnaryOperator.values().map { LegalToken(it.str, it.regex, TokenType.UNARY_OPERATOR) }
private val numberToken = LegalToken("number", numberRegex, TokenType.VALUE)
private val variableToken = LegalToken("variable", variableRegex, TokenType.VARIABLE)

private val legalInitialTokens = unaryOperatorTokens + listOf(groupOpenToken, numberToken, variableToken)
private val legalTokensFollowingNumber = binaryOperatorTokens + listOf(groupCloseToken, terminationToken)
private val legalTokensFollowingVariable = legalTokensFollowingNumber
private val legalTokensFollowingUnaryOperator = unaryOperatorTokens + listOf(groupOpenToken, numberToken, variableToken)
private val legalTokensFollowingBinaryOperator = unaryOperatorTokens + listOf(groupOpenToken, numberToken, variableToken)
private val legalTokensFollowingGroupOpenToken = unaryOperatorTokens + listOf(groupOpenToken, numberToken, variableToken) + binaryOperatorTokens
private val legalTokensFollowingGroupCloseToken = binaryOperatorTokens + listOf(groupCloseToken, terminationToken)

internal fun String.extractTokens(): List<Token> {
    var nUnmatchedGroupOpeningOperators = 0

    var currentToken: TokenizationResult = extractNextToken(legalInitialTokens)
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
                throw IllegalArgumentException("Illegal group closing token at index $nextTokenIndexStart")
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

data class Token(
        val str: String,
        val type: TokenType
)

private data class TokenizationResult(
        val tokenMatch: String,
        val tokenType: TokenType
) {
    val result: String = tokenMatch.trim()
    fun toToken(): Token = Token(result, tokenType)
}

data class LegalToken(
        val prettyToken: String,
        val regex: Regex,
        val type: TokenType
)

enum class TokenType {
    VALUE, VARIABLE, GROUP_OPEN, GROUP_CLOSE, BINARY_OPERATOR, UNARY_OPERATOR, TERMINATION;
    fun legalTokens(): List<LegalToken> = when (this) {
        VALUE -> legalTokensFollowingNumber
        VARIABLE -> legalTokensFollowingVariable
        GROUP_OPEN -> legalTokensFollowingGroupOpenToken
        GROUP_CLOSE -> legalTokensFollowingGroupCloseToken
        BINARY_OPERATOR -> legalTokensFollowingBinaryOperator
        UNARY_OPERATOR -> legalTokensFollowingUnaryOperator
        TERMINATION -> emptyList()
    }
}

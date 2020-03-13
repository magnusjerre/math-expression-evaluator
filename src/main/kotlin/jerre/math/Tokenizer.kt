package jerre.math

import jerre.math.operators.BinaryOperator
import jerre.math.operators.UnaryOperator

internal val numberRegex = """^\s*(-?\d+(?:\.\d+)?)""".toRegex()
internal val variableRegex = """^\s*(\w[\w\d]*)""".toRegex()
internal val namedOperatorsRegex = """^\s*(abs|sqrt)""".toRegex(RegexOption.IGNORE_CASE)
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

    var currentToken: TokenizationResult = extractNextToken(legalInitialTokens)!!
    if (currentToken.tokenType == TokenType.GROUP_OPEN) {
        nUnmatchedGroupOpeningOperators++
    }
    val output = mutableListOf(currentToken.toToken())
    var nextTokenIndexStart = currentToken.tokenMatch.length

    while (nextTokenIndexStart < length) {
        val nextToken = substring(nextTokenIndexStart).extractNextToken(currentToken.tokenType.legaltokens())
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

internal class UnexpectedTokenException(
        val currentToken: Token?,
        val indexOfNextToken: Int,
        val entireString: String
) : RuntimeException(
        "Unexpected token at $indexOfNextToken. Expected on of ${currentToken?.type?.legaltokens()?.asString()}, but got ${entireString.substring(indexOfNextToken)}"
)

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

internal fun List<Token>.asListOfStrTokens(): List<String> = map { it.str }

private fun String.extractNextToken(legalTokens: List<LegalToken>): TokenizationResult? {
    for (legalToken in legalTokens) {
        val result: MatchResult? = legalToken.regex.find(this)
        if (result != null) return TokenizationResult(result.value, legalToken.type)
    }
    return null
}

internal data class Token(
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

internal data class LegalToken(
        val basePattern: String,
        val regex: Regex,
        val type: TokenType
)

internal enum class TokenType {
    VALUE, VARIABLE, GROUP_OPEN, GROUP_CLOSE, BINARY_OPERATOR, UNARY_OPERATOR, TERMINATION;
    internal fun legaltokens(): List<LegalToken> = when (this) {
        VALUE -> legalTokensFollowingNumber
        VARIABLE -> legalTokensFollowingVariable
        GROUP_OPEN -> legalTokensFollowingGroupOpenToken
        GROUP_CLOSE -> legalTokensFollowingGroupCloseToken
        BINARY_OPERATOR -> legalTokensFollowingBinaryOperator
        UNARY_OPERATOR -> legalTokensFollowingUnaryOperator
        TERMINATION -> emptyList()
    }
}

private fun List<LegalToken>.asString(): String = joinToString(prefix = "[", postfix = "]") { it.basePattern }

internal fun String.isGroupOpenToken(): Boolean = "(" == this
internal fun String.isGroupCloseToken(): Boolean = ")" == this
internal fun String.isNumber(): Boolean = numberRegex.matches(this)
internal fun String.isVariable(): Boolean = !namedOperatorsRegex.matches(this) && variableRegex.matches(this)
internal fun String.isValue(): Boolean = isNumber() || isVariable()
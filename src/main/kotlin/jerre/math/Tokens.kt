package jerre.math

import jerre.math.operators.BinaryOperator
import jerre.math.operators.UnaryOperator

private val terminationToken = LegalToken("", "^$".toRegex(),  TokenType.TERMINATION)
private val groupOpenToken = LegalToken("(", """^\s*\(\s*""".toRegex(), TokenType.GROUP_OPEN)
private val groupCloseToken = LegalToken(")", """^\s*\)\s*""".toRegex(), TokenType.GROUP_CLOSE)
private val binaryOperatorTokens = BinaryOperator.values().map { LegalToken(it.str, it.regex, TokenType.BINARY_OPERATOR) }
private val unaryOperatorTokens = UnaryOperator.values().map { LegalToken(it.str, it.regex, TokenType.UNARY_OPERATOR) }
private val numberToken = LegalToken("number", """^\s*(-?\d+(?:\.\d+)?)""".toRegex(), TokenType.VALUE)
private val variableToken = LegalToken("variable", """^\s*(\w[\w\d]*)""".toRegex(), TokenType.VARIABLE)

data class Token(
        val str: String,
        /**
         * Indicates the index of the value or variable in the expression for substitution by index.
         * Is {null} for non-value and non-variable tokens.
         * Example: x + 10 - y ^ 2
         * x-index: 0
         * 10-index: 1
         * y-index: 2
         * 2-index: 3
         */
        val indexForValueOrVariable: Int? = null,
        val type: TokenType
)

data class LegalToken(
        val prettyToken: String,
        val regex: Regex,
        val type: TokenType
)

enum class TokenType {
    VALUE, VARIABLE, GROUP_OPEN, GROUP_CLOSE, BINARY_OPERATOR, UNARY_OPERATOR, TERMINATION;
    fun legalTokens(): List<LegalToken> = when (this) {
        VALUE -> binaryOperatorTokens + listOf(groupCloseToken, terminationToken)
        VARIABLE -> binaryOperatorTokens + listOf(groupCloseToken, terminationToken)
        GROUP_OPEN -> unaryOperatorTokens + listOf(groupOpenToken, numberToken, variableToken) + binaryOperatorTokens
        GROUP_CLOSE -> binaryOperatorTokens + listOf(groupCloseToken, terminationToken)
        BINARY_OPERATOR -> unaryOperatorTokens + listOf(groupOpenToken, numberToken, variableToken)
        UNARY_OPERATOR -> unaryOperatorTokens + listOf(groupOpenToken, numberToken, variableToken)
        TERMINATION -> emptyList()
    }

    companion object {
        val LEGAL_INITIAL_TOKENS = unaryOperatorTokens + listOf(groupOpenToken, numberToken, variableToken)
    }
}
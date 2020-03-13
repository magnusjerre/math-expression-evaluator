package jerre.math

import jerre.math.exceptions.UnexpectedTokenException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExpressionErrorsTest {

    @Test
    fun `Should throw an error since we have too many + operators`() {
        try {
            "(3 + 2)++2".extractTokens()
        } catch (e: UnexpectedTokenException) {
            assertEquals(TokenType.BINARY_OPERATOR, e.currentToken!!.type)
            assertEquals("+", e.currentToken!!.str)
            assertEquals(8, e.indexOfNextToken)
        }
    }

}
package jerre.math

import jerre.math.exceptions.UnexpectedTokenException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ExpressionErrorsTest {

    @Test
    fun `Should throw an error since we have too many + operators`() {
        try {
            "(3 + 2)++2".toMathematicalExpression()
        } catch (e: UnexpectedTokenException) {
            assertEquals(TokenType.BINARY_OPERATOR, e.currentToken!!.type)
            assertEquals("+", e.currentToken!!.str)
            assertEquals(8, e.indexOfNextToken)
        }
    }

    @Test
    fun `Should throw an error since we start with an illegal token`() {
        try {
            "+2 - 2".toMathematicalExpression()
        } catch (e: UnexpectedTokenException) {
            assertNull(e.currentToken)
            assertEquals(0, e.indexOfNextToken)
        }
    }

    @Test
    fun `Should throw an error due to unmatched group close token`() {
        try {
            "(3 + 2)) * 2".toMathematicalExpression()
        } catch (e: UnexpectedTokenException) {
            assertEquals(TokenType.GROUP_CLOSE, e.currentToken!!.type)
            assertEquals(")", e.currentToken!!.str)
            assertEquals(7, e.indexOfNextToken)
        }
    }

}
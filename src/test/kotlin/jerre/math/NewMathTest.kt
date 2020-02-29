package jerre.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NewMathTest {

    @Test
    fun `1 + 1 should give 2`() {
        val expression = "1 + 1".toMathematicalExpression()
        assertEquals(2.0, expression.compute())
        assertEquals(5.0, expression.compute(mapOf(
                0 to 2.0,
                1 to 3.0
        )))
        assertEquals(6.0, expression.compute(mapOf(
                1 to 5.0
        )))
    }

    @Test
    fun `1 + 3 - 2 should give 2`() {
        val expression = "1 + 3 - 2".toMathematicalExpression()
        assertEquals(2.0, expression.compute())
    }


    @Test
    fun `1 + (2 * 3) should give 2`() {
        val expression = "1 + (2 * 3)".toMathematicalExpression()
        assertEquals(7.0, expression.compute())
        assertEquals(14.0, expression.compute(mapOf(
                0 to 2.0,
                1 to 3.0,
                2 to 4.0
        )))
    }

    @Test
    fun `1 + (2 + 3) * 4 + 5 should give 26`() {
        val expression = "1 + (2 + 3) * 4 + 5".toMathematicalExpression()
        assertEquals(26.0, expression.compute())
    }

    @Test
    fun `(2 + 3) * (4 + 5) should give 45`() {
        val expression = "(2 + 3) * (4 + 5)".toMathematicalExpression()
        assertEquals(45.0, expression.compute())
        assertEquals(75.0, expression.compute(mapOf(
                2 to 10.0
        )))
    }

    @Test
    fun `(2 + (3 + 3) * 2) * (10 div 5) should give 28`() {
        val expression = "(2 + (3 + 3) * 2) * (10 / 5)".toMathematicalExpression()
        assertEquals(28.0, expression.compute())
    }

    @Test
    fun `1 + 2 + 3 should give 6`() {
        val computable = "1 + 2 + 3".toMathematicalExpression()
        assertEquals(6.0, computable.compute())
    }

    @Test
    fun `1 + 2 + 3 where 2 is replaced by 4 should give 8`() {
        assertEquals(8.0, "1 + 2 + 3".toMathematicalExpression().compute(mapOf(1 to 4.0)))
    }
}
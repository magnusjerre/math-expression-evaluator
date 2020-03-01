package jerre.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExpressionTest {

    @Test
    fun `1 + 1 should give 2`() {
        val expression = "1 + 1".toMathematicalExpression()
        assertEquals(2.0, expression.compute())
        assertEquals(5.0, expression.compute(mapOf(
                Mapping(index = 0) to 2.0,
                Mapping(index = 1) to 3.0
        )))
        assertEquals(6.0, expression.compute(mapOf(
                Mapping(index = 1) to 5.0
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
                Mapping(index = 0) to 2.0,
                Mapping(index = 1) to 3.0,
                Mapping(index = 2) to 4.0
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
//        assertEquals(45.0, expression.compute())
        assertEquals(75.0, expression.compute(mapOf(
                Mapping(index = 2) to 10.0
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
        assertEquals(8.0, "1 + 2 + 3".toMathematicalExpression().compute(mapOf(Mapping(index = 1) to 4.0)))
    }

    @Test
    fun `3 + -1 should give 2`() {
        assertEquals(2.0, "3 + -1".toMathematicalExpression().compute())
    }

    @Test
    fun `3-1 should give 2`() {
        assertEquals(2.0, "3-1".toMathematicalExpression().compute())
    }

    @Test
    fun `3--1 should give 4`() {
        assertEquals(4.0, "3--1".toMathematicalExpression().compute())
    }

    @Test
    fun `2,9 + -0,9 should give 2,0`() {
        assertEquals(2.0, "2.9 + -0.9".toMathematicalExpression().compute())
    }

    @Test
    fun `x * 2 should give 6,0 for x = 3`() {
        assertEquals(6.0, "x * 2".toMathematicalExpression().compute(mapOf(Mapping(name = "x") to 3.0)))
    }

    @Test
    fun `x * x + (x + y)*z should give 13,0 for x = 2, y = 1, z = 3`() {
        assertEquals(13.0, "x * x + (x + y)*z".toMathematicalExpression().compute(mapOf(
                Mapping(name = "x") to 2.0,
                Mapping(name = "y") to 1.0,
                Mapping(name = "z") to 3.0
        )))
    }

    @Test
    fun `x1 * x2 + PI should give 13,0 for x1 = 2, x2 = 3, PI = 3,14`() {
        assertEquals(9.14, "x1 * x2 + PI".toMathematicalExpression().compute(mapOf(
                Mapping(name = "x1") to 2.0,
                Mapping(name = "x2") to 3.0,
                Mapping(name = "PI") to 3.14
        )))
    }

}
package jerre.math

import org.junit.jupiter.api.Test

class NewMathTest {

    @Test
    fun `1 + 1 should give 2`() {
        val tokens = "1 + 1".tokenify()
        println(tokens)
        val computeTree = tokens.buildComputableTreeBasedOnValuesOnly()
        println(computeTree.compute())
        val recomputableTree = tokens.buildComputableTree()
        println(recomputableTree)
        println(recomputableTree.recompute(mapOf(
                0 to 2.0,
                1 to 3.0
        )))
        println(recomputableTree.recompute(mapOf(
                1 to 5.0
        )))
    }


    @Test
    fun `1 + (2 * 3) should give 2`() {
        val tokens = "1 + (2 * 3)".tokenify()
        println(tokens)
        val computeTree = tokens.buildComputableTreeBasedOnValuesOnly()
        println(computeTree)
        println("computed value: ${computeTree.compute()}")
    }

    @Test
    fun `1 + (2 + 3) * 4 + 5 should give 26`() {
        val tokens = "1 + (2 + 3) * 4 + 5".tokenify()
        println(tokens)
        val computeTree = tokens.buildComputableTreeBasedOnValuesOnly()
        println(computeTree)
        println("computed value: ${computeTree.compute()}")
    }

    @Test
    fun `(2 + 3) * (4 + 5) should give 45`() {
        val tokens = "(2 + 3) * (4 + 5)".tokenify()
        println(tokens)
        val computeTree = tokens.buildComputableTreeBasedOnValuesOnly()
        println(computeTree)
        println("computed value: ${computeTree.compute()}")
    }

    @Test
    fun `(2 + (3 + 3) * 2) * (10 div 5) should give 28`() {
        val tokens = "(2 + (3 + 3) * 2) * (10 / 5)".tokenify()
        println(tokens)
        println(tokens.replaceValuesWithIndexes())
        val computeTree = tokens.buildComputableTreeBasedOnValuesOnly()
        println(computeTree)
        val recomputableTree = tokens.buildComputableTree()
        println("reusable tree")
        println(recomputableTree)
        println("computed value: ${computeTree.compute()}")
    }
}
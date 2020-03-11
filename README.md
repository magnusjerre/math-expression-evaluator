# Math

This repo contains functions that take a human readable math-string, e.g. "1 + (2 + 3) * 4" and evaluates the expression. The expression can be stored as an expression object and then reused with different variables by supplying a value-mapping-map.

# Samples
```kotlin
// Simple computation
"1 + (2 + 3) * 4 + 5".toMathematicalExpression().compute()  // 26.0

// We can store the expression in a variable if we wan't to reuse it with different numbers
// Note that applying multiple successive "computes" won't alter the base-values used.
val expression = "(2 + 3) * (4 + 5)".toMathematicalExpression()
expression.compute()                                    // (2 + 3) * (4 + 5) = 45.0
expression.compute(mapOf(Mapping(index = 2) to 10.0))   // (2 + 3) * (10.0 + 5) = 75.0
expression.compute(mapOf(Mapping(index = 0) to 1.0))    // (1.0 + 3) * (4 + 5) = 36.0

// Support for negative numbers
"3 - -1".toMathematicalExpression().compute()   // 4.0

// Substitute the variables for values
"x * x + (x + y)*z".toMathematicalExpression().compute(mapOf(
                Mapping(name = "x") to 2.0,
                Mapping(name = "y") to 1.0,
                Mapping(name = "z") to 3.0
))  // 13.0

// Combine unary operators with binary ones
"sqrt(abs(-4)) + 2".toMathematicalExpression().compute()    // 4.0

// We can also omit the paranthesis if we want
"sqrt abs -4 + 2".toMathematicalExpression().compute()  // 4.0
```

# Operands
These are the values that some operation (eg. +, -, abs) is performed on. An operand can be a number, a variable or a group. A lower number means a higher precedence.

|Example operands|Type|
|-----------|-----|
|1|Positive number|
|-1|Negative number|
|x|Variable|
|longName2|Variable|
|(1 + x)|Group|

# Operators
Operators are split into binary and unary operators. The binary operators (eg. +, -) take a left hand and right hand operand. The unary operators (eg. abs, sqrt) take only a single operand. All unary operators have precedence over binary operators.

## Unary operators
|Operator|Precedence|
|--------|-----------|
|abs|10|
|sqrt|10|

## Binary operators
|Operator|Precendence|
|--------|-----------|
|+|30|
|-|30|
|*|20|
|/|20|
|^|15|

## Grouping
We can group operations in order to circumvent the normal precedence order using paranthesis like so: `(2 + 3) * 2 = 10` vs `2 + 3 * 2 = 8`

# Mapping in compute
To alter the values used in an expression, we must supply a map that maps the new values to the correct operands. When we have an expression containing operands that are only numbers and groups, providing a mapping is optional. If we have variables in our expression however, we must supply a map.

Each operand is assigned an index, starting a 0 and increasing from left to right in the expression. Variables can also be mapped using their indexes, but also using their variable name.
```kotlin
// The compute function takes a Map<Mapping, Double> as input

// The Mapping-class specifies which operand that should be mapped
data class Mapping(val name: String? = null, val index: Int? = null)
``` 

package jerre.math


internal val numberRegex = """^\s*(-?\d+(?:\.\d+)?)""".toRegex()
internal val nonNumbers = """^\s*(\+|-|\*|/|\(|\)|)""".toRegex()

internal fun String.tokenize(): List<String> {
    val tokens = mutableListOf<String>()
    var startAtIndex = 0
    while (startAtIndex < length) {
        val rest = this.substring(startAtIndex)
        val nextTokenResult = numberRegex.find(rest) ?: nonNumbers.find(rest)
        val nextToken = nextTokenResult?.value?.trim() ?: ""

        // The following happens if we omit spaces like so: 1-2, instead of writing 1 - 2.
        // Since 1-2 will be interpreted as two numbers, we need special handling to convert it to 1 - 2
        if (nextToken.isNumber() && nextToken.first() == '-' && tokens.lastOrNull()?.isNumber() == true) {
            tokens.add("-")
            tokens.add(nextToken.substring(1))
        } else {
            tokens.add(nextToken)
        }
        startAtIndex += nextTokenResult?.value?.length ?: length
    }
    return tokens
}

internal fun List<String>.replaceValuesWithIndexes(): List<String> {
    var variableIndex = 0
    val output = mutableListOf<String>()
    for (i in 0 until size) {
        if (this[i].isNumber()) {
            output.add("${variableIndex++}")
        } else {
            output.add(this[i])
        }
    }
    return output
}

internal fun List<String>.indexOfMatchingGroupClose(): Int {

    require(isNotEmpty())
    var nUnMatchedGroupOpen = 1
    for (i in 1 until size) {
        if (this[i].isGroupCloseToken()) {
            nUnMatchedGroupOpen--
            if (nUnMatchedGroupOpen == 0) {
                return i
            }
        } else if (this[i].isGroupOpenToken()) {
            nUnMatchedGroupOpen++
        }
    }
    return -1
}

internal fun String.isGroupOpenToken(): Boolean = "(" == this
internal fun String.isGroupCloseToken(): Boolean = ")" == this
internal fun String.isNumber(): Boolean = numberRegex.matches(this)




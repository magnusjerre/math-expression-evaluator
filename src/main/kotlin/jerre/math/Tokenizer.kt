package jerre.math


internal val legalTokens = """\s+|\+|\-|\*|\/|\(|\)|\d+|\d+\.\d+""".toRegex()
internal val numberRegex = """^(\d+|\d+.\d+)$""".toRegex()


internal fun String.tokenize(): List<String> {
    val matchResult: Sequence<MatchResult> = legalTokens.findAll(this) ?: throw IllegalArgumentException("Invalid pattern")
    return matchResult.toList().flatMap {  mr -> mr.groupValues.map { it.trim() } }.filterNot { "\\s*".toRegex().matches(it) }
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




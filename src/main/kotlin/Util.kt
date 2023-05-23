// Pair of 1. The pattern to be searched, 2. The text in which we are searching
typealias PatternText = Pair<String, String>

// Matrix, Pair<Pattern, Text>
typealias DistanceMatrix = Pair<Array<IntArray>, PatternText>

// Matrix of the sources from where I come
typealias SourceMatrix = Array<Array<Source>>

// SourceMatrix and the corresponding pattern and text
typealias BacktracingMatrix = Pair<SourceMatrix, PatternText>

// List of columns where an alignments ends and the corresponding costs
typealias ColAndCosts = List<Pair<Int, Int>>

// Pair<Pattern, Text> with computed gaps and Matches / Mismatches
typealias Alignment = Pair<String, String>

const val diagonalArrow: Char = '\u2196'
const val leftArrow: Char = '\u2190'
const val upArrow: Char = '\u2191'

/**
 * Calculates the costs for a specific position in the alignment with a given cost function.
 */
fun cost(charA: Char, charB: Char, costs: Costs): Int {
    return if (charA == charB) {
        costs.match
    } else {
        costs.mismatch
    }
}

/**
 * Adds a given Character, that should not be part of the alphabet of the text or the pattern,
 * to the beginning of a string.
 */
fun addEmptyString(string: String, symbolEmpty: Char): String {
    return symbolEmpty.plus(string)
}

/**
 * Prints the distance matrix.
 */
fun printDictanceMatrix(distanceMatrix: DistanceMatrix) {
    val pattern: String = distanceMatrix.second.first
    val text: String = distanceMatrix.second.second
    val matrix: Array<IntArray> = distanceMatrix.first
    println()
    println(text.toCharArray().joinToString(separator = "   ", prefix = "       "))
    println("    " + "-".repeat(4 * text.length + 2))
    for ((i, inner) in matrix.withIndex()) {
        println( pattern[i] + inner.joinToString(separator = " ", prefix = "   [", postfix = " ]" )
        { j -> "%3d".format(j) } )
    }
}

/**
 * Prints output columns and the corresponding costs.
 */
fun printColAndCosts(list: MutableList<Pair<Int, Int>>) {
    for (pair in list) {
        println("\nColumn: ${pair.first} || Costs: ${pair.second}")
    }
}

/**
 * Prints the backtracing matrix
 */
fun printBacktracingMatrix(backtracingMatrix: BacktracingMatrix) {
    val pattern: String = backtracingMatrix.second.first
    val text: String = backtracingMatrix.second.second
    val matrix: SourceMatrix = backtracingMatrix.first
    println("\n" + text.toCharArray().joinToString(separator = "    ", prefix = "       "))
    println("    " + "-".repeat(5 * text.length + 2))
    for ((i, inner) in matrix.withIndex()) {
        println(pattern[i] + inner.joinToString(separator = "  ", prefix = "   [ ", postfix = " ]" )
        { j -> sourceToString(j).format(j) } )
    }
}

/**
 * Converts a source from a backtracing matrix into a String, that can be printed on the console.
 */
fun sourceToString(source: Source): String {
    val diagonalTopLeft = Source(diagonal = true, top = true, left = true)
    val diagonalTop     = Source(diagonal = true, top = true, left = false)
    val diagonalLeft    = Source(diagonal = true, top = false, left = true)
    val diagonal        = Source(diagonal = true, top = false, left = false)
    val topLeft         = Source(diagonal = false, top = true, left = true)
    val top             = Source(diagonal = false, top = true, left = false)
    val left            = Source(diagonal = false, top = false, left = true)

    return when (source) {
        diagonalTopLeft -> ("$diagonalArrow$upArrow$leftArrow")
        diagonalTop     -> ("$diagonalArrow$upArrow ")
        diagonalLeft    -> ("$diagonalArrow $leftArrow")
        diagonal        -> (" $diagonalArrow ")
        topLeft         -> (" $upArrow$leftArrow")
        top             -> (" $upArrow ")
        left            -> (" $leftArrow ")
        else            -> ("   ")
    }
}

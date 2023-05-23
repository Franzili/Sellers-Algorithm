@file:Suppress("DuplicatedCode")

import kotlin.math.min

/**
 * This function computes a distance matrix with the Seller's Algorithm.
 *
 * Returns a Pair of
 * 1. The distance-matrix as a result of the pairwise Alignment
 * 2. The columns where a match ends and the corresponding costs
 */
fun sellers(pattern: String, text: String, costs: Costs, emptyStringSymbol: Char): Pair<Array<IntArray>, ColAndCosts> {

    // Pairs of columns where a match ends and the corresponding costs
    val outputColumns: MutableList<Pair<Int, Int>> = ArrayList()

    // Add a symbol to the beginning of the given words, that are not part of the alphabet
    val patternE: String = addEmptyString(pattern, emptyStringSymbol)
    val textE: String    = addEmptyString(text, emptyStringSymbol)

    // Matrix -> Array of IntArrays
    val dMatrix: Array<IntArray> = Array(patternE.length) { IntArray(textE.length) { Int.MAX_VALUE } }

    // Initialize the first column
    for (i in dMatrix.indices) {
        dMatrix[i][0] = i * costs.gap
    }

    // Proceed column-wise
    val n = dMatrix[0].size     // text
    val m = dMatrix.size        // pattern
    for (j in (1 until n)) {
        dMatrix[0][j] = 0
        for (i in (1 until m)) {
            val diagonal  = dMatrix[i-1][j-1] + cost(patternE[i], textE[j],
                            Costs(costs.threshold, costs.match, costs.mismatch, costs.gap))
            val left      = dMatrix[i][j-1] + costs.gap
            val above     = dMatrix[i-1][j] + costs.gap
            dMatrix[i][j] = min(min(diagonal, left), above)
        }
        // If we are in the last row and the costs are under the threshold, return this column and the costs
        if (dMatrix[m-1][j] <= costs.threshold) {
            outputColumns.add(Pair(j, dMatrix[m - 1][j]))
        }
    }
    val toAlign = Pair(patternE, textE)
    printDictanceMatrix(DistanceMatrix(dMatrix, toAlign))
    return Pair(dMatrix, outputColumns)
}


/**
 * Seller's Algorithm that computes each column, only until reaching some index called lastEssentialIndex, where we
 * know that when we have reached this index, the costs can only get worse, so we can break and skip computing the
 * following cells in that column.
 *
 * Returns a list of Pair<Column, Costs>, that represents the output columns and it's costs
 */
fun sellersWithCutoff(pattern: String, text: String, costs: Costs, emptyStringSymbol: Char): ColAndCosts {

    // Pairs of columns where a match ends and the corresponding costs
    val outputColumns: MutableList<Pair<Int, Int>> = ArrayList()

    // Add a symbol to the beginning of the given words, that are not part of the alphabet
    val patternE: String = addEmptyString(pattern, emptyStringSymbol)
    val textE: String    = addEmptyString(text, emptyStringSymbol)

    // Backtracing matrix
    val bMatrix: SourceMatrix = Array(patternE.length)
        { Array(textE.length) { Source(diagonal = false, top = false, left = false) } }
    bMatrix[0] = Array(textE.length) { Source(diagonal = false, top = false, left = true) }

    // Two arrays are used to simulate the two columns that are needed for the algorithm
    val listEvenJ = IntArray(patternE.length) { Int.MAX_VALUE - costs.gap }
    val listOddJ  = IntArray(patternE.length) { Int.MAX_VALUE - costs.gap }

    // Last essential index for the first column, rounded down
    var lastEssentialIndex: Int = (costs.threshold.toFloat() / costs.gap.toFloat()).toInt()

    // Initialize the first column
    for (i in (0 .. lastEssentialIndex)) {
        listEvenJ[i] = i * costs.gap
    }

    // Proceed column-wise
    for (j in (1 .. text.length)) {
        listOddJ[0] = 0
        val colLength = min(pattern.length, lastEssentialIndex + 1)    // lastEssentialIndex of the previous column + 1

        // j is even, so we are working in listEvenJ
        if (j % 2 == 0) {

            // Fill the column
            for (i in (1 .. colLength)) {
                val diagonal    = listOddJ[i - 1] + cost(patternE[i], textE[j], costs)
                val left        = listOddJ[i] + costs.gap
                val top         = listEvenJ[i - 1] + costs.gap
                val min         = min(diagonal, min(left, top))
                listEvenJ[i]    = min

                // Fill the backtracing matrix
                bMatrix[i][j]   = Source(diagonal = diagonal == min, top = top == min, left = left == min)
            }

            if (listEvenJ[colLength] < costs.threshold) {   // we are under the threshold, so we compute a new lastEssentialIndex
                lastEssentialIndex = min(pattern.length, colLength
                        + (costs.threshold - ((listEvenJ[colLength]).toFloat() / (costs.gap).toFloat())).toInt())
                for (x in (colLength + 1) .. lastEssentialIndex) {  // and continue computing the matrix until we reach the new lastEssentialIndex
                    listEvenJ[x] = listEvenJ[x - 1] + costs.gap
                }

            } else {    // compute lastEssentialIndex = max{ i in [0, colLength] : D(i,j) <= k }
                if (listEvenJ[colLength] == costs.threshold) {
                    lastEssentialIndex = colLength
                } else {
                    for (x in (0 .. colLength)) {
                        if (listEvenJ[x] > costs.threshold) {
                            lastEssentialIndex = x - 1
                        }
                    }
                }
            }
            if (lastEssentialIndex == pattern.length) {    // reached the last row, so we can report
                outputColumns.add(Pair(j, listEvenJ[pattern.length]))
            }

            // j is odd, so we are working in listOddJ
        } else {

            // Fill the column
            for (i in (1 .. colLength)) {
                val diagonal    = listEvenJ[i - 1] + cost(patternE[i], textE[j], costs)
                val left        = listEvenJ[i] + costs.gap
                val top         = listOddJ[i - 1] + costs.gap
                val min         = min(diagonal, min(left, top))
                listOddJ[i]     = min

                // Fill the backtracing matrix
                bMatrix[i][j]   = Source(diagonal = diagonal == min, top = top == min, left = left == min)
            }
            if (listOddJ[colLength] < costs.threshold) {    // we are under the threshold, so we compute a new lastEssentialIndex
                lastEssentialIndex = min(pattern.length, colLength
                        + (costs.threshold - ((listOddJ[colLength]).toFloat() / (costs.gap).toFloat())).toInt())
                for (x in (colLength + 1) .. lastEssentialIndex) {  // and continue computing the matrix until we reach the new lastEssentialIndex
                    listOddJ[x] = listOddJ[x - 1] + costs.gap
                }

            } else {    // compute lastEssentialIndex = max{ i in [0, colLength] : D(i,j) <= k }
                if (listOddJ[colLength] == costs.threshold) {
                    lastEssentialIndex = colLength
                } else {
                    for (x in (0 .. colLength)) {
                        if (listOddJ[x] > costs.threshold) {
                            lastEssentialIndex = x - 1
                        }
                    }
                }
            }
            if (lastEssentialIndex == pattern.length) {    // reached the last row, so we can report
                outputColumns.add(Pair(j, listOddJ[pattern.length]))
            }
        }
    }
    printColAndCosts(outputColumns)

    // Get the columns with minimal costs
    var minCost     = Int.MAX_VALUE
    val minCostCols = ArrayList<Int>()
    for (pair in outputColumns) {
        if (pair.second <= minCost) {
            minCost = pair.second
            minCostCols.add(pair.first)
        }
    }

    // Print all alignments with minimal costs
    val backtracingMatrix: BacktracingMatrix = BacktracingMatrix(bMatrix, Pair(patternE, textE))
    val backtracing = Backtracing(backtracingMatrix)
    printBacktracingMatrix(backtracingMatrix)
    for (column in minCostCols) {
        print("\nAlignments with minimal costs of ${minCost}:\n")
        backtracing.printAlignments(backtracing.backtracing(Pair(patternE.length - 1, column)))
    }

    // Print all possible alignments
    /*
    for (pair in outputColumns) {
        print("\nAlignments with costs of ${pair.second}:\n")
        backtracing.printAlignments(backtracing.backtracing(Pair(patternE.length - 1, pair.first)))
    }
    */

    return outputColumns
}


fun main(args: Array<String>) {

    // Use to compute the distance matrix

    sellers("ABI", "BARBIER", Costs(1,0,1,1), '$')
    sellersWithCutoff("ABI", "BARBIER", Costs(1,0,1,1), '$')


    sellers("AABB", "BABAABABB", Costs(1,0,1,1),'$')
    sellersWithCutoff("AABB", "BABAABABB", Costs(1,0,1,1),'$')

}

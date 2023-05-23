class Backtracing(backtracingMatrix: BacktracingMatrix) {

    private val pattern: String = backtracingMatrix.second.first
    private val text: String = backtracingMatrix.second.second
    private val matrix: SourceMatrix = backtracingMatrix.first

    /**
     * Calls the {@see recursiveBacktracing} method and appends the last chars of the alignment
     * that are behind the starting position of the alignment.
     */
    fun backtracing(position: Pair<Int, Int>): List<Alignment> {
        val gapText     = text.length - position.second - 1
        val newPattern  = "-".repeat(gapText).plus(pattern[position.first])
        val newText     = text.subSequence(position.second, text.length).toString()
        return recursiveBacktracing(position, Pair(newPattern, newText.reversed()))
    }

    /**
     * Reconstructs all possible alignments from a given {@param position} and returns a list of these alignments.
     */
    private fun recursiveBacktracing(position: Pair<Int, Int>, alignment: Alignment): List<Alignment> {
        val alignments: MutableList<Alignment> = ArrayList()
        if (position.first == 0 && position.second == 0) {
            alignments.add(Pair(alignment.first.reversed(), alignment.second.reversed()))
            return alignments
        }
        val source: Source = matrix[position.first][position.second]
        val alignedPattern = alignment.first
        val alignedText    = alignment.second

        if (source.diagonal) {
            val newPattern = alignedPattern + pattern[position.first - 1]
            val newText    = alignedText + text[position.second - 1]
            alignments.addAll(recursiveBacktracing(Pair(position.first - 1, position.second - 1),
                    Pair(newPattern, newText)))
        }
        if (source.top) {
            val newPattern = alignedPattern + pattern[position.first - 1]
            val newText    = alignedText.plus("-")
            alignments.addAll(recursiveBacktracing(Pair(position.first - 1, position.second), Pair(newPattern, newText)))
        }
        if (source.left) {
            val newPattern = alignedPattern.plus("-")
            val newText    = alignedText + text[position.second - 1]
            alignments.addAll(recursiveBacktracing(Pair(position.first, position.second - 1), Pair(newPattern, newText)))
        }
        return alignments
    }

    /**
     * Prints each alignment in a given list of {@param alignments}.
     */
    fun printAlignments(alignments: List<Alignment>) {
        for (alignment: Alignment in alignments) {
            println()
            println(alignment.first)
            println(alignment.second)
        }
    }
}
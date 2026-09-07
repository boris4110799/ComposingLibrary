package tw.boris4110799.composing.library.ui.util

object FilterUtil {
    /** Alphabet and Numeric */
    val alphanumeric: (Char) -> Boolean = { it.toString() matches Regex("""[A-Za-z\d]""") }

    /** Printable ASCII char */
    val printableChar: (Char) -> Boolean = { it.toString() matches Regex("""\p{Print}""") }

    /**
     * The char filter that match the [regex]
     */
    fun regexFilter(regex: Regex): (Char) -> Boolean = { it.toString() matches regex }
}

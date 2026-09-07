package tw.boris4110799.composing.library.common.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char

/**
 * The utility for `kotlin-datetime`.
 */
object DateTimeUtil {
    /**
     * The pattern of date time.
     */
    enum class Pattern(
        val format: String,
        val toDateTimeFormat: () -> DateTimeFormat<LocalDateTime>
    ) {
        DATE_TIME("yyyy/MM/dd HH:mm:ss.SSS", {
            LocalDateTime.Format {
                year()
                char('/')
                monthNumber()
                char('/')
                day()
                char(' ')
                hour()
                char(':')
                minute()
                char(':')
                second()
                char('.')
                secondFraction(3)
            }
        })
    }

    /**
     * Format the [dateTime] with [pattern] or return empty string if pattern not matched.
     */
    fun format(
        dateTime: LocalDateTime,
        pattern: Pattern
    ) = try {
        dateTime.format(pattern.toDateTimeFormat())
    } catch (e: Exception) {
        ""
    }

    /**
     * Parse the [dateTime] with [pattern] or return null if pattern not matched.
     */
    fun parse(
        dateTime: String,
        pattern: Pattern
    ) = try {
        LocalDateTime.parse(dateTime, pattern.toDateTimeFormat())
    } catch (e: Exception) {
        null
    }
}

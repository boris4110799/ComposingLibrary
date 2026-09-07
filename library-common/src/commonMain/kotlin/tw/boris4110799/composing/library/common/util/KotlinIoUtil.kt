package tw.boris4110799.composing.library.common.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.readLine

/**
 * The utility for `kotlin-io`.
 */
object KotlinIoUtil {
    fun RawSource.readLines(): Flow<String> = flow {
        buffered().use { source ->
            while (true) {
                val line = try {
                    source.readLine()
                } catch (e: Exception) {
                    null
                } ?: break

                emit(line)
            }
        }
    }
}

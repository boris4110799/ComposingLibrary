package tw.boris4110799.composing.library.common.result

/**
 * The error that can be thrown by [AppResult].
 */
interface AppError

data object NetworkError : AppError

data class ExceptionError(val exception: Exception) : AppError

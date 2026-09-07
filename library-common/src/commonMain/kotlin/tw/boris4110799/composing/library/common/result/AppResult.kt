package tw.boris4110799.composing.library.common.result

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * The wrap up of result.
 */
sealed interface AppResult<out T, out E : AppError> {
    data class Success<out T>(val data: T) : AppResult<T, Nothing>

    data class Error<out E : AppError>(val error: E) : AppResult<Nothing, E>
}

/**
 * Return whether result is success.
 */
@OptIn(ExperimentalContracts::class)
fun <T, E : AppError> AppResult<T, E>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is AppResult.Success<T>)
    }
    return this is AppResult.Success<T>
}

/**
 * Return whether result is error.
 */
@OptIn(ExperimentalContracts::class)
fun <T, E : AppError> AppResult<T, E>.isError(): Boolean {
    contract {
        returns(true) implies (this@isError is AppResult.Error<E>)
    }
    return this is AppResult.Error<E>
}

/**
 * Return the data of success or null.
 */
fun <T, E : AppError> AppResult<T, E>.getOrNull(): T? {
    return when {
        isSuccess() -> data
        else        -> null
    }
}

/**
 * Return the data of success or the default value.
 */
fun <T, E : AppError> AppResult<T, E>.getOrDefault(defaultValue: T): T {
    return when {
        isSuccess() -> data
        else        -> defaultValue
    }
}

/**
 * Map the data of success.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, E : AppError, R> AppResult<T, E>.map(transform: (T) -> R): AppResult<R, E> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Error   -> this
    }
}

/**
 * Map the error.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, E : AppError, R : AppError> AppResult<T, E>.mapError(transform: (E) -> R): AppResult<T, R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is AppResult.Success -> this
        is AppResult.Error   -> AppResult.Error(transform(error))
    }
}

/**
 * Flat map the data of success.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, E : AppError, R> AppResult<T, E>.flatMap(transform: (T) -> AppResult<R, E>): AppResult<R, E> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is AppResult.Success -> transform(data)
        is AppResult.Error   -> this
    }
}

/**
 * Flat map the error.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, E : AppError, R : AppError> AppResult<T, E>.flatMapError(transform: (E) -> AppResult<T, R>): AppResult<T, R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is AppResult.Success -> this
        is AppResult.Error   -> transform(error)
    }
}

/**
 * Fold the result.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, E : AppError, R> AppResult<T, E>.fold(
    onSuccess: (T) -> R,
    onError: (AppError) -> R
): R {
    contract {
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onError, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is AppResult.Success -> onSuccess(data)
        is AppResult.Error   -> onError(error)
    }
}

/**
 * Execute the [action] if result is success.
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified T, E : AppError> AppResult<T, E>.onSuccess(crossinline action: (T) -> Unit): AppResult<T, E> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (isSuccess()) {
        action(data)
    }
    return this
}

/**
 * Execute the [action] if result is error.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, reified E : AppError> AppResult<T, E>.onError(crossinline action: (E) -> Unit): AppResult<T, E> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (isError()) {
        action(error)
    }
    return this
}

/**
 * Convert to [AppResult] with [predicate].
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, E : AppError, R> T.toAppResult(
    predicate: (T) -> Boolean,
    onSuccess: (T) -> R,
    onError: (T) -> E
): AppResult<R, E> {
    contract {
        callsInPlace(predicate, InvocationKind.EXACTLY_ONCE)
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onError, InvocationKind.AT_MOST_ONCE)
    }
    return if (predicate(this)) {
        AppResult.Success(onSuccess(this))
    } else {
        AppResult.Error(onError(this))
    }
}

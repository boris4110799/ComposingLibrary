@file:OptIn(ExperimentalContracts::class)

package tw.boris4110799.composing.library.common.extensions

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Add the item to list.
 */
fun <T> List<T>.add(item: T): List<T> = toMutableList().apply {
    add(item)
}

/**
 * Add the element at the specified position in this list.
 */
fun <T> List<T>.addAt(
    index: Int,
    item: T
): List<T> {
    check(index in indices) { "Index $index is out of bounds for list of size $size" }

    return toMutableList().apply {
        add(index, item)
    }
}

/**
 * Remove the item to list.
 */
fun <T> List<T>.remove(item: T): List<T> = toMutableList().apply {
    remove(item)
}

/**
 * Remove the element at the specified position in this list.
 */
fun <T> List<T>.removeAt(index: Int): List<T> {
    check(index in indices) { "Index $index is out of bounds for list of size $size" }

    return toMutableList().apply {
        removeAt(index)
    }
}

/**
 * Updates the element at the specified position in this list.
 */
inline fun <T> List<T>.updateAt(
    index: Int,
    newValue: (T) -> T
): List<T> {
    contract {
        callsInPlace(newValue, InvocationKind.EXACTLY_ONCE)
    }
    check(index in indices) { "Index $index is out of bounds for list of size $size" }

    return toMutableList().apply {
        set(index, newValue(get(index)))
    }
}

/**
 * Updates the element of the specified element in this list.
 */
inline fun <T> List<T>.update(
    item: T,
    newValue: (T) -> T
): List<T> {
    contract {
        callsInPlace(newValue, InvocationKind.AT_MOST_ONCE)
    }
    val index = indexOf(item)

    if (index >= 0) {
        return updateAt(index, newValue)
    }

    return this
}

/**
 * Updates the element of matching the given [predicate] in this list.
 */
inline fun <T> List<T>.updateBy(
    predicate: (T) -> Boolean,
    newValue: (T) -> T
): List<T> {
    contract {
        callsInPlace(newValue, InvocationKind.AT_MOST_ONCE)
    }
    val index = indexOfFirst(predicate)

    if (index >= 0) {
        return updateAt(index, newValue)
    }

    return this
}

/**
 * Updates all the elements in this list.
 */
inline fun <T> List<T>.updateAll(newValue: (T) -> T): List<T> = toMutableList().apply {
    for (index in indices) {
        set(index, newValue(get(index)))
    }
}

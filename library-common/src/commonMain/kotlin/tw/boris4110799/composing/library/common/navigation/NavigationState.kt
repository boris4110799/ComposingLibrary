package tw.boris4110799.composing.library.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

/**
 * Create a navigation state that persists config changes and process death.
 */
@Composable
expect inline fun <reified T : NavKey> rememberNavigationState(
    startRoute: T,
    topLevelRoutes: Set<T>
): NavigationState<T>

/**
 * State holder for navigation state.
 *
 * @param startRoute the start NavKey. The user will exit the app through this NavKey.
 * @param topLevelRoute the current top level NavKey.
 * @param backStacks the back stacks for each top level NavKey.
 */
class NavigationState<T : NavKey>(
    val startRoute: T,
    topLevelRoute: MutableState<T>,
    val backStacks: Map<T, NavBackStack<T>>
) {
    /** Current top level route. */
    var topLevelRoute: T by topLevelRoute

    /** Current top level back stack. */
    val topLevelStack: List<T>
        get() = if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }
}

/**
 * Convert NavigationState into NavEntries.
 */
@Composable
fun <T : NavKey> NavigationState<T>.toEntries(entryProvider: (T) -> NavEntry<T>): SnapshotStateList<NavEntry<T>> {
    val decoratedEntries = backStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<T>(),
            rememberViewModelStoreNavEntryDecorator()
        )

        rememberDecoratedNavEntries(
            backStack = stack, entryDecorators = decorators, entryProvider = entryProvider
        )
    }

    return topLevelStack.flatMap { decoratedEntries[it] ?: emptyList() }.toMutableStateList()
}

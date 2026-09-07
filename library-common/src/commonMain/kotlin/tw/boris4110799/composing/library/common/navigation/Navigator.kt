package tw.boris4110799.composing.library.common.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Interface for navigation events.
 */
interface Navigator<T : NavKey> {
    /**
     * Navigate to [route].
     */
    fun navigate(route: T)

    /**
     * Go back to the previous route.
     */
    fun goBack()
}

/**
 * Default implementation of [Navigator].
 */
open class DefaultNavigator<T : NavKey>(val navigationState: NavigationState<T>) : Navigator<T> {

    override fun navigate(route: T) {
        if (route in navigationState.backStacks.keys) {
            // This is a top level route, just switch to it.
            navigationState.topLevelRoute = route
        } else {
            navigationState.backStacks[navigationState.topLevelRoute]?.add(route)
        }
    }

    override fun goBack() {
        val currentStack =
            navigationState.backStacks[navigationState.topLevelRoute] ?: error("Stack for ${navigationState.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        // If we're at the base of the current route, go back to the start route stack.
        if (currentRoute == navigationState.topLevelRoute) {
            navigationState.topLevelRoute = navigationState.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}

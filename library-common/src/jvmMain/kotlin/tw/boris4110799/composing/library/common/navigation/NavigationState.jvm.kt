package tw.boris4110799.composing.library.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import kotlinx.serialization.serializer

@Composable
actual inline fun <reified T : NavKey> rememberNavigationState(
    startRoute: T,
    topLevelRoutes: Set<T>
): NavigationState<T> {
    val topLevelRoute = rememberSerializable(
        startRoute, topLevelRoutes, serializer = MutableStateSerializer(serializer<T>())
    ) {
        mutableStateOf(startRoute)
    }

    val backStacks = topLevelRoutes.associateWith { key ->
        rememberSerializable(serializer = NavBackStackSerializer<T>()) {
            NavBackStack(key)
        }
    }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(startRoute, topLevelRoute, backStacks)
    }
}

package tw.boris4110799.composing.library.ui.components.gestures

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

/**
 * The item that can be swiped left or right.
 * @param backgroundColor The background color of item.
 * @param startActionWidth The width of start action.
 * @param startAction The content of start action.
 * @param endActionWidth The width of end action.
 * @param endAction The content of end action.
 * @param content The content of item.
 */
@Composable
fun SwipeableItem(
    modifier: Modifier = Modifier,
    state: AnchoredDraggableState<SwipeableAnchors> = rememberSwipeableState(),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    startActionWidth: Dp = SwipeableScope.DefaultActionWidth,
    startAction: @Composable (SwipeableScope.() -> Unit) = {},
    endActionWidth: Dp = SwipeableScope.DefaultActionWidth,
    endAction: @Composable (SwipeableScope.() -> Unit) = {},
    content: @Composable (RowScope.() -> Unit),
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val startScope = remember { SwipeableScope(startActionWidth) }
    val endScope = remember { SwipeableScope(endActionWidth) }
    // +0.5f for distinguish from Center (0)
    val startActionSizePx =
        with(density) { (startActionWidth * startScope.actionCount).toPx() } + 0.5f
    val endActionSizePx = with(density) { (endActionWidth * endScope.actionCount).toPx() } + 0.5f

    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = state,
        positionalThreshold = { distance -> distance * 0.5f },
        animationSpec = tween()
    )

    LaunchedEffect(startScope.actionCount, endScope.actionCount) {
        state.updateAnchors(newAnchors = DraggableAnchors {
            SwipeableAnchors.Start at -startActionSizePx
            SwipeableAnchors.Center at 0f
            SwipeableAnchors.End at endActionSizePx
        })
    }

    LaunchedEffect(state) {
        snapshotFlow { state.targetValue }.distinctUntilChanged().collect { anchor ->
            if (anchor != SwipeableAnchors.Center) {
                focusRequester.requestFocus()
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Row(
            modifier = Modifier.width(IntrinsicSize.Min)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
        ) {
            startScope.startAction()
        }

        Row(
            modifier = Modifier.width(IntrinsicSize.Min).fillMaxHeight().align(Alignment.CenterEnd)
        ) {
            endScope.endAction()
        }

        Row(
            modifier = Modifier.fillMaxSize()
                .offset { IntOffset(x = -state.requireOffset().roundToInt(), y = 0) }
                .anchoredDraggable(
                    state, true, Orientation.Horizontal, flingBehavior = flingBehavior
                )
                .focusRequester(focusRequester)
                .onFocusChanged {
                    // Make item back to center when lose focus
                    if (!it.hasFocus) {
                        scope.launch {
                            try {
                                state.animateTo(SwipeableAnchors.Center)
                            } catch (e: CancellationException) {
                                // Back to center when animation is interrupted
                                state.snapTo(SwipeableAnchors.Center)
                            }
                        }
                    }
                }
                .focusable()
                .background(backgroundColor) then modifier,
            verticalAlignment = Alignment.CenterVertically) {

            content()
        }
    }
}

/**
 * Custom swipe state
 */
enum class SwipeableAnchors {
    Start, Center, End
}

/**
 * Create a [AnchoredDraggableState] that is remembered across compositions.
 */
@Composable
fun rememberSwipeableState() = remember {
    AnchoredDraggableState(SwipeableAnchors.Center, DraggableAnchors {
        SwipeableAnchors.Start at -0.5f
        SwipeableAnchors.Center at 0f
        SwipeableAnchors.End at 0.5f
    })
}

/**
 * The scope of [SwipeableItem].
 * @param actionWidth The width of action.
 */
class SwipeableScope(private val actionWidth: Dp = DefaultActionWidth) {

    companion object {
        /**
         * Default action width.
         */
        val DefaultActionWidth = 80.dp
    }

    /**
     * The count of actions.
     */
    var actionCount by mutableIntStateOf(0)
        private set

    /**
     * Custom action.
     * @param content Action content.
     */
    @Composable
    fun Action(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        shape: Shape = RectangleShape,
        containerColor: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = contentColorFor(containerColor),
        content: @Composable () -> Unit
    ) {
        DisposableEffect(Unit) {
            actionCount += 1

            onDispose {
                actionCount -= 1
            }
        }

        Surface(
            onClick = onClick,
            modifier = Modifier.width(actionWidth).fillMaxHeight() then modifier,
            shape = shape,
            color = containerColor,
            contentColor = contentColor
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwipeableItemPreview() {
    SwipeableItem(backgroundColor = Color.Gray, startAction = {
        Action(onClick = {}, containerColor = Color.Yellow) {
            Text(text = "Edit")
        }
    }, endAction = {
        Action(onClick = {}, containerColor = Color.Red) {
            Text(text = "Delete")
        }
    }) {
        Text(text = "SwipeableItem", style = MaterialTheme.typography.titleLarge)
    }
}

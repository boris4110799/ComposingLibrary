package tw.boris4110799.composing.library.ui.components.textfields

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Wrapper for State-Driven TextField.
 *
 * @param value Initial value.
 * @param onValueChange The callback that is triggered when the input service updates the text.
 * @param allowExternalUpdate Set whether the text field value can be updated by [value]. Since new text field mechanism not depend on state change, set this to true make text field updated.
 * @param content text field content
 */
@Composable
fun TextFieldWrapper(
    value: String = "",
    onValueChange: (String) -> Unit,
    allowExternalUpdate: Boolean = false,
    content: @Composable (TextFieldState) -> Unit
) {
    val textFieldState = rememberTextFieldState(initialText = value)

    val textFlow = snapshotFlow { textFieldState.text.toString() }

    var currentValue by rememberSaveable { mutableStateOf(value) }

    LaunchedEffect(textFieldState) {
        textFlow.distinctUntilChanged().collectLatest {
            onValueChange(it)
            currentValue = it
        }
    }

    LifecycleResumeEffect(value) {
        if (allowExternalUpdate && value != currentValue) {
            textFieldState.setTextAndPlaceCursorAtEnd(value)
        }

        onPauseOrDispose { }
    }

    content(textFieldState)
}

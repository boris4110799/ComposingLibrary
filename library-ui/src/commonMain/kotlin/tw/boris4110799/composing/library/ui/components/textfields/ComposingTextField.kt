package tw.boris4110799.composing.library.ui.components.textfields

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.byValue
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A text field that wrap up with Composing.
 * @param textSelectionColors Customize cursor and selection background colors
 * @see BasicTextField
 */
@Composable
fun ComposingTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    textStyle: TextStyle = TextStyle.Default,
    inputTransformation: ComposingInputTransformation.() -> InputTransformation? = { null },
    outputTransformation: ComposingOutputTransformation.() -> OutputTransformation? = { null },
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    cursorBrush: Brush = SolidColor(Color.Black),
    textSelectionColors: TextSelectionColors = TextSelectionColors(
        MaterialTheme.colorScheme.onSecondaryContainer, MaterialTheme.colorScheme.secondaryContainer
    ),
    decorator: @Composable ComposingTextFieldDecoration.() -> TextFieldDecorator = { underlinedDecorator() }
) {
    val input = remember { ComposingInputTransformation() }
    val output = remember { ComposingOutputTransformation() }
    val decoration = remember { ComposingTextFieldDecoration(state) }

    CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
        BasicTextField(
            state = state,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            inputTransformation = input.inputTransformation(),
            textStyle = textStyle.merge(color = textColor),
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            cursorBrush = cursorBrush,
            outputTransformation = output.outputTransformation(),
            decorator = decoration.decorator()
        )
    }
}

/**
 * InputTransformation for ComposingTextField.
 */
class ComposingInputTransformation {
    fun filterByChar(filter: (Char) -> Boolean) = InputTransformation.byValue { current, proposed ->
        proposed.filter(filter)
    }

    fun maxLength(length: Int) = InputTransformation.byValue { current, proposed ->
        proposed.take(length)
    }
}

/**
 * OutputTransformation for ComposingTextField.
 */
class ComposingOutputTransformation {
    fun dateBySlash() = OutputTransformation {
        if (originalText.length >= 7) {
            insert(6, "/")
        }
        if (originalText.length >= 5) {
            insert(4, "/")
        }
    }
}

/**
 * Decoration for ComposingTextField.
 */
class ComposingTextFieldDecoration(private val state: TextFieldState) {
    val currentText: CharSequence
        get() = state.text

    @Composable
    fun decoratorLayout(
        modifier: Modifier = Modifier,
        contentPadding: PaddingValues = PaddingValues(0.dp),
        content: @Composable BoxScope.() -> Unit = {}
    ) = TextFieldDecorator { innerTextField ->
        Box(
            modifier = Modifier.height(IntrinsicSize.Min).then(modifier).padding(contentPadding),
            contentAlignment = Alignment.BottomStart
        ) {
            innerTextField()

            content()
        }
    }

    @Composable
    fun underlinedDecorator() = decoratorLayout {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }

    @Composable
    fun outlinedDecorator() = decoratorLayout(
        modifier = Modifier.border(
            1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small
        ), contentPadding = PaddingValues(8.dp)
    )

    @Composable
    fun hintDecorator(hint: String) = decoratorLayout {
        if (currentText.isEmpty() && hint.isNotBlank()) {
            Text(text = hint, color = MaterialTheme.colorScheme.outline)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

@Preview(showBackground = true)
@Composable
private fun ComposingTextFieldPreview() {
    val state = rememberTextFieldState()

    Box(modifier = Modifier.padding(16.dp)) {
        ComposingTextField(
            state = state,
            inputTransformation = { filterByChar { it.isDigit() }.then(maxLength(8)) },
            outputTransformation = { dateBySlash() },
            decorator = { hintDecorator("Enter date") })
    }
}

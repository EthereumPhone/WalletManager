package com.core.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@SuppressLint("SuspiciousIndentation")
@Composable
fun DgenBasicSearchTextfield(
    value: TextFieldValue = TextFieldValue(""),
    onValueChange: (TextFieldValue) -> Unit,
    keyboardtype: KeyboardType =  KeyboardType.Text,
    textfieldFocusManager: FocusManager? = null,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    maxLines: Int = 1,
    cursorColor: Color = DgenTheme.colors.dgenWhite,
    cursorWidth: Dp = 12.dp,
    cursorHeight: Dp = 32.dp,
    textStyle: TextStyle = DgenTheme.typography.body2,
    placeholder: @Composable() (() -> Unit)? = null,
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    var isFocused by remember { mutableStateOf(true) }
    val focusManager = textfieldFocusManager ?: LocalFocusManager.current

    val cursorAlpha by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val customTextSelectionColors = TextSelectionColors(
        handleColor = Color.Transparent,     // <- Hides the handle
        backgroundColor = Color.Transparent  // <- Optional: also hides selection highlight
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.text.isEmpty()){
            placeholder?.invoke()
        }


        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier
                    .fillMaxWidth()
                    .drawBehind {
                        if (isFocused){
                            textLayoutResult?.let {
                                val cursorRect = it.getCursorRect(value.selection.start)
                                val cursorX = cursorRect.left
                                val maxCursorX = size.width - cursorWidth.toPx()
                                val clampedCursorX = cursorX.coerceIn(0f, maxCursorX)

                                // Adjust cursor Y position when text field is empty to align with placeholder
                                val cursorYOffset = if (value.text.isEmpty()) -2.dp.toPx() else 0f
                                val cursorY = ((cursorRect.top + cursorRect.bottom) / 2 ) - (cursorHeight.toPx() /2) + cursorYOffset

                                drawRect(
                                    color = cursorColor,
                                    topLeft = Offset(clampedCursorX, cursorY),
                                    size = androidx.compose.ui.geometry.Size(cursorWidth.toPx(), cursorHeight.toPx()),
                                    alpha = cursorAlpha
                                )
                            }
                        }
                    }
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    }
                ,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = keyboardtype
                ),

                keyboardActions = KeyboardActions(
                    onGo = {
                        isFocused = false
                    },
                    onDone = {
                        focusManager.clearFocus() // Fokus entfernen, wenn Enter gedrückt wird
                        isFocused = false
                    }
                ),
                textStyle = textStyle,
                visualTransformation = VisualTransformation.None, // Ensure no transformations
                enabled = enabled,
                readOnly = readOnly,
                cursorBrush = SolidColor(Color.Unspecified),
                minLines = minLines,
                maxLines = maxLines,
                interactionSource = interactionSource,
                onTextLayout = { textLayoutResult = it },
                singleLine = true
            )
        }
    }
}
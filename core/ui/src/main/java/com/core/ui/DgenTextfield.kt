package com.core.ui

import android.annotation.SuppressLint
import android.text.Layout
import android.view.View
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.body2_fontSize
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun DgenTextfield(
    value: TextFieldValue = TextFieldValue(""),
    onValueChange: (TextFieldValue) -> Unit,
    keyboardtype: KeyboardType =  KeyboardType.Text,
    isAnyFieldFocused: MutableState<Boolean>,
    textfieldFocusManager: FocusManager? = null,
    onEditDone: () -> Unit,
    onDoubleTap: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 100,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = RoundedCornerShape(0.dp),
    backgroundColor: Color = dgenTurqoise,
    cursorColor: Color = dgenWhite,
    cursorWidth: Dp = 18.dp,
    cursorHeight: Dp = 32.dp,
    textStyle: TextStyle = TextStyle(
        fontFamily = PitagonsSans,
        color = dgenWhite,
        fontWeight = FontWeight.SemiBold,
        fontSize = body2_fontSize
    ),
    description: String = "",
    view: View,
    placeholder: @Composable() (() -> Unit)? = null,
    labelContent: @Composable() (() -> Unit)? = null,
) {


    var isFocused by remember { mutableStateOf(false) }

    val focusManager = textfieldFocusManager ?: LocalFocusManager.current


    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) backgroundColor else Color.Transparent,
        animationSpec = tween(durationMillis = 500),
        label = "backgroundColor" // Dauer der Animation in Millisekunden
    )

    val haptics = LocalHapticFeedback.current


    Column (
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .drawBehind {
                drawRect(
                    color = backgroundColor, // Farbe des Rechtecks
                    size = size, // Füllt den gesamten Platz aus
                    topLeft = Offset(0f, 0f), // Startposition
                    alpha = 0.2f
                )
            }
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTap()
                    },
                    onLongPress = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        focusManager.clearFocus()
                        focusManager.moveFocus(focusDirection = androidx.compose.ui.focus.FocusDirection.Enter) // Request focus here

                    }
                )
            }
            .onKeyEvent { // Ermöglicht zusätzliche Tastatureingaben
                if (it.nativeKeyEvent.keyCode == Key.Enter.nativeKeyCode) {
                    focusManager.clearFocus() // Fokus entfernen bei Enter
                    true
                } else {
                    false
                }
            }
        ,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start
    ){

        if (labelContent != null) {
            labelContent()
        }


        DgenBasicTextfield(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 32.dp)
                .onFocusChanged { focusState ->
                    if (isFocused && !focusState.isFocused) {
                        // Send Toast when losing focus
                        onEditDone()
                    }
                    isFocused = focusState.isFocused
                }
            ,
            textStyle = textStyle,
            enabled = enabled,
            readOnly = readOnly,
            minLines = minLines,
            maxLines = maxLines,
            keyboardtype = keyboardtype,
            interactionSource = interactionSource,
            cursorColor =  cursorColor,
            cursorWidth = cursorWidth,
            cursorHeight = cursorHeight,
            placeholder = placeholder,
            isAnyFieldFocused = isAnyFieldFocused
        )

    }

}

@SuppressLint("SuspiciousIndentation")
@Composable
fun DgenBasicTextfield(
    value: TextFieldValue = TextFieldValue(""),
    onValueChange: (TextFieldValue) -> Unit,
    keyboardtype: KeyboardType =  KeyboardType.Text,
    isAnyFieldFocused: MutableState<Boolean>,
    textfieldFocusManager: FocusManager? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    visualTransformation: VisualTransformation = VisualTransformation.None,
    maxLines: Int = 100,
    cursorColor: Color = dgenWhite,
    cursorWidth: Dp = 18.dp,
    cursorHeight: Dp = 32.dp,
    textStyle: TextStyle = TextStyle(
        fontFamily = PitagonsSans,
        color = dgenWhite,
        fontWeight = FontWeight.Normal,
        fontSize = body2_fontSize
    ),
    placeholder: @Composable() (() -> Unit)? = null,
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    var isFocused by remember { mutableStateOf(false) }
    val focusManager = textfieldFocusManager ?: LocalFocusManager.current

    val infiniteTransition = rememberInfiniteTransition()
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val scrollState = rememberScrollState()

    /*val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }*/

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (value.text.isEmpty()) {
            if (placeholder != null && !isFocused) {
                placeholder()
            }
        }



        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            modifier = modifier
                .fillMaxWidth()
                .drawBehind {
                    if (isFocused){
                        textLayoutResult
                            ?.takeIf { value.selection.collapsed }
                            ?.let { tlr ->
                                val rect = tlr.getCursorRect(value.selection.start)

                                // subtract scroll to bring into view
                                //val y = rect.top + (rect.height - 32.dp.toPx()) / 2f + 48.dp.toPx()
                                val y = ((rect.top + rect.bottom) / 2 ) - (cursorHeight.toPx() /2)
                                drawRect(
                                    color   = cursorColor.copy(alpha = if (isFocused) blinkAlpha else 0f),
                                    topLeft = Offset(rect.left, y),
                                    size    = Size(cursorWidth.toPx(), cursorHeight.toPx())
                                )
                            }
                        /*textLayoutResult?.let {
                            val cursorRect = it.getCursorRect(value.selection.start)
                            drawRect(
                                color = cursorColor,
                                topLeft = Offset(cursorRect.left, ((cursorRect.top + cursorRect.bottom) / 2 ) - (cursorHeight.toPx() /2)),
                                size = androidx.compose.ui.geometry.Size(cursorWidth.toPx(), cursorHeight.toPx()),
                                alpha = blinkAlpha
                            )
                        }*/
                    }

                }
                .onFocusChanged { isFocused = it.isFocused },
            visualTransformation = VisualTransformation.None, // Ensure no transformations
            cursorBrush = SolidColor(Color.Unspecified),
            onTextLayout = { layoutResult ->
                textLayoutResult = layoutResult

            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = keyboardtype
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    isFocused = true
                },
                onDone = {
                    focusManager.clearFocus() // Fokus entfernen, wenn Enter gedrückt wird
                    isFocused = false
                }
            )

        )



        /*BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            visualTransformation = visualTransformation,
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            cursorBrush = SolidColor(Color.Transparent),
            onTextLayout = { layoutResult ->
                textLayoutResult = layoutResult
            },
            singleLine = false,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = keyboardtype
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    isFocused = true
                },
                onDone = {
                    focusManager.clearFocus() // Fokus entfernen, wenn Enter gedrückt wird
                    isFocused = false
                }
            ),

        ) { innerTextField ->
            // 4) draw text + custom cursor, offset by scrollState.value
            Box(
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        textLayoutResult
                            ?.takeIf { value.selection.collapsed }
                            ?.let { tlr ->
                                val rect = tlr.getCursorRect(value.selection.start)

                                // subtract scroll to bring into view
                                val y = rect.top
                                - scrollState.value
                                + (rect.height - cursorHeight.toPx()) / 2f
                                + 18.dp.toPx()

                                drawRect(
                                    color   = cursorColor.copy(alpha = if (isFocused) blinkAlpha else 0f),
                                    topLeft = Offset(rect.left, y),
                                    size    = Size(cursorWidth.toPx(), cursorHeight.toPx())
                                )
                            }
                    }
            ) {
                innerTextField()
            }
        }*/



        /*BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .drawBehind {
                    if (isFocused){
                        textLayoutResult
                            ?.takeIf { value.selection.collapsed }
                            ?.let { tlr ->
                                val rect = tlr.getCursorRect(value.selection.start)

                                // subtract scroll to bring into view
                                //val y = rect.top + (rect.height - 32.dp.toPx()) / 2f + 48.dp.toPx()
                                val y = ((rect.top + rect.bottom) / 2 ) - (cursorHeight.toPx() /2)
                                drawRect(
                                    color   = cursorColor.copy(alpha = if (isFocused) blinkAlpha else 0f),
                                    topLeft = Offset(rect.left, y),
                                    size    = Size(18.dp.toPx(), 32.dp.toPx())
                                )
                            }
                        /*textLayoutResult?.let {
                            val cursorRect = it.getCursorRect(value.selection.start)
                            drawRect(
                                color = cursorColor,
                                topLeft = Offset(cursorRect.left, ((cursorRect.top + cursorRect.bottom) / 2 ) - (cursorHeight.toPx() /2)),
                                size = androidx.compose.ui.geometry.Size(cursorWidth.toPx(), cursorHeight.toPx()),
                                alpha = blinkAlpha
                            )
                        }*/
                    }

                }
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    isAnyFieldFocused.value = focusState.isFocused
                }
            ,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = keyboardtype
            ),

            keyboardActions = KeyboardActions(
                onGo = {
                    isFocused = true
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
            onTextLayout = { textLayoutResult = it }
        )*/



    }

}
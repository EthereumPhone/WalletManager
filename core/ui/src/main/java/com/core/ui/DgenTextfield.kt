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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.unit.toSize
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.body2_fontSize
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
                
                drawLine(
                    color = backgroundColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2f
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

    val density = LocalDensity.current
    val lineHeight = with(density) { textStyle.fontSize.toDp() }
    val maxHeight = if (maxLines != Int.MAX_VALUE) {
        lineHeight * maxLines + 32.dp  // Mehr Padding für bessere Darstellung
    } else {
        Dp.Unspecified
    }

    // Auto-scroll to cursor position when text changes
    LaunchedEffect(value.selection.start, textLayoutResult) {
        textLayoutResult?.let { layoutResult ->
            if (isFocused && value.selection.collapsed) {
                val cursorRect = layoutResult.getCursorRect(value.selection.start)
                val lineHeight = with(density) { textStyle.fontSize.toPx() }
                val maxVisibleHeight = lineHeight * maxLines
                
                // Calculate target scroll position to keep cursor visible
                val cursorTop = cursorRect.top
                val cursorBottom = cursorRect.bottom
                val currentScroll = scrollState.value.toFloat()
                
                // Ensure the cursor line is always visible with better logic
                val targetScroll = when {
                    cursorTop < currentScroll -> {
                        // Cursor is above visible area - scroll up to show it
                        cursorTop.coerceAtLeast(0f)
                    }
                    cursorBottom > currentScroll + maxVisibleHeight -> {
                        // Cursor is below visible area - scroll down to show it
                        (cursorBottom - maxVisibleHeight).coerceAtLeast(0f)
                    }
                    else -> {
                        // Cursor is visible, no need to scroll
                        null
                    }
                }
                
                targetScroll?.let {
                    scrollState.animateScrollTo(it.toInt())
                }
            }
        }
    }

    Box(
        modifier = modifier
            .then(
                if (maxHeight != Dp.Unspecified) {
                    Modifier.heightIn(max = maxHeight)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.TopStart
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
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .drawBehind {
                    if (isFocused){
                        textLayoutResult
                            ?.takeIf { value.selection.collapsed }
                            ?.let { tlr ->
                                val rect = tlr.getCursorRect(value.selection.start)
                                
                                // Calculate the line height and max visible lines
                                val lineHeight = with(density) { textStyle.fontSize.toPx() }
                                
                                // Calculate which line the cursor is on
                                val currentLine = (rect.top / lineHeight).toInt()
                                
                                // Determine the Y position for the cursor
                                val cursorY = if (maxLines != Int.MAX_VALUE && currentLine >= maxLines - 1) {
                                    // Fix cursor on the last visible line when max lines is reached
                                    (maxLines - 1) * lineHeight
                                } else {
                                    // Use normal cursor position for lines within max
                                    rect.top
                                }
                                
                                val adjustedY = cursorY - scrollState.value
                                
                                // Use the actual line height from the text layout for cursor height
                                val actualCursorHeight = rect.height
                                
                                // Draw cursor with fixed or normal position
                                drawRect(
                                    color = cursorColor.copy(alpha = blinkAlpha),
                                    topLeft = Offset(rect.left, adjustedY),
                                    size = Size(cursorWidth.toPx(), actualCursorHeight)
                                )
                            }
                    }
                }
                .onFocusChanged { 
                    isFocused = it.isFocused 
                    isAnyFieldFocused.value = it.isFocused
                },
            visualTransformation = visualTransformation,
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
                    focusManager.clearFocus()
                    isFocused = false
                }
            ),
            singleLine = false,
            minLines = minLines,
            maxLines = maxLines,
            enabled = enabled,
            readOnly = readOnly,
            interactionSource = interactionSource
        )
    }

}
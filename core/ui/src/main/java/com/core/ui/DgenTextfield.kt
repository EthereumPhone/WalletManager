package com.core.ui

import android.annotation.SuppressLint
import android.text.Layout
import android.util.Log
import android.view.View
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.horizontalScroll
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
    autoCorrectEnabled: Boolean = true,
    isAnyFieldFocused: MutableState<Boolean>,
    textfieldFocusManager: FocusManager? = null,
    onEditDone: () -> Unit,
    onDoubleTap: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 100,
    maxLength: Int = Int.MAX_VALUE,
    scrollHorizontally: Boolean = true,
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


    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isFocused) backgroundColor else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    val haptics = LocalHapticFeedback.current


    Column (
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .drawBehind {
                drawRect(
                    color = animatedBackgroundColor,
                    size = size,
                    topLeft = Offset(0f, 0f),
                    alpha = 0.2f
                )
//                drawLine(
//                    color = animatedBackgroundColor,
//                    start = Offset(0f, 0f),
//                    end = Offset(0f, size.height),
//                    strokeWidth = 2f
//                )
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
            onValueChange = { newValue ->
                // Apply max length restriction
                if (newValue.text.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            modifier = modifier
                .animateContentSize()
                .fillMaxWidth()
                .heightIn(max=600.dp)
                .padding(end = 32.dp),
            textStyle = textStyle,
            enabled = enabled,
            readOnly = readOnly,
            minLines = minLines,
            maxLines = maxLines,
            maxLength = maxLength,
            scrollHorizontally = scrollHorizontally,
            autoCorrectEnabled = autoCorrectEnabled,
            keyboardtype = keyboardtype,
            interactionSource = interactionSource,
            cursorColor =  cursorColor,
            cursorWidth = cursorWidth,
            cursorHeight = cursorHeight,
            placeholder = placeholder,
            isAnyFieldFocused = isAnyFieldFocused,
            onFocusChanged = { focusState ->
                if (isFocused && !focusState) {
                    // Send Toast when losing focus
                    onEditDone()
                }
                Log.d("DEBUG","isFocused: $isFocused - focusState: $focusState")
                isFocused = focusState
                Log.d("DEBUG","After- isFocused: $isFocused - focusState: $focusState")
            }
        )

    }

}

@SuppressLint("SuspiciousIndentation")
@Composable
fun DgenBasicTextfield(
    value: TextFieldValue = TextFieldValue(""),
    onValueChange: (TextFieldValue) -> Unit,
    keyboardtype: KeyboardType =  KeyboardType.Text,
    autoCorrectEnabled: Boolean = true,
    isAnyFieldFocused: MutableState<Boolean>,
    textfieldFocusManager: FocusManager? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    visualTransformation: VisualTransformation = VisualTransformation.None,
    maxLines: Int = 100,
    maxLength: Int = Int.MAX_VALUE,
    scrollHorizontally: Boolean = true,
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
    onFocusChanged: ((Boolean) -> Unit)? = null,
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

    // Auto-scroll to cursor position when text changes
    LaunchedEffect(value.selection.start, textLayoutResult, value.text) {
        textLayoutResult?.let { layoutResult ->
            if (isFocused) {
                val cursorRect = layoutResult.getCursorRect(value.selection.start)
                
                if (scrollHorizontally) {
                    // Horizontal scrolling logic
                    val cursorLeft = cursorRect.left
                    val cursorRight = cursorRect.right
                    val currentScroll = scrollState.value.toFloat()
                    val viewportWidth = scrollState.viewportSize.toFloat()
                    
                    val targetScroll = when {
                        viewportWidth <= 0 -> null
                        cursorRight > currentScroll + viewportWidth - 20f -> {
                            (cursorRight - viewportWidth + 20f).coerceAtLeast(0f)
                        }
                        cursorLeft < currentScroll + 20f -> {
                            (cursorLeft - 20f).coerceAtLeast(0f)
                        }
                        else -> null
                    }
                    
                    targetScroll?.let {
                        scrollState.scrollTo(it.toInt())
                    }
                } else {
                    // Vertical scrolling logic
                    val lineHeight = with(density) { textStyle.fontSize.toPx() }
                    val maxVisibleHeight = lineHeight * maxLines
                    val cursorTop = cursorRect.top
                    val cursorBottom = cursorRect.bottom
                    val currentScroll = scrollState.value.toFloat()
                    
                    val targetScroll = when {
                        cursorTop < currentScroll -> {
                            cursorTop.coerceAtLeast(0f)
                        }
                        cursorBottom > currentScroll + maxVisibleHeight -> {
                            (cursorBottom - maxVisibleHeight).coerceAtLeast(0f)
                        }
                        else -> null
                    }
                    
                    targetScroll?.let {
                        scrollState.animateScrollTo(it.toInt())
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .then(
                if (!scrollHorizontally && maxLines != Int.MAX_VALUE) {
                    val lineHeight = with(density) { textStyle.fontSize.toDp() }
                    Modifier.heightIn(max = lineHeight * maxLines + 16.dp)
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
            onValueChange = { newValue ->
                // Apply max length restriction
                if (newValue.text.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            textStyle = textStyle,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (scrollHorizontally) {
                        Modifier.horizontalScroll(scrollState)
                    } else {
                        Modifier.verticalScroll(scrollState)
                    }
                )
                .drawBehind {
                    if (isFocused){
                        textLayoutResult
                            ?.takeIf { value.selection.collapsed }
                            ?.let { tlr ->
                                val rect = tlr.getCursorRect(value.selection.start)
                                
                                if (scrollHorizontally) {
                                    // Calculate cursor position adjusted for horizontal scroll
                                    val cursorX = rect.left - scrollState.value
                                    val cursorY = rect.top
                                    
                                    // Use the actual line height from the text layout for cursor height
                                    val actualCursorHeight = rect.height
                                    
                                    // Draw cursor at the correct horizontal position
                                    drawRect(
                                        color = cursorColor.copy(alpha = blinkAlpha),
                                        topLeft = Offset(cursorX, cursorY),
                                        size = Size(cursorWidth.toPx(), actualCursorHeight)
                                    )
                                } else {
                                    // Calculate cursor position adjusted for vertical scroll
                                    val lineHeight = with(density) { textStyle.fontSize.toPx() }
                                    val currentLine = (rect.top / lineHeight).toInt()
                                    
                                    // Determine if cursor should be fixed at bottom
                                    val cursorY = if (maxLines != Int.MAX_VALUE && currentLine >= maxLines - 1) {
                                        // Fix cursor on the last visible line when max lines is reached
                                        val fixedY = (maxLines - 1) * lineHeight
                                        fixedY - scrollState.value
                                    } else {
                                        // Use normal cursor position for lines within max
                                        rect.top - scrollState.value
                                    }
                                    
                                    val cursorX = rect.left
                                    val actualCursorHeight = rect.height
                                    
                                    // Draw cursor at the correct vertical position
                                    drawRect(
                                        color = cursorColor.copy(alpha = blinkAlpha),
                                        topLeft = Offset(cursorX, cursorY),
                                        size = Size(cursorWidth.toPx(), actualCursorHeight)
                                    )
                                }
                            }
                    }
                }
                .onFocusChanged { 
                    isFocused = it.isFocused 
                    isAnyFieldFocused.value = it.isFocused
                    onFocusChanged?.invoke(it.isFocused)
                },
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(Color.Unspecified),
            onTextLayout = { layoutResult ->
                textLayoutResult = layoutResult
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                autoCorrectEnabled = autoCorrectEnabled,
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
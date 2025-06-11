package com.core.ui

import android.util.Log
import android.view.View
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.ui.util.dgenTurqoise
import com.core.ui.util.dgenWhite
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalView
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenOcean
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import android.view.ViewTreeObserver
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.CircleShape
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.body2_fontSize

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun DgenButtonTextfield(
    value: TextFieldValue = TextFieldValue(""),
    onValueChange: (TextFieldValue) -> Unit,
    keyboardtype: KeyboardType = KeyboardType.Number,
    autoCorrectEnabled: Boolean = true,
    isAnyFieldFocused: MutableState<Boolean>,
    textfieldFocusManager: FocusManager? = null,
    onEditDone: () -> Unit,
    onDoubleTap: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1,
    maxLength: Int = 14,
    scrollHorizontally: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = RoundedCornerShape(8.dp),
    backgroundColor: Color = dgenOcean,
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
    showButtons: Boolean = true,
    button1Text: String = "25%",
    button2Text: String = "MAX",
    button1Value: String = "",
    button2Value: String = "",
    onButton1Click: () -> Unit = {},
    onButton2Click: () -> Unit = {},
) {

    var isFocused by remember { mutableStateOf(false) }

    val focusManager = textfieldFocusManager ?: LocalFocusManager.current

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isFocused) backgroundColor else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    val animatedBackgroundOpacity by animateFloatAsState(
        targetValue = if (isFocused) 0.33f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    val haptics = LocalHapticFeedback.current

    val isButton1Active = value.text == button1Value && button1Value.isNotEmpty()
    val isButton2Active = value.text == button2Value && button2Value.isNotEmpty()

    // Click-Outside-Unfocus Handler
    DisposableEffect(view, isFocused) {
        val listener = ViewTreeObserver.OnGlobalFocusChangeListener { _, _ ->
            if (isFocused && !view.hasFocus()) {
                focusManager.clearFocus()
            }
        }
        
        if (isFocused) {
            view.viewTreeObserver.addOnGlobalFocusChangeListener(listener)
        }
        
        onDispose {
            view.viewTreeObserver.removeOnGlobalFocusChangeListener(listener)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .drawBehind {
                drawRect(
                    color = Color(0xFF536F79),
                    size = size,
                    topLeft = Offset(0f, 0f),
                    alpha = animatedBackgroundOpacity
                )
            }
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTap()
                    },
                    onLongPress = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        focusManager.clearFocus()
                        focusManager.moveFocus(focusDirection = androidx.compose.ui.focus.FocusDirection.Enter)
                    }
                )
            }
            .onKeyEvent {
                if (it.nativeKeyEvent.keyCode == Key.Enter.nativeKeyCode) {
                    focusManager.clearFocus()
                    true
                } else {
                    false
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column (
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start
        ){

            if (labelContent != null) {
                labelContent()
            }

            DgenBasicTextfield(
                value = value,
                onValueChange = { newValue ->
                    // Entferne Leerzeichen und Bindestriche
                    var filteredText = newValue.text.replace(" ", "").replace("-", "")
                    
                    // Begrenze auf maximal zwei Punkte
                    val dotCount = filteredText.count { it == '.' }
                    if (dotCount > 2) {
                        // Entferne überschüssige Punkte (behalte nur die ersten zwei)
                        var dotsFound = 0
                        filteredText = filteredText.filter { char ->
                            if (char == '.') {
                                dotsFound++
                                dotsFound <= 2
                            } else {
                                true
                            }
                        }
                    }
                    
                    if (filteredText.length <= maxLength) {
                        onValueChange(newValue.copy(text = filteredText))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
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
                cursorColor = cursorColor,
                cursorWidth = cursorWidth,
                cursorHeight = cursorHeight,
                placeholder = placeholder,
                isAnyFieldFocused = isAnyFieldFocused,
                onFocusChanged = { focusState ->
                    if (isFocused && !focusState) {
                        onEditDone()
                    }
                    Log.d("DEBUG","isFocused: $isFocused - focusState: $focusState")
                    isFocused = focusState
                    Log.d("DEBUG","After- isFocused: $isFocused - focusState: $focusState")
                }
            )
        }

        if (showButtons) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isButton1Active) dgenTurqoise else Color.Transparent,
                            shape = CircleShape
                        )
                        .border(
                            BorderStroke(1.dp, dgenTurqoise),
                            CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onButton1Click()
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = PitagonsSans,
                                    color = if (isButton1Active) dgenOcean else dgenTurqoise,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                )
                            ) {
                                append("\$")
                            }
                            append(button1Text)
                        },
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = if (isButton1Active) dgenOcean else dgenTurqoise,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = if (isButton2Active) dgenTurqoise else Color.Transparent,
                            shape = CircleShape
                        )
                        .border(
                            BorderStroke(1.dp, dgenTurqoise),
                            CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onButton2Click()
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = PitagonsSans,
                                    color = if (isButton2Active) dgenOcean else dgenTurqoise,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                )
                            ) {
                                append("\$")
                            }
                            append(button2Text)
                        },
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = if (isButton2Active) dgenOcean else dgenTurqoise,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050505)
@Composable
fun DgenButtonTextfieldPreview() {
    val textFieldValue1 = remember { mutableStateOf(TextFieldValue("25.00")) }
    val textFieldValue2 = remember { mutableStateOf(TextFieldValue("12345678901234")) } // 14 Zeichen für Overflow-Test
    val textFieldValue3 = remember { mutableStateOf(TextFieldValue("100.00")) }
    val isAnyFieldFocused = remember { mutableStateOf(false) }
    val view = LocalView.current
    
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack),
        color = dgenBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Preview mit aktivem Button 1 (25%)
            DgenButtonTextfield(
                value = textFieldValue1.value,
                onValueChange = { textFieldValue1.value = it },
                isAnyFieldFocused = isAnyFieldFocused,
                onEditDone = { },
                view = view,
                labelContent = {
                    Text(
                        text = "Aktiver 25% Button",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenTurqoise,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                },
                placeholder = {
                    Text(
                        text = "0.00",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenWhite.copy(0.5f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = body2_fontSize
                        )
                    )
                },
                button1Text = "25%",
                button2Text = "MAX",
                button1Value = "25.00",
                button2Value = "100.00",
                onButton1Click = {
                    textFieldValue1.value = TextFieldValue("25.00")
                },
                onButton2Click = {
                    textFieldValue1.value = TextFieldValue("100.00")
                }
            )
            
            // Preview mit Text-Overflow und Fade-Effekt
            DgenButtonTextfield(
                value = textFieldValue2.value,
                onValueChange = { textFieldValue2.value = it },
                isAnyFieldFocused = remember { mutableStateOf(true) }, // Fokussiert für Fade-Effekt
                onEditDone = { },
                view = view,
                labelContent = {
                    Text(
                        text = "Text-Overflow mit Fade (14 Zeichen Limit)",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenTurqoise,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                },
                button1Text = "50%",
                button2Text = "ALL",
                button1Value = "50.00",
                button2Value = "200.00"
            )
            
            // Preview mit aktivem Button 2 (MAX)
            DgenButtonTextfield(
                value = textFieldValue3.value,
                onValueChange = { textFieldValue3.value = it },
                isAnyFieldFocused = remember { mutableStateOf(false) },
                onEditDone = { },
                view = view,
                labelContent = {
                    Text(
                        text = "Aktiver MAX Button (persistente Buttons)",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenTurqoise,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                },
                placeholder = {
                    Text(
                        text = "Nur Zahlen eingeben...",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenWhite.copy(0.5f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = body2_fontSize
                        )
                    )
                },
                button1Text = "25%",
                button2Text = "MAX", 
                button1Value = "25.00",
                button2Value = "100.00",
                onButton1Click = {
                    textFieldValue3.value = TextFieldValue("25.00")
                },
                onButton2Click = {
                    textFieldValue3.value = TextFieldValue("100.00")
                }
            )
        }
    }
}

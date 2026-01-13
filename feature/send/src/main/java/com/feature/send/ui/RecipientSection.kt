package com.feature.send.ui

import android.view.ViewTreeObserver
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.data.model.dto.Contact
import com.core.ui.DgenBasicTextfield
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenGreen
import com.core.ui.util.dgenOcean
import com.core.ui.util.dgenOrche
import com.core.ui.util.dgenRed
import com.core.ui.util.dgenWhite
import com.core.ui.util.extraLargeEnterDuration
import com.core.ui.util.extraLargeExitDuration
import com.core.ui.util.ghostOpacity
import com.core.ui.util.label_fontSize
import com.core.ui.util.pulseOpacity
import com.core.ui.util.smallDuration
import android.util.Log
import com.feature.send.RecipientUiState

/**
 * Generic DgenTextfield with animated fading background.
 * Independent of RecipientUiState or Contact - can be used anywhere.
 *
 * @param value The current text field value
 * @param onValueChange Called when the text changes
 * @param showTextField Whether to show the text field (false to show custom content instead)
 * @param headerTitle Simple string title for the header (used if headerContent is null)
 * @param headerColor Color for the header title
 * @param headerContent Custom composable for the header (overrides headerTitle if provided)
 * @param customContent Content to show when showTextField is false
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DgenTextfield(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    showTextField: Boolean = true,
    headerTitle: String = "",
    headerColor: Color = SystemColorManager.primaryColor,
    headerContent: @Composable (() -> Unit)? = null,
    customContent: @Composable (() -> Unit)? = null,
    onEditDone: () -> Unit = {},
    enabled: Boolean = true,
    readOnly: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 4,
    maxLength: Int = 43,
    scrollHorizontally: Boolean = false,
    autoCorrectEnabled: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cursorColor: Color = SystemColorManager.primaryColor,
    cursorWidth: Dp = 16.dp,
    cursorHeight: Dp = 16.dp,
    activeColor: Color = SystemColorManager.primaryColor,
    textStyle: TextStyle = TextStyle(
        fontFamily = PitagonsSans,
        color = dgenWhite,
        fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp
    ),
    backgroundOverflowHorizontal: Dp = 12.dp,
    backgroundOverflowVertical: Dp = 8.dp,
    placeholder: @Composable (() -> Unit)? = null,
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val haptics = LocalHapticFeedback.current

    var isFocused by remember { mutableStateOf(false) }
    val isAnyFieldFocused = remember { mutableStateOf(false) }

    // Animated background opacity for fading effect
    val animatedBackgroundOpacity by animateFloatAsState(
        targetValue = if (isFocused) ghostOpacity else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundOpacity"
    )

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

    Column(
        modifier = modifier

            .drawBehind {
                val overflowH = backgroundOverflowHorizontal.toPx()
                val overflowV = backgroundOverflowVertical.toPx()
                drawRoundRect(
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    color = activeColor,
                    size = Size(size.width + overflowH * 2, size.height + overflowV * 2),
                    topLeft = Offset(-overflowH, -overflowV),
                    alpha = animatedBackgroundOpacity
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
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
            }
    ) {
        // Header section - use custom content if provided, otherwise use title
        if (headerContent != null) {
            headerContent()
        } else if (headerTitle.isNotEmpty()) {
            Text(
                text = headerTitle,
                color = headerColor,
                style = TextStyle(
                    fontFamily = SpaceMono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = label_fontSize,
                    lineHeight = label_fontSize,
                    letterSpacing = 1.sp,
                    textDecoration = TextDecoration.None,
                    textAlign = TextAlign.Left
                )
            )
        }

        AnimatedContent(
            targetState = showTextField,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(smallDuration)
                ) togetherWith fadeOut(animationSpec = tween(smallDuration))
            },
            label = "textfieldAnimation"
        ) { targetStatus ->
            // Show either the text field or custom content
            if (targetStatus) {
                DgenBasicTextfield(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth().padding(top=8.dp),
                    textStyle = textStyle,
                    enabled = enabled,
                    readOnly = readOnly,
                    minLines = minLines,
                    maxLines = maxLines,
                    maxLength = maxLength,
                    scrollHorizontally = scrollHorizontally,
                    autoCorrectEnabled = autoCorrectEnabled,
                    keyboardtype = keyboardType,
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
                        isFocused = focusState
                    }
                )
            } else {
                // Show custom content when text field is hidden
                customContent?.invoke()
            }

        }
        
    }
}

/**
 * RecipientSection - Specialized text field for entering recipient addresses.
 * Uses RecipientUiState for ENS resolution status and supports contact selection.
 */
@Composable
fun RecipientSection(
    recipientUiState: RecipientUiState,
    selectedContact: Contact? = null,
    hasContactsWithEth: Boolean = false,
    onContentChanged: (String) -> Unit,
    onContactIconClick: () -> Unit = {},
    onClearContact: () -> Unit = {},
    shouldDismissKeyboard: Boolean = false,
    onKeyboardDismissed: () -> Unit = {}
) {
    // #region agent log
    Log.d("DEBUG_AGENT", "RecipientSection:entry - hasContactsWithEth=$hasContactsWithEth, selectedContact=${selectedContact?.name ?: "null"}, shouldShowContactButton=${hasContactsWithEth && selectedContact == null}, hypothesisId=A,B")
    // #endregion

    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor
    val focusManager = LocalFocusManager.current

    val (headerText, headerColor) = when {
        selectedContact != null -> "SENDING TO CONTACT" to primaryColor
        recipientUiState.ensError.isNotEmpty() -> "ENS ERROR" to dgenRed
        recipientUiState.isResolving -> "RESOLVING ENS..." to dgenOrche
        recipientUiState.recipientAddress.endsWith(".eth") -> "ENS RESOLVED" to dgenGreen
        else -> "TARGET ADDRESS" to primaryColor
    }

    // Keep cursor position
    var textFieldValue by remember { mutableStateOf(TextFieldValue(recipientUiState.recipientAddress)) }
    LaunchedEffect(recipientUiState) {
        if (textFieldValue.text != recipientUiState.recipientAddress) {
            textFieldValue = TextFieldValue(
                text = recipientUiState.recipientAddress,
                selection = TextRange(recipientUiState.recipientAddress.length)
            )
        }
    }

    // Clear keyboard when ViewModel indicates it should be dismissed
    LaunchedEffect(shouldDismissKeyboard) {
        if (shouldDismissKeyboard) {
            focusManager.clearFocus()
            onKeyboardDismissed()
        }
    }

    DgenTextfield(
        value = textFieldValue,
        onValueChange = { new ->
            textFieldValue = new
            onContentChanged(new.text)
        },
        showTextField = selectedContact == null,
        headerContent = {
            // Header with contacts icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerText,
                    color = headerColor,
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = label_fontSize,
                        lineHeight = label_fontSize,
                        letterSpacing = 1.sp,
                        textDecoration = TextDecoration.None,
                        textAlign = TextAlign.Left
                    )
                )

                Spacer(modifier = Modifier.width(25.dp))

                // #region agent log
                Log.d("DEBUG_AGENT", "RecipientSection:buttonCheck - hasContactsWithEth=$hasContactsWithEth, selectedContactIsNull=${selectedContact == null}, willShowButton=${hasContactsWithEth && selectedContact == null}, hypothesisId=A")
                // #endregion

                // Only show contacts icon if we have contacts with ETH addresses and no contact is selected
                if (hasContactsWithEth && selectedContact == null) {
                    IconButton(
                        onClick = onContactIconClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Contacts,
                            contentDescription = "Select contact",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        customContent = {
            // Show contact UI if a contact is selected
            if (selectedContact != null) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.fillMaxWidth().height(4.dp))
                    SelectedContact(
                        contact = selectedContact,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        onClear = onClearContact
                    )
                }
            }
        },
        placeholder = {
            Text(
                text = "Address",
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenWhite.copy(pulseOpacity),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                ),
            )
        },
        keyboardType = KeyboardType.Text,
        cursorColor = primaryColor,
        activeColor = primaryColor
    )
}

@Preview
@Composable
fun PreviewRecipientSection() {
    RecipientSection(
        recipientUiState = RecipientUiState(),
        selectedContact = null,
        hasContactsWithEth = false,
        onContentChanged = {},
        onContactIconClick = {},
        onClearContact = {}
    )
}

@Preview(name = "RecipientSection - Selected Contact")
@Composable
fun PreviewRecipientSectionSelected() {
    val sampleContact = Contact(
        id = "1",
        name = "Alice Wonderland",
        ens = "alice.eth",
        address = "0x1234567890abcdef1234567890abcdef12345678",
        image = ""
    )
    RecipientSection(
        recipientUiState = RecipientUiState(),
        selectedContact = sampleContact,
        hasContactsWithEth = true,
        onContentChanged = {},
        onContactIconClick = {},
        onClearContact = {}
    )
}

@Preview(name = "DgenTextfield - Generic")
@Composable
fun PreviewDgenTextfield() {
    var textValue by remember { mutableStateOf(TextFieldValue("")) }
    DgenTextfield(
        value = textValue,
        onValueChange = { textValue = it },
        headerTitle = "AMOUNT",
        placeholder = {
            Text(
                text = "0.00",
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenWhite.copy(pulseOpacity),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                ),
            )
        }
    )
}

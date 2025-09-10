package com.feature.send.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.ui.SimpleDgenTextfield
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenGreen
import com.core.ui.util.dgenOrche
import com.core.ui.util.dgenRed
import com.core.ui.util.dgenWhite
import com.core.ui.util.label_fontSize
import com.core.ui.util.pulseOpacity
import com.feature.send.RecipientUiState

@Composable
fun RecipientSection(
    recipientUiState: RecipientUiState,
    onContentChanged: (String) -> Unit
) {
    val primaryColor = SystemColorManager.primaryColor
    val view = LocalView.current



    val (headerText, headerColor) = when {
        recipientUiState.ensError.isNotEmpty() -> "ENS ERROR" to dgenRed
        recipientUiState.isResolving -> "RESOLVING ENS..." to dgenOrche
        recipientUiState.recipientAddress.endsWith(".eth") -> "ENS RESOLVED" to dgenGreen
        else -> "TARGET ADDRESS" to primaryColor
    }

    Column(Modifier.offset(x = (-12).dp)) {
        // trick to keep cursor position
        var textFieldValue by remember { mutableStateOf(TextFieldValue(recipientUiState.recipientAddress)) }
        LaunchedEffect(recipientUiState) {
            if (textFieldValue.text != recipientUiState.recipientAddress) {
                // Move cursor to end when text changes (e.g., after ENS resolution)
                textFieldValue = TextFieldValue(
                    text = recipientUiState.recipientAddress,
                    selection = TextRange(recipientUiState.recipientAddress.length)
                )
            }
        }

        SimpleDgenTextfield(
            value = textFieldValue,
            maxLines = 4,
            maxLength = 43,
            scrollHorizontally = false,
            autoCorrectEnabled = false,
            onValueChange = { new ->
                textFieldValue = new
                onContentChanged(new.text)
            },
            textStyle = TextStyle(
                fontFamily = PitagonsSans,
                color = dgenWhite,
                fontWeight = FontWeight. SemiBold,
                fontSize = 25.sp
            ),
            activeColor = primaryColor,
            placeholder = {
                Text(
                    modifier = Modifier,
                    text = "Address",
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenWhite.copy(pulseOpacity),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    ),
                )
            },
            cursorColor = primaryColor,
            keyboardtype =  KeyboardType.Text,
            cursorWidth = 16.dp,
            cursorHeight= 16.dp,
            isAnyFieldFocused= remember { mutableStateOf(false) },
            onEditDone = {},
            view = view
        ) {
            // header
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
        }
    }
}

@Preview
@Composable
fun PreviewRecipientSection() {


    RecipientSection(
        recipientUiState = RecipientUiState(),
        {}
    )
}
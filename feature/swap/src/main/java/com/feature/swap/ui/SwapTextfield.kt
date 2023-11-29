package com.feature.swap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun SwapTextField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: String="0",
    //placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    isError: Boolean = false,
    color: Color = Color.White,
    size: Int,
    maxChar: Int = 42,
    sizeCut: Int = 2,

    ) {

    var fontSize by remember { mutableStateOf(size.sp) }

//    val textStyling = TextStyle(
//        color = Color.White,
//        fontSize = 20.sp,
//        fontWeight = FontWeight.SemiBold
//    )
//    else Color(0xFF24303D)

    BasicTextField(
        value = value,
        onValueChange = {
            if(it.length < maxChar){
                onChange(it)
            }
            fontSize = size.sp

        },
        singleLine = true,
        minLines = 1,
        maxLines = 2,
        textStyle = LocalTextStyle.current.copy(
            color = color,
            fontSize = calculateFontSize(value.length,size,sizeCut),
            fontWeight = FontWeight.SemiBold
        ),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(Color.White),
        modifier = modifier
            .clip(RoundedCornerShape(10))
            .background(Color.Transparent)
            .height(IntrinsicSize.Min)
            .padding(
                horizontal = 0.dp,
                vertical = 8.dp
            ),

        )
    { innerTextField ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                if(value == "") {
                    Text(
                        text = placeholder,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Medium,
                        fontSize = size.sp,
                        color = Color(0xFF9FA2A5),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                innerTextField()
            }

            Spacer(Modifier.width(10.dp))
            if (trailingIcon != null) {
                trailingIcon()
            }
        }

    }



}

fun calculateFontSize(length: Int, defaultSize: Int, maxChar: Int ): TextUnit {
    val defaultFontSize = defaultSize.sp
    val maxChars = maxChar
    val scaleFactor = 0.8

    var adjustedSize = if (length > maxChars) {
        val scaledSize = (defaultFontSize * scaleFactor).value
        val minValue = 12f // Minimum font size
        val result = max(scaledSize, minValue)
        result.sp
    } else {
        defaultFontSize
    }

    if (length > maxChars*2) {
        val scaledSize = (adjustedSize * scaleFactor).value
        val minValue = 12f // Minimum font size
        val result = max(scaledSize, minValue)
        adjustedSize = result.sp
        //Log.e("Length: ", "${ adjustedSize }")
    }

    if (length > maxChars*3) {
        val scaledSize = (adjustedSize * scaleFactor).value
        val minValue = 12f // Minimum font size
        val result = max(scaledSize, minValue)
        adjustedSize = result.sp
        //Log.e("Length: ", "${ adjustedSize }")
    }

//    if (length > (maxChars*2) - 15) {
//        val scaledSize = (adjustedSize * scaleFactor).value
//        val minValue = 12f // Minimum font size
//        val result = max(scaledSize, minValue)
//        adjustedSize = result.sp
//    }
    return adjustedSize
}

@Preview
@Composable
fun PreviewWmTextField() {
    var text by remember { mutableStateOf("") }

    SwapTextField(
        value = text,
        onChange = { text = it },
        placeholder = "0",
        trailingIcon = {
            //TokenAssetIcon()
//            Button(
//                onClick = {  },
//                contentPadding = PaddingValues(horizontal = 12.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF1E2730),
//                    contentColor = Color.White
//                ),
//                shape = CircleShape,
//                content = {
//                    Text("Select Token")
//                }
//            )
//            IconButton(
//                onClick = {  },
//
//                ) {
//
//                Icon(
//                    imageVector = Icons.Rounded.QrCode,
//                    contentDescription = "Address by QR",
//                    tint = Color.White ,
//                    modifier = Modifier.size(32.dp)
//                )
//            }
        },
        size = 64,
        maxChar = 9,
        sizeCut = 6,

    )
}

@Preview
@Composable
fun previewErrorWmTextFiled() {
    var text by remember { mutableStateOf("bii b") }

    SwapTextField(
        value = text,
        onChange = { text = it },
//        leadingIcon = {
//            //TokenAssetIcon()
//        },
        isError = text.contains("b"),
        size = 64
    )
}
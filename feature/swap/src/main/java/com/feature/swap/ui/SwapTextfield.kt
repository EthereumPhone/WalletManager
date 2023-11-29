package com.feature.swap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SwapTextField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: String="Placeholder",
    //placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    isError: Boolean = false
    ) {

    val textStyling = TextStyle(
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    )
    val background = if(isError) Color(0xFFC63B3B)
    else Color(0xFF24303D)

    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = textStyling,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(Color.White),
        modifier = modifier
            .clip(RoundedCornerShape(10))
            .background(background)
            .height(64.dp)
            .padding(
                horizontal = 16.dp,
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
                        color = Color(0xFF9FA2A5),
                        fontSize = 18.sp,
                        fontWeight= FontWeight.Normal,
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

@Preview
@Composable
fun PreviewWmTextField() {
    var text by remember { mutableStateOf("bii b") }

    SwapTextField(
        value = text,
        onChange = { text = it },
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
        }
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
        isError = text.contains("b")
    )
}
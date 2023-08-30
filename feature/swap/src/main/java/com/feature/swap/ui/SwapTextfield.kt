package com.feature.swap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapTextField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: String="Placeholder",
    //placeholder: @Composable (() -> Unit)? = null,
    focusRequester: FocusRequester,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean
    ) {

    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold

        ),
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(Color.White),
        modifier = modifier
            .clip(RoundedCornerShape(10))
            .background(Color(0xFF24303D))
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
                modifier = Modifier.weight(1f).focusRequester(focusRequester)
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

//    SwapTextField(
//        value = text,
//        onChange = { text = it },
//        leadingIcon = {
//            //TokenAssetIcon()
//        }
//    )


}
package com.feature.send.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feature.send.R

@Composable
fun ethOSTextField(
    text: String,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    label: String = "",
    trailingIcon: @Composable (() -> Unit)?,
    singleLine: Boolean = true,
    onTextChanged: (String) -> Unit
) {
    BasicTextField(
        value = text,
        onValueChange = onTextChanged,
        singleLine = singleLine,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 20.sp
        ),
        cursorBrush = SolidColor(Color.White),
        modifier = modifier
            .clip(RoundedCornerShape(10))
            .background(Color(0xFF24303D))
            .height(64.dp)
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) { innerTextField ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.weight(1f).focusRequester(focusRequester)
            ) {
                if(text == "") {
                    Text(
                        label,
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
fun PreviewTextField() {

    var test by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ethOSTextField(
                text = test,
                label = "ENS or Address",
                trailingIcon = {
//                    Icon(painter = painterResource(id = R.drawable.baseline_qr_code_24),
//                        contentDescription = "",
//                        tint = Color.White,
//                        modifier = Modifier.size(32.dp)
//                    )
                               Icon(imageVector = Icons.Rounded.QrCode,
                                   tint = Color.White,
                                    modifier = Modifier.size(32.dp),
                                   contentDescription = "")
                },
                modifier = Modifier.fillMaxWidth(),
                focusRequester = focusRequester,

            ) {test = it}
            //TextField(value = test, onValueChange = {test = it})
        }

}
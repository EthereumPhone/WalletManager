package org.ethereumphone.walletmanager.core.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.core.designsystem.WmTextFieldColors
import org.ethereumphone.walletmanager.core.designsystem.WmTheme
import org.ethereumphone.walletmanager.core.designsystem.placeHolder
import org.ethereumphone.walletmanager.core.ui.WmTextFieldDefaults.iconSize

@Composable
fun WmTextField(
    text: String,
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
            .clip(RoundedCornerShape(5))
            .background(WmTextFieldDefaults.WmDarkTextField().containerColor)
            .padding(horizontal = 16.dp,
                vertical = 8.dp)
    ) { innerTextField ->
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                if(text == "") {
                    Text(
                        label,
                        color = Color.LightGray,
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

object WmTextFieldDefaults {
    val verticalPaddingValues = 0.dp
    val horizontalPaddingValues = 0.dp
    val indicatorColor = Color.White
    val fontSize = 20.sp
    val iconSize = 32.dp

    fun WmDarkTextField() = WmTextFieldColors()
}

@Preview
@Composable
fun PreviewTextField() {

    var test by remember { mutableStateOf("") }
    WmTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WmTextField(
                text = test,
                label = "ENS or Address",
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "",
                        tint = Color.White,
                        modifier = Modifier.size(iconSize)
                    )
                },
                modifier = Modifier.fillMaxWidth()

            ) {test = it}
            TextField(value = test, onValueChange = {test = it})
        }
    }
}
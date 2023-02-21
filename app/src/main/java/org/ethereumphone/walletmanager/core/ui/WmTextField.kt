package org.ethereumphone.walletmanager.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.core.designsystem.WmTextFieldColors
import org.ethereumphone.walletmanager.core.designsystem.placeHolder

@Composable
fun WmTextField(
    text: String,
    placeholder: String = "",
    icon: ImageVector? = null,
    colors: WmTextFieldColors = WmTextFieldColors(),
    singleLine: Boolean = true,
    onTextChanged: (String) -> Unit
) {
    BasicTextField(
        value = text,
        onValueChange = onTextChanged,
        singleLine = singleLine,
    ) {
        Surface(
            shape = RoundedCornerShape(16),
            color= colors.containerColor,
            modifier = Modifier
                .defaultMinSize(
                    minWidth = TextFieldDefaults.MinWidth,
                    minHeight = TextFieldDefaults.MinHeight
                ),
            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            ) {
                if(text.isEmpty()) {
                    Text(placeholder,
                        color= placeHolder,
                        fontSize = WmTextFieldDefaults.fontSize,
                        maxLines = if(singleLine) 1 else Int.MAX_VALUE,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(text,
                        color = colors.contentColor,
                        fontSize = WmTextFieldDefaults.fontSize,
                        maxLines = if(singleLine) 1 else Int.MAX_VALUE,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (icon != null) {
                    Spacer(Modifier.size(16.dp))
                    Icon(icon,
                        "",
                        Modifier.size(WmTextFieldDefaults.iconSize),
                        tint = colors.contentColor
                    )
                }
            }
        }
    }
}

object WmTextFieldDefaults {
    val verticalPaddingValues = 0.dp
    val horizontalPaddingValues = 0.dp
    val fontSize = 20.sp
    val iconSize = 32.dp

    fun WmDarkTextField() = WmTextFieldColors(

    )
    
}

@Preview
@Composable
fun PreviewTextField() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WmTextField(
            text = "Test",
                    icon = Icons.Default.QrCode

        ) {}
        WmTextField(
            "",
            "ENS or Address",
            icon = Icons.Default.QrCode
        ) {}
    }
}
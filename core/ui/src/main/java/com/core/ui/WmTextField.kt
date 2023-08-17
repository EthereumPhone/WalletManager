package com.core.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun WmTextField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeHolder: @Composable (() -> Unit)? = null
) {
    TextField(
        colors= TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF24303D),
            unfocusedContainerColor = Color(0xFF24303D),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
        ),
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(16.dp),
        placeholder = placeHolder
    )
}

@Preview
@Composable
fun PreviewWmTextField() {
    var text by remember { mutableStateOf("") }

    WmTextField(
        value = text,
        onChange = { text = it },
    )


}
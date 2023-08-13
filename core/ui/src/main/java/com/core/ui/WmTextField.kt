package com.core.ui

import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WmTextField(
    value: String,
    onChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeHolder: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = { onChange(it) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
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
package com.feature.swap.ui

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.feature.swap.SwapTokenUiState

@Composable
fun TokenSelector(
    swapToken: SwapTokenUiState,
    onValueChange: () -> Unit
) {
    val showDialog by remember { mutableStateOf(false) }

    TextField(
        value = swapToken.toString(),
        //supportingText = { Text(text = swapToken.maxAmount) },
        //isError = swapToken.isTooHigh,
        onValueChange = { onValueChange() }
    )
}







@Preview
@Composable
fun PreviewTokenSelector() {

}
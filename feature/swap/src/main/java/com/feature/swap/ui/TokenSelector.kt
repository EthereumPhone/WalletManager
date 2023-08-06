package com.feature.swap.ui

import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun TokenSelector(

) {
    val showDialog by remember { mutableStateOf(false) }


    TextField(
        value = "",
        onValueChange = {}
    )
}



@Composable
private fun TokenPicker() {
    Dialog(
        onDismissRequest = { /*TODO*/ },
    ) {
        
    }

}



@Preview
@Composable
fun PreviewTokenSelector() {

}
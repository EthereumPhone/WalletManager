package com.example.transactions

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun TransactionScreen(
    modifier: Modifier = Modifier
){
    Text("hallo")
}


@Composable
@Preview
fun PreviewTransactionScreen(
    modifier: Modifier = Modifier
){
    TransactionScreen()
}

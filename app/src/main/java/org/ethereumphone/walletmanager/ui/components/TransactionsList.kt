package org.ethereumphone.walletmanager.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.ethereumphone.walletmanager.models.Transaction


@Composable
fun TransactionList(
    transactionList: List<Transaction>

) {
    LazyColumn() {
        items(transactionList) { item ->
            TransactionItem(item)
        }
    }
}


@Composable
fun TransactionItem(
    transaction: Transaction
) {
    Row() {
        Box() {

        }
        Column() {
            val text = if(transaction.type) "sent to ${transaction.toAddr}" else "received from ${transaction.fromAddr}"
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = ""
            )
        }
        Column() {
            Text(
                text = transaction.value + " ETH"
            )
            Text(
                text = ""
            )
        }
    }
}



@Composable
@Preview
fun PreviewTransactionList() {
    //TransactionList()
}
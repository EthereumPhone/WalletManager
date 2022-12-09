package org.ethereumphone.walletmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.VerticalAlignBottom
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.models.Transaction
import org.ethereumphone.walletmanager.theme.ListBackground
import org.ethereumphone.walletmanager.theme.WalletManagerTheme


@Composable
fun TransactionList(
    transactionList: List<Transaction>,
    modifier: Modifier? = Modifier
) {
    Box(modifier = Modifier
        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
        .background(color = ListBackground)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 30.dp, end = 30.dp, top = 24.dp)
        ) {
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.subtitle1,
                color = Color.White.copy(alpha = .7f)
            )
            Spacer(modifier = Modifier.height(20.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(transactionList) { item ->
                    TransactionItem(transaction = item)
                }
            }
        }
    }
}


@Composable
fun TransactionItem(
    transaction: Transaction
) {
    val image = if(transaction.type) Icons.Rounded.NorthEast else Icons.Rounded.VerticalAlignBottom
    val text = if(transaction.type) "sent to ${transaction.toAddr}" else "received from ${transaction.fromAddr}"

    Row(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                image,
                contentDescription = "outgoing transaction",
                tint = Color.Black,
                modifier = Modifier
                    .fillMaxSize(.70f)
                    .align(Alignment.Center)
                )

        }
        Column(Modifier.weight(1f)) {
            Text(
                modifier = Modifier.padding(horizontal = 18.dp),
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )
            Text(
                text = ""
            )
        }
        Column{
            Text(
                text = transaction.value + " ETH",
                textAlign = TextAlign.End,
                color = Color.White
            )
            Text(
                text = "",
                textAlign = TextAlign.End,
            )
        }
    }
}

@Preview
@Composable
fun PreviewTransactionList() {
    val t1 = Transaction(
        type = true,
        value = "0.5",
        hash =  "0x1231",
        toAddr = "nceornea.eth",
        fromAddr = "0x3a4e6ed8b0f02bfbfaa3c6506af2db939ea5798c"
    )
    val t2 = Transaction(
        type = false,
        value = "0.5",
        hash =  "0x1231",
        toAddr = "nceornea.eth",
        fromAddr = "0x3a4e6ed8b0f02bfbfaa3c6506af2db939ea5798c"
    )
    val transactionList = listOf(t1,t2)
    WalletManagerTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TransactionList(transactionList)
        }
    }
}

@Preview
@Composable
fun PreviewTransactionItem() {
    val t1 = Transaction(
        type = true,
        value = "0.5",
        hash =  "0x1231",
        toAddr = "nceornea.eth",
        fromAddr = "mhaas.eth"
    )
    val t2 = Transaction(
        type = false,
        value = "0.5",
        hash =  "0x1231",
        toAddr = "nceornea.eth",
        fromAddr = "mhaas.eth"
    )

    WalletManagerTheme {
        Column {
            TransactionItem(t1)
            TransactionItem(t2)
        }
    }
}

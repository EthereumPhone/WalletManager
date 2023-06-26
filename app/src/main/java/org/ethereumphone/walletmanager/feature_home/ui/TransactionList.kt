package org.ethereumphone.walletmanager.feature_home.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.VerticalAlignBottom
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.ethereumphone.walletmanager.core.designsystem.background
import org.ethereumphone.walletmanager.core.designsystem.primary
import org.ethereumphone.walletmanager.core.model.Network
import org.ethereumphone.walletmanager.core.model.Transaction
import org.ethereumphone.walletmanager.core.ui.WmListItem
import org.ethereumphone.walletmanager.core.ui.WmTextField
import org.ethereumphone.walletmanager.ui.components.Ethereum

@Composable
fun TransactionList(
    address: String,
    network: Network,
    transactionList: List<Transaction>
) {
    Column {
        Text(
            text = "Transactions",
            fontSize = 18.sp,
            color = primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(45.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(transactionList) {transaction ->
                val sent = transaction.to.equals(address, true)
                val image = if(sent) Icons.Rounded.Add else Icons.Rounded.Remove
                val type = if(sent) "received" else "sent"
                val details = if(sent) " from " + transaction.from else " to " + transaction.to
                val context = LocalContext.current

                WmListItem(
                    primaryText = "${transaction.value} ETH",
                    secondaryText = type + details,
                    icon = image
                    ) {
                    openTxInEtherscan(
                        context = context,
                        txHash = transaction.hash,
                        selectedNetwork = network

                    )
                }
            }
        }
    }
}

fun openTxInEtherscan(context: Context, txHash: String, selectedNetwork: Network) {
    val url = "${selectedNetwork.chainExplorer}/tx/$txHash"
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    ContextCompat.startActivity(context, intent, null)
}

@Preview
@Composable
fun PreviewTransactionList() {
    var items = listOf(
        Transaction(
            blockNumber = "",
            timeStamp = "",
            hash = "0x1231",
            nonce = "",
            blockHash = "",
            transactionIndex = "",
            from = "0x3a4e6ed8b0f02bfbfaa3c6506af2db939ea5798c",
            to = "nceornea.eth",
            value = "0.5",
            gas = "",
            gasPrice = "",
            isError = "",
            txReceiptStatus = "",
            input = "",
            contractAddress = "",
            cumulativeGasUsed = "",
            gasUsed = "",
            confirmations = "",
            methodId = "",
            functionName = ""
        ),
        Transaction(
            blockNumber = "",
            timeStamp = "",
            hash = "0x1231",
            nonce = "",
            blockHash = "",
            transactionIndex = "",
            from = "nceornea.eth",
            to = "0x3a4e6ed8b0f02bfbfaa3c6506af2db939ea5798c",
            value = "0.5",
            gas = "",
            gasPrice = "",
            isError = "",
            txReceiptStatus = "",
            input = "",
            contractAddress = "",
            cumulativeGasUsed = "",
            gasUsed = "",
            confirmations = "",
            methodId = "",
            functionName = ""
        )
    )
Surface(
    color = background
) {
    TransactionList(
        "nceornea.eth",
        Ethereum,
        items
    )
    }
}
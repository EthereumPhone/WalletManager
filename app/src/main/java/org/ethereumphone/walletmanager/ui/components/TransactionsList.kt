package org.ethereumphone.walletmanager.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.VerticalAlignBottom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import org.ethereumphone.walletmanager.core.model.Network
import org.ethereumphone.walletmanager.core.model.Transaction
import org.ethereumphone.walletmanager.theme.ListBackground
import org.ethereumphone.walletmanager.theme.WalletManagerTheme


@Composable
fun TransactionList(
    transactionList: List<Transaction>,
    selectedNetwork: State<Network>,
    modifier: Modifier? = Modifier,
    address: String
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
                fontSize = 20.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(20.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(transactionList) { item ->
                    TransactionItem(
                        transaction = item,
                        selectedNetwork = selectedNetwork,
                        address = address
                    )
                }
            }
        }
    }
}


@Composable
fun TransactionItem(
    transaction: Transaction,
    selectedNetwork: State<Network>,
    address: String,
) {
    val sent = transaction.to.equals(address, true)
    val image = if(sent) Icons.Rounded.VerticalAlignBottom else Icons.Rounded.NorthEast
    val type = if(sent) "received Ether" else "sent Ether"
    val details = if(sent) "from: " + transaction.from else "to: " + transaction.to
    val context = LocalContext.current

    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                openTxInEtherscan(context, transaction.hash, selectedNetwork)
            }
    ) {
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
        Column {
            Row {
                Text(
                    text = type,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )

                Text(
                    text = transaction.value + " ETH",
                    textAlign = TextAlign.End,
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 18.dp)
                )
            }
            Row {
                Text(
                    text = details,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 18.dp)
                    )
            }
        }
    }
}

fun openTxInEtherscan(context: Context, txHash: String, selectedNetwork: State<Network>) {
    val url = "${selectedNetwork.value.chainExplorer}/tx/$txHash"
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(context, intent, null)
}

@Preview
@Composable
fun PreviewTransactionList() {
    val t1 = Transaction(
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
    )

    val t2 = Transaction(
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
    val transactionList = listOf(t1,t2)
    WalletManagerTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // create a State<Network> object
            val selectedNetwork = object : State<Network> {
                override val value: Network
                    get() = Network(1, "https://cloudflare-eth.com", "ETH", chainName = "Ethereum", chainExplorer = "https://etherscan.io")
            }
            TransactionList(transactionList, selectedNetwork = selectedNetwork, address = "nceornea.eth")
        }
    }
}
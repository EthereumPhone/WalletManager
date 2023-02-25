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
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
import java.math.BigDecimal


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
                //fontSize = 20.sp,
                color = Color.White,
                style = MaterialTheme.typography.h5
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

    val image = if(transaction.from == address) Icons.Rounded.NorthEast else Icons.Rounded.VerticalAlignBottom
    val type = if(transaction.from == address) "Sent"  else "Received"
    val sent = transaction.to.equals(address, true)
    val details = if(sent) "from: " + transaction.from else "to: " + transaction.to
    val context = LocalContext.current

    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable {
                openTxInEtherscan(context, transaction.hash, selectedNetwork)
            } ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically


    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
                ){
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
                        .fillMaxSize(.60f)
                        .align(Alignment.Center)
                )

            }
            Column(
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(
                    text = type,
                    modifier = Modifier.padding(horizontal = 18.dp),
                    style = MaterialTheme.typography.body2,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = address,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 18.dp),
                    style = MaterialTheme.typography.body2,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
        Box(

        ) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val sign = if(transaction.from == address) "-"  else "+"
                Text(
                    text = sign+" "+ BigDecimal(transaction.value).stripTrailingZeros().toString() + " ETH",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.body2,
                    fontSize = 15.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,


                        //.padding(horizontal = 18.dp)
                )
                /*
                Dollar-Amount
                Text(
                    text = details,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.body2,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    //modifier = Modifier.padding(horizontal = 18.dp),
                    color = ListBackground//Color.White.copy(alpha = 0.75f)
                )*/


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

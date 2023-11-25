package com.example.transactions

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.example.transactions.ui.TransactionDetailItem
import com.example.transactions.ui.TransferListItem


@Composable
fun TransactionScreen(
    modifier: Modifier = Modifier
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,

        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {

        //Header

        Row (
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)

        ){
            Text(
                modifier = modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = "Transactions",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        val token = TokenAsset(
            address = "",
            chainId = 1,
            symbol = "ETH",
            name = "Ether",
            balance = 0.61
        )

        val list = listOf(
            TransferItem(
                chainId = 5,
                from = "0x123123123123123123123123",
                to =  "0x123123123123123123123123",
                asset = "ETH",
                //address = "0x123123123123123123123123",
                value = "2.24",
                timeStamp = "10-19-01",//Clock.System.now().toString(),
                userSent = false,
                txHash= "pfpfnopjfpfn",
                ispending = false
            ),
            TransferItem(
                chainId = 5,
                from = "0x123123123123123123123123",
                to =  "0x123123123123123123123123",
                asset = "ETH",
                //address = "0x123123123123123123123123",
                value = "2.24",
                timeStamp = "10-19-01",//Clock.System.now().toString(),
                userSent = true,
                txHash= "pfpfnopjfpfn",
                ispending = false

            ),
            TransferItem(
                chainId = 5,
                from = "0x123123123123123123123123",
                to =  "0x123123123123123123123123",
                asset = "ETH",
                //address = "0x123123123123123123123123",
                value = "2.24",
                timeStamp = "10-19-01",//Clock.System.now().toString(),
                userSent = false,
                txHash= "pfpfnopjfpfn",
                ispending = false
            ),
            TransferItem(
                chainId = 5,
                from = "0x123123123123123123123123",
                to =  "0x123123123123123123123123",
                asset = "ETH",
                //address = "0x123123123123123123123123",
                value = "2.24",
                timeStamp = "10-19-01",//Clock.System.now().toString(),
                userSent = false,
                txHash= "pfpfnopjfpfn",
                ispending = false
            ),
            TransferItem(
                chainId = 5,
                from = "0x123123123123123123123123",
                to =  "0x123123123123123123123123",
                asset = "ETH",
                //address = "0x123123123123123123123123",
                value = "2.24",
                timeStamp = "11-19-01",//Clock.System.now().toString(),
                userSent = true,
                txHash= "pfpfnopjfpfn",
                ispending = false

            ),
            TransferItem(
                chainId = 5,
                from = "0x123123123123123123123123",
                to =  "0x123123123123123123123123",
                asset = "ETH",
                //address = "0x123123123123123123123123",
                value = "2.24",
                timeStamp = "11-19-01",//Clock.System.now().toString(),
                userSent = false,
                txHash= "pfpfnopjfpfn",
                ispending = false
            )

        )
        LazyColumn (
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ){
            var month = ""
            list.reversed().forEach { transfer ->
                item {
                    Log.e("Month", month)
                    if (transfer.timeStamp.substring(0,2) != month){
                        month = transfer.timeStamp.substring(0,2)
                        val monthtext = getMonth(transfer.timeStamp.substring(0,2).toInt())
                        Text(text = monthtext,color = Color(0xFF9FA2A5))

                    }
                    TransferListItem(
                        transfer = transfer ,
                        onCardClick = {}
                    )
                }
            }
//            items(3){
//                //AssetListItem(token, true)
//                TransferListItem(
//                    transfer = ,
//                    onCardClick = {}
//                )
//            }
        }

    }
}

fun getMonth(month: Int): String {
    var res = "This month"
    when(month){
        11 -> res = "November"
        10 -> res = "Oktober"
    }

    return res
}


@Composable
@Preview
fun PreviewTransactionScreen(
    modifier: Modifier = Modifier
){
    TransactionScreen()
}

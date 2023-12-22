package com.example.transactions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TransferItem
import com.core.ui.util.formatDouble
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransferListItem(
    modifier: Modifier = Modifier,
    transfer: TransferItem,
    onCardClick: () -> Unit = {}
) {

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            //Color of Icon
            var icontint = if (transfer.userSent) Color(0xFF2C63B3B) else Color(0xFF1B7C12)

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
                ) {

                Column (
                    modifier = modifier
                ){
                    Text(
                        text = if(transfer.userSent) "Sent ${transfer.asset}" else "Received ${transfer.asset}",
                        color = Color.White,//(0xFF9FA2A5),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.width(125.dp)
                    )
                    Text(transfer.timeStamp.take(10), color = Color(0xFF9FA2A5), fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }

                Row (
                    modifier = modifier,
                    verticalAlignment = Alignment.CenterVertically,


                ){
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ){
                        Row (
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,

                            ){
                            Text("${if(transfer.userSent) "-" else "+"}${formatDouble(transfer.value.toDouble())} ${transfer.asset}", color = icontint, fontSize = 18.sp, fontWeight = FontWeight.Medium)//"${tokenAsset.balance}", color = Color.White)
                            //Text(transfer.asset, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)//tokenAsset.symbol, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    IconButton(
                        onClick = onCardClick,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForwardIos,
                            contentDescription = "Go back",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }


        }



}

fun compareDate(transfer: String): String{//date1: String, date2:String) {
    //val d1 = "12/24/2018"
    val year = transfer.substring(0,4)
    val month = transfer.substring(5,7)
    val day = transfer.substring(8,10)
    val txdate = "${month}-${day}-${year}"
    val txtime = transfer.takeLast(8)
    val today = "${LocalDateTime.now().month.value}-${LocalDateTime.now().dayOfMonth}-${LocalDateTime.now().year}"

    val sdf = SimpleDateFormat("MM-dd-yyyy")

    val firstDate: Date = sdf.parse(txdate)
    val secondDate: Date = sdf.parse(today)

    val cmp = firstDate.compareTo(secondDate)
    when {
        cmp > 0 -> {
            return "${txdate} ${txtime}"//after today
        }
        cmp < 0 -> {
            return txdate//before today
        }
        else -> {
            return txtime// same day

        }
    }
}

private fun chainToNetworkName(chainId: Int): String = when(chainId) {
        1 -> "Mainnet"
        5 -> "Görli"
        10 -> "Optimism"
        137 -> "Polygon"
        42161 -> "Arbitrum"
        else -> ""
    }

@Preview
@Composable
fun TransferItemPreview() {
//    TransferItem(
//        chainId = 5,
//        asset = "ether",
//        address = "0x123123",
//        value = "2.24",
//        timeStamp = Clock.System.now().toString(),
//        userSent = true
//    )
    TransferListItem(
        transfer = TransferItem(
            chainId = 5,
            from = "0x123123123123123123123123",
            to =  "0x123123123123123123123123",
            asset = "ETH",
            //address = "0x123123123123123123123123",
            value = "2.24",
            timeStamp = "1000-19-01 00:00:00",//Clock.System.now().toString(),
            userSent = true,
            txHash= "pfpfnopjfpfn",
        ),
        onCardClick = {}
    )

}
package com.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.core.model.Transfer
import com.core.model.TransferItem

@Composable
fun TransferDialog(
    transfer: TransferItem= TransferItem(
        chainId = 1,
        from= "",
        to="",
        asset = "",
        value = "",
        timeStamp = "",
        userSent = true
    ),
    currencyPrice: String,
    onCurrencyChange: (String) -> Unit,
    setShowDialog: () -> Unit,
){

    val network = when(transfer.chainId) {
        1 -> "Mainnet"
        5 -> "Görli"
        10 -> "Optimism"
        137 -> "Polygon"
        42161 -> "Arbitrum"
        else -> ""
    }

    var currencychange = when(transfer.chainId){
        137 -> "MATICUSDT"
        else -> "ETHUSDT"
    }
    onCurrencyChange(currencychange)

    Dialog(onDismissRequest = { setShowDialog() }) { //}){//
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF24303D),
            contentColor = Color.White,
            tonalElevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)

                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "",
                            tint = Color(0xFF24303D),
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                        )
                        Text(
                            text = "Transfer Info",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF24303D),//Color.White,
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clickable { }//setShowDialog() }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))


                    //Amount
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)  ,
                        horizontalAlignment = Alignment.Start,
                    ){
                        Text(
                            text = "Amount",
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Left,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if(transfer.chainId != 5){
                            Text(
                                text = "${transfer.value} ${transfer.asset.uppercase()} " +
                                        "${ if (currencyPrice!= "") "($${currencyPrice.toFloat()*transfer.value.toFloat()})" else "" }",
                                fontSize = 16.sp,
                                color = Color(0xFF9FA2A5),
                                textAlign = TextAlign.Left,
                                fontWeight = FontWeight.Normal,
                            )
                        }else{
                            Text(
                                text = "${transfer.value} ${transfer.asset.uppercase()} " +
                                        "($0.0)",
                                fontSize = 16.sp,
                                color = Color(0xFF9FA2A5),
                                textAlign = TextAlign.Left,
                                fontWeight = FontWeight.Normal,
                            )
                        }

                    }



                    //From address
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)  ,
                        horizontalAlignment = Alignment.Start,
                    ){
                        Text(
                            text = if (transfer.userSent) "To" else "From",
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Left,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (transfer.userSent) transfer.to else transfer.from,
                            fontSize = 16.sp,
                            color = Color(0xFF9FA2A5),
                            textAlign = TextAlign.Left,
                            fontWeight = FontWeight.Normal,
                        )
                    }




                    //Network
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)  ,
                        horizontalAlignment = Alignment.Start,
                    ){
                        Text(
                            text = "Network",
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Left,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = network,
                            fontSize = 16.sp,
                            color = Color(0xFF9FA2A5),
                            textAlign = TextAlign.Left,
                            fontWeight = FontWeight.Normal,
                        )
                    }


                    //Time
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)  ,
                        horizontalAlignment = Alignment.Start,
                    ){
                        Text(
                            text = "Time",
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Left,
                            fontWeight = FontWeight.SemiBold,
                        )
                        val year = transfer.timeStamp.substring(0,4)
                        val month = transfer.timeStamp.substring(5,7)
                        val day = transfer.timeStamp.substring(8,10)
                        val txdate = "${month}-${day}-${year}"
                        val txtime = transfer.timeStamp.takeLast(8)
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(
                                text = "${txdate}",
                                fontSize = 16.sp,
                                color = Color(0xFF9FA2A5),
                                textAlign = TextAlign.Left,
                                fontWeight = FontWeight.Normal,
                            )
                            Text(
                                text = " at ",
                                fontSize = 16.sp,
                                color = Color.White,
                                textAlign = TextAlign.Left,
                                fontWeight = FontWeight.Normal,
                            )
                            Text(
                                text = "${txtime}",
                                fontSize = 16.sp,
                                color = Color(0xFF9FA2A5),
                                textAlign = TextAlign.Left,
                                fontWeight = FontWeight.Normal,
                            )
                        }

                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewDialogScreen() {
//    TransferDialog(
//        text = "Transfer info",
//        title = "Transfer info"
//    )
}

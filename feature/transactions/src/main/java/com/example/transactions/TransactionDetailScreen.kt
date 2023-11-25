package com.example.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAsset
import com.example.transactions.ui.TransactionDetailItem

@Composable
fun TransctionDetailScreen(
    modifier: Modifier = Modifier
){
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ){
        //Header
        Row (
            modifier = modifier
                .fillMaxWidth().padding(start = 12.dp,end = 24.dp,top = 24.dp)
            ,
            //.background(Color.Red),
            Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ){


            IconButton(
                onClick = {
                    //onBackClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Go back",
                    tint = Color.White
                )
            }



            Text(
                text = "Transactions",
                color = Color.White,
                fontSize = 18.sp,

                )

        }
        Column(
            horizontalAlignment = Alignment.Start,

            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 24.dp)
        ) {

            Spacer(modifier = modifier.height(64.dp))

            Text(text = "Sent ETH",fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 48.sp)

            Spacer(modifier = modifier.height(84.dp))

            val token = TokenAsset(
                address = "",
                chainId = 1,
                symbol = "ETH",
                name = "Mainnet",
                balance = 0.61
            )

            Column (
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ){
                TransactionDetailItem("Date","Hallo")
                TransactionDetailItem("Address","Hallo")
                TransactionDetailItem("Chain","Hallo")
                TransactionDetailItem("Amount","Hallo")
            }

            Spacer(modifier = Modifier.height(96.dp))
            Button(
                onClick = { /*TODO*/ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "View on etherscan", color = Color(0xFF71B5FF), fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

}
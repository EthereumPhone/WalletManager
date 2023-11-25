package com.example.assets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
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

@Composable
fun AssetDetailScreen(){

    Column {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom=24.dp),
            //.background(Color.Red),
            Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ){
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .width(100.dp)
            ) {

                IconButton(
                    onClick = {

                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Go back",
                        tint = Color.White
                    )
                }
            }

            //Header title
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = "title",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            //Warning or info

            Box (
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .width(100.dp)
            ){

            }
        }

        Text("WETH")


    }
}

@Composable
fun AssetNetworkDetail(
    network: String,
    balance: Double,
    fiatbalance: Double
) {
    Row {
        Text(network)
        Column {
            Text("${balance}")
            Text("$${fiatbalance}")
        }
    }
}

@Composable
@Preview
fun PreviewAssetNetworkDetail(){
    //AssetNetworkDetail("Mainnet",0.0,0.0)
    AssetDetailScreen()
}



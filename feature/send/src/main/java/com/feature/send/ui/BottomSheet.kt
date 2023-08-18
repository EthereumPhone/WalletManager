package com.feature.send.ui

import android.net.Network
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetValue
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ethoscomponents.components.NumPad

@Composable
fun BottomSheet(
    value: Int,
    modifier: Modifier = Modifier,
    walletInfo: List<MockNetworkData>
){
    val context = LocalContext.current
    Box(

        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(Color(0xFF24303D))
            ) {
        Column {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = modifier
                    .fillMaxWidth()
                    //.background(Color.Red)
                    .height(64.dp)
                    .padding(horizontal = 24.dp, vertical = 8.dp)

            ){

                    Text(
                        text = "Select Network $value",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 24.sp
                    )


            }
            Divider(
                color = Color(0xFF9FA2A5),
                thickness = 1.dp

            )
            LazyColumn(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Text(text = "Hello Geek!", fontSize = 20.sp, color = Color.White)
                val tmp = listOf("hfoa","jsohgo")
                itemsIndexed(
                   walletInfo
                ){ index, item  ->

                        BottomSheetAsset(
                            item.name,
                            item.ethAmount,
                            item.fiatAmount,
                            modifier = Modifier.clickable {
                                //value = item.chainId
                            }
                        )


                    if(index < walletInfo.size-1) {
                        Divider(
                            color = Color(0xFF9FA2A5)
                        )

                    }

                }
            }

        }

    }
}

@Composable
fun BottomSheetAsset(
    network: String,
    ethAmount: Double,
    fiatAmount: Double,
    modifier: Modifier = Modifier,
){
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            //.background(Color.Red)
            .height(64.dp)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.weight(1f)
            ){
            //TODO: add picture
            Box(
                modifier = modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF9FA2A5))
            ) {

            }
            Text(
                text = network,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 24.sp
            )
        }

        Column (
            horizontalAlignment = Alignment.End
        ){
            Text(
                text = "$ethAmount ETH",
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 16.sp,

            )
            Text(
                text = "$ $fiatAmount",
                fontWeight = FontWeight.Normal,
                color = Color(0xFF9FA2A5),
                fontSize = 14.sp,

            )
        }
    }
}

@Preview
@Composable
fun PreviewBottomSheet() {
   // BottomSheet()
}
package com.example.assets.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.core.model.TokenAsset

@Composable
fun AssetListItem(
    tokenAsset: TokenAsset
){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(tokenAsset.symbol)
        Row {
            Column {
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text("${tokenAsset.balance}")
                    Text(tokenAsset.symbol)
                }
                Text("$0.00")
            }
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
    }
}


@Composable
@Preview
fun PreviewAssetListItem(){

    val token = TokenAsset(
        address = "",
        chainId = 1,
        symbol = "ETH",
        name = "Ether",
        balance = 0.61
    )

    AssetListItem(token)


}

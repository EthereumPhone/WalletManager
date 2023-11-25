package com.example.assets.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAsset

@Composable
fun AssetListItem(
    tokenAsset: TokenAsset,
    withMoreDetail: Boolean=false,
    linkTo: () -> Unit = {},
){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ){
        Text(tokenAsset.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ){
                Row (
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,

                    ){
                    Text("${tokenAsset.balance}", color = Color.White, fontWeight = FontWeight.Medium)

                    Text(tokenAsset.symbol, color = Color.White, fontWeight = FontWeight.Medium)
                }
                Text("$0.00", color = Color(0xFF9FA2A5),fontWeight = FontWeight.Medium )
            }
            if(withMoreDetail){
                androidx.compose.material.IconButton(
                    onClick = linkTo,
                    modifier = Modifier.size(32.dp),
                ) {
                    androidx.compose.material.Icon(
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

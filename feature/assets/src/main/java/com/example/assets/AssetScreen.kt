package com.example.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.core.model.TokenAsset
import com.core.ui.TopHeader
import com.example.assets.ui.AssetListItem

@Composable
fun AssetRoute(
    modifier: Modifier = Modifier,
//    onBackClick: () -> Unit,
//    initialAddress: String = "",
//    viewModel: SendViewModel = hiltViewModel()
) {
    AssetScreen()
}

@Composable
fun AssetScreen(
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

        TopHeader(
            onBackClick = {},//onBackClick ,
            title = "My Assets",
            icon = {
                androidx.compose.material.IconButton(
                    onClick = {
                        //opens InfoDialog
//                        showInfoDialog.value = true
                    }
                ) {
                    androidx.compose.material.Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Information",
                        tint = Color(0xFF9FA2A5),
                        modifier = modifier
                            .clip(CircleShape)

                    )
                }
            }
        )
        val token = TokenAsset(
            address = "",
            chainId = 1,
            symbol = "ETH",
            name = "Ether",
            balance = 0.61
        )

        LazyColumn (
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ){
            items(3){
                AssetListItem(token, true)
            }

        }

    }
}

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
fun PreviewAssetScreen(){
    AssetScreen()
}
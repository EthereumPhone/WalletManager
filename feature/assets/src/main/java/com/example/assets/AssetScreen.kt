package com.example.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowBackIosNew
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
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {

        //Header

        Row (
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom=24.dp),
            //.background(Color.Red),
            Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ){
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .width(100.dp)
            ) {

                androidx.compose.material.IconButton(
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


//                    Text(
//                        text = "Home",
//                        color = Color.White,
//                        fontSize = 18.sp,
//
//                        )
            }

            //Header title
            Text(
                modifier = modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = "title",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            //Warning or info

            Box (
                contentAlignment = Alignment.CenterEnd,
                modifier = modifier
                    .width(100.dp)
            ){
//            Icon(
//                imageVector = Icons.Outlined.Info,
//                contentDescription = "Information",
//                tint = Color(0xFF9FA2A5),
//                modifier = modifier
//                    .clip(CircleShape)
//                    //.background(Color.Red)
//                    .clickable {
//                        //opens InfoDialog
//                        //showDialog.value = true
//                        openOnClick
//                    }
//
//            )
                //icon()
            }
        }

        val token = TokenAsset(
            address = "",
            chainId = 1,
            symbol = "ETH",
            name = "Ether",
            balance = 0.61
        )

        AssetListItem(token)
    }
}

@Composable
@Preview
fun PreviewAssetScreen(){
    AssetScreen()
}
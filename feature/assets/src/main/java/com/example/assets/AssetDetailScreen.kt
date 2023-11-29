package com.example.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AssetDetailRoute(
    modifier: Modifier = Modifier,
    navigateToAsset: () -> Unit,
    viewModel: AssetDetailViewModel = hiltViewModel(),
) {

    val assetDetailUiState: AssetUiState by viewModel.currentState.collectAsStateWithLifecycle()

    AssetDetailScreen(
        assetDetailUiState = assetDetailUiState,
        navigateToAsset = navigateToAsset
    )
}
@Composable
fun AssetDetailScreen(
    modifier: Modifier = Modifier,
    assetDetailUiState: AssetUiState,
    navigateToAsset: () -> Unit,
){
    when(assetDetailUiState){
        is AssetUiState.Error -> { }
        is AssetUiState.Empty -> { }
        is AssetUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text(text = "Loading...", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        is AssetUiState.Success -> {
            var asset = assetDetailUiState.assets
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ){
                    Row (
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 24.dp, top = 24.dp)
                        ,
                        //.background(Color.Red),
                        Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ){


                        IconButton(
                            onClick = navigateToAsset
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = "Go back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Assets",
                            color = Color.White,
                            fontSize = 18.sp,

                            )

                    }
                    Column(
                        horizontalAlignment = Alignment.Start,

                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                    ) {

                        Spacer(modifier = modifier.height(64.dp))

                        Text(text = asset.get(0).symbol,fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 48.sp)

                        Spacer(modifier = modifier.height(84.dp))



                        LazyColumn (
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ){
                            asset.forEach {
                                item {
                                    AssetListItem(it, false)
                                }

                            }


                        }
                    }
                }

        }


    }



}



@Composable
@Preview
fun PreviewAssetNetworkDetail(){
    //AssetNetworkDetail("Mainnet",0.0,0.0)
    //AssetDetailScreen()
}



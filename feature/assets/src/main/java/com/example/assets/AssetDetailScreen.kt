package com.example.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.core.ui.util.shimmerEffect
import com.example.assets.ui.AssetListDetailItem
import com.example.assets.ui.LoadingAssetListDetailItem
import org.ethosmobile.components.library.theme.Colors

@Composable
fun AssetDetailRoute(
    modifier: Modifier = Modifier,
    navigateToAsset: () -> Unit,
    viewModel: AssetDetailViewModel = hiltViewModel(),
) {
    val detailAssetUiState by viewModel.currentState.collectAsStateWithLifecycle()
    AssetDetailScreen(
        detailAssetUiState = detailAssetUiState,
        navigateToAsset = navigateToAsset
    )
}
@Composable
fun AssetDetailScreen(
    modifier: Modifier = Modifier,
    detailAssetUiState: DetailAssetUiState,
    navigateToAsset: () -> Unit,
){
    when(detailAssetUiState){
        is DetailAssetUiState.Error -> { }
        is DetailAssetUiState.Empty -> { }
        is DetailAssetUiState.Loading -> {
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ){
                Row (
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 0.dp, top = 24.dp)
                    ,
                    Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Button(
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor=  Colors.TRANSPARENT,
                            contentColor = Colors.WHITE
                        ),
                        contentPadding = PaddingValues(0.dp,0.dp,16.dp,0.dp),
                        onClick = navigateToAsset
                    ) {
                        Row (
                            modifier = modifier,
                            Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ){
                            Icon(
                                imageVector = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = "Go back",
                                tint = Colors.WHITE
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Assets",
                                color = Colors.WHITE,
                                fontSize = 18.sp,
                            )
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.Start,

                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(horizontal = 24.dp, vertical = 0.dp)
                ) {

                    Spacer(modifier = modifier.height(64.dp))

                    Box(
                        contentAlignment = Alignment.Center ,
                        modifier = Modifier
                            .height(72.dp)
                            .width(250.dp)
                            .clip(CircleShape)
                            .shimmerEffect()

                    ) {

                    }

                    Spacer(modifier = modifier.height(84.dp))

                    Column (
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ){

                        LoadingAssetListDetailItem()
                        LoadingAssetListDetailItem()
                        LoadingAssetListDetailItem()
                        LoadingAssetListDetailItem()

                    }

//                    Spacer(modifier = modifier.height(120.dp))
//
//                    Column (
//                        modifier = modifier.fillMaxWidth(),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ){
//                        Box(
//                            contentAlignment = Alignment.Center ,
//                            modifier = Modifier
//                                .height(48.dp)
//                                .width(200.dp)
//                                .clip(CircleShape)
//                                .shimmerEffect()
//
//                        ) {
//
//                        }
//                    }


                }
            }


//            Box(
//                modifier = modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ){
//                Text(text = "Loading...", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
//            }
        }
        is DetailAssetUiState.Success -> {
            var asset = detailAssetUiState.assets
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ){
                    Row (
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 0.dp, top = 24.dp)
                        ,
                        //.background(Color.Red),
                        Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Button(
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor=  Colors.TRANSPARENT,
                                contentColor = Colors.WHITE
                            ),
                            contentPadding = PaddingValues(0.dp,0.dp,16.dp,0.dp),
                            onClick = navigateToAsset
                        ) {
                            Row (
                                modifier = modifier,
                                Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                Icon(
                                    imageVector = Icons.Rounded.ArrowBackIosNew,
                                    contentDescription = "Go back",
                                    tint = Colors.WHITE
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Assets",
                                    color = Colors.WHITE,
                                    fontSize = 18.sp,
                                )
                            }
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.Start,

                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .padding(horizontal = 24.dp, vertical = 0.dp)
                    ) {

                        Spacer(modifier = modifier.height(64.dp))

                        Text(text = asset.get(0).symbol.uppercase(),fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 48.sp)



                        LazyColumn (
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ){
                            asset.forEach {
                                item {
                                    AssetListDetailItem(it)
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
//    AssetNetworkDetail("Mainnet",0.0,0.0)
    val testData = listOf(
        TokenAsset(
            address = "",
            chainId = 1,
            symbol = "ETH",
            name = "Super long spammy name",
            balance = 0.61,
            decimals = 3,
            logoUrl = "https://icons.iconarchive.com/icons/cjdowner/cryptocurrency-flat/256/Ethereum-ETH-icon.png"//"https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/9720e55c-d222-4769-90b8-aec2262c0988/ddvtmz1-cadfaa7f-6da9-4b59-a0fe-6ed5742af38c.jpg/v1/fill/w_1192,h_670,q_70,strp/bruh_by_jukeboxfromao_ddvtmz1-pre.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7ImhlaWdodCI6Ijw9NzIwIiwicGF0aCI6IlwvZlwvOTcyMGU1NWMtZDIyMi00NzY5LTkwYjgtYWVjMjI2MmMwOTg4XC9kZHZ0bXoxLWNhZGZhYTdmLTZkYTktNGI1OS1hMGZlLTZlZDU3NDJhZjM4Yy5qcGciLCJ3aWR0aCI6Ijw9MTI4MCJ9XV0sImF1ZCI6WyJ1cm46c2VydmljZTppbWFnZS5vcGVyYXRpb25zIl19.OTeZgFcV45DZqyg43rAeGzSld3mOIMTCffVyi3SGM8o"
        )
    )
    //.Success(testData)
    AssetDetailScreen(detailAssetUiState = DetailAssetUiState.Loading, navigateToAsset = {})
}



package com.example.assets

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.CurrentState
import com.core.model.TokenAsset
import com.core.model.UserData
import com.example.assets.ui.AssetListItem
import com.example.assets.ui.formatDouble
import java.text.DecimalFormat

@Composable
fun AssetRoute(
    modifier: Modifier = Modifier,
    navigateToAssetDetail: (String) -> Unit,
    viewModel: AssetViewModel = hiltViewModel(),
) {

    val walletDataUiState: WalletDataUiState by viewModel.walletDataState.collectAsStateWithLifecycle()
    val assetsUiState: AssetUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val refreshState: Boolean by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val localContext = LocalContext.current


    //For refresher
    var updater by remember { mutableStateOf(true) }

//    if(updater) {
//        Log.d("automatic updater", "TEST")
//        viewModel.refreshData()
//        updater = false
//    }

    AssetScreen(
        assetsUiState = assetsUiState,
        refreshState = refreshState,
        onRefresh = viewModel::refreshData,
        navigateToAssetDetail = navigateToAssetDetail


    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun AssetScreen(
    modifier: Modifier = Modifier,
    assetsUiState: AssetUiState,
    refreshState: Boolean,
    onRefresh: () -> Unit,
    navigateToAssetDetail: (String) -> Unit,
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,

        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {
        Row (
            //modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)
        ){
            Text(
                modifier = modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = "Assets",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

    }

    when(assetsUiState){
        is AssetUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text(text = "Loading...", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        is AssetUiState.Empty -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text(text = "No Assets yet", color = Color(0xFF9FA2A5), fontSize = 24.sp, fontWeight = FontWeight.Medium)
            }
        }
        is AssetUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text(text = "Error...", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        is AssetUiState.Success -> {
            val pullRefreshState = rememberPullRefreshState(
                refreshing = refreshState,
                onRefresh = {
                    onRefresh()
                }
            )
            Box{

                    if(assetsUiState.assets.isNotEmpty()){
                        val groupedAssets = assetsUiState.assets.groupBy { it.symbol }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            LazyColumn {
                                groupedAssets.forEach { (assetName, assetList) ->
                                    item(key = assetName) {
                                        AssetListItem(assetName,assetList,
                                            true
                                        ) {
                                            //set currentState


                                            //navigate to detailscreen
                                            navigateToAssetDetail(assetList[0].symbol)
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Text(text = "No Assets yet", color = Color(0xFF9FA2A5), fontSize = 24.sp, fontWeight = FontWeight.Medium)
                        }
                    }






                    PullRefreshIndicator(
                        refreshing = refreshState,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }




        }

        else -> {
            Text(text = "Else...", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)

        }
    }

}

fun formatDouble(input: Double): String {
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(input)
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
                IconButton(
                    onClick = linkTo,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
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
    AssetScreen(
        assetsUiState = AssetUiState.Success(
            listOf(
                    TokenAsset(
                        address = "byuohui",
                        chainId =  5,
                        symbol=  "ETH",
                        name= "Ether",
                        balance= 1.657
                    ),
                    TokenAsset(
                        address = "yuooyvyuv",
                        chainId =  10,
                        symbol=  "ETH",
                        name= "Ether",
                        balance= 0.12
                    )
                )


        ),
        refreshState = false,
        onRefresh = {},
        navigateToAssetDetail ={},
//        toAssetDetail= {
//            CurrentState(
//                address = "",
//                symbol = "ETH",
//                name = "assetName",
//                balance = 0.0,
//                assets = emptyList()
//            )
//        }
    )
}
package com.example.assets

import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.core.ui.TopHeader
import com.example.assets.ui.AssetListItem
import com.example.assets.ui.formatDouble
import java.text.DecimalFormat

@Composable
fun AssetRoute(
    modifier: Modifier = Modifier,
    navigateToAssetDetail: (String) -> Unit,
    viewModel: AssetViewModel = hiltViewModel(),
) {
    val assetsUiState: AssetUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val refreshState: Boolean by viewModel.isRefreshing.collectAsStateWithLifecycle()

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
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshState,
        onRefresh = {
            onRefresh()
        }
    )


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,

        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {
        TopHeader(title = "Assets")
        Spacer(modifier = Modifier.height(48.dp))
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

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    LazyColumn(

                    ) {
                        assetsUiState.assets.forEach {
                            item(it.key) {
                                AssetListItem(title = it.key, assets = it.value) {
                                    navigateToAssetDetail(it.key)
                                }
                            }
                        }
                    }

                    PullRefreshIndicator(
                        refreshing = refreshState,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

fun formatDouble(input: Double): String {
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(input)
}


@Composable
@Preview
fun PreviewAssetScreen(){
    AssetScreen(
        assetsUiState = AssetUiState.Loading,
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

/*
                    val groupedAssets = assetsUiState.assets.groupBy { it.symbol }

                    LazyColumn {
                        groupedAssets.forEach { (assetName, assetList) ->
                            item(key = assetName) {
                                AssetListItem(assetName,assetList,) {
                                    //navigate to detailscreen
                                    navigateToAssetDetail(assetList[0].symbol)
                                }
                            }
                        }
                    }

                     */
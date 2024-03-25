package com.example.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.example.assets.ui.AssetListItem
import org.ethosmobile.components.library.core.ethOSHeader
import org.ethosmobile.components.library.theme.Fonts
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(start = 32.dp,end = 32.dp, bottom = 32.dp)
    ) {
        ethOSHeader(title = "Assets")
        Spacer(modifier = Modifier.height(48.dp))

        when(assetsUiState){
            is AssetUiState.Loading -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = "Loading...", fontFamily = Fonts.INTER, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                }
            }
            is AssetUiState.Empty -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = "No assets yet",fontFamily = Fonts.INTER, color = Color(0xFF9FA2A5), fontSize = 24.sp, fontWeight = FontWeight.Medium)
                }
            }
            is AssetUiState.Error -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = "Error...",fontFamily = Fonts.INTER, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                }
            }
            is AssetUiState.Success -> {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
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


@Composable
@Preview
fun PreviewAssetScreen(){
    val testData = mapOf("ethereum" to listOf(
        TokenAsset(
            address = "",
            chainId = 1,
            symbol = "eth",
            name = "ethereum",
            logoUrl = "https://www.deviantart.com/jukeboxfromao/art/bruh-839511181",
            balance = 1.2,
    )))


    AssetScreen(
        assetsUiState = AssetUiState.Success(testData),
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
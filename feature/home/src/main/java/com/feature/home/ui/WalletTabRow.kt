package com.feature.home.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box



import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.core.database.model.TransferEntity
import com.core.designsystem.theme.placeHolder
import com.core.designsystem.theme.primary
import com.core.designsystem.theme.primaryVariant
import com.core.model.TransferItem
import com.feature.home.AssetUiState
import com.feature.home.TransfersUiState
import kotlinx.coroutines.launch
import java.lang.Float.min
import kotlin.math.roundToInt

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
internal fun WalletTabRow(
//    transfersUiState: TransfersUiState,
    assetsUiState: AssetUiState,
//    refreshState: Boolean,
//    onRefresh: () -> Unit,
//    onDelete: (TransferItem) -> Unit,
//    onTxOpen: (TransferItem) -> Unit,
//    userAddress: String,
) {


//    val tabItems = remember { TabItems.values().toList() }
//    val pagerState = rememberPagerState(
//        initialPage = 0,
//        initialPageOffsetFraction = 0f
//    ) {
//        tabItems.size
//    }

//    val pullRefreshState = rememberPullRefreshState(
//        refreshing = refreshState,
//        onRefresh = {
//            onRefresh()
//        }
//    )


        AssetList(
            assetsUiState
//            assets
        )

//        PullRefreshIndicator(
//            refreshing = refreshState,
//            state = pullRefreshState,
//            modifier = Modifier.align(Alignment.TopCenter)
//        )

}

 data class Asset(
    val chain: Int,
    val amount: Double,
    val fiatAmount: Double,

)

 data class AssetItem(
    val assetname: String,
    val assets: List<Asset>,
    val abbriviation: String
)

@Composable
private fun AssetList(
    assetsUiState: AssetUiState,
//    list: List<AssetItem>
){
                Box(
                    contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
            ) {

                    when(assetsUiState){
                        is AssetUiState.Empty ->{
                            Text(text = "No assets", color = Color(0xFF9FA2A5), fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        }
                        is AssetUiState.Loading -> {
                            Text(text = "Loading...", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        }
                        is AssetUiState.Success -> {
                            val assets = assetsUiState.assets.take(5)
                            LazyColumn(
                                modifier = Modifier,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                assets.forEach { item ->
                                    item(key = item.address) {

                                        AssetListItem(title = item.symbol, value = item.balance)
                                        //AssetExpandableItem(title = formatString(assetName), assets = assetAmount)//, currencyPrice = currencyPrice,onCurrencyChange= onCurrencyChange)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }

                        else -> {}
                    }

                //}
            }


//, currencyPrice: String, onCurrencyChange: (String) -> Unit) {
//    when (assetsUiState) {
//        is AssetUiState.Loading -> {  }
//        is AssetUiState.Error -> {  }
//        is AssetUiState.Empty -> {
//            Box(
//                modifier = Modifier
//                    //.background(primaryVariant)
//                    .fillMaxSize()
//            ) {
//                Text(
//                    text = "No assets found",
//                    color = placeHolder,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            }
//        }
//        is AssetUiState.Success -> {
//            val groupedAssets = assetsUiState.assets.filter { it.chainId != 5 }.groupBy { it.symbol }
//
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//            ) {
////                Card(
////                    colors = CardDefaults.cardColors(containerColor = primaryVariant)
////                ) {
//                    LazyColumn {
//                        groupedAssets.forEach { (assetName, assetList) ->
//                            item(key = assetName) {
//                                AssetExpandableItem(title = formatString(assetName), assets = assetList)//, currencyPrice = currencyPrice,onCurrencyChange= onCurrencyChange)
//                                Spacer(modifier = Modifier.height(8.dp))
//                            }
//                        }
//                    }
//                //}
//            }
//        }
//
//        else -> {}
//    }
}

fun formatString(input: String): String {
    return input.uppercase()
}



@Composable
@Preview
fun PreviewWalletTabRow() {
//    var txInfo by remember { mutableStateOf(
//        TransferItem(
//            chainId = 1,
//            address = "",
//            asset = "",
//            value = "",
//            timeStamp = "",
//            userSent = true
//        )
//    ) }
//
//    Column {
//        WalletTabRow(
//            TransfersUiState.Loading,
//            AssetUiState.Loading,
//            false,
//            {},
//            onTxOpen = {
//                txInfo = it
//            }
//        )
//    }

    val assets = mutableListOf(
        AssetItem(
            "Ether",
            mutableListOf(
                Asset(
                    1,
                    0.1,
                    201.51
                )
            ),
            "ETH"
        ),
        AssetItem(
            "DAI",
            mutableListOf(
                Asset(
                    1,
                    1234.56 ,
                    1234.56
                )
            ),
            "DAI"
        ),
        AssetItem(
            "Dogecoin",
            mutableListOf(
                Asset(
                    1,
                    50000.0,
                    3896.67
                )
            ),
            "DOGE"
        ),
        AssetItem(
            "WETH",
            mutableListOf(
                Asset(
                    1,
                    0.41,
                    826.20
                ),
                Asset(
                    2,
                    0.2,
                    403.02
                )

            ),
            "WETH"
        )
    )

    Box {
//        AssetList(
////            assetsUiState
////            assets
//        )
    }

}


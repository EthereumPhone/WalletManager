package com.feature.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.core.designsystem.theme.placeHolder
import com.core.designsystem.theme.primary
import com.core.designsystem.theme.primaryVariant
import com.feature.home.AssetUiState
import com.feature.home.TransfersUiState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
internal fun WalletTabRow(
    transfersUiState: TransfersUiState,
    assetsUiState: AssetUiState,
    refreshState: Boolean,
    onRefresh: () -> Unit
) {
    val tabItems = remember { TabItems.values().toList() }
    val pagerState = rememberPagerState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshState,
        onRefresh = { onRefresh() }
    )

    TabRow(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(primaryVariant),
        backgroundColor = primaryVariant,
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            WmTabIndicator(tabPositions, pagerState)
        }
    ) {
        tabItems.forEachIndexed { index, title ->
            TabRowItem(
                index = index,
                tabName = title.name,
                pagerState = pagerState
            )
        }
    }

    Box {
        HorizontalPager(
            pageCount = tabItems.size,
            state = pagerState,
            modifier = Modifier.pullRefresh(pullRefreshState)
        ) { page ->
            when(tabItems[pagerState.currentPage]) {
                TabItems.TRANSFERS -> TransferList(transfersUiState)
                TabItems.ASSETS -> AssetList(assetsUiState)
            }
        }

        PullRefreshIndicator(
            refreshing = refreshState,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}


@Composable
private fun AssetList(assetsUiState: AssetUiState) {
    when (assetsUiState) {
        is AssetUiState.Loading -> {  }
        is AssetUiState.Error -> {  }
        is AssetUiState.Empty -> {
            Box(
                modifier = Modifier
                    .background(primaryVariant)
                    .fillMaxSize()
            ) {
                Text(
                    text = "No assets found",
                    color = placeHolder,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is AssetUiState.Success -> {
            val groupedAssets = remember { assetsUiState.assets.groupBy { it.symbol } }

            Card(
                colors = CardDefaults.cardColors(containerColor = primaryVariant)
            ) {
                LazyColumn {
                    groupedAssets.forEach { (assetName, assetList) ->
                        item(key = assetName) {
                            AssetExpandableItem(title = assetName, assets = assetList)
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun TransferList(transfersUiState: TransfersUiState) {
    when (transfersUiState) {
        is TransfersUiState.Success -> if(transfersUiState.transfers.isNotEmpty()){
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = primaryVariant
                ),
            ) {
                LazyColumn {
                    items(
                        items = transfersUiState.transfers,
                        key = { transfer -> transfer.timeStamp }
                    ) {
                        TransferItem(transfer = it)
                    }
                }
            }
        } else {
            LazyColumn {
                item { Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                ) {
                    Text(
                        text = "No Transfers found",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } }
            }
        }
        is TransfersUiState.Loading -> {  }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabRowItem(
    index: Int,
    tabName: String,
    pagerState: PagerState
) {
    val coroutineScope = rememberCoroutineScope()

    val isSelected = index == pagerState.currentPage

    val color by animateColorAsState(
        targetValue = if(isSelected) primaryVariant else primary,
        animationSpec = tween(durationMillis = 300),
        label = ""
    )


    Tab(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .zIndex(2f),
        text = {
            Text(
                text = tabName,
                color = color
            )
        },
        selected = isSelected,
        onClick = {
            coroutineScope.launch {
                pagerState.animateScrollToPage(index)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WmTabIndicator(
    tabPositions: List<TabPosition>,
    pagerState: PagerState
) {


    val offsetTransition by animateDpAsState(
        targetValue = tabPositions[pagerState.currentPage].left,
        label = ""
    )
    val offsetPx = with(LocalDensity.current) { offsetTransition.toPx() }

    Box(
        Modifier
            .offset {
                IntOffset(offsetPx.roundToInt(), 0)
            }
            .wrapContentSize(align = Alignment.BottomStart)
            .width(tabPositions[pagerState.currentPage].width)
            .padding(2.dp)
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF), RoundedCornerShape(50))
            .border(BorderStroke(2.dp, Color(0xFFFFFFFF)), RoundedCornerShape(50))
            .zIndex(1f)
    )
}

enum class TabItems {
    TRANSFERS,
    ASSETS
}

@Composable
@Preview
fun PreviewWalletTabRow() {
    Column {
        WalletTabRow(
            TransfersUiState.Loading,
            AssetUiState.Loading,
            false,
            {}
        )
    }
}

@Composable
@Preview
fun PreviewEmptyWalletTabRow() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        WalletTabRow(
            TransfersUiState.Success(emptyList()),
            AssetUiState.Empty,
            false,
            {}
        )
    }
}
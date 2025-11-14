package com.feature.send.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAssetWithPrice
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.lazerCore
import com.feature.send.AssetsUiState
import com.feature.send.R
import com.feature.send.SelectedAssetUiState
import kotlin.text.uppercase

@Composable
fun NetworkSelector(
    modifier: Modifier,
    itemWidth: Dp = 200.dp,
    itemHeight: Dp = 150.dp,
    assetsUiState: AssetsUiState,
    selectedAssetUiState: SelectedAssetUiState,
    onNetworkSelected: (chainId: Int) -> Unit
) {

    val listState = rememberLazyListState()

    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    val selectedAsset = when(selectedAssetUiState) {
        is SelectedAssetUiState.Selected -> selectedAssetUiState.tokenAsset.chainId
        SelectedAssetUiState.Unselected -> 0
    }

    LazyRow(
        modifier = modifier,
        state = listState,
        flingBehavior = remember {
            object : FlingBehavior {
                override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                    return 0f
                }
            }
        },
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when(assetsUiState) {

            is AssetsUiState.Success -> {
                itemsIndexed(assetsUiState.assets) { index, item ->
                    val isSelected = item.chainId == selectedAsset

                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) primaryColor else secondaryColor
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .width(itemWidth)
                            .height(itemHeight)
                            .clickable { onNetworkSelected(item.chainId) }
                    ) {
                        Column(
                            modifier = Modifier.fillParentMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val imageModifier = Modifier
                                .size(34.dp)
                                .then(
                                    if (primaryColor == lazerCore && item.chainId == 8453) {
                                        Modifier.border(
                                            1.dp, secondaryColor,
                                            RoundedCornerShape(2.dp)
                                        )
                                    }
                                    else Modifier
                                )

                            Image(
                                painter = painterResource(networkSymbolResolver(item.chainId)),
                                modifier = imageModifier,
                                contentDescription = item.symbol
                            )

                            Text(
                                text = networkNameResolver(item.chainId),
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                ),
                                color = if (isSelected) secondaryColor
                                else primaryColor
                            )

                        }
                    }
                }
            }
            else -> {}
        }
    }
}

private fun networkSymbolResolver(chainId: Int): Int {
    return when(chainId) {
        1 -> R.drawable.mainnet
        10 -> R.drawable.optimism
        137 -> R.drawable.polygon
        56 -> R.drawable.bnb
        8453 -> R.drawable.base_square
        42161 -> R.drawable.arbitrum
        43114 -> R.drawable.avalanche
        7777777 -> R.drawable.zorb
        else -> R.drawable.mainnet //TODO change to something different?
    }
}

private fun networkNameResolver(chainId: Int): String {
    return when(chainId) {
        1 -> "MAIN"
        10 -> "OP"
        137 -> "POL"
        56 -> "BNB"
        8453 -> "BASE"
        42161 -> "ARB"
        43114 -> "AVAX"
        7777777 -> "ZORA"
        else -> "MAINNET" //TODO change to something different?
    }
}

@Preview(name = "Network Selector - BNB", showBackground = true)
@Composable
private fun NetworkSelectorPreview_BNB() {
    val assets = sampleNetworkAssets()
    NetworkSelector(
        modifier = Modifier.fillMaxWidth(),
        itemWidth = 120.dp,
        itemHeight = 100.dp,
        assetsUiState = AssetsUiState.Success(assets),
        selectedAssetUiState = SelectedAssetUiState.Selected(
            assets.first { it.chainId == 56 }
        ),
        onNetworkSelected = {}
    )
}

@Preview(name = "Network Selector - AVAX", showBackground = true)
@Composable
private fun NetworkSelectorPreview_AVAX() {
    val assets = sampleNetworkAssets()
    NetworkSelector(
        modifier = Modifier.fillMaxWidth(),
        itemWidth = 120.dp,
        itemHeight = 100.dp,
        assetsUiState = AssetsUiState.Success(assets),
        selectedAssetUiState = SelectedAssetUiState.Selected(
            assets.first { it.chainId == 43114 }
        ),
        onNetworkSelected = {}
    )
}

private fun sampleNetworkAssets(): List<TokenAssetWithPrice> {
    return listOf(
//        TokenAssetWithPrice(
//            address = "0x0000000000000000000000000000000000000000",
//            chainId = 1,
//            symbol = "ETH",
//            name = "Ethereum",
//            balance = 0.0,
//            decimals = 18,
//            logoUrl = null,
//            swappable = false,
//            fiatAmount = 0.0
//        ),
//        TokenAssetWithPrice(
//            address = "0x0000000000000000000000000000000000000000",
//            chainId = 10,
//            symbol = "OP",
//            name = "Optimism",
//            balance = 0.0,
//            decimals = 18,
//            logoUrl = null,
//            swappable = false,
//            fiatAmount = 0.0
//        ),
//        TokenAssetWithPrice(
//            address = "0x0000000000000000000000000000000000000000",
//            chainId = 137,
//            symbol = "POL",
//            name = "Polygon",
//            balance = 0.0,
//            decimals = 18,
//            logoUrl = null,
//            swappable = false,
//            fiatAmount = 0.0
//        ),
        TokenAssetWithPrice(
            address = "0x0000000000000000000000000000000000000000",
            chainId = 56,
            symbol = "BNB",
            name = "Binance",
            balance = 0.0,
            decimals = 18,
            logoUrl = null,
            swappable = false,
            fiatAmount = 0.0
        ),
        TokenAssetWithPrice(
            address = "0x0000000000000000000000000000000000000000",
            chainId = 8453,
            symbol = "BASE",
            name = "Base",
            balance = 0.0,
            decimals = 18,
            logoUrl = null,
            swappable = false,
            fiatAmount = 0.0
        ),
        TokenAssetWithPrice(
            address = "0x0000000000000000000000000000000000000000",
            chainId = 43114,
            symbol = "AVAX",
            name = "Avalanche",
            balance = 0.0,
            decimals = 18,
            logoUrl = null,
            swappable = false,
            fiatAmount = 0.0
        ),
        TokenAssetWithPrice(
            address = "0x0000000000000000000000000000000000000000",
            chainId = 7777777,
            symbol = "ZORA",
            name = "Zora",
            balance = 0.0,
            decimals = 18,
            logoUrl = null,
            swappable = false,
            fiatAmount = 0.0
        )
    )
}

package com.feature.home.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.zIndex
import com.core.model.TokenAsset
import com.core.model.TokenData
import com.core.model.TokenMetadata
import com.core.ui.Card
import com.core.ui.views.IdleView
import com.example.dgenlibrary.ui.theme.largeEnterDuration
import com.example.dgenlibrary.ui.theme.smallDuration
import com.feature.send.SelectedTokenUiState
import kotlin.collections.find
import kotlin.math.abs
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.SnapOffsets
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalSnapperApi::class)
@SuppressLint("RestrictedApi")
@Composable
fun TokenCardCarousel(
    assets: List<TokenAsset>,
    tokenData: List<TokenData>,
    loadSymbol: (List<String>) -> Unit,
    selectedTokenUiState: SelectedTokenUiState,
    sharedTransitionScope: SharedTransitionScope,
    navigateToSend: (address: String, tokenId: String) -> Unit,
    animatedContentScope: AnimatedContentScope,
    setSelectedToken: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var savedScrollIndex by rememberSaveable { mutableStateOf(0) }
    var savedScrollOffset by rememberSaveable { mutableStateOf(0) }
    var autoScrollDone by rememberSaveable { mutableStateOf(false) }
    var isUserScrolling by remember { mutableStateOf(false) }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = savedScrollIndex,
        initialFirstVisibleItemScrollOffset = savedScrollOffset
    )

    LaunchedEffect(listState.isScrollInProgress, isUserScrolling) {
        if (!listState.isScrollInProgress && isUserScrolling) {
            savedScrollIndex = listState.firstVisibleItemIndex
            savedScrollOffset = listState.firstVisibleItemScrollOffset
            Log.d("ScrollSave", "User scroll FINISHED. Saved: index=${savedScrollIndex}, offset=${savedScrollOffset}")
            isUserScrolling = false
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val listofTokenSymbol = remember { mutableListOf<String>() }
    var scrollJob by remember { mutableStateOf<Job?>(null) }

    Log.d("TokenCardCarousel", "Recomposing. autoScrollDone: $autoScrollDone. Assets: ${assets.size}. Selected: $selectedTokenUiState. Saved: $savedScrollIndex @ $savedScrollOffset. UserScrolling: $isUserScrolling")

    LaunchedEffect(assets, selectedTokenUiState) {
        Log.d("TokenCardCarousel", "Effect: Assets/SelectedToken changed. Assets: ${assets.size}, autoScrollDone: $autoScrollDone, Selected: $selectedTokenUiState")
        if (assets.isNotEmpty()) {
            if (!autoScrollDone) {
                autoScrollDone = true
                Log.d("TokenCardCarousel", "Attempting auto-scroll, autoScrollDone set to true immediately.")
                scrollJob?.cancel()
                isUserScrolling = false
                val targetToken = when (selectedTokenUiState) {
                    is SelectedTokenUiState.Unselected -> assets.lastOrNull()
                    is SelectedTokenUiState.Selected -> {
                        assets.find { it.address == selectedTokenUiState.tokenAsset.address && it.chainId == selectedTokenUiState.tokenAsset.chainId }
                            ?: assets.lastOrNull()
                    }
                }

                targetToken?.let { token ->
                    val targetIndex = assets.indexOf(token)
                    if (targetIndex != -1) {
                        Log.d("TokenCardCarousel", "Auto-scrolling to ${token.symbol} at index $targetIndex")
                        scrollJob = coroutineScope.launch {
                            listState.scrollToItem(targetIndex)
                            savedScrollIndex = targetIndex
                            savedScrollOffset = 0
                            Log.d("ScrollSave", "Auto-scroll COMPLETED & SAVED. New saved: index=$savedScrollIndex, offset=$savedScrollOffset.")
                            setSelectedToken(token.address)
                        }
                    } else {
                        Log.d("TokenCardCarousel", "Auto-scroll target token not found in assets.")
                    }
                } ?: run {
                    Log.d("TokenCardCarousel", "No target token for auto-scroll.")
                }
            }

            listofTokenSymbol.clear()
            assets.forEach { asset ->
                val symbolToAdd = when (asset.symbol.lowercase()) {
                    "base", "arbitrum", "mainnet", "polygon", "sepolia", "optimism", "zora" -> "ETH"
                    else -> asset.symbol
                }
                if (!listofTokenSymbol.contains(symbolToAdd)) {
                    listofTokenSymbol.add(symbolToAdd)
                }
            }
            if (listofTokenSymbol.isNotEmpty()) {
                Log.d("TokenCardCarousel", "Loading symbols: $listofTokenSymbol")
                loadSymbol(listofTokenSymbol.distinct())
            }
        }
    }

    val cardHeight  = 400.dp
    val visibleCount= 5
    val overlap     = (-cardHeight / visibleCount) *4     // -50.dp
    val clampRange  = (visibleCount - 1).toFloat()     // 3f

    val sensitivity = 0.2f

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag) {
                    if (!isUserScrolling) {
                        Log.d("ScrollState", "User scroll STARTED via Drag")
                        isUserScrolling = true
                        scrollJob?.cancel()
                    }
                }
                val scaledY = available.y * sensitivity
                if (isUserScrolling) {
                    coroutineScope.launch {
                        listState.scrollBy(scaledY)
                    }
                    return Offset(x = 0f, y = scaledY)
                }
                return Offset.Zero
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .offset(0.dp,15.dp)
            .nestedScroll(nestedScrollConnection)
            .zIndex(3f),
        verticalArrangement = Arrangement.spacedBy(overlap-32.dp),
        contentPadding = PaddingValues(top = 72.dp, bottom = 16.dp)
    ) {
        itemsIndexed(assets, key = { _, asset -> asset.address + "_" + asset.chainId }) { index, item ->
            val rotX: Float by animateFloatAsState ( -25f , label = "rotX")

            val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
            val isFirstCard = index == firstVisibleIndex

            LaunchedEffect(isFirstCard, item.address, listState.isScrollInProgress) {
                if (isFirstCard && !listState.isScrollInProgress && !isUserScrolling) {
                    Log.d("FirstCard", "Card $index (${item.symbol}) is first & settled. setSelectedToken.")
                    setSelectedToken(item.address)
                }
            }

            val firstVisibleOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
            val scrollOffset = firstVisibleIndex + firstVisibleOffset / 1000f
            val relIdx = (index - scrollOffset).coerceIn(-clampRange, clampRange)

            val scale by animateFloatAsState(
                targetValue = when {
                    abs(relIdx) <= 0.5f       -> 0.8f
                    abs(relIdx) <= clampRange -> lerp(0.8f, 0.55f, (abs(relIdx)-0.5f)/(clampRange-0.5f))
                    else                      -> 0.55f
                },
                animationSpec = tween(smallDuration, easing = FastOutSlowInEasing), label = "scaleAnimation"
            )

            val alphafactor by animateFloatAsState(
                targetValue = when {
                    abs(relIdx) <= 0.5f       -> 1f
                    abs(relIdx) <= clampRange -> lerp(1f, 0f, (abs(relIdx)-0.5f)/(clampRange-0.5f))
                    else                      -> 0f
                },
                animationSpec = tween(smallDuration, easing = FastOutSlowInEasing), label = "alphaAnimation"
            )

            val frontCardTranslation by animateFloatAsState(
                targetValue = lerp(0f, 800f, (relIdx / 2).coerceIn(0f, 1f)),
                animationSpec = tween(durationMillis = largeEnterDuration, easing = FastOutSlowInEasing), label = "translationAnimation"
            )

            val fiatamount = when(item.symbol.lowercase()) {
                "base", "arbitrum", "mainnet", "polygon", "sepolia", "optimism", "zora" -> {
                    tokenData.find { it.symbol == "ETH" }?.prices?.firstOrNull()?.value?.toDoubleOrNull() ?: 0.0
                }
                else -> {
                    tokenData.find { it.symbol == item.symbol }?.prices?.firstOrNull()?.value?.toDoubleOrNull() ?: 0.0
                }
            }

            with(sharedTransitionScope) {
                Card(
                    isFirst = isFirstCard,
                    modifier = Modifier
                        .height(cardHeight)
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = alphafactor
                            rotationX = rotX
                            translationY = frontCardTranslation
                            cameraDistance = 32f * density
                        },
                    frontSide = {
                        val tokenName = if (item.name.equals(item.symbol, ignoreCase = true) && item.symbol.equals("ETH", ignoreCase=true)) {
                            if (item.name.isNotEmpty() && !item.name.equals(item.symbol, ignoreCase = true)) item.name else item.symbol
                        } else if (item.name.equals(item.symbol, ignoreCase = true)) {
                            item.symbol
                        } else {
                            item.symbol
                        }

                        IdleView(
                            amount = item.balance,
                            tokenName = tokenName,
                            fiatAmount = item.balance * fiatamount,
                            icon = item.logoUrl,
                            navigateToSend = {
                                navigateToSend(item.address, item.address)
                            },
                            enableSend = item.balance > 0
                        )
                    },
                )
            }
        }
    }
}



/*
TESTDATA

TokenCardCarousel(
                            modifier = Modifier.padding(bottom = 24.dp),
                            assets = testTokenAssets,
                            tokenData = tokenData,
                            tokenMetadata = tokenMetadata,
                            loadSymbol = loadSymbol,
                            navigateToSend = navigateToSend,
                            selectedTokenUiState = selectedTokenUiState,
                            setSelectedToken = setSelectedTokenId,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedContentScope = animatedContentScope,
                        )

val testTokenAssets = listOf(
        TokenAsset(
            address   = "0x6B175474E89094C44Da98b954EedeAC495271d0F",
            chainId   = 1,
            symbol    = "DAI",
            name      = "Dai Stablecoin",
            balance   = 1234.575878756,
            decimals  = 18,
            logoUrl   = "https://cryptologos.cc/logos/multi-collateral-dai-dai-logo.png",
            swappable = true
        ),
        TokenAsset(
            address   = "0xdAC17F958D2ee523a2206206994597C13D831ec7",
            chainId   = 1,
            symbol    = "USDT",
            name      = "Tether USD",
            balance   = 7890.12857787857,
            decimals  = 6,
            logoUrl   = "https://cryptologos.cc/logos/tether-usdt-logo.png",
            swappable = true
        ),
        TokenAsset(
            address   = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
            chainId   = 1,
            symbol    = "USDC",
            name      = "USD Coin",
            balance   = 311541645.88279937477,
            decimals  = 6,
            logoUrl   = "https://cryptologos.cc/logos/usd-coin-usdc-logo.png",
            swappable = true
        ),
        TokenAsset(
            address   = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
            chainId   = 1,
            symbol    = "WETH",
            name      = "Wrapped Ether",
            balance   = 2.378785752745,
            decimals  = 18,
            logoUrl   = "https://cryptologos.cc/logos/wrapped-ether-weth-logo.png",
            swappable = true
        ),
        TokenAsset(
            address   = "0x55d398326f99059fF775485246999027B3197955",
            chainId   = 56,
            symbol    = "USDT",
            name      = "Tether USD",
            balance   = 10156234.3757878,
            decimals  = 18,
            logoUrl   = "https://cryptologos.cc/logos/tether-usdt-logo.png",
            swappable = true
        ),
        TokenAsset(
            address   = "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
            chainId   = 1,
            symbol    = "ETH",
            name      = "Ether",
            balance   = 0.4718777823,
            decimals  = 18,
            logoUrl   = null,
            swappable = false
        ),
        TokenAsset(
            address   = "0x7D1Afa7B718fb893dB30A3abc0Cfc608AaCfeBB0",
            chainId   = 137,
            symbol    = "MATIC",
            name      = "Polygon",
            balance   = 1_234_567_890_123.0,
            decimals  = 18,
            logoUrl   = "https://cryptologos.cc/logos/polygon-matic-logo.png",
            swappable = true
        ),
        TokenAsset(
            address   = "0x4fabb145d64652a948d72533023f6e7a623c7c53",
            chainId   = 1,
            symbol    = "BUSD",
            name      = "Binance USD",
            balance   = 5500.5875870,
            decimals  = 18,
            logoUrl   = "https://cryptologos.cc/logos/binance-usd-busd-logo.png",
            swappable = false
        ),
        TokenAsset(
            address   = "0x0000000000085d4780B73119b644AE5ecd22b376",
            chainId   = 1,
            symbol    = "TUSD",
            name      = "TrueUSD",
            balance   = 250.2867857865,
            decimals  = 18,
            logoUrl   = "https://cryptologos.cc/logos/trueusd-tusd-logo.png",
            swappable = true
        ),
        TokenAsset(
            address   = "0x9f8F72aA9304c8B593d555F12eF6589cC3A579A2",
            chainId   = 1,
            symbol    = "MKR",
            name      = "Maker",
            balance   = 0.587587875,
            decimals  = 18,
            logoUrl   = "https://cryptologos.cc/logos/maker-mkr-logo.png",
            swappable = false
        )
    )
 */
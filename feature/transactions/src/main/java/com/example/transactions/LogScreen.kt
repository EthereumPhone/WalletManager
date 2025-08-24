package com.example.transactions

import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.core.ui.util.dgenBlack
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenOcean
import com.core.ui.util.dgenTurqoise
import com.example.transactions.ui.LogEntry
import kotlinx.coroutines.delay
import kotlin.random.Random
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.core.model.TokenMetadata
import com.core.ui.DgenLoadingMatrix
import com.core.ui.HeaderBar
import com.core.ui.util.PitagonsSans
import com.core.ui.util.dgenGunMetal
import com.core.ui.util.extraLargeEnterDuration
import com.core.ui.util.extraLargeExitDuration
import kotlin.math.max 
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch

@Composable
fun LogRoute(
    navigateBack: () -> Unit,
    tokenId: String?,
    onTransactionClick: (String) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
){
    val transfersUIState: TransfersUiState by viewModel.transferState.collectAsStateWithLifecycle()
    val refreshState by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val tokenMetadata by viewModel.tokenMetadata.collectAsStateWithLifecycle()
    val userData by viewModel.userData.collectAsStateWithLifecycle()

    // Track if user is navigating back to home
    // Set to false by default to ensure LED is always cleared
    var isNavigatingBack by remember { mutableStateOf(false) }
    
    // Add navigation state to prevent multiple navigation calls
    var isNavigating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Debounced navigation function
    val safeNavigateBack: () -> Unit = remember {
        {
            if (!isNavigating) {
                isNavigating = true
                isNavigatingBack = false
                navigateBack()
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenOpenedAfterResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Don't clear LED if navigating back to home screen
            viewModel.onLogClosed(clearLed = !isNavigatingBack)
        }
    }

    // Handle device back button press
    BackHandler {
        // Use safe navigation to prevent multiple calls
        safeNavigateBack()
    }

    LogScreen(
        transfersUIState = transfersUIState,
        onNavigateBack = safeNavigateBack,
        refreshState = refreshState,
        tokenMetadata = tokenMetadata,
        tokenId = tokenId,
        onTransactionClick = onTransactionClick,
        //onRefresh = viewModel::refreshData
    )
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LogScreen(
    modifier: Modifier = Modifier,
    transfersUIState: TransfersUiState,
    tokenMetadata: List<TokenMetadata>,
    onNavigateBack: () -> Unit = {},
    refreshState: Boolean,
    tokenId: String?,
    onTransactionClick: (String) -> Unit,
    //onRefresh: () -> Unit,
){
    Log.d("LogScreen", "LogScreen displayed with tokenId: $tokenId")

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        SystemColorManager.refresh(context)
    }

    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    val scrollState = rememberLazyListState()

    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if ( SDK_INT >= 28 ) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
            .statusBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HeaderBar(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = "ACTIVITY LOG",
            onClick = onNavigateBack,
            primaryColor = primaryColor
        )

        Box(modifier = Modifier.fillMaxSize()){
            AnimatedContent(
                transfersUIState,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(extraLargeEnterDuration)
                    ) togetherWith fadeOut(animationSpec = tween(extraLargeExitDuration))
                },
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                label = "Animated Content"
            ) { txState ->
                when(txState){
                    is TransfersUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            DgenLoadingMatrix(activeLEDColor = primaryColor, unactiveLEDColor = secondaryColor)
                        }
                    }
                    is TransfersUiState.Success -> {
                        val transfers = txState.transfers

                        if (transfers.isNotEmpty()){
                            val metaBySymbol = remember(tokenMetadata) {
                                tokenMetadata.associateBy { it.symbol }
                            }

                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                LazyColumn(
                                    state= scrollState,
                                    modifier = Modifier
                                            .verticalLazyListScrollbar(
                                                scrollState,
                                                scrollBarTrackColor = secondaryColor,
                                                scrollBarColor = primaryColor,
                                            ) // Apply the scrollbar first
                                        .fillMaxSize()
                                        ,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    item {
                                        Spacer(Modifier.height(8.dp))
                                    }

                                    items(transfers.reversed()) { transfer ->
                                        //TODO: Add Logos
                                        Log.d("LogScreen", "transfer.asset ${transfer.txHash} logoUrl ${metaBySymbol[transfer.asset]?.logo ?: ""}")

                                        LogEntry(
                                            logEntry = transfer,
                                            primaryColor = primaryColor,
                                            logoUrl = metaBySymbol[transfer.asset]?.logo ?: "",
                                            onNavigateToDetail = onTransactionClick
                                        )
                                    }

                                    item {
                                        Spacer(Modifier.height(16.dp))
                                    }
                                }


                            }


                        }else{
                            Box(
                                modifier = modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ){
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                    modifier = Modifier.offset(y= 0.dp)
                                ) {
                                    AsyncImage(
                                        imageLoader = gifEnabledLoader,
                                        model = com.core.ui.R.drawable.wireframe_torus,
                                        contentDescription = null,
                                        modifier = Modifier.size(275.dp),
                                        colorFilter = ColorFilter.tint(dgenGunMetal)
                                    )

                                    Text(
                                        text = "Complete your first transaction, or add assets from another wallet.",
                                        style = TextStyle(
                                            fontFamily = PitagonsSans,
                                            color = dgenGunMetal,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            letterSpacing = 0.sp,
                                            textDecoration = TextDecoration.None,
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier.width(300.dp)
                                    )
                                }

                            }
                        }
                    }
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(dgenBlack, Color.Transparent)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(32.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, dgenBlack)
                        )
                    )
            )
        }

    }

}


@Preview(
    showBackground = true,
    widthDp = 447,
    heightDp = 447,
)
@Composable
fun LogViewPreview(){

    val tokenAssets = listOf(
        TokenAsset(
            address = "0x6B175474E89094C44Da98b954EedeAC495271d0F",
            chainId = 1,
            symbol = "DAI",
            name = "Dai Stablecoin",
            balance = 1534.25,
            decimals = 18,
            logoUrl = "https://cryptologos.cc/logos/multi-collateral-dai-dai-logo.png",
            swappable = true
        ),
        TokenAsset(
            address = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
            chainId = 1,
            symbol = "mainnet",
            name = "mainnet",
            balance = 0.753,
            decimals = 18,
            logoUrl = "https://cryptologos.cc/logos/wrapped-ether-weth-logo.png",
            swappable = true
        ),
        TokenAsset(
            address = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
            chainId = 1,
            symbol = "USDC",
            name = "USD Coin",
            balance = 10420.10,
            decimals = 6,
            logoUrl = "https://cryptologos.cc/logos/usd-coin-usdc-logo.png",
            swappable = true
        ),
        TokenAsset(
            address = "0xdAC17F958D2ee523a2206206994597C13D831ec7",
            chainId = 1,
            symbol = "USDT",
            name = "Tether USD",
            balance = 256.75,
            decimals = 6,
            logoUrl = "https://cryptologos.cc/logos/tether-usdt-logo.png",
            swappable = true
        ),
        TokenAsset(
            address = "0x514910771AF9Ca656af840dff83E8264EcF986CA",
            chainId = 1,
            symbol = "LINK",
            name = "Chainlink",
            balance = 89.34,
            decimals = 18,
            logoUrl = "https://cryptologos.cc/logos/chainlink-link-logo.png",
            swappable = true
        )
    )

    val txs = generateRandomTransfers()

    val sampleMetadata = listOf(
        TokenMetadata(
            contractAddress = "0x000…eth",
            decimals        = 18,
            name            = "Ether",
            symbol          = "ETH",
            logo            = "https://cryptologos.cc/logos/ethereum-eth-logo.png",
            chainId         = 1
        ),
        TokenMetadata(
            contractAddress = "0x000…usdt",
            decimals        = 6,
            name            = "Tether USD",
            symbol          = "USDT",
            logo            = "https://cryptologos.cc/logos/tether-usdt-logo.png",
            chainId         = 56
        ),
        TokenMetadata(
            contractAddress = "0x000…usdt",
            decimals        = 6,
            name            = "LINK",
            symbol          = "LINK",
            logo            = "https://cryptologos.cc/logos/chainlink-link-logo.png",
            chainId         = 56
        )
    )

    LogScreen(
        transfersUIState = TransfersUiState.Success(txs),
        refreshState = false,
        tokenId = "DAI",
        //onRefresh = {},
        tokenMetadata = sampleMetadata,
        onTransactionClick = {}
    )

}
/*
@Preview(name = "Long Token Names Test", showBackground = true)
@Composable
fun LogScreenLongTokenNamesPreview() {
    // Generate mock transfers with very long token names to test the fade effect
    val longTokenTransfers = listOf(
        TransferItem(
            chainId = 1,
            from = "0x742d35Cc6634C0532925a3b844Bc9e7595f89999",
            to = "0x742d35Cc6634C0532925a3b844Bc9e7595f89000",
            asset = "VERYLONGTOKENNAMETHATSHOULDFADEINTOSCROLLBAR",
            value = "123.456",
            timeStamp = "2024-01-01 10:00:00",
            userSent = true,
            txHash = "0xabc123"
        ),
        TransferItem(
            chainId = 1,
            from = "0x123456789012345678901234567890123456789012",
            to = "0x987654321098765432109876543210987654321098",
            asset = "ANOTHERLONGTOKENNAMEFORFADETESTING-SCROLLBAR",
            value = "789.012",
            timeStamp = "2024-01-01 09:00:00",
            userSent = false,
            txHash = "0xdef456"
        ),
        TransferItem(
            chainId = 137,
            from = "longusername.eth",
            to = "anotherlongusername.eth",
            asset = "SUPERLONGPOLYGONTOKENNAMETESTINGFADEEFFECT",
            value = "1000.0",
            timeStamp = "2024-01-01 08:00:00",
            userSent = true,
            txHash = "0xghi789"
        ),
        // Add more transfers to test scrollbar
        TransferItem(
            chainId = 1,
            from = "0x111",
            to = "0x222",
            asset = "SHORTTOKEN",
            value = "50.0",
            timeStamp = "2024-01-01 07:00:00",
            userSent = true,
            txHash = "0x111"
        ),
        TransferItem(
            chainId = 1,
            from = "0x333",
            to = "0x444",
            asset = "VERYLONGTOKENNAMETHATEXCEEDSTHEBOUNDARIESOFTHESCREENWIDTHANDSHOULDFADE",
            value = "75.0",
            timeStamp = "2024-01-01 06:00:00",
            userSent = false,
            txHash = "0x222"
        ),
        // Add more items to ensure scrollbar appears
        *generateRandomTransfers().toTypedArray()
    )
    
    LogScreen(
        transfersUIState = TransfersUiState.Success(longTokenTransfers),
        tokenMetadata = listOf(),
        refreshState = false,
        tokenId = null,
        onTransactionClick = {}
    )
}
*/


//method for testing
@Composable
fun generateRandomTransfers(): List<TransferItem> {
    val random = Random(System.currentTimeMillis())

    // Beispielhafte Listen für zufällige Werte
    val possibleChainIds = listOf(1, 56, 137, 42)  // z.B. Ethereum, BSC, Polygon, Kovan
    val possibleAssets = listOf("LINK","USDT","ETH")

    // Beispiel-Adressen (typisch 0x + 40 Hex-Stellen, hier verkürzt oder zufällig generiert)
    val sampleAddresses = listOf(
        "emunsi.eth",
        "0x4e83362442B8d1beC281594cEa3050c8EB01311C",
        "0xC0fFee0000000000000000000000000000000000",
        "0x7Bb4fC5D2f9afE98Ed7be9cEB49F2C4dA333b0B3",
        "0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB",
        "0x1111111254EEB25477B68fb85Ed929f73A960582"
    )

    // Beispiel-Zeitstempel im Format YYYY-MM-dd HH:mm:ss, hier stark vereinfacht
    val sampleTimeStamps = listOf(
        "2023-01-10 14:25:13",
        "2023-02-11 09:41:22",
        "2023-03-15 22:07:59",
        "2023-03-16 01:33:45",
        "2023-03-17 16:00:00"
    )

    // Beispiel-Hashes (typisch 0x + 64 Hex-Stellen)
    val sampleTxHashes = listOf(
        "0xaaaabbbbccccddddeeeeffff1111222233334444555566667777888899990000",
        "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
        "0xbad00bad00bad00bad00bad00bad00bad00bad00bad00bad00bad00bad00bad0",
        "0x7777777777777777777777777777777777777777777777777777777777777777",
        "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
    )

    // Erzeuge mindestens 10 Einträge
    return List(10) {
        TransferItem(
            chainId = possibleChainIds.random(random),
            from = sampleAddresses.random(random),
            to = sampleAddresses.random(random),
            asset = possibleAssets.random(random),
            value = (random.nextInt(1000, 10000) + random.nextDouble()).toString(),
            timeStamp = sampleTimeStamps.random(random),
            userSent = random.nextBoolean(),
            txHash = sampleTxHashes.random(random),
        )
    }
}



@Composable
fun Modifier.verticalLazyListScrollbar(
    lazyListState: LazyListState,
    width: Dp = 6.dp,
    showScrollBarTrack: Boolean = true,
    scrollBarTrackColor: Color = dgenOcean,
    scrollBarColor: Color = dgenTurqoise,
    scrollBarCornerRadius: Float = 4f,
    trackTopInset: Dp = 32.dp,
    trackBottomInset: Dp = 32.dp,
    endPadding: Dp = 12.dp,
    minThumbHeight: Dp = 40.dp,
    verticalOffset: Dp = 0.dp
): Modifier {
    val layoutInfo = lazyListState.layoutInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex
    val firstVisibleItemScrollOffset = lazyListState.firstVisibleItemScrollOffset
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    val viewportHeightPx = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset)
        .toFloat()
        .coerceAtLeast(1f)

    // Smooth, stable per-item height estimate to avoid jumping thumb size/position
    var smoothedItemHeightPx by remember(totalItemsCount) { mutableStateOf<Float?>(null) }
    val currentVisibleAverage = if (visibleItemsInfo.isNotEmpty()) {
        visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItemsInfo.size
    } else null
    if (currentVisibleAverage != null) {
        smoothedItemHeightPx = when (val prev = smoothedItemHeightPx) {
            null -> currentVisibleAverage
            else -> prev + (currentVisibleAverage - prev) * 0.15f
        }
    }
    val itemHeightEstimatePx = smoothedItemHeightPx ?: viewportHeightPx

    val contentHeightEstimatePx = (itemHeightEstimatePx * totalItemsCount)
        .coerceAtLeast(viewportHeightPx)
    val scrolledOffsetPx = (firstVisibleItemIndex * itemHeightEstimatePx) + firstVisibleItemScrollOffset
    val maxScrollPx = (contentHeightEstimatePx - viewportHeightPx).coerceAtLeast(1f)
    var progressFraction = (scrolledOffsetPx / maxScrollPx).coerceIn(0f, 1f)

    // Force exact ends when at the start or when the last item is fully visible
    if (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset <= 0) {
        progressFraction = 0f
    } else if (visibleItemsInfo.isNotEmpty()) {
        val lastVisible = visibleItemsInfo.last()
        val lastVisibleBottom = (lastVisible.offset + lastVisible.size).toFloat()
        if (lastVisible.index >= totalItemsCount - 1 && lastVisibleBottom <= viewportHeightPx + 0.5f) {
            progressFraction = 1f
        }
    }

    return this.then(
        Modifier.drawWithContent {
            drawContent()

            if (totalItemsCount == 0) return@drawWithContent

            // Track geometry (right side with end padding)
            val widthPx = width.toPx()
            val endPaddingPx = endPadding.toPx()
            val xRight = size.width - endPaddingPx - widthPx
            val yShiftPx = verticalOffset.toPx()
            val trackStartY = trackTopInset.toPx() + yShiftPx
            val trackEndY = (size.height - trackBottomInset.toPx() + yShiftPx)
                .coerceAtLeast(trackStartY)
            val trackHeight = (trackEndY - trackStartY).coerceAtLeast(0f)

            // Pill shape: ensure fully rounded ends regardless of provided corner radius
            val effectiveCorner = max(scrollBarCornerRadius, widthPx / 2f)
            val cornerRadius = CornerRadius(effectiveCorner)

            // Thumb size: proportional to viewport vs content; clamped to min height
            val thumbFractionOfTrack = (viewportHeightPx / contentHeightEstimatePx)
                .coerceIn(0.06f, 1f)
            val thumbHeightPx = (trackHeight * thumbFractionOfTrack)
                .coerceAtLeast(minThumbHeight.toPx())
                .coerceAtMost(trackHeight)

            // Thumb position mapped smoothly to the full track range
            val maxThumbOffset = (trackHeight - thumbHeightPx).coerceAtLeast(0f)
            val thumbOffsetYPx = (progressFraction * maxThumbOffset)
                .coerceIn(0f, maxThumbOffset)

            if (showScrollBarTrack) {
                drawRoundRect(
                    color = scrollBarTrackColor,
                    cornerRadius = cornerRadius,
                    topLeft = Offset(xRight, trackStartY),
                    size = Size(widthPx, trackHeight)
                )
            }

            drawRoundRect(
                color = scrollBarColor,
                cornerRadius = cornerRadius,
                topLeft = Offset(xRight, trackStartY + thumbOffsetYPx),
                size = Size(widthPx, thumbHeightPx)
            )
        }
    )
}





package com.example.transactions

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGray
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import com.example.transactions.ui.LogEntry
import com.example.transactions.ui.TxEntry
import com.example.transactions.ui.TxType
import kotlinx.coroutines.delay
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.walletmanager.ethOSTransferListItem
import kotlin.random.Random

@Composable
fun LogRoute(
    navigateBack: () -> Unit,
    tokenId: String?,
    viewModel: TransactionViewModel = hiltViewModel()
){
    val transfersUIState: TransfersUiState by viewModel.transferState.collectAsStateWithLifecycle()
    val refreshState by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val tokenAssetUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()

    LogScreen(
        transfersUIState = transfersUIState,
        onNavigateBack = navigateBack,
        refreshState = refreshState,
        tokenId = tokenId,
        onRefresh = viewModel::refreshData
    )


//    val txs = generateRandomTransfers()
//
//    LogScreen(
//        transfersUIState = TransfersUiState.Success(txs),
//        onNavigateBack = navigateBack,
//        refreshState = false,
//        tokenId = tokenId,
//        tokenAssetUiState = tokenAssetUiState,
//        onRefresh = {}
//    )
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LogScreen(
    modifier: Modifier = Modifier,
    transfersUIState: TransfersUiState,
    onNavigateBack: () -> Unit = {},
    refreshState: Boolean,
    tokenId: String?,
    onRefresh: () -> Unit,
){

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshState,
        onRefresh = {
            onRefresh()
        }
    )

    val scrollState = rememberLazyListState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)

    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    end = 24.dp,
                    start = 24.dp, top = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){



            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                Text(
                    text = "ACTIVITY LOG",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )
            }
                Icon(
                    modifier = Modifier.size(32.dp).pointerInput(Unit){
                        detectTapGestures {
                            onNavigateBack()
                        }
                    },
                    painter = painterResource(R.drawable.baseline_close_24),
                    contentDescription = "Back",
                    tint = dgenTurqoise
                )

        }



        Box(modifier = Modifier.fillMaxSize()){

            when(transfersUIState){
                is TransfersUiState.Loading -> {

                }
                is TransfersUiState.Success -> {

                    val transfers = transfersUIState.transfers.filter {
                        it.asset == tokenId
                    }


                    if (transfers.isNotEmpty()){

                        Box(
                            Modifier
                                .fillMaxSize()
                                .pullRefresh(pullRefreshState)
                        ) {
                            LazyColumn(
                                state= scrollState,
                                modifier = Modifier
                                    .verticalLazyListScrollbar(scrollState) // Apply the scrollbar first
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Spacer(Modifier.height(8.dp))
                                }

                                items(transfers.reversed()) { transfer ->
                                    //TODO: Add Logos
                                    LogEntry(logEntry = transfer)
                                }

                                item {
                                    Spacer(Modifier.height(16.dp))
                                }
                            }

                            PullRefreshIndicator(
                                refreshing = refreshState,
                                state = pullRefreshState,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }


                    }else{
                        Box(
                            modifier = modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    modifier = Modifier.size(82.dp),
                                    contentScale = ContentScale.Fit,
                                    painter = painterResource(id = R.drawable.baseline_swap_vert_24),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(dgenTurqoise)
                                )
                                Text(
                                    text = "No transactions".uppercase(),
                                    style = TextStyle(
                                        fontFamily = SpaceMono,
                                        color = dgenTurqoise,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 24.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None
                                    )
                                )

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


    LogScreen(
        transfersUIState = TransfersUiState.Success(txs),
        refreshState = false,
        tokenId = "DAI",
        onRefresh = {},
//        tokenAssetUiState = TokenAssetUiState.Success(tokenAssets)
    )
}


//method for testing
@Composable
fun generateRandomTransfers(): List<TransferItem> {
    val random = Random(System.currentTimeMillis())

    // Beispielhafte Listen für zufällige Werte
    val possibleChainIds = listOf(1, 56, 137, 42)  // z.B. Ethereum, BSC, Polygon, Kovan
    val possibleAssets = listOf("DAI")

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
            value = (random.nextInt(1, 1000) + random.nextDouble()).toString(),
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
    endPadding: Float = 12f
): Modifier {
    val coroutineScope = rememberCoroutineScope()
    var isScrolling by remember { mutableStateOf(false) }
    var targetAlpha by remember { mutableStateOf(0f) } // Start hidden
    var targetScrollBarOffset by remember { mutableStateOf(0f) } // Thumb Y position

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 250, easing = LinearEasing)
    )

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress) {
            isScrolling = true
            targetAlpha = 1f // 🔥 Fade in
        } else {
            delay(1000) // Wait 1 second before fading out
            targetAlpha = 0f // 🔥 Fade out smoothly
        }
    }

    return this.then(
        Modifier.drawWithContent {
            drawContent()

            val layoutInfo = lazyListState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            val totalItemsCount = layoutInfo.totalItemsCount

            if (visibleItemsInfo.isEmpty() || totalItemsCount == 0) return@drawWithContent

            // 1️⃣ Fixed scrollbar track height
            val trackHeight = size.height - 64.dp.toPx()

            // 2️⃣ Compute thumb height proportionally
            val visibleItemCount = visibleItemsInfo.size.toFloat()
            val thumbHeight = (visibleItemCount / totalItemsCount) * trackHeight
                .coerceAtLeast(40.dp.toPx()) // Ensuring a minimum thumb height

            // 3️⃣ Compute scrollbar thumb position based on scroll progress
            val firstVisibleItem = lazyListState.firstVisibleItemIndex
            val firstItemOffset = lazyListState.firstVisibleItemScrollOffset

            // Estimate total scrollable distance
            val averageItemHeight = visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItemsInfo.size
            val maxScrollOffset = (totalItemsCount - visibleItemCount) * averageItemHeight
            val scrolledOffset = (firstVisibleItem * averageItemHeight) + firstItemOffset

            // Compute scrollbar thumb position and update animated target
            targetScrollBarOffset = ((scrolledOffset / maxScrollOffset) * (trackHeight - thumbHeight))
                .coerceIn(0f, trackHeight - thumbHeight)

            // 4️⃣ Draw the scrollbar track
            if (showScrollBarTrack) {
                drawRoundRect(
                    color = scrollBarTrackColor.copy(alpha = alpha),
                    cornerRadius = CornerRadius(scrollBarCornerRadius),
                    topLeft = Offset(size.width - 32.dp.toPx(), 32.dp.toPx()),
                    size = Size(width.toPx(), trackHeight)
                )
            }

            // 5️⃣ Draw the scrollbar thumb
            drawRoundRect(
                color = scrollBarColor.copy(alpha = alpha),
                cornerRadius = CornerRadius(scrollBarCornerRadius),
                topLeft = Offset(size.width - 32.dp.toPx(), 32.dp.toPx() + targetScrollBarOffset),
                size = Size(width.toPx(), thumbHeight)
            )
        }
    )
}





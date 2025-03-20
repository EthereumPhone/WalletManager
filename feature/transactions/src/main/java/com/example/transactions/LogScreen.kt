package com.example.transactions

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TransferItem
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGray
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import com.example.transactions.ui.LogEntry
import com.example.transactions.ui.TxEntry
import com.example.transactions.ui.TxType
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.walletmanager.ethOSTransferListItem
import kotlin.random.Random

@Composable
fun LogRoute(
    navigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
){
    val transfersUIState: TransfersUiState by viewModel.transferState.collectAsStateWithLifecycle()
    val refreshState by viewModel.isRefreshing.collectAsStateWithLifecycle()

    LogScreen(
        transfersUIState = transfersUIState,
        onNavigateBack = navigateBack,
        refreshState = refreshState,
        onRefresh = viewModel::refreshData
    )
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LogScreen(
    modifier: Modifier = Modifier,
    transfersUIState: TransfersUiState,
    onNavigateBack: () -> Unit = {},
    refreshState: Boolean,
    onRefresh: () -> Unit,
){

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshState,
        onRefresh = {
            onRefresh()
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)

    ) {
        Row (
            modifier = Modifier.fillMaxWidth().padding(end = 4.dp,
                start = 4.dp,top = 8.dp)
        ){
            IconButton(modifier = Modifier, onClick = onNavigateBack) {
                Icon(
                    modifier = Modifier.width(16.dp),
                    painter = painterResource(R.drawable.back_icon),
                    contentDescription = "Back",
                    tint = dgenTurqoise
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()){

            when(transfersUIState){
                is TransfersUiState.Loading -> {

                }
                is TransfersUiState.Success -> {

                    val transfers = transfersUIState.transfers


                    if (transfers.isNotEmpty()){

                        Box(
                            Modifier
                                .fillMaxSize()
                                .pullRefresh(pullRefreshState)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Spacer(Modifier.height(16.dp))
                                }
                                items(transfers.reversed()) { transfer ->
                                    LogEntry(transfer)
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
                            colors = listOf(dgenBlack,Color.Transparent)
                        )
                    )

            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(40.dp)
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

    val txs = generateRandomTransfers()

    LogScreen(
        transfersUIState = TransfersUiState.Success(txs),
        refreshState = false,
        onRefresh = {}
    )
}

//method for testing
@Composable
fun generateRandomTransfers(): List<TransferItem> {
    val random = Random(System.currentTimeMillis())

    // Beispielhafte Listen für zufällige Werte
    val possibleChainIds = listOf(1, 56, 137, 42)  // z.B. Ethereum, BSC, Polygon, Kovan
    val possibleAssets = listOf("ETH", "BNB", "MATIC", "USDT", "DAI")

    // Beispiel-Adressen (typisch 0x + 40 Hex-Stellen, hier verkürzt oder zufällig generiert)
    val sampleAddresses = listOf(
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
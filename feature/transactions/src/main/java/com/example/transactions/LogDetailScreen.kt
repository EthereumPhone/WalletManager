package com.example.transactions

import android.content.Intent
import android.net.Uri
import android.view.SurfaceControl.Transaction
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.core.ui.DgenLoadingMatrix
import com.core.ui.HeaderBar
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.body1_fontSize
import com.core.ui.util.body2_fontSize
import com.core.ui.util.chainIdToName
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenWhite
import com.core.ui.util.formatAddress
import com.core.ui.util.formatWithSuffix
import com.core.ui.util.label_fontSize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.core.data.util.chainIdToName

@Composable
fun DetailLogRoute(
    navigateBack: () -> Unit,
    txHash: String,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transfersUIState by viewModel.transferState.collectAsStateWithLifecycle()
    val tokenMetadata by viewModel.tokenMetadata.collectAsStateWithLifecycle()

    LaunchedEffect(txHash) {
        viewModel.onDetailLogOpened(txHash)
    }

    var hasHandledInitialResume by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (hasHandledInitialResume) {
                        viewModel.onDetailLogOpened(txHash)
                    } else {
                        hasHandledInitialResume = true
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            viewModel.onDetailLogClosed()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val secondaryColor = SystemColorManager.secondaryColor
    val primaryColor = SystemColorManager.primaryColor

    when (transfersUIState) {
        is TransfersUiState.Loading -> {
            Box(Modifier.fillMaxSize().background(dgenBlack), contentAlignment = Alignment.Center) {
                DgenLoadingMatrix(
                    unactiveLEDColor = secondaryColor,
                    activeLEDColor = primaryColor
                )
            }
        }
        is TransfersUiState.Success -> {
            val transfer = (transfersUIState as TransfersUiState.Success).transfers.find { it.txHash == txHash }
            if (transfer != null) {
                val meta = tokenMetadata.find { it.symbol == transfer.asset }
                LogDetailScreen(
                    transfer = transfer,
                    logoUrl = meta?.logo ?: "",
                    onNavigateBack = navigateBack
                )
            } else {
                // Handle case where transaction is not found
                Box(Modifier.fillMaxSize().background(dgenBlack), contentAlignment = Alignment.Center) {
                    Text("Transaction not found", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun LogDetailScreen(
    modifier: Modifier = Modifier,
    transfer: TransferItem,
    logoUrl: String,
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        SystemColorManager.refresh(context)
    }
    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor
    val scrollState = rememberLazyListState()

    val fromValue = transfer.from
    val toValue = transfer.to

    // Format timestamp
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date: Date? = try {
        inputFormat.parse(transfer.timeStamp)
    } catch (e: Exception) {
        null
    }
    val outputFormat = SimpleDateFormat("MMMM d, yyyy 'at' hh:mm a", Locale.getDefault())
    val formattedTimestamp = date?.let { outputFormat.format(it) } ?: transfer.timeStamp


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

            HeaderBar(
                modifier = modifier.padding(horizontal = 24.dp),
                onClick = onNavigateBack,
                text = "",
                content = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (transfer.userSent) "SENT" else "RECEIVED",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = primaryColor,
                                fontWeight = FontWeight.Medium,
                                fontSize = 24.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            )
                        )
                        if (logoUrl.isNotEmpty()) {
                            AsyncImage(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape),
                                model = logoUrl,
                                contentDescription = "Token logo"
                            )
                        } else {
                            Image(
                                modifier = Modifier.size(28.dp),
                                painter = painterResource(com.core.ui.R.drawable.placeholer_icon_5),
                                contentDescription = "Placeholder"
                            )
                        }

                        Text(
                            text = transfer.asset.uppercase(),
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = primaryColor,
                                fontWeight = FontWeight.Medium,
                                fontSize = 24.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            )
                        )
                    }
                },
                primaryColor = primaryColor
            )


        Box(
            modifier = Modifier.fillMaxSize(1f)
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .verticalLazyListScrollbar(
                        lazyListState = scrollState,
                        scrollBarTrackColor = secondaryColor,
                        scrollBarColor = primaryColor
                    )
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    DetailItem(label = "Value", value = "${transfer.value.toDouble().formatWithSuffix()} ${transfer.asset}", primaryColor = primaryColor)
                }
                item {
                    DetailItem(label = if (transfer.userSent) "To" else "From", value = if (transfer.userSent) toValue else fromValue, primaryColor = primaryColor)
                }
                item {
                    DetailItem(label = "Date", value = formattedTimestamp, primaryColor = primaryColor)
                }
                item {
                    DetailItem(label = "Network", value = networkToName(transfer.chainId), primaryColor = primaryColor)
                }
                item {
                    DetailItem(label = "Tx Hash", value = transfer.txHash, primaryColor = primaryColor)
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
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

fun networkToName(chainId: Int): String = when(chainId) {
    1 -> "Mainnet"
    11155111 -> "Sepolia"
    10 -> "Optimism"
    42161 -> "Arbitrum"
    137 -> "Polygon"
    8453 -> "Base"
    5 -> "Goerli"
    else -> ""
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    primaryColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(end=24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = TextStyle(
                fontFamily = SpaceMono,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = label_fontSize,
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = PitagonsSans,
                color = dgenWhite,
                fontWeight = FontWeight.Medium,
                fontSize = body1_fontSize,
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LogDetailScreenPreview() {
    val transferItem = TransferItem(
        chainId = 1,
        from = "0xAbcde12345...890",
        to = "emunsi.eth",
        asset = "ETH",
        value = "1.23456789",
        timeStamp = "2023-03-17 16:00:00",
        userSent = true,
        txHash = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    )

    Box(modifier = Modifier.background(dgenBlack)) {
        LogDetailScreen(
            transfer = transferItem,
            logoUrl = "", // "https://cryptologos.cc/logos/ethereum-eth-logo.png",
            onNavigateBack = {}
        )
    }
}

fun getEtherscanDomainForChain(chainId: Int): String {
    return when(chainId) {
        1 -> "https://etherscan.io/"
        11155111 -> "https://goerli.etherscan.io/"
        10 -> "https://optimistic.etherscan.io/"
        137 -> "https://polygonscan.com/"
        42161 -> "https://arbiscan.io/"
        8453 -> "https://basescan.org/"
        else -> ""
    }
}

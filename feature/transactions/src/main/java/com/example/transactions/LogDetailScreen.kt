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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.core.ui.DgenLoadingMatrix
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
import com.core.ui.util.TokenLogoFallback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.Brush
import com.core.data.util.chainIdToName
import androidx.activity.compose.BackHandler
import com.example.dgenlibrary.DetailItem
import com.example.dgenlibrary.ui.backgrounds.DgenHeaderBackground
import com.example.dgenlibrary.ui.theme.DgenBackgroundHorizontalPadding

@Composable
fun DetailLogRoute(
    navigateBack: () -> Unit,
    txHash: String,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transfersUIState by viewModel.transferState.collectAsStateWithLifecycle()
    val tokenMetadata by viewModel.tokenMetadata.collectAsStateWithLifecycle()

    // Track if onDetailLogOpened has been called for this txHash
    var hasCalledForCurrentTx by remember(txHash) { mutableStateOf(false) }
    
    // Track if we're returning from background
    var isReturningFromBackground by remember { mutableStateOf(false) }
    
    // Add navigation state to prevent multiple navigation calls
    var isNavigating by remember { mutableStateOf(false) }
    
    // Track if we're navigating away to prevent resume operations
    var isNavigatingAway by remember { mutableStateOf(false) }
    
    // Debounced navigation function with additional protection


    // Call onDetailLogOpened only once when first navigating to this screen
    LaunchedEffect(Unit) {
        if (!hasCalledForCurrentTx) {
            viewModel.onDetailLogOpened(txHash)
            hasCalledForCurrentTx = true
        }
    }

    // Handle lifecycle events for resume from background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Only mark as returning from background if not navigating away
                    if (!isNavigatingAway) {
                        isReturningFromBackground = true
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Only call if we're returning from background (not initial load or navigating away)
                    if (isReturningFromBackground && hasCalledForCurrentTx && !isNavigatingAway) {
                        viewModel.onDetailLogResume(txHash)
                    }
                    isReturningFromBackground = false
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.onDetailLogClosed()
            viewModel.cancelPendingOperations()
        }
    }

    val secondaryColor = SystemColorManager.secondaryColor
    val primaryColor = SystemColorManager.primaryColor
    
    // Handle device back button press with safe navigation
    BackHandler {
        isNavigatingAway = true
        viewModel.cancelPendingOperations()
        navigateBack()
    }

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
                    onNavigateBack = { navigateBack() }
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

    DgenHeaderBackground(
        primaryColor = primaryColor,
        headerContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {
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
                // Check for fallback logo for consistent display across screens
                val fallbackLogo = TokenLogoFallback.getFallbackLogo(transfer.asset)

                // Determine the effective logo: prefer fallback for consistency
                val effectiveLogoUrl = when {
                    // If we have a fallback URL, use it for consistency
                    fallbackLogo is TokenLogoFallback.LogoSource.Url -> fallbackLogo.url
                    // Otherwise use the provided logo URL
                    logoUrl.isNotEmpty() -> logoUrl
                    else -> ""
                }

                // Use themed placeholder based on primary color
                val placeholderDrawable = SystemColorManager.getPlaceholderTokenDrawable()

                when {
                    // Use URL (either fallback or provided)
                    effectiveLogoUrl.isNotEmpty() -> {
                        AsyncImage(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape),
                            model = effectiveLogoUrl,
                            contentDescription = "Token logo",
                            placeholder = painterResource(placeholderDrawable),
                            error = painterResource(placeholderDrawable)
                        )
                    }
                    // Check for local resource fallback
                    fallbackLogo is TokenLogoFallback.LogoSource.LocalResource -> {
                        Image(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape),
                            painter = painterResource(fallbackLogo.resourceId),
                            contentDescription = "Token logo"
                        )
                    }
                    // Fallback: Use placeholder
                    else -> {
                        Image(
                            modifier = Modifier.size(28.dp),
                            painter = painterResource(placeholderDrawable),
                            contentDescription = "Placeholder"
                        )
                    }
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
        onBackClick = onNavigateBack
    ) {




        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = DgenBackgroundHorizontalPadding)
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .verticalLazyListScrollbar(
                        lazyListState = scrollState,
                        scrollBarTrackColor = secondaryColor,
                        scrollBarColor = primaryColor
                    ),
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

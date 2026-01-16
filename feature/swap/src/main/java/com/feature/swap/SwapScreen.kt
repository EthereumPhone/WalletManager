package com.feature.swap

import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.core.model.TokenAsset
import com.core.model.SwapUIState
import com.core.model.SwapToken
import com.core.ui.HeaderBar
import com.core.ui.showDgenToast
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenBlack
import com.feature.swap.ui.SwapInterface
import com.feature.swap.ui.SwapTransactionStatus
import com.feature.swap.ui.SwapTransactionStatusOverlay
import com.feature.swap.ui.TokenSelectorOverlay
import kotlinx.coroutines.delay

@Composable
internal fun SwapRoute(
    modifier: Modifier,
    viewModel: SwapViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    initialTokenGroupId: String? = null
) {
    var hasHandledInitialResume by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (!hasHandledInitialResume) {
                        hasHandledInitialResume = true
                    } else {
                        viewModel.onScreenOpenedAfterResume()
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.onSwapTerminalClosed()
        }
    }
    
    BackHandler {
        viewModel.onSwapTerminalClosed()
        onBackClick()
    }
    
    SwapScreen(
        modifier = modifier,
        onBackClick = {
            viewModel.onSwapTerminalClosed()
            onBackClick()
        },
        viewModel = viewModel,
        initialTokenGroupId = initialTokenGroupId
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwapScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    viewModel: SwapViewModel = hiltViewModel(),
    initialTokenGroupId: String? = null
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { SystemColorManager.refresh(context) }

    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    // Collect states from SwapViewModel
    val groupedAssetsUiState by viewModel.groupedTokenAssetState.collectAsStateWithLifecycle()
    val fromTokensUiState by viewModel.fromTokensState.collectAsStateWithLifecycle()
    val isTokenOverlayVisible by viewModel.isTokenOverlayVisible.collectAsStateWithLifecycle()
    val swapUIState by viewModel.swapUIState.collectAsStateWithLifecycle()
    val selectionMode by viewModel.tokenSelectionMode.collectAsStateWithLifecycle()
    val tokenListUi by viewModel.swapTokenUiState.collectAsStateWithLifecycle()
    val selectedTokenChainId by viewModel.selectedTokenChainId.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val swapTransactionStatus by viewModel.swapTransactionStatus.collectAsStateWithLifecycle()
    val isDexScreenerLoading by viewModel.isDexScreenerLoading.collectAsStateWithLifecycle()
    
    // Show toast when message is set
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            showDgenToast(context, message)
            viewModel.clearToastMessage()
        }
    }
    
    // Handle transaction status changes and auto-navigation
    LaunchedEffect(swapTransactionStatus) {
        Log.d("SwapScreen", "=== TRANSACTION STATUS CHANGED ===")
        Log.d("SwapScreen", "New swapTransactionStatus: $swapTransactionStatus")

        when (swapTransactionStatus) {
            SwapTransactionStatus.SUCCESS -> {
                Log.d("SwapScreen", "🟢 SUCCESS status detected - swap successful")
                delay(SwapTransactionTiming.SUCCESS_DISPLAY_DURATION)
                Log.d("SwapScreen", "${SwapTransactionTiming.SUCCESS_DISPLAY_DURATION}ms passed, starting smooth fade navigation")
                onBackClick()
                delay(SwapTransactionTiming.FADE_TRANSITION_DURATION)
                Log.d("SwapScreen", "Fade transition complete, clearing overlay")
                viewModel.clearSwapTransactionStatus()
            }
            is SwapTransactionStatus.FAILURE -> {
                Log.d("SwapScreen", "🔴 FAILURE status detected - showing error state")
                // Display failure overlay for a reasonable duration to acknowledge the error
                delay(SwapTransactionTiming.FAILURE_DISPLAY_DURATION)
                Log.d("SwapScreen", "${SwapTransactionTiming.FAILURE_DISPLAY_DURATION}ms passed, starting fade navigation")

                // Start navigation while overlay is still visible for smooth fade effect
                onBackClick()

                // Keep overlay visible during fade transition for seamless experience
                delay(SwapTransactionTiming.FADE_TRANSITION_DURATION)
                Log.d("SwapScreen", "Fade transition complete, clearing overlay")
                viewModel.clearSwapTransactionStatus()
            }
            else -> {
                Log.d("SwapScreen", "Other status: $swapTransactionStatus - no auto-navigation")
            }
        }
        Log.d("SwapScreen", "=== TRANSACTION STATUS HANDLING ENDED ===")
    }
    
    // Debug logging
    LaunchedEffect(swapUIState) {
        Log.d("SwapScreen", "SwapUIState changed - FROM: ${swapUIState.fromToken?.token?.symbol}, TO: ${swapUIState.toToken?.token?.symbol}")
    }
    
    // Handle initial token selection if provided
    LaunchedEffect(initialTokenGroupId) {
        initialTokenGroupId?.let { tokenId ->
            viewModel.selectTokenFromCarousel(tokenId)
        }
    }
    
    // Ensure terminal renders swap content on initial open
    LaunchedEffect(Unit) {
        viewModel.onScreenOpenedAfterResume()
    }
    
    // Create GIF-enabled ImageLoader for animations
    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(dgenBlack)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
        //.padding(horizontal = 32.dp, vertical = 32.dp)
    ) {
        HeaderBar(text = "SWAP ASSETS", onClick = onBackClick, primaryColor = primaryColor)
        
        SwapInterface(
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            uiState = swapUIState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }

    // Unified Token Selector Overlay - handles both From and To selections
    val fromTokens = (fromTokensUiState as? FromTokensUiState.Success)?.tokens ?: emptyList()
    val toTokens = when (tokenListUi) {
        is SwapTokenUiState.Success -> (tokenListUi as SwapTokenUiState.Success).tokenAssets
        else -> emptyList()
    }
    val groupedTokens = (groupedAssetsUiState as? GroupedAssetsUiState.Success)?.assets ?: emptyList()
    
    // Determine if tokens are still loading (initial load or search)
    val isTokensLoading = when (selectionMode) {
        TokenSelectionMode.From -> fromTokensUiState is FromTokensUiState.Loading
        TokenSelectionMode.To -> tokenListUi is SwapTokenUiState.Loading
        else -> false
    }

    TokenSelectorOverlay(
        isVisible = isTokenOverlayVisible,
        mode = selectionMode,
        fromTokens = fromTokens,
        toTokens = toTokens,
        selectFromToken = { token -> viewModel.selectFromTokenAsset(token) },
        selectToToken = { token -> viewModel.selectToTokenAsset(token) },
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        onDismiss = { 
            viewModel.hideTokenOverlay()
            viewModel.clearDexScreenerResults()
        },
        currentChainId = toTokens.firstOrNull()?.chainId,
        selectedChainId = selectedTokenChainId,
        onChainSelected = { chainId -> viewModel.setTokenSelectorChain(chainId) },
        groupedTokens = groupedTokens,
        isDexScreenerLoading = isDexScreenerLoading,
        isTokensLoading = isTokensLoading,
        onSearchDexScreener = { query -> viewModel.searchDexScreener(query) },
        onClearDexScreenerResults = { viewModel.clearDexScreenerResults() }
    )
    
    // Swap Transaction Status Overlay - shows swap progress and results
    SwapTransactionStatusOverlay(
        status = swapTransactionStatus,
        gifLoader = gifEnabledLoader,
        onDismiss = { viewModel.clearSwapTransactionStatus() },
        primaryColor = primaryColor,
        secondaryColor = secondaryColor
    )
}

fun isEthereumTransactionHash(input: String): Boolean {
    val transactionHashPattern = "^0x([A-Fa-f0-9]{64})$"
    return Regex(transactionHashPattern).matches(input)
}

object SwapTransactionTiming {
    // How long to show the SUCCESS overlay before starting navigation (in milliseconds)
    const val SUCCESS_DISPLAY_DURATION = 4000L // 4 seconds to enjoy the success

    // How long to show the FAILURE overlay before starting navigation (in milliseconds)
    const val FAILURE_DISPLAY_DURATION = 2500L // 2.5 seconds for failure state

    // Delay between starting navigation and clearing the overlay for smooth fade transition (in milliseconds)
    const val FADE_TRANSITION_DURATION = 1000L // 1 second fade overlap
}

@Preview
@Composable
fun PreviewSwapScreen() {
    SwapScreen(
        onBackClick= {},
    )
}



package com.feature.swap

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
import com.core.model.TokenAsset
import com.core.model.SwapUIState
import com.core.model.SwapToken
import com.core.ui.HeaderBar
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenBlack
import com.feature.swap.ui.SwapInterface
import com.feature.swap.ui.TokenSelectorOverlay

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
    val isTokenOverlayVisible by viewModel.isTokenOverlayVisible.collectAsStateWithLifecycle()
    val swapUIState by viewModel.swapUIState.collectAsStateWithLifecycle()
    val selectionMode by viewModel.tokenSelectionMode.collectAsStateWithLifecycle()
    val tokenListUi by viewModel.swapTokenUiState.collectAsStateWithLifecycle()
    
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
        
        // Debug button - TODO: Remove this later
        Button(
            onClick = {
                Log.d("SwapScreen", "Debug button clicked – executing swap")
                viewModel.swap { result ->
                    Log.d("SwapScreen", "Swap result: $result")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("DEBUG: Log Swap State")
        }
    }

    // Unified Token Selector Overlay - handles both From and To selections
    val fromAssets = (groupedAssetsUiState as? GroupedAssetsUiState.Success)?.assets ?: emptyList()
    val toTokens = when (tokenListUi) {
        is SwapTokenUiState.Success -> (tokenListUi as SwapTokenUiState.Success).tokenAssets
        else -> emptyList()
    }

    TokenSelectorOverlay(
        isVisible = isTokenOverlayVisible,
        mode = selectionMode,
        fromAssets = fromAssets,
        toTokens = toTokens,
        selectFromGroup = { groupId -> viewModel.selectTokenFromCarousel(groupId) },
        selectToToken = { token -> viewModel.selectToTokenAsset(token) },
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        onDismiss = { viewModel.hideTokenOverlay() },
        currentChainId = toTokens.firstOrNull()?.chainId
    )
}

fun isEthereumTransactionHash(input: String): Boolean {
    val transactionHashPattern = "^0x([A-Fa-f0-9]{64})$"
    return Regex(transactionHashPattern).matches(input)
}

@Preview
@Composable
fun PreviewSwapScreen() {
    SwapScreen(
        onBackClick= {},
    )
}



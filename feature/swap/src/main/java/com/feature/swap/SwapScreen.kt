package com.feature.swap

import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feature.home.AssetUiState
import com.feature.swap.ui.TokenPickerSheet

@Composable
internal fun SwapRoute(
    modifier: Modifier,
    viewModel: SwapViewModel = hiltViewModel()

) {
    val fromAmountUiState by viewModel.fromAmount.collectAsStateWithLifecycle()
    val toAmountUiState by viewModel.toAmount.collectAsStateWithLifecycle()
    val fromAssetUiState by viewModel.fromAsset.collectAsStateWithLifecycle()
    val toAssetUiState by viewModel.toAsset.collectAsStateWithLifecycle()

    val swapTokenUiState by viewModel.swapTokenUiState.collectAsStateWithLifecycle()
    val exchangeUiState by viewModel.exchangeRate.collectAsStateWithLifecycle()

    SwapScreen(
        modifier = modifier,
        swapTokenUiState = swapTokenUiState,
        exchangeUiState = exchangeUiState,
        fromAmountUiState = fromAmountUiState,
        toAmountUiState = toAmountUiState,
        fromAssetUiState = toAssetUiState,
        toAssetUiState = fromAssetUiState,
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwapScreen(
    modifier: Modifier,
    swapTokenUiState: SwapTokenUiState,
    exchangeUiState: Double,
    fromAmountUiState: Double,
    toAmountUiState: Double,
    fromAssetUiState: SelectedTokenUiState,
    toAssetUiState: SelectedTokenUiState

) {

    var isVisible by remember { mutableStateOf(true) }

    // Create a BottomSheetScaffoldState
    val scaffoldState = rememberBottomSheetScaffoldState()

    // Wrap the content inside the BottomSheetScaffold
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            //TokenPickerSheet(emptyList())
                       },
        sheetPeekHeight = 0.dp
    ) {



    }
}

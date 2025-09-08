package com.feature.send

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun sendRoute(
    onBackClick: () -> Unit,
    viewModel: SendViewModel = hiltViewModel()
) {

    val amountUiState by viewModel.amountUiState.collectAsStateWithLifecycle()
    val recipientUiState by viewModel.recipientUiState.collectAsStateWithLifecycle()
    val assetsUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val selectedAssetUiState by viewModel.selectedAssetUiState.collectAsStateWithLifecycle()
    val qrScannerTriggered by viewModel.qrScannerTriggered.collectAsStateWithLifecycle()
    val sendTransactionTriggered by viewModel.sendTransactionTriggered.collectAsStateWithLifecycle()
    val transactionStatus by viewModel.transactionStatus.collectAsStateWithLifecycle()


    sendScreen(
        amountUiState,
        recipientUiState,
        selectedAssetUiState
    )

}




fun sendScreen(
    amountUiState: AmountUiState,
    recipientUiState: RecipientUiState,
    selectedAssetUiState: SelectedTokenUiState
) {



}
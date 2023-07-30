package com.feature.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feature.home.ui.AddressBar
import com.feature.home.ui.FunctionsRow
import com.feature.home.ui.WalletTabRow

@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userAddress by viewModel.userAddress.collectAsStateWithLifecycle()
    val transfersUiState by viewModel.transferState.collectAsStateWithLifecycle()
    val assetsUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val refreshState by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val localContext = LocalContext.current


    HomeScreen(
        userAddress = userAddress,
        transfersUiState = transfersUiState,
        assetsUiState = assetsUiState,
        refreshState = refreshState,
        onAddressClick = {
            // had to do it here because I need the local context.
            copyTextToClipboard(localContext, userAddress)
        },
        navigateToSwap = navigateToSwap,
        navigateToSend = navigateToSend,
        navigateToReceive = navigateToReceive,
        onRefresh = viewModel::refreshData,

        modifier = modifier
    )
}

@Composable
internal fun HomeScreen(
    userAddress: String,
    transfersUiState: TransfersUiState,
    assetsUiState: AssetUiState,
    refreshState: Boolean,
    onAddressClick: () -> Unit,
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        AddressBar(
            userAddress,
            onAddressClick
        )
        FunctionsRow(
            navigateToSwap,
            navigateToSend,
            navigateToReceive
        )

        WalletTabRow(
            transfersUiState,
            assetsUiState,
            refreshState,
            onRefresh = { onRefresh() }
        )
    }
}

@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}

@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen(
        userAddress = "0x123123123123123123123123",
        transfersUiState = TransfersUiState.Loading,
        assetsUiState = AssetUiState.Loading,
        refreshState = false,
        onAddressClick = { },
        navigateToReceive = { },
        navigateToSend = { },
        navigateToSwap = { },
        onRefresh = { }
    )
}
package com.feature.send

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.core.model.TokenAssetWithPrice
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenBlack
import com.core.ui.util.pulseOpacity
import com.feature.send.ui.AmountTextField
import com.feature.send.ui.NetworkSelector
import com.feature.send.ui.RecipientSection
import com.feature.send.ui.SendHeader


@Composable
fun SendRoute(
    onBackClick: () -> Unit,
    viewModel: SendViewModel = hiltViewModel()
) {

    val amountUiState by viewModel.amountUiState.collectAsStateWithLifecycle()
    val recipientUiState by viewModel.recipientUiState.collectAsStateWithLifecycle()
    val assetsUiState by viewModel.assetsUiState.collectAsStateWithLifecycle()
    val selectedAssetUiState by viewModel.selectedAssetUiState.collectAsStateWithLifecycle()
    val qrScannerTriggered by viewModel.qrScannerTriggered.collectAsStateWithLifecycle()
    val sendTransactionTriggered by viewModel.sendTransactionTriggered.collectAsStateWithLifecycle()
    val transactionStatus by viewModel.transactionStatus.collectAsStateWithLifecycle()


    SendScreen(
        amountUiState = amountUiState,
        recipientUiState = recipientUiState,
        assetsUiState = assetsUiState,
        selectedAssetUiState = selectedAssetUiState,
        onNetworkSelected = viewModel::changeSelectedAsset,
        onAmountChange = viewModel::updateAmount,
        maxAmountClicked = viewModel::setMaxAmount,
        onRecipientChange = viewModel::updateAddress,
        onBackClick = onBackClick
    )

}



@Composable
fun SendScreen(
    amountUiState: AmountUiState,
    recipientUiState: RecipientUiState,
    assetsUiState: AssetsUiState,
    selectedAssetUiState: SelectedAssetUiState,
    onNetworkSelected: (Int) -> Unit,
    onAmountChange: (String, Boolean) -> Unit,
    maxAmountClicked: () -> Unit,
    onRecipientChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val primaryColor = SystemColorManager.primaryColor
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current

    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if ( SDK_INT >= 28 ) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
            .statusBarsPadding()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        AsyncImage(
            modifier = Modifier
                .alpha(pulseOpacity)
                .offset(x = 250.dp, y = 20.dp)
                .scale(1.3f)
                .aspectRatio(1f),
            imageLoader = gifEnabledLoader,
            model = R.drawable.globe_wireframe,
            contentDescription = null,
            colorFilter = ColorFilter.tint(primaryColor)
        )


        Column(
            Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 12.dp),
        ) {
            SendHeader(
                modifier = Modifier.padding(bottom = 48.dp),
                assetsUiState = assetsUiState,
                onBackClick
            )

            AmountTextField(
                selectedAssetUiState= selectedAssetUiState,
                amountUiState = amountUiState,
                onAmountChange = onAmountChange,
                onMaxClick = maxAmountClicked
            )

            NetworkSelector(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .weight(1f),
                itemWidth = 65.dp,
                itemHeight = 65.dp,
                assetsUiState = assetsUiState,
                selectedAssetUiState = selectedAssetUiState,
                onNetworkSelected = onNetworkSelected
            )



            RecipientSection(
                recipientUiState = recipientUiState,
                onContentChanged = onRecipientChange
            )
        }
    }
}

@Preview(device = "spec:width=720px,height=720px,dpi=240")
@Composable
fun PreviewSendScreen() {
    val amountUiState = AmountUiState(
        maxAmount = 100.0,
        maxFiatAmount = 100.0,
        formattedMaxAmount = "100",
        currentAmount = "12.1",
        formattedMaxFiatAmount = "100",
        currentFiatAmount = "12.1",
        useMaxAmount = false,
    )

    val recipientUiState = RecipientUiState()

    val assetsUiState = AssetsUiState.Success(
        listOf(
            TokenAssetWithPrice(
                address = "0x0123",
                chainId = 1,
                symbol = "ETH",
                name = "Ethereum",
                balance = 100.0,
                decimals = 16,
                swappable = true,
                fiatAmount = 100.0
            ),
            TokenAssetWithPrice(
                address = "0x0123",
                chainId = 137,
                symbol = "ETH",
                name = "Ethereum",
                balance = 100.0,
                decimals = 16,
                swappable = true,
                fiatAmount = 100.0
            )
        )
    )

    val selectedAssetUiState = SelectedAssetUiState.Selected(
        TokenAssetWithPrice(
            address = "0x0123",
            chainId = 1,
            symbol = "ETH",
            name = "Ethereum",
            balance = 100.0,
            decimals = 16,
            swappable = true,
            fiatAmount = 100.0
        )
    )

    SendScreen(
        amountUiState,
        recipientUiState,
        assetsUiState,
        selectedAssetUiState,
        {},
        {} as (String, Boolean) -> Unit,
        {},
        {},
        {}
    )
}
package com.feature.send

import android.Manifest
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.feature.send.ui.TransactionStatusOverlay
import com.feature.send.ui.TransactionStatus
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.journeyapps.barcodescanner.ScanContract
import kotlinx.coroutines.delay


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
    val shouldDismissKeyboard by viewModel.shouldDismissKeyboard.collectAsStateWithLifecycle()



    // Flag to ensure the first ON_RESUME (which happens on the initial screen launch) is ignored
    var hasHandledInitialResume by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, transactionStatus) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Skip the very first ON_RESUME that occurs when the screen is opened for the first time
                    if (!hasHandledInitialResume) {
                        hasHandledInitialResume = true
                    } else if (assetsUiState is AssetsUiState.Success && transactionStatus == null) {
                        // Only call onScreenOpenedAfterResume on subsequent resumes when no transaction is running
                        println("SendScreen2: ON_RESUME - calling onScreenOpenedAfterResume() ETHOSDEBUG")
                        viewModel.onScreenOpenedAfterResume()
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Display QR code on secondary screen only when the actual send screen content appears
    LaunchedEffect(assetsUiState, transactionStatus) {
        // Display QR code only when assets are loaded and there is no active transaction status
        if (assetsUiState is AssetsUiState.Success && transactionStatus == null) {
            viewModel.onScreenOpened()
        }
    }

    // Track if user is navigating back to home
    var isNavigatingBack by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onScreenClosed(clearLed = !isNavigatingBack)
        }
    }

    LaunchedEffect(transactionStatus) {
        Log.d("SendScreen", "=== TRANSACTION STATUS CHANGED ===")
        Log.d("SendScreen", "New transactionStatus: $transactionStatus")

        when (transactionStatus) {
            TransactionStatus.SUCCESS -> {
                Log.d("SendScreen", "🟢 SUCCESS status detected - transaction successful")
                delay(TransactionTiming.SUCCESS_DISPLAY_DURATION)
                Log.d("SendScreen", "${TransactionTiming.SUCCESS_DISPLAY_DURATION}ms passed, starting smooth fade navigation")
                onBackClick()
                delay(TransactionTiming.FADE_TRANSITION_DURATION)
                Log.d("SendScreen", "Fade transition complete, clearing overlay")
                viewModel.clearTransactionStatus()
            }
            TransactionStatus.FAILURE -> {
                Log.d("SendScreen", "🔴 FAILURE status detected - showing error state")
                // Display failure overlay for a reasonable duration to acknowledge the error
                delay(TransactionTiming.FAILURE_DISPLAY_DURATION)
                Log.d("SendScreen", "${TransactionTiming.FAILURE_DISPLAY_DURATION}ms passed, starting fade navigation")

                // Start navigation while overlay is still visible for smooth fade effect
                onBackClick()

                // Keep overlay visible during fade transition for seamless experience
                delay(TransactionTiming.FADE_TRANSITION_DURATION)
                Log.d("SendScreen", "Fade transition complete, clearing overlay")
                viewModel.clearTransactionStatus()
            }
            else -> {
                Log.d("SendScreen", "Other status: $transactionStatus - no auto-navigation")
            }
        }
        Log.d("SendScreen", "=== TRANSACTION STATUS HANDLING ENDED ===")
    }




    SendScreen(
        amountUiState = amountUiState,
        recipientUiState = recipientUiState,
        assetsUiState = assetsUiState,
        selectedAssetUiState = selectedAssetUiState,
        transactionStatus = transactionStatus,
        qrScannerTriggered = qrScannerTriggered,
        shouldDismissKeyboard = shouldDismissKeyboard,
        onNetworkSelected = viewModel::changeSelectedAsset,
        onAmountChange = viewModel::updateAmount,
        maxAmountClicked = viewModel::setMaxAmount,
        onRecipientChange = viewModel::updateAddress,
        clearTransactionStatus = viewModel::clearTransactionStatus,
        resetQrScannerTrigger = viewModel::resetQrScannerTrigger,
        onKeyboardDismissed = viewModel::onKeyboardDismissed,
        onBackClick = onBackClick
    )
}



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SendScreen(
    amountUiState: AmountUiState,
    recipientUiState: RecipientUiState,
    assetsUiState: AssetsUiState,
    selectedAssetUiState: SelectedAssetUiState,
    transactionStatus: TransactionStatus?, // TODO: Change this
    qrScannerTriggered: Boolean, // TODO: Change this
    shouldDismissKeyboard: Boolean,
    onNetworkSelected: (Int) -> Unit,
    onAmountChange: (String, Boolean) -> Unit,
    maxAmountClicked: () -> Unit,
    onRecipientChange: (String) -> Unit,
    clearTransactionStatus: () -> Unit,
    resetQrScannerTrigger: () -> Unit,
    onKeyboardDismissed: () -> Unit,
    onBackClick: () -> Unit
) {
    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor


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
                onContentChanged = onRecipientChange,
                shouldDismissKeyboard = shouldDismissKeyboard,
                onKeyboardDismissed = onKeyboardDismissed
            )
        }

        TransactionStatusOverlay(
            status = transactionStatus,
            gifLoader = gifEnabledLoader,
            onDismiss = { clearTransactionStatus() },
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )
    }


    val barCodeLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            Log.d("QRScanner", "Scan result received: ${result.contents}")
            if(result.contents == null) {
                // Optional: Handle cancelled scan
                Log.d("QRScanner", "Scan was cancelled or no content found")
            } else {
                // Parse Ethereum URI according to EIP-681 spec
                // Format: ethereum:<address>[@<chain_id>][?<parameters>]
                val cleanAddress = parseEthereumUri(result.contents)
                Log.d("QRScanner", "Extracted address: $cleanAddress")
                onRecipientChange(cleanAddress)
            }
        }
    )

    val scanningPermissionsToRequest = listOf(
        Manifest.permission.CAMERA
    )

    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = scanningPermissionsToRequest
    )

    // Handle QR scanner trigger from ViewModel
    LaunchedEffect(qrScannerTriggered) {
        if (qrScannerTriggered) {
            if (multiplePermissionsState.allPermissionsGranted) {
                showCamera(barCodeLauncher)
            } else {
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
            resetQrScannerTrigger()
        }
    }

    LaunchedEffect(qrScannerTriggered) {
        if (qrScannerTriggered) {
            if (multiplePermissionsState.allPermissionsGranted) {
                showCamera(barCodeLauncher)
            } else {
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
            resetQrScannerTrigger()
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
        amountUiState = amountUiState,
        recipientUiState = recipientUiState,
        assetsUiState = assetsUiState,
        selectedAssetUiState = selectedAssetUiState,
        transactionStatus = null,
        qrScannerTriggered = false,
        shouldDismissKeyboard = false,
        onNetworkSelected = {},
        onAmountChange = {_,_ ->},
        maxAmountClicked = {},
        onRecipientChange = {},
        clearTransactionStatus = {},
        resetQrScannerTrigger = {},
        onKeyboardDismissed = {},
        onBackClick = {}
    )
}
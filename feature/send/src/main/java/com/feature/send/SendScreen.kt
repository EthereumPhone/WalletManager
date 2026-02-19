package com.feature.send

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.core.data.model.dto.Contact
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAssetWithPrice
import com.core.ui.util.SystemColorManager
import com.core.ui.util.mediumEnterDuration
import com.feature.send.ui.AmountTextField
import com.feature.send.ui.ContactPickerOverlay
import com.feature.send.ui.CustomCaptureActivity
import com.feature.send.ui.NetworkSelector
import com.feature.send.ui.RecipientSection
import com.example.dgenlibrary.ui.TransactionStatus
import com.example.dgenlibrary.ui.TransactionStatusOverlay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import android.Manifest
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.backgrounds.DgenHeaderGlobeBackground
//import com.example.dgenlibrary.ui.backgrounds.DgenHeaderGlobeBackground
import com.example.dgenlibrary.ui.backgrounds.LargeGlobeBackground
import com.example.dgenlibrary.ui.theme.DgenBackgroundHorizontalPadding
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.mediumExitDuration
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
    val transactionStatus by viewModel.transactionStatus.collectAsStateWithLifecycle()
    val shouldDismissKeyboard by viewModel.shouldDismissKeyboard.collectAsStateWithLifecycle()
    val selectedContact by viewModel.selectedContact.collectAsStateWithLifecycle()
    val contactsWithEth by viewModel.contactsWithEth.collectAsStateWithLifecycle()
    val shouldRequestContactsPermission by viewModel.shouldRequestContactsPermission.collectAsStateWithLifecycle()



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
            viewModel.onSendClosed()
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

    BackHandler {
        viewModel.onSendClosed()
        onBackClick()
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
            is TransactionStatus.FAILURE -> {
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
        selectedContact = selectedContact,
        contactsWithEth = contactsWithEth,
        shouldRequestContactsPermission = shouldRequestContactsPermission,
        onNetworkSelected = viewModel::changeSelectedAsset,
        onAmountChange = viewModel::updateAmount,
        maxAmountClicked = viewModel::setMaxAmount,
        onRecipientChange = viewModel::updateAddress,
        onContactSelected = viewModel::selectContact,
        onClearContact = viewModel::clearSelectedContact,
        onContactIconClick = viewModel::onContactIconClick,
        onContactsPermissionResult = viewModel::onContactsPermissionResult,
        clearTransactionStatus = viewModel::clearTransactionStatus,
        resetQrScannerTrigger = viewModel::resetQrScannerTrigger,
        onKeyboardDismissed = viewModel::onKeyboardDismissed,
        onBackClick = {
            viewModel.onSendClosed()
            onBackClick()
        }

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
    selectedContact: Contact? = null,
    contactsWithEth: List<Contact> = emptyList(),
    shouldRequestContactsPermission: Boolean = false,
    onNetworkSelected: (Int) -> Unit,
    onAmountChange: (String, Boolean) -> Unit,
    maxAmountClicked: () -> Unit,
    onRecipientChange: (String) -> Unit,
    onContactSelected: (Contact) -> Unit = {},
    onClearContact: () -> Unit = {},
    onContactIconClick: () -> Unit = {},
    onContactsPermissionResult: (Boolean) -> Unit = {},
    clearTransactionStatus: () -> Unit,
    resetQrScannerTrigger: () -> Unit,
    onKeyboardDismissed: () -> Unit,
    onBackClick: () -> Unit
) {
    // State for showing contact picker
    var showContactPicker by remember { mutableStateOf(false) }
    
    // Handle contacts permission request
    val contactsPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.READ_CONTACTS)
    )
    
    LaunchedEffect(shouldRequestContactsPermission) {
        if (shouldRequestContactsPermission) {
            contactsPermissionState.launchMultiplePermissionRequest()
        }
    }
    
    LaunchedEffect(contactsPermissionState.allPermissionsGranted) {
        if (contactsPermissionState.allPermissionsGranted && shouldRequestContactsPermission) {
            onContactsPermissionResult(true)
            showContactPicker = true
        } else if (!contactsPermissionState.allPermissionsGranted && 
                   contactsPermissionState.shouldShowRationale) {
            onContactsPermissionResult(false)
        }
    }
    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor


    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DgenHeaderGlobeBackground(
            onBackClick = onBackClick,
            headerContent = {
                SendHeader(
                    assetsUiState = assetsUiState,
                )
            },
            primaryColor = primaryColor,
            content = {

                Column(
                        Modifier.fillMaxSize().padding(start = DgenBackgroundHorizontalPadding, end = DgenBackgroundHorizontalPadding, bottom = 32.dp),
                ){
                        Spacer(Modifier.fillMaxWidth().height(48.dp))


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



                        // #region agent log
                        Log.d("DEBUG_AGENT", "SendScreen:RecipientSection - selectedContact=${selectedContact?.name ?: "null"}, contactsWithEthSize=${contactsWithEth.size}, hasContactsWithEth=${contactsWithEth.isNotEmpty()}, hypothesisId=C")
                        // #endregion

                        RecipientSection(
                            recipientUiState = recipientUiState,
                            selectedContact = selectedContact,
                            hasContactsWithEth = contactsWithEth.isNotEmpty(),
                            onContentChanged = onRecipientChange,
                            onContactIconClick = {
                                onContactIconClick()
                                if (contactsWithEth.isNotEmpty()) {
                                    showContactPicker = true
                                }
                            },
                            onClearContact = onClearContact,
                            shouldDismissKeyboard = shouldDismissKeyboard,
                            onKeyboardDismissed = onKeyboardDismissed
                        )
                    }
            }
        )
        TransactionStatusOverlay(
            status = transactionStatus,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            onDismiss = { clearTransactionStatus() }
        )

        // Contact picker overlay with fade in/out like send screen overlays
        AnimatedVisibility(
            visible = showContactPicker,
            enter = fadeIn(animationSpec = tween(durationMillis = mediumEnterDuration)),
            exit = fadeOut(animationSpec = tween(durationMillis = mediumEnterDuration))
        ) {
            ContactPickerOverlay(
                contacts = contactsWithEth,
                onContactSelected = { contact ->
                    onContactSelected(contact)
                    showContactPicker = false
                },
                onDismiss = { showContactPicker = false }
            )
        }
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
        selectedContact = null,
        contactsWithEth = emptyList(),
        onNetworkSelected = {},
        onAmountChange = {_,_ ->},
        maxAmountClicked = {},
        onRecipientChange = {},
        onContactSelected = {},
        onClearContact = {},
        onContactIconClick = {},
        onContactsPermissionResult = {},
        clearTransactionStatus = {},
        resetQrScannerTrigger = {},
        onKeyboardDismissed = {},
        onBackClick = {}
    )
}


//TODO: Move all this stuff under this line to a move appropriate context
fun parseEthereumUri(uri: String): String {
    // Parse Ethereum URI according to EIP-681 spec
    // Format: ethereum:<address>[@<chain_id>][?<parameters>]
    // Examples:
    // ethereum:0x1234567890123456789012345678901234567890
    // ethereum:0x1234567890123456789012345678901234567890@0x1
    // ethereum:0x1234567890123456789012345678901234567890@0x1?value=1000000000000000000

    var cleanUri = uri.trim()

    // Remove ethereum: prefix if present
    if (cleanUri.startsWith("ethereum:", ignoreCase = true)) {
        cleanUri = cleanUri.removePrefix("ethereum:")
    }

    // Split by @ to remove chain ID (e.g., @0x1)
    val addressPart = cleanUri.split("@").firstOrNull() ?: cleanUri

    // Split by ? to remove query parameters
    val finalAddress = addressPart.split("?").firstOrNull() ?: addressPart

    return finalAddress.trim()
}

fun showCamera(
    cameraLauncher: ManagedActivityResultLauncher<ScanOptions?, ScanIntentResult?>
) {
    try {
        val options = ScanOptions()
        // Verwende die neue CustomCaptureActivity
        options.setCaptureActivity(CustomCaptureActivity::class.java)
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("") // Kein Prompt, da wir unseren eigenen Text haben
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setOrientationLocked(true) // Portrait only
        cameraLauncher.launch(options)
    } catch (e: Exception) {
        Log.e("QRScanner", "Error launching camera: ${e.message}", e)
    }
}

object TransactionTiming {
    // How long to show the SUCCESS overlay before starting navigation (in milliseconds)
    const val SUCCESS_DISPLAY_DURATION = 4000L // 4 seconds to enjoy the success

    // How long to show the FAILURE overlay before starting navigation (in milliseconds)
    const val FAILURE_DISPLAY_DURATION = 2500L // 2.5 seconds for failure state

    // Delay between starting navigation and clearing the overlay for smooth fade transition (in milliseconds)
    const val FADE_TRANSITION_DURATION = 1000L // 1 second fade overlap
}


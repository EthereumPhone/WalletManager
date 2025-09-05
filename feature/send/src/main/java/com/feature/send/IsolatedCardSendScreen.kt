package com.feature.send

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.style.TextAlign
import com.core.ui.DgenLoadingMatrix
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenGreen
import com.core.ui.util.dgenOrche
import com.core.ui.util.dgenRed
import com.core.ui.util.dgenWhite
import com.feature.send.ui.SelectableCarousel
import com.feature.send.ui.TextToggle
import com.feature.send.ui.TransactionStatusOverlay
import com.feature.send.ui.TransactionStatus
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.core.ui.DgenBasicTextfield
import com.core.ui.HeaderBar
import androidx.compose.ui.draw.scale
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.feature.send.ui.CustomCaptureActivity
import org.web3j.crypto.WalletUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.ColorFilter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.core.ui.util.formatWithSuffix
import androidx.compose.ui.unit.TextUnit
import com.core.ui.SimpleDgenTextfield
import kotlin.math.abs
import java.util.Locale
import androidx.compose.ui.text.TextRange
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.extraLargeEnterDuration
import com.core.ui.util.extraLargeExitDuration
import com.core.ui.util.label_fontSize
import com.core.ui.util.smallDuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import com.core.model.TokenAssetWithPrice
import com.core.ui.showDgenToast
import com.core.ui.util.pulseOpacity
import com.core.ui.util.TokenLogoFallback
import java.math.BigDecimal

// ===== CONFIGURABLE TRANSACTION OVERLAY DURATIONS =====
// These constants control the timing of transaction status overlays and navigation
object TransactionTiming {
    // How long to show the SUCCESS overlay before starting navigation (in milliseconds)
    const val SUCCESS_DISPLAY_DURATION = 4000L // 4 seconds to enjoy the success
    
    // How long to show the FAILURE overlay before starting navigation (in milliseconds)
    const val FAILURE_DISPLAY_DURATION = 2500L // 2.5 seconds for failure state
    
    // Delay between starting navigation and clearing the overlay for smooth fade transition (in milliseconds)
    const val FADE_TRANSITION_DURATION = 1000L // 1 second fade overlap
}

// Copied and adapted from IdleCardView.kt
private fun calculateSendScreenMaxAmountFontSize(text: String): TextUnit {
    // Adjusted for potentially smaller display area compared to IdleCardView
    return when {
        text.length <= 4 -> 20.sp // Slightly larger for small numbers
        text.length <= 5 -> 18.sp // Base size
        text.length <= 6 -> 16.sp
        text.length <= 7 -> 14.sp
        else -> 12.sp // Min size
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SendRoute2(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    initialAddress: String?,
    groupId: String?,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: SendViewModel = hiltViewModel()
) {
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val toAddress by viewModel.recipientUiState.collectAsStateWithLifecycle()
    val assetsUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val selectedToken by viewModel.selectedAssetUiState.collectAsStateWithLifecycle()
    val qrScannerTriggered by viewModel.qrScannerTriggered.collectAsStateWithLifecycle()
    val sendTransactionTriggered by viewModel.sendTransactionTriggered.collectAsStateWithLifecycle()
    val transactionStatus by viewModel.transactionStatus.collectAsStateWithLifecycle()


    // Flag to ensure the first ON_RESUME (which happens on the initial screen launch) is ignored
    var hasHandledInitialResume by remember { mutableStateOf(false) }

    // Track if user is navigating back to home
    var isNavigatingBack by remember { mutableStateOf(false) }

    // Observe lifecycle events to handle app resume
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

    // Remove QR code from secondary screen when this screen is disposed/closed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onScreenClosed(clearLed = !isNavigatingBack)
        }
    }

    SendScreen2(
        initialAddress = initialAddress,
        modifier = Modifier,
        onBackClick = {
            isNavigatingBack = true
            onBackClick()
        },
        recipientUiState = toAddress,
        amount = amount,
        assets = assetsUiState,
        selectedToken = selectedToken,
        onAmountChange = viewModel::updateAmount,
        onToAddressChanged = viewModel::updateToAddress,
        sendTransaction = viewModel::send,
        updateSelectedAsset = viewModel::changeSelectedAsset,
        qrScannerTriggered = qrScannerTriggered,
        resetQrScannerTrigger = viewModel::resetQrScannerTrigger,
        sendTransactionTriggered = sendTransactionTriggered,
        resetSendTransactionTrigger = viewModel::resetSendTransactionTrigger,
        transactionStatus = transactionStatus,
        clearTransactionStatus = viewModel::clearTransactionStatus,
        showFailedMatrix = viewModel::showFailedMatrix,
        showSuccessMatrix = viewModel::showSuccessMatrix,
        setMaxAmount = viewModel::setMaxAmount
    )
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalPermissionsApi::class)
@Composable
fun SendScreen2(
    modifier: Modifier = Modifier,
    recipientUiState: RecipientUiState,
    amount: String,
    assets: AssetsUiState,
    onAmountChange: (String) -> Unit,
    onToAddressChanged: (String) -> Unit,
    sendTransaction: (() -> Unit) -> Unit,
    updateSelectedAsset: (TokenAssetWithPrice) -> Unit,
    selectedToken: SelectedTokenUiState,
    onBackClick: () -> Unit,
    initialAddress: String?,
    qrScannerTriggered: Boolean,
    resetQrScannerTrigger: () -> Unit,
    sendTransactionTriggered: Boolean,
    resetSendTransactionTrigger: () -> Unit,
    showFailedMatrix: () -> Unit,
    showSuccessMatrix: () -> Unit,
    transactionStatus: TransactionStatus?,
    clearTransactionStatus: () -> Unit,
    setMaxAmount: (BigDecimal, Int) -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()



    // Monitor transaction status for auto-dismiss of FAILURE state
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
                clearTransactionStatus()
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
                clearTransactionStatus()
            }
            else -> {
                Log.d("SendScreen", "Other status: $transactionStatus - no auto-navigation")
            }
        }
        Log.d("SendScreen", "=== TRANSACTION STATUS HANDLING ENDED ===")
    }

    var useDollarAmount by remember { mutableStateOf(false) }
    
    // TextFieldValue for amount, for keeping Cursor-Position
    var amountFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    
    // Synchronize amountFieldValue with amount from ViewModel
    LaunchedEffect(amount) {
        // only update if text is different (avoids cursor reset)
        if (amount != amountFieldValue.text) {
            amountFieldValue = TextFieldValue(amount)
        }
    }
    
    // TextFieldValue for toAddress, for keeping Cursor-Position
    var toAddressFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    
    // Variables for error handling
    var isAmountError by remember { mutableStateOf(false) }
    var convertedTokenAmount by remember { mutableStateOf("") }


    //Loader for GIF
    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if ( SDK_INT >= 28 ) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()

    //Variables for QR Scanner
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
                onToAddressChanged(cleanAddress)
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

    // Handle permission result for ViewModel-triggered QR scanner
    LaunchedEffect(multiplePermissionsState.allPermissionsGranted) {
        if (qrScannerTriggered && multiplePermissionsState.allPermissionsGranted) {
            showCamera(barCodeLauncher)
            resetQrScannerTrigger()
        }
    }

    //Variable for animating the max button
    var setMax by remember { mutableStateOf(false) }
    var isMaxAmount by remember { mutableStateOf(false) }  // Track if MAX was used
    val maxAlpha by animateFloatAsState(
        targetValue = if (isMaxAmount) {
            1f
        } else {
            pulseOpacity
        },
        animationSpec = tween(smallDuration,easing = FastOutLinearInEasing),
    )


    LaunchedEffect(Unit) {
        SystemColorManager.refresh(context)
    }

    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // Clear focus when tapping outside of text fields
                        focusManager.clearFocus()
                    }
                )
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

        AnimatedContent(
            assets,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(extraLargeEnterDuration)
                ) togetherWith fadeOut(animationSpec = tween(extraLargeExitDuration))
            },
            modifier = Modifier.fillMaxSize(),
            label = "Animated Content"
        ) { assetsUiState ->

            when(assetsUiState){
                AssetsUiState.Empty -> {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                            Text(
                                text = "EMPTY",
                                style = TextStyle(
                                    fontFamily = PitagonsSans,
                                    color = primaryColor.copy(pulseOpacity),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 24.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.width(300.dp)
                            )
                    }

                }
                AssetsUiState.Error -> {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            text = "ERROR",
                            style = TextStyle(
                                fontFamily = PitagonsSans,
                                color = primaryColor.copy(pulseOpacity),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.width(300.dp)
                        )
                    }
                }
                AssetsUiState.Loading -> {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        DgenLoadingMatrix(
                            unactiveLEDColor = secondaryColor,
                            activeLEDColor = primaryColor
                        )
                    }
                }
                is AssetsUiState.Success -> {

                    val availableChains = assetsUiState.assets.map { it.chainId }


                    // Validate Address
                    val isValidAddress = remember(recipientUiState, toAddressFieldValue.text, isResolvingENS, ensError) {
                        when {
                            recipientUiState.isEmpty() -> false
                            toAddressFieldValue.text.endsWith(".eth") -> !isResolvingENS && ensError == null && recipientUiState.isNotEmpty()
                            else -> WalletUtils.isValidAddress(recipientUiState)
                        }
                    }


                    // Handle send transaction trigger from ViewModel
                    LaunchedEffect(sendTransactionTriggered) {
                        if (sendTransactionTriggered) {
                            focusManager.clearFocus()
                            Log.d("SendScreen", "=== SEND TRANSACTION TRIGGERED ===")
                            Log.d("SendScreen", "Validating transaction parameters...")
                            Log.d("SendScreen", "Amount error: $isAmountError, Max amount: $isMaxAmount")
                            Log.d("SendScreen", "Valid address: $isValidAddress")
                            Log.d("SendScreen", "Selected token: $selectedToken")
                            Log.d("SendScreen", "To address: ${recipientUiState}")
                            Log.d("SendScreen", "Amount: ${amount}")
                            
                            // Use the same validation logic as the removed button
                            if ((isAmountError && !isMaxAmount) || !isValidAddress || selectedToken == SelectedTokenUiState.Unselected) {
                                Log.w("SendScreen", "🔴 Validation failed, showing error message")
                                // Show specific error messages
                                when {
                                    isAmountError && !isMaxAmount -> {
                                        Log.w("SendScreen", "Error: Insufficient balance")
                                        showDgenToast(context,"Insufficient balance")
                                        showFailedMatrix()
                                    }
                                    recipientUiState.isEmpty() -> {
                                        Log.w("SendScreen", "Error: Enter target address")
                                        showDgenToast(context,"Enter target address")
                                        showFailedMatrix()
                                    }
                                    isResolvingENS -> {
                                        Log.w("SendScreen", "Error: Resolving ENS name...")
                                        showDgenToast(context,"Resolving ENS name...", toastBackgroundColor = dgenOrche)
                                    }
                                    ensError != null -> {
                                        Log.w("SendScreen", "Error: ENS error - $ensError")
                                        showDgenToast(context,ensError ?: "ENS error")
                                        showFailedMatrix()
                                    }
                                    !isValidAddress -> {
                                        Log.w("SendScreen", "Error: Invalid address format")
                                        showDgenToast(context,"Invalid address format")
                                        showFailedMatrix()
                                    }
                                    selectedToken == SelectedTokenUiState.Unselected -> {
                                        Log.w("SendScreen", "Error: Select a chain")
                                        showDgenToast(context,"Select a chain")
                                        showFailedMatrix()
                                    }
                                }
                            } else {
                                Log.d("SendScreen", "✅ Validation passed, proceeding with transaction")
                                // Get current amounts and token symbol
                                val currentDollarAmount = if (useDollarAmount) dollarAmount.text else ""
                                val currentTokenAmount = if (!useDollarAmount) amount else ""
                                
                                if (currentDollarAmount.isEmpty() && currentTokenAmount.isEmpty()) {
                                    Log.w("SendScreen", "Error: No amount specified")
                                    showDgenToast(context, "Type in an amount")
                                    showFailedMatrix()
                                } else {
                                    Log.d("SendScreen", "🟡 Executing transaction (ViewModel will set PENDING status)")
                                    
                                    // Execute transaction - the ViewModel will handle setting PENDING status
                                    val finalAmount = if (isMaxAmount) {
                                        // Use exact balance for MAX amount
                                        availableBalance.toString()
                                    } else if (useDollarAmount) {
                                        convertedTokenAmount
                                    } else {
                                        currentTokenAmount
                                    }
                                    
                                    Log.d("SendScreen", "Final amount to send: $finalAmount")
                                    
                                    if (finalAmount.isNotEmpty()) {
                                        onAmountChange(finalAmount)
                                        Log.d("SendScreen", "Calling sendTransaction with callback...")
                                        sendTransaction {
                                            // Callback after send operation - the status will be handled by ViewModel
                                            Log.d("SendScreen", "✅ Transaction callback executed")
//                                            showSuccessMatrix()
                                        }
                                    } else {
                                        Log.w("SendScreen", "Error: Final amount is empty")
                                    }
                                }
                            }
                            Log.d("SendScreen", "Resetting send transaction trigger")
                            resetSendTransactionTrigger()
                            Log.d("SendScreen", "=== SEND TRANSACTION TRIGGER HANDLED ===")
                        }
                    }

                    Box(
                        Modifier.fillMaxSize()
                    ) {
                        val customTextSelectionColors = TextSelectionColors(
                            handleColor = Color.Transparent,
                            backgroundColor = primaryColor.copy(alpha = 0.4f)
                        )
                        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = modifier
                                    .fillMaxSize()
                                    .padding(bottom = 24.dp)
                            ){
                                HeaderBar(content = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "SEND",
                                            style = TextStyle(
                                                fontFamily = SpaceMono,
                                                color = primaryColor,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 24.sp,
                                                letterSpacing = 0.sp,
                                                textDecoration = TextDecoration.None
                                            )
                                        )

                                        // Show token logo based on selectedToken
                                        when (selectedToken) {
                                            is SelectedTokenUiState.Selected -> {
                                                val token = selectedToken.tokenAsset

                                                // Token Logo with fallback support
                                                val fallbackLogo = TokenLogoFallback.getFallbackLogo(token.symbol)
                                                
                                                when {
                                                    // First try the token's logoUrl if available
                                                    !token.logoUrl.isNullOrEmpty() -> {
                                                        AsyncImage(
                                                            model = token.logoUrl,
                                                            contentDescription = token.name,
                                                            modifier = Modifier
                                                                .size(28.dp)
                                                                .clip(CircleShape),
                                                            placeholder = painterResource(R.drawable.placeholer_icon_5),
                                                            error = painterResource(R.drawable.placeholer_icon_5)
                                                        )
                                                    }
                                                    // Then check for URL fallback
                                                    fallbackLogo is TokenLogoFallback.LogoSource.Url -> {
                                                        AsyncImage(
                                                            model = fallbackLogo.url,
                                                            contentDescription = token.name,
                                                            modifier = Modifier
                                                                .size(28.dp)
                                                                .clip(CircleShape),
                                                            placeholder = painterResource(R.drawable.placeholer_icon_5),
                                                            error = painterResource(R.drawable.placeholer_icon_5)
                                                        )
                                                    }
                                                    // Then check for local resource fallback
                                                    fallbackLogo is TokenLogoFallback.LogoSource.LocalResource -> {
                                                        Image(
                                                            modifier = Modifier.size(28.dp),
                                                            painter = painterResource(fallbackLogo.resourceId),
                                                            contentDescription = token.name
                                                        )
                                                    }
                                                    // Finally use hardcoded mappings (temporary until drawables are added)
                                                    else -> {
                                                        val tokenLogo = when(token.symbol.uppercase()) {
                                                            "MAINNET" -> R.drawable.mainnet
                                                            "OPTIMISM" -> R.drawable.mainnet
                                                            "ARBITRUM" -> R.drawable.mainnet
                                                            "POLYGON" -> R.drawable.polygon
                                                            "SEPOLIA" -> R.drawable.mainnet
                                                            "BASE" -> R.drawable.mainnet
                                                            "ZORA" -> R.drawable.mainnet
                                                            "WETH" -> R.drawable.placeholer_icon_5 // WETH placeholder
                                                            else -> R.drawable.placeholer_icon_5
                                                        }
                                                        Image(
                                                            modifier = Modifier.size(28.dp),
                                                            painter = painterResource(tokenLogo),
                                                            contentDescription = token.name
                                                        )
                                                    }
                                                }



                                                val tokenName = when(token.symbol.uppercase()){
                                                    "MAINNET" -> "ETH"
                                                    "OPTIMISM" -> "ETH"
                                                    "ARBITRUM" -> "ETH"
                                                    "POLYGON" -> "MATIC"
                                                    "SEPOLIA" -> "ETH"
                                                    "BASE" -> "ETH"
                                                    "ZORA" -> "ETH"
                                                    "WETH" -> "WETH" // Show WETH as WETH, not ETH
                                                    else -> {
                                                        token.symbol.uppercase()
                                                    }
                                                }
                                                // Token Symbol
                                                Text(
                                                    text = tokenName,
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
                                            else -> {
                                                // Fallback to ETH when no token is selected
                                                Image(
                                                    modifier = Modifier
                                                        .size(28.dp),
                                                    painter = painterResource(R.drawable.ethereum_placeholder),
                                                    contentDescription = "Ethereum"
                                                )
                                                Text(
                                                    text = "ETH",
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
                                        }
                                    }
                                }, primaryColor = primaryColor, onClick = onBackClick, modifier = modifier.padding(horizontal = 24.dp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {

                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ){
                                        Spacer(Modifier
                                            .offset(y = 5.dp)
                                            .height(77.dp)
                                            .width(8.dp)
                                            .background(primaryColor.copy(pulseOpacity))
                                            .padding(end = 8.dp)
                                        )
                                        Column(
                                                modifier = Modifier
                                            ,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)

                                        ) {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            TextToggle(
                                                Modifier.offset(x = 2.dp, y=2.dp),
                                                when (selectedToken) {
                                                    is SelectedTokenUiState.Selected -> {
                                                        when (selectedToken.tokenAsset.symbol.uppercase()) {
                                                            "MAINNET" -> "ETH"
                                                            "OPTIMISM" -> "ETH"
                                                            "ARBITRUM" -> "ETH"
                                                            "POLYGON" -> "MATIC"
                                                            "SEPOLIA" -> "ETH"
                                                            "BASE" -> "ETH"
                                                            "ZORA" -> "ETH"
                                                            else -> selectedToken.tokenAsset.symbol.uppercase()
                                                        }
                                                    }
                                                    else -> "ETH"
                                                },
                                                "$",
                                                onToggle = {
                                                    useDollarAmount = !useDollarAmount
                                                    isMaxAmount = false  // Reset MAX when toggling
                                                    scope.launch{
                                                        delay(200)
                                                        dollarAmount = TextFieldValue("")
                                                        amountFieldValue = TextFieldValue("")
                                                        onAmountChange("")
                                                    }

                                                },
                                                value = useDollarAmount,
                                                primaryColor = primaryColor
                                            )

                                            val valueString = remember(availableBalance, useDollarAmount, selectedToken) {
                                                if (useDollarAmount) {
                                                    if (availableBalance > 0) {
                                                        val tokenSymbolForPriceLookup = when (val currentSelectedToken = selectedToken) {
                                                            is SelectedTokenUiState.Selected -> {
                                                                val assetSymbolUpper = currentSelectedToken.tokenAsset.symbol.uppercase()
                                                                when (assetSymbolUpper) {
                                                                    "MAINNET", "OPTIMISM", "ARBITRUM", "SEPOLIA", "BASE", "ZORA" -> "ETH"
                                                                    "POLYGON" -> "MATIC"
                                                                    else -> assetSymbolUpper
                                                                }
                                                            }
                                                            else -> "ETH"
                                                        }
                                                        //TODO: CHANGE WITH REAL
                                                        val currentPrice = 12.2

                                                        if (currentPrice != null && currentPrice > 0) {
                                                            (availableBalance * currentPrice).formatWithSuffix(maxDecimals = 2)
                                                        } else {
                                                            0.0.formatWithSuffix(maxDecimals = 2) // Fallback if price not available, now uses 2 decimals for $ value
                                                        }
                                                    } else {
                                                        0.0.formatWithSuffix(maxDecimals = 2) // No balance, now uses 2 decimals for $ value
                                                    }
                                                } else { // Token amount
                                                    if (availableBalance > 0.0) {
                                                        if (abs(availableBalance) >= 1000.0) {
                                                            // If amount is 1000 or more, use formatWithSuffix (which applies K, M, B, T)
                                                            // formatWithSuffix uses 2 decimals when a suffix is present by default.
                                                            availableBalance.formatWithSuffix()
                                                        } else {
                                                            // For amounts less than 1000, use precise formatting up to 6 decimals
                                                            String.format(Locale.US, "%.6f", availableBalance).trimEnd('0').trimEnd('.')
                                                        }
                                                    } else { // availableBalance is 0.0 or less
                                                        "0" // For zero or negative balance, display "0"
                                                    }
                                                }
                                            }


                                            Text(
                                                text = buildAnnotatedString {
                                                    withStyle(style = SpanStyle(fontFamily = SpaceMono, fontSize = 18.sp)) {
                                                        append("MAX ") // "MAX "
                                                    }
                                                    // Append currency symbol and value in PitagonsSans with dynamic font size
                                                    if (useDollarAmount) {
                                                        withStyle(style = SpanStyle(fontFamily = PitagonsSans, fontSize = 18.sp)) {
                                                            append("$")
                                                        }
                                                    }
                                                    withStyle(style = SpanStyle(fontFamily = PitagonsSans, fontSize = 18.sp)) {
                                                        append(valueString)
                                                    }
                                                },
                                                color = primaryColor.copy(maxAlpha),
                                                fontWeight = FontWeight.SemiBold,
                                                lineHeight = 18.sp,
                                                letterSpacing = 0.sp,
                                                textDecoration = TextDecoration.None,
                                                modifier = Modifier
                                                    .offset(y = 2.dp)
                                                    .pointerInput(Unit) {
                                                        detectTapGestures {
                                                            setMax = !setMax
                                                        }
                                                    }
                                            )


                                        }

                                        Row(
                                            Modifier.fillMaxWidth(),
                                        ){
                                            Crossfade(
                                                useDollarAmount,
                                                animationSpec = tween(smallDuration),
                                            ) { usedollar ->
                                                if(usedollar){
                                                    DgenBasicTextfield(
                                                        value = dollarAmount,
                                                        onValueChange = { new ->
                                                            isMaxAmount = false  // Reset when user manually changes amount
                                                            val cleanInput = new.text.removePrefix("$")

                                                            // Allow only numbers and a single dot, no commas or spaces
                                                            if (cleanInput.matches("^\\d*\\.?\\d*$".toRegex())) {
                                                                if (cleanInput.isEmpty()) {
                                                                    dollarAmount = TextFieldValue("")
                                                                } else {
                                                                    val newText = "$$cleanInput"
                                                                    dollarAmount = TextFieldValue(
                                                                        text = newText,
                                                                        selection = TextRange(newText.length)
                                                                    )
                                                                }
                                                            }
                                                        },
                                                        maxLines = 1,
                                                        maxLength = 15,
                                                        placeholder = {
                                                            Row (
                                                                Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.Start
                                                            ){
                                                                Text(
                                                                    modifier = Modifier,
                                                                    text =  buildAnnotatedString {
                                                                        withStyle(
                                                                            style = SpanStyle(
                                                                                fontFamily = PitagonsSans,
                                                                                color = dgenWhite.copy(alpha = pulseOpacity),
                                                                                fontWeight = FontWeight.SemiBold,
                                                                                fontSize = 39.sp,
                                                                            )
                                                                        ){
                                                                            append("$")
                                                                        }
                                                                        append("0.0") // Static placeholder
                                                                    },
                                                                    style = TextStyle(
                                                                        fontFamily = PitagonsSans,
                                                                        color = dgenWhite.copy(alpha = pulseOpacity),
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        fontSize = 42.sp,
                                                                        textAlign = TextAlign.Start
                                                                    ),
                                                                )
                                                            }

                                                        },
                                                        textStyle = TextStyle(
                                                            fontFamily = PitagonsSans,
                                                            color = if (isAmountError) dgenRed else dgenWhite,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 42.sp,
                                                            textAlign = TextAlign.Start
                                                        ),
                                                        keyboardtype =  KeyboardType.Number,
                                                        cursorWidth = 24.dp,
                                                        cursorHeight= 24.dp,
                                                        cursorColor = primaryColor,
                                                        isAnyFieldFocused= remember { mutableStateOf(false) },
                                                    )
                                                }else{
                                                    DgenBasicTextfield(
                                                        value = amountFieldValue,
                                                        onValueChange={ new ->
                                                            isMaxAmount = false  // Reset when user manually changes amount

                                                            // Allow only numbers and a single dot
                                                            if (new.text.matches("^\\d*\\.?\\d*$".toRegex())) {
                                                                amountFieldValue = new
                                                                onAmountChange(new.text)
                                                            }
                                                        },
                                                        maxLines = 1,
                                                        maxLength = 15,
                                                        cursorColor = primaryColor,
                                                        placeholder = {
                                                            Row (
                                                                Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.Start
                                                            ){
                                                                Text(
                                                                    modifier = Modifier,
                                                                    text = "0.0", // Static placeholder
                                                                    style = TextStyle(
                                                                        fontFamily = PitagonsSans,
                                                                        color = dgenWhite.copy(alpha = pulseOpacity),
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        fontSize = 42.sp,
                                                                        textAlign = TextAlign.Start
                                                                    ),
                                                                )
                                                            }

                                                        },
                                                        textStyle = TextStyle(
                                                            fontFamily = PitagonsSans,
                                                            color = if (isAmountError) dgenRed else dgenWhite,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 42.sp,
                                                            textAlign = TextAlign.Start
                                                        ),
                                                        keyboardtype =  KeyboardType.Number,
                                                        cursorWidth = 24.dp,
                                                        cursorHeight= 24.dp,
                                                        isAnyFieldFocused= remember { mutableStateOf(false) },
                                                    )
                                                }

                                            }
                                        }


                                        LaunchedEffect(dollarAmount.text, useDollarAmount, selectedToken) {
                                            if (useDollarAmount && dollarAmount.text.isNotEmpty()) {
                                                delay(500)

                                                val tokenSymbol = when (selectedToken) {
                                                    is SelectedTokenUiState.Selected -> {
                                                        when (selectedToken.tokenAsset.symbol.uppercase()) {
                                                            "MAINNET", "OPTIMISM", "ARBITRUM", "SEPOLIA", "BASE", "ZORA" -> "ETH"
                                                            "POLYGON" -> "MATIC"
                                                            else -> selectedToken.tokenAsset.symbol.uppercase()
                                                        }
                                                    }
                                                    else -> "ETH"
                                                }


                                                // Calculate the converted token amount for validation
                                                try {
                                                    val dollarValue = dollarAmount.text.removePrefix("$").toDoubleOrNull() ?: 0.0

                                                    // TODO: CHANGE TO REAL
                                                    val currentPrice = 12.3

                                                    if (currentPrice != null && currentPrice > 0) {
                                                        convertedTokenAmount = (dollarValue / currentPrice).toString()
                                                    } else {
                                                        convertedTokenAmount = "0"
                                                    }
                                                } catch (e: Exception) {
                                                    // Error handling
                                                }
                                            } else if (!useDollarAmount) {
                                                convertedTokenAmount = ""
                                            }
                                        }

                                        // Add LaunchedEffect for MAX functionality
                                        LaunchedEffect(setMax, availableBalance, selectedToken, useDollarAmount) {
                                            if (setMax && availableBalance > 0) {
                                                isMaxAmount = true  // Set flag when MAX is used
                                                val formattedBalance = String.format("%.6f", availableBalance).trimEnd('0').trimEnd('.')

                                                if (useDollarAmount) {
                                                    // Calculate dollar value from token balance
                                                    val tokenSymbol = when (selectedToken) {
                                                        is SelectedTokenUiState.Selected -> {
                                                            when (selectedToken.tokenAsset.symbol.uppercase()) {
                                                                "MAINNET", "OPTIMISM", "ARBITRUM", "SEPOLIA", "BASE", "ZORA" -> "ETH"
                                                                "POLYGON" -> "MATIC"
                                                                else -> selectedToken.tokenAsset.symbol.uppercase()
                                                            }
                                                        }
                                                        else -> "ETH"
                                                    }

                                                    //TODO: CHANGE TO REAL ONE
                                                    val currentPrice = 12.3

                                                    if (currentPrice != null && currentPrice > 0) {
                                                        val dollarValue = availableBalance * currentPrice
                                                        val formattedDollar = String.format("%.2f", dollarValue)
                                                        val newText = "$$formattedDollar"
                                                        dollarAmount = TextFieldValue(
                                                            text = newText,
                                                            selection = TextRange(newText.length)
                                                        )
                                                    }
                                                } else {
                                                    // For native tokens, use setMaxAmount to calculate gas-adjusted amount
                                                    val isNativeToken = when (selectedToken) {
                                                        is SelectedTokenUiState.Selected -> {
                                                            selectedToken.tokenAsset.address == selectedToken.tokenAsset.chainId.toString()
                                                        }
                                                        else -> true // Default to native if nothing selected
                                                    }
                                                    
                                                    if (isNativeToken && selectedChainId != null) {
                                                        // Call setMaxAmount for native tokens to get gas-adjusted amount
                                                        setMaxAmount(BigDecimal(availableBalance), selectedChainId)
                                                    } else {
                                                        // For ERC20 tokens, use full balance
                                                        amountFieldValue = TextFieldValue(formattedBalance)
                                                        onAmountChange(formattedBalance)
                                                    }
                                                }

                                                // Reset setMax after setting the value
                                                setMax = false
                                            }
                                        }

                                    }
                                    }



                                    // Chain selection based on token availability
                                    // REMOVED - Already defined above before availableBalance
                                    
                                    // Set the initial chain  based on the selected token
                                    LaunchedEffect(selectedToken, availableChains) {
                                        when (selectedToken) {
                                            is SelectedTokenUiState.Selected -> {
                                                // If a token is already selected, find its chain index
                                                val tokenChainName = when (selectedToken.tokenAsset.chainId) {
                                                    1 -> "main"
                                                    11155111 -> "sepolia"
                                                    10 -> "op"
                                                    137 -> "pol"
                                                    42161 -> "arb"
                                                    8453 -> "base"
                                                    7777777 -> "zora"
                                                    else -> null
                                                }
                                                
                                                tokenChainName?.let { chainName ->
                                                    val chainIndex = availableChains.indexOf(chainName)
                                                    if (chainIndex >= 0) {
                                                        selectedChainIndex = chainIndex
                                                    }
                                                }
                                            }
                                            else -> {
                                                // Only auto-select if there's exactly one chain and no token is selected
                                                if (availableChains.size == 1 && assetsUiState is AssetsUiState.Success) {
                                                    val chainName = availableChains.first()
                                                    val chainId = when (chainName) {
                                                        "main" -> 1
                                                        "sepolia" -> 11155111
                                                        "op" -> 10
                                                        "pol" -> 137
                                                        "arb" -> 42161
                                                        "base" -> 8453
                                                        "zora" -> 7777777
                                                        else -> null
                                                    }
                                                    
                                                    chainId?.let { id ->
                                                        val chainIndex = availableChains.indexOf(chainName)
                                                        if (chainIndex >= 0) {
                                                            selectedChainIndex = chainIndex
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Ensure a token is selected when a chain is already chosen (initial load)
                                    LaunchedEffect(selectedChainIndex, selectedToken) {
                                        if (!tokenPreselected && selectedToken == SelectedTokenUiState.Unselected && assetsUiState is AssetsUiState.Success) {
                                            val selectedChainName = availableChains.getOrNull(selectedChainIndex)
                                            val selectedChainId = when (selectedChainName) {
                                                "main" -> 1
                                                "sepolia" -> 11155111
                                                "op" -> 10
                                                "pol" -> 137
                                                "arb" -> 42161
                                                "base" -> 8453
                                                "zora" -> 7777777
                                                else -> null
                                            }
                                            selectedChainId?.let { chainId ->
                                                val nativeToken = assetsUiState.assets.firstOrNull { asset ->
                                                    asset.chainId == chainId && asset.address == chainId.toString()
                                                }
                                                nativeToken?.let { updateSelectedAsset(it) }
                                            }
                                        }
                                    }

                                    SelectableCarousel(
                                        modifier = modifier.offset(x = 0.dp),
                                        items = availableChains,
                                        itemWidth = 65.dp,
                                        itemHeight = 65.dp,
                                        initialSelectedIndex = selectedChainIndex,
                                        onItemSelected = { index -> 
                                            val newIndex = index ?: 0
                                            
                                            // Only process if actually changing to a different chain
                                            if (selectedChainIndex != newIndex) {
                                                val oldChainName = availableChains.getOrNull(selectedChainIndex)
                                                val newChainName = availableChains.getOrNull(newIndex)
                                                Log.d("SendScreen2", "Chain selection changed from $oldChainName (index $selectedChainIndex) to $newChainName (index $newIndex)")
                                                
                                                selectedChainIndex = newIndex
                                                
                                                // Handle token selection based on current state
                                                if (assetsUiState is AssetsUiState.Success) {
                                                    val selectedChainName = availableChains.getOrNull(newIndex)
                                                    val selectedChainId = when (selectedChainName) {
                                                        "main" -> 1
                                                        "sepolia" -> 11155111
                                                        "op" -> 10
                                                        "pol" -> 137
                                                        "arb" -> 42161
                                                        "base" -> 8453
                                                        "zora" -> 7777777
                                                        else -> null
                                                    }
                                                    
                                                    Log.d("SendScreen2", "Selected chain: $selectedChainName -> chainId: $selectedChainId")
                                                    
                                                    selectedChainId?.let { chainId ->
                                                        when (selectedToken) {
                                                            // If no token is selected, select the native token
                                                            is SelectedTokenUiState.Unselected -> {
                                                                if (!tokenPreselected) {
                                                                    val nativeToken = assetsUiState.assets.firstOrNull { asset ->
                                                                        asset.chainId == chainId && 
                                                                        asset.address == chainId.toString()
                                                                    }
                                                                    nativeToken?.let {
                                                                        updateSelectedAsset(it)
                                                                    }
                                                                }
                                                            }
                                                            // If a token is selected, check if it's native or ERC20
                                                            is SelectedTokenUiState.Selected -> {
                                                                val currentToken = selectedToken.tokenAsset
                                                                // If current token is native, switch to the native token of the new chain
                                                                if (currentToken.address == currentToken.chainId.toString()) {
                                                                    val nativeToken = assetsUiState.assets.firstOrNull { asset ->
                                                                        asset.chainId == chainId && 
                                                                        asset.address == chainId.toString()
                                                                    }
                                                                    nativeToken?.let {
                                                                        updateSelectedAsset(it)
                                                                    }
                                                                } else {
                                                                    // If current token is ERC20, find the same token on the new chain
                                                                    Log.d("SendScreen2", "Current ERC20 token: ${currentToken.symbol} (${currentToken.address}) on chain ${currentToken.chainId}")
                                                                    Log.d("SendScreen2", "Looking for matching token on chain $chainId")
                                                                    
                                                                    val matchingToken = assetsUiState.assets.firstOrNull { asset ->
                                                                        val isMatchingChain = asset.chainId == chainId
                                                                        val isERC20 = asset.address != asset.chainId.toString()
                                                                        val isSymbolMatch = if (currentToken.symbol.equals("WETH", ignoreCase = true)) {
                                                                            // For WETH, match by symbol since addresses differ across chains
                                                                            asset.symbol.equals("WETH", ignoreCase = true)
                                                                        } else {
                                                                            // For other tokens, match by symbol
                                                                            asset.symbol.equals(currentToken.symbol, ignoreCase = true)
                                                                        }
                                                                        
                                                                        if (isMatchingChain && isERC20 && isSymbolMatch) {
                                                                            Log.d("SendScreen2", "Found potential match: ${asset.symbol} (${asset.address}) on chain ${asset.chainId}, balance: ${asset.balance}")
                                                                        }
                                                                        
                                                                        // For WETH, don't require balance since it's a common token
                                                                        // For other tokens, require balance > 0
                                                                        val balanceOk = if (currentToken.symbol.equals("WETH", ignoreCase = true)) {
                                                                            true // Allow WETH selection even with 0 balance
                                                                        } else {
                                                                            asset.balance > 0.0
                                                                        }
                                                                        
                                                                        // Return true if all conditions match
                                                                        isMatchingChain && isERC20 && isSymbolMatch && balanceOk
                                                                    }
                                                                    
                                                                    if (matchingToken != null) {
                                                                        Log.d("SendScreen2", "✅ Switching ${currentToken.symbol} from chain ${currentToken.chainId} to ${matchingToken.symbol} on chain ${matchingToken.chainId}")
                                                                        Log.d("SendScreen2", "New token address: ${matchingToken.address}")
                                                                        updateSelectedAsset(matchingToken)
                                                                        
                                                                        // Log to confirm update was called
                                                                        Log.d("SendScreen2", "Called updateSelectedAsset with token on chain ${matchingToken.chainId}")
                                                                    } else {
                                                                        // If no matching token found on new chain, keep current selection but log warning
                                                                        Log.w("SendScreen2", "⚠️ No ${currentToken.symbol} with balance found on chain $chainId")
                                                                        Log.w("SendScreen2", "⚠️ Current selection remains: ${currentToken.symbol} on chain ${currentToken.chainId}")
                                                                        
                                                                        // Log available assets on target chain for debugging
                                                                        val assetsOnTargetChain = assetsUiState.assets.filter { it.chainId == chainId }
                                                                        Log.d("SendScreen2", "Available assets on chain $chainId:")
                                                                        assetsOnTargetChain.forEach { asset ->
                                                                            Log.d("SendScreen2", "  - ${asset.symbol}: ${asset.address} (balance: ${asset.balance})")
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        primaryColor = primaryColor,
                                        secondaryColor = secondaryColor
                                    )
                                }

                                SimpleDgenTextfield(
                                    modifier = modifier.padding(horizontal = 10.dp),
                                    value = toAddressFieldValue,
                                    maxLines = 4,
                                    maxLength = 43,
                                    scrollHorizontally = false,
                                    autoCorrectEnabled = false,
                                    onValueChange = { new ->
                                        toAddressFieldValue = new
                                        onToAddressChanged(new.text)
                                    },
                                    textStyle = TextStyle(
                                        fontFamily = PitagonsSans,
                                        color = dgenWhite,
                                        fontWeight = FontWeight. SemiBold,
                                        fontSize = 25.sp
                                    ),
                                    activeColor = primaryColor,
                                    placeholder = {
                                        Row (
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start
                                        ){
                                            Text(
                                                modifier = Modifier,
                                                text = "Address",
                                                style = TextStyle(
                                                    fontFamily = PitagonsSans,
                                                    color = dgenWhite.copy(pulseOpacity),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 24.sp
                                                ),
                                            )
                                        }
                                    },
                                    cursorColor = primaryColor,
                                    keyboardtype =  KeyboardType.Text,
                                    cursorWidth = 16.dp,
                                    cursorHeight= 16.dp,
                                    isAnyFieldFocused= remember { mutableStateOf(false) },
                                    onEditDone = {},
                                    view = view
                                ){
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                isResolvingENS -> "Resolving ENS...".uppercase()
                                                ensError != null -> "ENS Error".uppercase()
                                                toAddressFieldValue.text.endsWith(".eth") && isValidAddress -> "ENS Resolved".uppercase()
                                                else -> "Target Address".uppercase()
                                            },
                                            style = TextStyle(
                                                fontFamily = SpaceMono,
                                                color = when {
                                                    isResolvingENS -> dgenOrche
                                                    ensError != null -> dgenRed
                                                    toAddressFieldValue.text.endsWith(".eth") && isValidAddress -> dgenGreen
                                                    else -> primaryColor
                                                },
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = label_fontSize,
                                                lineHeight = label_fontSize,
                                                letterSpacing = 1.sp,
                                                textDecoration = TextDecoration.None,
                                                textAlign = TextAlign.Left
                                            ),
                                            color = when {
                                                isResolvingENS -> dgenOrche
                                                ensError != null -> dgenRed
                                                toAddressFieldValue.text.endsWith(".eth") && isValidAddress -> dgenGreen
                                                else -> primaryColor
                                            }
                                        )
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }

        // Add TransactionStatusOverlay at the end of the Box
        TransactionStatusOverlay(
            status = transactionStatus,
            gifLoader = gifEnabledLoader,
            onDismiss = { clearTransactionStatus() },
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )
    }

}

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
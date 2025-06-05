package com.feature.send

import android.Manifest
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.collectAsState
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
import com.core.model.TokenAsset
import com.core.model.TokenData
import com.core.ui.DgenLoadingMatrix
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGray
import com.example.dgenlibrary.ui.theme.dgenGreen
import com.example.dgenlibrary.ui.theme.dgenOrche
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import com.example.dgenlibrary.ui.theme.extraLargeEnterDuration
import com.example.dgenlibrary.ui.theme.extraLargeExitDuration
import com.example.dgenlibrary.ui.theme.largeEnterDuration
import com.example.dgenlibrary.ui.theme.mediumEnterDuration
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.feature.send.ui.SelectableCarousel
import com.feature.send.ui.TextToggle
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.core.ui.DgenBasicTextfield
import com.core.ui.DgenTextfield
import com.core.ui.HeaderBar
import com.example.dgenlibrary.ui.theme.SpaceMono
import androidx.compose.ui.draw.scale
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.dgenlibrary.ui.theme.smallDuration
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import android.widget.Toast
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.core.data.util.chainToApiKey
import com.core.ui.showCustomToast
import com.example.dgenlibrary.ui.theme.dgenGunMetal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.feature.send.ui.CustomCaptureActivity
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.crypto.WalletUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlin.reflect.KFunction1
import com.core.ui.util.formatWithSuffix
import java.util.concurrent.CompletableFuture
import androidx.compose.ui.unit.TextUnit
import kotlin.math.abs
import java.util.Locale

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
    tokenId: String?,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: SendViewModel = hiltViewModel(),

    ) {
    val currentNetwork by viewModel.currentChain.collectAsStateWithLifecycle(initialValue = "loading")
    val walletDataUiState by viewModel.walletDataState.collectAsStateWithLifecycle()
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val toAddress by viewModel.toAddress.collectAsStateWithLifecycle(initialValue = initialAddress ?: "")
    //val assets by viewModel.tokensAssetState.collectAsStateWithLifecycle()
    val assetsUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val selectedToken by viewModel.selectedAssetUiState.collectAsStateWithLifecycle()
    val txComplete by viewModel.txComplete.collectAsStateWithLifecycle()
    val qrScannerTriggered by viewModel.qrScannerTriggered.collectAsStateWithLifecycle()
    val sendTransactionTriggered by viewModel.sendTransactionTriggered.collectAsStateWithLifecycle()

    val selectedTokenId = viewModel.selectedTokenIdFlow.collectAsState()
    //val tokenId by viewModel.tokenIdFlow.collectAsState()

    val tokenData by viewModel.tokenData.collectAsState()

    // Display QR code on secondary screen only when the actual send screen content appears
    LaunchedEffect(assetsUiState) {
        if (assetsUiState is AssetsUiState.Success) {
            viewModel.onScreenOpened()
        }
    }

    // Remove QR code from secondary screen when this screen is disposed/closed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onScreenClosed()
        }
    }

    SendScreen2(
        initialAddress = initialAddress,
        modifier = Modifier,
        onBackClick = onBackClick,
        toAddress = toAddress,
        amount = amount,
        walletDataUiState = walletDataUiState,
        assets = assetsUiState,
        selectedToken = selectedToken,
        onAmountChange = viewModel::updateAmount,
        onToAddressChanged = viewModel::updateToAddress,
        sendTransaction = viewModel::send,
        updateSelectedAsset = viewModel::updateSelectedAsset,
        txComplete = txComplete,
        tokenId = tokenId,
        tokenData = tokenData,
        loadSymbol = viewModel::loadSymbol,
        convertDollarToToken = viewModel::convertDollarToToken,
        qrScannerTriggered = qrScannerTriggered,
        resetQrScannerTrigger = viewModel::resetQrScannerTrigger,
        sendTransactionTriggered = sendTransactionTriggered,
        resetSendTransactionTrigger = viewModel::resetSendTransactionTrigger
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalPermissionsApi::class)
@Composable
fun SendScreen2(
    modifier: Modifier = Modifier,
    toAddress: String,
    amount: String,
    walletDataUiState: WalletDataUiState,
    assets: AssetsUiState,
    onAmountChange: (String) -> Unit,
    onToAddressChanged: (String) -> Unit,
    sendTransaction: (() -> Unit) -> Unit,
    updateSelectedAsset: (TokenAsset) -> Unit,
    selectedToken: SelectedTokenUiState,
    txComplete: TxCompleteUiState,
    onBackClick: () -> Unit,
    initialAddress: String?,
    tokenId: String?,
    tokenData:  List<TokenData>,
    loadSymbol: (List<String>) -> Unit,
    convertDollarToToken: (String, String) -> Unit,
    qrScannerTriggered: Boolean,
    resetQrScannerTrigger: () -> Unit,
    sendTransactionTriggered: Boolean,
    resetSendTransactionTrigger: () -> Unit,
){
    val tokenPreselected = tokenId != null && tokenId.isNotEmpty() &&
                           (assets as? AssetsUiState.Success)?.assets?.firstOrNull {
                               it.address.equals(tokenId, ignoreCase = true)
                           }?.let { it.address != it.chainId.toString() } == true

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    var rotated by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = largeEnterDuration, easing = FastOutSlowInEasing)
    )

    var isAnimating by remember { mutableStateOf(false) }

    // Adjusted scaling values for a smoother transition
    val initialScale = 0.8f  // Corresponds to the scaling factor from CardCarousel
    val targetScale = 0.85f  // Target scaling for the send screen

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) initialScale else targetScale,
        animationSpec = tween(
            durationMillis = mediumEnterDuration,
            easing = FastOutSlowInEasing
        ),
        label = "ScaleAnimation"
    )
    var translateY by remember { mutableStateOf(0f) }

    Log.d("CardAnimation","tokenId: ${tokenId} - initialAddress: ${initialAddress} ")


    var testamount by remember { mutableStateOf("TEST") }
    var testaddress by remember { mutableStateOf("TEST") }

    Log.d("DEBUG","initialAddress: $initialAddress, tokenId: $tokenId")

    // Set the selected asset when the screen loads with a tokenId
    LaunchedEffect(tokenId, assets) {
        if (tokenId != null && tokenId.isNotEmpty() && assets is AssetsUiState.Success) {
            val token = assets.assets.firstOrNull {
                it.address.equals(tokenId, ignoreCase = true)
            }
            token?.let {
                updateSelectedAsset(it)
                Log.d("SendScreen2", "Selected asset set to: ${it.symbol}")
            }
        }
    }


    var dollarAmount by remember { mutableStateOf(TextFieldValue("")) }
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
    
    // Synchronize toAddressFieldValue with toAddress from ViewModel
    LaunchedEffect(toAddress) {
        // Only update if the text is different (avoids cursor reset)
        if (toAddress != toAddressFieldValue.text) {
            toAddressFieldValue = TextFieldValue(toAddress)
        }
    }
    
    // Variables for error handling
    var isAmountError by remember { mutableStateOf(false) }
    var convertedTokenAmount by remember { mutableStateOf("") }
    
    // ENS Resolution State
    var isResolvingENS by remember { mutableStateOf(false) }
    var ensError by remember { mutableStateOf<String?>(null) }
    
    // ENS Resolution
    LaunchedEffect(toAddressFieldValue.text) {
        val address = toAddressFieldValue.text
        if (address.endsWith(".eth") && ENSName(address.lowercase()).isPotentialENSDomain()) {
            isResolvingENS = true
            ensError = null
            
            try {
                withContext(Dispatchers.IO) {
                    val ens = ENS(
                        HttpEthereumRPC(
                            "https://eth-mainnet.g.alchemy.com/v2/${chainToApiKey("eth-mainnet")}"
                        )
                    )
                    val ensAddr = ens.getAddress(ENSName(address.lowercase()))
                    
                    withContext(Dispatchers.Main) {
                        ensAddr?.let { resolvedAddress ->
                            // Update the address in ViewModel with the resolved address
                            onToAddressChanged(resolvedAddress.hex)
                            // Don't update toAddressFieldValue here to keep the ENS name visible
                        } ?: run {
                            ensError = "ENS name not found"
                        }
                    }
                }
            } catch (e: Exception) {
                ensError = "Failed to resolve ENS: ${e.message}"
            } finally {
                isResolvingENS = false
            }
        } else {
            isResolvingENS = false
            ensError = null
        }
    }
    
    // Load exchange rates for selected token
    LaunchedEffect(selectedToken) {
        when (selectedToken) {
            is SelectedTokenUiState.Selected -> {
                val symbols = listOf(
                    selectedToken.tokenAsset.symbol.uppercase(),
                    "ETH" // Also load ETH for comparison purposes
                )
                loadSymbol(symbols)
            }
            else -> {
                // Load ETH exchange rate when no token is selected
                loadSymbol(listOf("ETH"))
            }
        }
    }

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

    //Variabel for animating the max button
    var setMax by remember { mutableStateOf(false) }
    var isMaxAmount by remember { mutableStateOf(false) }  // Track if MAX was used
    val maxAlpha by animateFloatAsState(
        targetValue = if (isMaxAmount) {
            1f
        } else {
            0.5f
        },
        animationSpec = tween(smallDuration,easing = FastOutLinearInEasing),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack),
        contentAlignment = Alignment.Center
    ) {

        AsyncImage(
            modifier = Modifier.alpha(0.2f).offset(x = 250.dp,y = 20.dp).scale(1.3f).aspectRatio(1f),
            imageLoader = gifEnabledLoader,
            model = R.drawable.globe_wireframe,
            contentDescription = null
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
                                    color = dgenGunMetal,
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
                                color = dgenGunMetal,
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
                        DgenLoadingMatrix()
                    }
                }
                is AssetsUiState.Success -> {

                    
                    // Selected chain state
                    var selectedChainIndex by remember { mutableStateOf(0) }
                    
                    // Get available chains
                    val availableChains = remember(selectedToken, assetsUiState.assets) {
                        when (selectedToken) {
                            is SelectedTokenUiState.Selected -> {
                                // Check if the selected token is a native token
                                if (selectedToken.tokenAsset.address == selectedToken.tokenAsset.chainId.toString()) {
                                    // For native tokens, show all chains where user has native tokens
                                    val chainsWithNativeTokens = assetsUiState.assets
                                        .filter { asset ->
                                            // Find all native tokens (where address equals chainId)
                                            asset.address == asset.chainId.toString()
                                        }
                                        .map { it.chainId }
                                        .distinct()
                                    
                                    chainsWithNativeTokens.mapNotNull { chainId ->
                                        when (chainId) {
                                            1 -> "main"
                                            11155111 -> "sepolia"
                                            10 -> "op"
                                            137 -> "pol"
                                            42161 -> "arb"
                                            8453 -> "base"
                                            7777777 -> "zora"
                                            else -> null
                                        }
                                    }
                                } else {
                                    // For ERC20 tokens, find all chains where this token exists by symbol
                                    val tokenSymbol = selectedToken.tokenAsset.symbol
                                    
                                    val chainsWithThisToken = assetsUiState.assets
                                        .filter { asset ->
                                            // Only match ERC20 tokens (not native tokens) with the same symbol
                                            asset.address != asset.chainId.toString() &&
                                            asset.symbol.equals(tokenSymbol, ignoreCase = true)
                                        }
                                        .map { it.chainId }
                                        .distinct()
                                    
                                    chainsWithThisToken.mapNotNull { chainId ->
                                        when (chainId) {
                                            1 -> "main"
                                            11155111 -> "sepolia"
                                            10 -> "op"
                                            137 -> "pol"
                                            42161 -> "arb"
                                            8453 -> "base"
                                            7777777 -> "zora"
                                            else -> null
                                        }
                                    }
                                }
                            }
                            else -> {
                                // If no token is selected, show all chains that have any tokens
                                val chainsWithTokens = assetsUiState.assets
                                    .map { it.chainId }
                                    .distinct()
                                
                                chainsWithTokens.mapNotNull { chainId ->
                                    when (chainId) {
                                        1 -> "main"
                                        11155111 -> "sepolia"
                                        10 -> "op"
                                        137 -> "pol"
                                        42161 -> "arb"
                                        8453 -> "base"
                                        7777777 -> "zora"
                                        else -> null
                                    }
                                }
                            }
                        }
                    }
                    
                    // Get available balance based on selected chain
                    val availableBalance = remember(selectedToken, selectedChainIndex, availableChains, assetsUiState.assets) {
                        // Get the selected chain name
                        val selectedChainName = availableChains.getOrNull(selectedChainIndex)
                        
                        // Convert chain name to chain ID
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
                            when (selectedToken) {
                                is SelectedTokenUiState.Selected -> {
                                    // Check if it's a native token
                                    if (selectedToken.tokenAsset.address == selectedToken.tokenAsset.chainId.toString()) {
                                        // For native tokens, find the matching native token on the selected chain
                                        assetsUiState.assets
                                            .firstOrNull { asset -> 
                                                asset.chainId == chainId && 
                                                asset.address == chainId.toString()
                                            }?.balance ?: 0.0
                                    } else {
                                        // For ERC20 tokens, find by symbol match
                                        val tokenSymbol = selectedToken.tokenAsset.symbol
                                        assetsUiState.assets
                                            .firstOrNull { asset -> 
                                                asset.chainId == chainId &&
                                                asset.symbol.equals(tokenSymbol, ignoreCase = true)
                                            }?.balance ?: 0.0
                                    }
                                }
                                else -> {
                                    // When no token is selected, get the native token balance for the selected chain
                                    assetsUiState.assets
                                        .firstOrNull { asset ->
                                            asset.chainId == chainId && 
                                            asset.address == chainId.toString()
                                        }?.balance ?: 0.0
                                }
                            }
                        } ?: 0.0
                    }
                    
                    // Validate Address
                    val isValidAddress = remember(toAddress, toAddressFieldValue.text, isResolvingENS, ensError) {
                        when {
                            toAddress.isEmpty() -> false
                            toAddressFieldValue.text.endsWith(".eth") -> !isResolvingENS && ensError == null && toAddress.isNotEmpty()
                            else -> WalletUtils.isValidAddress(toAddress)
                        }
                    }
                    
                    // Validate Amount
                    LaunchedEffect(amount, dollarAmount.text, useDollarAmount, convertedTokenAmount, availableBalance, isMaxAmount) {
                        if (isMaxAmount) {
                            // If MAX was used, don't show error
                            isAmountError = false
                        } else if (useDollarAmount) {
                            // If dollar input is active, check token amount
                            val tokenAmount = convertedTokenAmount.toDoubleOrNull() ?: 0.0
                            isAmountError = tokenAmount > availableBalance
                        } else {
                            // If token input is active, check token amount directly
                            val tokenAmount = amount.toDoubleOrNull() ?: 0.0
                            isAmountError = tokenAmount > availableBalance
                        }
                    }

                    // Handle send transaction trigger from ViewModel
                    LaunchedEffect(sendTransactionTriggered) {
                        if (sendTransactionTriggered) {
                            // Use the same validation logic as the removed button
                            if ((isAmountError && !isMaxAmount) || !isValidAddress || selectedToken == SelectedTokenUiState.Unselected) {
                                // Show specific error messages
                                when {
                                    isAmountError && !isMaxAmount -> showToast(context,"Insufficient balance")
                                    toAddress.isEmpty() -> showToast(context,"Enter target address")
                                    isResolvingENS -> showToast(context,"Resolving ENS name...", backgroundColor = dgenOrche)
                                    ensError != null -> showToast(context,ensError ?: "ENS error")
                                    !isValidAddress -> showToast(context,"Invalid address format")
                                    selectedToken == SelectedTokenUiState.Unselected -> showToast(context,"Select a chain")
                                }
                            } else {
                                // Get current amounts and token symbol
                                val currentDollarAmount = if (useDollarAmount) dollarAmount.text else ""
                                val currentTokenAmount = if (!useDollarAmount) amount else ""
                                
                                if (currentDollarAmount.isEmpty() && currentTokenAmount.isEmpty()) {
                                    showToast(context, "Type in an amount")
                                } else {
                                    // Execute transaction
                                    val finalAmount = if (isMaxAmount) {
                                        // Use exact balance for MAX amount
                                        availableBalance.toString()
                                    } else if (useDollarAmount) {
                                        convertedTokenAmount
                                    } else {
                                        currentTokenAmount
                                    }
                                    
                                    if (finalAmount.isNotEmpty()) {
                                        onAmountChange(finalAmount)
                                        sendTransaction {
                                            // Callback after successful send
                                            Log.d("SendScreen", "Transaction sent successfully from secondary screen")
                                            // Navigate back after successful transaction
                                            onBackClick()
                                        }
                                    }
                                }
                            }
                            resetSendTransactionTrigger()
                        }
                    }

                    Box(
                        Modifier.fillMaxSize()
                    ) {
                        Column (
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
                                            color = dgenTurqoise,
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

                                            // Token Logo
                                            if (!token.logoUrl.isNullOrEmpty()) {
                                                AsyncImage(
                                                    model = token.logoUrl,
                                                    contentDescription = token.name,
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape),
                                                    placeholder = painterResource(R.drawable.placeholer_icon_5),
                                                    error = painterResource(R.drawable.placeholer_icon_5)
                                                )
                                            } else {
                                                val tokenLogo = when(token.symbol.uppercase()) {
                                                    "MAINNET" -> R.drawable.mainnet
                                                    "OPTIMISM" -> R.drawable.mainnet
                                                    "ARBITRUM" -> R.drawable.mainnet
                                                    "POLYGON" -> R.drawable.polygon
                                                    "SEPOLIA" -> R.drawable.mainnet
                                                    "BASE" -> R.drawable.mainnet
                                                    "ZORA" -> R.drawable.mainnet
                                                    else -> R.drawable.placeholer_icon_5
                                                }
                                                Image(
                                                    modifier = Modifier
                                                        .size(28.dp),
                                                    painter = painterResource(tokenLogo),
                                                    contentDescription = token.name
                                                )
                                            }



                                            val tokenName = when(token.symbol.uppercase()){
                                                "MAINNET" -> "ETH"
                                                "OPTIMISM" -> "ETH"
                                                "ARBITRUM" -> "ETH"
                                                "POLYGON" -> "MATIC"
                                                "SEPOLIA" -> "ETH"
                                                "BASE" -> "ETH"
                                                "ZORA" -> "ETH"

                                                else -> {
                                                    token.symbol.uppercase()
                                                }
                                            }
                                            // Token Symbol
                                            Text(
                                                text = tokenName,
                                                style = TextStyle(
                                                    fontFamily = SpaceMono,
                                                    color = dgenTurqoise,
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
                                                    color = dgenTurqoise,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 24.sp,
                                                    letterSpacing = 0.sp,
                                                    textDecoration = TextDecoration.None
                                                )
                                            )
                                        }
                                    }
                                }
                            }, onClick = onBackClick, modifier = modifier.padding(start = 24.dp, end = 24.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {

                                Column(
                                    modifier = Modifier
                                        .drawBehind {
                                            drawLine(
                                                color = dgenGray.copy(0.5f),
                                                start = Offset(0f, 15f),
                                                end = Offset(0f, size.height-0f),
                                                strokeWidth = 8.dp.toPx()
                                            )
                                        }
                                        .padding(start = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)

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
                                            value = useDollarAmount
                                        )

                                        val valueString = remember(availableBalance, useDollarAmount, selectedToken, tokenData) {
                                            if (useDollarAmount) {
                                                if (availableBalance > 0) {
                                                    val tokenSymbolForPriceLookup = when (val currentSelectedToken = selectedToken) {
                                                        is SelectedTokenUiState.Selected -> {
                                                            val assetSymbolUpper = currentSelectedToken.tokenAsset.symbol.uppercase()
                                                            when (assetSymbolUpper) {
                                                                "MAINNET" -> "ETH"
                                                                // Assuming other native tokens (OPTIMISM, ARBITRUM, POLYGON, etc.)
                                                                // are keyed by their own symbol in tokenData for price,
                                                                // or "ETH" if that's how their price is listed.
                                                                // This matches convertDollarToToken's logic.
                                                                else -> assetSymbolUpper
                                                            }
                                                        }
                                                        else -> "ETH"
                                                    }
                                                    val currentPrice = tokenData.find { it.symbol.equals(tokenSymbolForPriceLookup, ignoreCase = true) }
                                                        ?.prices?.firstOrNull()?.value?.toDoubleOrNull()

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
                                            color = dgenTurqoise.copy(maxAlpha),
                                            fontWeight = FontWeight.SemiBold,
                                            lineHeight = 18.sp,
                                            letterSpacing = 0.sp,
                                            textDecoration = TextDecoration.None,
                                            modifier = Modifier.offset( y=2.dp).pointerInput(Unit){
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
                                                        // Check if the new value contains more than one dot
                                                        if (dollarAmount.text.isEmpty() || dollarAmount.text == "." || dollarAmount.text.matches("-?\\d*(\\.\\d*)?".toRegex())) {
                                                            // If it's a valid format or empty, call onAmountChange with the text
                                                            dollarAmount = new
                                                        }
//                                                        val dotCount = new.text.count { it == '.' }
//                                                        if (dotCount <= 1) {
//                                                            dollarAmount = new
//                                                        }
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
                                                                            color = dgenGray.copy(alpha = 0.5f),
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
                                                                    color = dgenGray.copy(alpha = 0.5f),
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
                                                    cursorHeight= 32.dp,
                                                    isAnyFieldFocused= remember { mutableStateOf(false) },
                                                )
                                            }else{
                                                DgenBasicTextfield(
                                                    value = amountFieldValue,
                                                    onValueChange={ new ->
                                                        isMaxAmount = false  // Reset when user manually changes amount

                                                        if (amountFieldValue.text.isEmpty() || amountFieldValue.text == "." || amountFieldValue.text.matches("-?\\d*(\\.\\d*)?".toRegex())) {
                                                            // If it's a valid format or empty, call onAmountChange with the text
                                                            amountFieldValue = new
                                                            onAmountChange(new.text)
                                                        }
//                                                        // Check if the new value contains more than one dot
//                                                        val dotCount = new.text.count { it == '.' }
//                                                        if (dotCount <= 1) {
//                                                            amountFieldValue = new
//                                                            onAmountChange(new.text)
//                                                        }
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
                                                                text = "0.0", // Static placeholder
                                                                style = TextStyle(
                                                                    fontFamily = PitagonsSans,
                                                                    color = dgenGray.copy(alpha = 0.5f),
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
                                                    cursorHeight= 32.dp,
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
                                                        "MAINNET" -> "ETH"
                                                        else -> selectedToken.tokenAsset.symbol.uppercase()
                                                    }
                                                }
                                                else -> {
                                                    "ETH"
                                                }
                                            }

                                            convertDollarToToken(dollarAmount.text, tokenSymbol)
                                            
                                            // Calculate the converted token amount for validation
                                            try {
                                                val dollarValue = dollarAmount.text.toDoubleOrNull() ?: 0.0
                                                val currentPrice = tokenData.find { 
                                                    it.symbol.equals(tokenSymbol, ignoreCase = true) 
                                                }?.prices?.firstOrNull()?.value?.toDoubleOrNull()
                                                
                                                if (currentPrice != null && currentPrice > 0) {
                                                    convertedTokenAmount = (dollarValue / currentPrice).toString()
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
                                                            "MAINNET" -> "ETH"
                                                            else -> selectedToken.tokenAsset.symbol.uppercase()
                                                        }
                                                    }
                                                    else -> "ETH"
                                                }
                                                
                                                val currentPrice = tokenData.find { 
                                                    it.symbol.equals(tokenSymbol, ignoreCase = true) 
                                                }?.prices?.firstOrNull()?.value?.toDoubleOrNull()
                                                
                                                if (currentPrice != null && currentPrice > 0) {
                                                    val dollarValue = availableBalance * currentPrice
                                                    val formattedDollar = String.format("%.2f", dollarValue)
                                                    dollarAmount = TextFieldValue(formattedDollar)
                                                }
                                            } else {
                                                // Set token amount directly
                                                amountFieldValue = TextFieldValue(formattedBalance)
                                                onAmountChange(formattedBalance)
                                            }
                                            
                                            // Reset setMax after setting the value
                                            setMax = false
                                        }
                                    }

                                }


                                // Chain selection based on token availability
                                // REMOVED - Already defined above before availableBalance
                                
                                // Set the initial chain based on the selected token
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
                                    items = availableChains,
                                    itemWidth = 65.dp,
                                    itemHeight = 65.dp,
                                    initialSelectedIndex = selectedChainIndex,
                                    onItemSelected = { index -> 
                                        val newIndex = index ?: 0
                                        
                                        // Only process if actually changing to a different chain
                                        if (selectedChainIndex != newIndex) {
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
                                                            }
                                                            // If current token is ERC20, keep it selected
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                            DgenTextfield(
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
                                                color = dgenGray.copy(0.5f),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 24.sp
                                            ),
                                        )
                                    }

                                },
                                keyboardtype =  KeyboardType.Text,
                                cursorWidth = 16.dp,
                                cursorHeight= 32.dp,
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
                                                else -> dgenTurqoise
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
                                            else -> dgenTurqoise
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

}

fun showToast(
    context: Context,
    message: String,
    backgroundColor: Color = dgenRed,
    textColor: Color = dgenWhite
) {
    context.showCustomToast(
        message,
        Toast.LENGTH_SHORT,
        fontFamily = PitagonsSans,
        fontWeight = FontWeight.SemiBold,
        backgroundColor = backgroundColor,
        textColor = textColor,
    )
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
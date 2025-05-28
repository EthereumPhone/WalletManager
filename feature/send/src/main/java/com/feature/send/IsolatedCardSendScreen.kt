package com.feature.send

import android.Manifest
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.core.ui.Card
import com.core.ui.DgenLoadingMatrix
import com.feature.send.ui.SendCardView
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
import com.example.dgenlibrary.ui.theme.largeEnterDuration
import com.example.dgenlibrary.ui.theme.mediumEnterDuration
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
import com.example.dgenlibrary.ui.theme.body1_fontSize
import androidx.compose.ui.draw.scale
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.core.ui.BottomBarButton
import com.example.dgenlibrary.ui.theme.smallDuration
import com.feature.send.ui.ToolbarCaptureActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import org.ethosmobile.components.library.theme.Colors
import android.widget.Toast
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.feature.send.ui.CustomCaptureActivity

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

    val selectedTokenId = viewModel.selectedTokenIdFlow.collectAsState()
    //val tokenId by viewModel.tokenIdFlow.collectAsState()

    val tokenData by viewModel.tokenData.collectAsState()


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
        onToAddressChanged= viewModel::updateToAddress,
        sendTransaction = viewModel::send,
        updateSelectedAsset = viewModel::updateSelectedAsset,
        txComplete = txComplete,
        tokenId = tokenId,
        tokenData = tokenData,
        loadSymbol = viewModel::loadSymbol,
        convertDollarToToken = viewModel::convertDollarToToken
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
){


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

    // Angepasste Skalierungswerte für eine glattere Transition
    val initialScale = 0.8f  // Entspricht dem Skalierungsfaktor aus dem CardCarousel
    val targetScale = 0.85f  // Ziel-Skalierung für den Send-Screen

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


    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var dollarAmount by remember { mutableStateOf(TextFieldValue("")) }
    var toValue by remember { mutableStateOf(TextFieldValue("")) }
    var useDollarAmount by remember { mutableStateOf(false) }
    
    // Synchronisiere toValue mit toAddress (für QR-Scanner und andere Updates)
    LaunchedEffect(toAddress) {
        if (toAddress != toValue.text) {
            toValue = TextFieldValue(toAddress)
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
            Toast.makeText(context, "Scan result: ${result.contents}", Toast.LENGTH_LONG).show()
            if(result.contents == null) {
                // Optional: Handle cancelled scan
                Log.d("QRScanner", "Scan was cancelled or no content found")
                Toast.makeText(context, "Scan cancelled", Toast.LENGTH_SHORT).show()
            } else {
                val address = result.contents.removePrefix("ethereum:")
                Log.d("QRScanner", "Extracted address: $address")
                Toast.makeText(context, "Address found: $address", Toast.LENGTH_LONG).show()
                onToAddressChanged(address)
            }
        }
    )

    var showCameraWithPerm by remember {
        mutableStateOf(false)
    }

    val scanningPermissionsToRequest = listOf(
        Manifest.permission.CAMERA
    )

    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = scanningPermissionsToRequest
    )

    LaunchedEffect(showCameraWithPerm) {
        if (showCameraWithPerm) {
            if (multiplePermissionsState.allPermissionsGranted) {
                showCamera(barCodeLauncher)
                showCameraWithPerm = false // Reset the state after launching
            } else {
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
        }
    }

    // Handle permission result
    LaunchedEffect(multiplePermissionsState.allPermissionsGranted) {
        if (showCameraWithPerm && multiplePermissionsState.allPermissionsGranted) {
            showCamera(barCodeLauncher)
            showCameraWithPerm = false // Reset the state after launching
        }
    }




    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack),
        contentAlignment = Alignment.Center
    ) {

        AsyncImage(
            modifier = Modifier.alpha(0.2f).offset(x = 250.dp,y = 20.dp).scale(1.1f).aspectRatio(1f),
            imageLoader = gifEnabledLoader,
            model = R.drawable.wireframe_globe,
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
                AssetUiState.Empty -> {
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
                AssetUiState.Error -> {
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
                AssetUiState.Loading -> {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        DgenLoadingMatrix()
                    }
                }
                is AssetUiState.Success -> {
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
                                    
                                    // Zeige Token-Logo basierend auf selectedToken
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
                                                    placeholder = painterResource(R.drawable.ethereum_placeholder),
                                                    error = painterResource(R.drawable.ethereum_placeholder)
                                                )
                                            } else {
                                                Image(
                                                    modifier = Modifier
                                                        .size(28.dp),
                                                    painter = painterResource(R.drawable.placeholer_icon_5),
                                                    contentDescription = token.name
                                                )
                                            }
                                            
                                            // Token Symbol
                                            Text(
                                                text = token.symbol.uppercase(),
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
                                            // Fallback zu ETH wenn kein Token ausgewählt ist
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
                                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                        .padding(start = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)

                                ) {
                                    TextToggle(
                                        Modifier.offset(x = 2.dp, y=2.dp),
                                        when (selectedToken) {
                                            is SelectedTokenUiState.Selected -> selectedToken.tokenAsset.symbol.uppercase()
                                            else -> "ETH"
                                        }, 
                                        "$",
                                        onToggle = {
                                            useDollarAmount = !useDollarAmount
                                            scope.launch{
                                                delay(200)
                                                dollarAmount = TextFieldValue("")
                                                amount = TextFieldValue("")
                                            }

                                        },
                                        value = useDollarAmount
                                    )
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
                                                    onValueChange={ new ->
                                                        // Check if the new value contains more than one dot
                                                        val dotCount = new.text.count { it == '.' }
                                                        if (dotCount <= 1) {
                                                            dollarAmount = new
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
                                                                            color = dgenGray,
                                                                            fontWeight = FontWeight.SemiBold,
                                                                            fontSize = 39.sp,
                                                                        )
                                                                    ){
                                                                        append("\$")
                                                                    }
                                                                    append("0.0")
                                                                },
                                                                style = TextStyle(
                                                                    fontFamily = PitagonsSans,
                                                                    color = dgenGray,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontSize = 42.sp,
                                                                    textAlign = TextAlign.Start
                                                                ),
                                                            )
                                                        }

                                                    },
                                                    textStyle = TextStyle(
                                                        fontFamily = PitagonsSans,
                                                        color = dgenWhite,
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
                                                    value = amount,
                                                    onValueChange={ new ->
                                                        // Check if the new value contains more than one dot
                                                        val dotCount = new.text.count { it == '.' }
                                                        if (dotCount <= 1) {
                                                            amount = new
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
                                                                text = "0.0",
                                                                style = TextStyle(
                                                                    fontFamily = PitagonsSans,
                                                                    color = dgenGray,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontSize = 42.sp,
                                                                    textAlign = TextAlign.Start
                                                                ),
                                                            )
                                                        }

                                                    },
                                                    textStyle = TextStyle(
                                                        fontFamily = PitagonsSans,
                                                        color = dgenWhite,
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
                                                    selectedToken.tokenAsset.symbol.uppercase()
                                                }
                                                else -> {
                                                    "ETH"
                                                }
                                            }

                                            convertDollarToToken(dollarAmount.text, tokenSymbol)
                                        }
                                    }

                                }


                                // Chain-Auswahl basierend auf Token-Verfügbarkeit
                                val availableChains = remember(selectedToken, assetsUiState.assets) {
                                    when (selectedToken) {
                                        is SelectedTokenUiState.Selected -> {
                                            // Finde alle Chains, auf denen dieser Token verfügbar ist
                                            val tokenSymbol = selectedToken.tokenAsset.symbol
                                            val chainsWithToken = assetsUiState.assets
                                                .filter { it.symbol.equals(tokenSymbol, ignoreCase = true) }
                                                .map { it.chainId }
                                                .distinct()
                                            
                                            // Mappe chainIds zu Chain-Namen
                                            chainsWithToken.mapNotNull { chainId ->
                                                when (chainId) {
                                                    1 -> "mainnet"
                                                    11155111 -> "sepolia"
                                                    10 -> "optimism"
                                                    137 -> "polygon"
                                                    42161 -> "arbitrum"
                                                    8453 -> "base"
                                                    7777777 -> "zora"
                                                    else -> null
                                                }
                                            }
                                        }
                                        else -> {
                                            // Wenn kein Token ausgewählt ist, zeige alle Chains mit ETH/MATIC
                                            val nativeTokenChains = assetsUiState.assets
                                                .filter { 
                                                    it.symbol.equals("ETH", ignoreCase = true) || 
                                                    it.symbol.equals("MATIC", ignoreCase = true) ||
                                                    it.symbol.equals("mainnet", ignoreCase = true) ||
                                                    it.symbol.equals("sepolia", ignoreCase = true) ||
                                                    it.symbol.equals("optimism", ignoreCase = true) ||
                                                    it.symbol.equals("polygon", ignoreCase = true) ||
                                                    it.symbol.equals("arbitrum", ignoreCase = true) ||
                                                    it.symbol.equals("base", ignoreCase = true) ||
                                                    it.symbol.equals("zora", ignoreCase = true)
                                                }
                                                .map { it.chainId }
                                                .distinct()
                                            
                                            nativeTokenChains.mapNotNull { chainId ->
                                                when (chainId) {
                                                    1 -> "mainnet"
                                                    11155111 -> "sepolia"
                                                    10 -> "optimism"
                                                    137 -> "polygon"
                                                    42161 -> "arbitrum"
                                                    8453 -> "base"
                                                    7777777 -> "zora"
                                                    else -> null
                                                }
                                            }
                                        }
                                    }
                                }

                                // Ausgewählte Chain
                                var selectedChainIndex by remember { mutableStateOf(0) }
                                
                                // Setze die initiale Chain basierend auf dem ausgewählten Token
                                LaunchedEffect(selectedToken, availableChains) {
                                    when (selectedToken) {
                                        is SelectedTokenUiState.Selected -> {
                                            // Finde den Index der Chain des ausgewählten Tokens
                                            val tokenChainId = selectedToken.tokenAsset.chainId
                                            val chainName = when (tokenChainId) {
                                                1 -> "mainnet"
                                                11155111 -> "sepolia"
                                                10 -> "optimism"
                                                137 -> "polygon"
                                                42161 -> "arbitrum"
                                                8453 -> "base"
                                                7777777 -> "zora"
                                                else -> null
                                            }
                                            
                                            chainName?.let { name ->
                                                val index = availableChains.indexOf(name)
                                                if (index >= 0) {
                                                    selectedChainIndex = index
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                }

                                SelectableCarousel(
                                    items = availableChains,
                                    itemWidth = 70.dp,
                                    itemHeight = 70.dp,
                                    initialSelectedIndex = selectedChainIndex,
                                    onItemSelected = { index -> 
                                        selectedChainIndex = index ?: 0
                                    }
                                )

                            }

                            DgenTextfield(
                                value = toValue,
                                maxLines = 4,
                                maxLength = 42,
                                scrollHorizontally = false,
                                onValueChange={ new -> 
                                    toValue = new
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
                                                color = dgenGray,
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
                                        text = "Target Address".uppercase(),
                                        style = TextStyle(
                                            fontFamily = SpaceMono,
                                            color = dgenTurqoise,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = label_fontSize,
                                            lineHeight = label_fontSize,
                                            letterSpacing = 1.sp,
                                            textDecoration = TextDecoration.None,
                                            textAlign = TextAlign.Left
                                        ),
                                        color = dgenTurqoise
                                    )
                                    IconButton(onClick = {
                                        showCameraWithPerm = true
                                    }) {
                                        Icon(imageVector = Icons.Rounded.QrCodeScanner, contentDescription = "QR Scan", tint= dgenTurqoise, modifier = modifier.size(24.dp))
                                    }
                                }

//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(dgenBlack).align(Alignment.BottomCenter)
//                                .padding(start=8.dp, end=8.dp, top=8.dp),
//                            horizontalArrangement = Arrangement.Center
//                        ) {
//                            BottomBarButton(
//                                onClick = {
//                                    Log.d("QRScanner", "QR Scanner button clicked")
//                                    showCameraWithPerm = true
//                                },
//                                icon = {
//                                    Icon(imageVector = Icons.Rounded.QrCodeScanner, contentDescription = "QR Scan",tint= dgenTurqoise, modifier = modifier.size(28.dp))
//                                },
//                                text = "Log"
//                            )
//                        }
                    }
                }
            }
        }
    }

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
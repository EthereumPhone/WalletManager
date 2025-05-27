package com.feature.send

import android.Manifest
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
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
import com.feature.send.ui.ToolbarCaptureActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.ethosmobile.components.library.theme.Colors

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
        tokenData = tokenData
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
    tokenData:  List<TokenData>
){

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
    var toValue by remember { mutableStateOf(TextFieldValue("")) }
    var showTokenAmount by remember { mutableStateOf(false) }


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
            if(result.contents == null) {
                // Optional: Handle cancelled scan
            } else {
                val address = result.contents.removePrefix("ethereum:")
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
                AssetsUiState.Empty -> {
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
                                            Modifier.offset(x = 2.dp, y=2.dp),"ETH", "$",
                                            onToggle = {
                                                showTokenAmount = !showTokenAmount
                                            },
                                            value = showTokenAmount
                                        )
                                        Row(
                                            Modifier.fillMaxWidth(),
                                        ){
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


                                // Sample list
                                val sampleItems = listOf("base", "mainnet", "zora", "optimism", "arbitrum", "polygon")

                                // Preview state holder
                                var selected by remember { mutableStateOf<Int?>(null) }

                                SelectableCarousel(
                                    items = sampleItems,
                                    itemWidth = 70.dp,
                                    itemHeight = 70.dp,
                                    initialSelectedIndex = 0,
                                    onItemSelected = { index -> selected = index }
                                )

                            }

                        DgenTextfield(
                            value = toValue,
                            maxLines = 4,
                            maxLength = 42,
                            scrollHorizontally = false,
                            onValueChange={ new -> toValue = new},
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
                            view = LocalView.current
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
                                    Log.d("QRScanner", "QR Scanner button clicked")
                                    showCameraWithPerm = true
                                }) {
                                    Icon(imageVector = Icons.Rounded.QrCodeScanner, contentDescription = "QR Scan",tint= dgenTurqoise, modifier = modifier.size(28.dp))
                                }
                            }
                        }
                        




            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {

                IconButton(
                    modifier = modifier.size(56.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = dgenRed,
                        disabledContainerColor = dgenGray,
                        disabledContentColor = dgenBlack
                    ),
                    onClick =  onBackClick,
                ){
                    Icon(
                        modifier = Modifier.size(36.dp),
                        painter = painterResource(R.drawable.baseline_close_24),
                        contentDescription = "Send Icon",
                        tint = dgenRed
                    )
                }

                IconButton(
                    modifier = modifier.size(56.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = dgenTurqoise,
                        disabledContainerColor = dgenGray,
                        disabledContentColor = dgenBlack
                    ),
                    onClick = {
                        Log.d("SEND TX vor","$testaddress - ${testamount}  ")
                        Log.d("SEND TX nach","$testaddress - ${testamount}  ")
                        Log.d("SEND TX nach nach","$toAddress - ${amount}")
                        //g.d("SEND TX nach","$toAddress - ${amount.toDouble()}  ")

                        when(assets){

                            AssetsUiState.Empty -> {

                            }
                            AssetsUiState.Error -> {

                            }
                            AssetsUiState.Loading -> {

                            }
                            is AssetsUiState.Success -> {

                                val token = assets.assets.firstOrNull {
                                    it.address.equals(tokenId, ignoreCase = true)
                                }
                                Log.d("SEND TX","$toAddress - ${amount.toDouble()} - ${token?.balance} ")

                                if (token != null) {
                                    // Update the selected asset in the view model before sending
                                    updateSelectedAsset(token)
                                    
                                    if(amount.toDouble() < token.balance) {
                                        sendTransaction {
                                            onBackClick()
                                        }
                                    }
                                }
                            }
                        }

                    },
                ){
                    Icon(
                        modifier = Modifier.size(36.dp),
                        painter = painterResource(R.drawable.baseline_arrow_outward_24),
                        contentDescription = "Send Icon"
                    )
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
        options.setCaptureActivity(ToolbarCaptureActivity::class.java)
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan QR Code")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setOrientationLocked(false)
        cameraLauncher.launch(options)
    } catch (e: Exception) {
        Log.e("QRScanner", "Error launching camera: ${e.message}", e)
    }
}
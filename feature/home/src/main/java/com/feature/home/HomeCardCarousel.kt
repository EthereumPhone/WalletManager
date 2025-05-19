package com.feature.home

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.core.model.TokenData
import com.core.model.TokenMetadata
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.feature.send.SelectedTokenUiState
import com.feature.send.SendViewModel
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.core.model.TokenAsset
import com.core.ui.DgenLoadingMatrix
import com.core.ui.initializeFontMap
import com.core.ui.showCustomToast
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenWhite
import com.example.dgenlibrary.ui.theme.extraLargeEnterDuration
import com.example.dgenlibrary.ui.theme.extraLargeExitDuration
import com.example.dgenlibrary.ui.theme.mediumEnterDuration
import com.example.dgenlibrary.ui.theme.mediumExitDuration
import com.feature.home.screens.EmptyHomeScreen
import com.feature.home.screens.ErrorHomeScreen
import com.feature.home.screens.HomeScreenContent
import com.feature.home.screens.LoadingHomeScreen
import com.feature.home.ui.TokenCardCarousel
import kotlin.reflect.KSuspendFunction1


@OptIn(ExperimentalSharedTransitionApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
internal fun HomeRoute2(
    modifier: Modifier = Modifier,
    navigateToSwap: () -> Unit,
    navigateToSend: (address: String, tokenId: String ) -> Unit,
    navigateToLog: (String) -> Unit,
    navigateToReceive: () -> Unit,
    isOffline: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: HomeViewModel = hiltViewModel(),
    sendViewModel: SendViewModel = hiltViewModel()

) {
    val walletDataUiState: WalletDataUiState by viewModel.walletDataState.collectAsStateWithLifecycle()
    val assetsUiState: AssetsUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val selectedTokenUiState: SelectedTokenUiState by sendViewModel.selectedAssetUiState.collectAsStateWithLifecycle()

    val selectedTokenId = sendViewModel.selectedTokenIdFlow.collectAsState()

    var updater by remember { mutableStateOf(true) }

    if(updater) {
        Log.d("automatic updater", "TEST")
        viewModel.refreshData()
        updater = false
    }

    val tokenData by viewModel.tokenData.collectAsState()

    val tokenMetadata by viewModel.tokenMetadata.collectAsState()

    val hasTransfer by viewModel.hasTransfers.collectAsState()

    initializeFontMap(SpaceMono, PitagonsSans)

    HomeScreen2(
        userData = walletDataUiState,
        assetsUiState = assetsUiState,
        navigateToSwap = navigateToSwap,
        navigateToSend = navigateToSend,
        navigateToLog = navigateToLog,
        navigateToReceive = navigateToReceive,
        selectedTokenUiState = selectedTokenUiState,
        selectedTokenId = selectedTokenId,
        setSelectedTokenId = sendViewModel::updateSelectedTokenId,
        isOffline = isOffline,
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        tokenData = tokenData,
        tokenMetadata = tokenMetadata,
        loadSymbol = viewModel::loadSymbol,
        getLink = viewModel::getLink,
        hasTransfer = hasTransfer


    )
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen2(
    userData: WalletDataUiState,
    assetsUiState: AssetsUiState,
    navigateToSwap: () -> Unit,
    navigateToSend: (address: String, tokenId: String ) -> Unit,
    navigateToLog: (String) -> Unit,
    navigateToReceive: () -> Unit,
    tokenMetadata:  List<TokenMetadata>,
    selectedTokenUiState: SelectedTokenUiState,
    selectedTokenId: State<String>,
    setSelectedTokenId: (String) -> Unit,
    isOffline: Boolean,
    tokenData:  List<TokenData>,
    loadSymbol: (List<String>) -> Unit,
    hasTransfer: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    getLink: KSuspendFunction1<String, String>,
    modifier: Modifier = Modifier,
) {


    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if ( SDK_INT >= 28 ) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()

    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack),
    ) {


        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .background(dgenBlack)
            //.weight(1f) // Allows it to take up remaining space

        ) {

            AnimatedContent(
                assetsUiState,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(extraLargeEnterDuration)
                    ) togetherWith fadeOut(animationSpec = tween(extraLargeExitDuration))
                },
                modifier = Modifier.fillMaxSize(),
                label = "Animated Content"
            ) { assetState ->
                when(assetState){
                    is AssetsUiState.Empty -> {
                        EmptyHomeScreen(
                            gifEnabledLoader = gifEnabledLoader
                        )

                        /*TokenCardCarousel(
                            modifier = Modifier.padding(bottom = 24.dp),
                            assets = testTokenAssets,
                            tokenData = tokenData,
                            tokenMetadata = tokenMetadata,
                            loadSymbol = loadSymbol,
                            navigateToSend = navigateToSend,
                            selectedTokenUiState = selectedTokenUiState,
                            setSelectedToken = setSelectedTokenId,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedContentScope = animatedContentScope,
                        )*/
                    }
                    is AssetsUiState.Error -> {
                        ErrorHomeScreen(
                            gifEnabledLoader = gifEnabledLoader
                        )
                    }
                    is AssetsUiState.Loading -> {
                        LoadingHomeScreen()
                    }
                    is AssetsUiState.Success -> {
                        HomeScreenContent(
                            areAssetsVisible = assetState.assets.isNotEmpty() ,
                            primaryContent = {
                                TokenCardCarousel(
                                    modifier = Modifier.padding(bottom = 24.dp),
                                    assets = assetState.assets,
                                    tokenData = tokenData,
                                    tokenMetadata = tokenMetadata,
                                    loadSymbol = loadSymbol,
                                    navigateToSend = navigateToSend,
                                    selectedTokenUiState = selectedTokenUiState,
                                    setSelectedToken = setSelectedTokenId,
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedContentScope = animatedContentScope,
                                )
                            },
                            secondaryContent = {
                                Box(
                                    modifier = modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ){
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(0.dp),
                                        modifier = Modifier.offset(y= -48.dp)
                                    ) {
                                        AsyncImage(
                                            imageLoader = gifEnabledLoader,
                                            model = com.core.ui.R.drawable.wireframe_torus,
                                            contentDescription = null,
                                            modifier = Modifier.size(275.dp),
                                            colorFilter = ColorFilter.tint(dgenTurqoise.copy(0.35f))

                                        )
                                        Text(
                                            text = "Tap Buy to purchase your first token, or Receive to add assets from \n another wallet.",
                                            style = TextStyle(
                                                fontFamily = PitagonsSans,
                                                color = dgenTurqoise.copy(0.35f),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp,
                                                letterSpacing = 0.sp,
                                                textDecoration = TextDecoration.None,
                                                textAlign = TextAlign.Center
                                            ),
                                            modifier = Modifier.width(300.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()

        ) {
            // Fading border overlay (Green to Transparent)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp) // Adjust thickness of fading border
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, dgenBlack)
                        )
                    )

            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(dgenBlack)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {

                AnimatedVisibility(
                    hasTransfer,
                    enter = fadeIn(
                        animationSpec = tween(mediumEnterDuration,easing=FastOutSlowInEasing)
                    ),
                    exit = fadeOut(
                        animationSpec = tween(mediumExitDuration,easing=FastOutSlowInEasing)
                    )
                ){
                    IconButton(modifier = Modifier
                        .clip(RoundedCornerShape(0.dp))
                        .width(100.dp)
                        .height(50.dp)
                        .padding(bottom = 8.dp),
                        onClick = {
                            if (isOffline){
                                context.showCustomToast(
                                    message = "No internet connection!",
                                    fontFamily = PitagonsSans,
                                    fontWeight = FontWeight.SemiBold,
                                    backgroundColor = dgenRed,
                                    textColor = dgenWhite
                                )
                            } else {
                                navigateToLog(selectedTokenId.value)
                                //navigateToSend(selectedTokenId.value,selectedTokenId.value)
                            }
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.baseline_swap_vert_24),
                                contentDescription = "Back",
                                tint = dgenTurqoise
                            )
                            Text(
                                text= "LOG",
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = dgenTurqoise,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    lineHeight = 16.sp,
                                    letterSpacing = 1.sp,
                                    textDecoration = TextDecoration.None
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }


                IconButton(modifier = Modifier
                    .clip(RoundedCornerShape(0.dp))
                    .width(110.dp)
                    .height(50.dp)
                    .padding(bottom = 8.dp),
                    onClick = {
                        navigateToReceive()
                    }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(180f),
                            imageVector = Icons.Outlined.ArrowOutward,
                            contentDescription = "Back",
                            tint = dgenTurqoise
                        )
                        Text(
                            text= "RECEIVE",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = dgenTurqoise,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                letterSpacing = 1.sp,
                                textDecoration = TextDecoration.None
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(modifier = Modifier
                    .clip(RoundedCornerShape(0.dp))
                    .width(100.dp)
                    .height(50.dp)
                    .padding(bottom = 8.dp),
                    onClick = {
                        if (userData is WalletDataUiState.Success) {
                            val address = userData.userData.walletAddress
                            scope.launch {
                                val json = Uri.encode("{\"eth\":\"$address\"}")
                                getLink("https://buy.moonpay.com/?apiKey=pk_live_jzpq2k0QOfqab9kF1Nk75vjWfll4axA&walletAddresses=$json").let { uri ->
                                    println("Opening URI: $uri")
                                    uriHandler.openUri(uri)
                                }
                            }
                        }
                    }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Back",
                            tint = dgenTurqoise
                        )
                        Text(
                            text= "BUY",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = dgenTurqoise,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                letterSpacing = 1.sp,
                                textDecoration = TextDecoration.None
                            )
                        )
                    }
                }
            }
        }
    }

}

@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}


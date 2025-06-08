package com.feature.home

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
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
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.feature.send.SelectedTokenUiState
import com.feature.send.SendViewModel
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.core.ui.R
import com.core.ui.initializeFontMap
import com.core.ui.showCustomToast
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.dgenGunMetal
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenWhite
import com.example.dgenlibrary.ui.theme.extraLargeEnterDuration
import com.example.dgenlibrary.ui.theme.extraLargeExitDuration
import com.feature.home.screens.ErrorHomeScreen
import com.feature.home.screens.HomeScreenContent
import com.feature.home.screens.LoadingHomeScreen
import com.core.ui.BottomBar
import com.feature.home.screens.EmptyHomeScreen
import com.feature.home.screens.NoInternetHomeScreen
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
    navigateToPayMaster: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: HomeViewModel = hiltViewModel(),
    sendViewModel: SendViewModel = hiltViewModel()

) {
    val walletDataUiState: WalletDataUiState by viewModel.walletDataState.collectAsStateWithLifecycle()
    val assetsUiState: AssetsUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val selectedTokenUiState: SelectedTokenUiState by sendViewModel.selectedAssetUiState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

    val selectedTokenId = sendViewModel.selectedTokenIdFlow.collectAsState()


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
        loadSymbol = {}, // not needed anymore
        getLink = viewModel::getLink,
        hasTransfer = hasTransfer,
        navigateToPayMaster = navigateToPayMaster

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
    navigateToPayMaster: () -> Unit,
    selectedTokenUiState: SelectedTokenUiState,
    selectedTokenId: State<String>,
    setSelectedTokenId: (String) -> Unit,
    isOffline: Boolean,
    loadSymbol: (List<String>) -> Unit,
    hasTransfer: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    getLink: KSuspendFunction1<String, String?>,
    modifier: Modifier = Modifier,
) {
    // Log HomeScreen2 recomposition
    SideEffect {
        Log.d("RECOMPOSE", "HomeScreen2 recomposed")
        Log.d("RECOMPOSE", "HomeScreen2 - userData: $userData")
        Log.d("RECOMPOSE", "HomeScreen2 - assetsUiState: ${assetsUiState::class.simpleName}")
        Log.d("RECOMPOSE", "HomeScreen2 - hasTransfer: $hasTransfer")
        Log.d("RECOMPOSE", "HomeScreen2 - isOffline: $isOffline")
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val gifEnabledLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if ( SDK_INT >= 28 ) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.build()
    }

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
                targetState = assetsUiState,
                contentKey = { state ->
                    // Provide a stable key based on the *type* of state
                    when (state) {
                        is AssetsUiState.Success -> "SuccessState"
                        is AssetsUiState.Empty -> "EmptyState"
                        is AssetsUiState.Error -> "ErrorState"
                        is AssetsUiState.Loading -> "LoadingState"
                    }
                },
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(extraLargeEnterDuration)
                    ) togetherWith fadeOut(animationSpec = tween(extraLargeExitDuration))
                },
                modifier = Modifier.fillMaxSize(),
                label = "Animated Content Assets"
            ) { assetState ->
                // Log state changes
                SideEffect {
                    Log.d(
                        "RECOMPOSE",
                        "AnimatedContent - assetState changed to: ${assetState::class.simpleName}"
                    )
                }

                if (isOffline) {
                    NoInternetHomeScreen(
                        gifEnabledLoader = gifEnabledLoader
                    )
                } else {
                    when(assetState){
                        is AssetsUiState.Empty -> {
                            EmptyHomeScreen(
                                gifEnabledLoader = gifEnabledLoader
                            )
                            Log.d("DEBUG","AssetsUiState.EMPTY")
                        }
                        is AssetsUiState.Error -> {
                            Log.d("DEBUG","AssetsUiState.ERROR")
                            ErrorHomeScreen(
                                gifEnabledLoader = gifEnabledLoader
                            )
                        }
                        is AssetsUiState.Loading -> {
                            Log.d("DEBUG","AssetsUiState.LOADING")
                            LoadingHomeScreen()
                        }
                        is AssetsUiState.Success -> {
                            Log.d("DEBUG","AssetsUiState.SUCCESS")
                            Log.d(
                                "RECOMPOSE",
                                "Success state - assets count: ${assetState.assets.size}"
                            )
                            HomeScreenContent(
                                areAssetsVisible = assetState.assets.isNotEmpty() ,
                                primaryContent = {
                                    TokenCardCarousel(
                                        modifier = Modifier.padding(bottom = 24.dp),
                                        assets = assetState.assets,
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
                                                model = R.drawable.wireframe_torus,
                                                contentDescription = null,
                                                modifier = Modifier.size(275.dp),
                                                colorFilter = ColorFilter.tint(dgenGunMetal)
                                            )
                                            Text(
                                                text = "Tap Buy to purchase your first token, or Receive to add assets from \n another wallet.",
                                                style = TextStyle(
                                                    fontFamily = PitagonsSans,
                                                    color = dgenGunMetal,
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // Adjust thickness of fading border
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(dgenBlack, Color.Transparent)
                        )
                    )

            )

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

            BottomBar(
                hasTransfer,
                navigateToLog = {
                    if (isOffline) {
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
                },
                navigateToReceive = {
                    navigateToReceive()
                },
                navigateToBuy = {
                    if (userData is WalletDataUiState.Success) {
                        val address = userData.userData.walletAddress
                        scope.launch {
                            val json = Uri.encode("{\"eth\":\"$address\"}")
                            getLink("https://buy.moonpay.com/?apiKey=pk_live_jzpq2k0QOfqab9kF1Nk75vjWfll4axA&walletAddresses=$json")?.let { uri ->
                                println("Opening URI: $uri")
                                uriHandler.openUri(uri)
                            }
                        }
                    }
                },
                navigateToPayMaster = {
                    navigateToPayMaster()

                }
            )
        }
    }

}

@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}


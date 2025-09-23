package com.feature.home

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.core.ui.util.SystemColorManager
import com.feature.send.SendViewModel
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.core.terminalsdk.TerminalLEDController
import com.core.ui.R
import com.core.ui.initializeFontMap
import com.feature.home.screens.ErrorHomeScreen
import com.feature.home.screens.HomeScreenContent
import com.feature.home.screens.LoadingHomeScreen
import com.core.ui.BottomBar
import com.core.ui.showDgenToast
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenBlack
import com.core.ui.util.extraLargeEnterDuration
import com.core.ui.util.extraLargeExitDuration
import com.core.ui.util.neonOpacity
import com.core.ui.util.pulseOpacity
import com.core.ui.util.rememberDebouncedClickHandler
import com.feature.home.screens.EmptyHomeScreen
import com.feature.home.screens.NoInternetHomeScreen
import com.feature.home.ui.TokenCardCarousel
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction1


@OptIn(ExperimentalSharedTransitionApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
internal fun HomeRoute2(
    modifier: Modifier = Modifier,
    navigateToSwap: () -> Unit,
    navigateToSend: (groupId: String) -> Unit,
    navigateToLog: () -> Unit,
    navigateToReceive: () -> Unit,
    navigateToPayMaster: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val groupedAssetsUiState: GroupedAssetsUiState by viewModel.groupedTokenAssetState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val hasTransfer by viewModel.hasTransfers.collectAsState()
    initializeFontMap(SpaceMono, PitagonsSans)

    HomeScreen2(
        groupedAssetsUiState = groupedAssetsUiState,
        navigateToSwap = navigateToSwap,
        navigateToSend = navigateToSend,
        navigateToLog = navigateToLog,
        navigateToReceive = navigateToReceive,
        isOffline = isOffline,
        hasTransfer = hasTransfer,
        navigateToPayMaster = navigateToPayMaster
    )
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen2(
    groupedAssetsUiState: GroupedAssetsUiState,
    navigateToSwap: () -> Unit,
    navigateToSend: (groupId: String) -> Unit,
    navigateToLog: () -> Unit,
    navigateToReceive: () -> Unit,
    navigateToPayMaster: () -> Unit,
    isOffline: Boolean,
    hasTransfer: Boolean,
    modifier: Modifier = Modifier,
) {
    // Log HomeScreen2 recomposition
    SideEffect {
        Log.d("RECOMPOSE", "HomeScreen2 recomposed")
        Log.d("RECOMPOSE", "HomeScreen2 - assetsUiState: ${groupedAssetsUiState::class.simpleName}")
        Log.d("RECOMPOSE", "HomeScreen2 - hasTransfer: $hasTransfer")
        Log.d("RECOMPOSE", "HomeScreen2 - isOffline: $isOffline")
    }

    val context = LocalContext.current

    val debouncedClickHandler = rememberDebouncedClickHandler(intervalMillis = 300L)

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

    LaunchedEffect(Unit) {
        SystemColorManager.refresh(context)
    }

    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    Box (
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {


        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
            //.weight(1f) // Allows it to take up remaining space

        ) {

            AnimatedContent(
                targetState = groupedAssetsUiState,
                contentKey = { state ->
                    // Provide a stable key based on the *type* of state
                    when (state) {
                        is GroupedAssetsUiState.Success -> "SuccessState"
                        is GroupedAssetsUiState.Empty -> "EmptyState"
                        is GroupedAssetsUiState.Error -> "ErrorState"
                        is GroupedAssetsUiState.Loading -> "LoadingState"
                    }
                },
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(extraLargeEnterDuration)
                    ) togetherWith fadeOut(animationSpec = tween(extraLargeExitDuration))
                },
                modifier = Modifier.fillMaxSize(),
                label = "Animated Content Assets"
            ) { groupedAssetsState ->
                // Log state changes
                SideEffect {
                    Log.d(
                        "RECOMPOSE",
                        "AnimatedContent - assetState changed to: ${groupedAssetsState::class.simpleName}"
                    )
                }

                if (isOffline) {
                    NoInternetHomeScreen(
                        gifEnabledLoader = gifEnabledLoader,
                        primaryColor = primaryColor
                    )
                } else {
                    when(groupedAssetsState){
                        is GroupedAssetsUiState.Empty -> {
                            EmptyHomeScreen(
                                gifEnabledLoader = gifEnabledLoader,
                                primaryColor = primaryColor
                            )
                            Log.d("DEBUG","AssetsUiState.EMPTY")
                        }
                        is GroupedAssetsUiState.Error -> {
                            Log.d("DEBUG","AssetsUiState.ERROR")
                            ErrorHomeScreen(
                                gifEnabledLoader = gifEnabledLoader,
                                primaryColor = primaryColor
                            )
                        }
                        is GroupedAssetsUiState.Loading -> {
                            Log.d("DEBUG","AssetsUiState.LOADING")
                            LoadingHomeScreen(
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                            )
                        }
                        is GroupedAssetsUiState.Success -> {
                            Log.d("DEBUG","AssetsUiState.SUCCESS")
                            Log.d(
                                "RECOMPOSE",
                                "Success state - assets count: ${groupedAssetsState.assets.size}"
                            )
                            HomeScreenContent(
                                areAssetsVisible = groupedAssetsState.assets.isNotEmpty() ,
                                primaryContent = {
                                    TokenCardCarousel(
                                        modifier = Modifier.padding(bottom = 24.dp),
                                        assets = groupedAssetsState.assets,
                                        navigateToSend = navigateToSend,
                                        primaryColor = primaryColor,
                                        secondaryColor = secondaryColor
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
                                                colorFilter = ColorFilter.tint(primaryColor.copy(pulseOpacity))
                                            )
                                            Text(
                                                text = "Tap Buy to purchase your first token, or Receive to add assets from \n another wallet.",
                                                style = TextStyle(
                                                    fontFamily = PitagonsSans,
                                                    color = primaryColor.copy(neonOpacity),
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
                        showDgenToast(
                            context,
                            message = "No internet connection!",
                        )
                    } else {
                        navigateToLog()
                        //navigateToSend(selectedTokenId.value,selectedTokenId.value)
                    }
                },
                navigateToReceive = {
                    debouncedClickHandler {
                        navigateToReceive()
                    }
                },
                navigateToBuy = {
                    debouncedClickHandler {
                        try {
                            val intent = Intent().apply {
                                setClassName(
                                    "org.ethosmobile.webpwaemul",              // WebPWA Emulator package
                                    "org.ethosmobile.webpwaemul.MainActivity"   // Main activity
                                )
                                data = Uri.parse("https://app.uniswap.org")                   // Pass the URL as data
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)         // Launch in new task
                            }

                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // If the specific app is not installed, open in default browser
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://app.uniswap.org"))
                                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(browserIntent)
                            } catch (ex: Exception) {
                                // Show error if no browser is available
                                showDgenToast(context, "Unable to open Uniswap")
                            }
                        }
                    }
                },
                navigateToPayMaster = {
                    navigateToPayMaster()
                },
                primaryColor = primaryColor
            )
        }
    }

}

@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}


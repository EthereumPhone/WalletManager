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
import com.core.model.NFT
import com.feature.home.screens.ErrorHomeScreen
import com.feature.home.screens.HomeScreenContent
import com.feature.home.screens.LoadingHomeScreen
import androidx.activity.compose.BackHandler
import com.core.model.TokenGroupAssetOverview
import com.core.ui.BottomBar
import com.core.ui.SelectionBottomBar
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
import com.feature.home.ui.AssetPager
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
    navigateToSendNft: (contractAddress: String, tokenId: String, chainId: Int) -> Unit = { _, _, _ -> },
    navigateToLog: () -> Unit,
    navigateToReceive: () -> Unit,
    navigateToPayMaster: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val groupedAssetsUiState: GroupedAssetsUiState by viewModel.groupedTokenAssetState.collectAsStateWithLifecycle()
    val nftUiState: NftUiState by viewModel.nftState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val hasTransfer by viewModel.hasTransfers.collectAsState()
    val selectedToken by viewModel.selectedTokenForAction.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val hiddenTokens by viewModel.hiddenTokensState.collectAsStateWithLifecycle()
    initializeFontMap(SpaceMono, PitagonsSans)

    HomeScreen2(
        groupedAssetsUiState = groupedAssetsUiState,
        nftUiState = nftUiState,
        navigateToSwap = navigateToSwap,
        navigateToSend = navigateToSend,
        navigateToSendNft = navigateToSendNft,
        navigateToLog = navigateToLog,
        navigateToReceive = navigateToReceive,
        isOffline = isOffline,
        hasTransfer = hasTransfer,
        navigateToPayMaster = navigateToPayMaster,
        isSelectionMode = isSelectionMode,
        selectedToken = selectedToken,
        hiddenTokens = hiddenTokens,
        onLongPressToken = viewModel::selectTokenForAction,
        onHideToken = viewModel::hideSelectedToken,
        onUnhideToken = viewModel::unhideSelectedToken,
        onCopyToken = { viewModel.getSelectedTokenAddress() },
        onClearSelection = viewModel::clearSelection,
    )
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen2(
    groupedAssetsUiState: GroupedAssetsUiState,
    nftUiState: NftUiState = NftUiState.Empty,
    navigateToSwap: () -> Unit,
    navigateToSend: (groupId: String) -> Unit,
    navigateToSendNft: (contractAddress: String, tokenId: String, chainId: Int) -> Unit = { _, _, _ -> },
    navigateToLog: () -> Unit,
    navigateToReceive: () -> Unit,
    navigateToPayMaster: () -> Unit,
    isOffline: Boolean,
    hasTransfer: Boolean,
    isSelectionMode: Boolean = false,
    selectedToken: TokenGroupAssetOverview? = null,
    hiddenTokens: List<TokenGroupAssetOverview> = emptyList(),
    onLongPressToken: (TokenGroupAssetOverview) -> Unit = {},
    onHideToken: () -> Unit = {},
    onUnhideToken: () -> Unit = {},
    onCopyToken: () -> String? = { null },
    onClearSelection: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // (Removed excessive recomposition logs)

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

    BackHandler(enabled = isSelectionMode) {
        onClearSelection()
    }

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
                // (Removed excessive AnimatedContent state change logs)

                if (isOffline) {
                    NoInternetHomeScreen(
                        gifEnabledLoader = gifEnabledLoader,
                        primaryColor = primaryColor
                    )
                } else {
                    when(groupedAssetsState){
                        is GroupedAssetsUiState.Empty -> {
                            if (hiddenTokens.isNotEmpty()) {
                                // All visible tokens are hidden — show AssetPager with only the Spam tab
                                val nfts = when (nftUiState) {
                                    is NftUiState.Success -> nftUiState.nfts
                                    else -> emptyList()
                                }
                                HomeScreenContent(
                                    areAssetsVisible = true,
                                    primaryContent = {
                                        AssetPager(
                                            modifier = Modifier.padding(bottom = 24.dp),
                                            tokens = emptyList(),
                                            nfts = nfts,
                                            hiddenTokens = hiddenTokens,
                                            navigateToSend = navigateToSend,
                                            navigateToSendNft = navigateToSendNft,
                                            onLongPressToken = onLongPressToken,
                                            isSelectionMode = isSelectionMode,
                                            onClearSelection = onClearSelection,
                                            primaryColor = primaryColor,
                                            secondaryColor = secondaryColor
                                        )
                                    },
                                    secondaryContent = {}
                                )
                            } else {
                                EmptyHomeScreen(
                                    gifEnabledLoader = gifEnabledLoader,
                                    primaryColor = primaryColor
                                )
                            }
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
                            // Filter out zero-balance tokens to avoid showing empty cards when returning
                            val nonZeroAssets = groupedAssetsState.assets.filter { it.totalBalance > 0.0 }
                            val nfts = when (nftUiState) {
                                is NftUiState.Success -> nftUiState.nfts
                                else -> emptyList()
                            }
                            HomeScreenContent(
                                areAssetsVisible = nonZeroAssets.isNotEmpty() || nfts.isNotEmpty() || hiddenTokens.isNotEmpty(),
                                primaryContent = {
                                    AssetPager(
                                        modifier = Modifier.padding(bottom = 24.dp),
                                        tokens = nonZeroAssets,
                                        nfts = nfts,
                                        hiddenTokens = hiddenTokens,
                                        navigateToSend = navigateToSend,
                                        navigateToSendNft = navigateToSendNft,
                                        onLongPressToken = onLongPressToken,
                                        isSelectionMode = isSelectionMode,
                                        onClearSelection = onClearSelection,
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

//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(48.dp) // Adjust thickness of fading border
//                    .align(Alignment.TopCenter)
//                    .background(
//                        brush = Brush.verticalGradient(
//                            colors = listOf(dgenBlack, Color.Transparent)
//                        )
//                    )
//
//            )

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

            AnimatedContent(
                targetState = isSelectionMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "bottomBarSwap"
            ) { inSelectionMode ->
                if (inSelectionMode) {
                    val isSelectedHidden = hiddenTokens.any { it.groupId == selectedToken?.groupId }
                    SelectionBottomBar(
                        onHide = {
                            if (isSelectedHidden) onUnhideToken() else onHideToken()
                        },
                        primaryColor = primaryColor,
                        hideLabel = if (isSelectedHidden) "Unhide" else "Hide"
                    )
                } else {
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
                            }
                        },
                        navigateToReceive = {
                            debouncedClickHandler {
                                navigateToReceive()
                            }
                        },
                        navigateToBuy = {
                            debouncedClickHandler {
                                navigateToSwap()
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
    }

}

@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}


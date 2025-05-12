package com.feature.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.core.model.TokenData
import com.core.model.TokenMetadata
import com.feature.home.ui.CardCarousel
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.feature.send.SelectedTokenUiState
import com.feature.send.SendViewModel
import kotlinx.coroutines.launch
import org.ethosmobile.components.library.core.ethOSSnackbarHost
import org.ethosmobile.components.library.utils.SnackbarState
import org.ethosmobile.components.library.utils.rememberSnackbarDelegate
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.dgenGray


@OptIn(ExperimentalSharedTransitionApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
internal fun HomeRoute2(
    modifier: Modifier = Modifier,
    navigateToSwap: () -> Unit,
    navigateToSend: (address: String, tokenId: String ) -> Unit,
    navigateToLog: (String) -> Unit,
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


    HomeScreen2(
        userData = walletDataUiState,
        assetsUiState = assetsUiState,
        navigateToSwap = navigateToSwap,
        navigateToSend = navigateToSend,
        navigateToLog = navigateToLog,
        selectedTokenUiState = selectedTokenUiState,
        selectedTokenId = selectedTokenId,
        setSelectedTokenId = sendViewModel::updateSelectedTokenId,
        isOffline = isOffline,
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        tokenData = tokenData,
        tokenMetadata = tokenMetadata,
        loadSymbol = viewModel::loadSymbol


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
    tokenMetadata:  List<TokenMetadata>,
    selectedTokenUiState: SelectedTokenUiState,
    selectedTokenId: State<String>,
    setSelectedTokenId: (String) -> Unit,
    isOffline: Boolean,
    tokenData:  List<TokenData>,
    loadSymbol: (List<String>) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
) {


    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val snackbarHostState = rememberSnackbarDelegate(hostState,scope)


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

            when(assetsUiState){
                AssetsUiState.Empty -> {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            modifier = Modifier.offset(y= 0.dp)
                        ) {
                            AsyncImage(
                                imageLoader = gifEnabledLoader,
                                model = com.core.ui.R.drawable.wireframe_torus,
                                contentDescription = null,
                                modifier = Modifier.size(275.dp),
                                colorFilter = ColorFilter.tint(dgenTurqoise.copy(0.5f))

                            )
                            Text(
                                text = "Tap Buy to purchase your first token, or Receive to add assets from another wallet.",
                                style = TextStyle(
                                    fontFamily = PitagonsSans,
                                    color = dgenTurqoise.copy(0.5f),
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.padding(horizontal = 64.dp)
                            )
                        }
                    }
                }
                AssetsUiState.Error -> {
                    Log.d("Assets", "Assets Error")

                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        
                    }
                }
                AssetsUiState.Loading -> {
                    Log.d("Assets", "Assets is loading")

                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Column(
                            modifier = modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "Loading...",
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = dgenTurqoise,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 24.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                ))
                        }
                  }
                }
                is AssetsUiState.Success -> {

                    Log.d("CardAnimation Assets", "${ tokenMetadata.size }")


                    if(assetsUiState.assets.isNotEmpty()){
                        CardCarousel(
                            modifier = Modifier.padding(bottom = 24.dp),
                            assets = assetsUiState.assets,
                            tokenData = tokenData,
                            tokenMetadata = tokenMetadata,
                            loadSymbol = loadSymbol,
                            navigateToSend = navigateToSend,
                            selectedTokenUiState = selectedTokenUiState,
                            setSelectedToken = setSelectedTokenId,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedContentScope = animatedContentScope,
                        )
                    }
                    else {
                        Log.d("Assets", "Assets Size ${assetsUiState.assets.size}")
                        Box(
                            modifier = modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(0.dp),
                                modifier = Modifier.offset(y= 0.dp)
                            ) {
                                AsyncImage(
                                    imageLoader = gifEnabledLoader,
                                    model = com.core.ui.R.drawable.wireframe_torus,
                                    contentDescription = null,
                                    modifier = Modifier.size(275.dp),
                                    colorFilter = ColorFilter.tint(dgenTurqoise.copy(0.5f))

                                )
                                Text(
                                    text = "Tap Buy to purchase your first token, or Receive to add assets from another wallet.",
                                    style = TextStyle(
                                        fontFamily = PitagonsSans,
                                        color = dgenTurqoise.copy(0.5f),
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.padding(horizontal = 64.dp)
                                )
                            }
                        }
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
                    .height(40.dp) // Adjust thickness of fading border
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

                IconButton(modifier = Modifier, onClick = {
                    if (isOffline){
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                state = SnackbarState.ERROR,
                                message = "You are offline!",
                                actionLabel = "UNDO",
                                duration = SnackbarDuration.Short
                            )
                        }
                    } else {
                        navigateToLog(selectedTokenId.value)
                        //navigateToSend(selectedTokenId.value,selectedTokenId.value)
                    }
                }) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.width(16.dp),
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
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            )
                        )
                    }

                }
                Spacer(modifier = Modifier.width(32.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = dgenTurqoise,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.Gray
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.width(IntrinsicSize.Max),
                    onClick = {

                        when(userData){
                            WalletDataUiState.Loading -> {
                                copyTextToClipboard(context,"Loading...")
                                Toast.makeText(context, "Failed to copy address", Toast.LENGTH_SHORT).show()

                            }
                            is WalletDataUiState.Success -> {
                                copyTextToClipboard(context, userData.userData.walletAddress)
                                Toast.makeText(context, "Copied Address", Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier
                                .width(16.dp)
                                .graphicsLayer {
                                    rotationZ = 90f
                                },
                            painter = painterResource(R.drawable.cpyaddress),
                            contentDescription = "Back",
                            tint = dgenTurqoise
                        )
                        Text(
                            text= "CPY ADD",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = dgenTurqoise,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            )
                        )
                    }

                }

            }
        }

        ethOSSnackbarHost(snackbarHostState, modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 80.dp))

    }

}

@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}


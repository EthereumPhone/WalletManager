package com.feature.home

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.core.ui.Card
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.feature.home.ui.CardCarousel
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGray
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import com.feature.send.AssetUiState
import com.feature.send.SelectedTokenUiState
import com.feature.send.SendViewModel
import com.feature.send.ui.ErrorCardView
import com.feature.send.ui.SendCardView
import kotlinx.coroutines.launch
import org.ethosmobile.components.library.core.ethOSSnackbarHost
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import org.ethosmobile.components.library.utils.SnackbarState
import org.ethosmobile.components.library.utils.rememberSnackbarDelegate


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
internal fun HomeRoute2(
    modifier: Modifier = Modifier,
    navigateToSwap: () -> Unit,
    navigateToSend: (address: String, tokenId: String ) -> Unit,
    navigateToLog: () -> Unit,
    isOffline: Boolean,
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


    val amount by sendViewModel.amount.collectAsStateWithLifecycle()
    val toAddress by sendViewModel.toAddress.collectAsStateWithLifecycle(initialValue = "")

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
        onAmountChange = sendViewModel::updateAmount,
        onToAddressChanged= sendViewModel::updateToAddress,
        sendTransaction = sendViewModel::send,
        toAddress = toAddress,
        amount = amount,


    )
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen2(
    userData: WalletDataUiState,
    assetsUiState: AssetsUiState,
    navigateToSwap: () -> Unit,
    navigateToSend: (address: String, tokenId: String ) -> Unit,
    navigateToLog: () -> Unit,
    selectedTokenUiState: SelectedTokenUiState,
    selectedTokenId: State<String>,
    setSelectedTokenId: (String) -> Unit,
    isOffline: Boolean,

    onAmountChange: (String) -> Unit,
    onToAddressChanged: (String) -> Unit,
    sendTransaction: (() -> Unit) -> Unit,
    toAddress: String,
    amount: String,

    modifier: Modifier = Modifier,
) {


    var showDetails by remember {
        mutableStateOf(false)
    }

    SharedTransitionLayout {
        AnimatedContent(
            showDetails,
            label = "basic_transition"
        ) { targetState ->
            if (!targetState) {
                HomeContent(
                    userData = userData,
                    assetsUiState =assetsUiState,
                    navigateToLog=navigateToLog,
                    selectedTokenUiState =selectedTokenUiState,
                    selectedTokenId = selectedTokenId,
                    setSelectedTokenId = setSelectedTokenId,
                    isOffline = isOffline,
                    animatedVisibilityScope = this@AnimatedContent,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    onChange = {
                        showDetails = true
                    }
                )
            } else {

                        TestAnimated(
                            assets = assetsUiState,
                            toAddress = toAddress,
                            amount = amount,
                            onBackClick = {
                                showDetails = false
                            },
                            onAmountChange = onAmountChange,
                            onToAddressChanged = onToAddressChanged,
                            sendTransaction = sendTransaction,
                            tokenId = selectedTokenId.value,
                            initialAddress = selectedTokenId.value,
                            animatedVisibilityScope = this@AnimatedContent,
                            sharedTransitionScope = this@SharedTransitionLayout
                        )

                }

            }
        }
}




@OptIn(ExperimentalSharedTransitionApi::class)
val boundsTransform = BoundsTransform { initialBounds, targetBounds ->
    spring(stiffness = Spring.StiffnessMediumLow)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TestAnimated(
    modifier: Modifier = Modifier,
    assets: AssetsUiState,
    toAddress: String,
    amount: String,
    onBackClick: () -> Unit,
    onAmountChange: (String) -> Unit,
    onToAddressChanged: (String) -> Unit,
    sendTransaction: (() -> Unit) -> Unit,
    tokenId: String?,
    initialAddress: String?,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {

            with(sharedTransitionScope) {
                Card(
                    modifier = Modifier.sharedBounds(
                        rememberSharedContentState(key = "token-${tokenId}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        boundsTransform = boundsTransform
                    ),

                    frontSide = {
                        when (assets) {

                            AssetsUiState.Empty -> {
//                            ErrorCardView()
                            }

                            AssetsUiState.Error -> {
                                ErrorCardView()
                            }

                            AssetsUiState.Loading -> {
//                            ErrorCardView()
                            }

                            is AssetsUiState.Success -> {
                                val token = assets.assets.firstOrNull {
                                    it.address.equals(initialAddress, ignoreCase = true)
                                }

                                Log.d("SendID", "add- ${initialAddress}")
                                if (token == null) {
                                    Log.d("SendID", "token null ")
                                }

                                if (token != null) {
                                    SendCardView(
                                        amount = amount,
                                        toAddress = toAddress,
                                        maxamount = token.balance,
                                        tokenName = token.symbol,
                                        onAddressChange = onAmountChange,
                                        onAmountChange = onToAddressChanged
                                    )
                                }
                            }
                        }
                    }

                )
            }


            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {

                IconButton(
                    modifier = modifier.size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = dgenRed,
                        disabledContainerColor = dgenGray,
                        disabledContentColor = dgenBlack
                    ),
                    onClick =  onBackClick,
                ){
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(com.feature.send.R.drawable.baseline_close_24),
                        contentDescription = "Send Icon",
                        tint = dgenRed
                    )
                }
                IconButton(
                    modifier = modifier.size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = dgenTurqoise,
                        disabledContainerColor = dgenGray,
                        disabledContentColor = dgenBlack
                    ),
                    onClick = {
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
                                if (token != null) {
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
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(com.feature.send.R.drawable.baseline_arrow_outward_24),
                        contentDescription = "Send Icon"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    userData: WalletDataUiState,
    assetsUiState: AssetsUiState,
    navigateToLog: () -> Unit,
    selectedTokenUiState: SelectedTokenUiState,
    selectedTokenId: State<String>,
    setSelectedTokenId: (String) -> Unit,
    isOffline: Boolean,
    onChange: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
){

    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val snackbarHostState = rememberSnackbarDelegate(hostState,scope)

    val context = LocalContext.current

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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                modifier = Modifier.size(82.dp),
                                contentScale = ContentScale.Fit,
                                painter = painterResource(id = R.drawable.wallet_icon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(dgenTurqoise)
                            )
                            Text(
                                text = "No assets".uppercase(),
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = dgenTurqoise,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 24.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                )
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                modifier = Modifier.size(82.dp),
                                contentScale = ContentScale.Crop,
                                painter = painterResource(id = com.core.ui.R.drawable.baseline_error_outline_24),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Colors.GRAY)
                            )
                            Text(text = "Error",
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
                    Log.d("Assets", assetsUiState.assets.toString())
                    if(assetsUiState.assets.isNotEmpty()){
                        Log.d("Assets", "Assets is not Empty")
                        CardCarousel(
                            modifier = Modifier.padding(bottom = 24.dp),
                            assets = assetsUiState.assets,
                            selectedTokenUiState = selectedTokenUiState,
                            setSelectedToken = setSelectedTokenId,
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope

                        )
                    }else {
                        Log.d("Assets", "Assets Size ${assetsUiState.assets.size}")
                        Box(
                            modifier = modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    modifier = Modifier.size(82.dp),
                                    contentScale = ContentScale.Fit,
                                    painter = painterResource(id = R.drawable.wallet_icon),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(dgenTurqoise)
                                )
                                Text(
                                    text = "No Assets".uppercase(),
                                    style = TextStyle(
                                        fontFamily = SpaceMono,
                                        color = dgenTurqoise,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 24.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None
                                    )
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
                when(assetsUiState){
                    AssetsUiState.Empty -> {

                    }
                    AssetsUiState.Error -> {

                    }
                    AssetsUiState.Loading -> {

                    }
                    is AssetsUiState.Success -> {
                        if(assetsUiState.assets.isNotEmpty()){
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
                                    Log.d("SendID","Home ${selectedTokenId.value} ")
                                    //Toast.makeText(context, "Token ${selectedTokenId.value}", Toast.LENGTH_SHORT).show()
                                    //navigateToSend(selectedTokenId.value,selectedTokenId.value)

                                    onChange()
                                }


                            }) {

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        modifier = Modifier.width(16.dp),
                                        painter = painterResource(R.drawable.baseline_arrow_outward_24),
                                        contentDescription = "Back",
                                        tint = dgenTurqoise
                                    )
                                    Text(
                                        text= "SEND",
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
                        }

                    }
                }

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
                        navigateToLog()
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
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}

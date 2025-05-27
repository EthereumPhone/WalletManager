package com.feature.send

import android.util.Log
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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

@OptIn(ExperimentalSharedTransitionApi::class)
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

                Log.d("CardBounds Send", "token-${initialAddress}")

                Card(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            rotationY = rotation
                            translationY = -translateY
                            cameraDistance = 12f * density
                        }

                    ,
                    frontSide = {
                        AnimatedContent(
                            assets,
                            transitionSpec = {
                                fadeIn(
                                    animationSpec = tween(extraLargeEnterDuration)
                                ) togetherWith fadeOut(animationSpec = tween(extraLargeExitDuration))
                            },
                            modifier = Modifier.fillMaxSize(),
                            label = "Animated Content"
                        ) { assetsState ->
                            when(assetsState){
                                AssetsUiState.Empty -> {
                                    Log.d("DEBUG","Empty")
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(24.dp)
                                        ) {
                                            DgenLoadingMatrix(
                                                size = 88.dp,
                                                LEDSize = 24.dp,
                                                unactiveLEDColor = dgenBlack.copy(0.15f),
                                                activeLEDColor = dgenTurqoise
                                            )
                                            Text(
                                                text = "NO ASSETS",
                                                style = TextStyle(
                                                    fontFamily = PitagonsSans,
                                                    color = dgenTurqoise,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 24.sp,
                                                    letterSpacing = 0.sp,
                                                    textDecoration = TextDecoration.None,
                                                    textAlign = TextAlign.Center
                                                )
                                            )
                                        }
                                    }
                                }
                                AssetsUiState.Error -> {
                                    //ErrorCardView()
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(24.dp)
                                        ) {
                                            DgenLoadingMatrix(
                                                size = 88.dp,
                                                LEDSize = 24.dp,
                                                unactiveLEDColor = dgenBlack.copy(0.15f),
                                                activeLEDColor = dgenTurqoise
                                            )
                                            Text(
                                                text = "ERROR",
                                                style = TextStyle(
                                                    fontFamily = PitagonsSans,
                                                    color = dgenTurqoise,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 24.sp,
                                                    letterSpacing = 0.sp,
                                                    textDecoration = TextDecoration.None,
                                                    textAlign = TextAlign.Center
                                                )
                                            )
                                        }
                                    }
                                }
                                AssetsUiState.Loading -> {
                                    Log.d("DEBUG","Loading")
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(24.dp)
                                        ) {
                                            DgenLoadingMatrix(
                                                size = 88.dp,
                                                LEDSize = 24.dp,
                                                unactiveLEDColor = dgenBlack.copy(0.15f),
                                                activeLEDColor = dgenTurqoise
                                            )
                                            Text(
                                                text = "LOADING...",
                                                style = TextStyle(
                                                    fontFamily = PitagonsSans,
                                                    color = dgenTurqoise,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 24.sp,
                                                    letterSpacing = 0.sp,
                                                    textDecoration = TextDecoration.None,
                                                    textAlign = TextAlign.Center
                                                )
                                            )
                                        }
                                    }
                                }
                                is AssetsUiState.Success -> {
                                    Log.d("DEBUG","Success")
                                    val token = assetsState.assets.firstOrNull {
                                        it.address.equals(tokenId, ignoreCase = true)
                                    }

                                    if(token == null){
                                        Log.d("SendID","token null ")
                                    }

                                    val tokenName = if (token?.name == token?.symbol)  "ETH-${token?.symbol}" else token?.symbol


                                    if (token != null) {
                                        when (token.symbol) {
                                            "base" -> {
                                                //loadSymbol(listOf("ETH"))
                                            }

                                            "mainnet" -> {
                                                //loadSymbol(listOf("ETH"))
                                            }

                                            else -> {
                                                //loadSymbol(listOf(token.symbol))
                                            }
                                        }

                                        val fiatamount = when (token.symbol) {
                                            "base" -> {
                                                //get eth value
                                                val tokenasset = tokenData.find { it.symbol == "ETH" }
                                                //set eth value
                                                if (tokenasset == null) {
                                                    0.0
                                                } else {
                                                    tokenasset.prices?.get(0)?.value?.toDouble()
                                                }
                                            }

                                            "mainnet" -> {
                                                //get eth value
                                                val tokenasset = tokenData.find { it.symbol == "ETH" }
                                                //set eth value
                                                //if tokenasset null turn into 0.00
                                                if (tokenasset == null) {
                                                    0.0
                                                } else {
                                                    tokenasset.prices?.get(0)?.value?.toDouble()
                                                }
                                            }

                                            else -> {
                                                //get eth value
                                                val tokenasset = tokenData.find { it.symbol == token.symbol }
                                                //set eth value
                                                if (tokenasset == null) {
                                                    0.0
                                                } else {
                                                    tokenasset.prices?.get(0)?.value?.toDouble()
                                                }
                                            }
                                        }


                                        Log.d("DEBUG","tokenName $tokenName")
                                        if (tokenName != null) {
                                            SendCardView(
                                                amount = amount,
                                                toAddress = toAddress,
                                                maxamount = token.balance,
                                                tokenName = tokenName.uppercase(),
                                                onAddressChange = onToAddressChanged,
                                                onAmountChange = onAmountChange
                                            )
                                        } else {

                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(24.dp)
                                                ) {
                                                    DgenLoadingMatrix(
                                                        size = 88.dp,
                                                        LEDSize = 24.dp,
                                                        unactiveLEDColor = dgenBlack.copy(0.15f),
                                                        activeLEDColor = dgenTurqoise
                                                    )
                                                    Text(
                                                        text = "TOKENNAME IS NULL",
                                                        style = TextStyle(
                                                            fontFamily = PitagonsSans,
                                                            color = dgenTurqoise,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 24.sp,
                                                            letterSpacing = 0.sp,
                                                            textDecoration = TextDecoration.None,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } else {

                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(24.dp)
                                            ) {
                                                DgenLoadingMatrix(
                                                    size = 88.dp,
                                                    LEDSize = 24.dp,
                                                    unactiveLEDColor = dgenBlack.copy(0.15f),
                                                    activeLEDColor = dgenTurqoise
                                                )
                                                Text(
                                                    text = "NON EXISTING TOKEN",
                                                    style = TextStyle(
                                                        fontFamily = PitagonsSans,
                                                        color = dgenTurqoise,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 24.sp,
                                                        letterSpacing = 0.sp,
                                                        textDecoration = TextDecoration.None,
                                                        textAlign = TextAlign.Center
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    rotation = rotation,
                    backSide = {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationY = -180f
                                },
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                "SEND",
                                style = TextStyle(
                                    fontFamily = PitagonsSans,
                                    color = dgenWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 128.sp,
                                    lineHeight = 128.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                ),
                            )
                        }
                    },
                )




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
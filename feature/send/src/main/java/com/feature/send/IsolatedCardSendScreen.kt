package com.feature.send

import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.State
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
import androidx.compose.runtime.getValue
import com.core.model.TokenAsset
import com.core.ui.Card
import com.feature.send.ui.ErrorCardView
import com.feature.send.ui.SendCardView
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGray
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite

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
        txComplete = txComplete,
        tokenId = tokenId,
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,

    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SendScreen2(
    modifier: Modifier = Modifier,
    toAddress: String,
    amount: String,
    walletDataUiState: WalletDataUiState,
    assets: AssetUiState,
    onAmountChange: (String) -> Unit,
    onToAddressChanged: (String) -> Unit,
    sendTransaction: (() -> Unit) -> Unit,
    selectedToken: SelectedTokenUiState,
    txComplete: TxCompleteUiState,
    onBackClick: () -> Unit,
    initialAddress: String?,
    tokenId: String?,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
){

    var rotated by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    var isAnimating by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 0.7f else 0.85f,
        animationSpec = tween(600, 200),
        label = "ScaleAnimation"
    )
    var translateY by remember { mutableStateOf(0f) }

    Log.d("CardAnimation","tokenId: ${tokenId} - initialAddress: ${initialAddress} ")



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
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            rotationY = rotation
                            translationY = -translateY
                            cameraDistance = 12f * density
                        }
                        .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(key = "token-${initialAddress}"),
                            animatedVisibilityScope = animatedContentScope
                        ),
                    frontSide = {

                        when(assets){

                            AssetUiState.Empty -> {
//                            ErrorCardView()
                            }
                            AssetUiState.Error -> {
                                ErrorCardView()
                            }
                            AssetUiState.Loading -> {
//                            ErrorCardView()
                            }
                            is AssetUiState.Success -> {
                                val token = assets.assets.firstOrNull {
                                    it.address.equals(initialAddress, ignoreCase = true)
                                }
                                Log.d("CardItem", "Assets: ${assets.assets} ")
                                Log.d("CardItem", "$token ")

                                Log.d("SendID","add- ${initialAddress}")
                                if(token == null){
                                    Log.d("SendID","token null ")
                                }

                                val tokenName = if (token?.name == token?.symbol)  "ETH-${token?.symbol}" else token?.symbol

                                if (token != null) {
                                    if (tokenName != null) {
                                        SendCardView(
                                            amount = amount,
                                            toAddress = toAddress,
                                            maxamount = token.balance,
                                            tokenName = tokenName.uppercase(),
                                            onAddressChange = onAmountChange,
                                            onAmountChange = onToAddressChanged
                                        )
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
                        painter = painterResource(R.drawable.baseline_close_24),
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

                            AssetUiState.Empty -> {

                            }
                            AssetUiState.Error -> {

                            }
                            AssetUiState.Loading -> {

                            }
                            is AssetUiState.Success -> {
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
                        painter = painterResource(R.drawable.baseline_arrow_outward_24),
                        contentDescription = "Send Icon"
                    )
                }
            }
        }
    }

}
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import coil.compose.AsyncImage
import com.core.model.TokenData
import com.core.ui.Card
import com.core.ui.DgenBasicTextfield
import com.core.ui.DgenLoadingMatrix
import com.core.ui.DgenTextfield
import com.core.ui.HeaderBar
import com.core.ui.R
import com.feature.send.ui.SendCardView
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.body1_fontSize
import com.example.dgenlibrary.ui.theme.body2_fontSize
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGray
import com.example.dgenlibrary.ui.theme.dgenGreen
import com.example.dgenlibrary.ui.theme.dgenGunMetal
import com.example.dgenlibrary.ui.theme.dgenOrche
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import com.example.dgenlibrary.ui.theme.extraLargeEnterDuration
import com.example.dgenlibrary.ui.theme.extraLargeExitDuration
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.example.dgenlibrary.ui.theme.largeEnterDuration
import com.example.dgenlibrary.ui.theme.mediumEnterDuration
import com.feature.send.ui.SelectableCarousel
import com.feature.send.ui.TextToggle
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind

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
        txComplete = txComplete,
        tokenId = tokenId,
        tokenData = tokenData,
        loadSymbol = viewModel::loadSymbol

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
    tokenData:  List<TokenData>,
    loadSymbol: (List<String>) -> Unit,
){

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val view = LocalView.current


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

    Log.d("DEBUG","initialAddress: $initialAddress, tokenId: $tokenId")



    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var toValue by remember { mutableStateOf(TextFieldValue("")) }
    var showTokenAmount by remember { mutableStateOf(false) }




    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack),
        contentAlignment = Alignment.Center
    ) {

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
                            view = view
                        ){

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


                        }
                    }
//                    Box(
//                        modifier = modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ){
//                            Text(
//                                text = "EMPTY",
//                                style = TextStyle(
//                                    fontFamily = PitagonsSans,
//                                    color = dgenGunMetal,
//                                    fontWeight = FontWeight.SemiBold,
//                                    fontSize = 24.sp,
//                                    letterSpacing = 0.sp,
//                                    textDecoration = TextDecoration.None,
//                                    textAlign = TextAlign.Center
//                                ),
//                                modifier = Modifier.width(300.dp)
//                            )
//                    }
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
                    Column (
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = modifier
                            .fillMaxSize()
                            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                    ){
                        HeaderBar(content = {
                            Row {
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
                            }
                        }, onClick = onBackClick)

                        Column(

                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            Row(

                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Spacer(Modifier
                                    .offset(y = 5.dp)
                                    .height(77.dp)
                                    .width(8.dp)
                                    .background(dgenGray.copy(0.5f))
                                    .padding(end = 16.dp)
                                )
                                Column {
                                    Text(
                                        buildAnnotatedString {
                                            //append("Sent ")
                                            append("ETH")

                                            withStyle(
                                                style = SpanStyle(
                                                    fontFamily = PitagonsSans,
                                                    color = dgenTurqoise,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 17.sp,
                                                    letterSpacing = 0.sp,
                                                    textDecoration = TextDecoration.None
                                                )
                                            ) {
                                                append(" \$")
                                            }
                                        },
                                        fontFamily = SpaceMono,
                                        color = dgenTurqoise,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp,
                                        lineHeight = 18.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None,
                                        modifier = Modifier.offset(y=8.dp)

                                    )
                                    //TODO: Change Amount to Gas Amount
                                    Text(
                                        "\$206.19",
                                        fontFamily = PitagonsSans,
                                        color = dgenWhite,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 48.sp,
                                        lineHeight = 48.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None
                                    )
                                }
                            }

// Sample list
                            val sampleItems = listOf("Card A", "Card B", "Card C", "Card D")

                            // Preview state holder
                            var selected by remember { mutableStateOf<Int?>(null) }

                            SelectableCarousel(
                                items = sampleItems,
                                onItemSelected = { index -> selected = index }
                            )

                        }

                        DgenTextfield(
                            value = toValue,
                            onValueChange={ new -> toValue = new},
                            placeholder = {
                                Text(
                                    modifier = Modifier,
                                    text = "Address",
                                    style = TextStyle(
                                        fontFamily = PitagonsSans,
                                        color = dgenGray,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = body2_fontSize
                                    ),
                                )
                            },
                            keyboardtype =  KeyboardType.Text,
                            cursorWidth = 16.dp,
                            cursorHeight= 32.dp,
                            isAnyFieldFocused= remember { mutableStateOf(false) },
                            onEditDone = {},
                            view = view
                        ){
                            Text(
                                text = "Target Address".uppercase(),
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = dgenTurqoise,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = label_fontSize,
                                    lineHeight = label_fontSize,
                                    letterSpacing = 1.sp,
                                    textDecoration = TextDecoration.None
                                ),
                                color = dgenTurqoise
                            )
                        }


                    }
                }
            }

        }

    }

}
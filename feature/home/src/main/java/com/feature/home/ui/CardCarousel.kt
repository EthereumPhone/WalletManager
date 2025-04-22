package com.feature.home.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.core.model.TokenAsset
import com.core.model.TokenData
import com.core.model.TokenMetadata
import com.core.ui.Card
import com.core.ui.views.IdleView
import com.feature.send.SelectedTokenUiState
import kotlin.math.abs

@OptIn(ExperimentalSharedTransitionApi::class)
@SuppressLint("RestrictedApi")
@Composable
fun CardCarousel(
    assets: List<TokenAsset>,
    tokenData: List<TokenData>,
    tokenMetadata:  List<TokenMetadata>,
    loadSymbol: (List<String>) -> Unit,
    selectedTokenUiState: SelectedTokenUiState,
    sharedTransitionScope: SharedTransitionScope,
    navigateToSend: (address: String, tokenId: String) -> Unit,
    animatedContentScope: AnimatedContentScope,
    setSelectedToken: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = assets.lastIndex)

    val listofTokenSymbol = remember { mutableListOf<String>() }




    // Ensure scrolling starts at the last item
    LaunchedEffect(Unit) {

        if (assets.isNotEmpty()){
            val token = when(selectedTokenUiState){
                is SelectedTokenUiState.Unselected -> {
                    assets.last()
                }

                is SelectedTokenUiState.Selected -> {
                    selectedTokenUiState.tokenAsset
                }
            }
            val index = assets.indexOf(token)
            listState.scrollToItem(index)
            //setSelectedToken(token)

            //collect all token symbols
            for (asset in assets){
                when(asset.symbol){
                    "base" -> {
                        if(!listofTokenSymbol.contains("ETH")){
                            Log.d("Fetch Card", "base added")
                            listofTokenSymbol.add("ETH")
                        }
                    }
                    "arbitrum" -> {
                        if(!listofTokenSymbol.contains("ETH")){
                            Log.d("Fetch Card", "mainnet added")

                            listofTokenSymbol.add("ETH")
                        }
                    }
                    "mainnet" -> {
                    if(!listofTokenSymbol.contains("ETH")){
                        Log.d("Fetch Card", "mainnet added")

                        listofTokenSymbol.add("ETH")
                    }
                }
                    "polygon" -> {
                        if(!listofTokenSymbol.contains("ETH")){
                            Log.d("Fetch Card", "mainnet added")

                            listofTokenSymbol.add("ETH")
                        }
                    }
                    "sepolia" -> {
                        if(!listofTokenSymbol.contains("ETH")){
                            Log.d("Fetch Card", "mainnet added")

                            listofTokenSymbol.add("ETH")
                        }
                    }
                    "optimism" -> {
                        if(!listofTokenSymbol.contains("ETH")){
                            Log.d("Fetch Card", "optimism added")

                            listofTokenSymbol.add("ETH")
                        }
                    }
                    "zora" -> {
                        if(!listofTokenSymbol.contains("ETH")){
                            Log.d("Fetch Card", "optimism added")

                            listofTokenSymbol.add("ETH")
                        }
                    }
                    else -> {
                        Log.d("Fetch Card", "${asset.symbol} added")
                        listofTokenSymbol.add(asset.symbol)
                    }
                }
            }


            //load token price based of token list
            Log.d("tokendata effect", "Token: $listofTokenSymbol")
            for (token in listofTokenSymbol){
                Log.d("tokendata effect loop", "Token: $token")
            }

            loadSymbol(listofTokenSymbol)

        }
    }



    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .zIndex(3f),
        verticalArrangement = Arrangement.spacedBy((-225).dp), // Overlapping effect
        contentPadding = PaddingValues(top = 100.dp, bottom = 100.dp) // Ensures enough space for scrolling
    ) {
        val firstVisibleIndex = listState.firstVisibleItemIndex
        
        itemsIndexed(assets) { index, item ->
            Log.d("SetToken", "${ item.address } - ${ item.symbol } - ${ item.name }")

            Log.d("SendID", "Token: $index - ${ item.name } - ${ item.address }")

            Log.d("tokendata", "Token: $tokenData")

            var enabled by remember { mutableStateOf(false) }

            val rotX: Float by animateFloatAsState ( -10f , label = "alpha")



            val scrollOffset = listState.firstVisibleItemIndex + listState.firstVisibleItemScrollOffset / 1000f
            val relativeIndex = (index - scrollOffset).coerceIn(-2f, 2f) // Keep relative index in a reasonable range


            // Animated values

            //val scaleFactor = lerp(0.9f, 0.5f, (abs(relativeIndex) / 5).coerceIn(0f, 1f))
            val scaleFactor by animateFloatAsState(
                targetValue = when {
                    abs(relativeIndex) <= 0.5f -> 0.8f  // Vorderste Karte mit konsistentem Skalierungsfaktor
                    abs(relativeIndex) <= 2f -> lerp(0.8f, 0.55f, (abs(relativeIndex) - 0.5f) / 1.5f)
                    else -> 0.55f
                },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )

            val alphaFactor by animateFloatAsState(
                targetValue = when {
                    abs(relativeIndex) <= 0.5f -> 1f
                    abs(relativeIndex) <= 2f -> lerp(1f, 0f, abs(relativeIndex) / 2f)
                    else -> 0f
                },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )

            val frontCardTranslation by animateFloatAsState(
                targetValue = lerp(0f, 800f, (relativeIndex / 2).coerceIn(0f, 1f)), // Reduced translation range
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )


            // Detect the front card
            val isFirstCard = index == firstVisibleIndex

            LaunchedEffect(isFirstCard) {
                if (isFirstCard) {
                    Log.d("FirstCard", "Karte mit Index $index ist jetzt die erste sichtbare")
                    // Weitere Logik, z.B. setSelectedToken(item.address) usw.
                    enabled = true
//                    Log.d("ADDRESS", "address ${item.address} - network ${item.chainId} - name ${item.name} - symbol ${item.symbol}")
                    setSelectedToken(item.address)
                } else {
                    enabled = false
                }
            }

            //Calculate fiat amount
            val fiatamount = when(item.symbol){
                "base" -> {
                    //get eth value
                   val tokenasset = tokenData.find { it.symbol == "ETH" }
                    //set eth value
                    if (tokenasset == null){
                        0.0
                    }else{
                        tokenasset.prices?.get(0)?.value?.toDouble()
                    }
                }
                "mainnet" -> {
                    //get eth value
                    val tokenasset = tokenData.find { it.symbol == "ETH" }
                    //set eth value
                    //if tokenasset null turn into 0.00
                    if (tokenasset == null){
                        0.0
                    }else{
                        tokenasset.prices?.get(0)?.value?.toDouble()
                    }
                }
                else -> {
                    //get eth value
                    val tokenasset = tokenData.find { it.symbol == item.symbol }
                    //set eth value
                    if (tokenasset == null){
                        0.0
                    }else{
                        tokenasset.prices?.get(0)?.value?.toDouble()
                    }
                }
            }

            val logoUrl = when(item.symbol) {
                "arbitrum" -> {"ETH"}
                "polygon" -> {"ETH"}
                "sepolia" -> {"ETH"}
                "mainnet" -> {"ETH"}
                "optimism" -> {"ETH"}
                "base" -> {"ETH"}
                "zora" -> {"ETH"}
                else -> {
                    tokenMetadata.firstOrNull() { item.symbol == it.symbol }?.logo ?: ""
                }
            }

            Log.d("fiatamount","${item.symbol} - $fiatamount")
            with(sharedTransitionScope) {
                Card(
                    isFirst = isFirstCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scaleFactor
                            scaleY = scaleFactor
                            alpha = alphaFactor
                            rotationX = rotX
                            translationY = frontCardTranslation
                            cameraDistance = 12f * density
                        }
                    ,
                    frontSide = {
                        val tokenName = if (item.name == item.symbol) "ETH-${item.symbol}" else item.symbol
                        //if (fiatamount != null) {
                            IdleView(
                                amount = item.balance,
                                tokenName = tokenName,
                                fiatAmount = item.balance * fiatamount!!,
                                icon = logoUrl,
                                navigateToSend = {
                                    navigateToSend(item.address, item.address)
                                },
                                enableSend = item.balance > 0


                            )
                       // }
                    },
                )
            }



        }
    }


}

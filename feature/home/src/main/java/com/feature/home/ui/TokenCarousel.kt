package com.feature.home.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.zIndex
import com.core.model.TokenAsset
import com.core.model.TokenData
import com.core.model.TokenMetadata
import com.core.ui.Card
import com.core.ui.views.IdleView
import com.feature.send.SelectedTokenUiState
import kotlin.collections.find
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalSharedTransitionApi::class)
@SuppressLint("RestrictedApi")
@Composable
fun TokenCardCarousel(
    assets: List<TokenAsset>,
    tokenData: List<TokenData>,
    tokenMetadata: List<TokenMetadata>,
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

    val cardHeight  = 400.dp
    val visibleCount= 5
    val overlap     = (-cardHeight / visibleCount) *4     // -50.dp
    val clampRange  = (visibleCount - 1).toFloat()     // 3f

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .zIndex(3f),
        verticalArrangement = Arrangement.spacedBy(overlap-35.dp), // Overlapping effect
        contentPadding = PaddingValues(top = 80.dp, bottom = 120.dp) // Ensures enough space for scrolling
    ) {
        val firstVisibleIndex = listState.firstVisibleItemIndex

        itemsIndexed(assets) { index, item ->
            Log.d("SetToken", "${ item.address } - ${ item.symbol } - ${ item.name }")

            Log.d("SendID", "Token: $index - ${ item.name } - ${ item.address }")

            Log.d("tokendata", "Token: $tokenData")



            var enabled by remember { mutableStateOf(false) }

            val rotX: Float by animateFloatAsState ( -25f , label = "rotX")

            val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

            val firstVisibleOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }

            val scrollOffset = firstVisibleIndex + firstVisibleOffset / 1000f

            val relIdx = (index - scrollOffset).coerceIn(-clampRange, clampRange)

            val scale by animateFloatAsState(
                targetValue = when {
                    abs(relIdx) <= 0.5f       -> 0.8f
                    abs(relIdx) <= clampRange -> lerp(0.8f, 0.55f, (abs(relIdx)-0.5f)/(clampRange-0.5f))
                    else                      -> 0.55f
                },
                tween(300, easing = FastOutSlowInEasing)
            )

            val alphafactor by animateFloatAsState(
                targetValue = when {
                    abs(relIdx) <= 0.5f       -> 1f
                    abs(relIdx) <= clampRange -> lerp(1f, 0f, (abs(relIdx)-0.5f)/(clampRange-0.5f))
                    else                      -> 0f
                },
                tween(300, easing = FastOutSlowInEasing)
            )

            val frontCardTranslation by animateFloatAsState(
                targetValue = lerp(0f, 800f, (relIdx / 2).coerceIn(0f, 1f)), // Reduced translation range
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
                        .height(cardHeight)
                        .aspectRatio(16f/9f)
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = alphafactor
                            rotationX = rotX
                            translationY = frontCardTranslation
                            cameraDistance = 32f * density
                        },
                    frontSide = {
                        val tokenName =
                            if (item.name == item.symbol) "ETH-${item.symbol}" else item.symbol
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


/*
NICE
@OptIn(ExperimentalSharedTransitionApi::class)
@SuppressLint("RestrictedApi")
@Composable
fun TokenCardCarousel(
    assets: List<TokenAsset>,
    tokenData: List<TokenData>,
    tokenMetadata: List<TokenMetadata>,
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

    val cardHeight  = 400.dp
    val visibleCount= 5
    val overlap     = (-cardHeight / visibleCount) *4     // -50.dp
    val clampRange  = (visibleCount - 1).toFloat()     // 3f

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .zIndex(3f),
        verticalArrangement = Arrangement.spacedBy(overlap-35.dp), // Overlapping effect
        contentPadding = PaddingValues(top = 80.dp, bottom = 80.dp) // Ensures enough space for scrolling
    ) {
        val firstVisibleIndex = listState.firstVisibleItemIndex

        itemsIndexed(assets) { index, item ->
            Log.d("SetToken", "${ item.address } - ${ item.symbol } - ${ item.name }")

            Log.d("SendID", "Token: $index - ${ item.name } - ${ item.address }")

            Log.d("tokendata", "Token: $tokenData")



            var enabled by remember { mutableStateOf(false) }

            val rotX: Float by animateFloatAsState ( -25f , label = "alpha")

            val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

            val firstVisibleOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }

            val scrollOffset = firstVisibleIndex + firstVisibleOffset / 1000f

            val relIdx = (index - scrollOffset).coerceIn(-clampRange, clampRange)

            val scale by animateFloatAsState(
                targetValue = when {
                    abs(relIdx) <= 0.5f       -> 0.8f
                    abs(relIdx) <= clampRange -> lerp(0.8f, 0.55f, (abs(relIdx)-0.5f)/(clampRange-0.5f))
                    else                      -> 0.55f
                },
                tween(300, easing = FastOutSlowInEasing)
            )

            val alphafactor by animateFloatAsState(
                targetValue = when {
                    abs(relIdx) <= 0.5f       -> 1f
                    abs(relIdx) <= clampRange -> lerp(1f, 0f, (abs(relIdx)-0.5f)/(clampRange-0.5f))
                    else                      -> 0f
                },
                tween(300, easing = FastOutSlowInEasing)
            )

            val frontCardTranslation by animateFloatAsState(
                targetValue = lerp(0f, 800f, (relIdx / 2).coerceIn(0f, 1f)), // Reduced translation range
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
                        .height(cardHeight)
                        .aspectRatio(16f/9f)
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = alphafactor
                            rotationX = rotX
                            translationY = frontCardTranslation
                            cameraDistance = 32f * density
                        },
                    frontSide = {
                        val tokenName =
                            if (item.name == item.symbol) "ETH-${item.symbol}" else item.symbol
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
 */


// Sample data model
/*data class MyData(val title: String)


@Composable
fun <T> FullscreenItemLazyColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: T, index: Int) -> Unit
) {
    // grab the current screen-height in dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    LazyColumn(
        modifier = modifier,
        // no extra padding so it really is one-per-screen
        contentPadding = PaddingValues(0.dp),
    ) {
        itemsIndexed(items) { index, item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // force each item to exactly the viewport height
                    .height(screenHeight)
            ) {
                itemContent(item, index)
            }
        }
    }
}

@Composable
fun MyScreen(myList: List<MyData>) {
    FullscreenItemLazyColumn(
        items = myList,
        modifier = Modifier.fillMaxSize(),
    ) { item, index ->
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            elevation = (8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Item #${index + 1}\n${item.title}",
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(
    device = "spec:width=720px,height=720px,dpi=240",
    name = "DDevice",
    showBackground = true
)
@Composable
fun PreviewFullscreenLazy() {
    // five silly items
    val sample = List(5) { MyData("Sample Title ${it + 1}") }
    MyScreen(sample)
}*/